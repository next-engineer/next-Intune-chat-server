#!/usr/bin/env bash
set -euo pipefail

AWS_REGION=$1
COMMAND_ID=$2

echo "Waiting for SSM Command $COMMAND_ID ..."

for i in $(seq 1 120); do
  STATUS=$(aws ssm list-commands \
    --command-id "$COMMAND_ID" \
    --query "Commands[0].Status" \
    --output text \
    --region "$AWS_REGION")

  echo "SSM Status: $STATUS"
  case "$STATUS" in
    Success) exit 0 ;;
    Failed|Cancelled|TimedOut) echo "SSM failed: $STATUS"; exit 1 ;;
  esac
  sleep 5
done

echo "Timeout waiting for SSM command"
exit 1
