/**
 * EXEMPLO 03 — Inversao de Controle e Injecao de Dependencias
 *
 * Cenario: explorar as formas de Injecao de Dependencias (DI) e entender
 * como a "inversao" de controle muda quem e responsavel por criar objetos.
 *
 * CONCEITO: "Inversion of Control" (IoC) significa que o controle de
 * criacao e ligacao dos objetos e transferido para um orchestrador externo
 * (um framework, uma factory, ou o proprio metodo main).
 *
 * Tres formas principais de Injecao de Dependencias:
 *   1. Construtor (a mais recomendada — dependencias obrigatorias)
 *   2. Setter (dependencias opcionais ou configuracao pos-construcao)
 *   3. Interface (raro, exige interface especial — nao e o padrao hoje)
 *
 * Tambem exploraremos:
 *   - Service Locator (anti-pattern relacionado)
 *   - Factory como orchestrador simples
 *   - Como o DIP se relaciona com testabilidade
 */

import java.util.*;

// -----------------------------------------------------------------------
// ABSTRACOES DO DOMINIO
// -----------------------------------------------------------------------

interface NotificadorDIP {
    void notificar(String usuario, String mensagem);
}

interface UsuarioRepository {
    Optional<String> buscarEmail(String usuarioId);
    void salvar(String usuarioId, String email);
}

interface AuditoriaService {
    void registrar(String acao, String detalhe);
}

// -----------------------------------------------------------------------
// FORMA 1: Injecao pelo Construtor (Constructor Injection)
// Recomendada para dependencias obrigatorias.
// -----------------------------------------------------------------------

/**
 * CadastroPorConstrutor recebe TODAS as dependencias no construtor.
 * Vantagens:
 *   - Objeto nunca criado em estado invalido (sem nulls implicitos)
 *   - Dependencias imutaveis (final)
 *   - Evidente quais colaboradores o objeto precisa
 */
class CadastroServiceConstrutorInjecao {

    private final UsuarioRepository  repository;
    private final NotificadorDIP     notificador;
    private final AuditoriaService   auditoria;

    public CadastroServiceConstrutorInjecao(
            UsuarioRepository repository,
            NotificadorDIP    notificador,
            AuditoriaService  auditoria) {
        this.repository  = repository;
        this.notificador = notificador;
        this.auditoria   = auditoria;
    }

    public void cadastrar(String usuarioId, String email) {
        repository.salvar(usuarioId, email);
        notificador.notificar(usuarioId, "Bem-vindo! Seu cadastro foi confirmado.");
        auditoria.registrar("CADASTRO", "usuario=" + usuarioId);
    }
}

// -----------------------------------------------------------------------
// FORMA 2: Injecao por Setter (Setter Injection)
// Para dependencias opcionais ou que podem ser trocadas em runtime.
// -----------------------------------------------------------------------

/**
 * CadastroPorSetter permite alterar colaboradores apos a construcao.
 * Desvantagem: objeto pode ser usado com dependencia nula (require cuidado).
 * Uso correto: dependencias verdadeiramente opcionais.
 */
class CadastroServiceSetterInjecao {

    private UsuarioRepository  repository;
    private NotificadorDIP     notificador;
    private AuditoriaService   auditoria; // opcional — pode ser null

    public void setRepository(UsuarioRepository repository) {
        this.repository = repository;
    }

    public void setNotificador(NotificadorDIP notificador) {
        this.notificador = notificador;
    }

    public void setAuditoria(AuditoriaService auditoria) {
        this.auditoria = auditoria; // pode ser null — auditoria e opcional
    }

    public void cadastrar(String usuarioId, String email) {
        Objects.requireNonNull(repository,  "repository nao pode ser null");
        Objects.requireNonNull(notificador, "notificador nao pode ser null");

        repository.salvar(usuarioId, email);
        notificador.notificar(usuarioId, "Bem-vindo!");

        // Auditoria e opcional — chama so se configurada
        if (auditoria != null) {
            auditoria.registrar("CADASTRO", "usuario=" + usuarioId);
        }
    }
}

// -----------------------------------------------------------------------
// IMPLEMENTACOES CONCRETAS
// -----------------------------------------------------------------------

class EmailNotificador implements NotificadorDIP {
    @Override
    public void notificar(String usuario, String mensagem) {
        System.out.println("[EMAIL -> " + usuario + "] " + mensagem);
    }
}

class SMSNotificador implements NotificadorDIP {
    @Override
    public void notificar(String usuario, String mensagem) {
        System.out.println("[SMS -> " + usuario + "] " + mensagem);
    }
}

class InMemoryUsuarioRepository implements UsuarioRepository {
    private final Map<String, String> dados = new HashMap<>();

    @Override
    public Optional<String> buscarEmail(String usuarioId) {
        return Optional.ofNullable(dados.get(usuarioId));
    }

    @Override
    public void salvar(String usuarioId, String email) {
        dados.put(usuarioId, email);
        System.out.println("[InMemory] Salvo: " + usuarioId + " -> " + email);
    }

    public Map<String, String> todos() { return Collections.unmodifiableMap(dados); }
}

class ConsoleAuditoriaService implements AuditoriaService {
    @Override
    public void registrar(String acao, String detalhe) {
        System.out.println("[AUDITORIA] " + acao + " | " + detalhe);
    }
}

/** Stub de auditoria para testes — registra em lista */
class TestAuditoriaService implements AuditoriaService {
    private final List<String> registros = new ArrayList<>();

    @Override
    public void registrar(String acao, String detalhe) {
        registros.add(acao + "|" + detalhe);
    }

    public List<String> getRegistros() { return List.copyOf(registros); }
}

// -----------------------------------------------------------------------
// FACTORY COMO ORCHESTRADOR SIMPLES
// Centraliza a criacao e composicao dos objetos.
// -----------------------------------------------------------------------

/**
 * SimpleFactory e uma forma simples de IoC sem framework.
 * O "quem cria" foi invertido: nao e a classe de negocio, e a factory.
 *
 * Em producao, Spring/Quarkus/CDI fazem isso automaticamente via
 * anotacoes como @Bean, @Inject, @ApplicationScoped.
 */
class AppFactory {

    public static CadastroServiceConstrutorInjecao criarCadastroServiceProducao() {
        return new CadastroServiceConstrutorInjecao(
            new InMemoryUsuarioRepository(),
            new EmailNotificador(),
            new ConsoleAuditoriaService()
        );
    }

    public static CadastroServiceConstrutorInjecao criarCadastroServiceTeste(
            TestAuditoriaService auditoriaSpy) {
        return new CadastroServiceConstrutorInjecao(
            new InMemoryUsuarioRepository(),
            usuario -> System.out.println("[STUB NOTIFICADOR] " + usuario), // lambda como impl
            auditoriaSpy
        );
    }
}

// -----------------------------------------------------------------------
// ANTI-PATTERN: Service Locator
// Aparentemente "inverte" controle, mas na pratica esconde dependencias.
// -----------------------------------------------------------------------

/**
 * ServiceLocator parece DIP mas viola o principio de dependencias explicitas.
 * Problemas:
 *   1. Dependencias ocultas — impossivel saber o que o servico precisa
 *   2. Testabilidade ruim — precisa configurar o locator antes de cada teste
 *   3. Acoplamento global — qualquer lugar do codigo pode "puxar" qualquer coisa
 */
class ServiceLocator {
    private static final Map<Class<?>, Object> registry = new HashMap<>();

    public static <T> void registrar(Class<T> tipo, T instancia) {
        registry.put(tipo, instancia);
    }

    @SuppressWarnings("unchecked")
    public static <T> T obter(Class<T> tipo) {
        T instancia = (T) registry.get(tipo);
        if (instancia == null) throw new RuntimeException("Servico nao registrado: " + tipo.getSimpleName());
        return instancia;
    }
}

/**
 * Parece DIP mas usa Service Locator internamente.
 * Dependencias nao sao visiveis no construtor — problema!
 */
class CadastroServiceLocatorRuim {

    public void cadastrar(String usuarioId, String email) {
        // Quem lê o construtor não sabe que isso precisa de Repository e Notificador
        UsuarioRepository repo = ServiceLocator.obter(UsuarioRepository.class);
        NotificadorDIP    not  = ServiceLocator.obter(NotificadorDIP.class);

        repo.salvar(usuarioId, email);
        not.notificar(usuarioId, "Bem-vindo!");
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO
// -----------------------------------------------------------------------
public class DIPInversaoDeControle {

    public static void main(String[] args) {
        System.out.println("=== FORMA 1: Injecao por Construtor ===\n");

        CadastroServiceConstrutorInjecao svcProd = AppFactory.criarCadastroServiceProducao();
        svcProd.cadastrar("u001", "ana@empresa.com");
        svcProd.cadastrar("u002", "bob@empresa.com");

        System.out.println("\n=== FORMA 1: Mesmo servico, notificador diferente ===\n");

        // Troca o notificador sem alterar nenhuma linha de CadastroService
        CadastroServiceConstrutorInjecao svcSMS = new CadastroServiceConstrutorInjecao(
            new InMemoryUsuarioRepository(),
            new SMSNotificador(),           // <- SMS ao inves de Email
            new ConsoleAuditoriaService()
        );
        svcSMS.cadastrar("u003", "carlos@empresa.com");

        System.out.println("\n=== FORMA 1: Simulando testes com stubs ===\n");

        TestAuditoriaService auditoriaSpy = new TestAuditoriaService();
        CadastroServiceConstrutorInjecao svcTeste = AppFactory.criarCadastroServiceTeste(auditoriaSpy);
        svcTeste.cadastrar("u-teste", "teste@exemplo.com");

        System.out.println("Registros de auditoria capturados: " + auditoriaSpy.getRegistros());

        System.out.println("\n=== FORMA 2: Injecao por Setter ===\n");

        CadastroServiceSetterInjecao svcSetter = new CadastroServiceSetterInjecao();
        svcSetter.setRepository(new InMemoryUsuarioRepository());
        svcSetter.setNotificador(new EmailNotificador());
        // auditoria NAO configurada — e opcional
        svcSetter.cadastrar("u004", "diana@empresa.com");

        System.out.println("\n(configurando auditoria depois)");
        svcSetter.setAuditoria(new ConsoleAuditoriaService()); // adiciona auditoria
        svcSetter.cadastrar("u005", "eva@empresa.com");

        System.out.println("\n=== ANTI-PATTERN: Service Locator ===\n");

        // Precisa configurar o locator globalmente — dependencias ocultas
        ServiceLocator.registrar(UsuarioRepository.class, new InMemoryUsuarioRepository());
        ServiceLocator.registrar(NotificadorDIP.class,    new EmailNotificador());

        CadastroServiceLocatorRuim svcLocator = new CadastroServiceLocatorRuim();
        svcLocator.cadastrar("u006", "fausto@empresa.com");

        System.out.println("\nPROBLEMA: olhando o construtor de CadastroServiceLocatorRuim,");
        System.out.println("         nao ha como saber que ele precisa de Repository e Notificador.");
        System.out.println("         As dependencias estao ocultas dentro do metodo cadastrar().");

        System.out.println("\n=== RESUMO: Injecao por Construtor > Service Locator ===");
        System.out.println("✓ Construtor: dependencias explicitas, imutaveis, testavel");
        System.out.println("✓ Setter:     dependencias opcionais configuradas apos construcao");
        System.out.println("✗ Locator:    dependencias ocultas, acoplamento global, dificil de testar");
    }
}
