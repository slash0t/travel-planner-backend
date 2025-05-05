#!/bin/sh
# wait-for.sh

set -e

host="$1"
shift
port="$1"
shift
timeout="$1"
shift
cmd="$@"

until nc -z $host $port; do
  echo "Waiting for $host:$port..."
  sleep 1
  timeout=$((timeout - 1))
  if [ $timeout -le 0 ]; then
    echo "Timeout waiting for $host:$port"
    exit 1
  fi
done

exec $cmd