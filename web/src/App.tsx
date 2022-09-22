import React, {useEffect, useState} from 'react'
import {useDispatch} from 'react-redux';
import http from './config/axios-interceptor';
import {Button} from 'devextreme-react';
import {subscribeEvent} from './util/SseUtil';
import {useTypedSelector} from './reducer/reducer';

const ApiAsync = async () => {
  return await http.get('/api/member/test')
}

const App = () => {
  const dispatch = useDispatch()
  const sse = useTypedSelector(state => state.sseStore)
  const [output, setOutput] = useState<any>()

  useEffect(() => {
    if (sse.event) {
      setOutput(sse.event)
    }
  }, [sse.event])

  const handleSse = async () => {
    try {
      const res: any = await ApiAsync()

      subscribeEvent(dispatch, res['eventName'])
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
