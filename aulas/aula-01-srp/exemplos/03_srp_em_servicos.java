/**
 * EXEMPLO 03 — SRP em Camadas de Servico
 *
 * Cenario: sistema de e-commerce com processamento de pedidos.
 *
 * Versao SEM SRP: um unico OrderService que faz tudo.
 * Versao COM SRP: responsabilidades divididas entre servicos especializados.
 *
 * Este exemplo e mais proximo de um sistema real.
 */

import java.util.List;
import java.util.ArrayList;

// -----------------------------------------------------------------------
// MODELOS DE DOMINIO
// -----------------------------------------------------------------------
class Produto {
    private String nome;
    private double preco;
    private int estoque;

    public Produto(String nome, double preco, int estoque) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public String getNome()    { return nome; }
    public double getPreco()   { return preco; }
    public int getEstoque()    { return estoque; }
    public void reduzirEstoque(int quantidade) { this.estoque -= quantidade; }
}

class ItemPedido {
    private Produto produto;
    private int quantidade;

    public ItemPedido(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto getProduto()    { return produto; }
    public int getQuantidade()     { return quantidade; }
    public double getSubtotal()    { return produto.getPreco() * quantidade; }
}

class Pedido {
    private int id;
    private String emailCliente;
    private List<ItemPedido> itens;
    private String status;

    public Pedido(int id, String emailCliente) {
        this.id = id;
        this.emailCliente = emailCliente;
        this.itens = new ArrayList<>();
        this.status = "PENDENTE";
    }

    public void adicionarItem(ItemPedido item) { itens.add(item); }
    public void setStatus(String status)        { this.status = status; }

    public int getId()                   { return id; }
    public String getEmailCliente()      { return emailCliente; }
    public List<ItemPedido> getItens()   { return itens; }
    public String getStatus()            { return status; }
}

// ======================================================================
// VERSAO SEM SRP — OrderService fazendo tudo
// ======================================================================
class OrderServiceGordoViolandoSRP {

    // Valida, calcula, verifica estoque, persiste, notifica, gera relatorio
    // TUDO em um lugar so — classico "God Object"
    public void processarPedido(Pedido pedido) {
        // 1. Validacao
        if (pedido.getItens().isEmpty()) {
            throw new IllegalStateException("Pedido sem itens");
        }

        // 2. Verificacao de estoque
        for (ItemPedido item : pedido.getItens()) {
            if (item.getProduto().getEstoque() < item.getQuantidade()) {
                throw new IllegalStateException(
                    "Estoque insuficiente para: " + item.getProduto().getNome()
                );
            }
        }

        // 3. Calculo do total
        double total = pedido.getItens().stream()
            .mapToDouble(ItemPedido::getSubtotal)
            .sum();
        System.out.println("Total calculado: R$ " + String.format("%.2f", total));

        // 4. Atualizacao de estoque
        for (ItemPedido item : pedido.getItens()) {
            item.getProduto().reduzirEstoque(item.getQuantidade());
        }

        // 5. Persistencia
        System.out.println("[DB] Salvando pedido #" + pedido.getId());
        pedido.setStatus("CONFIRMADO");

        // 6. Envio de email
        System.out.println("[EMAIL] Confirmacao enviada para: " + pedido.getEmailCliente());

        // 7. Geracao de relatorio
        System.out.println("[RELATORIO] Pedido #" + pedido.getId() + " registrado.");
    }
    // 7 responsabilidades em 1 metodo. Impossivel testar qualquer parte isolada.
}

// ======================================================================
// VERSAO COM SRP — cada servico tem uma unica responsabilidade
// ======================================================================

// Valida regras de negocio do pedido
class PedidoValidator {
    public void validar(Pedido pedido) {
        if (pedido.getItens().isEmpty()) {
            throw new IllegalStateException("Pedido nao pode estar vazio");
        }
        System.out.println("[VALIDACAO] Pedido #" + pedido.getId() + " valido.");
    }
}

// Verifica e atualiza o estoque dos produtos
class EstoqueService {
    public void verificarDisponibilidade(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            if (item.getProduto().getEstoque() < item.getQuantidade()) {
                throw new IllegalStateException(
                    "Sem estoque para: " + item.getProduto().getNome()
                );
            }
        }
        System.out.println("[ESTOQUE] Disponibilidade confirmada.");
    }

    public void reservarEstoque(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            item.getProduto().reduzirEstoque(item.getQuantidade());
        }
        System.out.println("[ESTOQUE] Estoque reservado.");
    }
}

// Calcula o valor total do pedido
class CalculadoraDePedido {
    public double calcularTotal(Pedido pedido) {
        return pedido.getItens().stream()
            .mapToDouble(ItemPedido::getSubtotal)
            .sum();
    }
}

// Persiste o pedido no banco
class PedidoRepository {
    public void salvar(Pedido pedido) {
        System.out.println("[DB] Pedido #" + pedido.getId() + " salvo com status: " + pedido.getStatus());
    }
}

// Envia notificacoes ao cliente
class PedidoNotificacaoService {
    public void enviarConfirmacao(Pedido pedido, double total) {
        System.out.println("[EMAIL] Para: " + pedido.getEmailCliente());
        System.out.println("[EMAIL] Pedido #" + pedido.getId()
            + " confirmado. Total: R$ " + String.format("%.2f", total));
    }
}

// Orquestra o fluxo — responsabilidade: coordenar os servicos
class PedidoOrchestrator {

    private final PedidoValidator validator;
    private final EstoqueService estoque;
    private final CalculadoraDePedido calculadora;
    private final PedidoRepository repository;
    private final PedidoNotificacaoService notificacao;

    public PedidoOrchestrator(
        PedidoValidator validator,
        EstoqueService estoque,
        CalculadoraDePedido calculadora,
        PedidoRepository repository,
        PedidoNotificacaoService notificacao
    ) {
        this.validator   = validator;
        this.estoque     = estoque;
        this.calculadora = calculadora;
        this.repository  = repository;
        this.notificacao = notificacao;
    }

    public void processarPedido(Pedido pedido) {
        validator.validar(pedido);
        estoque.verificarDisponibilidade(pedido);

        double total = calculadora.calcularTotal(pedido);
        System.out.println("[CALCULO] Total: R$ " + String.format("%.2f", total));

        estoque.reservarEstoque(pedido);
        pedido.setStatus("CONFIRMADO");
        repository.salvar(pedido);
        notificacao.enviarConfirmacao(pedido, total);
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class SRPEmServicos {

    public static void main(String[] args) {
        // Preparando dados
        Produto notebook  = new Produto("Notebook", 3500.00, 10);
        Produto mouse     = new Produto("Mouse", 150.00, 25);

        Pedido pedido = new Pedido(42, "cliente@email.com");
        pedido.adicionarItem(new ItemPedido(notebook, 1));
        pedido.adicionarItem(new ItemPedido(mouse, 2));

        System.out.println("=== PROCESSANDO PEDIDO #" + pedido.getId() + " ===\n");

        // Montando os servicos (em producao, isso seria feito por injecao de dependencia)
        PedidoOrchestrator orchestrator = new PedidoOrchestrator(
            new PedidoValidator(),
            new EstoqueService(),
            new CalculadoraDePedido(),
            new PedidoRepository(),
            new PedidoNotificacaoService()
        );

        orchestrator.processarPedido(pedido);

        /*
         * AGORA E POSSIVEL:
         *
         * - Testar PedidoValidator sem banco ou email
         * - Testar CalculadoraDePedido com pedidos montados na memoria
         * - Trocar EstoqueService por uma versao que consulta API externa
         * - Trocar PedidoNotificacaoService por um que envia SMS
         * - Tudo sem tocar no restante do codigo
         */
    }
}
