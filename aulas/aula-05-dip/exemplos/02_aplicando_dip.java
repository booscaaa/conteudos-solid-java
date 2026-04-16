/**
 * EXEMPLO 02 — Aplicando o DIP
 *
 * Cenario: as mesmas tres situacoes do exemplo 01, agora refatoradas
 * para respeitar o Dependency Inversion Principle.
 *
 * SOLUCAO: definir interfaces (abstracoes) para cada dependencia e injetar
 * as implementacoes concretas pelo construtor — nunca instanciar com "new".
 *
 * BENEFICIOS:
 *   - Alto nivel (regras de negocio) desacoplado do baixo nivel (infraestrutura)
 *   - Trocar implementacoes sem tocar no codigo de negocio
 *   - Testes unitarios com doubles (mocks/stubs) sem infraestrutura real
 *   - Dependencias explicitas e visiveis no construtor
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// -----------------------------------------------------------------------
// ABSTRACOES (Interfaces — o que o alto nivel precisa)
// -----------------------------------------------------------------------

/** Abstracao: contrato de persistencia de pedidos */
interface PedidoRepository {
    void salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(String id);
}

/** Abstracao: contrato de envio de mensagens */
interface EmailService {
    void enviar(String destinatario, String assunto, String corpo);
}

/** Abstracao: contrato de log */
interface Logger {
    void info(String mensagem);
    void erro(String mensagem);
}

/** Abstracao: geracao de documentos */
interface DocumentoGenerator {
    byte[] gerar(String titulo, List<String> dados);
}

/** Abstracao: armazenamento de arquivos */
interface ArquivoStorage {
    void salvar(String caminho, byte[] conteudo);
}

/** Abstracao: provedor de identidade */
interface IdentityProvider {
    boolean validar(String usuario, String credencial);
}

// -----------------------------------------------------------------------
// SOLUCAO 1: PedidoService com Injecao de Dependencias
// -----------------------------------------------------------------------

/**
 * PedidoService agora e um modulo de ALTO NIVEL puro.
 * Ele nao sabe nada sobre MySQL, JavaMail ou Console.
 * Depende apenas das abstracoes — interfaces definidas acima.
 *
 * PRINCIPIO DIP: alto nivel depende de abstracoes, nunca de detalhes.
 */
class PedidoService {

    private final PedidoRepository repository;
    private final EmailService      emailService;
    private final Logger            logger;

    // Dependencias declaradas explicitamente — injetadas pelo construtor
    public PedidoService(PedidoRepository repository,
                         EmailService      emailService,
                         Logger            logger) {
        this.repository   = repository;
        this.emailService = emailService;
        this.logger       = logger;
    }

    public void criarPedido(String clienteId, List<String> itens, double total) {
        logger.info("Criando pedido para cliente: " + clienteId);

        Pedido pedido = new Pedido(clienteId, itens, total, LocalDateTime.now());
        repository.salvar(pedido);      // <- usa a abstracao, nao a implementacao

        emailService.enviar(            // <- usa a abstracao, nao a implementacao
            clienteId + "@cliente.com",
            "Pedido confirmado",
            "Seu pedido no valor de R$ " + total + " foi criado."
        );

        logger.info("Pedido criado: " + pedido.getId());
    }

    public Optional<Pedido> buscarPedido(String pedidoId) {
        return repository.buscarPorId(pedidoId);
    }
}

// -----------------------------------------------------------------------
// IMPLEMENTACOES DE BAIXO NIVEL (detalhes de infraestrutura)
// -----------------------------------------------------------------------

/** Detalhe: implementacao MySQL */
class MySQLPedidoRepository implements PedidoRepository {

    @Override
    public void salvar(Pedido pedido) {
        System.out.println("[MySQL] INSERT pedido: " + pedido.getId());
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        System.out.println("[MySQL] SELECT pedido: " + id);
        return Optional.of(new Pedido("cliente1", List.of("Item A"), 99.90, LocalDateTime.now()));
    }
}

/** Detalhe: implementacao alternativa em memoria (util para testes) */
class InMemoryPedidoRepository implements PedidoRepository {

    private final List<Pedido> pedidos = new ArrayList<>();

    @Override
    public void salvar(Pedido pedido) {
        pedidos.add(pedido);
        System.out.println("[InMemory] Pedido salvo: " + pedido.getId() + " (total: " + pedidos.size() + ")");
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        return pedidos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<Pedido> todos() { return List.copyOf(pedidos); }
}

/** Detalhe: JavaMail */
class JavaMailEmailService implements EmailService {

    @Override
    public void enviar(String dest, String assunto, String corpo) {
        System.out.println("[JavaMail] -> " + dest + " | " + assunto);
    }
}

/** Detalhe: SendGrid — implementacao alternativa */
class SendGridEmailService implements EmailService {

    @Override
    public void enviar(String dest, String assunto, String corpo) {
        System.out.println("[SendGrid API] -> " + dest + " | " + assunto);
    }
}

/** Detalhe: log no console */
class ConsoleLoggerImpl implements Logger {

    @Override
    public void info(String mensagem) {
        System.out.println("[INFO  " + LocalDateTime.now().toLocalTime() + "] " + mensagem);
    }

    @Override
    public void erro(String mensagem) {
        System.out.println("[ERRO  " + LocalDateTime.now().toLocalTime() + "] " + mensagem);
    }
}

/** Detalhe: log silencioso para testes */
class SilentLogger implements Logger {
    @Override public void info(String mensagem) { /* silencioso em testes */ }
    @Override public void erro(String mensagem) { /* silencioso em testes */ }
}

// -----------------------------------------------------------------------
// SOLUCAO 2: RelatorioService com abstracoes
// -----------------------------------------------------------------------

class RelatorioService {

    private final DocumentoGenerator generator;
    private final ArquivoStorage     storage;
    private final Logger             logger;

    public RelatorioService(DocumentoGenerator generator,
                            ArquivoStorage     storage,
                            Logger             logger) {
        this.generator = generator;
        this.storage   = storage;
        this.logger    = logger;
    }

    public void gerarRelatorio(String titulo, List<String> dados) {
        logger.info("Gerando relatorio: " + titulo);

        byte[] conteudo = generator.gerar(titulo, dados);       // <- abstracao
        storage.salvar("/relatorios/" + titulo + ".pdf", conteudo); // <- abstracao

        logger.info("Relatorio salvo: " + titulo + ".pdf");
    }
}

/** Detalhe: iText */
class ITextGenerator implements DocumentoGenerator {
    @Override
    public byte[] gerar(String titulo, List<String> dados) {
        System.out.println("[iText] Gerando PDF: " + titulo);
        return new byte[dados.size()];
    }
}

/** Detalhe: Apache PDFBox — implementacao alternativa */
class PDFBoxGenerator implements DocumentoGenerator {
    @Override
    public byte[] gerar(String titulo, List<String> dados) {
        System.out.println("[PDFBox] Gerando PDF: " + titulo);
        return new byte[dados.size()];
    }
}

/** Detalhe: sistema de arquivos local */
class LocalFileStorage implements ArquivoStorage {
    @Override
    public void salvar(String caminho, byte[] conteudo) {
        System.out.println("[LocalFS] Escrevendo em: " + caminho);
    }
}

/** Detalhe: AWS S3 — implementacao alternativa */
class S3Storage implements ArquivoStorage {
    @Override
    public void salvar(String caminho, byte[] conteudo) {
        System.out.println("[AWS S3] Upload: " + caminho + " (" + conteudo.length + " bytes)");
    }
}

// -----------------------------------------------------------------------
// SOLUCAO 3: AutenticacaoService com abstracao de provedor de identidade
// -----------------------------------------------------------------------

class AutenticacaoService {

    private final IdentityProvider provider;
    private final Logger           logger;

    public AutenticacaoService(IdentityProvider provider, Logger logger) {
        this.provider = provider;
        this.logger   = logger;
    }

    public boolean autenticar(String usuario, String credencial) {
        logger.info("Autenticando: " + usuario);

        boolean ok = provider.validar(usuario, credencial); // <- abstracao

        if (ok) logger.info("Autenticacao aprovada: " + usuario);
        else    logger.erro("Autenticacao negada: " + usuario);

        return ok;
    }
}

/** Detalhe: LDAP */
class LDAPIdentityProvider implements IdentityProvider {

    private final String url;

    public LDAPIdentityProvider(String url) { this.url = url; }

    @Override
    public boolean validar(String usuario, String credencial) {
        System.out.println("[LDAP @ " + url + "] validando: " + usuario);
        return !credencial.isEmpty();
    }
}

/** Detalhe: OAuth2 — implementacao alternativa */
class OAuth2IdentityProvider implements IdentityProvider {

    @Override
    public boolean validar(String usuario, String credencial) {
        System.out.println("[OAuth2] validando token para: " + usuario);
        return credencial.startsWith("Bearer ");
    }
}

// -----------------------------------------------------------------------
// CLASSE DE APOIO
// -----------------------------------------------------------------------

class Pedido {
    private static int contador = 1;
    private final String id;
    private final String clienteId;
    private final List<String> itens;
    private final double total;
    private final LocalDateTime criadoEm;

    public Pedido(String clienteId, List<String> itens, double total, LocalDateTime criadoEm) {
        this.id        = "PED-" + (contador++);
        this.clienteId = clienteId;
        this.itens     = itens;
        this.total     = total;
        this.criadoEm  = criadoEm;
    }

    public String getId()          { return id; }
    public String getClienteId()   { return clienteId; }
    public List<String> getItens() { return itens; }
    public double getTotal()       { return total; }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO — DIP APLICADO
// -----------------------------------------------------------------------
public class AplicandoDIP {

    public static void main(String[] args) {
        Logger logger = new ConsoleLoggerImpl();

        System.out.println("=== DIP SOLUCAO 1: PedidoService com MySQL + JavaMail ===\n");

        PedidoService pedidoMysql = new PedidoService(
            new MySQLPedidoRepository(),    // <- injetado
            new JavaMailEmailService(),     // <- injetado
            logger
        );
        pedidoMysql.criarPedido("cliente42", List.of("Notebook"), 3499.90);

        System.out.println();

        // Trocar TUDO sem tocar no PedidoService
        System.out.println("=== Mesma regra de negocio, infraestrutura diferente ===\n");

        InMemoryPedidoRepository memRepo = new InMemoryPedidoRepository();
        PedidoService pedidoMem = new PedidoService(
            memRepo,                        // <- InMemory ao inves de MySQL
            new SendGridEmailService(),     // <- SendGrid ao inves de JavaMail
            logger
        );
        pedidoMem.criarPedido("cliente77", List.of("Mouse", "Teclado"), 450.00);
        pedidoMem.criarPedido("cliente88", List.of("Monitor"), 1800.00);

        System.out.println("\nPedidos em memoria: " + memRepo.todos().size());

        System.out.println("\n=== DIP SOLUCAO 2: RelatorioService — iText + Local vs PDFBox + S3 ===\n");

        RelatorioService relatorioLocal = new RelatorioService(
            new ITextGenerator(),
            new LocalFileStorage(),
            logger
        );
        relatorioLocal.gerarRelatorio("Vendas-Abril", List.of("Item A: R$100", "Item B: R$200"));

        System.out.println();

        RelatorioService relatorioCloud = new RelatorioService(
            new PDFBoxGenerator(),          // <- outra biblioteca de PDF
            new S3Storage(),                // <- AWS S3 ao inves de local
            logger
        );
        relatorioCloud.gerarRelatorio("Vendas-Maio", List.of("Item C: R$300"));

        System.out.println("\n=== DIP SOLUCAO 3: AutenticacaoService — LDAP vs OAuth2 ===\n");

        AutenticacaoService authLdap = new AutenticacaoService(
            new LDAPIdentityProvider("ldap://corp.empresa.com"),
            logger
        );
        authLdap.autenticar("joao.silva", "senha@123");

        System.out.println();

        AutenticacaoService authOauth = new AutenticacaoService(
            new OAuth2IdentityProvider(),   // <- OAuth2 ao inves de LDAP
            logger
        );
        authOauth.autenticar("maria.souza", "Bearer eyJhbGciOiJIUzI1NiJ9");

        System.out.println("\n=== BENEFICIOS ===");
        System.out.println("✓ PedidoService nao sabe se usa MySQL ou InMemory");
        System.out.println("✓ Trocar SendGrid por JavaMail: alterar APENAS a composicao");
        System.out.println("✓ RelatorioService nao sabe se salva em disco ou na nuvem");
        System.out.println("✓ AutenticacaoService nao sabe se o provedor e LDAP ou OAuth2");
        System.out.println("✓ Para testar PedidoService: usar InMemoryPedidoRepository + SilentLogger");
        System.out.println("✓ Regras de negocio isoladas — compiladas sem dependencia de infraestrutura");
    }
}
