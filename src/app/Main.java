package app;

import dao.JogadorDAO;
import dao.JogadorDAOJdbc;
import ml.kmeans.ExperimentoKmeans;
import ml.knn.ExperimentoKNN;
import model.Jogador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String URL_SERVIDOR = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String URL_BANCO = "jdbc:mysql://localhost:3306/jogo_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final int QUANTIDADE_ESPERADA_JOGADORES = 150;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   PROJETO MACHINE LEARNING COM POO");
        System.out.println("==================================================\n");

        Scanner scanner = new Scanner(System.in);

        System.out.print("Usuário do MySQL (pressione Enter para 'root'): ");
        String usuario = scanner.nextLine().trim();
        if (usuario.isEmpty()) {
            usuario = "root";
        }

        System.out.print("Senha do MySQL: ");
        String senha = scanner.nextLine();
        System.out.println();

        if (!prepararBancoDeDados(usuario, senha)) {
            scanner.close();
            return;
        }

        List<Jogador> jogadores = carregarJogadoresComDAO(usuario, senha);
        scanner.close();

        if (jogadores.isEmpty()) {
            System.err.println("Execução interrompida: nenhum jogador encontrado na tabela.");
            return;
        }

        if (jogadores.size() < 2) {
            System.err.println("Execução interrompida: é necessário pelo menos dois jogadores para realizar a divisão treino/teste.");
            return;
        }

        System.out.println("**************************************************");
        System.out.println("        PARTE A - BANCO DE DADOS E SQL");
        System.out.println("**************************************************");
        System.out.println("Conferência de registros na tabela 'jogador': " + jogadores.size() + " linhas"
                + (jogadores.size() == 150 ? " (OK - 150 linhas confirmadas!)" : " (ATENÇÃO: esperado 150 linhas!)") + "\n");

        executarConsultasRelatorio(usuario, senha);

        System.out.println("\n**************************************************");
        System.out.println("                  PARTE B - KNN                   ");
        System.out.println("**************************************************");
        ExperimentoKNN.executar(jogadores);

        System.out.println("\n**************************************************");
        System.out.println("                PARTE C - K-MEANS                 ");
        System.out.println("**************************************************");
        ExperimentoKmeans.executar(jogadores);

        System.out.println("\nExecução concluída com sucesso!");
    }

    private static List<Jogador> carregarJogadoresComDAO(String usuario, String senha) {
        try (Connection conn = DriverManager.getConnection(URL_BANCO, usuario, senha)) {
            JogadorDAO dao = new JogadorDAOJdbc(conn);
            return dao.listarTodos();
        } catch (SQLException | RuntimeException e) {
            System.err.println("Erro ao carregar jogadores via DAO: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static boolean prepararBancoDeDados(String usuario, String senha) {
        try (Connection conn = DriverManager.getConnection(URL_SERVIDOR, usuario, senha);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS jogo_db");
            stmt.execute("USE jogo_db");

            boolean precisaImportar = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM jogador")) {
                if (rs.next() && rs.getInt(1) != QUANTIDADE_ESPERADA_JOGADORES) {
                    precisaImportar = true;
                }
            } catch (SQLException e) {
                precisaImportar = true;
            }

            if (precisaImportar) {
                Path caminhoSql = localizarArquivoSql();
                if (caminhoSql == null) {
                    System.err.println("Arquivo 'jogadores.sql' não encontrado na raiz do projeto, em 'src/' nem em 'sql/'.");
                    return false;
                }

                System.out.println("Importando dados de '" + caminhoSql + "' para o banco jogo_db...");
                String conteudo = Files.readString(caminhoSql, StandardCharsets.UTF_8);

                StringBuilder sqlLimpo = new StringBuilder();
                for (String linha : conteudo.split("\\R")) {
                    String trimmed = linha.trim();
                    if (!trimmed.startsWith("--") && !trimmed.isEmpty()) {
                        sqlLimpo.append(linha).append("\n");
                    }
                }

                for (String comando : sqlLimpo.toString().split(";")) {
                    String cmd = comando.trim();
                    if (!cmd.isEmpty()) {
                        stmt.execute(cmd);
                    }
                }
                System.out.println("Banco 'jogo_db' criado e populado com sucesso!\n");
            } else {
                System.out.println("Conectado ao banco 'jogo_db' existente!\n");
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Falha na conexão com o MySQL: " + e.getMessage());
            System.err.println("Verifique se o serviço do MySQL está ativo e se o usuário/senha estão corretos.");
            return false;
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo jogadores.sql: " + e.getMessage());
            return false;
        }
    }

    private static Path localizarArquivoSql() {
        Path naRaiz = Paths.get("jogadores.sql");
        if (Files.exists(naRaiz)) return naRaiz;

        Path naSrc = Paths.get("src", "jogadores.sql");
        if (Files.exists(naSrc)) return naSrc;

        Path naSql = Paths.get("sql", "jogadores.sql");
        if (Files.exists(naSql)) return naSql;

        return null;
    }

    private static void executarConsultasRelatorio(String usuario, String senha) {
        String sqlA = "SELECT perfil, COUNT(*) AS total_jogadores FROM jogador GROUP BY perfil ORDER BY total_jogadores DESC";

        String sqlB = "SELECT perfil, "
                + "AVG(horas_semana) AS media_horas, "
                + "AVG(partidas_semana) AS media_partidas, "
                + "AVG(gasto_mensal) AS media_gasto, "
                + "AVG(taxa_vitoria) AS media_taxa_vitoria "
                + "FROM jogador GROUP BY perfil ORDER BY perfil";

        String sqlC = "SELECT apelido, perfil, horas_semana, taxa_vitoria "
                + "FROM jogador "
                + "WHERE horas_semana > 20 AND taxa_vitoria > 55 "
                + "ORDER BY taxa_vitoria DESC";

        try (Connection conn = DriverManager.getConnection(URL_BANCO, usuario, senha);
             Statement stmt = conn.createStatement()) {

            System.out.println("--- Consulta (a): Quantidade de pessoas jogadoras por perfil ---");
            try (ResultSet rs = stmt.executeQuery(sqlA)) {
                System.out.printf("%-15s | %-10s%n", "PERFIL", "QUANTIDADE");
                System.out.println("----------------------------");
                while (rs.next()) {
                    System.out.printf("%-15s | %-10d%n",
                            rs.getString("perfil"),
                            rs.getInt("total_jogadores"));
                }
            }
            System.out.println();

            System.out.println("--- Consulta (b): Média de cada característica por perfil (GROUP BY) ---");
            try (ResultSet rs = stmt.executeQuery(sqlB)) {
                System.out.printf("%-15s | %-12s | %-15s | %-13s | %-15s%n",
                        "PERFIL", "HORAS/SEM", "PARTIDAS/SEM", "GASTO MENSAL", "TAXA VITÓRIA");
                System.out.println("--------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-15s | %-12.2f | %-15.2f | %-13.2f | %-15.2f%n",
                            rs.getString("perfil"),
                            rs.getDouble("media_horas"),
                            rs.getDouble("media_partidas"),
                            rs.getDouble("media_gasto"),
                            rs.getDouble("media_taxa_vitoria"));
                }
            }
            System.out.println();

            System.out.println("--- Consulta (c): Jogadores com > 20h semanais e taxa de vitória > 55% ---");
            try (ResultSet rs = stmt.executeQuery(sqlC)) {
                System.out.printf("%-20s | %-15s | %-10s | %-12s%n",
                        "APELIDO", "PERFIL", "HORAS/SEM", "TAXA VITÓRIA");
                System.out.println("------------------------------------------------------------------");
                int encontrados = 0;
                while (rs.next()) {
                    encontrados++;
                    System.out.printf("%-20s | %-15s | %-10.2f | %-12.2f%n",
                            rs.getString("apelido"),
                            rs.getString("perfil"),
                            rs.getDouble("horas_semana"),
                            rs.getDouble("taxa_vitoria"));
                }
                if (encontrados == 0) {
                    System.out.println("Nenhum jogador atendeu aos critérios.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao executar consultas da Parte A: " + e.getMessage());
        }
    }
}