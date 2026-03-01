import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { MessageCircle } from "lucide-react";
import { login as apiLogin, AuthResponse } from "@/services/api";
import { useToast } from "@/hooks/use-toast";
const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();
  const handleSubmit = async (e: React.FormEvent) => {
    // Isso deve ser a primeira coisa a impedir o reload
    e.preventDefault();
    e.stopPropagation(); 
    console.log("Tentando fazer login...");
    if (!username || !password) {
        toast({
            title: "Campos obrigatórios",
            description: "Por favor, preencha usuário e senha.",
            variant: "destructive",
        });
        return;
    }
    setLoading(true);
    try {
      const response: AuthResponse = await apiLogin(username, password);
      console.log("Login bem-sucedido:", response);
      login(response.token, {
        id: response.id,
        username: response.username,
        email: response.email,
      });
      navigate("/");
    } catch (error) {
      console.error("Erro no login:", error);
      toast({
        title: "Erro ao entrar",
        description: "Usuário ou senha inválidos. Verifique suas credenciais.",
        variant: "destructive",
      });
    } finally {
      if (document.body.contains(document.activeElement)) {
        // Opcional: manter foco ou limpar loading
      }
      setLoading(false);
    }
  };
  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md animate-fade-in border-border bg-card">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10">
            <MessageCircle className="h-7 w-7 text-primary" />
          </div>
          <CardTitle className="text-2xl font-bold text-foreground">Entrar</CardTitle>
          <CardDescription className="text-muted-foreground">
            Faça login para acessar seus chats
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-2">
              <Label htmlFor="username">Usuário</Label>
              <Input
                id="username"
                type="text"
                placeholder="Digite seu usuário"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="bg-secondary border-border"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Senha</Label>
              <Input
                id="password"
                type="password"
                placeholder="Digite sua senha"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="bg-secondary border-border"
              />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Entrando..." : "Entrar"}
            </Button>
          </form>
          <p className="mt-4 text-center text-sm text-muted-foreground">
            Não tem conta?{" "}
            <Link to="/register" className="text-primary hover:underline">
              Cadastre-se
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
};
export default LoginPage;
