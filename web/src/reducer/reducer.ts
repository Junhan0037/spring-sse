import {combineReducers} from '@reduxjs/toolkit';
import {sseSlice} from './sseSlice';
import {TypedUseSelectorHook, useSelector} from 'react-redux';

export const reducer = combineReducers({
  sseStore: sseSlice.reducer
})

export type RootState = ReturnType<typeof reducer>
export const useTypedSelector: TypedUseSelectorHook<RootState> = useSelector
