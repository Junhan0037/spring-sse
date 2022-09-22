import {createSlice} from '@reduxjs/toolkit';

interface SseState {
  event: any
}

const initialState: SseState = {
  event: null
}

export const sseSlice = createSlice({
  name: 'sseSlice',
  initialState,
  reducers: {
    setSse(state, action) {
      state.event = action.payload
    }
  },
  extraReducers: {},
})

export const { setSse } = sseSlice.actions
