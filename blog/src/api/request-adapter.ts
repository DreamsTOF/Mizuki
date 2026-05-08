import api from '@/api/api.ts';
import type { AxiosRequestConfig } from 'axios';

export const customInstance = <T>(config: AxiosRequestConfig): Promise<T> => {
  return api(config) as Promise<T>;
};
