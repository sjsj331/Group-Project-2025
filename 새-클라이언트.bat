echo [🖥️] 클라이언트 실행...
cd /d "%~dp0"
start cmd /k "call mvnw.cmd exec:java -Dexec.mainClass=com.example.bufschat.client.StompChatClient"
