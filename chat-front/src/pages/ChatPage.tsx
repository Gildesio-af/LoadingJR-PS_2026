import { useState, useRef, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ArrowLeft, Send, XCircle, Loader2, Wifi, WifiOff } from "lucide-react";
import { closeChat, getChatDetails, Message, Chat } from "@/services/api";
import { cn } from "@/lib/utils";
import { Client } from "@stomp/stompjs";
import { toast } from "@/hooks/use-toast";

const ChatPage = () => {
  const { chatId } = useParams<{ chatId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [messages, setMessages] = useState<Message[]>([]);
  const [chatInfo, setChatInfo] = useState<Chat | null>(null);
  const [input, setInput] = useState("");
  const [closing, setClosing] = useState(false);
  const [stompConnected, setStompConnected] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const stompClientRef = useRef<Client | null>(null);
  const closedHandledRef = useRef(false);

  // Fetch initial chat details and history
  useEffect(() => {
    if (!chatId) return;
    const fetchChat = async () => {
      try {
        const chatData = await getChatDetails(chatId);
        setChatInfo(chatData);
        if (chatData.messages?.content) {
          setMessages(chatData.messages.content);
        }
      } catch (error) {
        toast({ title: "Erro ao carregar chat", variant: "destructive" });
        navigate("/dashboard");
      }
    };
    fetchChat();
  }, [chatId, navigate]);

  useEffect(() => {
    if (!chatId) return;

    const intervalId = window.setInterval(async () => {
      try {
        const updated = await getChatDetails(chatId);
        setChatInfo(updated);
        if (updated.status === "CLOSED" && !closedHandledRef.current) {
          closedHandledRef.current = true;
          toast({ title: "Chat encerrado" });
          navigate("/dashboard");
        }
      } catch {
        // ignore transient errors
      }
    }, 5000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [chatId, navigate]);

  const otherUsername = chatInfo
    ? (chatInfo.initiatorUsername === user?.username ? chatInfo.participantUsername : chatInfo.initiatorUsername)
    : "Carregando...";

  const formatMessageTime = (sentAt?: string) => {
    if (!sentAt) return "";
    const parsed = new Date(sentAt);
    const resolved = Number.isNaN(parsed.getTime()) ? new Date(`${sentAt}Z`) : parsed;
    if (Number.isNaN(resolved.getTime())) return "";
    return new Intl.DateTimeFormat("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
      timeZone: "America/Sao_Paulo",
    }).format(resolved);
  };

  // Scroll to bottom on new messages
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // STOMP WebSocket connection
  useEffect(() => {
    if (!chatId) return;

    const token = localStorage.getItem("jwt_token");
    const wsUrl = `ws://localhost:8080/ws-chat`;

    const client = new Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${token || ""}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setStompConnected(true);
        console.log("[STOMP] Connected to", wsUrl);

        client.subscribe(`/topic/chat/${chatId}`, (message) => {
          try {
            const parsed = JSON.parse(message.body) as any;
            // The backend returns MessageResponseDTO which matches our Message interface
            // Ensure fields match.
            const newMsg: Message = {
                id: parsed.id,
                senderId: parsed.senderId,
                senderUsername: parsed.senderUsername,
                content: parsed.content,
                sentAt: parsed.sentAt
            };

            setMessages((prev) => {
              // Avoid duplicates
              if (prev.some((m) => m.id === newMsg.id)) return prev;
              return [...prev, newMsg];
            });
          } catch (err) {
            console.error("[STOMP] Failed to parse message:", err);
          }
        });
      },
      onDisconnect: () => {
        setStompConnected(false);
        console.log("[STOMP] Disconnected");
      },
      onStompError: (frame) => {
        console.error("[STOMP] Error:", frame.headers["message"]);
        setStompConnected(false);
      },
      onWebSocketError: () => {
        setStompConnected(false);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
      stompClientRef.current = null;
    };
  }, [chatId]);

  // Send message via STOMP
  const handleSend = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      if (!input.trim() || !stompClientRef.current?.connected) return;

      // Ensure we send valid JSON structure expected by MessageRequestDTO
      // Backend expects MessageRequestDTO { content: string }
      const payload = { content: input.trim() };

      stompClientRef.current.publish({
        destination: `/app/chat/${chatId}/sendMessage`,
        body: JSON.stringify(payload),
      });

      // We don't verify success here, we wait for the message to come back via subscription.
      // But for better UX we might want optimistic UI.
      // However since we rely on ID from server, optimistic UI is tricky without temp ID.
      // Let's just wait for roundtrip or assume success.

      setInput("");
    },
    [input, chatId]
  );

  // Close chat via REST
  const handleCloseChat = async () => {
    if (!chatId) return;
    setClosing(true);
    try {
      await closeChat(chatId);
      closedHandledRef.current = true;
      toast({ title: "Chat encerrado" });
      navigate("/dashboard");
    } catch {
      toast({ title: "Erro ao encerrar chat", variant: "destructive" });
    } finally {
      setClosing(false);
    }
  };
  return (
    <div className="flex h-screen flex-col bg-background">
      {/* Header */}
      <header className="flex items-center justify-between border-b border-border bg-card/80 backdrop-blur-md px-4 py-3">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate("/")}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <p className="font-semibold text-foreground">{otherUsername}</p>
            <div className="flex items-center gap-1">
              {stompConnected ? (
                <>
                  <Wifi className="h-3 w-3 text-primary" />
                  <p className="text-xs text-primary">Online</p>
                </>
              ) : (
                <>
                  <WifiOff className="h-3 w-3 text-muted-foreground" />
                  <p className="text-xs text-muted-foreground">Conectando...</p>
                </>
              )}
            </div>
          </div>
        </div>
        <Button variant="destructive" size="sm" onClick={handleCloseChat} disabled={closing}>
          {closing ? (
            <Loader2 className="h-4 w-4 animate-spin mr-1" />
          ) : (
            <XCircle className="h-4 w-4 mr-1" />
          )}
          Encerrar
        </Button>
      </header>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {messages.map((msg) => {
          const isMine = msg.senderUsername === user?.username;
          return (
            <div
              key={msg.id}
              className={cn("flex animate-fade-in", isMine ? "justify-end" : "justify-start")}
            >
              <div
                className={cn(
                  "max-w-[75%] rounded-2xl px-4 py-2.5 text-sm",
                  isMine
                    ? "bg-primary text-primary-foreground rounded-br-md"
                    : "bg-secondary text-secondary-foreground rounded-bl-md"
                )}
              >
                <p>{msg.content}</p>
                <p
                  className={cn(
                    "mt-1 text-[10px]",
                    isMine ? "text-primary-foreground/60" : "text-muted-foreground"
                  )}
                >
                  {formatMessageTime(msg.sentAt)}
                </p>
              </div>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSend} className="border-t border-border bg-card/80 backdrop-blur-md p-4">
        <div className="flex items-center gap-2">
          <Input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Digite sua mensagem..."
            className="flex-1 bg-secondary border-border"
          />
          <Button type="submit" size="icon" disabled={!stompConnected || !input.trim()}>
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </form>
    </div>
  );
};


export default ChatPage;

