#!/usr/bin/env bash
#
# Fix quoted S3 object keys using the prod upload log.
# Wrapper around fix-quoted-s3-keys-from-log.sh
#
# Usage:
#   ./scripts/fix-quoted-s3-keys-prod.sh --dry-run
#   ./scripts/fix-quoted-s3-keys-prod.sh
#   ./scripts/fix-quoted-s3-keys-prod.sh --execute
#
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="${DOCUMENT_LOG_PROD:-$HOME/document-log-prod.txt}"

exec "$SCRIPT_DIR/fix-quoted-s3-keys-from-log.sh" "$LOG_FILE" "$@"
