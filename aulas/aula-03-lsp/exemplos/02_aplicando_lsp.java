/**
 * EXEMPLO 02 — Aplicando o LSP
 *
 * A mesma logica do exemplo anterior, refatorada para seguir o LSP.
 *
 * Solucao: redesenhar as hierarquias para que subclasses NUNCA
 * precisem quebrar o contrato da superclasse.
 *
 * Principio: prefira modelar pelo COMPORTAMENTO REAL, nao pelo
 * relacionamento "e-um" do mundo real. No mundo real, Quadrado e Retangulo.
 * No codigo, eles tem COMPORTAMENTOS INCOMPATIVEIS — entao nao devem herdar.
 */

// ======================================================================
// SOLUCAO 1: Retangulo / Quadrado — sem heranca problemtica
// ======================================================================

/**
 * Abstrai apenas o que e comum: calcular area.
 * Nao expoe setters com contrato incompativel.
 */
interface Forma {
    double calcularArea();
    String descricao();
}

class RetanguloLSP implements Forma {
    private final double largura;
    private final double altura;

    public RetanguloLSP(double largura, double altura) {
        this.largura = largura;
        this.altura = altura;
    }

    @Override
    public double calcularArea() {
        return largura * altura;
    }

    @Override
    public String descricao() {
        return "Retangulo " + largura + "x" + altura;
    }
}

class QuadradoLSP implements Forma {
    private final double lado;

    public QuadradoLSP(double lado) {
        this.lado = lado;
    }

    @Override
    public double calcularArea() {
        return lado * lado;
    }

    @Override
    public String descricao() {
        return "Quadrado lado=" + lado;
    }
}

// ======================================================================
// SOLUCAO 2: Hierarquia de Aves — separando capacidades
// ======================================================================

/**
 * Avezinha: comportamento que TODAS as aves tem.
 * Nao inclui voar — pois nem toda ave voa.
 */
interface AveLSP {
    String getNome();
    void comer();
    void emitirSom();
}

/**
 * Capacidade de voar: so aves que realmente voam implementam.
 */
interface AveVoadora {
    void voar();
    double getAltitudeMaxima();
}

/**
 * Capacidade de nadar: so aves que realmente nadam implementam.
 */
interface AveNadadora {
    void nadar();
}

// Pardal: voa — implementa ambas as interfaces que fazem sentido
class PardalLSP implements AveLSP, AveVoadora {
    @Override public String getNome() { return "Pardal"; }
    @Override public void comer() { System.out.println("Pardal comendo sementes."); }
    @Override public void emitirSom() { System.out.println("Pardal: piu piu!"); }
    @Override public void voar() { System.out.println("Pardal voando baixinho..."); }
    @Override public double getAltitudeMaxima() { return 100; }
}

// Aguia: voa e bem alto
class AguiaLSP implements AveLSP, AveVoadora {
    @Override public String getNome() { return "Aguia"; }
    @Override public void comer() { System.out.println("Aguia cacando peixes."); }
    @Override public void emitirSom() { System.out.println("Aguia: kreee!"); }
    @Override public void voar() { System.out.println("Aguia planando nas correntes de ar..."); }
    @Override public double getAltitudeMaxima() { return 3000; }
}

// Pinguim: nao voa, mas nada — sem mentiras, sem excecoes
class PinguimLSP implements AveLSP, AveNadadora {
    @Override public String getNome() { return "Pinguim"; }
    @Override public void comer() { System.out.println("Pinguim comendo peixe."); }
    @Override public void emitirSom() { System.out.println("Pinguim: gronk gronk!"); }
    @Override public void nadar() { System.out.println("Pinguim nadando a 30 km/h!"); }
}

// Pato: voa E nada — implementa ambas as capacidades
class PatoLSP implements AveLSP, AveVoadora, AveNadadora {
    @Override public String getNome() { return "Pato"; }
    @Override public void comer() { System.out.println("Pato comendo no lago."); }
    @Override public void emitirSom() { System.out.println("Pato: quack quack!"); }
    @Override public void voar() { System.out.println("Pato voando sobre o lago."); }
    @Override public double getAltitudeMaxima() { return 500; }
    @Override public void nadar() { System.out.println("Pato nadando graciosamente."); }
}

// ======================================================================
// SOLUCAO 3: ContaBancaria — hierarquia correta
// ======================================================================

/**
 * Contrato minimo: apenas consulta de saldo.
 * Operacoes sao capacidades separadas.
 */
interface Conta {
    String getTitular();
    double getSaldo();
}

interface ContaOperavel extends Conta {
    void depositar(double valor);
    void sacar(double valor);
}

// Conta corrente: pode tudo
class ContaCorrente implements ContaOperavel {
    private final String titular;
    private double saldo;

    public ContaCorrente(String titular, double saldoInicial) {
        this.titular = titular;
        this.saldo = saldoInicial;
    }

    @Override
    public void depositar(double valor) {
        saldo += valor;
        System.out.println("[DEPOSITO] R$ " + valor + " | Saldo: R$ " + saldo);
    }

    @Override
    public void sacar(double valor) {
        if (valor > saldo) { System.out.println("[ERRO] Saldo insuficiente."); return; }
        saldo -= valor;
        System.out.println("[SAQUE] R$ " + valor + " | Saldo: R$ " + saldo);
    }

    @Override public String getTitular() { return titular; }
    @Override public double getSaldo() { return saldo; }
}

// Conta investimento: pode depositar mas nao sacar livremente
class ContaInvestimento implements ContaOperavel {
    private final String titular;
    private double saldo;
    private final double taxaRentabilidade = 0.008; // 0.8% ao mes

    public ContaInvestimento(String titular, double aporte) {
        this.titular = titular;
        this.saldo = aporte;
    }

    @Override
    public void depositar(double valor) {
        saldo += valor;
        System.out.println("[APORTE] R$ " + valor + " | Saldo investido: R$ " + saldo);
    }

    @Override
    public void sacar(double valor) {
        // Saque com resgate — logica diferente, mas contrato mantido
        if (valor > saldo) { System.out.println("[ERRO] Saldo insuficiente para resgate."); return; }
        saldo -= valor;
        System.out.println("[RESGATE] R$ " + valor + " | Saldo restante: R$ " + saldo);
    }

    @Override public String getTitular() { return titular; }
    @Override public double getSaldo() { return saldo; }
}

// Conta somente leitura: NAO implementa ContaOperavel — e honesta sobre suas capacidades
class ContaVisualizacao implements Conta {
    private final String titular;
    private final double saldo;

    public ContaVisualizacao(String titular, double saldo) {
        this.titular = titular;
        this.saldo = saldo;
    }

    @Override public String getTitular() { return titular; }
    @Override public double getSaldo() { return saldo; }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO — tudo funciona sem surpresas
// -----------------------------------------------------------------------
import java.util.List;
import java.util.Arrays;

public class AplicandoLSP {

    // Funcao que aceita QUALQUER Forma — sempre funciona
    static void imprimirArea(Forma forma) {
        System.out.printf("%-25s -> Area: %.2f%n", forma.descricao(), forma.calcularArea());
    }

    // Funcao que faz qualquer AveVoadora voar — sem medo de excecao
    static void realizarVoo(AveVoadora ave) {
        System.out.println(ave.getClass().getSimpleName() + " decolando... altitude max: " + ave.getAltitudeMaxima() + "m");
        ave.voar();
    }

    // Funcao que deposita em qualquer ContaOperavel — sem medo de excecao
    static void receberDeposito(ContaOperavel conta, double valor) {
        System.out.print("[" + conta.getTitular() + "] ");
        conta.depositar(valor);
    }

    public static void main(String[] args) {
        System.out.println("=== SOLUCAO 1: Formas Geometricas ===\n");

        List<Forma> formas = Arrays.asList(
            new RetanguloLSP(5, 3),
            new QuadradoLSP(4),
            new RetanguloLSP(10, 2),
            new QuadradoLSP(7)
        );

        for (Forma f : formas) {
            imprimirArea(f);
        }

        System.out.println("\n=== SOLUCAO 2: Hierarquia de Aves ===\n");

        // Apenas aves que REALMENTE voam entram aqui
        List<AveVoadora> aves = Arrays.asList(new PardalLSP(), new AguiaLSP(), new PatoLSP());
        for (AveVoadora ave : aves) {
            realizarVoo(ave);
        }

        System.out.println();
        // Pinguim nada — e tratado como AveNadadora
        PinguimLSP pinguim = new PinguimLSP();
        System.out.print(pinguim.getNome() + " -> ");
        pinguim.nadar();

        System.out.println("\n=== SOLUCAO 3: Contas Bancarias ===\n");

        // Contas operaveis: deposito funciona em todas sem excecao
        List<ContaOperavel> contasOperaveis = Arrays.asList(
            new ContaCorrente("Ana", 1000),
            new ContaInvestimento("Bruno", 5000)
        );

        for (ContaOperavel conta : contasOperaveis) {
            receberDeposito(conta, 500);
        }

        System.out.println();

        // Conta de visualizacao: usada apenas para consulta — sem mentiras
        Conta contaVisualizacao = new ContaVisualizacao("Carlos", 3200);
        System.out.println("Visualizando conta de " + contaVisualizacao.getTitular()
            + ": R$ " + contaVisualizacao.getSaldo());

        /*
         * OBSERVE:
         *
         * - imprimirArea() nunca precisa checar o tipo real da Forma
         * - realizarVoo() nunca lanca excecao inesperada
         * - receberDeposito() nunca precisa try/catch para UnsupportedOperationException
         * - Cada hierarquia modela o COMPORTAMENTO, nao o "e-um" do mundo real
         *
         * A regra: se voce precisa de instanceof ou try/catch para usar uma subclasse,
         * e sinal de violacao do LSP.
         */
    }
}
