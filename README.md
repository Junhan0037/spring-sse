# Spring SSE

Non Blocking, Blocking, Parallel, Serial, Multi-Thread, SSE  
- Task 를 포함한 Job 을 순차 or 병렬 처리  
- Job 들을 포함한 JobQueue 를 순차 or 병렬 처리  
- 해당 결과를 SSE 를 통해 전달


## Backend

- JobGroup  
  유저의 모든 Job 들의 집합 (ex. 어떤 유저의 모든 실행 중인 쓰레드)
- JobQueue  
  Job 들을 Queue 에 쌓아서 순차적으로 실행 (Work Runner 에서 실행하여 결과를 얻는 최소 단위)
- Job  
  실제 실행되는 쓰레드들의 집합 (순차, 병렬 실행 가능)
- Task
  실행 로직


## FrontEnd

`EventSource` 를 통해 해당 Non Blocking 로직을 `subscribe` 한다.  
결과를 Redux Store 에 저장


## 프로세스

1. **FE** eventName 을 정해서 subscribe 시작
2. **BE** emitter 생성
3. **FE** 로직 API 호출 (Non-Blocking, Blocking, Parallel, Serial 등)
4. **BE** 로직 결과가 나올때 마다 해당 emitter 를 통해 publish
5. **FE** subscribe 채널에 오는 결과를 화면에 표시

