import {createSlice} from '@reduxjs/toolkit';

interface EventState {
  event: any
}

const initialState: EventState = {
  event: null
}

export const eventSlice = createSlice({
  name: 'eventSlice',
  initialState,
  reducers: {
    setEvent(state, action) {
      state.event = action.payload
    }
  },
  extraReducers: {},
})

export const { setEvent } = eventSlice.actions
