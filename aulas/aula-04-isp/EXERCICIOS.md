# Exercicios — Interface Segregation Principle

---

## Identificando Violacoes

**1.** O codigo abaixo viola o ISP? Qual e o problema concreto ao usar `ImpressoraJato` no lugar de `Impressora`?

```java
interface Impressora {
    void imprimir(String doc);
    void escanear(String arquivo);
    void enviarFax(String numero, String doc);
}

class ImpressoraJato implements Impressora {
    public void imprimir(String doc) { System.out.println("Imprimindo: " + doc); }
    public void escanear(String a)   { throw new UnsupportedOperationException("Sem scanner"); }
    public void enviarFax(String n, String d) { throw new UnsupportedOperationException("Sem fax"); }
}
```

**2.** Um desenvolvedor criou a seguinte interface. Identifique quantas responsabilidades distintas ela mistura e proponha uma divisao:

```java
interface UsuarioManager {
    void criarUsuario(String nome, String email);
    void removerUsuario(Long id);
    void autenticar(String email, String senha);
    void enviarEmailBoasVindas(String email);
    void gerarRelatorioDeAcesso(Long usuarioId);
    void exportarParaCsv(String caminhoArquivo);
}
```

**3.** Analise o cenario: "Toda vez que adicionamos um novo metodo na interface `Animal`, somos forcados a atualizar todas as 15 classes que a implementam, mesmo quando o metodo nao se aplica a elas." Que principio isso viola? Como corrigiria?

**4.** O codigo abaixo tem um corpo vazio no lugar de excecao. Isso ainda viola o ISP? Qual e o risco especifico dessa abordagem?

```java
interface Autenticador {
    boolean autenticar(String credencial);
    void revogarTodosOsTokens(Long usuarioId);
    List<String> listarSessoesAtivas(Long usuarioId);
}

class AutenticadorBasico implements Autenticador {
    public boolean autenticar(String c)          { return c != null && c.length() >= 8; }
    public void revogarTodosOsTokens(Long id)    { /* nao implementado */ }
    public List<String> listarSessoesAtivas(Long id) { return List.of(); }
}
```

**5.** Dado o seguinte uso, qual interface o cliente realmente precisa? A interface `ServicoCompleto` viola o ISP em relacao a esse cliente?

```java
interface ServicoCompleto {
    String buscarPorId(Long id);
    List<String> buscarTodos();
    void salvar(String entidade);
    void deletar(Long id);
    void publicarEvento(String tipo, String payload);
    void enviarNotificacao(Long usuarioId, String msg);
}

// Este cliente so faz isso:
class RelatorioMensal {
    private final ServicoCompleto servico;
    public void gerar() {
        List<String> dados = servico.buscarTodos();
        // processa dados...
    }
}
```

---

## Refatorando para o ISP

**6.** Refatore a interface `Impressora` do exercicio 1 em interfaces menores. Mostre como `ImpressoraJato`, `MultifuncionalEscritorio` e `ImpressoraEmpresarial` ficam apos a refatoracao.

**7.** Refatore a `UsuarioManager` do exercicio 2 em interfaces coesas. Para cada interface, justifique por que os metodos agrupados pertencem ao mesmo contexto.

**8.** Voce tem o seguinte repositorio. Refatore-o para que um repositorio somente leitura possa ser criado sem lancar excecoes:

```java
interface ProdutoRepository {
    Produto findById(Long id);
    List<Produto> findAll();
    void save(Produto p);
    void delete(Long id);
    List<Produto> findByCategoria(String cat);
    void importarDePlanilha(byte[] planilha);
}
```

**9.** Refatore a interface abaixo. O sistema precisa de: (a) servico que so envia email; (b) servico que envia email e SMS; (c) servico que envia email, SMS e push; (d) servico que so envia push:

```java
interface Notificador {
    void enviarEmail(String dest, String assunto, String corpo);
    void enviarSMS(String tel, String msg);
    void enviarPush(String deviceId, String titulo, String corpo);
    void enviarSlack(String canal, String msg);
}
```

**10.** O sistema de pagamento abaixo tem uma interface gorda. Refatore-a considerando que: PIX nao tem parcelamento, boleto nao tem reembolso imediato, e cartao de debito nao tem parcelamento:

```java
interface MetodoPagamento {
    boolean cobrar(double valor);
    boolean reembolsar(String transId, double valor);
    boolean parcelar(double valor, int n);
    String consultarStatus(String transId);
    boolean cancelar(String transId);
}
```

---

## Codigo para Implementar

**11.** Implemente um sistema de exportacao de dados com ISP:
- `Exportavel` com `byte[] exportar(List<String> dados)`
- `ExportadorCSV` — exporta como CSV
- `ExportadorJSON` — exporta como JSON (simplificado)
- `ExportadorPDF` — exporta como PDF (simulado)
- Funcao `void exportarRelatorio(Exportavel exp, List<String> dados)` — funciona para todos

**12.** Implemente um sistema de cache com tres niveis de acesso:
- `CacheLegivel<K,V>`: `Optional<V> get(K k)` e `boolean contem(K k)`
- `CacheGravavel<K,V>`: `void put(K k, V v)` e `void put(K k, V v, long ttlMs)`
- `CacheInvalidavel<K>`: `void invalidar(K k)` e `void limpar()`
- `CacheCompleto<K,V>` extendendo os tres
- `CacheEmMemoria` implementando `CacheCompleto`
- Mostre um servico que depende apenas de `CacheLegivel`

**13.** Implemente um sistema de validacao segregado:
- `Validador<T>`: `boolean validar(T valor)` e `String mensagemDeErro()`
- `ValidadorEmail` — valida formato de email
- `ValidadorCPF` — valida CPF (11 digitos)
- `ValidadorSenhaForte` — minimo 8 chars, 1 maiuscula, 1 numero
- `ValidadorCNPJ` — valida CNPJ (14 digitos)
- `PipelineValidacao<T>` — executa lista de validadores em cadeia

**14.** Crie um sistema de autenticacao com capacidades opcionais:
- `Autenticavel`: `boolean autenticar(String credencial)`
- `Renovavel`: `String renovar(String tokenExpirado)`
- `Revogavel`: `void revogar(String credencial)`
- `AutenticacaoSenha` implementa apenas `Autenticavel`
- `AutenticacaoJWT` implementa os tres
- `AutenticacaoOAuth` implementa `Autenticavel` e `Renovavel`
- Funcao `boolean verificarAcesso(Autenticavel auth, String cred)`

**15.** Implemente um sistema de log com interfaces segregadas:
- `LogLegivel`: `List<String> buscarLogs(String filtro)` e `String buscarUltimo()`
- `LogGravavel`: `void info(String msg)`, `void aviso(String msg)`, `void erro(String msg)`
- `LogLimpavel`: `void limpar()` e `void arquivar(String destino)`
- `LogCompleto` extendendo os tres
- `LogConsole` implementa apenas `LogGravavel`
- `LogArquivo` implementa `LogCompleto`
- Servico que depende apenas de `LogGravavel`

---

## Questoes de Reflexao

**16.** Qual e a diferenca entre ISP e SRP? Um colega diz: "ISP e so SRP para interfaces — sao a mesma coisa." Voce concorda? Qual e a nuance importante?

**17.** Em quais situacoes uma interface com 1 unico metodo (como `Runnable`, `Callable`, `Comparator`) faz sentido e nao e "granularidade excessiva"? Como distinguir ISP correto de over-engineering?

**18.** O ISP pode ser violado mesmo sem `UnsupportedOperationException`? Descreva um cenario onde a violacao e silenciosa (sem excecao, sem corpo vazio) mas ainda causa problemas.

**19.** Uma equipe argumenta: "Nossa interface tem 12 metodos, mas cada implementacao usa pelo menos 8 deles — entao nao viola o ISP." Voce concorda com esse argumento? Que perguntas faria para investigar?

**20.** Como o ISP se relaciona com o conceito de *acoplamento*? Uma classe que depende de uma interface gorda tem mais ou menos acoplamento do que uma que depende de uma interface pequena? Quais sao as consequencias praticas para testes, substituicao e evolucao do sistema?

---

*[Voltar ao conteudo](./README.md)*
