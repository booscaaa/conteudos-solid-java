/**
 * EXEMPLO 02 — Aplicando o SRP
 *
 * A mesma logica do exemplo anterior, agora com responsabilidades separadas:
 *
 *   Funcionario          -> representa os dados (modelo de dominio)
 *   CalculadoraDeSalario -> calcula o salario liquido (regra de negocio)
 *   FuncionarioRepository-> persiste no banco (infraestrutura)
 *   RelatorioService     -> gera relatorio (apresentacao)
 *   NotificacaoService   -> envia emails (comunicacao)
 *
 * Cada classe tem UMA razao para mudar. Mudancas ficam isoladas.
 * Cada classe pode ser testada de forma independente.
 */

// -----------------------------------------------------------------------
// MODELO DE DOMINIO — representa apenas os dados do funcionario
// Razao para mudar: se o modelo de funcionario mudar (campos, validacoes)
// -----------------------------------------------------------------------
class Funcionario {

    private final String nome;
    private final String email;
    private final double salarioBruto;
    private final String cargo;

    public Funcionario(String nome, String email, double salarioBruto, String cargo) {
        this.nome = nome;
        this.email = email;
        this.salarioBruto = salarioBruto;
        this.cargo = cargo;
    }

    public String getNome()          { return nome; }
    public String getEmail()         { return email; }
    public double getSalarioBruto()  { return salarioBruto; }
    public String getCargo()         { return cargo; }
}

// -----------------------------------------------------------------------
// REGRA DE NEGOCIO — calcula o salario liquido
// Razao para mudar: se as aliquotas de INSS ou IR mudarem
// -----------------------------------------------------------------------
class CalculadoraDeSalario {

    private static final double ALIQUOTA_INSS = 0.11;
    private static final double ALIQUOTA_IR   = 0.15;
    private static final double PISO_IR        = 3000.0;

    public double calcularLiquido(Funcionario funcionario) {
        double bruto = funcionario.getSalarioBruto();
        double inss  = bruto * ALIQUOTA_INSS;
        double ir    = bruto > PISO_IR ? bruto * ALIQUOTA_IR : 0;
        return bruto - inss - ir;
    }
}

// -----------------------------------------------------------------------
// INFRAESTRUTURA — persistencia no banco de dados
// Razao para mudar: se o banco ou ORM for trocado
// -----------------------------------------------------------------------
class FuncionarioRepository {

    public void salvar(Funcionario funcionario) {
        // Aqui ficaria a logica de conexao, SQL, ORM...
        System.out.println("[DB] Salvando funcionario: " + funcionario.getNome());
    }

    public Funcionario buscarPorNome(String nome) {
        // Logica de consulta...
        System.out.println("[DB] Buscando: " + nome);
        return null; // simulacao
    }
}

// -----------------------------------------------------------------------
// APRESENTACAO — geracao de relatorio
// Razao para mudar: se o formato ou conteudo do relatorio mudar
// -----------------------------------------------------------------------
class RelatorioService {

    private final CalculadoraDeSalario calculadora;

    public RelatorioService(CalculadoraDeSalario calculadora) {
        this.calculadora = calculadora;
    }

    public String gerarRelatorio(Funcionario funcionario) {
        double liquido = calculadora.calcularLiquido(funcionario);
        return "=== RELATORIO DE FUNCIONARIO ===\n"
            + "Nome:           " + funcionario.getNome() + "\n"
            + "Cargo:          " + funcionario.getCargo() + "\n"
            + "Salario Bruto:  R$ " + String.format("%.2f", funcionario.getSalarioBruto()) + "\n"
            + "Salario Liquido:R$ " + String.format("%.2f", liquido) + "\n"
            + "================================";
    }
}

// -----------------------------------------------------------------------
// COMUNICACAO — envio de notificacoes
// Razao para mudar: se o template ou provedor de email mudar
// -----------------------------------------------------------------------
class NotificacaoService {

    private final CalculadoraDeSalario calculadora;

    public NotificacaoService(CalculadoraDeSalario calculadora) {
        this.calculadora = calculadora;
    }

    public void enviarHolerite(Funcionario funcionario) {
        double liquido = calculadora.calcularLiquido(funcionario);
        String corpo   = "Ola " + funcionario.getNome() + ",\n\n"
            + "Seu salario liquido deste mes e: R$ "
            + String.format("%.2f", liquido) + "\n\n"
            + "Atenciosamente,\nRH";

        // Aqui ficaria a chamada real ao servidor de email
        System.out.println("[EMAIL] Para: " + funcionario.getEmail());
        System.out.println("[EMAIL] Assunto: Holerite - " + funcionario.getCargo());
        System.out.println("[EMAIL] Corpo:\n" + corpo);
    }
}

// -----------------------------------------------------------------------
// DEMONSTRACAO — cada classe usada de forma independente
// -----------------------------------------------------------------------
public class AplicandoSRP {

    public static void main(String[] args) {
        // Criando o funcionario (modelo)
        Funcionario funcionario = new Funcionario(
            "Ana Oliveira",
            "ana@empresa.com",
            5000.00,
            "Engenheira de Software"
        );

        // Dependencias
        CalculadoraDeSalario calculadora   = new CalculadoraDeSalario();
        FuncionarioRepository repository  = new FuncionarioRepository();
        RelatorioService relatorio        = new RelatorioService(calculadora);
        NotificacaoService notificacao    = new NotificacaoService(calculadora);

        // Cada servico faz apenas o que e seu
        repository.salvar(funcionario);
        System.out.println();

        System.out.println(relatorio.gerarRelatorio(funcionario));
        System.out.println();

        notificacao.enviarHolerite(funcionario);

        /*
         * VANTAGENS AGORA:
         *
         * 1. Teste unitario de CalculadoraDeSalario: sem banco, sem email.
         *    So instancia a calculadora e testa os calculos.
         *
         * 2. Troca de banco (PostgreSQL -> MongoDB): muda apenas FuncionarioRepository.
         *    Funcionario, CalculadoraDeSalario e NotificacaoService nem sabem.
         *
         * 3. Novo canal de notificacao (SMS): cria SmsNotificacaoService.
         *    Nada muda nas outras classes.
         *
         * 4. Cada time cuida do seu arquivo:
         *    RH         -> CalculadoraDeSalario
         *    DBA        -> FuncionarioRepository
         *    Front-end  -> RelatorioService
         *    Infra      -> NotificacaoService
         */
    }
}
