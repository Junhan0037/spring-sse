type SseStatusType = {
  WAITING: string
  RUNNING: string
  COMPLETE: string
  ERROR: string
  CANCELED: string
}

export const SseStatusType: SseStatusType = {
  WAITING: 'WAITING',
  RUNNING: 'RUNNING',
  COMPLETE: 'COMPLETE',
  ERROR: 'ERROR',
  CANCELED: 'CANCELED',
}
