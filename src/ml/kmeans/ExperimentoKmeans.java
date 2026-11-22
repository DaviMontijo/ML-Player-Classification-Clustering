package ml.kmeans;

import grafico.GeradorGrafico;
import ml.DistanciaEuclidiana;
import ml.MedidaDeDistancia;
import ml.Normalizador;
import model.Jogador;
import model.Perfil;

import java.util.List;

public class ExperimentoKmeans {

    private static final int NUM_TENTATIVAS_SEMENTES = 20;

    public static void executar(List<Jogador> todosJogadores) {
        Normalizador normalizador = new Normalizador();
        normalizador.ajustar(todosJogadores);

        MedidaDeDistancia euclidiana = new DistanciaEuclidiana();

        System.out.println("=== ITEM 3: MÉTODO DO COTOVELO (K = 1 a 6 | Melhor de " + NUM_TENTATIVAS_SEMENTES + " sementes) ===");
        int[] valoresK = {1, 2, 3, 4, 5, 6};
        double[] inercias = new double[valoresK.length];

        for (int i = 0; i < valoresK.length; i++) {
            KMeans melhorKm = executarComMultiplasSementes(
                    valoresK[i], todosJogadores, normalizador, euclidiana, NUM_TENTATIVAS_SEMENTES);
            inercias[i] = melhorKm.calcularInercia();
            System.out.printf("K = %d -> Menor Inércia: %.4f%n", valoresK[i], inercias[i]);
        }

        GeradorGrafico.salvarGraficoCotovelo(valoresK, inercias, "grafico_cotovelo.png");

        System.out.println("\n=== ITEM 4A: K = 3 COM NORMALIZAÇÃO (Melhor de " + NUM_TENTATIVAS_SEMENTES + " sementes) ===");
        executarAnaliseK3(todosJogadores, normalizador, euclidiana);

        System.out.println("\n=== ITEM 4B: K = 3 SEM NORMALIZAÇÃO (Melhor de " + NUM_TENTATIVAS_SEMENTES + " sementes) ===");
        executarAnaliseK3(todosJogadores, null, euclidiana);
    }

    private static KMeans executarComMultiplasSementes(
            int k,
            List<Jogador> jogadores,
            Normalizador norm,
            MedidaDeDistancia dist,
            int tentativas) {

        KMeans melhorModelo = null;
        double menorInercia = Double.POSITIVE_INFINITY;

        for (long semente = 1L; semente <= tentativas; semente++) {
            KMeans candidato = new KMeans(k, dist, semente, 100);
            candidato.treinar(jogadores, norm);
            double inerciaAtual = candidato.calcularInercia();

            if (inerciaAtual < menorInercia) {
                menorInercia = inerciaAtual;
                melhorModelo = candidato;
            }
        }

        return melhorModelo;
    }

    private static void executarAnaliseK3(List<Jogador> jogadores, Normalizador norm, MedidaDeDistancia dist) {
        KMeans km = executarComMultiplasSementes(3, jogadores, norm, dist, NUM_TENTATIVAS_SEMENTES);

        int[] tamanhos = km.getTamanhosGrupos();
        double[][] centroides = km.getCentroidesEscalaOriginal();
        int[][] tabela = km.getTabelaGruposPorPerfil();
        Perfil[] perfis = Perfil.values();

        System.out.printf("Menor inércia encontrada: %.4f%n", km.calcularInercia());
        System.out.println("Centroides na escala original (Horas | Partidas | Gasto | Vitória%):");
        for (int g = 0; g < 3; g++) {
            System.out.printf("Grupo %d (tamanho=%d): [%.2f h | %.2f part | R$ %.2f | %.2f%%]%n",
                    g, tamanhos[g], centroides[g][0], centroides[g][1], centroides[g][2], centroides[g][3]);
        }

        System.out.println("\nTabela de Grupos por Perfil Real:");
        System.out.printf("%-10s | %-10s | %-10s | %-10s%n", "GRUPO", perfis[0], perfis[1], perfis[2]);
        for (int g = 0; g < 3; g++) {
            System.out.printf("Grupo %-4d | %-10d | %-10d | %-10d%n",
                    g, tabela[g][0], tabela[g][1], tabela[g][2]);
        }

        System.out.printf("Pureza total: %.2f%%%n", km.calcularPureza());
    }
}