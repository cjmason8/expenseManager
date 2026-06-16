#!/usr/bin/env bash
#
# Fix quoted S3 object keys using the dev upload log.
# Wrapper around fix-quoted-s3-keys-from-log.sh
#
# Usage:
#   ./scripts/fix-quoted-s3-keys-dev.sh --dry-run
#   ./scripts/fix-quoted-s3-keys-dev.sh
#   ./scripts/fix-quoted-s3-keys-dev.sh --execute
#
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="${DOCUMENT_LOG_DEV:-$HOME/document-log-dev.txt}"

exec "$SCRIPT_DIR/fix-quoted-s3-keys-from-log.sh" "$LOG_FILE" "$@"
