#!/bin/bash

# ─── Gradle bootRun 프로세스 종료 ─────────────────────────────
PID=$(lsof -ti tcp:8080)

if [ -z "$PID" ]; then
  echo "[INFO] 실행 중인 서버가 없습니다. (port 8080)"
  exit 0
fi

echo "[INFO] 서버를 종료합니다. (PID: $PID)"
kill "$PID"

# 종료 확인
sleep 1
if kill -0 "$PID" 2>/dev/null; then
  echo "[WARN] 정상 종료 실패. 강제 종료합니다."
  kill -9 "$PID"
fi

echo "[INFO] 서버가 종료되었습니다."
