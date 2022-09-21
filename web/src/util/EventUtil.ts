import {setEvent} from '../reducer/eventSlice';
import {EventStatusType} from './EventStatusType';

export const subscribeEvent = (dispatch: any, eventName: string): void => {
  const eventSource = new EventSource(`/sse/subscribe/${eventName}`, {
    withCredentials: true,
  })

  eventSource.addEventListener(eventName, (event: any) => {
    const payload = {
      event: JSON.parse(event.data)
    }

    console.log(JSON.parse(event.data))

    dispatch(setEvent(payload))

    if (payload.event.status === EventStatusType.COMPLETE || payload.event.status === EventStatusType.ERROR) {
      eventSource.close()
    }
  })
}
