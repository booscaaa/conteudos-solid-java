/**
 * EXEMPLO 01 — Violando o DIP
 *
 * Cenario: modulos de alto nivel que instanciam diretamente suas dependencias
 * de baixo nivel, criando acoplamento rigido entre camadas.
 *
 * PROBLEMA: quando uma classe de negocio cria seus proprios colaboradores
 * com "new", ela esta:
 *   a) Acoplada a uma implementacao especifica — trocar e cirurgia
 *   b) Impedindo testes unitarios sem o colaborador real
 *   c) Violando OCP — mudar o banco exige alterar a regra de negocio
 *   d) Tornando dependencias implicitas e ocultas
 *
 * Isso viola o DIP: "Modulos de alto nivel nao devem depender de modulos
 * de baixo nivel. Ambos devem depender de abstracoes." — Robert C. Martin
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------
// VIOLACAO 1: Servico de Pedidos acoplado ao MySQL e Email
// -----------------------------------------------------------------------

/**
 * PROBLEMA: PedidoService e o modulo de alto nivel (regra de negocio).
 * Ele CRIA suas dependencias de baixo nivel com "new".
 * Trocar MySQL por PostgreSQL exige alterar essa classe.
 * Trocar EmailSender por SmtpSender tambem exige alterar essa classe.
 */
class PedidoServiceRuim {

    // Acoplamento direto a implementacoes concretas
    private final MySQLPedidoDatabase database   = new MySQLPedidoDatabase();
    private final JavaMailEmailSender  emailSender = new JavaMailEmailSender();
    private final ConsoleLogger        logger      = new ConsoleLogger();

    public void criarPedido(String clienteId, List<String> itens, double total) {
        logger.log("Criando pedido para cliente: " + clienteId);

        Pedido pedido = new Pedido(clienteId, itens, total, LocalDateTime.now());
        database.salvar(pedido);  // <- acoplado ao MySQL

        emailSender.enviar(         // <- acoplado ao JavaMail
            clienteId + "@cliente.com",
            "Pedido confirmado",
            "Seu pedido no valor de R$ " + total + " foi criado."
        );

        logger.log("Pedido criado com sucesso: " + pedido.getId());
    }

    public Pedido buscarPedido(String pedidoId) {
        return database.buscarPorId(pedidoId); // <- acoplado ao MySQL
    }
}

/** Modulo de baixo nivel: detalhe de infraestrutura — banco de dados */
class MySQLPedidoDatabase {

    public void salvar(Pedido pedido) {
        // Simulacao de INSERT no MySQL
        System.out.println("[MySQL] INSERT INTO pedidos VALUES (" + pedido.getId() + ", ...)");
    }

    public Pedido buscarPorId(String id) {
        System.out.println("[MySQL] SELECT * FROM pedidos WHERE id = " + id);
        return new Pedido("cliente1", List.of("Item A"), 99.90, LocalDateTime.now());
    }
}

/** Modulo de baixo nivel: detalhe de infraestrutura — envio de email */
class JavaMailEmailSender {

    public void enviar(String destinatario, String assunto, String corpo) {
        // Simulacao de envio via JavaMail/SMTP
        System.out.println("[JavaMail] SMTP -> " + destinatario + " | " + assunto);
    }
}

/** Modulo de baixo nivel: detalhe de infraestrutura — log */
class ConsoleLogger {

    public void log(String mensagem) {
        System.out.println("[LOG " + LocalDateTime.now() + "] " + mensagem);
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 2: Relatorio acoplado a FileSystem e PDF especifico
// -----------------------------------------------------------------------

/**
 * PROBLEMA: RelatorioService decide COMO gerar e ONDE salvar —
 * nao deveria saber esses detalhes.
 */
class RelatorioServiceRuim {

    // Cria diretamente — impossivel testar sem disco real
    private final ITextPDFGenerator  pdfGenerator  = new ITextPDFGenerator();
    private final LinuxFileSystem    fileSystem     = new LinuxFileSystem();

    public void gerarRelatorio(String titulo, List<String> dados) {
        System.out.println("[RELATORIO] Gerando: " + titulo);

        byte[] pdf = pdfGenerator.gerar(titulo, dados); // <- acoplado ao iText
        fileSystem.salvar("/relatorios/" + titulo + ".pdf", pdf); // <- acoplado ao Linux

        System.out.println("[RELATORIO] Salvo em /relatorios/" + titulo + ".pdf");
    }
}

class ITextPDFGenerator {
    public byte[] gerar(String titulo, List<String> dados) {
        System.out.println("[iText] Gerando PDF: " + titulo + " (" + dados.size() + " itens)");
        return new byte[]{/* conteudo pdf */};
    }
}

class LinuxFileSystem {
    public void salvar(String caminho, byte[] conteudo) {
        System.out.println("[Linux FS] Escrevendo " + conteudo.length + " bytes em: " + caminho);
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 3: Autenticacao acoplada a LDAP especifico
// -----------------------------------------------------------------------

/**
 * PROBLEMA: AutenticacaoService decide QUAL provedor usar.
 * Migrar de LDAP para OAuth exige alterar codigo de negocio.
 */
class AutenticacaoServiceRuim {

    // Hardcoded — impossivel usar outro provedor de identidade
    private final MicrosoftLDAPProvider ldap = new MicrosoftLDAPProvider("ldap://corp.empresa.com");

    public boolean autenticar(String usuario, String senha) {
        System.out.println("[AUTH] Autenticando usuario: " + usuario);

        boolean ok = ldap.validar(usuario, senha); // <- acoplado ao Microsoft LDAP

        if (ok) {
            System.out.println("[AUTH] Usuario autenticado com sucesso.");
        } else {
            System.out.println("[AUTH] Autenticacao falhou.");
        }

        return ok;
    }
}

class MicrosoftLDAPProvider {

    private final String url;

    public MicrosoftLDAPProvider(String url) {
        this.url = url;
        System.out.println("[LDAP] Conectando a: " + url);
    }

    public boolean validar(String usuario, String senha) {
        System.out.println("[LDAP] Consultando: " + url + " -> usuario=" + usuario);
        return !senha.isEmpty(); // Simulacao
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

    public String getId()        { return id; }
    public String getClienteId() { return clienteId; }
    public List<String> getItens() { return itens; }
    public double getTotal()     { return total; }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO DOS PROBLEMAS
// -----------------------------------------------------------------------
public class ViolandoDIP {

    public static void main(String[] args) {
        System.out.println("=== VIOLACAO 1: Pedido acoplado ao MySQL e JavaMail ===\n");

        PedidoServiceRuim pedidoService = new PedidoServiceRuim();
        pedidoService.criarPedido("cliente42", List.of("Notebook", "Mouse"), 3499.90);

        System.out.println();
        pedidoService.buscarPedido("PED-1");

        System.out.println("\n=== VIOLACAO 2: Relatorio acoplado ao iText e Linux ===\n");

        RelatorioServiceRuim relatorioService = new RelatorioServiceRuim();
        relatorioService.gerarRelatorio("Vendas-Abril", List.of("Item A: R$100", "Item B: R$200"));

        System.out.println("\n=== VIOLACAO 3: Autenticacao acoplada ao Microsoft LDAP ===\n");

        AutenticacaoServiceRuim authService = new AutenticacaoServiceRuim();
        authService.autenticar("joao.silva", "senha@123");
        authService.autenticar("maria.souza", "");

        System.out.println("\n=== CONSEQUENCIAS ===");
        System.out.println("✗ Trocar MySQL -> PostgreSQL exige alterar PedidoServiceRuim");
        System.out.println("✗ Trocar JavaMail -> SendGrid exige alterar PedidoServiceRuim");
        System.out.println("✗ Trocar iText -> Apache PDFBox exige alterar RelatorioServiceRuim");
        System.out.println("✗ Trocar LDAP -> OAuth exige alterar AutenticacaoServiceRuim");
        System.out.println("✗ Testar PedidoServiceRuim exige MySQL e servidor SMTP reais");
        System.out.println("✗ Regras de negocio sao compiladas junto com detalhes de infraestrutura");

        /*
         * RESUMO DOS PROBLEMAS:
         *
         * 1. PedidoService (alto nivel) depende de MySQLPedidoDatabase (baixo nivel)
         *    — direto, via "new", sem abstracao alguma
         *
         * 2. RelatorioService (alto nivel) depende de ITextPDFGenerator e LinuxFileSystem
         *    — trocar a biblioteca de PDF exige mexer na regra de negocio
         *
         * 3. AutenticacaoService (alto nivel) depende de MicrosoftLDAPProvider
         *    — migrar provedor de identidade exige alterar a logica de autenticacao
         *
         * REGRA DO DIP:
         *   Alto nivel NAO deve depender de baixo nivel.
         *   Ambos devem depender de ABSTRACOES (interfaces).
         *   Detalhes (implementacoes) devem depender de abstracoes, nao o contrario.
         */
    }
}
