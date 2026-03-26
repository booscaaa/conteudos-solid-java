# Liskov Substitution Principle — LSP

> "Se S e um subtipo de T, entao objetos do tipo T podem ser substituidos por objetos do tipo S sem alterar nenhuma propriedade desejavel do programa." — Barbara Liskov, 1987

📊 **[Abrir apresentacao de slides](https://htmlpreview.github.io/?https://github.com/booscaaa/conteudos-solid-java/blob/main/aulas/aula-03-lsp/lsp.html)** — navegue com as setas `←` `→` do teclado.

---

## 1. A Origem do Principio

### Barbara Liskov — A Cientista por tras do L

O principio foi formulado pela cientista da computacao americana **Barbara Liskov** em 1987, em um discurso chamado *"Data Abstraction and Hierarchy"*. Em 2008, ela recebeu o **Premio Turing** — o "Nobel da Computacao" — em parte por este trabalho.

A formulacao tecnica original e matematica:

> "Seja φ(x) uma propriedade provavel sobre objetos x do tipo T. Entao φ(y) deve ser verdadeiro para objetos y do tipo S, onde S e subtipo de T."

Na pratica: **se voce trocar um objeto por um de sua subclasse, tudo deve continuar funcionando igual.**

---

## 2. O que e o LSP?

O **Liskov Substitution Principle** estabelece que subclasses devem ser **substituiveis** pela superclasse sem que o programa precise saber disso.

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│   REGRA CENTRAL:                                             │
│                                                              │
│   Se voce tem uma funcao que aceita um Animal,               │
│   ela deve funcionar corretamente recebendo                  │
│   um Cachorro, um Gato ou um Papagaio —                      │
│   sem if, sem try/catch, sem instanceof.                     │
│                                                              │
│   A subclasse nao pode QUEBRAR o contrato da superclasse.    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### A Metafora do Contrato

Imagine que voce contrata uma empresa de entregas com o contrato:
- "Entregamos pacotes de ate 10kg em ate 3 dias uteis."

Uma subempresa que subcontrata esse trabalho pode entregar em **1 dia** (melhor que o contrato), mas nao pode entregar **em 7 dias** (pior que o prometido). O cliente confiou no contrato original.

---

## 3. Violando o LSP — Os Problemas Classicos

### 3.1 O Problema do Retangulo / Quadrado

O exemplo mais famoso de violacao do LSP:

```java
class Retangulo {
    void setLargura(double l)  { this.largura = l; }
    void setAltura(double a)   { this.altura = a; }
    double calcularArea()      { return largura * altura; }
}

class Quadrado extends Retangulo {
    @Override
    void setLargura(double l) {
        this.largura = l;
        this.altura = l;  // <- muda a altura sem avisar!
    }
    @Override
    void setAltura(double a) {
        this.altura = a;
        this.largura = a;  // <- muda a largura sem avisar!
    }
}
```

**O problema:** codigo que usa `Retangulo` assume que `setLargura` nao altera a altura:

```java
Retangulo r = new Quadrado(); // parece valido — Quadrado "e-um" Retangulo
r.setLargura(5);
r.setAltura(3);
// Area esperada: 15.0
// Area real:      9.0  <- ERRADO! Quadrado sobrescreveu a largura ao setar a altura
```

### 3.2 Ave que nao pode voar

```java
class Ave {
    void voar() { /* toda Ave voa */ }
}

class Pinguim extends Ave {
    @Override
    void voar() {
        throw new UnsupportedOperationException("Pinguim nao voa!");
    }
}

// Codigo que usa Ave:
void fazerAveVoar(Ave ave) {
    ave.voar(); // -> EXPLODE com Pinguim
}
```

### 3.3 Sinal classico de violacao: instanceof

```java
// Se voce escreve isso, e violacao do LSP:
if (animal instanceof Pinguim) {
    ((Pinguim) animal).nadar();
} else {
    animal.voar();
}
```

O codigo que usa a abstração `Animal` **nao deveria saber** o tipo real.

---

## 4. As Regras Formais do LSP

O LSP vai alem de "nao lance excecoes". Ele define tres regras:

### 4.1 Pre-condicoes

A subclasse **nao pode ser mais restritiva** que a superclasse.

```
Superclasse aceita: valor > 0
Subclasse aceita:   valor >= 50  <- VIOLA (mais restritiva)
Subclasse aceita:   valor >= 0   <- OK (igual ou mais permissiva)
```

### 4.2 Pos-condicoes

A subclasse **nao pode garantir menos** que a superclasse.

```
Superclasse garante: saldo sempre aumenta apos deposito
Subclasse:           saldo pode nao aumentar  <- VIOLA (garante menos)
Subclasse:           saldo aumenta E envia notificacao  <- OK (garante mais)
```

### 4.3 Invariantes

Propriedades que a superclasse mantem **sempre verdadeiras** devem ser mantidas pela subclasse.

---

## 5. A Solucao — Redesenhar as Hierarquias

A solucao para violacoes de LSP e geralmente **compor interfaces de capacidade** em vez de herdar de uma superclasse com comportamento demais.

```
SEM LSP (heranca problematica):
  Ave
  ├── Pardal   (voa — ok)
  ├── Aguia    (voa — ok)
  └── Pinguim  (nao voa — PROBLEMA)

COM LSP (interfaces de capacidade):
  AveLSP (comer, emitirSom)
  ├── Pardal implements AveLSP, AveVoadora
  ├── Aguia  implements AveLSP, AveVoadora
  └── Pinguim implements AveLSP, AveNadadora  (honesto sobre suas capacidades)
```

---

## 6. Como Identificar Violacoes

```
┌───────────────────────────────────────────────────────────────┐
│  SINAIS DE VIOLACAO DO LSP:                                   │
│                                                               │
│  [ ] Metodo sobrescrito lanca UnsupportedOperationException   │
│  [ ] Metodo sobrescrito nao faz nada (corpo vazio)            │
│  [ ] Codigo cliente usa instanceof para decidir o que fazer   │
│  [ ] Subclasse tem propriedade que superclasse nao pode ter   │
│  [ ] Precisa de try/catch para usar subclasse no lugar da base│
│  [ ] Resultado diferente do esperado (sem excecao, mas errado)│
└───────────────────────────────────────────────────────────────┘
```

---

## 7. LSP e os Outros Principios

O LSP se complementa com:

- **OCP**: o OCP pede que voce extenda sem modificar. O LSP garante que a extensao e segura.
- **ISP**: Interfaces pequenas (ISP) tornam mais facil respeitar o LSP — subclasses so implementam o que realmente podem fazer.
- **DIP**: Depender de abstracoes so e seguro se as subclasses respeitam o LSP.

---

## 8. Exemplos de Codigo

Os exemplos estao na pasta `exemplos/`:

| Arquivo                                                                            | Descricao                                               |
| ---------------------------------------------------------------------------------- | ------------------------------------------------------- |
| [01_violando_lsp.java](./exemplos/01_violando_lsp.java)                            | Retangulo/Quadrado, Ave/Pinguim, Conta somente leitura  |
| [02_aplicando_lsp.java](./exemplos/02_aplicando_lsp.java)                          | Hierarquias corrigidas com interfaces de capacidade     |
| [03_lsp_contratos.java](./exemplos/03_lsp_contratos.java)                          | Pre e pos-condicoes — violacoes silenciosas              |
| [04_lsp_hierarquias.java](./exemplos/04_lsp_hierarquias.java)                      | Notificacoes, repositorios e animais do zoologico        |

---

## 9. Resumo

```
┌─────────────────────────────────────────────────────────────┐
│           LISKOV SUBSTITUTION PRINCIPLE                      │
│                                                              │
│  PRINCIPIO: Subclasse deve poder substituir sua superclasse  │
│             sem quebrar o programa.                          │
│                                                              │
│  SINAL DE PROBLEMA:                                          │
│    - Metodo que lanca excecao ou nao faz nada               │
│    - instanceof no codigo cliente                            │
│    - Resultado diferente do esperado                         │
│    - Subclasse mais restritiva que a base                    │
│                                                              │
│  SOLUCAO:                                                    │
│    - Modelar por COMPORTAMENTO, nao por "e-um" do mundo real │
│    - Usar interfaces pequenas de capacidade                  │
│    - Nunca sobrescrever metodo para quebrar o contrato       │
│    - Prefira composicao sobre heranca quando necessario      │
│                                                              │
│  REGRAS FORMAIS:                                             │
│    - Pre-condicao: nao pode ser mais restritiva              │
│    - Pos-condicao: nao pode garantir menos                   │
│    - Invariante: propriedades da base devem ser mantidas     │
└─────────────────────────────────────────────────────────────┘
```

---

## Exercicios

[EXERCICIOS.md](./EXERCICIOS.md) — 20 exercicios sobre o Liskov Substitution Principle.

---

*[Voltar ao inicio](../../README.md)*
