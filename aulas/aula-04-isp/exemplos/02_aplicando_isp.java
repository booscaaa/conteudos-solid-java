/**
 * EXEMPLO 02 — Aplicando o ISP
 *
 * Cenario: as mesmas tres situacoes do exemplo 01, agora refatoradas
 * para respeitar o Interface Segregation Principle.
 *
 * SOLUCAO: quebrar interfaces gordas em interfaces pequenas e coesas,
 * cada uma representando uma capacidade especifica.
 *
 * BENEFICIOS:
 *   - Zero UnsupportedOperationException — nenhuma mentira estrutural
 *   - Implementacoes honestas sobre o que podem fazer
 *   - Clientes dependem apenas do que realmente precisam
 *   - Adicionar nova capacidade nao forca mudancas nas classes existentes
 */

import java.util.List;
import java.util.ArrayList;

// -----------------------------------------------------------------------
// SOLUCAO 1: Segregando a Impressora
// -----------------------------------------------------------------------

/** Capacidade: apenas imprimir em preto e branco */
interface Imprimivel {
    void imprimir(String documento);
}

/** Capacidade: apenas escanear */
interface Escaneavel {
    void escanear(String nomeArquivo);
}

/** Capacidade: enviar fax */
interface Enviavel {
    void enviarFax(String numero, String documento);
}

/** Capacidade: imprimir em cores */
interface Colorivel {
    void imprimirColorido(String documento);
}

/** Capacidade: fazer copias */
interface Copiavel {
    void copiar(int numeroDeCopias);
}

/**
 * ImpressoraBasica: so imprime.
 * Honesta — implementa exatamente o que pode fazer.
 */
class ImpressoraBasica implements Imprimivel {

    @Override
    public void imprimir(String documento) {
        System.out.println("[BASICA] Imprimindo: " + documento);
    }
}

/**
 * ImpressoraColorida: imprime, copia e imprime colorido.
 * Sem scanner, sem fax — honesta.
 */
class ImpressoraColorida implements Imprimivel, Colorivel, Copiavel {

    @Override
    public void imprimir(String documento) {
        System.out.println("[COLORIDA] Imprimindo P&B: " + documento);
    }

    @Override
    public void imprimirColorido(String documento) {
        System.out.println("[COLORIDA] Imprimindo colorido: " + documento);
    }

    @Override
    public void copiar(int numeroDeCopias) {
        System.out.println("[COLORIDA] Copiando " + numeroDeCopias + " via(s).");
    }
}

/**
 * MultifuncionalEscritorio: imprime, escaneia, copia e imprime colorido.
 * Sem fax — honesta sobre o que nao tem.
 */
class MultifuncionalEscritorio implements Imprimivel, Escaneavel, Colorivel, Copiavel {

    @Override
    public void imprimir(String documento) {
        System.out.println("[MULTIFUNC] Imprimindo: " + documento);
    }

    @Override
    public void escanear(String nomeArquivo) {
        System.out.println("[MULTIFUNC] Escaneando: " + nomeArquivo);
    }

    @Override
    public void imprimirColorido(String documento) {
        System.out.println("[MULTIFUNC] Colorido: " + documento);
    }

    @Override
    public void copiar(int numeroDeCopias) {
        System.out.println("[MULTIFUNC] Copiando " + numeroDeCopias + " via(s).");
    }
}

/**
 * ImpressoraEmpresarial: faz tudo.
 * Honesta — porque REALMENTE faz tudo.
 */
class ImpressoraEmpresarial implements Imprimivel, Escaneavel, Enviavel, Colorivel, Copiavel {

    @Override
    public void imprimir(String documento)                     { System.out.println("[EMPRESARIAL] Imprimindo: " + documento); }

    @Override
    public void escanear(String nomeArquivo)                   { System.out.println("[EMPRESARIAL] Escaneando: " + nomeArquivo); }

    @Override
    public void enviarFax(String numero, String documento)     { System.out.println("[EMPRESARIAL] Fax -> " + numero + ": " + documento); }

    @Override
    public void imprimirColorido(String documento)             { System.out.println("[EMPRESARIAL] Colorido: " + documento); }

    @Override
    public void copiar(int numeroDeCopias)                     { System.out.println("[EMPRESARIAL] Copiando " + numeroDeCopias + " via(s)."); }
}

// -----------------------------------------------------------------------
// SOLUCAO 2: Segregando Trabalhador / Robo
// -----------------------------------------------------------------------

interface Trabalhavel {
    void trabalhar();
}

interface Alimentavel {
    void comer();
}

interface Descansavel {
    void dormir();
}

interface Remuneravel {
    void receberSalario();
}

/**
 * Humano: trabalha, come, dorme e recebe salario.
 */
class Humano implements Trabalhavel, Alimentavel, Descansavel, Remuneravel {

    private String nome;

    public Humano(String nome) { this.nome = nome; }

    @Override public void trabalhar()      { System.out.println(nome + " esta trabalhando."); }
    @Override public void comer()          { System.out.println(nome + " esta comendo."); }
    @Override public void dormir()         { System.out.println(nome + " esta dormindo."); }
    @Override public void receberSalario() { System.out.println(nome + " recebeu salario."); }
}

/**
 * Robo: apenas trabalha.
 * Sem metodos biologicos — completamente honesto.
 */
class Robo implements Trabalhavel {

    private String modelo;

    public Robo(String modelo) { this.modelo = modelo; }

    @Override
    public void trabalhar() {
        System.out.println("[ROBO " + modelo + "] Executando ciclo de trabalho.");
    }
}

// -----------------------------------------------------------------------
// SOLUCAO 3: Segregando o Repository
// -----------------------------------------------------------------------

interface ReadRepository<T> {
    T findById(Long id);
    List<T> findAll();
}

interface WriteRepository<T> {
    void save(T entity);
    void delete(Long id);
}

interface FilterableRepository<T> extends ReadRepository<T> {
    List<T> findByFilter(String campo, Object valor);
}

interface BulkRepository<T> extends WriteRepository<T> {
    void bulkSave(List<T> entities);
}

/** Combina leitura e escrita — para repositorios completos */
interface FullRepository<T> extends ReadRepository<T>, WriteRepository<T> {}

/**
 * Repositorio somente leitura — honesto, implementa apenas ReadRepository.
 * Nenhum metodo de escrita, nenhuma excecao.
 */
class ProdutoReadRepository implements ReadRepository<String> {

    private final List<String> produtos = List.of("Notebook", "Mouse", "Teclado", "Monitor");

    @Override
    public String findById(Long id) {
        if (id < 1 || id > produtos.size()) return null;
        return produtos.get((int)(id - 1));
    }

    @Override
    public List<String> findAll() {
        return new ArrayList<>(produtos);
    }
}

/**
 * Repositorio completo — para quando precisa de leitura e escrita.
 */
class ProdutoRepository implements FullRepository<String>, FilterableRepository<String> {

    private final List<String> produtos = new ArrayList<>(List.of("Notebook", "Mouse"));
    private long proximoId = 3L;

    @Override
    public String findById(Long id) {
        return id >= 1 && id <= produtos.size() ? produtos.get((int)(id-1)) : null;
    }

    @Override
    public List<String> findAll() {
        return new ArrayList<>(produtos);
    }

    @Override
    public void save(String entity) {
        produtos.add(entity);
        System.out.println("[REPO] Salvo: " + entity + " (id=" + proximoId++ + ")");
    }

    @Override
    public void delete(Long id) {
        if (id >= 1 && id <= produtos.size()) {
            String removido = produtos.remove((int)(id-1));
            System.out.println("[REPO] Removido: " + removido);
        }
    }

    @Override
    public List<String> findByFilter(String campo, Object valor) {
        // Exemplo simples: filtra por contem o valor no nome
        return produtos.stream()
            .filter(p -> p.toLowerCase().contains(valor.toString().toLowerCase()))
            .toList();
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO — ISP APLICADO
// -----------------------------------------------------------------------
public class AplicandoISP {

    /**
     * Funcao que so precisa imprimir — depende de Imprimivel (interface minima).
     * Funciona com ImpressoraBasica, Colorida, Multifuncional e Empresarial.
     */
    static void imprimirDocumento(Imprimivel dispositivo, String doc) {
        dispositivo.imprimir(doc);
    }

    /**
     * Funcao que precisa escanear — depende de Escaneavel.
     * Nao compila se receber ImpressoraBasica — protecao em compile-time!
     */
    static void escanearDocumento(Escaneavel dispositivo, String arquivo) {
        dispositivo.escanear(arquivo);
    }

    /**
     * Funcao que gerencia hora do almoco — so para Alimentavel.
     * Nao aceita Robo — nao precisa de try/catch.
     */
    static void pausaParaAlmoco(Alimentavel trabalhador) {
        trabalhador.comer();
    }

    /**
     * Funcao que gerencia trabalho — aceita qualquer Trabalhavel.
     * Funciona para Humano e Robo.
     */
    static void iniciarTurno(Trabalhavel trabalhador) {
        trabalhador.trabalhar();
    }

    /**
     * Funcao que so precisa ler — depende de ReadRepository.
     * Funciona com qualquer repositorio (readonly ou full).
     */
    static void listarProdutos(ReadRepository<String> repo) {
        System.out.println("Produtos: " + repo.findAll());
    }

    public static void main(String[] args) {
        System.out.println("=== ISP SOLUCAO 1: Impressoras Segregadas ===\n");

        ImpressoraBasica        basica        = new ImpressoraBasica();
        ImpressoraColorida      colorida      = new ImpressoraColorida();
        MultifuncionalEscritorio multi         = new MultifuncionalEscritorio();
        ImpressoraEmpresarial   empresarial   = new ImpressoraEmpresarial();

        // Todas aceitam imprimir — polimorfismo real
        imprimirDocumento(basica,      "Relatorio.pdf");
        imprimirDocumento(colorida,    "Apresentacao.pptx");
        imprimirDocumento(multi,       "Contrato.docx");
        imprimirDocumento(empresarial, "Balancete.xlsx");

        System.out.println();

        // escanearDocumento(basica, "foto.png"); <- NAO COMPILA — protecao em compile-time!
        escanearDocumento(multi,       "Documento.pdf");
        escanearDocumento(empresarial, "Foto.jpg");

        empresarial.enviarFax("11 9999-0000", "Proposta.pdf");
        colorida.imprimirColorido("Marketing.pdf");

        System.out.println("\n=== ISP SOLUCAO 2: Trabalhador / Robo ===\n");

        Humano ana  = new Humano("Ana");
        Robo   robo = new Robo("R2-D2");

        // Ambos trabalham
        iniciarTurno(ana);
        iniciarTurno(robo);

        System.out.println();

        // So humanos comem — robo nao compila aqui!
        pausaParaAlmoco(ana);
        // pausaParaAlmoco(robo); <- ERRO DE COMPILACAO — Robo nao implementa Alimentavel!

        System.out.println("\n=== ISP SOLUCAO 3: Repository Segregado ===\n");

        ReadRepository<String> readOnly = new ProdutoReadRepository();
        ProdutoRepository full          = new ProdutoRepository();

        listarProdutos(readOnly); // funciona com readonly
        listarProdutos(full);     // funciona tambem com full

        full.save("Webcam");
        full.save("Headset");
        listarProdutos(full);

        System.out.println("\nFiltro 'web': " + full.findByFilter("nome", "web"));

        full.delete(1L);
        listarProdutos(full);

        System.out.println("\n=== BENEFICIOS ===");
        System.out.println("✓ Zero UnsupportedOperationException");
        System.out.println("✓ Erros de uso pegos em COMPILE-TIME, nao em runtime");
        System.out.println("✓ Cada classe honesta sobre o que pode fazer");
        System.out.println("✓ Clientes dependem apenas do que realmente usam");
    }
}
