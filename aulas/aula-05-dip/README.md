# Dependency Inversion Principle — DIP

> "Modulos de alto nivel nao devem depender de modulos de baixo nivel. Ambos devem depender de abstracoes." — Robert C. Martin

📊 **[Abrir apresentacao de slides](https://htmlpreview.github.io/?https://github.com/booscaaa/conteudos-solid-java/blob/main/aulas/aula-05-dip/dip.html)** — navegue com as setas `←` `→` do teclado.

---

## 1. O Problema do Acoplamento Rigido

### O que e "depender de implementacoes"?

Quando um modulo de alto nivel (regra de negocio) cria ou referencia diretamente um modulo de baixo nivel (banco de dados, email, API externa), eles ficam rigidamente acoplados. Mudar um detalhe de infraestrutura exige alterar o codigo de negocio.

```
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│   SEM DIP — Dependencias para baixo (acoplamento rigido):     │
│                                                                │
│   PedidoService ──── new ──→ MySQLPedidoDatabase             │
│   PedidoService ──── new ──→ JavaMailEmailSender              │
│   PedidoService ──── new ──→ ConsoleLogger                    │
│                                                                │
│   Para trocar MySQL -> PostgreSQL:                             │
│     Alterar PedidoService (regra de negocio!)                 │
│                                                                │
│   Para testar PedidoService:                                   │
│     Precisa de MySQL real + servidor SMTP real                 │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

O codigo de negocio deveria ser a parte mais estavel do sistema — mas com acoplamento rigido, ele muda toda vez que um detalhe de infraestrutura muda.

---

## 2. O que diz o DIP?

O **Dependency Inversion Principle** tem duas partes:

> **Parte 1:** Modulos de alto nivel nao devem depender de modulos de baixo nivel. Ambos devem depender de abstracoes.

> **Parte 2:** Abstracoes nao devem depender de detalhes. Detalhes (implementacoes concretas) devem depender de abstracoes.

### A Inversao

O nome "inversao" vem da direcao das dependencias. Sem o principio, as dependencias apontam de cima para baixo (negocio -> infraestrutura). Com o DIP, as dependencias se invertem: a infraestrutura implementa interfaces definidas pelo dominio.

```
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│   COM DIP — Todos dependem de abstracoes:                     │
│                                                                │
│   PedidoService ────→ <<interface>> PedidoRepository          │
│   PedidoService ────→ <<interface>> EmailService              │
│   PedidoService ────→ <<interface>> Logger                    │
│                                                                │
│   MySQLPedidoRepository ──implements──→ PedidoRepository      │
│   JavaMailEmailService  ──implements──→ EmailService           │
│   ConsoleLogger         ──implements──→ Logger                 │
│                                                                │
│   Para trocar MySQL -> PostgreSQL:                             │
│     Criar PostgreSQLPedidoRepository — PedidoService intocado │
│                                                                │
│   Para testar PedidoService:                                   │
│     Injetar InMemoryPedidoRepository + SilentLogger           │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. Alto Nivel vs Baixo Nivel

Entender qual modulo e "alto" e qual e "baixo" e fundamental para aplicar o DIP corretamente.

```
┌────────────────────────────────────────────────────────────────┐
│  MODULOS DE ALTO NIVEL (regras de negocio, casos de uso):      │
│                                                                │
│  - PedidoService      — logica de criacao de pedidos          │
│  - AutenticacaoService — logica de autenticacao               │
│  - RelatorioService   — logica de geracao de relatorios       │
│  - CarrinhoService    — logica de calculo de totais           │
│                                                                │
│  Caracteristica: mudam quando as REGRAS DE NEGOCIO mudam.     │
│                                                                │
├────────────────────────────────────────────────────────────────┤
│  MODULOS DE BAIXO NIVEL (detalhes de infraestrutura):          │
│                                                                │
│  - MySQLPedidoRepository — como salvar no MySQL               │
│  - JavaMailEmailService  — como enviar email via SMTP          │
│  - LDAPIdentityProvider  — como autenticar no LDAP            │
│  - ITextPDFGenerator     — como gerar PDF com iText           │
│                                                                │
│  Caracteristica: mudam quando a TECNOLOGIA muda.              │
└────────────────────────────────────────────────────────────────┘
```

Regra pratica: se o nome da classe referencia uma tecnologia (MySQL, SMTP, LDAP, iText, S3), e um modulo de baixo nivel.

---

## 4. Injecao de Dependencias (DI)

O DIP diz **o que** deve acontecer (dependa de abstracoes). A **Injecao de Dependencias** e a tecnica que diz **como** fazer isso: alguem externo injeta as implementacoes.

### 4.1 Injecao pelo Construtor (a mais recomendada)

```java
class PedidoService {

    private final PedidoRepository repository;
    private final EmailService      emailService;

    // Dependencias declaradas, imutaveis, explícitas
    public PedidoService(PedidoRepository repository, EmailService emailService) {
        this.repository   = repository;
        this.emailService = emailService;
    }
}
```

Vantagens:
- Dependencias imutaveis (`final`)
- Objeto nunca criado em estado invalido
- Dependencias visiveis na assinatura do construtor
- Ideal para dependencias obrigatorias

### 4.2 Injecao por Setter (dependencias opcionais)

```java
class RelatorioService {

    private AuditoriaService auditoria; // opcional

    public void setAuditoria(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }
}
```

Use quando a dependencia e verdadeiramente opcional ou precisa ser configurada apos a construcao.

---

## 5. Quem Define as Abstracoes?

A resposta e contraintuitiva: as **abstracoes devem ser definidas pelo modulo de alto nivel**, nao pelo baixo nivel.

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ERRADO:                                                     │
│  MySQLPedidoDatabase define a interface                      │
│  PedidoService adapta para usar essa interface               │
│                                                              │
│  CORRETO:                                                    │
│  PedidoService define o contrato que precisa                 │
│  (interface PedidoRepository)                                │
│  MySQLPedidoDatabase implementa esse contrato                │
│                                                              │
│  O dominio manda. A infraestrutura obedece.                  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

Isso e a essencia da "inversao": o alto nivel nao se adapta ao baixo nivel — o baixo nivel se adapta ao alto nivel.

---

## 6. Formas de Composicao

Sem um framework de IoC, a composicao dos objetos acontece manualmente. Os lugares corretos para isso sao:

### 6.1 Metodo main (para aplicacoes simples)

```java
public static void main(String[] args) {
    PedidoService service = new PedidoService(
        new MySQLPedidoRepository(),
        new JavaMailEmailService(),
        new ConsoleLogger()
    );
}
```

### 6.2 Factory (para isolar a criacao)

```java
class AppFactory {
    public static PedidoService criarPedidoService() {
        return new PedidoService(
            new MySQLPedidoRepository(),
            new JavaMailEmailService(),
            new ConsoleLogger()
        );
    }
}
```

### 6.3 Framework de IoC (Spring, Quarkus, CDI)

Em producao, frameworks como Spring fazem a composicao automaticamente via anotacoes:

```java
@Service
class PedidoService {

    private final PedidoRepository repository;
    private final EmailService      emailService;

    @Autowired // Spring injeta as implementacoes automaticamente
    public PedidoService(PedidoRepository repository, EmailService emailService) {
        this.repository   = repository;
        this.emailService = emailService;
    }
}
```

---

## 7. Anti-Pattern: Service Locator

O **Service Locator** parece DIP mas esconde as dependencias dentro dos metodos, nao no construtor.

```
┌────────────────────────────────────────────────────────────────┐
│  SERVICE LOCATOR — parece bom, tem problemas ocultos           │
│                                                                │
│  class CadastroService {                                       │
│      public void cadastrar(String id, String email) {          │
│          // dependencias ocultas aqui dentro                   │
│          UsuarioRepository repo =                              │
│              ServiceLocator.obter(UsuarioRepository.class);    │
│          ...                                                   │
│      }                                                         │
│  }                                                             │
│                                                                │
│  PROBLEMAS:                                                    │
│  [ ] Dependencias invisiveis no construtor                     │
│  [ ] Para testar, precisa configurar o locator global          │
│  [ ] Acoplamento ao proprio ServiceLocator                     │
│  [ ] Erros de "servico nao registrado" so aparecem em runtime  │
└────────────────────────────────────────────────────────────────┘
```

**Prefira sempre injecao pelo construtor ao Service Locator.**

---

## 8. Sinais de Violacao do DIP

```
┌────────────────────────────────────────────────────────────────┐
│  CHECKLIST DE VIOLACAO DO DIP:                                 │
│                                                                │
│  [ ] Classe de negocio usa "new" para criar infraestrutura     │
│  [ ] Import de pacote de banco/email/HTTP em camada de negocio │
│  [ ] Classe de negocio tem "MySQL", "SMTP", "S3" no nome       │
│  [ ] Trocar banco de dados exige alterar regra de negocio      │
│  [ ] Testes de negocio precisam de banco/SMTP reais            │
│  [ ] Dependencias ocultas dentro de metodos (service locator)  │
│  [ ] Construtor sem parametros — dependencias criadas dentro   │
└────────────────────────────────────────────────────────────────┘
```

---

## 9. DIP e os Outros Principios

O DIP e o ponto de uniao dos quatro principios anteriores:

- **SRP**: cada interface tem uma responsabilidade clara e unica — contrato coeso para injecao.

- **OCP**: injetar uma nova implementacao adiciona comportamento sem modificar o codigo existente. O `PedidoService` nunca precisa de `if (tipo == "postgres")`.

- **LSP**: implementacoes injetadas devem ser substituiveis. Se `PostgreSQLRepository` quebra o contrato de `PedidoRepository`, o DIP nao funciona.

- **ISP**: interfaces injetadas devem ser pequenas e especificas. Injetar uma interface gorda viola tanto ISP quanto DIP.

- **DIP**: a cola que conecta tudo — garante que as abstracoes definidas pelos principios anteriores sejam de fato usadas como pontos de desacoplamento.

---

## 10. Exemplos de Codigo

Os exemplos estao na pasta `exemplos/`:

| Arquivo                                                                               | Descricao                                                          |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| [01_violando_dip.java](./exemplos/01_violando_dip.java)                               | Servico de pedidos, relatorio e autenticacao com acoplamento rigido|
| [02_aplicando_dip.java](./exemplos/02_aplicando_dip.java)                             | Mesmos cenarios refatorados com interfaces e injecao               |
| [03_dip_inversao.java](./exemplos/03_dip_inversao.java)                               | IoC, formas de injecao, factory e anti-pattern Service Locator     |
| [04_dip_avancado.java](./exemplos/04_dip_avancado.java)                               | Layered Architecture, Decorator, Strategy e Pipeline com DIP       |

---

## 11. Resumo

```
┌──────────────────────────────────────────────────────────────┐
│              DEPENDENCY INVERSION PRINCIPLE                   │
│                                                              │
│  PRINCIPIO:                                                  │
│    Alto nivel nao depende de baixo nivel.                    │
│    Ambos dependem de abstracoes (interfaces).                │
│    Detalhes dependem de abstracoes, nao o contrario.         │
│                                                              │
│  SINAL DE PROBLEMA:                                          │
│    - "new MySQL...", "new JavaMail..." em codigo de negocio  │
│    - Trocar banco/email exige alterar regra de negocio       │
│    - Testes precisam de infraestrutura real                  │
│    - Dependencias nao visiveis no construtor                 │
│                                                              │
│  SOLUCAO:                                                    │
│    - Definir interfaces para cada dependencia externa        │
│    - Injetar implementacoes pelo construtor                  │
│    - O dominio define as abstracoes                          │
│    - A infraestrutura implementa os contratos                │
│                                                              │
│  BENEFICIOS:                                                 │
│    - Trocar implementacoes sem alterar regras de negocio     │
│    - Testes unitarios sem infraestrutura real                │
│    - Codigo de negocio estavel mesmo com mudancas tecnicas   │
│    - Dependencias explicitas e verificaveis em compile-time  │
└──────────────────────────────────────────────────────────────┘
```

---

## Exercicios

[EXERCICIOS.md](./EXERCICIOS.md) — 20 exercicios sobre o Dependency Inversion Principle.

---

*[Voltar ao inicio](../../README.md)*
