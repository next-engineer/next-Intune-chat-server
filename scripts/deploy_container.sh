#!/usr/bin/env bash
set -euo pipefail

AWS_REGION=$1
ECR_REGISTRY=$2
IMAGE_URI=$3
CONTAINER_NAME=$4
DOCKER_RUN_ARGS=$5

echo "[1/5] ECR 로그인"
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "[2/5] 새 이미지 Pull: $IMAGE_URI"
docker pull "$IMAGE_URI"

echo "[3/5] 기존 컨테이너 정리: $CONTAINER_NAME"
if docker ps -a --format '{{.Names}}' | grep -wq "$CONTAINER_NAME"; then
  docker rm -f "$CONTAINER_NAME" || true
fi

echo "[4/5] 오래된 이미지 가비지 정리"
docker image prune -f || true

echo "[5/5] 새 컨테이너 기동"
docker run -d --name "$CONTAINER_NAME" $DOCKER_RUN_ARGS "$IMAGE_URI"

echo "DONE"