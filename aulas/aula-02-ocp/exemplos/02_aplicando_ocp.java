/**
 * EXEMPLO 02 — Aplicando o OCP
 *
 * A mesma logica do exemplo anterior, refatorada para seguir o OCP.
 *
 * Solucao: definir uma abstracao (interface) para cada comportamento variavel.
 * Novas variacoes sao adicionadas criando novas implementacoes —
 * sem tocar no codigo existente.
 *
 * Para adicionar um novo tipo de cliente: implemente DescontoStrategy.
 * Para adicionar um novo formato: implemente FormatoRelatorio.
 * Para adicionar um novo metodo de pagamento: implemente GatewayDePagamento.
 */

import java.util.List;
import java.util.Arrays;

// ======================================================================
// SOLUCAO 1: Desconto por tipo de cliente — via interface
// ======================================================================

// A abstracao — FECHADA para modificacao
interface DescontoStrategy {
    double calcular(double valorCompra);
    String getTipoCliente();
}

// Implementacoes — ABERTAS para extensao (novas classes, sem tocar nas existentes)

class DescontoComum implements DescontoStrategy {
    public double calcular(double valorCompra) {
        return valorCompra * 0.05;
    }
    public String getTipoCliente() { return "COMUM"; }
}

class DescontoVIP implements DescontoStrategy {
    public double calcular(double valorCompra) {
        return valorCompra * 0.10;
    }
    public String getTipoCliente() { return "VIP"; }
}

class DescontoPremium implements DescontoStrategy {
    private static final double TETO = 500.0;

    public double calcular(double valorCompra) {
        double desconto = valorCompra * 0.20;
        return Math.min(desconto, TETO);
    }
    public String getTipoCliente() { return "PREMIUM"; }
}

// Para adicionar CORPORATIVO: apenas esta nova classe. Nada mais muda.
class DescontoCorporativo implements DescontoStrategy {
    public double calcular(double valorCompra) {
        // Regra: 25% de desconto, mas apenas em compras acima de R$ 2000
        return valorCompra > 2000 ? valorCompra * 0.25 : valorCompra * 0.05;
    }
    public String getTipoCliente() { return "CORPORATIVO"; }
}

// A calculadora — FECHADA para modificacao (nao muda quando novos tipos surgem)
class CalculadoraDeDescontoOCP {

    private final DescontoStrategy strategy;

    public CalculadoraDeDescontoOCP(DescontoStrategy strategy) {
        this.strategy = strategy;
    }

    public double calcular(double valorCompra) {
        return strategy.calcular(valorCompra);
    }

    public void exibirDesconto(double valorCompra) {
        double desconto = calcular(valorCompra);
        System.out.printf("Cliente %s | Compra: R$ %.2f | Desconto: R$ %.2f | Total: R$ %.2f%n",
            strategy.getTipoCliente(), valorCompra, desconto, valorCompra - desconto);
    }
}

// ======================================================================
// SOLUCAO 2: Relatorio em diferentes formatos — via interface
// ======================================================================

// A abstracao — FECHADA para modificacao
interface FormatoRelatorio {
    String formatar(List<String> dados);
    String getNomeFormato();
}

// Implementacoes — ABERTAS para extensao

class FormatoCSV implements FormatoRelatorio {
    public String formatar(List<String> dados) {
        return String.join(",", dados);
    }
    public String getNomeFormato() { return "CSV"; }
}

class FormatoJSON implements FormatoRelatorio {
    public String formatar(List<String> dados) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < dados.size(); i++) {
            json.append("\"").append(dados.get(i)).append("\"");
            if (i < dados.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    public String getNomeFormato() { return "JSON"; }
}

// Para adicionar XML: apenas esta nova classe. GeradorDeRelatorioOCP nao muda.
class FormatoXML implements FormatoRelatorio {
    public String formatar(List<String> dados) {
        StringBuilder xml = new StringBuilder("<lista>\n");
        for (String item : dados) {
            xml.append("  <item>").append(item).append("</item>\n");
        }
        xml.append("</lista>");
        return xml.toString();
    }
    public String getNomeFormato() { return "XML"; }
}

// O gerador — FECHADO para modificacao (nao muda quando novos formatos surgem)
class GeradorDeRelatorioOCP {

    private final FormatoRelatorio formato;

    public GeradorDeRelatorioOCP(FormatoRelatorio formato) {
        this.formato = formato;
    }

    public void gerar(List<String> dados) {
        String resultado = formato.formatar(dados);
        System.out.println("[" + formato.getNomeFormato() + "] " + resultado);
    }
}

// ======================================================================
// SOLUCAO 3: Metodo de pagamento — via interface
// ======================================================================

// A abstracao — FECHADA para modificacao
interface GatewayDePagamento {
    void processar(double valor);
    String getMetodo();
}

// Implementacoes — ABERTAS para extensao

class GatewayCartaoCredito implements GatewayDePagamento {
    public void processar(double valor) {
        System.out.println("[CARTAO] Cobrando R$ " + String.format("%.2f", valor));
        System.out.println("[CARTAO] Enviando para operadora... Autorizado!");
    }
    public String getMetodo() { return "CARTAO_CREDITO"; }
}

class GatewayBoleto implements GatewayDePagamento {
    public void processar(double valor) {
        System.out.println("[BOLETO] Gerando boleto de R$ " + String.format("%.2f", valor));
        System.out.println("[BOLETO] Codigo: 1234.5678 9012.3456 | Venc: 3 dias uteis");
    }
    public String getMetodo() { return "BOLETO"; }
}

// Para adicionar PIX: apenas esta nova classe. ProcessadorOCP nao muda.
class GatewayPIX implements GatewayDePagamento {
    public void processar(double valor) {
        System.out.println("[PIX] Chave: pagamentos@empresa.com");
        System.out.println("[PIX] Valor: R$ " + String.format("%.2f", valor) + " | Transferencia instantanea");
    }
    public String getMetodo() { return "PIX"; }
}

// O processador — FECHADO para modificacao
class ProcessadorDePagamentoOCP {

    private final GatewayDePagamento gateway;

    public ProcessadorDePagamentoOCP(GatewayDePagamento gateway) {
        this.gateway = gateway;
    }

    public void processar(double valor) {
        System.out.println("Iniciando pagamento via " + gateway.getMetodo() + "...");
        gateway.processar(valor);
        System.out.println("Pagamento concluido.\n");
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class AplicandoOCP {

    public static void main(String[] args) {
        System.out.println("=== DESCONTOS POR TIPO DE CLIENTE ===\n");

        double compra = 1000.0;
        List<DescontoStrategy> estrategias = Arrays.asList(
            new DescontoComum(),
            new DescontoVIP(),
            new DescontoPremium(),
            new DescontoCorporativo()  // adicionado sem modificar CalculadoraDeDescontoOCP
        );

        for (DescontoStrategy estrategia : estrategias) {
            new CalculadoraDeDescontoOCP(estrategia).exibirDesconto(compra);
        }

        System.out.println("\n=== RELATORIOS EM DIFERENTES FORMATOS ===\n");

        List<String> dados = Arrays.asList("Ana", "Bruno", "Carlos");
        List<FormatoRelatorio> formatos = Arrays.asList(
            new FormatoCSV(),
            new FormatoJSON(),
            new FormatoXML()  // adicionado sem modificar GeradorDeRelatorioOCP
        );

        GeradorDeRelatorioOCP gerador = new GeradorDeRelatorioOCP(new FormatoCSV());
        for (FormatoRelatorio formato : formatos) {
            new GeradorDeRelatorioOCP(formato).gerar(dados);
        }

        System.out.println("\n=== METODOS DE PAGAMENTO ===\n");

        List<GatewayDePagamento> gateways = Arrays.asList(
            new GatewayCartaoCredito(),
            new GatewayBoleto(),
            new GatewayPIX()  // adicionado sem modificar ProcessadorDePagamentoOCP
        );

        for (GatewayDePagamento gateway : gateways) {
            new ProcessadorDePagamentoOCP(gateway).processar(250.00);
        }

        /*
         * OBSERVE:
         *
         * - CalculadoraDeDescontoOCP, GeradorDeRelatorioOCP e ProcessadorDePagamentoOCP
         *   nao foram tocados para adicionar CORPORATIVO, XML e PIX.
         *
         * - Testes para COMUM, VIP, CSV, JSON, CARTAO e BOLETO continuam validos —
         *   nenhuma linha dessas classes foi alterada.
         *
         * - Cada nova estrategia e um arquivo novo, independente, testavel isoladamente.
         */
    }
}
