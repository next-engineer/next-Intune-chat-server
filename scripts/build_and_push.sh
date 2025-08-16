#!/usr/bin/env bash
set -euo pipefail

ECR_REGISTRY=$1
ECR_REPOSITORY=$2
IMAGE_TAG=$3

docker build -t "$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" .
docker push "$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG"

# 여기서 바로 출력
echo "image=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" >> "$GITHUB_OUTPUT"
