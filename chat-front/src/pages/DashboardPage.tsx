import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  Users, MessageCircle, Clock, LogOut, UserPlus, Check, ArrowRight, Loader2, Play, X
} from "lucide-react";
import { searchUsers, getPendingChats, requestChat, acceptChat, rejectChat, getChatHistory, Chat, User } from "@/services/api";
import { toast } from "@/hooks/use-toast";
import { createStompClient } from "@/services/ws";

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);
  const [chats, setChats] = useState<Chat[]>([]);
  const [historyChats, setHistoryChats] = useState<Chat[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [loadingChats, setLoadingChats] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [loadingAction, setLoadingAction] = useState<string | null>(null);
  const stompClientRef = useRef<any>(null);
  const activeChat = chats.find(c => c.status === "ACTIVE");
  const pendingInvites = chats.filter(c => c.status === "PENDING" && c.participantUsername === user?.username);
  const sentRequests = chats.filter(c => c.status === "PENDING" && c.initiatorUsername === user?.username);

  const fetchData = async (silent = false) => {
    if (!silent) {
      setLoadingUsers(true);
      setLoadingChats(true);
      setLoadingHistory(true);
    }
    try {
      const [usersResult, chatsResult, historyResult] = await Promise.allSettled([
        searchUsers(),
        getPendingChats(),
        getChatHistory()
      ]);
      if (usersResult.status === "fulfilled") {
        setUsers(usersResult.value.content ?? []);
      } else {
        console.error("Erro ao buscar usuários", usersResult.reason);
        if (!silent) toast({ title: "Erro ao buscar usuários", description: "Verifique conexão e autenticação.", variant: "destructive" });
      }
      if (chatsResult.status === "fulfilled") {
        setChats(chatsResult.value ?? []);
      } else {
        console.error("Erro ao buscar convites", chatsResult.reason);
        if (!silent) toast({ title: "Erro ao buscar convites", description: "Verifique conexão e autenticação.", variant: "destructive" });
      }
      if (historyResult.status === "fulfilled") {
        setHistoryChats(historyResult.value ?? []);
      } else {
        console.error("Erro ao buscar histórico", historyResult.reason);
        if (!silent) toast({ title: "Erro ao buscar histórico", description: "Verifique conexão e autenticação.", variant: "destructive" });
      }
    } finally {
      if (!silent) {
        setLoadingUsers(false);
        setLoadingChats(false);
        setLoadingHistory(false);
      }
    }
  };

  useEffect(() => {
    fetchData();
    const silentInterval = setInterval(() => fetchData(true), 10000);
    return () => clearInterval(silentInterval);
  }, []);
  useEffect(() => {
    const client = createStompClient();
    stompClientRef.current = client;
    client.connectHeaders = () => {
      const token = localStorage.getItem("jwt_token");
      return token ? { Authorization: `Bearer ${token}` } : {};
    };
    client.onConnect = () => {
      client.subscribe("/topic/chat/updates", () => fetchData(true));
      if (activeChat) {
        client.subscribe(`/topic/chat/${activeChat.id}`, () => fetchData(true));
      }
    };
    client.activate();
    return () => client.deactivate();
  }, [activeChat?.id]);

  const handleAcceptInvite = async (chatId: string) => {
    setLoadingAction(chatId);
    try {
      await acceptChat(chatId);
      toast({ title: "Convite aceito!", description: "Entrando no chat..." });
      await fetchData(true);
      navigate(`/chat/${chatId}`);
    } catch (error) {
      toast({ title: "Erro ao aceitar convite", variant: "destructive" });
    } finally {
      setLoadingAction(null);
    }
  };
  const handleRejectInvite = async (chatId: string) => {
    setLoadingAction(chatId);
    try {
      await rejectChat(chatId);
      toast({ title: "Convite recusado" });
      await fetchData(true);
    } catch (error) {
      toast({ title: "Erro ao recusar convite", variant: "destructive" });
    } finally {
      setLoadingAction(null);
    }
  };
  const handleRequestChat = async (participantId: string) => {
    setLoadingAction(`request-${participantId}`);
    try {
      await requestChat(participantId);
      toast({ title: "Convite enviado!" });
      await fetchData(true);
    } catch (error: any) {
        if (error.response?.status === 409) {
             toast({ title: "Usuário ocupado", description: "Você ou o usuário já estão ocupados.", variant: "destructive" });
        } else {
             toast({ title: "Erro ao enviar convite", variant: "destructive" });
        }
    } finally {
      setLoadingAction(null);
    }
  };
  const handleLogout = () => {
    logout();
    navigate("/login");
  };
  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-10 border-b border-border bg-card/80 backdrop-blur-md">
        <div className="container flex h-16 items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary/10">
              <MessageCircle className="h-5 w-5 text-primary" />
            </div>
            <h1 className="text-lg font-bold text-foreground">ChatAI</h1>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-muted-foreground">
              Olá, <span className="text-foreground font-medium">{user?.username}</span>
            </span>
            <Button variant="ghost" size="icon" onClick={handleLogout} title="Sair">
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </header>
      <main className="container py-6 space-y-6">
        {/* ACTIVE CHAT */}
        {activeChat && (
            <section className="animate-fade-in">
                 <h2 className="text-lg font-semibold text-foreground mb-4">Chat Ativo</h2>
                 <Card className="bg-primary/5 border-primary/20">
                     <CardHeader>
                         <CardTitle className="flex items-center gap-2">
                             <MessageCircle className="h-5 w-5 text-primary"/>
                             Chat em andamento
                         </CardTitle>
                         <CardDescription>
                             Conversando com {activeChat.initiatorUsername === user?.username ? activeChat.participantUsername : activeChat.initiatorUsername}
                         </CardDescription>
                     </CardHeader>
                     <CardContent>
                         <Button onClick={() => navigate(`/chat/${activeChat.id}`)} className="w-full sm:w-auto">
                             Continuar Conversa <ArrowRight className="ml-2 h-4 w-4"/>
                         </Button>
                     </CardContent>
                 </Card>
            </section>
        )}
        <Separator />
        {/* INVITES */}
        <section className="animate-fade-in">
            <div className="flex items-center gap-2 mb-4">
                <Clock className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-semibold text-foreground">Convites Pendentes</h2>
                {pendingInvites.length > 0 && <Badge variant="secondary">{pendingInvites.length}</Badge>}
            </div>
            {loadingChats ? (
                 <p className="text-muted-foreground">Carregando...</p>
            ) : pendingInvites.length === 0 ? (
                 <p className="text-sm text-muted-foreground">Nenhum convite recebido.</p>
            ) : (
                <div className="grid gap-3 sm:grid-cols-2">
                    {pendingInvites.map(chat => (
                        <Card key={chat.id} className="bg-card border-l-4 border-l-yellow-500">
                             <CardContent className="flex items-center justify-between p-4">
                                 <div>
                                     <p className="font-medium">{chat.initiatorUsername}</p>
                                     <p className="text-xs text-muted-foreground">Convite recebido</p>
                                 </div>
                                 <div className="flex gap-2">
                                   <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => handleRejectInvite(chat.id)}
                                      disabled={loadingAction === chat.id}
                                   >
                                      {loadingAction === chat.id ? <Loader2 className="mr-2 h-4 w-4 animate-spin"/> : <X className="mr-2 h-4 w-4"/>}
                                      Recusar
                                   </Button>
                                   <Button
                                      size="sm"
                                      onClick={() => handleAcceptInvite(chat.id)}
                                      disabled={loadingAction === chat.id}
                                   >
                                      {loadingAction === chat.id ? <Loader2 className="mr-2 h-4 w-4 animate-spin"/> : <Check className="mr-2 h-4 w-4"/>}
                                      Aceitar
                                   </Button>
                                 </div>
                             </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </section>
        {/* SENT REQUESTS */}
        {sentRequests.length > 0 && (
             <section className="animate-fade-in">
                <h2 className="text-lg font-semibold text-foreground mb-4">Convites Enviados</h2>
                <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                     {sentRequests.map(chat => (
                        <Card key={chat.id} className="bg-card border-dashed opacity-80">
                            <CardContent className="p-4">
                                <p className="text-sm font-medium">Para: {chat.participantUsername}</p>
                                <p className="text-xs text-muted-foreground">Aguardando resposta...</p>
                            </CardContent>
                        </Card>
                     ))}
                </div>
             </section>
        )}
        <Separator />
        {/* HISTORY */}
        <section className="animate-fade-in">
          <div className="flex items-center gap-2 mb-4">
            <Play className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-semibold text-foreground">Histórico de Chats</h2>
          </div>
          {loadingHistory ? (
            <p className="text-muted-foreground">Carregando...</p>
          ) : historyChats.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhum chat encerrado ainda.</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {historyChats.map(chat => {
                const other = chat.initiatorUsername === user?.username ? chat.participantUsername : chat.initiatorUsername;
                return (
                  <Card key={chat.id} className="bg-card border-border hover:border-primary/30 transition-colors">
                    <CardContent className="flex items-center justify-between p-4">
                      <div>
                        <p className="font-medium text-foreground">{other}</p>
                        <p className="text-xs text-muted-foreground">
                          Encerrado em {chat.closedAt ? new Date(chat.closedAt).toLocaleDateString("pt-BR") : "-"}
                        </p>
                      </div>
                      <Button size="sm" variant="outline" onClick={() => navigate(`/report/${chat.id}`)}>
                        Ver relatório
                      </Button>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </section>
        <Separator />
        {/* USERS */}
        <section className="animate-fade-in">
          <div className="flex items-center gap-2 mb-4">
            <Users className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-semibold text-foreground">Usuários Disponíveis</h2>
          </div>
          {loadingUsers ? (
              <div className="flex justify-center p-8"><Loader2 className="h-8 w-8 animate-spin text-muted-foreground"/></div>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {users.filter(u => u.username !== user?.username).map((u) => (
                <Card key={u.id} className="bg-card border-border hover:border-primary/30 transition-colors">
                    <CardContent className="flex items-center justify-between p-4">
                    <div>
                        <p className="font-medium text-foreground">{u.username}</p>
                        <p className="text-xs text-muted-foreground">{u.email}</p>
                    </div>
                    <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleRequestChat(u.id)}
                        disabled={!!activeChat || loadingAction === `request-${u.id}`}
                        title={!!activeChat ? "Você já tem um chat ativo" : "Convidar"}
                    >
                        {loadingAction === `request-${u.id}` ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                        <UserPlus className="h-4 w-4" />
                        )}
                    </Button>
                    </CardContent>
                </Card>
                ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
};

export default DashboardPage;
