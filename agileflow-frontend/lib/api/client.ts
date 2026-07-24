import axios from 'axios';
import { getSession, signOut } from 'next-auth/react';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config) => {
    // Only get session on client side, or if we pass token explicitly
    if (typeof window !== 'undefined') {
      const session = await getSession();
      if (session?.user?.accessToken) {
        config.headers.Authorization = `Bearer ${session.user.accessToken}`;
      }
    }
    
    if (process.env.NODE_ENV === 'development') {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    if (process.env.NODE_ENV === 'development') {
      console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url} - ${response.status}`);
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    if (process.env.NODE_ENV === 'development') {
      console.error(`[API Error] ${originalRequest.method?.toUpperCase()} ${originalRequest.url} - ${error.response?.status}`);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      if (typeof window !== 'undefined') {
        // Since we handle refresh in NextAuth's jwt callback, calling getSession() 
        // forces NextAuth to evaluate the token and trigger refresh if expired.
        const session = await getSession();
        
        if (session?.error === 'RefreshAccessTokenError') {
          // Refresh failed, force sign out
          await signOut({ callbackUrl: '/login' });
          return Promise.reject(error);
        }

        if (session?.user?.accessToken) {
          originalRequest.headers.Authorization = `Bearer ${session.user.accessToken}`;
          return apiClient(originalRequest);
        }
      }
    }

    return Promise.reject(error);
  }
);
