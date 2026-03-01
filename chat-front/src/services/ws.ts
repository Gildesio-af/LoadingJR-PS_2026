import { Client, StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_BASE_URL = (import.meta as any)?.env?.VITE_WS_BASE_URL || (window as any)?.__env?.WS_BASE_URL || "http://localhost:8080/ws-chat";

export const createStompClient = () => {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_BASE_URL),
    reconnectDelay: 5000,
    onStompError: (frame) => {
      console.error("STOMP error", frame);
    },
  });
  return client;
};

export const subscribeToChat = (
  client: Client,
  chatId: string,
  onMessage: (body: any) => void
): StompSubscription | null => {
  if (!client.connected) return null;
  return client.subscribe(`/topic/chat/${chatId}`, (msg) => {
    try {
      const parsed = JSON.parse(msg.body);
      onMessage(parsed);
    } catch (e) {
      console.error("Erro ao parsear mensagem", e);
    }
  });
};

