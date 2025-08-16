#!/usr/bin/env bash
set -euo pipefail

AWS_REGION=$1
ECR_REGISTRY=$2
IMAGE_URI=$3
CONTAINER_NAME=$4
DOCKER_RUN_ARGS=$5

# 실행할 커맨드 문자열 생성
COMMAND=$(printf 'bash /home/ec2-user/deploy_container.sh %s %s %s %s "%s"' \
  "$AWS_REGION" \
  "$ECR_REGISTRY" \
  "$IMAGE_URI" \
  "$CONTAINER_NAME" \
  "$DOCKER_RUN_ARGS")

# JSON 출력 (AWS CLI가 그대로 먹을 수 있는 형식)
jq -n --arg c "$COMMAND" '{commands: [$c]}'