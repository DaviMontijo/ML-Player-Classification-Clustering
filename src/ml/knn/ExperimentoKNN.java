package ml.knn;

import ml.DistanciaDeManhattan;
import ml.DistanciaEuclidiana;
import ml.MedidaDeDistancia;
import ml.Normalizador;
import model.Jogador;
import model.Perfil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExperimentoKNN {

    public static void executar(List<Jogador> todosJogadores) {
        if (todosJogadores == null || todosJogadores.size() < 5) {
            throw new IllegalArgumentException("Lista de jogadores insuficiente para o experimento.");
        }

        List<Jogador> copia = new ArrayList<>(todosJogadores);
        Collections.shuffle(copia, new Random(42L));

        int pontoCorte = (int) (copia.size() * 0.8);
        List<Jogador> treino = new ArrayList<>(copia.subList(0, pontoCorte));
        List<Jogador> teste = new ArrayList<>(copia.subList(pontoCorte, copia.size()));

        Normalizador normalizador = new Normalizador();
        normalizador.ajustar(treino);

        MedidaDeDistancia euclidiana = new DistanciaEuclidiana();
        MedidaDeDistancia manhattan = new DistanciaDeManhattan();

        System.out.println("Regra de desempate (KNN): em caso de empate na votação dos K vizinhos, "
                + "vence o perfil que possui o vizinho mais próximo\n");

        System.out.println("=== ITEM 3: ACURÁCIA POR VALOR DE K (Euclidiana + Normalizado) ===");
        int[] valoresK = {1, 3, 5, 7, 9, 11};
        int melhorK = valoresK[0];
        double melhorAcuracia = -1.0;

        for (int k : valoresK) {
            KNN knn = new KNN(k, euclidiana, normalizador, treino);
            double acuracia = calcularAcuracia(knn, teste);
            System.out.printf("K = %2d -> Acurácia: %.2f%%%n", k, acuracia);

            if (acuracia > melhorAcuracia && k > 1 && k < 9) {
                melhorAcuracia = acuracia;
                melhorK = k;
            }
        }

        System.out.println("\n=== ITEM 4: COMPARAÇÃO DE CONFIGURAÇÕES (K = " + melhorK + ") ===");
        KNN eucComNorm = new KNN(melhorK, euclidiana, normalizador, treino);
        KNN eucSemNorm = new KNN(melhorK, euclidiana, null, treino);
        KNN manComNorm = new KNN(melhorK, manhattan, normalizador, treino);
        KNN manSemNorm = new KNN(melhorK, manhattan, null, treino);

        System.out.printf("Euclidiana + Com Normalização: %.2f%%%n", calcularAcuracia(eucComNorm, teste));
        System.out.printf("Euclidiana + Sem Normalização: %.2f%%%n", calcularAcuracia(eucSemNorm, teste));
        System.out.printf("Manhattan  + Com Normalização: %.2f%%%n", calcularAcuracia(manComNorm, teste));
        System.out.printf("Manhattan  + Sem Normalização: %.2f%%%n", calcularAcuracia(manSemNorm, teste));

        System.out.println("\n=== ITEM 5: MATRIZ DE CONFUSÃO (K = " + melhorK + " | Euclidiana + Normalizado) ===");
        imprimirMatrizConfusao(eucComNorm, teste);

        Jogador jogadorNovo = new Jogador(6161, "TokenDoFortnite", 18.5, 30, 120.0, 61.0, Perfil.CASUAL);
        Perfil perfilPrevisto = eucComNorm.classificar(jogadorNovo);
        System.out.println("\nClassificação do jogador novo (" + jogadorNovo.getApelido() + "): " + perfilPrevisto);

        executarParteE(todosJogadores);
    }

    public static void executarParteE(List<Jogador> todosJogadores) {
        System.out.println("\n**************************************************");
        System.out.println("   PARTE E - A DISTÂNCIA ALTERA A QUALIDADE DO KNN?");
        System.out.println("**************************************************");

        int numDivisoes = 30;
        int[] valoresK = {1, 3, 5, 7, 9, 11};
        MedidaDeDistancia euclidiana = new DistanciaEuclidiana();
        MedidaDeDistancia manhattan = new DistanciaDeManhattan();

        System.out.println("Comparação em " + numDivisoes + " divisões aleatórias (80% treino / 20% teste):\n");
        System.out.printf("%-4s | %-22s | %-22s | %-22s | %-22s%n",
                "K", "Euclidiana (Norm)", "Manhattan (Norm)", "Euclidiana (Bruto)", "Manhattan (Bruto)");
        System.out.println("------------------------------------------------------------------------------------------------------");

        for (int k : valoresK) {
            double[] accEucNorm = new double[numDivisoes];
            double[] accManNorm = new double[numDivisoes];
            double[] accEucBruto = new double[numDivisoes];
            double[] accManBruto = new double[numDivisoes];

            for (int d = 0; d < numDivisoes; d++) {
                List<Jogador> copia = new ArrayList<>(todosJogadores);
                Collections.shuffle(copia, new Random(100L + d));

                int corte = (int) (copia.size() * 0.8);
                List<Jogador> treino = copia.subList(0, corte);
                List<Jogador> teste = copia.subList(corte, copia.size());

                Normalizador norm = new Normalizador();
                norm.ajustar(treino);

                accEucNorm[d] = calcularAcuracia(new KNN(k, euclidiana, norm, treino), teste);
                accManNorm[d] = calcularAcuracia(new KNN(k, manhattan, norm, treino), teste);
                accEucBruto[d] = calcularAcuracia(new KNN(k, euclidiana, null, treino), teste);
                accManBruto[d] = calcularAcuracia(new KNN(k, manhattan, null, treino), teste);
            }

            System.out.printf("K=%-2d | %6.2f%% (± %5.2f%%)   | %6.2f%% (± %5.2f%%)   | %6.2f%% (± %5.2f%%)   | %6.2f%% (± %5.2f%%)%n",
                    k,
                    media(accEucNorm), desvioPadrao(accEucNorm),
                    media(accManNorm), desvioPadrao(accManNorm),
                    media(accEucBruto), desvioPadrao(accEucBruto),
                    media(accManBruto), desvioPadrao(accManBruto));
        }
    }

    private static double media(double[] valores) {
        double soma = 0.0;
        for (double v : valores) {
            soma += v;
        }
        return soma / valores.length;
    }

    private static double desvioPadrao(double[] valores) {
        double m = media(valores);
        double somaQuadrados = 0.0;
        for (double v : valores) {
            somaQuadrados += Math.pow(v - m, 2);
        }
        return Math.sqrt(somaQuadrados / (valores.length - 1));
    }

    private static double calcularAcuracia(KNN modelo, List<Jogador> teste) {
        int acertos = 0;
        for (Jogador j : teste) {
            if (modelo.classificar(j) == j.getPerfil()) {
                acertos++;
            }
        }
        return (double) acertos / teste.size() * 100.0;
    }

    private static void imprimirMatrizConfusao(KNN modelo, List<Jogador> teste) {
        Perfil[] perfis = Perfil.values();
        int[][] matriz = new int[perfis.length][perfis.length];

        for (Jogador j : teste) {
            int real = j.getPerfil().ordinal();
            int previsto = modelo.classificar(j).ordinal();
            matriz[real][previsto]++;
        }

        System.out.printf("%-12s | %-10s | %-10s | %-10s%n", "REAL \\ PREV", perfis[0], perfis[1], perfis[2]);
        System.out.println("----------------------------------------------------");
        for (int i = 0; i < perfis.length; i++) {
            System.out.printf("%-12s | %-10d | %-10d | %-10d%n",
                    perfis[i], matriz[i][0], matriz[i][1], matriz[i][2]);
        }
    }
}