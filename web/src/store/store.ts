import {configureStore} from '@reduxjs/toolkit';
import {reducer} from '../reducer/reducer';

export const store = configureStore({
  reducer: reducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat()
})

export type Rootstate = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
