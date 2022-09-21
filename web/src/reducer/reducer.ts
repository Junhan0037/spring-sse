import {combineReducers} from '@reduxjs/toolkit';
import {eventSlice} from './eventSlice';

export const reducer = combineReducers({
  eventStore: eventSlice.reducer
})
