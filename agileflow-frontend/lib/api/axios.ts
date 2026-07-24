import axios from "axios";
import { getSession } from "next-auth/react";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:3000/api",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  async (config) => {
    // For client-side we can get the session
    const session = await getSession();
    if (session?.user && "accessToken" in session.user) {
      const token = (session.user as any).accessToken;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    // Handle global API errors (e.g., 401 Unauthorized)
    if (error.response?.status === 401) {
      // You could trigger a global sign out or token refresh here
    }
    return Promise.reject(error);
  }
);

export default api;
