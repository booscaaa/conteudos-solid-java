# Exercicios — Liskov Substitution Principle

---

## Identificando Violacoes

**1.** O codigo abaixo viola o LSP? Justifique. Qual seria o comportamento inesperado ao usar `Quadrado` no lugar de `Retangulo`?

```java
class Retangulo {
    protected double largura, altura;
    public void setLargura(double l) { this.largura = l; }
    public void setAltura(double a)  { this.altura = a; }
    public double calcularArea()     { return largura * altura; }
}

class Quadrado extends Retangulo {
    @Override
    public void setLargura(double l) { this.largura = l; this.altura = l; }
    @Override
    public void setAltura(double a)  { this.altura = a; this.largura = a; }
}
```

**2.** Um desenvolvedor Junior escreveu o codigo abaixo e disse: "Ja que Pinguim nao voa, coloquei um corpo vazio — e melhor que lancar excecao." O codigo ainda viola o LSP? Por que?

```java
class Ave { public void voar() { System.out.println("voando!"); } }

class Pinguim extends Ave {
    @Override
    public void voar() { /* Pinguim nao voa, nao faco nada */ }
}
```

**3.** Identifique a violacao de LSP no codigo abaixo e explique qual propriedade (pre-condicao, pos-condicao ou invariante) foi quebrada:

```java
class Pilha<T> {
    private java.util.LinkedList<T> lista = new java.util.LinkedList<>();

    // Pre: valor != null
    // Pos: tamanho aumenta em 1
    public void empilhar(T valor) { lista.addFirst(valor); }

    // Pre: pilha nao esta vazia
    // Pos: retorna e remove o topo; tamanho diminui em 1
    public T desempilhar() {
        if (lista.isEmpty()) throw new RuntimeException("Pilha vazia");
        return lista.removeFirst();
    }
}

class PilhaLimitada<T> extends Pilha<T> {
    private final int limite;
    private int tamanho = 0;

    public PilhaLimitada(int limite) { this.limite = limite; }

    @Override
    public void empilhar(T valor) {
        if (tamanho >= limite) return; // silenciosamente descarta! Pos-condicao quebrada?
        super.empilhar(valor);
        tamanho++;
    }
}
```

**4.** O codigo abaixo usa `instanceof` para lidar com tipos diferentes. Isso indica violacao de LSP? Como reescreveria sem o `instanceof`?

```java
void processarPagamento(MetodoPagamento metodo, double valor) {
    if (metodo instanceof PagamentoCredito) {
        ((PagamentoCredito) metodo).cobrarComJuros(valor * 1.02);
    } else if (metodo instanceof PagamentoPIX) {
        ((PagamentoPIX) metodo).cobrarInstantaneo(valor);
    } else {
        metodo.cobrar(valor);
    }
}
```

**5.** Dado o cenario: "sempre que adicionamos um novo tipo de veiculo ao sistema, precisamos adicionar um `if` no metodo `calcularIPVA()` para verificar se o veiculo e isento." Isso e violacao do LSP? Do OCP? De ambos? Explique.

---

## Refatorando para o LSP

**6.** Refatore a hierarquia `Retangulo`/`Quadrado` do exercicio 1 para respeitar o LSP. Use uma interface `Forma` com o metodo `calcularArea()`. Mostre como criar um `Retangulo` e um `Quadrado` corretamente.

**7.** Refatore a hierarquia de `Ave`/`Pinguim` usando interfaces de capacidade (`AveVoadora`, `AveNadadora`). Adicione tambem `Andorinha` (voa), `Cisne` (voa e nada) e `Ema` (corre mas nao voa).

**8.** Voce tem o codigo abaixo. Refatore para que qualquer `Impressora` possa ser usada sem surpresas:

```java
class Impressora {
    public void imprimir(String documento)    { System.out.println("[IMPRIMINDO] " + documento); }
    public void imprimirColorido(String doc)  { System.out.println("[COLORIDO] " + doc); }
    public void escanear(String arquivo)      { System.out.println("[SCANNER] " + arquivo); }
    public void enviarFax(String numero, String doc) { System.out.println("[FAX -> " + numero + "] " + doc); }
}

class ImpressoraBasica extends Impressora {
    @Override
    public void imprimirColorido(String doc) { throw new UnsupportedOperationException("Sem colorido"); }
    @Override
    public void escanear(String arquivo) { throw new UnsupportedOperationException("Sem scanner"); }
    @Override
    public void enviarFax(String numero, String doc) { throw new UnsupportedOperationException("Sem fax"); }
}
```

**9.** Refatore o sistema de contas bancarias abaixo para respeitar o LSP:

```java
class ContaBancaria {
    protected double saldo;
    public void depositar(double v) { saldo += v; }
    public void sacar(double v)     { saldo -= v; }
    public double getSaldo()        { return saldo; }
}

class ContaPoupanca extends ContaBancaria {
    private int saquesNoMes = 0;
    @Override
    public void sacar(double v) {
        if (saquesNoMes >= 1) throw new RuntimeException("Limite de saques atingido");
        saldo -= v;
        saquesNoMes++;
    }
}

class ContaInvestimento extends ContaBancaria {
    @Override
    public void sacar(double v) { throw new UnsupportedOperationException("Use resgate()"); }
    public void resgatar(double v) { saldo -= v; }
}
```

**10.** Implemente uma hierarquia de `Funcionario` que respeite o LSP, com os seguintes tipos:
- `FuncionarioCLT`: recebe salario fixo mensal
- `FuncionarioFreelancer`: recebe por hora trabalhada
- `FuncionarioEstagiario`: recebe bolsa + vale-transporte
- `Socio`: retira pro-labore (pode ser zero)

A funcao `double calcularPagamento(Funcionario f)` deve funcionar para todos sem `instanceof`.

---

## Codigo para Implementar

**11.** Implemente um sistema de gerenciamento de arquivos que respeita o LSP:
- Interface `ArquivoLegivel` com `String ler()`
- Interface `ArquivoGravavel` com `void escrever(String conteudo)`
- `ArquivoTexto` implementa ambas
- `ArquivoReadOnly` implementa apenas `ArquivoLegivel`
- `StreamDeLog` implementa apenas `ArquivoGravavel`
- Mostre que nenhum metodo lanca excecao inesperada

**12.** Crie uma hierarquia de formas geometricas tridimensionais que respeita o LSP:
- Interface `Solido` com `double calcularVolume()` e `double calcularAreaSuperficie()`
- `Cubo(double lado)`
- `Esfera(double raio)`
- `Cilindro(double raio, double altura)`
- `Cone(double raio, double altura)`
- Funcao `void exibirPropriedades(Solido s)` que funciona para todos sem instanceof

**13.** Implemente um sistema de autenticacao com LSP:
- Interface `Autenticador` com `boolean autenticar(String credencial)`
- `AutenticadorSenha`: valida senha com minimo de 8 caracteres
- `AutenticadorToken`: valida token JWT (verifica se comeca com "eyJ")
- `AutenticadorBiometrico`: valida hash biometrico (verifica tamanho 64)
- `AutenticadorMultiFator`: combina dois autenticadores (ambos devem passar)
- NENHUMA implementacao pode lancar excecao — deve retornar false para credencial invalida

**14.** Crie um sistema de descontos que respeita pre-condicoes do LSP:
- Interface `CalculadorDeDesconto` com `double calcular(double valorCompra)`
- Contrato: `valorCompra >= 0` retorna desconto `>= 0` e `<= valorCompra`
- `DescontoPercentual(double percentual)`: desconto percentual simples
- `DescontoFixo(double valor)`: desconto fixo, nunca maior que o preco
- `DescontoProgressivo`: 10% ate R$100, 15% de R$100 a R$500, 20% acima
- `DescontoNenhum`: sem desconto (retorna 0)
- Funcao `void aplicarDesconto(CalculadorDeDesconto calc, double valor)` — funciona para todos

**15.** Implemente um sistema de ordenacao que respeita pos-condicoes do LSP:
- Interface `Ordenador<T>` com `List<T> ordenar(List<T> lista)`
- Contrato pos-condicao: lista retornada tem o MESMO numero de elementos
- `OrdenadorCrescente<T extends Comparable<T>>`
- `OrdenadorDecrescente<T extends Comparable<T>>`
- `OrdenadorAleatorio<T>`: embaralha — ainda respeita a pos-condicao de tamanho
- `OrdenadorPorTamanhoString`: ordena strings por comprimento
- Funcao `void exibirOrdenado(Ordenador<String> ord, List<String> itens)`

---

## Questoes de Reflexao

**16.** Por que "Quadrado e um Retangulo" no mundo real nao significa que `Quadrado` deve herdar de `Retangulo` no codigo? Qual e a diferenca entre o "e-um" do mundo real e o "e-um" de comportamento no OOP?

**17.** Um colega argumenta: "LSP e muito academico. Na pratica, basta documentar que Pinguim nao voa e os devs saberao usar corretamente." Quais sao os riscos concretos de nao seguir o LSP em um sistema com equipe grande?

**18.** Qual e a relacao entre o LSP e a facilidade de escrever testes unitarios? Como uma violacao de LSP dificulta o isolamento em testes?

**19.** Existe uma tensao entre LSP e heranca em Java. Quando voce preferiria composicao sobre heranca para evitar violacoes de LSP? Dê um exemplo pratico.

**20.** O LSP afeta a confiabilidade de sistemas em producao. Descreva um cenario real onde uma violacao silenciosa do LSP (sem excecao, apenas resultado errado) poderia causar um bug grave em producao.

---

*[Voltar ao conteudo](./README.md)*
