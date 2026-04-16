# Exercicios — Dependency Inversion Principle

---

## Identificando Violacoes

**1.** O codigo abaixo viola o DIP? Identifique exatamente qual linha e o problema e explique o que acontece se precisarmos trocar o banco de dados:

```java
class UsuarioService {

    private final OracleBancoDados db = new OracleBancoDados("jdbc:oracle:thin:@//servidor:1521/producao");

    public void criarUsuario(String nome, String email) {
        db.executarSQL("INSERT INTO usuarios (nome, email) VALUES (?, ?)", nome, email);
    }
}
```

**2.** Analise o cenario: "Sempre que o time de infraestrutura troca a biblioteca de envio de SMS, o time de produto precisa abrir PRs nos servicos de negocio para atualizar os imports e chamadas." Qual principio isso viola? Como voce resolveria?

**3.** O codigo abaixo tem uma forma sutil de violacao do DIP. Identifique-a:

```java
class RelatorioService {

    public void gerarPDF(String titulo, List<String> dados) {
        ITextPDFLibrary lib = PDFLibraryFactory.getInstance(); // factory global
        byte[] pdf = lib.render(titulo, dados);
        System.out.println("PDF gerado: " + pdf.length + " bytes");
    }
}
```

**4.** Um desenvolvedor argumenta: "Minha classe so usa `System.out.println` para logs — isso viola o DIP?" Voce concorda? Em que contexto isso seria um problema real?

**5.** Dado o seguinte cenario: uma classe `ProcessadorPagamento` tem um campo `private final StripeGateway stripe = new StripeGateway(API_KEY)`. O produto vai expandir para suportar PayPal e PagSeguro. Como a presenca do `new StripeGateway` dificulta essa expansao, e qual seria a solucao DIP?

---

## Refatorando para o DIP

**6.** Refatore o `UsuarioService` do exercicio 1. Defina a interface correta, renomeie a implementacao Oracle e mostre como o servico seria montado no metodo `main`.

**7.** Refatore o codigo abaixo para respeitar o DIP. Mostre as interfaces criadas e como as implementacoes se encaixam:

```java
class NotificacaoService {

    private final TwilioSMSClient   smsClient   = new TwilioSMSClient("SID", "TOKEN");
    private final MailchimpEmailApi emailApi    = new MailchimpEmailApi("API_KEY");

    public void notificarUsuario(String telefone, String email, String mensagem) {
        smsClient.enviarTexto(telefone, mensagem);
        emailApi.dispararCampanha(email, "Notificacao", mensagem);
    }
}
```

**8.** Refatore o servico abaixo e mostre como seria possivel usar um `InMemoryLogStorage` nos testes e um `ElasticsearchLogStorage` em producao, sem alterar `AuditoriaService`:

```java
class AuditoriaService {

    public void registrar(String acao, String usuarioId) {
        ElasticsearchClient client = new ElasticsearchClient("https://es.empresa.com");
        client.index("auditoria", Map.of("acao", acao, "usuario", usuarioId));
    }
}
```

**9.** O sistema abaixo calcula frete sempre pelo mesmo transportador. Refatore para suportar Correios, FedEx e transportadora propria, injetando a estrategia de calculo:

```java
class CarrinhoService {

    public double calcularTotal(List<Double> itens, String cep) {
        CorreiosFrete correios = new CorreiosFrete();
        double frete = correios.calcular(cep, itens.size());
        return itens.stream().mapToDouble(d -> d).sum() + frete;
    }
}
```

**10.** Refatore a classe abaixo para que seja possivel trocar o provedor de autenticacao (LDAP, OAuth2, API interna) sem modificar `LoginController`:

```java
class LoginController {

    public boolean login(String usuario, String senha) {
        LDAPConexao ldap = new LDAPConexao("ldap://corp.local");
        return ldap.bind(usuario, senha);
    }
}
```

---

## Codigo para Implementar

**11.** Implemente um sistema de estoque com DIP:
- Interface `EstoqueRepository` com `int consultar(String produtoId)` e `void atualizar(String produtoId, int quantidade)`
- `InMemoryEstoqueRepository` — implementacao para testes
- `EstoqueService` recebe `EstoqueRepository` pelo construtor
- Metodo `boolean reservar(String produtoId, int quantidade)` — retorna `false` se estoque insuficiente
- Metodo `void repor(String produtoId, int quantidade)`

**12.** Implemente um sistema de notificacao com multiplos canais:
- Interface `Canal` com `void enviar(String destinatario, String mensagem)`
- `CanalEmail` — simula envio via email
- `CanalSMS` — simula envio via SMS
- `CanalPush` — simula notificacao push
- `NotificacaoService` recebe `List<Canal>` pelo construtor
- Metodo `void notificarTodos(String destinatario, String mensagem)` — dispara em todos os canais

**13.** Implemente um pipeline de processamento de pagamento com DIP:
- Interface `EtapaPagamento` com `boolean executar(Map<String, Object> contexto)` e `String nome()`
- `ValidacaoCartao` — verifica se numero tem 16 digitos
- `AntifraudeCheck` — retorna true sempre (simulado)
- `CobrancaGateway` — simula a cobranca
- `EnvioRecibo` — simula o envio do recibo
- `PipelinePagamento` recebe `List<EtapaPagamento>` e para na primeira falha

**14.** Implemente um sistema de exportacao com DIP:
- Interface `ExportadorDados` com `byte[] exportar(List<Map<String, Object>> dados)` e `String formato()`
- `ExportadorCSV` — gera string CSV simples
- `ExportadorJSON` — gera JSON simplificado
- `ServicoExportacao` recebe `ExportadorDados` pelo construtor
- Metodo `void exportarRelatorio(String titulo, List<Map<String, Object>> dados)`
- Mostre na demo como trocar CSV por JSON sem alterar `ServicoExportacao`

**15.** Implemente uma camada de cache com DIP e decorator:
- Interface `RepositorioUsuario` com `Optional<String> buscarPorId(String id)` e `void salvar(String id, String nome)`
- `RepositorioUsuarioMemoria` — implementacao base
- `RepositorioUsuarioComCache` — decorator que envolve qualquer `RepositorioUsuario`, verifica cache antes de chamar o delegate e atualiza o cache apos uma busca bem-sucedida
- Use `Map<String, String>` simples como cache interno
- Mostre na demo: primeira busca vai ao repositorio, segunda vem do cache

---

## Questoes de Reflexao

**16.** Qual e a diferenca entre DIP e Injecao de Dependencias? Um colega diz: "DIP e so usar frameworks como Spring." Voce concorda? Explique a relacao entre os dois conceitos.

**17.** Em que situacoes faz sentido nao aplicar o DIP? Por exemplo, uma classe utilitaria que usa `Math.random()` ou `String.format()` precisaria de injecao? Como decidir onde o DIP e necessario?

**18.** O Service Locator resolve o mesmo problema que a injecao pelo construtor, mas com desvantagens. Descreva um cenario concreto onde o Service Locator causaria dificuldade real em testes.

**19.** Uma equipe argumenta: "Criamos interfaces para tudo, mas so temos uma implementacao de cada. Nao e over-engineering?" Como voce responderia? Quais criterios usaria para decidir quando criar uma interface?

**20.** Como o DIP se relaciona com o conceito de "testabilidade"? Explique por que um sistema que viola o DIP e dificil de testar unitariamente e como a aplicacao do DIP resolve esse problema especificamente com relacao a banco de dados e servicos externos.

---

*[Voltar ao conteudo](./README.md)*
