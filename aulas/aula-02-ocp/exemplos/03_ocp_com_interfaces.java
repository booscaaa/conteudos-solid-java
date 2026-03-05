/**
 * EXEMPLO 03 — OCP com Interfaces: sistema de notificacoes e exportacao
 *
 * Dois cenarios praticos mostrando OCP com interfaces em Java:
 *
 * Cenario A: Sistema de notificacoes multicanal
 *   - Email, SMS, Push Notification, Slack
 *   - Novas formas de notificar sem modificar o NotificadorService
 *
 * Cenario B: Sistema de exportacao de dados
 *   - CSV, JSON, PDF, Excel
 *   - Novos formatos sem modificar o ExportadorService
 *
 * Em ambos os casos, a logica central (o "orquestrador") nunca muda.
 * So as implementacoes variam.
 */

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

// ======================================================================
// CENARIO A: Sistema de Notificacoes
// ======================================================================

// Modelo de dominio
class Mensagem {
    private final String titulo;
    private final String corpo;
    private final String destinatario;

    public Mensagem(String titulo, String corpo, String destinatario) {
        this.titulo = titulo;
        this.corpo = corpo;
        this.destinatario = destinatario;
    }

    public String getTitulo()       { return titulo; }
    public String getCorpo()        { return corpo; }
    public String getDestinatario() { return destinatario; }
}

// A abstracao — a interface que nunca muda
interface CanalDeNotificacao {
    void enviar(Mensagem mensagem);
    String getNomeCanal();
}

// Implementacoes dos canais

class CanalEmail implements CanalDeNotificacao {
    public void enviar(Mensagem mensagem) {
        System.out.println("[EMAIL] Para: " + mensagem.getDestinatario());
        System.out.println("[EMAIL] Assunto: " + mensagem.getTitulo());
        System.out.println("[EMAIL] Corpo: " + mensagem.getCorpo());
    }
    public String getNomeCanal() { return "Email"; }
}

class CanalSMS implements CanalDeNotificacao {
    private static final int LIMITE_CHARS = 160;

    public void enviar(Mensagem mensagem) {
        String sms = mensagem.getTitulo() + ": " + mensagem.getCorpo();
        if (sms.length() > LIMITE_CHARS) {
            sms = sms.substring(0, LIMITE_CHARS - 3) + "...";
        }
        System.out.println("[SMS] Para: " + mensagem.getDestinatario() + " | " + sms);
    }
    public String getNomeCanal() { return "SMS"; }
}

class CanalPushNotification implements CanalDeNotificacao {
    public void enviar(Mensagem mensagem) {
        System.out.println("[PUSH] Device: " + mensagem.getDestinatario());
        System.out.println("[PUSH] Titulo: " + mensagem.getTitulo());
        System.out.println("[PUSH] Preview: " + mensagem.getCorpo().substring(0, Math.min(50, mensagem.getCorpo().length())));
    }
    public String getNomeCanal() { return "Push"; }
}

// Adicionado sem tocar em nenhuma das classes acima nem em NotificadorService
class CanalSlack implements CanalDeNotificacao {
    public void enviar(Mensagem mensagem) {
        System.out.println("[SLACK] Canal: #" + mensagem.getDestinatario());
        System.out.println("[SLACK] *" + mensagem.getTitulo() + "*");
        System.out.println("[SLACK] " + mensagem.getCorpo());
    }
    public String getNomeCanal() { return "Slack"; }
}

// O servico central — FECHADO para modificacao
// Nao sabe nada sobre Email, SMS ou Slack. So conhece CanalDeNotificacao.
class NotificadorService {

    private final List<CanalDeNotificacao> canais;

    public NotificadorService(List<CanalDeNotificacao> canais) {
        this.canais = canais;
    }

    // Este metodo nunca mudara, mesmo que 10 novos canais sejam adicionados
    public void notificar(Mensagem mensagem) {
        System.out.println("--- Enviando via " + canais.size() + " canal(is) ---");
        for (CanalDeNotificacao canal : canais) {
            try {
                canal.enviar(mensagem);
                System.out.println("   [OK] " + canal.getNomeCanal());
            } catch (Exception e) {
                System.out.println("   [ERRO] " + canal.getNomeCanal() + ": " + e.getMessage());
            }
        }
        System.out.println("--- Notificacao concluida ---\n");
    }
}

// ======================================================================
// CENARIO B: Sistema de Exportacao de Dados
// ======================================================================

// Modelo de dados
class DadosParaExportar {
    private final String titulo;
    private final List<List<String>> linhas;

    public DadosParaExportar(String titulo) {
        this.titulo = titulo;
        this.linhas = new ArrayList<>();
    }

    public void adicionarLinha(String... valores) {
        linhas.add(Arrays.asList(valores));
    }

    public String getTitulo()            { return titulo; }
    public List<List<String>> getLinhas() { return linhas; }
}

// A abstracao — nunca muda
interface ExportadorDeArquivo {
    String exportar(DadosParaExportar dados);
    String getExtensao();
}

// Implementacoes dos exportadores

class ExportadorCSV implements ExportadorDeArquivo {
    public String exportar(DadosParaExportar dados) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(dados.getTitulo()).append("\n");
        for (List<String> linha : dados.getLinhas()) {
            sb.append(String.join(";", linha)).append("\n");
        }
        return sb.toString();
    }
    public String getExtensao() { return "csv"; }
}

class ExportadorJSON implements ExportadorDeArquivo {
    public String exportar(DadosParaExportar dados) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"titulo\": \"").append(dados.getTitulo()).append("\",\n");
        sb.append("  \"registros\": [\n");
        List<List<String>> linhas = dados.getLinhas();
        for (int i = 0; i < linhas.size(); i++) {
            sb.append("    [");
            sb.append(String.join(", ",
                linhas.get(i).stream().map(v -> "\"" + v + "\"").toArray(String[]::new)));
            sb.append("]");
            if (i < linhas.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
    public String getExtensao() { return "json"; }
}

// Adicionado depois sem modificar ExportadorService
class ExportadorMarkdown implements ExportadorDeArquivo {
    public String exportar(DadosParaExportar dados) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(dados.getTitulo()).append("\n\n");
        for (List<String> linha : dados.getLinhas()) {
            sb.append("| ").append(String.join(" | ", linha)).append(" |\n");
        }
        return sb.toString();
    }
    public String getExtensao() { return "md"; }
}

// O servico de exportacao — FECHADO para modificacao
class ExportadorService {

    private final ExportadorDeArquivo exportador;

    public ExportadorService(ExportadorDeArquivo exportador) {
        this.exportador = exportador;
    }

    // Logica de orquestracao que nunca muda, independente do formato
    public void exportar(DadosParaExportar dados) {
        String conteudo = exportador.exportar(dados);
        String nomeArquivo = dados.getTitulo().toLowerCase().replace(" ", "_")
            + "." + exportador.getExtensao();

        // Em producao, gravaria em disco ou nuvem
        System.out.println("--- Exportando: " + nomeArquivo + " ---");
        System.out.println(conteudo);
        System.out.println("--- Arquivo gerado ---\n");
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class OCPComInterfaces {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  CENARIO A: SISTEMA DE NOTIFICACOES");
        System.out.println("========================================\n");

        Mensagem mensagem = new Mensagem(
            "Pedido Confirmado",
            "Seu pedido #1234 foi confirmado e esta sendo preparado. Previsao: 3 dias uteis.",
            "cliente@email.com"
        );

        // Notifica por todos os canais — adionar Slack nao mudou nada aqui
        NotificadorService notificador = new NotificadorService(Arrays.asList(
            new CanalEmail(),
            new CanalSMS(),
            new CanalPushNotification(),
            new CanalSlack()
        ));
        notificador.notificar(mensagem);

        System.out.println("========================================");
        System.out.println("  CENARIO B: SISTEMA DE EXPORTACAO");
        System.out.println("========================================\n");

        DadosParaExportar dados = new DadosParaExportar("Relatorio de Vendas");
        dados.adicionarLinha("Ana Silva", "Notebook", "R$ 3500");
        dados.adicionarLinha("Bruno Costa", "Mouse", "R$ 150");
        dados.adicionarLinha("Carla Mendes", "Teclado", "R$ 280");

        // Exporta nos tres formatos — Markdown adicionado sem tocar ExportadorService
        for (ExportadorDeArquivo exportador : Arrays.asList(
            new ExportadorCSV(),
            new ExportadorJSON(),
            new ExportadorMarkdown()
        )) {
            new ExportadorService(exportador).exportar(dados);
        }

        /*
         * BENEFICIOS OBSERVADOS:
         *
         * 1. CanalSlack e ExportadorMarkdown foram adicionados depois
         *    sem modificar NotificadorService nem ExportadorService.
         *
         * 2. Para testar CanalSlack, criamos apenas um objeto CanalSlack
         *    e chamamos enviar() — sem instanciar mais nada.
         *
         * 3. A lista de canais/exportadores pode vir de configuracao externa,
         *    banco de dados ou injecao de dependencia — total flexibilidade.
         *
         * 4. Desativar um canal e so remover da lista — nenhuma logica condicional.
         */
    }
}
