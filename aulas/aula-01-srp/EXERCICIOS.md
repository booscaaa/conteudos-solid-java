# Exercicios — Single Responsibility Principle

---

## Identificando Violacoes

**1.** Analise a classe abaixo e liste todas as responsabilidades que ela acumula. Quantas razoes ela tem para mudar?

```java
class Relatorio {
    private List<Venda> vendas;

    public double calcularTotalVendas() { ... }
    public double calcularImpostos() { ... }
    public String gerarHTML() { ... }
    public String gerarPDF() { ... }
    public void salvarEmArquivo(String caminho) { ... }
    public void enviarPorEmail(String destino) { ... }
    public void publicarNaAPI(String url) { ... }
}
```

**2.** Explique com suas palavras: qual e a diferenca entre uma classe ter "muitos metodos" e uma classe ter "multiplas responsabilidades"? Dê um exemplo de cada.

**3.** A classe `StringUtils` abaixo viola o SRP? Justifique.

```java
class StringUtils {
    public static String maiusculas(String s) { return s.toUpperCase(); }
    public static String minusculas(String s) { return s.toLowerCase(); }
    public static String removerEspacos(String s) { return s.trim(); }
    public static boolean eVazia(String s) { return s == null || s.isBlank(); }
    public static String inverter(String s) { return new StringBuilder(s).reverse().toString(); }
}
```

**4.** Leia o nome das classes abaixo e aponte quais provavelmente violam o SRP so pelo nome:
- `UsuarioRepository`
- `UserManagerAndEmailHelper`
- `PedidoService`
- `DataConverterAndValidator`
- `AuthenticatorAndLogger`
- `ContaCorrente`

**5.** Uma equipe argumenta: "Nossa classe `AppController` tem 2000 linhas, mas e justificavel porque e o controller principal do sistema." Voce concorda? Argumente.

---

## Refatorando para o SRP

**6.** Refatore a classe `Funcionario` abaixo separando suas responsabilidades em classes distintas. Nomeie cada nova classe de forma clara.

```java
class Funcionario {
    private String nome;
    private double salario;
    private String departamento;

    public double calcularBonus() {
        return salario * 0.10;
    }

    public void registrarPonto(LocalDateTime horario) {
        System.out.println("Ponto registrado: " + horario);
    }

    public void salvarNoBanco() {
        System.out.println("Salvando " + nome + " no banco...");
    }

    public String gerarCracha() {
        return "| " + nome + " | " + departamento + " |";
    }
}
```

**7.** Refatore o metodo abaixo aplicando SRP no nivel de metodos. Extraia metodos menores com uma unica responsabilidade cada.

```java
public void cadastrarCliente(String nome, String email, String cpf) {
    // Valida CPF
    if (cpf.length() != 11) throw new IllegalArgumentException("CPF invalido");
    if (!cpf.matches("\\d+")) throw new IllegalArgumentException("CPF deve conter so numeros");

    // Formata nome
    String nomeFormatado = nome.trim();
    nomeFormatado = nomeFormatado.substring(0, 1).toUpperCase() + nomeFormatado.substring(1).toLowerCase();

    // Salva no banco
    System.out.println("INSERT INTO clientes VALUES ('" + nomeFormatado + "', '" + email + "', '" + cpf + "')");

    // Envia email de boas vindas
    System.out.println("Enviando email para: " + email);
}
```

**8.** Voce tem uma classe `Autenticacao` que valida login, gera token JWT e registra logs de acesso. Proponha a separacao em classes com SRP. Defina o nome e a responsabilidade de cada uma.

**9.** Refatore a classe `Produto` abaixo. Identifique o que pertence ao dominio do produto e o que e responsabilidade de outra classe.

```java
class Produto {
    private String nome;
    private double preco;
    private int quantidadeEstoque;

    public boolean temEstoque(int quantidade) {
        return quantidadeEstoque >= quantidade;
    }

    public void aplicarDesconto(double percentual) {
        this.preco = preco - (preco * percentual / 100);
    }

    public void salvar() {
        System.out.println("[DB] Produto salvo: " + nome);
    }

    public void atualizarPrecoNaVitrine(double novoPreco) {
        System.out.println("[API] Atualizando preco na vitrine: " + novoPreco);
    }

    public String exportarParaCSV() {
        return nome + "," + preco + "," + quantidadeEstoque;
    }
}
```

**10.** Dado o seguinte cenario: "O sistema envia emails de confirmacao de compra, de recuperacao de senha e de atualizacoes de status do pedido." Como voce organizaria as classes de envio de email seguindo o SRP? Descreva a estrutura.

---

## Codigo para Implementar

**11.** Implemente uma classe `Temperatura` que representa apenas um valor de temperatura em Celsius. Em seguida, crie uma classe `TemperaturaConverter` que converte entre Celsius, Fahrenheit e Kelvin.

**12.** Crie um sistema simples de biblioteca com as seguintes responsabilidades separadas:
- `Livro`: modelo com titulo, autor e ISBN
- `LivroRepository`: salva e busca livros (simule com `System.out.println`)
- `LivroValidator`: valida se ISBN tem 13 digitos e se titulo nao e vazio
- `LivroService`: orquestra validacao e persistencia

**13.** Implemente uma classe `Senha` que encapsula apenas a senha em texto puro. Depois crie `SenhaHasher` que gera o hash (pode usar `String.hashCode()` para simular) e `SenhaValidator` que verifica se a senha tem minimo de 8 caracteres, uma letra maiuscula e um numero.

**14.** Crie uma classe `Nota` (de aluno) com os campos `disciplina`, `valor` e `aluno`. Depois crie:
- `NotaCalculadora`: calcula media de uma lista de notas
- `NotaRelatorio`: gera relatorio em texto com todas as notas e a media
- `NotaRepository`: simula persistencia

**15.** Refatore o codigo abaixo para seguir o SRP. Crie quantas classes forem necessarias.

```java
class SistemaDeLogin {
    public String autenticar(String usuario, String senha) {
        // Verifica no banco
        System.out.println("[DB] SELECT * FROM usuarios WHERE usuario = '" + usuario + "'");
        boolean encontrado = true; // simulacao

        if (!encontrado) throw new RuntimeException("Usuario nao encontrado");

        // Verifica senha (hash simulado)
        boolean senhaCorreta = senha.hashCode() == senha.hashCode();
        if (!senhaCorreta) throw new RuntimeException("Senha incorreta");

        // Gera token
        String token = usuario + "_" + System.currentTimeMillis();

        // Registra log
        System.out.println("[LOG] Login realizado: " + usuario + " em " + new java.util.Date());

        // Envia email de aviso
        System.out.println("[EMAIL] Novo acesso detectado para: " + usuario);

        return token;
    }
}
```

---

## Questoes de Reflexao

**16.** O SRP diz "uma razao para mudar". Quem define o que e uma "razao"? O negocio? A tecnologia? O time? Discuta.

**17.** Existe um limite de quantos metodos uma classe pode ter e ainda seguir o SRP? Justifique com um exemplo.

**18.** Um desenvolvedor diz: "Se eu separar tudo em classes pequenas, vou ter centenas de arquivos e vai ficar impossivel de navegar no projeto." Como voce responderia a essa critica?

**19.** Em quais situacoes pode ser aceitavel violar o SRP conscientemente? Dê um exemplo pratico.

**20.** Compare: uma classe com SRP bem aplicado e uma funcao pura em programacao funcional. O que elas tem em comum no que se refere a responsabilidade e previsibilidade?

---

*[Voltar ao conteudo](./README.md)*
