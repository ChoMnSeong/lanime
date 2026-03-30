#!/bin/bash

# ─── .env 파일 로드 ───────────────────────────────────────────
if [ -f .env ]; then
  set -o allexport
  source .env
  set +o allexport
  echo "[INFO] .env 파일을 로드했습니다."
else
  echo "[WARN] .env 파일이 없습니다. .env.example을 복사해서 만들어주세요."
  echo "       cp .env.example .env"
  exit 1
fi

# ─── 필수 환경변수 체크 ───────────────────────────────────────
REQUIRED_VARS=("DB_USERNAME" "DB_PASSWORD" "MAIL_USERNAME" "MAIL_PASSWORD" "JWT_SECRET")
MISSING=0

for VAR in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!VAR}" ]; then
    echo "[ERROR] 필수 환경변수 '$VAR' 가 설정되지 않았습니다."
    MISSING=1
  fi
done

if [ "$MISSING" -eq 1 ]; then
  echo "[ERROR] .env 파일을 확인해주세요."
  exit 1
fi

# ─── 서버 실행 ────────────────────────────────────────────────
echo "[INFO] Lanime 서버를 시작합니다..."
./gradlew bootRun
