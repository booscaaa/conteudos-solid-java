# Interface Segregation Principle — ISP

> "Nenhuma classe deve ser forcada a implementar metodos que nao utiliza." — Robert C. Martin

📊 **[Abrir apresentacao de slides](https://htmlpreview.github.io/?https://github.com/booscaaa/conteudos-solid-java/blob/main/aulas/aula-04-isp/isp.html)** — navegue com as setas `←` `→` do teclado.

---

## 1. O Problema das Interfaces Gordas

### O que e uma "Fat Interface"?

Uma interface gorda (fat interface) e aquela que agrega mais metodos do que qualquer cliente ou implementacao realmente precisa. O resultado e que implementadores sao **forcados** a declarar metodos que nao fazem sentido para eles.

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│   INTERFACE GORDA:                                           │
│                                                              │
│   interface Impressora {                                     │
│       void imprimir(String doc);      <- todos precisam?     │
│       void escanear(String arquivo);  <- nem todos tem       │
│       void enviarFax(String n, ...);  <- poucos tem          │
│       void imprimirColorido(...);     <- nem todos tem       │
│       void copiar(int copias);        <- nem todos tem       │
│   }                                                          │
│                                                              │
│   ImpressoraBasica usa 1 de 5 metodos.                       │
│   Implementa 4 mentiras.                                     │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

As "mentiras" tomam tres formas:

1. `throw new UnsupportedOperationException()` — viola o LSP, quebra em runtime
2. Corpo vazio `{ /* nao faz nada */ }` — silencioso e perigoso
3. Retorno fake como `null` ou `List.of()` — enganoso, dificulta depuracao

---

## 2. O que diz o ISP?

O **Interface Segregation Principle** establece que uma interface deve ser projetada a partir da perspectiva de **quem vai usa-la**, nao de quem vai implementa-la.

### A Pergunta Certa

Em vez de perguntar _"quais metodos este objeto precisa ter?"_, pergunte:

> **"Quais metodos este CLIENTE especifico precisa usar?"**

Cada cliente recebe a interface minima que satisfaz suas necessidades — sem dependencias desnecessarias.

### Interface deve ter uma razao para mudar

Assim como o SRP afirma que uma classe deve ter uma unica razao para mudar, uma interface ISP deve mudar **apenas quando a capacidade que ela representa muda**:

```
Mudar Imprimivel  → afeta so quem imprime      ✓
Mudar Escaneavel  → afeta so quem escaneia     ✓
Mudar Impressora  → afeta TODOS os clientes    ✗
```

---

## 3. Quem Usa o Que? — A Matriz da Verdade

Antes de segregar, monte a matriz de uso. Linhas: implementacoes. Colunas: metodos.

```
                 imprimir  escanear  enviarFax  colorido  copiar
ImpressoraBasica    ✓         ✗          ✗          ✗       ✗
ImpressoraColorida  ✓         ✗          ✗          ✓       ✓
MultifuncionalEsc   ✓         ✓          ✗          ✓       ✓
ImpressoraEmpres    ✓         ✓          ✓          ✓       ✓
```

Cada coluna com pelo menos um `✗` e um candidato a interface separada.

---

## 4. A Solucao — Segregar em Interfaces Coesas

### 4.1 Impressoras Segregadas

```java
interface Imprimivel  { void imprimir(String doc); }
interface Escaneavel  { void escanear(String arquivo); }
interface Enviavel    { void enviarFax(String numero, String doc); }
interface Colorivel   { void imprimirColorido(String doc); }
interface Copiavel    { void copiar(int copias); }

// Cada classe implementa EXATAMENTE o que pode fazer:
class ImpressoraBasica      implements Imprimivel { ... }
class ImpressoraColorida    implements Imprimivel, Colorivel, Copiavel { ... }
class MultifuncionalEsc     implements Imprimivel, Escaneavel, Colorivel, Copiavel { ... }
class ImpressoraEmpresarial implements Imprimivel, Escaneavel, Enviavel, Colorivel, Copiavel { ... }
```

**Zero UnsupportedOperationException.** Nenhuma classe implementa o que nao pode fazer.

### 4.2 Protecao em Compile-Time

```java
// Funcao que so imprime — aceita qualquer Imprimivel
void imprimirDocumento(Imprimivel dispositivo, String doc) {
    dispositivo.imprimir(doc);
}

// escanearDocumento(impressoraBasica, "foto.pdf"); <- ERRO DE COMPILACAO!
// O erro e pego antes de chegar em producao.
void escanearDocumento(Escaneavel dispositivo, String arquivo) {
    dispositivo.escanear(arquivo);
}
```

---

## 5. Composicao de Interfaces

Interfaces podem herdar de outras interfaces, criando camadas de capacidade:

```java
// Interfaces base (granulares)
interface ReadRepository<T>  { T findById(Long id); List<T> findAll(); }
interface WriteRepository<T> { void save(T entity); void delete(Long id); }

// Interface composta (conveniencia)
interface FullRepository<T> extends ReadRepository<T>, WriteRepository<T> {}

// Interface especializada
interface FilterableRepository<T> extends ReadRepository<T> {
    List<T> findByFilter(String campo, Object valor);
}
```

Vantagens da composicao:

```
Repositorio somente leitura → implements ReadRepository
   Nenhum metodo de escrita. Zero excecao.

Servico de relatorio       → depende de ReadRepository
   Nao conhece save/delete. Menos acoplamento.

Repositorio completo        → implements FullRepository
   Tudo funcionando. Sem surpresas.
```

---

## 6. Sinais de Violacao do ISP

```
┌────────────────────────────────────────────────────────────────┐
│  CHECKLIST DE VIOLACAO DO ISP:                                 │
│                                                                │
│  [ ] Metodo implementado lanca UnsupportedOperationException   │
│  [ ] Metodo com corpo vazio (nao faz nada silenciosamente)     │
│  [ ] Classe usa apenas 1-2 metodos de uma interface com 10     │
│  [ ] Adicionar metodo na interface quebra N implementacoes     │
│  [ ] Interface com nomes vagos: Manager, Handler, Service      │
│  [ ] Cliente importa classe por causa de metodos que nao usa   │
│  [ ] Testes precisam mockar metodos que nao sao testados       │
└────────────────────────────────────────────────────────────────┘
```

---

## 7. ISP e os Outros Principios

O ISP se complementa com todos os outros do SOLID:

- **SRP**: uma interface coesa tem uma razao para mudar — assim como uma classe SRP. ISP e, na essencia, SRP aplicado a interfaces.

- **LSP**: interfaces pequenas facilitam implementacoes honestas. Quando a interface tem poucos metodos, e muito mais facil para uma classe implementar todos eles sem mentir. ISP e LSP caminham juntos — violar ISP geralmente gera violacoes de LSP.

- **DIP**: clientes que dependem de interfaces pequenas tem dependencias mais estaveis. Uma mudanca em `Escaneavel` nao afeta quem depende de `Imprimivel`.

- **OCP**: interfaces bem segregadas sao mais faceis de extender. Adicionar `EnviaSlack` nao requer modificar `EnviaEmail` nem suas implementacoes.

---

## 8. Exemplos de Codigo

Os exemplos estao na pasta `exemplos/`:

| Arquivo                                                                               | Descricao                                                     |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| [01_violando_isp.java](./exemplos/01_violando_isp.java)                               | Impressora gorda, Trabalhador/Robo, Repository monolitico     |
| [02_aplicando_isp.java](./exemplos/02_aplicando_isp.java)                             | Mesmos cenarios refatorados com interfaces segregadas         |
| [03_isp_interfaces.java](./exemplos/03_isp_interfaces.java)                           | Composicao de interfaces em sistemas reais                    |
| [04_isp_avancado.java](./exemplos/04_isp_avancado.java)                               | Pagamento, cache, validadores e autenticacao com ISP          |

---

## 9. Resumo

```
┌──────────────────────────────────────────────────────────────┐
│              INTERFACE SEGREGATION PRINCIPLE                  │
│                                                              │
│  PRINCIPIO: Nenhuma classe deve ser forcada a implementar    │
│             metodos que nao utiliza.                         │
│                                                              │
│  SINAL DE PROBLEMA:                                          │
│    - UnsupportedOperationException em implementacao          │
│    - Corpo vazio nos metodos                                 │
│    - Classe usa 2 de 10 metodos da interface                 │
│    - Mudar interface impacta quem nem usa o metodo mudado    │
│                                                              │
│  SOLUCAO:                                                    │
│    - Quebrar interfaces por CAPACIDADE especifica            │
│    - Projetar a interface pelo ponto de vista do CLIENTE     │
│    - Compor interfaces maiores a partir de menores           │
│    - Uma interface, uma razao para mudar                     │
│                                                              │
│  BENEFICIOS:                                                 │
│    - Erros pegos em compile-time, nao em runtime             │
│    - Menor acoplamento entre modulos                         │
│    - Mais facil de testar (mocks menores)                    │
│    - Mais facil de extender sem quebrar o existente          │
└──────────────────────────────────────────────────────────────┘
```

---

## Exercicios

[EXERCICIOS.md](./EXERCICIOS.md) — 20 exercicios sobre o Interface Segregation Principle.

---

*[Voltar ao inicio](../../README.md)*
