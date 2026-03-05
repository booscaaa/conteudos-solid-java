# Open/Closed Principle — OCP

> "Entidades de software devem ser abertas para extensao, mas fechadas para modificacao." — Bertrand Meyer / Robert C. Martin

📊 **[Abrir apresentacao de slides](https://htmlpreview.github.io/?https://github.com/booscaaa/conteudos-solid-java/blob/main/aulas/aula-02-ocp/ocp.html)** — navegue com as setas `←` `→` do teclado.

---

## 1. A Origem do Principio

### Bertrand Meyer e o Eiffel

O principio Open/Closed foi formulado originalmente pelo cientista da computacao frances **Bertrand Meyer** em seu livro *Object-Oriented Software Construction* (1988). Meyer criou tambem a linguagem de programacao **Eiffel** e o conceito de **Design by Contract**.

No contexto de Meyer, o OCP significava principalmente: uma classe base (superclasse) nunca deveria ser modificada para acomodar novos comportamentos. Novos comportamentos deveriam ser adicionados via heranca.

### Robert C. Martin — A Reinterpretacao Moderna

**Uncle Bob** reapresentou o OCP nos anos 2000 com uma perspectiva mais ampla, baseada em **abstracoes e polimorfismo**, nao apenas em heranca. A interpretacao moderna do OCP e:

> Um modulo esta "fechado para modificacao" quando sua interface/contrato esta estavel.
> E "aberto para extensao" quando seu comportamento pode ser alterado sem tocar no codigo existente.

Essa reinterpretacao e o que torna o OCP pratico em linguagens modernas como Java.

---

## 2. O que e o OCP?

O **Open/Closed Principle** estabelece que um modulo de software deve ser:

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│   ABERTO para extensao                                       │
│   -> Voce pode adicionar novos comportamentos                │
│   -> Sem precisar modificar o codigo existente               │
│                                                              │
│   FECHADO para modificacao                                   │
│   -> O codigo ja escrito e testado nao deve ser tocado       │
│   -> Adicionar um novo caso nao quebra o que ja existe       │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### A Metafora da Tomada

Pense em uma tomada eletrica. Ela e:

- **Fechada para modificacao**: voce nao abre a parede e reconfigura os fios para usar um novo aparelho.
- **Aberta para extensao**: qualquer aparelho com o plugue certo pode ser conectado — liquidificador, carregador, TV.

O contrato (o plugue padrao) esta definido. Novos aparelhos (extensoes) se adaptam ao contrato, nao o contrario.

---

## 3. Violando o OCP — O Problema

O sinal mais classico de violacao do OCP e o `if/else` ou `switch` que cresce cada vez que um novo tipo e adicionado.

```java
class CalculadoraDeDesconto {

    public double calcular(String tipoCliente, double valorCompra) {
        if (tipoCliente.equals("COMUM")) {
            return valorCompra * 0.05;
        } else if (tipoCliente.equals("VIP")) {
            return valorCompra * 0.10;
        } else if (tipoCliente.equals("PREMIUM")) {
            return valorCompra * 0.20;
        }
        // E quando surgir o cliente "CORPORATIVO"?
        // Vamos ter que abrir esta classe e modificar este metodo.
        // Cada modificacao arrisca quebrar os casos que ja funcionam.
        return 0;
    }
}
```

### O que acontece quando o sistema cresce?

```
Semana 1: tipoCliente = "COMUM" ou "VIP"         -> 1 if/else
Semana 3: adicionamos "PREMIUM"                   -> 2 if/else
Semana 6: adicionamos "CORPORATIVO"               -> 3 if/else
Semana 9: adicionamos "FUNCIONARIO"               -> 4 if/else
Mes 6:    adicionamos "PARCEIRO" com regra complexa -> ...
```

A cada novo tipo, abrimos a classe, modificamos o metodo, re-testamos tudo do zero, e corremos o risco de introduzir bugs nos casos ja existentes. A classe **nunca para de crescer** e **nunca e realmente estavel**.

---

## 4. Aplicando o OCP — A Solucao

A solucao e definir uma **abstracao** (interface ou classe abstrata) e fazer cada tipo implementar essa abstracao.

```
┌─────────────────────────────────────────────┐
│           <<interface>>                      │
│         DescontoStrategy                     │
│                                              │
│  + calcular(double valorCompra): double      │
└──────────────────┬──────────────────────────┘
                   │ implementa
       ┌───────────┼───────────────┐
       ▼           ▼               ▼
┌────────────┐ ┌─────────┐ ┌────────────────┐
│ DescontoComum│ │DescontoVIP│ │DescontoPremium│
│            │ │         │ │               │
│ 5% desconto│ │10%      │ │20%            │
└────────────┘ └─────────┘ └───────────────┘
```

Para adicionar "CORPORATIVO": crie `DescontoCorporativo` implementando `DescontoStrategy`. Nenhuma outra classe e tocada.

---

## 5. Mecanismos de Extensao em Java

O OCP pode ser implementado com diferentes mecanismos:

### 5.1 Interfaces

```java
// O contrato — fechado para modificacao
interface DescontoStrategy {
    double calcular(double valorCompra);
}

// Extensoes — abertas para adicionar novas sem tocar nas existentes
class DescontoComum implements DescontoStrategy {
    public double calcular(double valorCompra) { return valorCompra * 0.05; }
}

class DescontoVIP implements DescontoStrategy {
    public double calcular(double valorCompra) { return valorCompra * 0.10; }
}
```

### 5.2 Classes Abstratas

Usadas quando ha logica compartilhada entre as implementacoes:

```java
abstract class Notificacao {
    // Template fixo — fechado para modificacao
    public final void enviar(String mensagem) {
        String formatada = formatar(mensagem);
        transmitir(formatada);
        registrarLog(formatada);
    }

    // Partes variaveis — abertas para extensao
    protected abstract String formatar(String mensagem);
    protected abstract void transmitir(String mensagem);

    private void registrarLog(String mensagem) {
        System.out.println("[LOG] Notificacao enviada: " + mensagem);
    }
}

class NotificacaoEmail extends Notificacao {
    protected String formatar(String m) { return "[EMAIL] " + m; }
    protected void transmitir(String m) { System.out.println("Enviando email: " + m); }
}

class NotificacaoSMS extends Notificacao {
    protected String formatar(String m) { return "[SMS] " + m.substring(0, Math.min(m.length(), 160)); }
    protected void transmitir(String m) { System.out.println("Enviando SMS: " + m); }
}
```

### 5.3 Composicao com Injecao de Dependencia

O OCP e frequentemente alcancado injetando a estrategia certa, em vez de codificar o comportamento na classe:

```java
class ProcessadorDePagamento {
    private final GatewayDePagamento gateway; // abstracao injetada

    public ProcessadorDePagamento(GatewayDePagamento gateway) {
        this.gateway = gateway;
    }

    public void processar(double valor) {
        gateway.cobrar(valor); // comportamento variavel via injecao
    }
}
```

---

## 6. O OCP e o Strategy Pattern

O **Strategy Pattern** (Padrao de Estrategia) e a implementacao mais direta do OCP. Ele define uma familia de algoritmos, encapsula cada um e os torna intercambiaveis.

```
┌─────────────────┐       usa        ┌──────────────────┐
│   Contexto      │ ───────────────► │  <<interface>>   │
│                 │                  │  Strategy        │
│ + executar()    │                  │                  │
│                 │                  │ + executar()     │
└─────────────────┘                  └────────┬─────────┘
                                              │
                                    ┌─────────┴──────────┐
                                    ▼                    ▼
                             ┌──────────────┐  ┌──────────────┐
                             │ StrategyA    │  │ StrategyB    │
                             └──────────────┘  └──────────────┘
```

---

## 7. Quando o OCP e Mais Importante?

```
┌───────────────────────────────────────────────────────────────┐
│  OCP IMPORTA MAIS QUANDO:                                     │
│                                                               │
│  [ ] O codigo e uma biblioteca ou framework usado por outros  │
│  [ ] O comportamento muda com frequencia ou e configuravel    │
│  [ ] Ha multiplos tipos com logica similar mas distinta       │
│  [ ] Voce quer adicionar funcionalidades sem risco de regressao│
│                                                               │
│  OCP PODE SER EXAGERO QUANDO:                                 │
│                                                               │
│  [ ] O comportamento nunca vai mudar                          │
│  [ ] Ha apenas 2 casos e o sistema e pequeno                  │
│  [ ] A abstracao adicionaria mais complexidade que valor      │
└───────────────────────────────────────────────────────────────┘
```

---

## 8. OCP e o Princípio da Abstracao

O OCP nao e magico — ele funciona porque **move a variabilidade para um lugar controlado** (a abstracao). Em vez de o codigo variar por condicional, ele varia por polimorfismo.

```
SEM OCP:
  codigo + dados -> if/else cresce -> modificacao constante

COM OCP:
  abstracao (interface) -> implementacoes variam -> codigo central estavel
```

A frase de Uncle Bob resume bem:

> "Separe o que muda do que permanece igual."

---

## 9. Armadilhas Comuns

### Abstracoes Prematuras

Criar interfaces para tudo antes de saber o que vai variar e over-engineering. O OCP deve ser aplicado quando a necessidade de extensao e real ou claramente antecipavel.

> "Abstraia quando voce vê a segunda variacao, nao a primeira."

### Heranca como unico mecanismo

Heranca implementa OCP, mas heranca profunda cria acoplamento rigido. Prefira **composicao** sobre heranca para OCP.

### Enums em vez de polimorfismo

Adicionar um novo `case` em um `switch` de enum e uma modificacao — viola o OCP. Se o comportamento por tipo muda com frequencia, prefira polimorfismo.

---

## 10. Exemplos de Codigo

Os exemplos estao na pasta `exemplos/`:

| Arquivo                                                                         | Descricao                                              |
| ------------------------------------------------------------------------------- | ------------------------------------------------------ |
| [01_violando_ocp.java](./exemplos/01_violando_ocp.java)                         | Calculadora com `if/else` que cresce sem parar         |
| [02_aplicando_ocp.java](./exemplos/02_aplicando_ocp.java)                       | Mesma logica refatorada com interface e polimorfismo   |
| [03_ocp_com_interfaces.java](./exemplos/03_ocp_com_interfaces.java)             | OCP em sistema de notificacoes e relatorios            |
| [04_ocp_strategy_pattern.java](./exemplos/04_ocp_strategy_pattern.java)         | Strategy Pattern como implementacao direta do OCP      |

---

## 11. Resumo

```
┌─────────────────────────────────────────────────────────────┐
│              OPEN/CLOSED PRINCIPLE                           │
│                                                              │
│  PRINCIPIO:  Aberto para extensao, fechado para modificacao  │
│                                                              │
│  SINAL DE PROBLEMA:                                          │
│    - if/else ou switch que cresce a cada novo tipo          │
│    - Modificar codigo existente para adicionar novo caso     │
│    - Re-testar tudo ao adicionar uma funcionalidade          │
│                                                              │
│  SOLUCAO:                                                    │
│    - Defina uma interface/abstracao para o comportamento     │
│    - Crie novas implementacoes para novos comportamentos     │
│    - O codigo que usa a abstracao nunca muda                 │
│                                                              │
│  MECANISMOS EM JAVA:                                         │
│    - Interfaces + polimorfismo                               │
│    - Classes abstratas + template method                     │
│    - Composicao + injecao de dependencia                     │
│    - Strategy Pattern                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Exercicios

[EXERCICIOS.md](./EXERCICIOS.md) — 20 exercicios sobre o Open/Closed Principle.

---

*[Voltar ao inicio](../../README.md)*
