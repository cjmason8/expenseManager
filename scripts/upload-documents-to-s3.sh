#!/usr/bin/env bash
#
# Migrate documents from the local filesystem to S3.
#
# Reads documents.csv with columns: id,filename,folderpath,isfolder
#   id         - document UUID (S3 object name for files)
#   filename   - original file name on disk (folders use directory name)
#   folderpath - /docs/... or any path containing /docs/ (e.g. ~/docs/..., ~/backups/docs/...)
#                S3 prefix is the path after /docs; local files use the path from the CSV when absolute
#   isfolder   - true/false (folders get S3 prefix markers only; files are uploaded as {prefix}/{id})
#
# Credentials and bucket are loaded from .env.local in the repo root (gitignored).
# Override with environment variables if needed.
#
# .env.local example:
#   AWS_ACCESS_KEY_ID=...
#   AWS_SECRET_ACCESS_KEY=...
#   AWS_SESSION_TOKEN=...          # optional
#   AWS_S3_REGION=ap-southeast-2   # optional
#   AWS_S3_BUCKET=expense-manager-documents-dev
#
# Environment:
#   AWS_S3_BUCKET          (default: expense-manager-documents-dev)
#   AWS_DEFAULT_REGION     (default: ap-southeast-2, or AWS_S3_REGION from .env.local)
#   DOCS_LOCAL_ROOT        (default: ~/docs) local root matching /docs
#   DOCUMENTS_CSV          (default: ~/documents.csv)
#   LOG_FILE               (default: ~/documents-upload.log)
#   ENV_FILE               (default: repo-root/.env.local)
#
# Usage:
#   ./scripts/upload-documents-to-s3.sh
#   ./scripts/upload-documents-to-s3.sh --dry-run
#   AWS_S3_BUCKET=other-bucket ./scripts/upload-documents-to-s3.sh
#
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="${ENV_FILE:-$REPO_ROOT/.env.local}"

load_env_file() {
	if [[ ! -f "$ENV_FILE" ]]; then
		return 0
	fi
	echo "Loading $ENV_FILE"
	set -a
	# shellcheck disable=SC1090
	source "$ENV_FILE"
	set +a
}

load_env_file

DRY_RUN=false
for arg in "$@"; do
	case "$arg" in
		--dry-run) DRY_RUN=true ;;
		-h|--help)
			sed -n '2,26p' "$0"
			exit 0
			;;
	esac
done

DOCUMENTS_CSV="${DOCUMENTS_CSV:-$HOME/documents.csv}"
LOG_FILE="${LOG_FILE:-$HOME/documents-upload.log}"
DOCS_LOCAL_ROOT="${DOCS_LOCAL_ROOT:-$HOME/docs}"
AWS_S3_BUCKET="expense-manager-documents-prod"
AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-${AWS_S3_REGION:-ap-southeast-2}}"
export AWS_S3_BUCKET AWS_DEFAULT_REGION

setup_logging() {
	{
		echo "========== $(date '+%Y-%m-%d %H:%M:%S') upload-documents-to-s3 =========="
		echo "Log file: $LOG_FILE"
		echo "CSV: $DOCUMENTS_CSV"
		echo "Bucket: $AWS_S3_BUCKET"
		echo "Region: $AWS_DEFAULT_REGION"
		echo "Dry run: $DRY_RUN"
		echo ""
	} > "$LOG_FILE"
	exec > >(tee -a "$LOG_FILE") 2>&1
}

setup_logging

if [[ ! -f "$DOCUMENTS_CSV" ]]; then
	echo "ERROR: CSV not found: $DOCUMENTS_CSV" >&2
	exit 1
fi

if ! command -v aws >/dev/null 2>&1; then
	echo "ERROR: aws CLI not found in PATH" >&2
	exit 1
fi

aws_cli() {
	aws "$@"
}

ensure_aws_auth() {
	if [[ -z "${AWS_ACCESS_KEY_ID:-}" || -z "${AWS_SECRET_ACCESS_KEY:-}" ]]; then
		echo "ERROR: AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY must be set in $ENV_FILE" >&2
		return 1
	fi

	export AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY
	if [[ -n "${AWS_SESSION_TOKEN:-}" ]]; then
		export AWS_SESSION_TOKEN
	fi
	# Prefer explicit keys from .env.local over any CLI profile
	unset AWS_PROFILE

	if ! aws_cli sts get-caller-identity >/dev/null 2>&1; then
		echo "ERROR: AWS credentials in $ENV_FILE are invalid or lack sts:GetCallerIdentity" >&2
		return 1
	fi

	echo "AWS authenticated: $(aws_cli sts get-caller-identity --query Arn --output text)"
	echo "Target bucket: $AWS_S3_BUCKET (region: $AWS_DEFAULT_REGION)"
}

if ! $DRY_RUN; then
	ensure_aws_auth || exit 1
fi

# Expand ~ in local docs root
DOCS_LOCAL_ROOT="${DOCS_LOCAL_ROOT/#\~/$HOME}"

EMPTY_FILE="$(mktemp)"
: > "$EMPTY_FILE"
trap 'rm -f "$EMPTY_FILE"' EXIT

normalize_s3_key() {
	local k="$1"
	k="${k//\\//}"
	k="$(echo "$k" | sed -E 's#/+#/#g')"
	while [[ "$k" == /* ]]; do k="${k:1}"; done
	while [[ "$k" == */ && ${#k} -gt 1 ]]; do k="${k%/}"; done
	echo "$k"
}

trim() {
	local s="$1"
	s="${s#"${s%%[![:space:]]*}"}"
	s="${s%"${s##*[![:space:]]}"}"
	echo "$s"
}

is_truthy() {
	local v
	v="$(echo "$1" | tr '[:upper:]' '[:lower:]')"
	[[ "$v" == "true" || "$v" == "t" || "$v" == "1" || "$v" == "yes" || "$v" == "y" ]]
}

local_dir_from_folderpath() {
	local folderpath="$1"
	local rest="${folderpath#/docs}"
	rest="${rest#/}"
	echo "${DOCS_LOCAL_ROOT}/${rest}"
}

# Map DB folderpath to canonical /docs/... for S3 keys
normalize_folderpath_for_s3() {
	local fp="$1"
	if [[ "$fp" == /docs/* || "$fp" == /docs ]]; then
		echo "$fp"
		return 0
	fi
	if [[ "$fp" == *"/docs/"* ]]; then
		echo "/docs/${fp#*/docs/}"
		return 0
	fi
	if [[ "$fp" == */docs ]]; then
		echo "/docs"
		return 0
	fi
	if [[ "$fp" == "$DOCS_LOCAL_ROOT"/* || "$fp" == "$DOCS_LOCAL_ROOT" ]]; then
		local rest="${fp#$DOCS_LOCAL_ROOT}"
		rest="${rest#/}"
		if [[ -n "$rest" ]]; then
			echo "/docs/${rest}"
		else
			echo "/docs"
		fi
		return 0
	fi
	return 1
}

# Where to read files on disk: absolute CSV path, or DOCS_LOCAL_ROOT for /docs/... paths
resolve_local_dir() {
	local raw="$1"
	if [[ "$raw" == /docs || "$raw" == /docs/* ]]; then
		local_dir_from_folderpath "$raw"
	elif [[ "$raw" == /* ]]; then
		echo "$raw"
	else
		return 1
	fi
}

s3_prefix_from_folderpath() {
	local folderpath="$1"
	local rest="${folderpath#/docs}"
	rest="${rest#/}"
	normalize_s3_key "$rest"
}

s3_object_exists() {
	local key="$1"
	aws_cli s3api head-object --bucket "$AWS_S3_BUCKET" --key "$key" >/dev/null 2>&1 \
		|| aws_cli s3api head-object --bucket "$AWS_S3_BUCKET" --key "${key%/}/" >/dev/null 2>&1
}

put_folder_marker() {
	local key="$1"
	if [[ "$key" != */ ]]; then
		key="${key}/"
	fi
	if s3_object_exists "$key"; then
		return 0
	fi
	if $DRY_RUN; then
		echo "  [dry-run] folder marker: s3://${AWS_S3_BUCKET}/${key}"
		return 0
	fi
	aws_cli s3api put-object --bucket "$AWS_S3_BUCKET" --key "$key" --body "$EMPTY_FILE" >/dev/null
}

ensure_folder_markers() {
	local prefix="$1"
	local acc="" part
	IFS='/' read -r -a parts <<< "$prefix"
	for part in "${parts[@]}"; do
		[[ -z "$part" ]] && continue
		if [[ -n "$acc" ]]; then
			acc="${acc}/${part}"
		else
			acc="$part"
		fi
		put_folder_marker "$acc"
	done
}

guess_content_type() {
	local file="$1"
	if command -v file >/dev/null 2>&1; then
		file -b --mime-type "$file" 2>/dev/null || echo "application/octet-stream"
	else
		case "${file##*.}" in
			pdf) echo "application/pdf" ;;
			jpg|jpeg) echo "image/jpeg" ;;
			png) echo "image/png" ;;
			doc) echo "application/msword" ;;
			docx) echo "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ;;
			xls) echo "application/vnd.ms-excel" ;;
			xlsx) echo "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ;;
			*) echo "application/octet-stream" ;;
		esac
	fi
}

upload_file() {
	local local_file="$1"
	local s3_key="$2"
	local content_type
	content_type="$(guess_content_type "$local_file")"
	if $DRY_RUN; then
		echo "  [dry-run] upload: $local_file -> s3://${AWS_S3_BUCKET}/${s3_key} (${content_type})"
		return 0
	fi
	aws_cli s3 cp "$local_file" "s3://${AWS_S3_BUCKET}/${s3_key}" \
		--content-type "$content_type" \
		--only-show-errors
}

line_num=0
ok=0
err=0

while IFS= read -r line || [[ -n "$line" ]]; do
	line_num=$((line_num + 1))
	line="$(trim "$line")"
	[[ -z "$line" ]] && continue

	# Header row
	if [[ $line_num -eq 1 && "$line" =~ ^[Ii][Dd], ]]; then
		continue
	fi

	# Simple CSV: id,filename,folderpath,isfolder (no commas inside fields)
	IFS=',' read -r id filename folderpath isfolder <<< "$line"
	id="$(trim "$id")"
	filename="$(trim "$filename")"
	folderpath="$(trim "$folderpath")"
	isfolder="$(trim "$isfolder")"

	if [[ -z "$id" || -z "$filename" || -z "$folderpath" || -z "$isfolder" ]]; then
		echo "ERROR line $line_num: empty field (id='$id' filename='$filename' folderpath='$folderpath' isfolder='$isfolder')" >&2
		err=$((err + 1))
		continue
	fi

	raw_folderpath="$folderpath"
	if ! folderpath="$(normalize_folderpath_for_s3 "$raw_folderpath")"; then
		echo "ERROR line $line_num: folderpath must contain /docs (got '$raw_folderpath')" >&2
		err=$((err + 1))
		continue
	fi
	if [[ "$raw_folderpath" != "$folderpath" ]]; then
		echo "NOTE line $line_num: folderpath '$raw_folderpath' -> '$folderpath' (S3)"
	fi

	if ! local_dir="$(resolve_local_dir "$raw_folderpath")"; then
		echo "ERROR line $line_num: cannot resolve local directory for '$raw_folderpath'" >&2
		err=$((err + 1))
		continue
	fi

	s3_prefix="$(s3_prefix_from_folderpath "$folderpath")"

	if is_truthy "$isfolder"; then
		folder_key="$(normalize_s3_key "${s3_prefix}/${filename}")"
		echo "FOLDER line $line_num: $folderpath -> s3://${AWS_S3_BUCKET}/${folder_key}/"
		ensure_folder_markers "$folder_key"
		put_folder_marker "$folder_key"
		ok=$((ok + 1))
		continue
	fi

	local_file="${local_dir}/${filename}"
	s3_key="$(normalize_s3_key "${s3_prefix}/${id}")"

	if [[ ! -f "$local_file" ]]; then
		echo "ERROR line $line_num: file not found: $local_file" >&2
		err=$((err + 1))
		continue
	fi

	echo "UPLOAD line $line_num: $local_file -> s3://${AWS_S3_BUCKET}/${s3_key}"
	ensure_folder_markers "$s3_prefix"
	if upload_file "$local_file" "$s3_key"; then
		ok=$((ok + 1))
	else
		echo "ERROR line $line_num: aws s3 cp failed for $local_file" >&2
		err=$((err + 1))
	fi
done < "$DOCUMENTS_CSV"

echo ""
echo "Done. uploaded=$ok errors=$err dry_run=$DRY_RUN"
echo "Full log: $LOG_FILE"
[[ $err -gt 0 ]] && exit 1
exit 0
