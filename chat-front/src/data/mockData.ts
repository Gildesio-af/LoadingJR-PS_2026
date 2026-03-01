export interface ChatUser {
  id: string;
  username: string;
  email: string;
  avatar?: string;
}

export interface ChatInvite {
  chatId: string;
  from: ChatUser;
  status: "PENDING" | "ACCEPTED";
}

export interface ChatSession {
  id: string;
  participants: ChatUser[];
  status: "ACTIVE" | "CLOSED";
  lastMessage?: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: string;
  chatId: string;
  senderId: string;
  content: string;
  timestamp: string;
}

// Mock data
export const MOCK_USERS: ChatUser[] = [
  { id: "u1", username: "Ana Silva", email: "ana@email.com" },
  { id: "u2", username: "Carlos Mendes", email: "carlos@email.com" },
  { id: "u3", username: "Beatriz Lopes", email: "bia@email.com" },
  { id: "u4", username: "Daniel Rocha", email: "daniel@email.com" },
];

export const MOCK_INVITES: ChatInvite[] = [
  { chatId: "c10", from: MOCK_USERS[1], status: "PENDING" },
  { chatId: "c11", from: MOCK_USERS[2], status: "PENDING" },
];

export const MOCK_CHATS: ChatSession[] = [
  {
    id: "c1",
    participants: [MOCK_USERS[0], MOCK_USERS[1]],
    status: "ACTIVE",
    lastMessage: "Ei, tudo certo?",
    updatedAt: "2025-02-25T10:30:00Z",
  },
  {
    id: "c2",
    participants: [MOCK_USERS[0], MOCK_USERS[2]],
    status: "ACTIVE",
    lastMessage: "Vamos marcar uma call?",
    updatedAt: "2025-02-24T18:00:00Z",
  },
  {
    id: "c3",
    participants: [MOCK_USERS[0], MOCK_USERS[3]],
    status: "CLOSED",
    lastMessage: "Obrigado pela conversa!",
    updatedAt: "2025-02-23T14:00:00Z",
  },
];

export const MOCK_MESSAGES: ChatMessage[] = [
  { id: "m1", chatId: "c1", senderId: "me", content: "Olá, tudo bem?", timestamp: "2025-02-25T10:00:00Z" },
  { id: "m2", chatId: "c1", senderId: "u2", content: "Tudo ótimo! E você?", timestamp: "2025-02-25T10:05:00Z" },
  { id: "m3", chatId: "c1", senderId: "me", content: "Bem também! Queria falar sobre o projeto.", timestamp: "2025-02-25T10:10:00Z" },
  { id: "m4", chatId: "c1", senderId: "u2", content: "Claro, manda ver!", timestamp: "2025-02-25T10:15:00Z" },
  { id: "m5", chatId: "c1", senderId: "me", content: "Precisamos definir os próximos passos.", timestamp: "2025-02-25T10:20:00Z" },
  { id: "m6", chatId: "c1", senderId: "u2", content: "Ei, tudo certo?", timestamp: "2025-02-25T10:30:00Z" },
];
