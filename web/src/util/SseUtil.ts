import {setSse} from '../reducer/sseSlice';
import {SseStatusType} from './SseStatusType';

export const subscribeEvent = (dispatch: any, eventName: string): void => {
  const eventSource = new EventSource(`/api/sse/subscribe/${eventName}`, {
    withCredentials: true,
  })

  eventSource.addEventListener(eventName, (event: any) => {
    console.log(JSON.parse(event.data))

    const payload = JSON.parse(event.data)

    dispatch(setSse(payload))

    if (payload.event.status === SseStatusType.COMPLETE || payload.event.status === SseStatusType.ERROR) {
      eventSource.close()
    }
  })
}
