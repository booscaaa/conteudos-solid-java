# SOLID com Java

Hub de conteúdos sobre os **Princípios SOLID** aplicados com Java.

Aqui você encontra explicações, exemplos de código e exercícios organizados por princípio. Todo o material é escrito em Java com foco em código limpo e boas práticas de design orientado a objetos.

---

## O que é SOLID?

**SOLID** é um acrônimo criado por **Robert C. Martin** (Uncle Bob) que representa cinco princípios de design de software orientado a objetos. Esses princípios visam tornar o código mais legível, flexível, testável e fácil de manter.

| Letra | Princípio                   | Definição curta                                              |
| ----- | --------------------------- | ------------------------------------------------------------ |
| **S** | Single Responsibility       | Uma classe deve ter apenas uma razão para mudar              |
| **O** | Open/Closed                 | Aberta para extensão, fechada para modificação               |
| **L** | Liskov Substitution         | Subclasses devem ser substituíveis pelas suas superclasses   |
| **I** | Interface Segregation       | Interfaces específicas são melhores que uma única genérica   |
| **D** | Dependency Inversion        | Dependa de abstrações, não de implementações concretas       |

---

## Estrutura do Repositório

```
conteudos-solid-java/
├── README.md                          <- Você está aqui
└── aulas/
    ├── aula-01-srp/
    │   ├── README.md                  <- Single Responsibility Principle
    │   ├── EXERCICIOS.md
    │   └── exemplos/
    │       ├── 01_violando_srp.java
    │       ├── 02_aplicando_srp.java
    │       ├── 03_srp_em_servicos.java
    │       └── 04_srp_avancado.java
    ├── aula-02-ocp/
    │   ├── README.md                  <- Open/Closed Principle
    │   ├── EXERCICIOS.md
    │   └── exemplos/
    │       ├── 01_violando_ocp.java
    │       ├── 02_aplicando_ocp.java
    │       ├── 03_ocp_com_interfaces.java
    │       └── 04_ocp_strategy_pattern.java
    └── aula-03-lsp/
        ├── README.md                  <- Liskov Substitution Principle
        ├── EXERCICIOS.md
        └── exemplos/
            ├── 01_violando_lsp.java
            ├── 02_aplicando_lsp.java
            ├── 03_lsp_contratos.java
            └── 04_lsp_hierarquias.java
```

---

## Conteudos

| #   | Letra | Principio                                                             | Assuntos                                                        |
| --- | ----- | --------------------------------------------------------------------- | --------------------------------------------------------------- |
| 01  | S     | [Single Responsibility Principle](./aulas/aula-01-srp/README.md)     | Uma classe, uma responsabilidade, coesao, separacao de papeis   |
| 02  | O     | [Open/Closed Principle](./aulas/aula-02-ocp/README.md)               | Extensao sem modificacao, polimorfismo, Strategy, interfaces     |
| 03  | L     | [Liskov Substitution Principle](./aulas/aula-03-lsp/README.md)       | Substituicao segura, contratos, hierarquias honestas             |

---

## Como usar os exemplos

1. Tenha o **JDK 17+** instalado
2. Compile com `javac NomeDoArquivo.java`
3. Execute com `java NomeDoArquivo`
4. Leia os comentarios no codigo — eles explicam cada decisao de design

---

## Dicas

> **Leia o codigo ruim primeiro.** Cada aula começa com um exemplo que viola o principio. Entender o problema e fundamental para valorizar a solucao.

> **Pergunte "por que?"** Antes de aplicar qualquer principio, pergunte por que ele existe. Principios sem contexto viram dogma.

> **Nao exagere.** SOLID e uma guia, nao uma regra absoluta. Aplicar todos os principios em todo lugar pode gerar complexidade desnecessaria.

---

## Contribuicoes

Sugestoes e correcoes sao bem-vindas!
