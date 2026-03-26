/**
 * EXEMPLO 04 — LSP em Hierarquias Reais
 *
 * Cenarios mais proximos do dia a dia:
 * - Sistema de notificacao (e-mail, SMS, push, slack)
 * - Repositorio de dados (banco, cache, arquivo)
 * - Animais no zoologico (diferentes comportamentos)
 *
 * Mostramos como modelar hierarquias profundas sem violar o LSP,
 * usando composicao de interfaces ao inves de heranca fragil.
 */

import java.util.List;
import java.util.Arrays;

// ======================================================================
// SISTEMA DE NOTIFICACAO
// ======================================================================

/**
 * Contrato base: toda notificacao pode ser enviada e tem um destinatario.
 * Pre: destinatario != null e nao vazio, mensagem != null
 * Pos: mensagem entregue ao canal (sem garantia de leitura pelo usuario)
 */
interface Notificacao {
    void enviar(String destinatario, String mensagem);
    String getCanal();
}

// Todas as implementacoes honram o contrato — nenhuma lanca excecao surpresa.

class NotificacaoEmail implements Notificacao {
    @Override
    public void enviar(String destinatario, String mensagem) {
        System.out.println("[EMAIL -> " + destinatario + "] " + mensagem);
    }
    @Override public String getCanal() { return "Email"; }
}

class NotificacaoSMS implements Notificacao {
    @Override
    public void enviar(String destinatario, String mensagem) {
        // SMS tem limite de 160 chars — mas AINDA honra o contrato (nao lanca excecao)
        String msg = mensagem.length() > 160 ? mensagem.substring(0, 157) + "..." : mensagem;
        System.out.println("[SMS -> " + destinatario + "] " + msg);
    }
    @Override public String getCanal() { return "SMS"; }
}

class NotificacaoPush implements Notificacao {
    @Override
    public void enviar(String destinatario, String mensagem) {
        System.out.println("[PUSH -> " + destinatario + "] " + mensagem);
    }
    @Override public String getCanal() { return "Push"; }
}

class NotificacaoSlack implements Notificacao {
    @Override
    public void enviar(String destinatario, String mensagem) {
        System.out.println("[SLACK #" + destinatario + "] " + mensagem);
    }
    @Override public String getCanal() { return "Slack"; }
}

// Servico de notificacao — usa qualquer Notificacao de forma segura
class ServicoDeNotificacao {

    private final List<Notificacao> canais;

    public ServicoDeNotificacao(List<Notificacao> canais) {
        this.canais = canais;
    }

    public void notificarTodos(String destinatario, String mensagem) {
        System.out.println("=== Notificando " + destinatario + " em " + canais.size() + " canal(is) ===");
        for (Notificacao canal : canais) {
            canal.enviar(destinatario, mensagem); // NUNCA vai quebrar — contrato respeitado
        }
        System.out.println();
    }
}

// ======================================================================
// REPOSITORIO DE DADOS
// ======================================================================

/**
 * Contrato: operacoes CRUD basicas.
 * Pre de salvar: entidade != null
 * Pos de salvar: entidade persiste ate que seja deletada
 * Pre de buscar: id valido
 * Pos de buscar: retorna entidade ou null (nao lanca excecao para "nao encontrado")
 */
interface Repositorio<T> {
    void salvar(String id, T entidade);
    T buscar(String id);
    void deletar(String id);
}

class RepositorioEmMemoria<T> implements Repositorio<T> {
    private final java.util.Map<String, T> dados = new java.util.HashMap<>();

    @Override
    public void salvar(String id, T entidade) {
        dados.put(id, entidade);
        System.out.println("[MEMORIA] Salvo: " + id);
    }

    @Override
    public T buscar(String id) {
        T resultado = dados.get(id);
        System.out.println("[MEMORIA] Busca " + id + ": " + (resultado != null ? "encontrado" : "nao encontrado"));
        return resultado;
    }

    @Override
    public void deletar(String id) {
        dados.remove(id);
        System.out.println("[MEMORIA] Deletado: " + id);
    }
}

class RepositorioComCache<T> implements Repositorio<T> {
    private final Repositorio<T> repositorioPrincipal;
    private final java.util.Map<String, T> cache = new java.util.HashMap<>();

    public RepositorioComCache(Repositorio<T> repositorioPrincipal) {
        this.repositorioPrincipal = repositorioPrincipal;
    }

    @Override
    public void salvar(String id, T entidade) {
        repositorioPrincipal.salvar(id, entidade);
        cache.put(id, entidade); // atualiza cache
    }

    @Override
    public T buscar(String id) {
        if (cache.containsKey(id)) {
            System.out.println("[CACHE] Hit para: " + id);
            return cache.get(id);
        }
        T resultado = repositorioPrincipal.buscar(id);
        if (resultado != null) cache.put(id, resultado);
        return resultado;
    }

    @Override
    public void deletar(String id) {
        repositorioPrincipal.deletar(id);
        cache.remove(id); // invalida cache
    }
}

// Servico que usa Repositorio — funciona identico com QUALQUER implementacao
class ServicoDeUsuario {

    private final Repositorio<String> repositorio;

    public ServicoDeUsuario(Repositorio<String> repositorio) {
        this.repositorio = repositorio;
    }

    public void cadastrar(String id, String nome) {
        repositorio.salvar(id, nome);
    }

    public String consultar(String id) {
        return repositorio.buscar(id);
    }

    public void remover(String id) {
        repositorio.deletar(id);
    }
}

// ======================================================================
// ANIMAIS DO ZOOLOGICO — hierarquia com capacidades compostas
// ======================================================================

interface Animal {
    String getNome();
    String getEspecie();
    void respirar();
    void comer(String alimento);
}

interface Corredeiro { void correr(); double getVelocidadeMaxima(); }
interface Trepador   { void trepar(); }
interface Nadador    { void nadar(); }
interface Voador     { void voar(); }

// Leao: corre, nao trepa nem nada profissionalmente
class Leao implements Animal, Corredeiro {
    @Override public String getNome()    { return "Simba"; }
    @Override public String getEspecie() { return "Leao"; }
    @Override public void respirar()     { System.out.println("Leao respirando ar."); }
    @Override public void comer(String a){ System.out.println("Leao comendo " + a + "."); }
    @Override public void correr()       { System.out.println("Leao correndo a " + getVelocidadeMaxima() + " km/h!"); }
    @Override public double getVelocidadeMaxima() { return 80; }
}

// Macaco: trepa e corre
class Macaco implements Animal, Corredeiro, Trepador {
    @Override public String getNome()    { return "Chico"; }
    @Override public String getEspecie() { return "Macaco"; }
    @Override public void respirar()     { System.out.println("Macaco respirando ar."); }
    @Override public void comer(String a){ System.out.println("Macaco comendo " + a + "."); }
    @Override public void correr()       { System.out.println("Macaco correndo!"); }
    @Override public double getVelocidadeMaxima() { return 55; }
    @Override public void trepar()       { System.out.println("Macaco trepando na arvore!"); }
}

// Pato (do zoologico): voa, corre e nada
class PatoZoo implements Animal, Voador, Nadador, Corredeiro {
    @Override public String getNome()    { return "Donald"; }
    @Override public String getEspecie() { return "Pato"; }
    @Override public void respirar()     { System.out.println("Pato respirando ar."); }
    @Override public void comer(String a){ System.out.println("Pato comendo " + a + "."); }
    @Override public void voar()         { System.out.println("Pato voando!"); }
    @Override public void nadar()        { System.out.println("Pato nadando no lago."); }
    @Override public void correr()       { System.out.println("Pato correndo (meio sem graca)."); }
    @Override public double getVelocidadeMaxima() { return 8; }
}

// Cobra: nenhuma capacidade especial — so Animal
class Cobra implements Animal {
    @Override public String getNome()    { return "Naja"; }
    @Override public String getEspecie() { return "Cobra"; }
    @Override public void respirar()     { System.out.println("Cobra respirando ar."); }
    @Override public void comer(String a){ System.out.println("Cobra engolindo " + a + " inteiro."); }
}

// Servico do zoologico — usa apenas o que cada interface garante
class ZoologicoService {

    static void fazerComer(Animal animal, String alimento) {
        System.out.print(animal.getNome() + " (" + animal.getEspecie() + "): ");
        animal.comer(alimento);
    }

    static void fazerCorrer(List<Corredeiro> corredores) {
        System.out.println("-- Corrida no zoologico! --");
        corredores.forEach(c -> {
            c.correr();
        });
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO COMPLETA
// -----------------------------------------------------------------------
public class LSPHierarquias {

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE NOTIFICACAO ===\n");

        ServicoDeNotificacao servico = new ServicoDeNotificacao(Arrays.asList(
            new NotificacaoEmail(),
            new NotificacaoSMS(),
            new NotificacaoPush(),
            new NotificacaoSlack()
        ));
        servico.notificarTodos("joao@email.com", "Sua conta foi aprovada!");

        System.out.println("=== REPOSITORIO COM CACHE ===\n");

        Repositorio<String> memRepo    = new RepositorioEmMemoria<>();
        Repositorio<String> cacheRepo  = new RepositorioComCache<>(memRepo);
        ServicoDeUsuario svc           = new ServicoDeUsuario(cacheRepo);

        svc.cadastrar("u001", "Ana Silva");
        svc.cadastrar("u002", "Bruno Costa");
        System.out.println("Consulta 1: " + svc.consultar("u001")); // cache miss
        System.out.println("Consulta 2: " + svc.consultar("u001")); // cache hit
        svc.remover("u001");
        System.out.println("Consulta 3: " + svc.consultar("u001")); // nao encontrado

        System.out.println("\n=== ZOOLOGICO ===\n");

        Leao leao = new Leao();
        Macaco macaco = new Macaco();
        PatoZoo pato = new PatoZoo();
        Cobra cobra = new Cobra();

        // Todo animal come
        ZoologicoService.fazerComer(leao, "gazela");
        ZoologicoService.fazerComer(macaco, "banana");
        ZoologicoService.fazerComer(pato, "peixe");
        ZoologicoService.fazerComer(cobra, "rato");

        System.out.println();
        // Apenas corredeiros correm
        ZoologicoService.fazerCorrer(Arrays.asList(leao, macaco, pato));

        System.out.println();
        // Apenas voadores voam
        System.out.println("-- Quem voa? --");
        pato.voar(); // pato voa

        System.out.println();
        // Apenas nadadores nadam
        System.out.println("-- Quem nada? --");
        pato.nadar(); // pato nada

        /*
         * CONCLUSAO:
         *
         * Hierarquias bem projetadas para o LSP:
         * 1. Interfaces pequenas por CAPACIDADE (Voador, Nadador, Corredeiro)
         * 2. Sem metodos "vazios" ou lancando excecao
         * 3. Substituicao sem surpresas — codigo nao precisa de instanceof
         * 4. Cada implementacao e um "participante honesto" do contrato
         */
    }
}
