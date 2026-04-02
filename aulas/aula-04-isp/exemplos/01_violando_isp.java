/**
 * EXEMPLO 01 — Violando o ISP
 *
 * Cenario: interfaces "gordas" que obrigam implementacoes a
 * declarar metodos que nao fazem sentido para elas.
 *
 * PROBLEMA: quando uma classe implementa uma interface grande
 * mas so usa parte dos metodos, ela e forcada a:
 *   a) Lancar UnsupportedOperationException (quebra LSP)
 *   b) Deixar o corpo vazio — silencioso e enganoso
 *   c) Retornar null — viola pos-condicoes implicitas
 *
 * Isso viola o ISP: "Nenhuma classe deve ser forcada a
 * implementar metodos que nao utiliza." — Robert C. Martin
 */

import java.util.List;

// -----------------------------------------------------------------------
// VIOLACAO 1: A "Impressora Gordinha"
// Interface monolitica que tenta cobrir todos os casos de uso de uma so vez.
// -----------------------------------------------------------------------

interface ImpressoraGorda {

    /**
     * Todos os dispositivos de impressao precisam realmente de todos esses metodos?
     * Obviamente nao — mas a interface os exige.
     */
    void imprimir(String documento);
    void escanear(String nomeArquivo);
    void enviarFax(String numero, String documento);
    void imprimirColorido(String documento);
    void copiar(int numeroDeCopias);
}

/**
 * PROBLEMA: ImpressoraBasica so sabe imprimir.
 * Mas e forcada a "implementar" 4 metodos que nao fazem sentido para ela.
 */
class ImpressoraBasicaRuim implements ImpressoraGorda {

    @Override
    public void imprimir(String documento) {
        System.out.println("[IMPRESSORA BASICA] Imprimindo: " + documento);
    }

    @Override
    public void escanear(String nomeArquivo) {
        // Opcao 1: lancar excecao — viola LSP (cliente confia que funciona)
        throw new UnsupportedOperationException("Esta impressora nao tem scanner!");
    }

    @Override
    public void enviarFax(String numero, String documento) {
        // Opcao 2: corpo vazio — silencioso e perigoso
        // Ninguem sabe que nao funcionou!
    }

    @Override
    public void imprimirColorido(String documento) {
        // Opcao 3: retornar sem fazer nada com uma mensagem
        System.out.println("Aviso: impressao colorida nao suportada, imprimindo em preto e branco.");
        imprimir(documento); // adapta silenciosamente — mas nao e o que foi pedido
    }

    @Override
    public void copiar(int numeroDeCopias) {
        throw new UnsupportedOperationException("Esta impressora nao tem funcao de copia!");
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 2: Trabalhador vs Robo
// Interface mistura comportamentos exclusivos de humanos com os de robos.
// -----------------------------------------------------------------------

interface TrabalhadorGordo {
    void trabalhar();
    void comer();     // Robo nao come!
    void dormir();    // Robo nao dorme!
    void receberSalario(); // Robo nao recebe salario!
}

class TrabalhadorHumano implements TrabalhadorGordo {

    private String nome;

    public TrabalhadorHumano(String nome) {
        this.nome = nome;
    }

    @Override
    public void trabalhar()       { System.out.println(nome + " esta trabalhando."); }

    @Override
    public void comer()           { System.out.println(nome + " esta comendo."); }

    @Override
    public void dormir()          { System.out.println(nome + " esta dormindo."); }

    @Override
    public void receberSalario()  { System.out.println(nome + " recebeu salario."); }
}

/**
 * PROBLEMA: Robo e forcado a implementar metodos biologicos.
 */
class RoboIndustrial implements TrabalhadorGordo {

    private String modelo;

    public RoboIndustrial(String modelo) {
        this.modelo = modelo;
    }

    @Override
    public void trabalhar() {
        System.out.println("[ROBO " + modelo + "] Executando ciclo de trabalho.");
    }

    @Override
    public void comer() {
        // Robo nao come. O que fazer aqui?
        throw new UnsupportedOperationException("Robo nao precisa comer!");
    }

    @Override
    public void dormir() {
        // Robo nao dorme — talvez "entra em standby"?
        System.out.println("[ROBO " + modelo + "] Entrando em modo standby... (nao e dormir!)");
        // Mas agora o contrato esta distorcido.
    }

    @Override
    public void receberSalario() {
        throw new UnsupportedOperationException("Robo nao recebe salario!");
    }
}

// -----------------------------------------------------------------------
// VIOLACAO 3: Repository Monolitico
// Uma interface de repositorio que mistura leitura, escrita, filtragem
// e operacoes que nem sao responsabilidade de um repositorio.
// -----------------------------------------------------------------------

interface RepositoryGordo<T> {

    // Operacoes de leitura
    T findById(Long id);
    List<T> findAll();

    // Operacoes de escrita
    void save(T entity);
    void delete(Long id);

    // Operacoes avancadas
    List<T> findByFilter(String campo, Object valor);
    void bulkSave(List<T> entities);

    // Isso nem e responsabilidade de um repositorio!
    void exportToCsv(String caminhoArquivo);
    String generateReport();
}

class ProdutoReadOnlyRepository implements RepositoryGordo<String> {

    @Override
    public String findById(Long id) {
        return "Produto #" + id; // OK
    }

    @Override
    public List<String> findAll() {
        return List.of("Produto A", "Produto B"); // OK
    }

    @Override
    public void save(String entity) {
        // Repositorio somente leitura nao pode salvar!
        throw new UnsupportedOperationException("Repositorio somente leitura!");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Repositorio somente leitura!");
    }

    @Override
    public List<String> findByFilter(String campo, Object valor) {
        return List.of(); // talvez ate suporte, mas e obrigado pela interface
    }

    @Override
    public void bulkSave(List<String> entities) {
        throw new UnsupportedOperationException("Repositorio somente leitura!");
    }

    @Override
    public void exportToCsv(String caminhoArquivo) {
        // Responsabilidade errada! Repositorio nao deveria exportar CSV.
        throw new UnsupportedOperationException("Nao e responsabilidade do repositorio!");
    }

    @Override
    public String generateReport() {
        throw new UnsupportedOperationException("Nao e responsabilidade do repositorio!");
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO DOS PROBLEMAS
// -----------------------------------------------------------------------
public class ViolandoISP {

    // Funcao que confia na interface — vai quebrar em runtime
    static void processarImpressora(ImpressoraGorda impressora, String doc) {
        System.out.println("Imprimindo...");
        impressora.imprimir(doc);

        System.out.println("Escaneando...");
        impressora.escanear(doc); // <- pode EXPLODIR com ImpressoraBasicaRuim!
    }

    // Funcao que trata trabalhadores — vai quebrar com Robo
    static void pausaParaAlmoco(TrabalhadorGordo trabalhador) {
        System.out.println("Hora do almoco!");
        trabalhador.comer(); // <- EXPLODE com RoboIndustrial!
    }

    public static void main(String[] args) {
        System.out.println("=== VIOLACAO 1: Impressora Gorda ===\n");

        ImpressoraGorda basica = new ImpressoraBasicaRuim();
        basica.imprimir("Ata da reuniao.pdf");   // OK

        try {
            basica.escanear("Contrato.pdf");      // EXCECAO!
        } catch (UnsupportedOperationException e) {
            System.out.println("EXCECAO: " + e.getMessage());
            System.out.println("*** Cliente confiou na interface — e foi enganado! ***\n");
        }

        basica.enviarFax("11 9999-0000", "Proposta.pdf"); // silencioso — nao funcionou!
        System.out.println("(fax enviado? nao sabemos — corpo vazio!)\n");

        System.out.println("=== VIOLACAO 2: Trabalhador vs Robo ===\n");

        TrabalhadorGordo humano = new TrabalhadorHumano("Ana");
        TrabalhadorGordo robo   = new RoboIndustrial("R2-D2");

        humano.trabalhar();
        humano.comer();

        robo.trabalhar();
        try {
            robo.comer(); // EXCECAO!
        } catch (UnsupportedOperationException e) {
            System.out.println("EXCECAO: " + e.getMessage());
            System.out.println("*** O sistema nao pode tratar humanos e robos uniformemente! ***\n");
        }

        System.out.println("=== VIOLACAO 3: Repository Gordo ===\n");

        RepositoryGordo<String> repo = new ProdutoReadOnlyRepository();
        System.out.println(repo.findById(1L));   // OK

        try {
            repo.save("Novo produto"); // EXCECAO!
        } catch (UnsupportedOperationException e) {
            System.out.println("EXCECAO: " + e.getMessage());
            System.out.println("*** O tipo diz que pode salvar, mas nao pode! ***");
        }

        /*
         * RESUMO DOS PROBLEMAS:
         *
         * 1. ImpressoraBasica e forcada a declarar 4 metodos que nao suporta
         * 2. RoboIndustrial e forcado a "comer" e "dormir" — absurdo
         * 3. ProdutoReadOnlyRepository declara save/delete — mentira estrutural
         * 4. Em TODOS os casos: quem chama os metodos descobre o problema APENAS em runtime
         * 5. Testes passam em compilacao, explodem em producao
         */
    }
}
