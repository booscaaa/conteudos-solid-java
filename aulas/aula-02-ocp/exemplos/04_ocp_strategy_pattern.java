/**
 * EXEMPLO 04 — Strategy Pattern como implementacao direta do OCP
 *
 * O Strategy Pattern e o padrao de design mais alinhado ao OCP.
 * Ele permite definir uma familia de algoritmos, encapsular cada um,
 * e torna-los intercambiaveis em tempo de execucao.
 *
 * Este exemplo mostra:
 *   1. Strategy Pattern classico aplicado a calculos de frete
 *   2. Composicao de strategies (combinando comportamentos)
 *   3. Strategy com estado (quando a strategy precisa de dados proprios)
 *   4. Selecao de strategy em tempo de execucao
 */

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// ======================================================================
// CENARIO: Sistema de calculo de frete para e-commerce
//
// Regra de negocio varia por:
//   - Tipo de entrega (Sedex, PAC, Transportadora, Retirada)
//   - Regiao do pais
//   - Peso do produto
//   - Programa de fidelidade do cliente
// ======================================================================

// Modelo do pedido
class PedidoFrete {
    private final double pesoKg;
    private final String cepDestino;
    private final double valorTotal;
    private final String regiaoDestino;

    public PedidoFrete(double pesoKg, String cepDestino, double valorTotal, String regiaoDestino) {
        this.pesoKg = pesoKg;
        this.cepDestino = cepDestino;
        this.valorTotal = valorTotal;
        this.regiaoDestino = regiaoDestino;
    }

    public double getPesoKg()          { return pesoKg; }
    public String getCepDestino()      { return cepDestino; }
    public double getValorTotal()      { return valorTotal; }
    public String getRegiaoDestino()   { return regiaoDestino; }
}

// -----------------------------------------------------------------------
// A INTERFACE DE STRATEGY — o contrato imutavel
// -----------------------------------------------------------------------
interface FreteStrategy {
    double calcular(PedidoFrete pedido);
    int getPrazoEmDias(PedidoFrete pedido);
    String getNome();
}

// -----------------------------------------------------------------------
// STRATEGIES CONCRETAS
// -----------------------------------------------------------------------

class FreteSedex implements FreteStrategy {
    private static final double TAXA_BASE = 15.00;
    private static final double TAXA_POR_KG = 3.50;

    public double calcular(PedidoFrete pedido) {
        return TAXA_BASE + (pedido.getPesoKg() * TAXA_POR_KG);
    }

    public int getPrazoEmDias(PedidoFrete pedido) {
        return pedido.getRegiaoDestino().equals("SUL/SUDESTE") ? 1 : 3;
    }

    public String getNome() { return "SEDEX"; }
}

class FretePAC implements FreteStrategy {
    private static final double TAXA_BASE = 8.00;
    private static final double TAXA_POR_KG = 2.00;

    public double calcular(PedidoFrete pedido) {
        return TAXA_BASE + (pedido.getPesoKg() * TAXA_POR_KG);
    }

    public int getPrazoEmDias(PedidoFrete pedido) {
        return pedido.getRegiaoDestino().equals("SUL/SUDESTE") ? 5 : 10;
    }

    public String getNome() { return "PAC"; }
}

// Strategy com estado proprio — a transportadora tem tabela de precos propria
class FreteTransportadora implements FreteStrategy {
    private final String nomeTransportadora;
    private final Map<String, Double> tabelaPorRegiao;

    public FreteTransportadora(String nome, Map<String, Double> tabela) {
        this.nomeTransportadora = nome;
        this.tabelaPorRegiao = tabela;
    }

    public double calcular(PedidoFrete pedido) {
        double taxaRegiao = tabelaPorRegiao.getOrDefault(pedido.getRegiaoDestino(), 50.0);
        return taxaRegiao + (pedido.getPesoKg() * 1.50);
    }

    public int getPrazoEmDias(PedidoFrete pedido) { return 7; }
    public String getNome() { return "TRANSPORTADORA " + nomeTransportadora; }
}

// Strategy "nula" — retirada na loja, sem frete
class FreteRetiradaNaLoja implements FreteStrategy {
    public double calcular(PedidoFrete pedido) { return 0.0; }
    public int getPrazoEmDias(PedidoFrete pedido) { return 0; }
    public String getNome() { return "RETIRADA NA LOJA"; }
}

// -----------------------------------------------------------------------
// STRATEGY DECORADA — adiciona comportamento sem modificar as existentes
// Exemplo: desconto de frete para clientes fidelidade
// -----------------------------------------------------------------------
class FreteComDescontoFidelidade implements FreteStrategy {
    private final FreteStrategy estrategiaBase;
    private final double percentualDesconto;

    // Envolve qualquer strategy existente — sem modificar nenhuma delas
    public FreteComDescontoFidelidade(FreteStrategy estrategiaBase, double percentualDesconto) {
        this.estrategiaBase = estrategiaBase;
        this.percentualDesconto = percentualDesconto;
    }

    public double calcular(PedidoFrete pedido) {
        double valorOriginal = estrategiaBase.calcular(pedido);
        return valorOriginal * (1 - percentualDesconto);
    }

    public int getPrazoEmDias(PedidoFrete pedido) {
        return estrategiaBase.getPrazoEmDias(pedido);
    }

    public String getNome() {
        return estrategiaBase.getNome() + " (FIDELIDADE " + (int)(percentualDesconto * 100) + "%)";
    }
}

// -----------------------------------------------------------------------
// O CONTEXTO — usa a strategy sem saber qual e
// FECHADO para modificacao: nunca muda quando novas strategies surgem
// -----------------------------------------------------------------------
class CalculadoraDeFrete {

    private FreteStrategy strategy;

    public CalculadoraDeFrete(FreteStrategy strategy) {
        this.strategy = strategy;
    }

    // Permite trocar a strategy em tempo de execucao
    public void setStrategy(FreteStrategy strategy) {
        this.strategy = strategy;
    }

    public void exibirOpcao(PedidoFrete pedido) {
        double valor = strategy.calcular(pedido);
        int prazo = strategy.getPrazoEmDias(pedido);
        System.out.printf("  %-35s | R$ %6.2f | %d dia(s)%n",
            strategy.getNome(), valor, prazo);
    }
}

// -----------------------------------------------------------------------
// SELETOR DE STRATEGY em tempo de execucao
// -----------------------------------------------------------------------
class SeletorDeFrete {

    // Seleciona a melhor opcao de frete com base em criterios do cliente
    // Mais um exemplo de OCP: adicionar nova regra = nova implementacao
    public FreteStrategy selecionarMaisBarato(List<FreteStrategy> opcoes, PedidoFrete pedido) {
        return opcoes.stream()
            .min((a, b) -> Double.compare(a.calcular(pedido), b.calcular(pedido)))
            .orElseThrow(() -> new IllegalStateException("Nenhuma opcao de frete disponivel"));
    }

    public FreteStrategy selecionarMaisRapido(List<FreteStrategy> opcoes, PedidoFrete pedido) {
        return opcoes.stream()
            .min((a, b) -> Integer.compare(a.getPrazoEmDias(pedido), b.getPrazoEmDias(pedido)))
            .orElseThrow(() -> new IllegalStateException("Nenhuma opcao de frete disponivel"));
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class OCPStrategyPattern {

    public static void main(String[] args) {
        PedidoFrete pedido = new PedidoFrete(2.5, "01310-100", 350.00, "SUL/SUDESTE");

        System.out.println("=== OPCOES DE FRETE ===");
        System.out.println("Pedido: " + pedido.getPesoKg() + "kg | Destino: " + pedido.getCepDestino());
        System.out.println();

        // Tabela da transportadora Rapida
        Map<String, Double> tabelaRapida = new HashMap<>();
        tabelaRapida.put("SUL/SUDESTE", 12.00);
        tabelaRapida.put("NORTE/NORDESTE", 35.00);
        tabelaRapida.put("CENTRO-OESTE", 22.00);

        List<FreteStrategy> opcoes = Arrays.asList(
            new FreteSedex(),
            new FretePAC(),
            new FreteTransportadora("Rapida Entregas", tabelaRapida),
            new FreteRetiradaNaLoja()
        );

        CalculadoraDeFrete calculadora = new CalculadoraDeFrete(new FreteSedex());
        System.out.printf("  %-35s | %-10s | %-10s%n", "Modalidade", "Valor", "Prazo");
        System.out.println("  " + "-".repeat(60));

        for (FreteStrategy opcao : opcoes) {
            calculadora.setStrategy(opcao);
            calculadora.exibirOpcao(pedido);
        }

        System.out.println("\n=== DESCONTOS FIDELIDADE (20%) ===\n");

        // Decorando strategies existentes — nenhuma delas foi modificada
        List<FreteStrategy> opcoesComDesconto = Arrays.asList(
            new FreteComDescontoFidelidade(new FreteSedex(), 0.20),
            new FreteComDescontoFidelidade(new FretePAC(), 0.20)
        );

        System.out.printf("  %-35s | %-10s | %-10s%n", "Modalidade", "Valor", "Prazo");
        System.out.println("  " + "-".repeat(60));

        for (FreteStrategy opcao : opcoesComDesconto) {
            calculadora.setStrategy(opcao);
            calculadora.exibirOpcao(pedido);
        }

        System.out.println("\n=== SELECAO AUTOMATICA ===\n");

        SeletorDeFrete seletor = new SeletorDeFrete();

        FreteStrategy maisBarato = seletor.selecionarMaisBarato(opcoes, pedido);
        FreteStrategy maisRapido = seletor.selecionarMaisRapido(opcoes, pedido);

        System.out.println("Mais barato: " + maisBarato.getNome()
            + " | R$ " + String.format("%.2f", maisBarato.calcular(pedido)));
        System.out.println("Mais rapido: " + maisRapido.getNome()
            + " | " + maisRapido.getPrazoEmDias(pedido) + " dia(s)");

        /*
         * PONTOS-CHAVE DO STRATEGY + OCP:
         *
         * 1. CalculadoraDeFrete nunca mudou — so conhece FreteStrategy.
         *
         * 2. FreteComDescontoFidelidade DECORA strategies existentes
         *    sem modificar nenhuma delas (padrao Decorator + OCP).
         *
         * 3. SeletorDeFrete tambem nao precisara mudar quando novos
         *    tipos de frete forem adicionados — so adicione a lista.
         *
         * 4. A strategy pode ser trocada em tempo de execucao (setStrategy),
         *    permitindo comportamento dinamico sem condicionais.
         *
         * 5. Cada strategy tem seus proprios dados (FreteTransportadora
         *    tem tabelaPorRegiao) — encapsulamento completo.
         */
    }
}
