/**
 * EXEMPLO 04 — DIP Avancado
 *
 * Cenario: aplicacoes reais com multiplas camadas, decorators, composicoes
 * e como o DIP se integra com os outros principios SOLID.
 *
 * TOPICOS:
 *   1. Layered Architecture — camadas dependendo de abstracoes
 *   2. Decorator pattern com DIP — adicionar comportamento sem alterar codigo
 *   3. Strategy + DIP — algoritmos intercambiaveis por injecao
 *   4. DIP + OCP — extender sem modificar via injecao
 *   5. Como DIP completa o SOLID
 */

import java.util.*;
import java.time.LocalDateTime;

// -----------------------------------------------------------------------
// TOPICO 1: Layered Architecture com DIP
// Controller -> UseCase -> Repository (todas as setas apontam para abstracoes)
// -----------------------------------------------------------------------

// Camada de Dominio (sem dependencias externas)
record Produto(String id, String nome, double preco) {}

// Porta de saida (definida no dominio, implementada na infraestrutura)
interface ProdutoRepository {
    Optional<Produto> findById(String id);
    List<Produto> findAll();
    void save(Produto produto);
}

// Porta de entrada (contrato do use case)
interface ProdutoUseCase {
    Produto criarProduto(String nome, double preco);
    List<Produto> listarProdutos();
    Optional<Produto> buscarProduto(String id);
}

// Use case (alto nivel — pura logica de negocio)
class ProdutoUseCaseImpl implements ProdutoUseCase {

    private final ProdutoRepository repository;

    public ProdutoUseCaseImpl(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Produto criarProduto(String nome, double preco) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome obrigatorio");
        if (preco <= 0) throw new IllegalArgumentException("Preco deve ser positivo");

        Produto produto = new Produto(UUID.randomUUID().toString().substring(0, 8), nome, preco);
        repository.save(produto);
        return produto;
    }

    @Override
    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    @Override
    public Optional<Produto> buscarProduto(String id) {
        return repository.findById(id);
    }
}

// Controller (alto nivel — orquestra request/response)
class ProdutoController {

    private final ProdutoUseCase useCase;

    public ProdutoController(ProdutoUseCase useCase) {
        this.useCase = useCase;
    }

    public void criarProduto(String nome, double preco) {
        try {
            Produto criado = useCase.criarProduto(nome, preco);
            System.out.println("[201 Created] " + criado.id() + " — " + criado.nome() + " R$" + criado.preco());
        } catch (IllegalArgumentException e) {
            System.out.println("[400 Bad Request] " + e.getMessage());
        }
    }

    public void listarProdutos() {
        List<Produto> produtos = useCase.listarProdutos();
        System.out.println("[200 OK] " + produtos.size() + " produto(s):");
        produtos.forEach(p -> System.out.println("  - " + p.id() + " | " + p.nome() + " R$" + p.preco()));
    }
}

// Infraestrutura (baixo nivel — implementa as portas)
class InMemoryProdutoRepository implements ProdutoRepository {

    private final Map<String, Produto> store = new LinkedHashMap<>();

    @Override
    public Optional<Produto> findById(String id) { return Optional.ofNullable(store.get(id)); }

    @Override
    public List<Produto> findAll() { return new ArrayList<>(store.values()); }

    @Override
    public void save(Produto produto) { store.put(produto.id(), produto); }
}

// -----------------------------------------------------------------------
// TOPICO 2: Decorator com DIP — adiciona comportamento sem alterar o original
// -----------------------------------------------------------------------

interface CacheService<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    void invalidate(K key);
}

/** Implementacao real de cache */
class InMemoryCacheService<K, V> implements CacheService<K, V> {

    private final Map<K, V> cache = new HashMap<>();

    @Override
    public Optional<V> get(K key) { return Optional.ofNullable(cache.get(key)); }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        System.out.println("[Cache] Stored: " + key);
    }

    @Override
    public void invalidate(K key) {
        cache.remove(key);
        System.out.println("[Cache] Invalidated: " + key);
    }
}

/**
 * LoggingCacheDecorator: adiciona log sem modificar InMemoryCacheService.
 * Recebe qualquer CacheService — DIP em acao.
 * O "decorado" pode ser trocado sem alterar este decorator.
 */
class LoggingCacheDecorator<K, V> implements CacheService<K, V> {

    private final CacheService<K, V> delegate; // <- abstracão

    public LoggingCacheDecorator(CacheService<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<V> get(K key) {
        Optional<V> result = delegate.get(key);
        System.out.println("[Cache Log] GET " + key + " -> " + (result.isPresent() ? "HIT" : "MISS"));
        return result;
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        System.out.println("[Cache Log] PUT " + key);
    }

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
        System.out.println("[Cache Log] INVALIDATE " + key);
    }
}

// -----------------------------------------------------------------------
// TOPICO 3: Strategy + DIP — algoritmo intercambiavel via injecao
// -----------------------------------------------------------------------

interface DescontoStrategy {
    double calcular(double valorOriginal, String clienteId);
    String descricao();
}

class SemDesconto implements DescontoStrategy {
    @Override public double calcular(double v, String c) { return v; }
    @Override public String descricao()                  { return "Sem desconto"; }
}

class DescontoFidelidade implements DescontoStrategy {
    @Override
    public double calcular(double v, String c) {
        double desconto = v * 0.10; // 10%
        System.out.println("[Fidelidade] 10% de desconto: -R$" + String.format("%.2f", desconto));
        return v - desconto;
    }
    @Override public String descricao() { return "Desconto fidelidade 10%"; }
}

class DescontoBlackFriday implements DescontoStrategy {
    @Override
    public double calcular(double v, String c) {
        double desconto = v * 0.30; // 30%
        System.out.println("[Black Friday] 30% de desconto: -R$" + String.format("%.2f", desconto));
        return v - desconto;
    }
    @Override public String descricao() { return "Black Friday 30%"; }
}

/**
 * CarrinhoService: recebe a estrategia de desconto injetada.
 * Nao sabe qual e — e uma abstencao.
 * Pode ser trocada sem alterar uma linha de CarrinhoService.
 */
class CarrinhoService {

    private final DescontoStrategy desconto;

    public CarrinhoService(DescontoStrategy desconto) {
        this.desconto = desconto;
    }

    public double finalizar(String clienteId, List<Double> itens) {
        double subtotal = itens.stream().mapToDouble(Double::doubleValue).sum();
        double total    = desconto.calcular(subtotal, clienteId);
        System.out.printf("[Carrinho] %s | subtotal=R$%.2f | total=R$%.2f (%s)%n",
            clienteId, subtotal, total, desconto.descricao());
        return total;
    }
}

// -----------------------------------------------------------------------
// TOPICO 4: Pipeline com DIP — processamento em etapas intercambiaveis
// -----------------------------------------------------------------------

interface ProcessadorPedido {
    boolean processar(Map<String, Object> contexto);
    String nome();
}

class ValidacaoEstoque implements ProcessadorPedido {
    @Override
    public boolean processar(Map<String, Object> ctx) {
        System.out.println("[ValidacaoEstoque] Verificando disponibilidade...");
        return true; // simulado
    }
    @Override public String nome() { return "ValidacaoEstoque"; }
}

class ProcessamentoPagamento implements ProcessadorPedido {
    @Override
    public boolean processar(Map<String, Object> ctx) {
        System.out.println("[ProcessamentoPagamento] Cobrando cartao...");
        return true; // simulado
    }
    @Override public String nome() { return "ProcessamentoPagamento"; }
}

class NotificacaoCliente implements ProcessadorPedido {
    @Override
    public boolean processar(Map<String, Object> ctx) {
        System.out.println("[NotificacaoCliente] Enviando confirmacao por email...");
        return true;
    }
    @Override public String nome() { return "NotificacaoCliente"; }
}

class AtualizacaoEstoque implements ProcessadorPedido {
    @Override
    public boolean processar(Map<String, Object> ctx) {
        System.out.println("[AtualizacaoEstoque] Decrementando estoque...");
        return true;
    }
    @Override public String nome() { return "AtualizacaoEstoque"; }
}

/**
 * PipelinePedido: orquestra etapas de processamento.
 * Cada etapa e injetada — DIP aplicado ao pipeline.
 * Adicionar/remover etapas = alterar a composicao, nunca o pipeline.
 */
class PipelinePedido {

    private final List<ProcessadorPedido> etapas;

    public PipelinePedido(List<ProcessadorPedido> etapas) {
        this.etapas = List.copyOf(etapas);
    }

    public boolean executar(String pedidoId) {
        Map<String, Object> contexto = new HashMap<>();
        contexto.put("pedidoId", pedidoId);
        contexto.put("timestamp", LocalDateTime.now());

        System.out.println("[Pipeline] Iniciando pedido: " + pedidoId);

        for (ProcessadorPedido etapa : etapas) {
            if (!etapa.processar(contexto)) {
                System.out.println("[Pipeline] Falhou em: " + etapa.nome());
                return false;
            }
        }

        System.out.println("[Pipeline] Pedido processado com sucesso.");
        return true;
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class DIPAvancado {

    public static void main(String[] args) {
        System.out.println("=== TOPICO 1: Layered Architecture com DIP ===\n");

        ProdutoRepository   repo       = new InMemoryProdutoRepository();
        ProdutoUseCase      useCase    = new ProdutoUseCaseImpl(repo);
        ProdutoController   controller = new ProdutoController(useCase);

        controller.criarProduto("Notebook Pro", 4999.90);
        controller.criarProduto("Mouse Wireless", 249.90);
        controller.criarProduto("", 0);            // valida e retorna 400
        controller.listarProdutos();

        System.out.println("\n=== TOPICO 2: Decorator de Cache ===\n");

        CacheService<String, String> baseCache     = new InMemoryCacheService<>();
        CacheService<String, String> loggedCache   = new LoggingCacheDecorator<>(baseCache);

        loggedCache.put("user:42", "Ana Lima");
        loggedCache.get("user:42");                 // HIT
        loggedCache.get("user:99");                 // MISS
        loggedCache.invalidate("user:42");
        loggedCache.get("user:42");                 // MISS (apos invalidacao)

        System.out.println("\n=== TOPICO 3: Strategy de Desconto Injetado ===\n");

        CarrinhoService carrinhoNormal = new CarrinhoService(new SemDesconto());
        carrinhoNormal.finalizar("cliente-bronze", List.of(100.0, 50.0, 25.0));

        System.out.println();

        CarrinhoService carrinhoFidelidade = new CarrinhoService(new DescontoFidelidade());
        carrinhoFidelidade.finalizar("cliente-gold", List.of(200.0, 300.0));

        System.out.println();

        CarrinhoService carrinhoBlack = new CarrinhoService(new DescontoBlackFriday());
        carrinhoBlack.finalizar("cliente-vip", List.of(500.0, 150.0));

        System.out.println("\n=== TOPICO 4: Pipeline de Processamento de Pedidos ===\n");

        PipelinePedido pipelineCompleto = new PipelinePedido(List.of(
            new ValidacaoEstoque(),
            new ProcessamentoPagamento(),
            new AtualizacaoEstoque(),
            new NotificacaoCliente()
        ));
        pipelineCompleto.executar("PED-0042");

        System.out.println();

        // Pipeline reduzido (sem notificacao) — sem alterar nenhuma etapa
        PipelinePedido pipelineSemNotificacao = new PipelinePedido(List.of(
            new ValidacaoEstoque(),
            new ProcessamentoPagamento(),
            new AtualizacaoEstoque()
        ));
        pipelineSemNotificacao.executar("PED-0043");

        System.out.println("\n=== COMO DIP COMPLETA O SOLID ===");
        System.out.println("S — SRP:  cada abstracao tem um contrato claro e unico");
        System.out.println("O — OCP:  novas implementacoes sem alterar o codigo de alto nivel");
        System.out.println("L — LSP:  implementacoes sao substituiveis sem surpresas");
        System.out.println("I — ISP:  interfaces injetadas sao pequenas e especificas");
        System.out.println("D — DIP:  a cola que conecta tudo — dependencias apontam para abstracoes");
    }
}
