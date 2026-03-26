/**
 * EXEMPLO 03 — LSP e Contratos (Pre e Pos Condicoes)
 *
 * O LSP vai alem de "nao lancar excecoes". Ele define regras formais
 * sobre pre-condicoes e pos-condicoes que subclasses devem respeitar.
 *
 * REGRAS:
 *   - Pre-condicoes: subclasse NUNCA pode ser mais restritiva que a superclasse
 *   - Pos-condicoes: subclasse NUNCA pode ser mais permissiva (garantir menos)
 *   - Invariantes: propriedades que devem ser verdadeiras antes e depois
 *
 * Analogia: se o contrato diz "entrega em ate 5 dias", a empresa pode entregar
 * em 3 dias (mais garantias) mas NAO pode entregar em 7 dias (menos garantias).
 */

// -----------------------------------------------------------------------
// CENARIO 1: Validador de pedidos — pre-condicao mais restritiva (ERRADO)
// -----------------------------------------------------------------------

class ProcessadorDePedido {

    /**
     * PRE-CONDICAO: valor deve ser positivo (> 0)
     * POS-CONDICAO: retorna true se o pedido foi aceito
     */
    public boolean processarPedido(double valor) {
        if (valor <= 0) {
            System.out.println("[ERRO] Valor invalido: " + valor);
            return false;
        }
        System.out.println("[OK] Pedido de R$ " + valor + " processado.");
        return true;
    }
}

/**
 * VIOLA LSP: restringe a pre-condicao (exige valor minimo de R$ 50).
 * Codigo que usa ProcessadorDePedido e passa valor = 20
 * espera que funcione, mas quebra com esta subclasse.
 */
class ProcessadorDePedidoVIP_ERRADO extends ProcessadoraDePedido {

    @Override
    public boolean processarPedido(double valor) {
        if (valor < 50) { // <- pre-condicao MAIS restritiva — viola LSP!
            System.out.println("[VIP] Pedido minimo e R$ 50. Pedido rejeitado.");
            return false;
        }
        System.out.println("[VIP] Pedido de R$ " + valor + " aprovado com prioridade.");
        return true;
    }
}

// -----------------------------------------------------------------------
// CENARIO 2: Gerenciador de estoque — pos-condicao mais fraca (ERRADO)
// -----------------------------------------------------------------------

class GerenciadorDeEstoque {

    protected int quantidade;

    public GerenciadorDeEstoque(int quantidadeInicial) {
        this.quantidade = quantidadeInicial;
    }

    /**
     * POS-CONDICAO: apos adicionar, this.quantidade > quantidade_antes.
     * O estoque SEMPRE aumenta quando adicionamos itens.
     */
    public void adicionarItens(int qtd) {
        if (qtd <= 0) return;
        quantidade += qtd;
        System.out.println("[ESTOQUE] +" + qtd + " itens. Total: " + quantidade);
    }

    public int getQuantidade() { return quantidade; }
}

/**
 * VIOLA LSP: a pos-condicao e mais fraca.
 * Apos chamar adicionarItens(), o estoque NAO aumenta necessariamente.
 * Codigo que confia na pos-condicao da superclasse vai tomar decisoes erradas.
 */
class GerenciadorComPerda extends GerenciadorDeEstoque {

    private final double taxaDePerda;

    public GerenciadorComPerda(int quantidadeInicial, double taxaDePerda) {
        super(quantidadeInicial);
        this.taxaDePerda = taxaDePerda;
    }

    @Override
    public void adicionarItens(int qtd) {
        if (qtd <= 0) return;
        int perdas = (int)(qtd * taxaDePerda);
        int efetivos = qtd - perdas;

        // VIOLA: se taxa = 100%, estoque nao aumenta — pos-condicao quebrada!
        quantidade += efetivos;
        System.out.println("[ESTOQUE] +" + qtd + " itens, -" + perdas + " perdas. Total: " + quantidade);
    }
}

// -----------------------------------------------------------------------
// FORMA CORRETA: respeitando contratos
// -----------------------------------------------------------------------

interface Processador {
    /**
     * CONTRATO DOCUMENTADO:
     * Pre: valor > 0
     * Pos: retorna true sse pedido foi aceito no sistema
     */
    boolean processarPedido(double valor);
}

class ProcessadorPadrao implements Processador {

    @Override
    public boolean processarPedido(double valor) {
        // Respeita a pre-condicao: valor > 0
        if (valor <= 0) return false;
        System.out.println("[PADRAO] Pedido R$ " + valor + " processado.");
        return true;
    }
}

/**
 * CORRETO: nao restringe a pre-condicao — qualquer valor > 0 e aceito.
 * Apenas ADICIONA comportamento (prioridade) sem quebrar o contrato.
 */
class ProcessadorVIP implements Processador {

    @Override
    public boolean processarPedido(double valor) {
        // Pre-condicao IGUAL a da interface: valor > 0
        if (valor <= 0) return false;

        // Pos-condicao respeitada: se aceito, retorna true
        System.out.println("[VIP] Pedido R$ " + valor + " com PRIORIDADE MAXIMA.");
        return true;
        // Nota: se tiver pedido minimo VIP, isso deve ser configuracao
        // aplicada ANTES de chamar este metodo — nao responsabilidade do processador.
    }
}

/**
 * CORRETO: o gerenciador com perda documenta e respeita sua pos-condicao.
 * A invariante "estoque pode nao aumentar" esta no CONTRATO da propria interface.
 */
interface EstoqueComPerda {
    /**
     * Adiciona itens ao estoque. Taxa de perda pode reduzir o ganho efetivo.
     * Pre: qtd > 0
     * Pos: quantidade >= quantidade_antes (nunca diminui, pode nao aumentar se perda = 100%)
     */
    void adicionarItens(int qtd);
    int getQuantidade();
}

class EstoqueFisico implements EstoqueComPerda {
    private int quantidade;
    private final double taxaDePerda;

    public EstoqueFisico(int inicial, double taxaDePerda) {
        this.quantidade = inicial;
        this.taxaDePerda = taxaDePerda;
    }

    @Override
    public void adicionarItens(int qtd) {
        if (qtd <= 0) return;
        int perdas = (int)(qtd * taxaDePerda);
        quantidade += (qtd - perdas);
        System.out.println("[FISICO] +" + qtd + ", -" + perdas + " perdas. Total: " + quantidade);
    }

    @Override
    public int getQuantidade() { return quantidade; }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class LSPContratos {

    static void usarProcessador(Processador p, double valor) {
        // Codigo que confia no contrato: valor > 0 sera processado
        boolean resultado = p.processarPedido(valor);
        System.out.println("   -> Resultado: " + (resultado ? "ACEITO" : "REJEITADO"));
    }

    public static void main(String[] args) {
        System.out.println("=== PROCESSADORES — Contrato respeitado ===\n");

        Processador padrao = new ProcessadorPadrao();
        Processador vip    = new ProcessadorVIP();

        System.out.println("Pedido R$ 30.00:");
        usarProcessador(padrao, 30.0); // ACEITO
        usarProcessador(vip, 30.0);    // ACEITO — mesmo com valor "baixo"

        System.out.println("\nPedido R$ 200.00:");
        usarProcessador(padrao, 200.0); // ACEITO
        usarProcessador(vip, 200.0);    // ACEITO com prioridade

        System.out.println("\n=== ESTOQUE — Contrato documentado e respeitado ===\n");

        EstoqueComPerda estoque = new EstoqueFisico(100, 0.10); // 10% de perda
        System.out.println("Quantidade inicial: " + estoque.getQuantidade());

        estoque.adicionarItens(50);
        System.out.println("Quantidade apos adicionar 50: " + estoque.getQuantidade());

        estoque.adicionarItens(20);
        System.out.println("Quantidade final: " + estoque.getQuantidade());

        /*
         * REGRAS RESUMIDAS:
         *
         * Pre-condicao:  subclasse pode RELAXAR (aceitar mais), nunca RESTRINGIR (aceitar menos)
         * Pos-condicao:  subclasse pode GARANTIR MAIS, nunca GARANTIR MENOS
         * Invariante:    propriedades documentadas devem ser mantidas
         *
         * Violacao silenciosa (sem excecao) e a mais perigosa —
         * o programa "funciona" mas produz resultados errados.
         */
    }
}
