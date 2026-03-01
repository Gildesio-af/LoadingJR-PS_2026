import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ArrowLeft, Bot, Loader2, MessageCircle } from "lucide-react";
import { getChatDetails, Chat, Message } from "@/services/api";
import { toast } from "@/hooks/use-toast";
import { useAuth } from "@/contexts/AuthContext";

const ReportPage = () => {
  const { chatId } = useParams<{ chatId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [chat, setChat] = useState<Chat | null>(null);
  const [loading, setLoading] = useState(true);
  const pollCountRef = useRef(0);

  // Polling to get the report if it's not ready yet
  useEffect(() => {
    if (!chatId) return;

    const fetchChatData = async () => {
      try {
        const chatData = await getChatDetails(chatId);
        setChat(chatData);

        if (chatData.aiReport) {
          return true; // Stop polling
        }
      } catch (error) {
        console.error("Error fetching chat details", error);
        // Don't stop polling on transient errors, but limit retries?
        // validation: if 404 stop.
      }
      return false;
    };

    const intervalId = setInterval(async () => {
        pollCountRef.current += 1;
        const done = await fetchChatData();

        if (done) {
            clearInterval(intervalId);
            setLoading(false);
        } else if (pollCountRef.current > 10) {
            // Stop after ~30 seconds of polling if report doesn't appear
            clearInterval(intervalId);
            setLoading(false);
            toast({ title: "O relatório está demorando para ser gerado.", variant: "default" });
        }
    }, 3000);

    // Initial fetch
    fetchChatData().then(done => {
        if (done) {
            clearInterval(intervalId);
            setLoading(false);
        }
    });

    return () => clearInterval(intervalId);
  }, [chatId]);

  const otherUsername = chat
    ? (chat.initiatorUsername === user?.username ? chat.participantUsername : chat.initiatorUsername)
    : "Chat";

  const messages: Message[] = chat?.messages?.content || [];

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-10 border-b border-border bg-card/80 backdrop-blur-md">
        <div className="container flex h-16 items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate("/dashboard")}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="font-semibold text-foreground">Relatório do Chat</h1>
            <p className="text-xs text-muted-foreground">
              com {loading ? <Loader2 className="inline h-3 w-3 animate-spin"/> : otherUsername}
            </p>
          </div>
        </div>
      </header>

      <main className="container py-6 space-y-6 max-w-2xl">
        {/* AI Report Card */}
        <Card
          className={`border-2 transition-all duration-500 ${
            chat?.aiReport ? "border-primary/40 shadow-lg shadow-primary/10" : "border-border"
          }`}
        >
          <CardHeader className="pb-3">
            <div className="flex items-center gap-3">
              <div
                className={`flex h-10 w-10 items-center justify-center rounded-xl ${
                  chat?.aiReport ? "bg-primary/20" : "bg-muted"
                }`}
              >
                <Bot className={`h-5 w-5 ${chat?.aiReport ? "text-primary" : "text-muted-foreground"}`} />
              </div>
              <div>
                  <CardTitle className="text-lg text-foreground">Relatório da IA</CardTitle>
                  <CardDescription>Resumo gerado automaticamente</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {!chat?.aiReport && loading ? (
              <div className="flex flex-col items-center gap-3 py-8 text-center">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <p className="text-sm text-muted-foreground">
                    Analisando conversa e gerando insights...<br/>
                    Isso pode levar alguns segundos.
                </p>
              </div>
            ) : chat?.aiReport ? (
              <div className="prose prose-sm dark:prose-invert max-w-none animate-fade-in">
                  <p className="whitespace-pre-wrap text-foreground/90 leading-relaxed">
                      {chat.aiReport}
                  </p>
              </div>
            ) : (
                <div className="py-4 text-center">
                    <p className="text-muted-foreground mb-4">Relatório indisponível ou falha na geração.</p>
                </div>
            )}
          </CardContent>
        </Card>

        <Separator />

        {/* Chat History */}
        <section>
          <div className="flex items-center gap-2 mb-4">
            <MessageCircle className="h-5 w-5 text-muted-foreground" />
            <h2 className="font-semibold text-foreground">Histórico de Mensagens</h2>
          </div>
          <div className="space-y-2">
            {messages.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">Sem mensagens registradas.</p>
            ) : (
              messages.map((msg) => (
                <div key={msg.id} className="rounded-lg bg-card border border-border p-3 animate-fade-in">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-medium text-primary">
                      {msg.senderUsername === user?.username ? "Você" : msg.senderUsername}
                    </span>
                    <span className="text-[10px] text-muted-foreground">
                      {new Date(msg.sentAt).toLocaleString("pt-BR")}
                    </span>
                  </div>
                  <p className="text-sm text-foreground/80">{msg.content}</p>
                </div>
              ))
            )}
          </div>
        </section>
      </main>
    </div>
  );
};

export default ReportPage;
