# Exercicios — Open/Closed Principle

---

## Identificando Violacoes

**1.** O codigo abaixo viola o OCP? Justifique. Se sim, qual seria o sinal mais obvio da violacao?

```java
class AreaCalculator {
    public double calcular(Object forma) {
        if (forma instanceof Circulo) {
            Circulo c = (Circulo) forma;
            return Math.PI * c.getRaio() * c.getRaio();
        } else if (forma instanceof Retangulo) {
            Retangulo r = (Retangulo) forma;
            return r.getLargura() * r.getAltura();
        } else if (forma instanceof Triangulo) {
            Triangulo t = (Triangulo) forma;
            return (t.getBase() * t.getAltura()) / 2;
        }
        return 0;
    }
}
```

**2.** Um desenvolvedor argumenta: "Se eu usar `enum` em vez de `String` para o tipo, o codigo fica mais seguro e nao viola o OCP." Ele esta certo? Por que?

```java
enum TipoImposto { NACIONAL, ESTADUAL, MUNICIPAL, ISENTO }

class CalculadoraDeImposto {
    public double calcular(TipoImposto tipo, double valor) {
        switch (tipo) {
            case NACIONAL:  return valor * 0.20;
            case ESTADUAL:  return valor * 0.12;
            case MUNICIPAL: return valor * 0.05;
            case ISENTO:    return 0;
            default:        return 0;
        }
    }
}
```

**3.** Identifique TODAS as violacoes do OCP no codigo abaixo e explique cada uma:

```java
class Autenticador {
    public boolean autenticar(String tipo, String credencial, String segredo) {
        if (tipo.equals("USUARIO_SENHA")) {
            return segredo.equals("senha_correta");
        } else if (tipo.equals("TOKEN_JWT")) {
            return credencial.startsWith("eyJ");
        } else if (tipo.equals("API_KEY")) {
            return credencial.length() == 32;
        } else if (tipo.equals("OAUTH")) {
            return credencial.contains("access_token");
        }
        return false;
    }
}
```

**4.** O codigo a seguir usa heranca para implementar OCP. Funciona, mas tem um problema. Qual e?

```java
abstract class Exportador {
    public abstract String exportar(String dados);
}

class ExportadorCSV extends Exportador {
    public String exportar(String dados) { return dados.replace(" ", ","); }
}

class ExportadorJSON extends Exportador {
    // Mas e se precisarmos adicionar validacao antes de exportar?
    // Teriamos que modificar a classe base Exportador, quebrando o OCP.
    public String exportar(String dados) { return "{\"data\": \"" + dados + "\"}"; }
}
```

**5.** Dado o cenario: "toda vez que um novo produto e adicionado ao catalogo, precisamos editar o metodo `calcularFrete()` da classe `CarrinhoDeCompras`." Isso e uma violacao do OCP? Como corrigi-la?

---

## Refatorando para o OCP

**6.** Refatore a classe `AreaCalculator` do exercicio 1 para seguir o OCP. Use uma interface `Forma` com o metodo `calcularArea()`.

**7.** Refatore a `CalculadoraDeImposto` do exercicio 2 para seguir o OCP, mesmo mantendo o enum. Dica: use polimorfismo no proprio enum com metodo abstrato.

**8.** Refatore o `Autenticador` do exercicio 3 usando uma interface `EstrategiaDeAutenticacao`. Crie pelo menos 3 implementacoes.

**9.** Voce tem o seguinte sistema de relatorio:

```java
class GeradorDeRelatorio {
    public void gerar(String tipo, List<String> dados) {
        if (tipo.equals("PDF")) {
            System.out.println("[PDF] Gerando relatorio PDF...");
            // logica de PDF
        } else if (tipo.equals("EXCEL")) {
            System.out.println("[EXCEL] Gerando planilha...");
            // logica de Excel
        }
    }
}
```

Refatore para que adicionar "HTML" e "Markdown" nao exija modificar `GeradorDeRelatorio`.

**10.** Implemente um sistema de ordenacao que segue o OCP. Crie uma interface `EstrategiaDeOrdenacao<T>` e implemente pelo menos: ordenacao crescente, decrescente e por comprimento de string.

---

## Codigo para Implementar

**11.** Implemente um sistema de compressao de arquivos seguindo o OCP:
- Interface `CompressorDeArquivo` com metodo `comprimir(String conteudo): String`
- `CompressorZip`: simule com prefixo `[ZIP]`
- `CompressorGzip`: simule com prefixo `[GZIP]`
- `CompressorSemCompressao`: retorna o conteudo sem alteracao
- `ServicoDeCompressao`: usa a interface, nunca muda ao adicionar novos compressores

**12.** Crie um sistema de calculo de salario para diferentes tipos de contrato:
- Interface `Contrato` com `calcularSalario(int horasTrabalhadas): double`
- `ContratoCLT`: salario fixo dividido por 220h, com hora extra a 150%
- `ContratoFreelancer`: valor fixo por hora
- `ContratoEstagio`: valor por hora com limite de 30h semanais
- `ContratoSocio`: salario base mais percentual sobre lucro (recebido no construtor)

**13.** Implemente um pipeline de processamento de texto seguindo o OCP:
- Interface `Transformacao` com metodo `aplicar(String texto): String`
- `TransformacaoMaiusculas`
- `TransformacaoRemoverEspacos`
- `TransformacaoRemoverAcentos` (simplificado: substitua a -> a, e -> e etc)
- `PipelineDeTexto`: recebe lista de `Transformacao` e aplica todas em sequencia

**14.** Crie um sistema de validacao de formulario com OCP:
- Interface `Validador<T>` com metodo `validar(T valor): boolean` e `getMensagemErro(): String`
- `ValidadorEmailFormato`: verifica se contem "@"
- `ValidadorCPFTamanho`: verifica se tem 11 digitos
- `ValidadorSenhaTamanhoMinimo`: verifica minimo de 8 caracteres
- `ValidadorSenhaCaractereEspecial`: verifica se contem pelo menos um de: `!@#$%`
- `CampoFormulario<T>`: recebe uma lista de validadores e valida o valor contra todos

**15.** Implemente um sistema de log com OCP:
- Interface `DestinoDeLog` com metodo `registrar(String nivel, String mensagem)`
- `LogConsole`: exibe no console com cor simulada (`[INFO]`, `[ERRO]`, `[WARN]`)
- `LogArquivo`: simula escrita em arquivo com `[ARQUIVO]`
- `LogServidor`: simula envio para servidor remoto com `[HTTP POST]`
- `Logger`: aceita lista de destinos e replica o log em todos eles

---

## Questoes de Reflexao

**16.** Robert Martin diz: "Abstraia quando voce ve a segunda variacao, nao a primeira." O que isso significa na pratica? Dê um exemplo de quando voce esperaria antes de criar a abstracao.

**17.** Qual e a relacao entre o OCP e a facilidade de escrever testes unitarios? Explique com um exemplo concreto.

**18.** Heranca tambem implementa OCP. Mas Uncle Bob prefere composicao sobre heranca para OCP. Por que a composicao e geralmente mais flexivel?

**19.** Existe uma tensao entre "nao se repetir" (DRY) e o OCP. Quando duplicar codigo pode ser preferivel a criar uma abstracao prematura?

**20.** O OCP faz sentido para configuracao de sistema (ex: parametros em `application.properties`)? Ou o principio se aplica apenas a codigo? Discuta.

---

*[Voltar ao conteudo](./README.md)*
