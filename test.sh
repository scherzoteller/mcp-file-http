#!/usr/bin/env zsh


if [ -z $1 ]; then
	FILE_TO_RETRIEVE="notes.txt"
else
	FILE_TO_RETRIEVE="$1"
fi
echo curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"read_file\",\"arguments\":{\"path\":\"$1\"}}}"

curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"read_file\",\"arguments\":{\"path\":\"$1\"}}}"

#curl -X POST http://localhost:8080/mcp \
#  -H "Content-Type: application/json" \
#  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"read_file","arguments":{"path":"notes.txt"}}}'
