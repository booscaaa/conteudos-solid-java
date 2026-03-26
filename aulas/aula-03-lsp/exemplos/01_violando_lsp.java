/**
 * EXEMPLO 01 — Violando o LSP
 *
 * Cenario: hierarquias de classes onde subclasses quebram o contrato
 * definido pela superclasse, tornando a substituicao impossivel.
 *
 * PROBLEMA: quando usamos a subclasse no lugar da superclasse,
 * o comportamento do programa muda de forma inesperada — ou pior,
 * o programa quebra com excecoes ou resultados errados.
 *
 * Isso viola o LSP: qualquer subclasse deve poder substituir
 * sua superclasse SEM que o programa precise saber disso.
 */

// -----------------------------------------------------------------------
// VIOLACAO 1: O problema classico do Retangulo / Quadrado
// -----------------------------------------------------------------------

class Retangulo {

    protected double largura;
    protected double altura;

    public void setLargura(double largura) {
        this.largura = largura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double calcularArea() {
        return largura * altura;
    }
}

/**
 * Matematicamente, um Quadrado E um Retangulo.
 * Mas em OOP, isso cria um problema serio.
 *
 * Para manter a invariante do quadrado (todos os lados iguais),
 * somos forcados a SOBRESCREVER o comportamento dos setters
 * de um jeito que viola o contrato original.
 */
class Quadrado extends Retangulo {

    @Override
    public void setLargura(double largura) {
        // Tentando manter a invariante: lado = lado
        this.largura = largura;
        this.altura = largura;   // <- sobrescreve a altura sem avisar!
    }

    @Override
    public void setAltura(double altura) {
        this.altura = altura;
        this.largura = altura;   // <- sobrescreve a largura sem avisar!
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 2: Ave que nao voa na hierarquia de Aves
// -----------------------------------------------------------------------

class Ave {

    protected String nome;

    public Ave(String nome) {
        this.nome = nome;
    }

    /**
     * CONTRATO: toda Ave pode voar.
     * Quem usa Ave assume que voar() funciona.
     */
    public void voar() {
        System.out.println(nome + " esta voando!");
    }

    public void comer() {
        System.out.println(nome + " esta comendo.");
    }
}

class Pardal extends Ave {
    public Pardal() { super("Pardal"); }
    // voar() herdado — funciona perfeitamente. Contrato OK.
}

class Aguia extends Ave {
    public Aguia() { super("Aguia"); }
    // voar() herdado — funciona perfeitamente. Contrato OK.
}

/**
 * PROBLEMA: Pinguim E uma Ave no mundo real,
 * mas NAO pode voar. Forcamos a heranca e quebramos o contrato.
 */
class Pinguim extends Ave {
    public Pinguim() { super("Pinguim"); }

    @Override
    public void voar() {
        // Opcao 1: nao fazer nada (silenciosa e perigosa!)
        // Opcao 2: lancara uma excecao (quebra o programa!)
        throw new UnsupportedOperationException("Pinguim nao pode voar!");
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 3: ContaBancaria — conta somente leitura que herda de conta normal
// -----------------------------------------------------------------------

class ContaBancaria {

    protected String titular;
    protected double saldo;

    public ContaBancaria(String titular, double saldoInicial) {
        this.titular = titular;
        this.saldo = saldoInicial;
    }

    /**
     * CONTRATO: depositar sempre aumenta o saldo.
     *           sacar diminui o saldo (se houver fundos).
     */
    public void depositar(double valor) {
        this.saldo += valor;
        System.out.println("[DEPOSITO] R$ " + valor + " adicionado. Saldo: R$ " + saldo);
    }

    public void sacar(double valor) {
        if (valor > saldo) {
            System.out.println("[ERRO] Saldo insuficiente.");
            return;
        }
        this.saldo -= valor;
        System.out.println("[SAQUE] R$ " + valor + " retirado. Saldo: R$ " + saldo);
    }

    public double getSaldo() { return saldo; }
}

/**
 * PROBLEMA: conta somente leitura nao pode sacar nem depositar.
 * Mas herda esses metodos e os quebra lancando excecoes.
 * Codigo que usa ContaBancaria vai QUEBRAR ao receber uma ContaSomenteLeitura.
 */
class ContaSomenteLeitura extends ContaBancaria {

    public ContaSomenteLeitura(String titular, double saldo) {
        super(titular, saldo);
    }

    @Override
    public void depositar(double valor) {
        throw new UnsupportedOperationException("Esta conta nao permite depositos!");
    }

    @Override
    public void sacar(double valor) {
        throw new UnsupportedOperationException("Esta conta nao permite saques!");
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO DOS PROBLEMAS
// -----------------------------------------------------------------------
public class ViolandoLSP {

    // Funcao que assume que qualquer Retangulo tem lados independentes
    static void testarRetangulo(Retangulo r) {
        r.setLargura(5);
        r.setAltura(3);

        double area = r.calcularArea();
        // Esperamos 5 * 3 = 15
        System.out.println("Area esperada: 15.0 | Area calculada: " + area);
        if (area != 15.0) {
            System.out.println("*** CONTRATO QUEBRADO! O programa nao pode confiar nessa classe. ***");
        }
    }

    // Funcao que assume que qualquer Ave pode voar
    static void fazerAveVoar(Ave ave) {
        System.out.print("Tentando fazer " + ave.nome + " voar... ");
        ave.voar(); // -> vai explodir com Pinguim!
    }

    // Funcao que usa qualquer ContaBancaria para depositar
    static void processarDeposito(ContaBancaria conta, double valor) {
        conta.depositar(valor); // -> vai explodir com ContaSomenteLeitura!
    }

    public static void main(String[] args) {
        System.out.println("=== VIOLACAO 1: Retangulo / Quadrado ===\n");

        System.out.print("[Retangulo normal]  -> ");
        testarRetangulo(new Retangulo());  // area = 15 — OK

        System.out.print("[Quadrado]          -> ");
        testarRetangulo(new Quadrado());   // area = 9 — ERRADO! (3*3 pois setLargura sobrescreveu altura)

        System.out.println("\n=== VIOLACAO 2: Ave que nao voa ===\n");

        fazerAveVoar(new Pardal());  // OK
        fazerAveVoar(new Aguia());   // OK

        try {
            fazerAveVoar(new Pinguim()); // -> EXCECAO!
        } catch (UnsupportedOperationException e) {
            System.out.println("EXCECAO: " + e.getMessage());
            System.out.println("*** O programa QUEBROU porque confiou no contrato de Ave! ***");
        }

        System.out.println("\n=== VIOLACAO 3: Conta Somente Leitura ===\n");

        ContaBancaria contaNormal = new ContaBancaria("Ana", 1000);
        processarDeposito(contaNormal, 500); // OK

        try {
            ContaBancaria contaLeitura = new ContaSomenteLeitura("Bruno", 500);
            processarDeposito(contaLeitura, 200); // -> EXCECAO!
        } catch (UnsupportedOperationException e) {
            System.out.println("EXCECAO: " + e.getMessage());
            System.out.println("*** O codigo nao deveria precisar saber o tipo real da conta! ***");
        }

        /*
         * RESUMO DOS PROBLEMAS:
         *
         * 1. Quadrado quebra o contrato matematico de Retangulo (setLargura != setAltura independentes)
         * 2. Pinguim lanca excecao onde Ave prometeu comportamento normal
         * 3. ContaSomenteLeitura lanca excecao onde ContaBancaria prometeu operacoes
         * 4. Em TODOS os casos: quem chama o metodo e obrigado a saber o tipo real
         *    para se proteger — isso e EXATAMENTE o que o LSP proibe.
         */
    }
}
