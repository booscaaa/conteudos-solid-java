# Single Responsibility Principle — SRP

> "Uma classe deve ter apenas uma razao para mudar." — Robert C. Martin

---

## 1. A Origem do SOLID

### Robert C. Martin — "Uncle Bob"

**Robert Cecil Martin** (1952 — presente), conhecido carinhosamente como **Uncle Bob**, e um dos engenheiros de software mais influentes do mundo. Programador desde os anos 1970, tornou-se referencia mundial em programacao orientada a objetos, desenvolvimento agil e praticas de codigo limpo.

Uncle Bob foi um dos 17 signatarios do **Manifesto Agil** em 2001 e autor de livros fundamentais como:

- *Clean Code* (Codigo Limpo) — 2008
- *Clean Architecture* — 2017
- *The Clean Coder* — 2011

### A Origem do Acronimo SOLID

O termo **SOLID** foi popularizado por Robert Martin no inicio dos anos 2000, reunindo cinco principios que ele e outros engenheiros vinham defendendo ha decadas. O acronimo em si foi cunhado por **Michael Feathers** a partir dos principios listados por Martin.

Esses principios nao sao invencoes arbitrarias — eles sao o resultado de decadas de experiencia coletiva da industria, observando o que faz o codigo envelhecer bem ou mal.

---

## 2. O que e o SRP?

O **Single Responsibility Principle** (Principio da Responsabilidade Unica) diz:

> **Uma classe deve ter apenas uma razao para mudar.**

Isso significa que cada classe deve ser responsavel por **uma unica parte** da funcionalidade do sistema. Se uma classe tem mais de uma responsabilidade, ela tem mais de uma razao para ser modificada — e isso e um problema.

### Por que "razao para mudar"?

A definicao fala em "razao para mudar", nao em "uma unica coisa". A diferenca e sutil mas importante.

Imagine uma classe `RelatorioDeVendas`:

```
Razao 1: O formato do relatorio mudou (negocio)
Razao 2: O banco de dados foi trocado (infraestrutura)
Razao 3: As regras de calculo de comissao mudaram (regra de negocio)
```

Se a mesma classe cuida das tres coisas, qualquer mudanca em qualquer uma delas afeta a classe inteira — aumentando o risco de introducao de bugs e dificultando os testes.

---

## 3. Violando o SRP — O Problema

Veja este exemplo classico: uma classe `Usuario` que faz tudo.

```java
public class Usuario {
    private String nome;
    private String email;
    private String senha;

    // Regra de negocio
    public boolean validarSenha(String senha) { ... }

    // Persistencia no banco
    public void salvarNoBanco() { ... }
    public Usuario buscarPorId(int id) { ... }

    // Comunicacao por email
    public void enviarEmailBoasVindas() { ... }
    public void enviarEmailRedefinicaoSenha() { ... }

    // Relatorio
    public String gerarRelatorioDeAtividade() { ... }
}
```

### Quantas razoes essa classe tem para mudar?

```
┌────────────────────────────────────────────────────────────┐
│                    class Usuario                            │
│                                                            │
│  [Dados do usuario]    <- muda se o modelo mudar           │
│  [Validacao de senha]  <- muda se as regras mudarem        │
│  [Acesso ao banco]     <- muda se o banco mudar            │
│  [Envio de email]      <- muda se o provedor de email mudar│
│  [Geracao de relatorio]<- muda se o formato mudar          │
│                                                            │
│              5 razoes para mudar!                          │
└────────────────────────────────────────────────────────────┘
```

Cada nova responsabilidade e um acoplamento a mais. Testar a logica de validacao de senha sem ter um banco de dados configurado? Impossivel com esse design.

---

## 4. Aplicando o SRP — A Solucao

A solucao e separar cada responsabilidade em sua propria classe:

```
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│    Usuario      │   │  UsuarioRepo    │   │  EmailService   │
│                 │   │                 │   │                 │
│  - nome         │   │  + salvar()     │   │  + enviarBoas   │
│  - email        │   │  + buscarPorId()│   │    Vindas()     │
│  - senha        │   │                 │   │  + enviarRedefinicao() │
│  + validarSenha │   │  [1 razao para  │   │                 │
│    ()           │   │   mudar]        │   │  [1 razao para  │
│                 │   │                 │   │   mudar]        │
│  [1 razao para  │   └─────────────────┘   └─────────────────┘
│   mudar]        │
└─────────────────┘   ┌─────────────────┐
                      │ RelatorioService │
                      │                 │
                      │  + gerarRelatorio│
                      │    DeAtividade() │
                      │                 │
                      │  [1 razao para  │
                      │   mudar]        │
                      └─────────────────┘
```

Agora cada classe tem **uma unica razao para mudar**. Se o banco de dados muda, so `UsuarioRepository` e afetado. Se o formato do email muda, so `EmailService` e afetado.

---

## 5. SRP nao e so sobre Classes

O principio se aplica em varios niveis:

### Nivel de Metodo

```java
// RUIM — um metodo fazendo tres coisas
public void processarPedido(Pedido pedido) {
    // 1. Valida o pedido
    if (pedido.getItens().isEmpty()) throw new Exception("Pedido vazio");

    // 2. Calcula o total
    double total = pedido.getItens().stream()
        .mapToDouble(Item::getPreco).sum();

    // 3. Salva no banco
    banco.salvar(pedido);

    // 4. Envia email de confirmacao
    emailService.enviarConfirmacao(pedido.getCliente().getEmail());
}

// BOM — cada metodo faz uma coisa
public void processarPedido(Pedido pedido) {
    validarPedido(pedido);
    calcularTotal(pedido);
    salvarPedido(pedido);
    notificarCliente(pedido);
}
```

### Nivel de Modulo/Pacote

```
com.empresa.sistema/
├── usuario/           <- tudo relacionado a usuario
├── pedido/            <- tudo relacionado a pedido
├── pagamento/         <- tudo relacionado a pagamento
└── notificacao/       <- tudo relacionado a notificacoes
```

---

## 6. Como Identificar uma Violacao do SRP?

Faca estas perguntas sobre a sua classe:

```
┌─────────────────────────────────────────────────────────────┐
│              CHECKLIST — SRP                                 │
│                                                              │
│  [ ] "Essa classe faz mais de uma coisa distinta?"          │
│                                                              │
│  [ ] "Ela tem dependencias de mais de um sistema externo    │
│       (banco, email, relatorio)?"                            │
│                                                              │
│  [ ] "Se eu precisar testar uma parte, sou forcado a        │
│       configurar tudo?"                                      │
│                                                              │
│  [ ] "Quando descrevo o que a classe faz, uso o 'e'?        │
│       Ex: 'ela valida o usuario E salva no banco E envia    │
│       email'"                                                │
│                                                              │
│  Se respondeu SIM para qualquer uma: violacao do SRP!       │
└─────────────────────────────────────────────────────────────┘
```

Se a descricao da classe inclui a palavra **"e"** ("ela busca usuarios **e** envia emails **e** gera relatorios"), quase sempre ha uma violacao.

---

## 7. Beneficios do SRP

| Beneficio          | Explicacao                                                            |
| ------------------ | --------------------------------------------------------------------- |
| **Testabilidade**  | Classes pequenas e focadas sao faceis de testar unitariamente         |
| **Legibilidade**   | O codigo comunica claramente seu proposito                            |
| **Manutencao**     | Mudancas ficam isoladas — menor risco de quebrar coisas nao-relacionadas |
| **Reuso**          | Uma classe de email pode ser reutilizada em qualquer parte do sistema |
| **Coesao**         | Tudo que esta na classe pertence a ela — nada de fora, nada a mais   |

---

## 8. Cuidados e Equivocos Comuns

### Nao confunda "responsabilidade" com "metodo"

SRP nao diz que uma classe deve ter um unico metodo. Uma classe `Pedido` pode ter `adicionarItem()`, `removerItem()`, `calcularTotal()` — todos pertencem ao mesmo contexto (gerenciamento do pedido).

### Nao exagere na separacao

Uma classe `NomeDoUsuarioFormatter` so para formatar o nome e exagero. O objetivo e coesao, nao atomizacao.

### O contexto importa

Em sistemas pequenos, uma unica classe pode acumular mais responsabilidades sem grande problema. O SRP brilha em sistemas que crescem e evoluem ao longo do tempo.

---

## 9. Exemplos de Codigo

Os exemplos estao na pasta `exemplos/`:

| Arquivo                                                                     | Descricao                                              |
| --------------------------------------------------------------------------- | ------------------------------------------------------ |
| [01_violando_srp.java](./exemplos/01_violando_srp.java)                     | Classe `Funcionario` violando o SRP — o problema       |
| [02_aplicando_srp.java](./exemplos/02_aplicando_srp.java)                   | Mesma logica refatorada com SRP — a solucao            |
| [03_srp_em_servicos.java](./exemplos/03_srp_em_servicos.java)               | SRP aplicado em camadas de servico                     |
| [04_srp_avancado.java](./exemplos/04_srp_avancado.java)                     | SRP no nivel de metodos e organizacao de pacotes       |

---

## 10. Resumo

```
┌─────────────────────────────────────────────────────────────┐
│              SINGLE RESPONSIBILITY PRINCIPLE                 │
│                                                              │
│  PRINCIPIO:  Uma classe = uma razao para mudar               │
│                                                              │
│  SINAL DE PROBLEMA:                                          │
│    - Classe que "faz A e B e C"                              │
│    - Teste que precisa configurar infra nao-relacionada      │
│    - Mudanca em X quebra comportamento de Y                  │
│                                                              │
│  SOLUCAO:                                                    │
│    - Extraia responsabilidades para classes proprias         │
│    - Nomeie cada classe com um substantivo claro             │
│    - Cada classe deve ter um unico motivo para existir       │
│                                                              │
│  LEMBRE-SE:                                                  │
│    - Coesao e o objetivo, nao fragmentacao                   │
│    - Contexto e tamanho do sistema importam                  │
│    - "Uma coisa" = um ator, um eixo de mudanca               │
└─────────────────────────────────────────────────────────────┘
```

---

## Exercicios

[EXERCICIOS.md](./EXERCICIOS.md) — 20 exercicios sobre o Single Responsibility Principle.

---

*[Voltar ao inicio](../../README.md)*
