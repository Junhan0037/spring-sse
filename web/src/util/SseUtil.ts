import {setSse} from '../reducer/sseSlice';
import {SseStatusType} from './SseStatusType';

export const subscribeEvent = (dispatch: any, eventName: string): void => {
  const eventSource = new EventSource(`/api/sse/subscribe/${eventName}`, {
    withCredentials: true,
  })

  eventSource.addEventListener(eventName, (event: any) => {
    const payload = JSON.parse(event.data)

    console.log('====================================')
    console.log(payload.result)
    console.log('====================================')

    dispatch(setSse(payload.result))

    if (payload.status === SseStatusType.COMPLETE || payload.status === SseStatusType.ERROR) {
      eventSource.close()
    }
  })
}
