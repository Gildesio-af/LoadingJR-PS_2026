import axios from "axios";
const API_BASE_URL = (import.meta as any)?.env?.VITE_API_BASE_URL || (window as any)?.__env?.API_BASE_URL || "http://localhost:8080";
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
api.interceptors.response.use(
  (res) => res,
  (error) => {
    console.error("API error", {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      data: error.response?.data,
    });
    return Promise.reject(error);
  }
);
export default api;
export interface User {
  id: string;
  username: string;
  email: string;
}
export interface AuthResponse {
  token: string;
  id: string;
  username: string;
  email: string;
}
export interface Chat {
  id: string;
  status: "PENDING" | "ACTIVE" | "CLOSED";
  initiatorUsername: string;
  participantUsername: string;
  messages: {
    content: Message[];
  } | null;
  createdAt: string;
  closedAt?: string | null;
  aiReport?: string | null;
}
export interface Message {
  id: string;
  senderId: string;
  senderUsername: string;
  content: string;
  sentAt: string;
}
type PageResponse<T> = { content?: T[] } & Record<string, unknown>;
// Auth API
export const login = async (username: string, password: string): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>("/auth/login", { username, password });
  return response.data;
};
export const register = async (userData: { username: string; email: string; password: string }): Promise<User> => {
  const response = await api.post<User>("/users", userData);
  return response.data;
};
// User API
export const getUser = async (id: string): Promise<User> => {
  const response = await api.get<User>(`/users/${id}`);
  return response.data;
};
export const searchUsers = async (username: string = ""): Promise<{ content: User[] }> => {
  const response = await api.get<PageResponse<User>>("/users/username", {
    params: { username, page: 0, size: 50, sort: "username,asc" }
  });
  return { content: response.data?.content ?? [] };
};
// Chat API
export const getPendingChats = async (): Promise<Chat[]> => {
  const response = await api.get<PageResponse<Chat>>("/chats", { params: { page: 0, size: 50, sort: "createdAt,desc" } });
  return response.data?.content ?? [];
};
export const getChatHistory = async (): Promise<Chat[]> => {
  const response = await api.get<PageResponse<Chat>>("/chats/history", { params: { } });
  return response.data?.content ?? [];
};
// Kept for backward compatibility if needed, but prefer getPendingChats
export const getPendingChat = async (): Promise<Chat | null> => {
  const chats = await getPendingChats();
  return chats.length > 0 ? chats[0] : null;
};
export const requestChat = async (participantId: string): Promise<Chat> => {
  const response = await api.post<Chat>("/chats", { participantId });
  return response.data;
};
export const acceptChat = async (chatId: string): Promise<void> => {
  await api.patch(`/chats/${chatId}/accept`);
};
export const closeChat = async (chatId: string): Promise<void> => {
  await api.patch(`/chats/${chatId}/close`);
};
export const getChatDetails = async (chatId: string): Promise<Chat> => {
  const response = await api.get<Chat>(`/chats/${chatId}`);
  return response.data;
};
export const rejectChat = async (chatId: string): Promise<void> => {
  await api.patch(`/chats/${chatId}/reject`);
};
