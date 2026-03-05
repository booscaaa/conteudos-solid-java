/**
 * EXEMPLO 04 — SRP Avancado: nivel de metodos e sinais de alerta
 *
 * Este exemplo mostra:
 *   1. SRP aplicado no nivel de metodos (metodos com uma responsabilidade)
 *   2. Como identificar violacoes pelo nome da classe
 *   3. O padrao "Extract Class" para refatorar violacoes
 *   4. Quando NAO e violacao do SRP (coesao x fragmentacao)
 */

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

// ======================================================================
// SINAL 1: Nome da classe usa "E" ou "Manager" ou "Helper" ou "Utils"
// Geralmente indicam multiplas responsabilidades
// ======================================================================

// RUIM — o nome "UsuarioManagerEEmailSender" grita que ha duas responsabilidades
class UsuarioManagerEEmailSender {
    public void criar(String nome) { /* ... */ }
    public void deletar(int id)    { /* ... */ }
    public void enviarEmail(String destino) { /* ... */ }
}

// BOM — nomes especificos e focados
class UsuarioService {
    public void criar(String nome) {
        System.out.println("[USUARIO] Criando: " + nome);
    }
    public void deletar(int id) {
        System.out.println("[USUARIO] Deletando id: " + id);
    }
}

class EmailService {
    public void enviar(String destino, String mensagem) {
        System.out.println("[EMAIL] Para: " + destino + " | " + mensagem);
    }
}

// ======================================================================
// SINAL 2: Metodo que faz mais de uma coisa
// ======================================================================

class ProcessadorDeTextoRuim {

    // RUIM — este metodo valida, transforma E persiste
    public void processarTexto(String texto) {
        // Passo 1: valida
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Texto invalido");
        }

        // Passo 2: transforma
        String processado = texto.trim().toLowerCase().replaceAll("\\s+", "_");

        // Passo 3: persiste
        System.out.println("[DB] Salvando: " + processado);
    }
}

class ProcessadorDeTexto {

    // BOM — cada metodo tem uma responsabilidade
    private void validar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Texto invalido");
        }
    }

    private String transformar(String texto) {
        return texto.trim().toLowerCase().replaceAll("\\s+", "_");
    }

    private void persistir(String texto) {
        System.out.println("[DB] Salvando: " + texto);
    }

    // O metodo principal ORQUESTRA os passos — sua responsabilidade e coordenar
    public void processar(String texto) {
        validar(texto);
        String transformado = transformar(texto);
        persistir(transformado);
    }
}

// ======================================================================
// SINAL 3: Classe dificil de testar porque tem muitas dependencias
// ======================================================================

// RUIM — RelatorioPesado depende de banco, email e formatador ao mesmo tempo
// Para testar qualquer coisa, precisamos mockar tudo
class RelatorioPesado {
    public void gerarEEnviar(List<String> dados) {
        // Busca no banco
        System.out.println("[DB] Buscando dados...");
        // Formata
        String csv = String.join(",", dados);
        // Envia email
        System.out.println("[EMAIL] Enviando CSV: " + csv);
    }
}

// BOM — cada parte pode ser testada separadamente
class RelatorioFormatter {
    public String formatarComoCSV(List<String> dados) {
        return String.join(",", dados);
    }

    public String formatarComoTabela(List<String> dados) {
        return dados.stream()
            .map(d -> "| " + d + " |")
            .collect(Collectors.joining("\n"));
    }
}

class RelatorioEnvioService {
    private final EmailService emailService;

    public RelatorioEnvioService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void enviar(String destino, String conteudo) {
        emailService.enviar(destino, "Relatorio: " + conteudo);
    }
}

// ======================================================================
// CONTRA-EXEMPLO: isso NAO e violacao do SRP
// ======================================================================

// Uma classe Pedido com varios metodos ainda pode ter UMA responsabilidade:
// representar e gerenciar o estado de um pedido.
// Todos esses metodos pertencem ao mesmo contexto.
class PedidoCoeso {
    private List<String> itens = new java.util.ArrayList<>();
    private String status = "ABERTO";

    // Todos esses metodos dizem respeito ao PEDIDO — nao ha violacao
    public void adicionarItem(String item)   { itens.add(item); }
    public void removerItem(String item)     { itens.remove(item); }
    public void confirmar()                  { this.status = "CONFIRMADO"; }
    public void cancelar()                   { this.status = "CANCELADO"; }
    public boolean estaConfirmado()          { return "CONFIRMADO".equals(status); }
    public double calcularTotal()            { return itens.size() * 10.0; } // simplificado
    public List<String> getItens()           { return itens; }
    public String getStatus()               { return status; }

    // Mas se adicionarmos isso aqui, VIOLA o SRP:
    // public void salvarNoBanco() { ... }   <- responsabilidade de repositorio
    // public void enviarEmail()  { ... }    <- responsabilidade de notificacao
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class SRPAvancado {

    public static void main(String[] args) {
        System.out.println("=== NIVEL DE METODOS ===\n");
        ProcessadorDeTexto processador = new ProcessadorDeTexto();
        processador.processar("  Ola Mundo  ");
        System.out.println();

        System.out.println("=== FORMATACAO E ENVIO SEPARADOS ===\n");
        List<String> dados = Arrays.asList("Ana", "Bruno", "Carlos");

        RelatorioFormatter formatter = new RelatorioFormatter();
        String csv    = formatter.formatarComoCSV(dados);
        String tabela = formatter.formatarComoTabela(dados);

        System.out.println("CSV:    " + csv);
        System.out.println("Tabela:\n" + tabela);
        System.out.println();

        EmailService email = new EmailService();
        RelatorioEnvioService envio = new RelatorioEnvioService(email);
        envio.enviar("gestor@empresa.com", csv);
        System.out.println();

        System.out.println("=== CLASSE COESA (SEM VIOLACAO) ===\n");
        PedidoCoeso pedido = new PedidoCoeso();
        pedido.adicionarItem("Notebook");
        pedido.adicionarItem("Mouse");
        pedido.confirmar();
        System.out.println("Status: " + pedido.getStatus());
        System.out.println("Itens: " + pedido.getItens());
        System.out.println("Total: R$ " + pedido.calcularTotal());

        /*
         * RESUMO DOS SINAIS DE ALERTA:
         *
         * 1. Nome da classe com "E", "Manager", "Helper", "Utils", "Common"
         * 2. Metodo que faz mais de uma coisa (multiplos comentarios "// Passo X")
         * 3. Classe com mais de 2-3 dependencias externas distintas
         * 4. "Para testar A, preciso configurar B, C e D"
         * 5. Descricao da classe usa a palavra "e" multiplas vezes
         *
         * LEMBRE-SE:
         * Coesao e o objetivo. Uma classe com 10 metodos pode ser coesa
         * se todos pertencerem ao mesmo contexto/ator.
         */
    }
}
