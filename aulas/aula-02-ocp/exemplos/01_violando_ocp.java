/**
 * EXEMPLO 01 — Violando o OCP
 *
 * Cenario: sistema de e-commerce que calcula descontos por tipo de cliente
 * e gera relatorios em diferentes formatos.
 *
 * PROBLEMA: sempre que um novo tipo de cliente ou formato de relatorio surgir,
 * precisamos MODIFICAR classes existentes — que ja foram testadas e funcionam.
 *
 * Isso viola o OCP: o codigo deveria estar FECHADO para modificacao.
 */

import java.util.List;
import java.util.Arrays;

// -----------------------------------------------------------------------
// VIOLACAO 1: Calculadora de desconto com if/else crescente
// -----------------------------------------------------------------------
class CalculadoraDeDesconto {

    /**
     * Para adicionar um novo tipo de cliente (ex: "CORPORATIVO"),
     * precisamos ABRIR esta classe e MODIFICAR este metodo.
     *
     * Riscos:
     * - Quebrar o calculo de clientes existentes (COMUM, VIP, PREMIUM)
     * - Re-testar todos os casos ja funcionando
     * - Conflito de merge se outro dev esta mexendo aqui ao mesmo tempo
     */
    public double calcular(String tipoCliente, double valorCompra) {
        if (tipoCliente.equals("COMUM")) {
            return valorCompra * 0.05;  // 5% de desconto

        } else if (tipoCliente.equals("VIP")) {
            return valorCompra * 0.10;  // 10% de desconto

        } else if (tipoCliente.equals("PREMIUM")) {
            // Regra mais complexa: 20% mas com teto de R$ 500
            double desconto = valorCompra * 0.20;
            return Math.min(desconto, 500.0);

        }
        // Quando vier "CORPORATIVO": nova condicao aqui.
        // Quando vier "FUNCIONARIO": mais uma condicao.
        // Quando vier "PARCEIRO":    mais uma condicao.
        // Este metodo nunca para de crescer.

        return 0;
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 2: Gerador de relatorio com switch crescente
// -----------------------------------------------------------------------
class GeradorDeRelatorio {

    private List<String> dados;

    public GeradorDeRelatorio(List<String> dados) {
        this.dados = dados;
    }

    /**
     * Para adicionar suporte a XML ou Markdown:
     * - Abrir esta classe
     * - Adicionar novo case no switch
     * - Re-testar os formatos ja existentes (CSV, JSON)
     */
    public String gerar(String formato) {
        switch (formato) {
            case "CSV":
                StringBuilder csv = new StringBuilder();
                for (String item : dados) {
                    csv.append(item).append(",");
                }
                return csv.toString();

            case "JSON":
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < dados.size(); i++) {
                    json.append("\"").append(dados.get(i)).append("\"");
                    if (i < dados.size() - 1) json.append(",");
                }
                json.append("]");
                return json.toString();

            // Quando precisarmos de XML:
            // case "XML": ...
            // Quando precisarmos de Markdown:
            // case "MARKDOWN": ...

            default:
                throw new IllegalArgumentException("Formato nao suportado: " + formato);
        }
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 3: Processador de pagamento com multiplos ifs
// -----------------------------------------------------------------------
class ProcessadorDePagamento {

    /**
     * Para adicionar PIX ou criptomoeda:
     * - Modificar este metodo
     * - Re-testar cartao de credito e boleto
     * - Aumentar a complexidade ciclomatica do metodo
     */
    public void processar(String metodoPagamento, double valor) {
        if (metodoPagamento.equals("CARTAO_CREDITO")) {
            System.out.println("[CARTAO] Cobrando R$ " + valor + " no cartao de credito");
            System.out.println("[CARTAO] Enviando para a operadora...");
            System.out.println("[CARTAO] Aguardando autorizacao...");

        } else if (metodoPagamento.equals("BOLETO")) {
            System.out.println("[BOLETO] Gerando boleto de R$ " + valor);
            System.out.println("[BOLETO] Codigo de barras: 1234.5678 9012.3456");
            System.out.println("[BOLETO] Vencimento: 3 dias uteis");

        } else if (metodoPagamento.equals("TRANSFERENCIA")) {
            System.out.println("[TED] Transferencia de R$ " + valor);
            System.out.println("[TED] Agencia: 0001 | Conta: 12345-6");

        }
        // Se adicionar PIX, mais um bloco aqui.
        // Se adicionar criptomoeda, mais um bloco.
        // O metodo nunca fica estavel.
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO DO PROBLEMA
// -----------------------------------------------------------------------
public class ViolandoOCP {

    public static void main(String[] args) {
        System.out.println("=== CALCULADORA DE DESCONTO ===\n");

        CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();
        double compra = 1000.0;

        System.out.println("Desconto COMUM:   R$ " + calculadora.calcular("COMUM", compra));
        System.out.println("Desconto VIP:     R$ " + calculadora.calcular("VIP", compra));
        System.out.println("Desconto PREMIUM: R$ " + calculadora.calcular("PREMIUM", compra));
        // Para adicionar CORPORATIVO aqui, precisamos abrir CalculadoraDeDesconto.
        System.out.println();

        System.out.println("=== GERADOR DE RELATORIO ===\n");

        List<String> dados = Arrays.asList("Ana", "Bruno", "Carlos");
        GeradorDeRelatorio gerador = new GeradorDeRelatorio(dados);

        System.out.println("CSV:  " + gerador.gerar("CSV"));
        System.out.println("JSON: " + gerador.gerar("JSON"));
        // Para adicionar XML aqui, precisamos abrir GeradorDeRelatorio.
        System.out.println();

        System.out.println("=== PROCESSADOR DE PAGAMENTO ===\n");

        ProcessadorDePagamento processador = new ProcessadorDePagamento();
        processador.processar("CARTAO_CREDITO", 250.00);
        System.out.println();
        processador.processar("BOLETO", 250.00);

        /*
         * RESUMO DOS PROBLEMAS:
         *
         * 1. Cada novo tipo/formato/metodo MODIFICA uma classe existente
         * 2. Risco de regressao a cada modificacao
         * 3. Necessidade de re-testar casos que nao mudaram
         * 4. Classes que "nunca ficam prontas" — sempre tem um novo caso
         * 5. Conflitos de merge em equipes (todos mexem nos mesmos ifs)
         * 6. Complexidade ciclomatica cresce sem controle
         */
    }
}
