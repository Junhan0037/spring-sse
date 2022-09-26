import {createSlice} from '@reduxjs/toolkit';

interface SseState {
  data: any
}

const initialState: SseState = {
  data: null
}

export const sseSlice = createSlice({
  name: 'sseSlice',
  initialState,
  reducers: {
    setSse(state, action) {
      state.data = action.payload
    }
  },
  extraReducers: {},
})

export const { setSse } = sseSlice.actions
