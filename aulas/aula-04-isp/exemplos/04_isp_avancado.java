/**
 * EXEMPLO 04 — ISP Avancado: Casos Reais e Patterns
 *
 * Cenario: aplicacoes do ISP em padroes arquiteturais comuns
 * encontrados em sistemas Java reais.
 *
 * TOPICOS:
 *   1. ISP em sistemas de pagamento (Strategy + ISP)
 *   2. ISP em sistemas de cache (Read-Through / Write-Through)
 *   3. ISP em validadores (Chain of Responsibility)
 *   4. ISP em exportadores (plugin-style)
 *   5. Como o ISP protege de mudancas futuras
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

// -----------------------------------------------------------------------
// TOPICO 1: ISP em Sistemas de Pagamento
// -----------------------------------------------------------------------

/** Capacidade basica: processar um pagamento */
interface Pagavel {
    boolean processar(double valor, String descricao);
}

/** Capacidade: reembolsar uma transacao */
interface Reembolsavel {
    boolean reembolsar(String transacaoId, double valor);
}

/** Capacidade: parcelar o pagamento */
interface Parcelavel {
    boolean processarParcelado(double valor, int parcelas);
}

/** Capacidade: verificar status de uma transacao */
interface Rastreavel {
    String consultarStatus(String transacaoId);
}

/** Pagamento via PIX — rapido, sem parcelamento, sem reembolso automatico */
class PagamentoPIX implements Pagavel, Rastreavel {

    @Override
    public boolean processar(double valor, String descricao) {
        System.out.printf("[PIX] Cobranca de R$ %.2f — %s%n", valor, descricao);
        return true;
    }

    @Override
    public String consultarStatus(String transacaoId) {
        return "PIX:" + transacaoId + " → LIQUIDADO";
    }
}

/** Pagamento por boleto — nao e instantaneo, pode ser cancelado */
class PagamentoBoleto implements Pagavel, Rastreavel, Reembolsavel {

    @Override
    public boolean processar(double valor, String descricao) {
        System.out.printf("[BOLETO] Emitindo boleto de R$ %.2f — %s%n", valor, descricao);
        return true;
    }

    @Override
    public String consultarStatus(String transacaoId) {
        return "BOLETO:" + transacaoId + " → AGUARDANDO_PAGAMENTO";
    }

    @Override
    public boolean reembolsar(String transacaoId, double valor) {
        System.out.printf("[BOLETO] Reembolsando R$ %.2f para transacao %s%n", valor, transacaoId);
        return true;
    }
}

/** Cartao de credito — parcelamento + reembolso + rastreamento */
class PagamentoCartaoCredito implements Pagavel, Parcelavel, Reembolsavel, Rastreavel {

    @Override
    public boolean processar(double valor, String descricao) {
        System.out.printf("[CARTAO] Processando R$ %.2f a vista — %s%n", valor, descricao);
        return true;
    }

    @Override
    public boolean processarParcelado(double valor, int parcelas) {
        System.out.printf("[CARTAO] Parcelando R$ %.2f em %dx de R$ %.2f%n",
            valor, parcelas, valor / parcelas);
        return true;
    }

    @Override
    public boolean reembolsar(String transacaoId, double valor) {
        System.out.printf("[CARTAO] Estorno de R$ %.2f na transacao %s%n", valor, transacaoId);
        return true;
    }

    @Override
    public String consultarStatus(String transacaoId) {
        return "CARTAO:" + transacaoId + " → APROVADO";
    }
}

// -----------------------------------------------------------------------
// TOPICO 2: ISP em Sistemas de Cache
// -----------------------------------------------------------------------

/** Capacidade: ler do cache */
interface CacheLegivel<K, V> {
    Optional<V> get(K chave);
    boolean contem(K chave);
}

/** Capacidade: escrever no cache */
interface CacheGravavel<K, V> {
    void put(K chave, V valor);
    void put(K chave, V valor, long ttlMs);
}

/** Capacidade: invalidar entradas do cache */
interface CacheInvalidavel<K> {
    void invalidar(K chave);
    void limpar();
}

/** Cache completo */
interface Cache<K, V> extends CacheLegivel<K, V>, CacheGravavel<K, V>, CacheInvalidavel<K> {}

/** Cache em memoria — implementacao completa */
class CacheEmMemoria<K, V> implements Cache<K, V> {

    private final Map<K, V> dados = new HashMap<>();
    private final Map<K, Long> expiracao = new HashMap<>();

    @Override
    public Optional<V> get(K chave) {
        if (!contem(chave)) return Optional.empty();
        return Optional.ofNullable(dados.get(chave));
    }

    @Override
    public boolean contem(K chave) {
        if (!dados.containsKey(chave)) return false;
        Long exp = expiracao.get(chave);
        if (exp != null && System.currentTimeMillis() > exp) {
            invalidar(chave);
            return false;
        }
        return true;
    }

    @Override
    public void put(K chave, V valor) {
        dados.put(chave, valor);
        expiracao.remove(chave);
    }

    @Override
    public void put(K chave, V valor, long ttlMs) {
        dados.put(chave, valor);
        expiracao.put(chave, System.currentTimeMillis() + ttlMs);
    }

    @Override
    public void invalidar(K chave) {
        dados.remove(chave);
        expiracao.remove(chave);
        System.out.println("[CACHE] Invalidado: " + chave);
    }

    @Override
    public void limpar() {
        int qtd = dados.size();
        dados.clear();
        expiracao.clear();
        System.out.println("[CACHE] Limpo. " + qtd + " entrada(s) removida(s).");
    }
}

/** Servico que usa o cache — depende apenas de CacheLegivel */
class ProdutoService {

    private final CacheLegivel<Long, String> cache;

    public ProdutoService(CacheLegivel<Long, String> cache) {
        this.cache = cache; // depende so de leitura!
    }

    public String buscarProduto(Long id) {
        return cache.get(id).orElse("Produto nao encontrado no cache (id=" + id + ")");
    }
}

// -----------------------------------------------------------------------
// TOPICO 3: ISP em Validadores
// -----------------------------------------------------------------------

/** Capacidade: validar um valor */
interface Validador<T> {
    boolean validar(T valor);
    String mensagemDeErro();
}

/** Validador de email minimo */
class ValidadorEmail implements Validador<String> {

    @Override
    public boolean validar(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    @Override
    public String mensagemDeErro() { return "Email invalido. Formato esperado: usuario@dominio.com"; }
}

/** Validador de CPF simplificado */
class ValidadorCPF implements Validador<String> {

    @Override
    public boolean validar(String cpf) {
        if (cpf == null) return false;
        String digitos = cpf.replaceAll("[^0-9]", "");
        return digitos.length() == 11;
    }

    @Override
    public String mensagemDeErro() { return "CPF invalido. Deve conter 11 digitos."; }
}

/** Validador de senha forte */
class ValidadorSenha implements Validador<String> {

    @Override
    public boolean validar(String senha) {
        return senha != null
            && senha.length() >= 8
            && senha.matches(".*[A-Z].*")
            && senha.matches(".*[0-9].*");
    }

    @Override
    public String mensagemDeErro() { return "Senha fraca. Use ao menos 8 chars, 1 maiuscula e 1 numero."; }
}

/** Pipeline de validacao — compoe validadores via ISP */
class PipelineDeValidacao<T> {

    private final List<Validador<T>> validadores = new ArrayList<>();

    public PipelineDeValidacao<T> adicionar(Validador<T> v) {
        validadores.add(v);
        return this;
    }

    public boolean validar(T valor) {
        for (Validador<T> v : validadores) {
            if (!v.validar(valor)) {
                System.out.println("[VALIDACAO FALHOU] " + v.mensagemDeErro());
                return false;
            }
        }
        return true;
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO FINAL
// -----------------------------------------------------------------------
public class ISPAvancado {

    static void processarPedido(Pagavel metodo, double valor) {
        System.out.println("Processando pedido...");
        boolean ok = metodo.processar(valor, "Pedido #42");
        System.out.println("Resultado: " + (ok ? "APROVADO" : "RECUSADO"));
    }

    static void tentarParcelar(Object metodo, double valor, int parcelas) {
        if (metodo instanceof Parcelavel p) {
            p.processarParcelado(valor, parcelas);
        } else {
            System.out.println("[INFO] Metodo de pagamento nao suporta parcelamento.");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TOPICO 1: Sistema de Pagamento ===\n");

        PagamentoPIX           pix    = new PagamentoPIX();
        PagamentoBoleto        boleto = new PagamentoBoleto();
        PagamentoCartaoCredito cartao = new PagamentoCartaoCredito();

        processarPedido(pix,    150.00);
        processarPedido(boleto, 299.90);
        processarPedido(cartao, 1200.00);

        System.out.println();
        tentarParcelar(pix,    150.00, 3);   // nao suporta
        tentarParcelar(cartao, 1200.00, 12); // suporta

        System.out.println();
        // Reembolso — so para quem implementa Reembolsavel
        boleto.reembolsar("BOL-2024-001", 299.90);
        cartao.reembolsar("CAR-2024-042", 1200.00);
        // pix.reembolsar(...); <- NAO COMPILA!

        System.out.println("\n=== TOPICO 2: Cache Segregado ===\n");

        CacheEmMemoria<Long, String> cache = new CacheEmMemoria<>();
        cache.put(1L, "Notebook Dell", 5000L);
        cache.put(2L, "Mouse Logitech");

        // Servico usa apenas CacheLegivel
        ProdutoService service = new ProdutoService(cache);
        System.out.println(service.buscarProduto(1L));
        System.out.println(service.buscarProduto(99L));

        // Invalida pelo Cache completo
        cache.invalidar(1L);
        System.out.println(service.buscarProduto(1L)); // sumiu!
        cache.limpar();

        System.out.println("\n=== TOPICO 3: Pipeline de Validacao ===\n");

        PipelineDeValidacao<String> validadorCadastro = new PipelineDeValidacao<String>()
            .adicionar(new ValidadorEmail());

        PipelineDeValidacao<String> validadorSenha = new PipelineDeValidacao<String>()
            .adicionar(new ValidadorSenha());

        System.out.println("Email valido: "   + validadorCadastro.validar("ana@empresa.com"));
        System.out.println("Email invalido: " + validadorCadastro.validar("nao-e-email"));

        System.out.println("Senha forte: "  + validadorSenha.validar("Segura123"));
        System.out.println("Senha fraca: "  + validadorSenha.validar("fraca"));

        ValidadorCPF cpfV = new ValidadorCPF();
        System.out.println("CPF valido: "   + cpfV.validar("123.456.789-09"));
        System.out.println("CPF invalido: " + cpfV.validar("123"));

        System.out.println("\n=== RESUMO AVANCADO ===");
        System.out.println("✓ ISP + Strategy: cada metodo de pagamento honesto sobre suas capacidades");
        System.out.println("✓ ISP + Cache: servico de leitura nao precisa conhecer invalidacao");
        System.out.println("✓ ISP + Validacao: validadores compostos de forma independente");
        System.out.println("✓ Adicionar novo metodo de pagamento nao quebra os existentes");
        System.out.println("✓ instanceof usado APENAS para feature detection opcional — nao para corrigir ISP quebrado");
    }
}
