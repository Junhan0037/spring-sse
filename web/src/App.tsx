import React, {useEffect, useState} from 'react'
import {useDispatch} from 'react-redux';
import http from './config/axios-interceptor';
import {Button} from 'devextreme-react';
import {subscribeEvent} from './util/SseUtil';
import {useTypedSelector} from './reducer/reducer';

const ApiAsync = async (eventName) => {
  return await http.get(`/api/member/test/${eventName}`)
}

const App = () => {
  const dispatch = useDispatch()
  const sse = useTypedSelector(state => state.sseStore)
  const [output, setOutput] = useState<any>()

  useEffect(() => {
    if (sse.data) {
      setOutput(sse.data)
    }
  }, [sse])

  const handleSse = async () => {
    const eventName = `event-${Date.now()}`
    try {
      // subscribe
      subscribeEvent(dispatch, eventName)

      // Non-Blocking Async
      const res: any = await ApiAsync(eventName)
    } catch (e) {
      console.log(e)
      return
    }
  }

  return (
    <>
      <h1>SSE TEST</h1>

      <Button onClick={handleSse}>테스트 버튼</Button>

      <div>
        {JSON.stringify(output)}
      </div>
    </>
  )
}

export default App
