#!/bin/bash

PORT=8080

# 1. 해당 포트를 사용하는 PID 리스트 가져오기 (배열 형태)
PIDS=$(lsof -t -i tcp:$PORT)

if [ -z "$PIDS" ]; then
  echo "[INFO] 포트 $PORT 를 사용하는 프로세스가 없습니다."
  exit 0
fi

# 2. 각 PID에 대해 종료 시도
for PID in $PIDS; do
  # 해당 PID가 실제 Java 프로세스인지 체크하는 로직을 넣으면 더 안전합니다.
  # (생략 가능하지만 Postman 등을 죽이는 걸 방지하려면 필요)
  
  echo "[INFO] 프로세스 종료 시도 (PID: $PID)"
  kill "$PID" 2>/dev/null
done

# 3. 종료 대기 및 확인 (최대 5초)
echo "[INFO] 종료 대기 중..."
for i in {1..5}; do
  sleep 1
  STILL_ALIVE=$(lsof -t -i tcp:$PORT)
  if [ -z "$STILL_ALIVE" ]; then
    echo "[SUCCESS] 모든 프로세스가 정상 종료되었습니다."
    exit 0
  fi
done

# 4. 여전히 살아있다면 강제 종료 (SIGKILL)
echo "[WARN] 정상 종료되지 않아 강제 종료(kill -9)를 실행합니다."
for PID in $(lsof -t -i tcp:$PORT); do
  kill -9 "$PID" 2>/dev/null
  echo "[INFO] 강제 종료 완료 (PID: $PID)"
done