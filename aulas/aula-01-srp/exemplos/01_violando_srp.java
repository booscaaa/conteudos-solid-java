/**
 * EXEMPLO 01 — Violando o SRP
 *
 * Neste exemplo, a classe Funcionario acumula multiplas responsabilidades:
 *   1. Representar os dados do funcionario (modelo de dados)
 *   2. Calcular o salario liquido (regra de negocio)
 *   3. Persistir no banco de dados (infraestrutura)
 *   4. Gerar relatorio em texto (apresentacao)
 *   5. Enviar notificacao por email (comunicacao)
 *
 * Cada uma dessas responsabilidades tem razoes DIFERENTES para mudar:
 *   - Regra de calculo de INSS muda  -> afeta calcularSalarioLiquido()
 *   - Banco de dados e trocado       -> afeta salvarNoBanco()
 *   - Formato do relatorio muda      -> afeta gerarRelatorio()
 *   - Provedor de email muda         -> afeta enviarNotificacao()
 *
 * PROBLEMA: uma mudanca em qualquer dessas areas afeta a classe inteira,
 * aumentando o risco de bugs e dificultando os testes unitarios.
 */

// Simulacoes de dependencias externas (para o exemplo compilar)
class BancoDeDados {
    public static void salvar(Object obj) {
        System.out.println("[DB] Salvando: " + obj);
    }
}

class ServidorEmail {
    public static void enviar(String destino, String assunto, String corpo) {
        System.out.println("[EMAIL] Para: " + destino + " | Assunto: " + assunto);
    }
}

// -----------------------------------------------------------------------
// CLASSE COM MULTIPLAS RESPONSABILIDADES — VIOLACAO DO SRP
// -----------------------------------------------------------------------
class Funcionario {

    private String nome;
    private String email;
    private double salarioBruto;
    private String cargo;

    public Funcionario(String nome, String email, double salarioBruto, String cargo) {
        this.nome = nome;
        this.email = email;
        this.salarioBruto = salarioBruto;
        this.cargo = cargo;
    }

    // ---------------------------------------------------------------
    // RESPONSABILIDADE 1: Regra de negocio — calcular salario
    // Razao para mudar: se a aliquota do INSS ou IR mudar
    // ---------------------------------------------------------------
    public double calcularSalarioLiquido() {
        double inss = salarioBruto * 0.11;
        double ir = salarioBruto > 3000 ? salarioBruto * 0.15 : 0;
        return salarioBruto - inss - ir;
    }

    // ---------------------------------------------------------------
    // RESPONSABILIDADE 2: Infraestrutura — persistencia no banco
    // Razao para mudar: se o banco de dados ou ORM for trocado
    // ---------------------------------------------------------------
    public void salvarNoBanco() {
        // Logica de conexao, mapeamento, SQL...
        BancoDeDados.salvar(this);
        System.out.println("Funcionario " + nome + " salvo no banco.");
    }

    public Funcionario buscarPorNome(String nome) {
        // Logica de consulta ao banco...
        System.out.println("[DB] Buscando funcionario: " + nome);
        return null; // simulacao
    }

    // ---------------------------------------------------------------
    // RESPONSABILIDADE 3: Apresentacao — gerar relatorio
    // Razao para mudar: se o formato ou conteudo do relatorio mudar
    // ---------------------------------------------------------------
    public String gerarRelatorio() {
        return "=== RELATORIO DE FUNCIONARIO ===\n"
            + "Nome:           " + nome + "\n"
            + "Cargo:          " + cargo + "\n"
            + "Salario Bruto:  R$ " + String.format("%.2f", salarioBruto) + "\n"
            + "Salario Liquido:R$ " + String.format("%.2f", calcularSalarioLiquido()) + "\n"
            + "================================";
    }

    // ---------------------------------------------------------------
    // RESPONSABILIDADE 4: Comunicacao — envio de email
    // Razao para mudar: se o template ou provedor de email mudar
    // ---------------------------------------------------------------
    public void enviarNotificacaoDeSalario() {
        String corpo = "Ola " + nome + ",\n\n"
            + "Seu salario liquido deste mes e: R$ "
            + String.format("%.2f", calcularSalarioLiquido()) + "\n\n"
            + "Atenciosamente,\nRH";
        ServidorEmail.enviar(email, "Seu holerite - " + cargo, corpo);
    }

    // Getters
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public double getSalarioBruto() { return salarioBruto; }
    public String getCargo() { return cargo; }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO DO PROBLEMA
// -----------------------------------------------------------------------
public class ViolandoSRP {

    public static void main(String[] args) {
        Funcionario funcionario = new Funcionario(
            "Ana Oliveira",
            "ana@empresa.com",
            5000.00,
            "Engenheira de Software"
        );

        // Funciona, mas a classe faz DEMAIS
        System.out.println(funcionario.gerarRelatorio());
        System.out.println();

        funcionario.salvarNoBanco();
        System.out.println();

        funcionario.enviarNotificacaoDeSalario();

        /*
         * PERGUNTAS PARA REFLEXAO:
         *
         * 1. Como testar apenas o calculo de salario sem configurar um banco?
         *    -> Impossivel — salvarNoBanco() esta na mesma classe.
         *
         * 2. Se trocarmos o banco de PostgreSQL para MongoDB, o que muda?
         *    -> A classe Funcionario — que nao deveria saber nada sobre banco!
         *
         * 3. Se adicionarmos suporte a notificacao por SMS alem de email?
         *    -> Mais um metodo em Funcionario — que nao deveria saber sobre comunicacao!
         *
         * 4. Quantas pessoas/times diferentes podem precisar alterar essa classe?
         *    -> Time de RH (regras), DBA (banco), Front-end (relatorio), Infra (email)
         *       Todos mexem no mesmo arquivo. Conflitos garantidos.
         */
    }
}
