To run service at background on ubuntu:
nohup java -jar build/libs/your-project-name-1.0-SNAPSHOT.jar > app.log 2>&1 &
Check service running:
ps aux | grep java