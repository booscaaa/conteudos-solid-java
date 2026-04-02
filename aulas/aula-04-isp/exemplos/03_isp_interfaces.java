/**
 * EXEMPLO 03 — Composicao de Interfaces com ISP
 *
 * Cenario: como usar heranca entre interfaces para compor capacidades
 * sem perder a granularidade do ISP.
 *
 * CONCEITO: interfaces podem herdar de outras interfaces, criando
 * "camadas de capacidade" que permitem tanto granularidade quanto
 * conveniencia para os casos mais completos.
 *
 * Isso e especialmente util em:
 *   - Sistemas de persistencia (CRUD dividido em partes)
 *   - Servicos com diferentes niveis de acesso
 *   - APIs com funcionalidades opcionais
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

// -----------------------------------------------------------------------
// EXEMPLO 1: Composicao em Camadas — Sistema de Arquivos
// -----------------------------------------------------------------------

/** Capacidade basica: ler conteudo */
interface Legivel {
    String ler();
    boolean existe();
}

/** Capacidade basica: escrever conteudo */
interface Gravavel {
    void escrever(String conteudo);
    void anexar(String conteudo);
}

/** Arquivo completo: le e escreve */
interface ArquivoCompleto extends Legivel, Gravavel {
    void excluir();
}

/** Stream de log: so grava (nunca le) */
interface StreamDeLog extends Gravavel {
    void gravarComTimestamp(String mensagem);
}

/** Arquivo somente leitura (ex: recurso empacotado no jar) */
class RecursoEmbarcado implements Legivel {

    private final String conteudo;

    public RecursoEmbarcado(String conteudo) {
        this.conteudo = conteudo;
    }

    @Override
    public String ler()    { return conteudo; }

    @Override
    public boolean existe() { return true; }
}

/** Arquivo de texto normal: le e escreve */
class ArquivoTexto implements ArquivoCompleto {

    private final String caminho;
    private StringBuilder conteudo = new StringBuilder();
    private boolean existe = false;

    public ArquivoTexto(String caminho) { this.caminho = caminho; }

    @Override
    public String ler() {
        if (!existe) throw new IllegalStateException("Arquivo nao existe: " + caminho);
        return conteudo.toString();
    }

    @Override
    public boolean existe() { return existe; }

    @Override
    public void escrever(String c) {
        conteudo = new StringBuilder(c);
        existe = true;
        System.out.println("[ARQUIVO " + caminho + "] Conteudo escrito.");
    }

    @Override
    public void anexar(String c) {
        conteudo.append(c);
        existe = true;
        System.out.println("[ARQUIVO " + caminho + "] Conteudo anexado.");
    }

    @Override
    public void excluir() {
        conteudo = new StringBuilder();
        existe = false;
        System.out.println("[ARQUIVO " + caminho + "] Excluido.");
    }
}

/** Log de aplicacao: so grava, nunca le */
class LogDeAplicacao implements StreamDeLog {

    private final String prefixo;

    public LogDeAplicacao(String prefixo) { this.prefixo = prefixo; }

    @Override
    public void escrever(String conteudo) {
        System.out.println("[" + prefixo + "] " + conteudo);
    }

    @Override
    public void anexar(String conteudo) {
        System.out.println("[" + prefixo + " +] " + conteudo);
    }

    @Override
    public void gravarComTimestamp(String mensagem) {
        System.out.println("[" + prefixo + "][" + System.currentTimeMillis() + "] " + mensagem);
    }
}

// -----------------------------------------------------------------------
// EXEMPLO 2: Servico de Notificacoes com Capacidades Opcionais
// -----------------------------------------------------------------------

interface EnviaEmail {
    void enviarEmail(String destinatario, String assunto, String corpo);
}

interface EnviaSMS {
    void enviarSMS(String telefone, String mensagem);
}

interface EnviaPush {
    void enviarPush(String dispositivoId, String titulo, String corpo);
}

interface EnviaWhatsApp {
    void enviarWhatsApp(String telefone, String mensagem);
}

/** Servico completo: email + sms + push */
interface NotificadorCompleto extends EnviaEmail, EnviaSMS, EnviaPush {}

/**
 * NotificadorEmail: so email.
 * Sistema legado que ainda nao tem SMS/Push.
 */
class NotificadorEmail implements EnviaEmail {

    @Override
    public void enviarEmail(String dest, String assunto, String corpo) {
        System.out.println("[EMAIL -> " + dest + "] " + assunto + ": " + corpo);
    }
}

/**
 * NotificadorMulticanal: email + sms + push.
 * Sistema moderno.
 */
class NotificadorMulticanal implements NotificadorCompleto {

    @Override
    public void enviarEmail(String dest, String assunto, String corpo) {
        System.out.println("[EMAIL -> " + dest + "] " + assunto);
    }

    @Override
    public void enviarSMS(String tel, String msg) {
        System.out.println("[SMS -> " + tel + "] " + msg);
    }

    @Override
    public void enviarPush(String id, String titulo, String corpo) {
        System.out.println("[PUSH -> " + id + "] " + titulo + ": " + corpo);
    }
}

/**
 * NotificadorWhatsApp: so WhatsApp.
 * Plugin adicionado depois, sem alterar as outras interfaces.
 */
class NotificadorWhatsApp implements EnviaWhatsApp {

    @Override
    public void enviarWhatsApp(String tel, String msg) {
        System.out.println("[WHATSAPP -> " + tel + "] " + msg);
    }
}

// -----------------------------------------------------------------------
// EXEMPLO 3: Autenticacao com capacidades especificas
// -----------------------------------------------------------------------

interface Autenticavel {
    boolean autenticar(String credencial);
}

interface Renovavel {
    String renovarToken(String tokenExpirado);
}

interface Revogavel {
    void revogar(String credencial);
}

/** Autenticacao basica por senha — so autentica */
class AutenticacaoSenha implements Autenticavel {

    @Override
    public boolean autenticar(String credencial) {
        // Credencial minima: 8 chars
        return credencial != null && credencial.length() >= 8;
    }
}

/** Autenticacao JWT — autentica E renova E revoga */
class AutenticacaoJWT implements Autenticavel, Renovavel, Revogavel {

    private final List<String> tokensRevogados = new ArrayList<>();

    @Override
    public boolean autenticar(String token) {
        if (tokensRevogados.contains(token)) return false;
        return token != null && token.startsWith("eyJ"); // simplificado
    }

    @Override
    public String renovarToken(String tokenExpirado) {
        return "eyJnovo." + System.currentTimeMillis();
    }

    @Override
    public void revogar(String token) {
        tokensRevogados.add(token);
        System.out.println("[JWT] Token revogado: " + token.substring(0, Math.min(20, token.length())) + "...");
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class ISPComposicaoDeInterfaces {

    /** Processa qualquer fonte legivel — nao importa se e arquivo ou recurso */
    static void processarConteudo(Legivel fonte) {
        if (fonte.existe()) {
            System.out.println("Conteudo: " + fonte.ler());
        } else {
            System.out.println("Fonte nao encontrada.");
        }
    }

    /** Notifica por email — qualquer implementacao de EnviaEmail */
    static void notificarUsuario(EnviaEmail notificador, String email, String msg) {
        notificador.enviarEmail(email, "Aviso do sistema", msg);
    }

    /** Valida acesso — qualquer Autenticavel */
    static boolean validarAcesso(Autenticavel auth, String credencial) {
        boolean ok = auth.autenticar(credencial);
        System.out.println("Autenticacao [" + credencial.substring(0, Math.min(10, credencial.length())) + "...]: " + (ok ? "OK" : "NEGADO"));
        return ok;
    }

    public static void main(String[] args) {
        System.out.println("=== COMPOSICAO 1: Sistema de Arquivos ===\n");

        RecursoEmbarcado recurso = new RecursoEmbarcado("Conteudo do jar");
        ArquivoTexto arquivo     = new ArquivoTexto("/tmp/dados.txt");
        LogDeAplicacao log       = new LogDeAplicacao("APP");

        processarConteudo(recurso); // OK

        // arquivo.escrever("Hello, ISP!"); processarConteudo(arquivo);
        arquivo.escrever("Hello, ISP!");
        processarConteudo(arquivo);

        // Log so grava — nao compila como Legivel
        // processarConteudo(log); <- ERRO DE COMPILACAO

        log.escrever("Sistema iniciado.");
        log.gravarComTimestamp("Primeira requisicao recebida.");

        System.out.println("\n=== COMPOSICAO 2: Notificacoes ===\n");

        NotificadorEmail    emailOnly = new NotificadorEmail();
        NotificadorMulticanal multi   = new NotificadorMulticanal();

        // Ambos aceitam como EnviaEmail
        notificarUsuario(emailOnly, "ana@empresa.com", "Bem-vinda!");
        notificarUsuario(multi,     "bob@empresa.com", "Novo pedido criado.");

        // Multi tambem faz SMS e Push
        multi.enviarSMS("+5511999990000", "Codigo: 1234");
        multi.enviarPush("device_abc123", "Pedido aprovado", "Seu pedido #42 foi aprovado.");

        System.out.println("\n=== COMPOSICAO 3: Autenticacao ===\n");

        AutenticacaoSenha authSenha = new AutenticacaoSenha();
        AutenticacaoJWT   authJWT   = new AutenticacaoJWT();

        validarAcesso(authSenha, "senha123");     // OK (>= 8 chars)
        validarAcesso(authSenha, "curta");         // NEGADO

        String token = "eyJhbGciOiJIUzI1NiJ9.payload.signature";
        validarAcesso(authJWT, token);             // OK

        authJWT.revogar(token);
        validarAcesso(authJWT, token);             // NEGADO — token revogado

        String novoToken = authJWT.renovarToken(token);
        System.out.println("Novo token: " + novoToken);

        System.out.println("\n=== RESUMO ===");
        System.out.println("✓ Composicao de interfaces sem herdar comportamento indesejado");
        System.out.println("✓ Clientes recebem apenas a interface que precisam");
        System.out.println("✓ Novos implementadores podem escolher o subconjunto certo");
        System.out.println("✓ Extensao facil: adicionar EnviaSlack sem tocar no existente");
    }
}
