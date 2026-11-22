package ml.kmeans;
import model.*;

import ml.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KMeans {

    private final int k;
    private final MedidaDeDistancia medida;
    private final Random random;
    private final int maxIteracoes;

    private double[][] centroides;
    private int[] grupos;
    private List<double[]> dadosTreino;
    private List<Jogador> jogadoresOriginais;
    private Normalizador normalizador;

    public KMeans(int k, MedidaDeDistancia medida, long semente, int maxIteracoes) {
        if (k <= 0) {
            throw new IllegalArgumentException("K deve ser maior que zero.");
        }
        this.k = k;
        this.medida = medida;
        this.random = new Random(semente);
        this.maxIteracoes = maxIteracoes;
    }

    public void treinar(List<Jogador> jogadores, Normalizador normalizador) {
        if (jogadores == null || jogadores.isEmpty()) {
            throw new IllegalArgumentException("Lista de jogadores não pode ser nula ou vazia.");
        }
        if (k > jogadores.size()) {
            throw new IllegalArgumentException("K não pode ser maior que o número de jogadores.");
        }

        this.jogadoresOriginais = jogadores;
        this.normalizador = normalizador;
        this.dadosTreino = new ArrayList<>();

        for (Jogador j : jogadores) {
            if (normalizador != null) {
                this.dadosTreino.add(normalizador.transformar(j));
            } else {
                this.dadosTreino.add(j.getCaracteristicas());
            }
        }

        int n = dadosTreino.size();
        int dimensoes = dadosTreino.get(0).length;

        this.centroides = new double[k][dimensoes];
        this.grupos = new int[n];
        Arrays.fill(this.grupos, -1);

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        Collections.shuffle(indices, random);

        for (int g = 0; g < k; g++) {
            this.centroides[g] = dadosTreino.get(indices.get(g)).clone();
        }

        for (int iter = 0; iter < maxIteracoes; iter++) {
            boolean mudouAlgumGrupo = false;

            for (int i = 0; i < n; i++) {
                int melhorGrupo = encontrarCentroideMaisProximo(dadosTreino.get(i));
                if (melhorGrupo != grupos[i]) {
                    grupos[i] = melhorGrupo;
                    mudouAlgumGrupo = true;
                }
            }

            if (!mudouAlgumGrupo) {
                break;
            }

            double[][] somaPorGrupo = new double[k][dimensoes];
            int[] contagemPorGrupo = new int[k];

            for (int i = 0; i < n; i++) {
                int g = grupos[i];
                contagemPorGrupo[g]++;
                double[] ponto = dadosTreino.get(i);
                for (int d = 0; d < dimensoes; d++) {
                    somaPorGrupo[g][d] += ponto[d];
                }
            }

            for (int g = 0; g < k; g++) {
                if (contagemPorGrupo[g] == 0) {
                    int indiceAleatorio = random.nextInt(n);
                    centroides[g] = dadosTreino.get(indiceAleatorio).clone();
                } else {
                    for (int d = 0; d < dimensoes; d++) {
                        centroides[g][d] = somaPorGrupo[g][d] / contagemPorGrupo[g];
                    }
                }
            }
        }
    }

    private int encontrarCentroideMaisProximo(double[] ponto) {
        int melhorGrupo = 0;
        double menorDistancia = medida.calcular(ponto, centroides[0]);

        for (int g = 1; g < k; g++) {
            double dist = medida.calcular(ponto, centroides[g]);
            if (dist < menorDistancia) {
                menorDistancia = dist;
                melhorGrupo = g;
            }
        }
        return melhorGrupo;
    }

    public double calcularInercia() {
        double inercia = 0.0;
        for (int i = 0; i < dadosTreino.size(); i++) {
            int g = grupos[i];
            double dist = medida.calcular(dadosTreino.get(i), centroides[g]);
            inercia += dist * dist;
        }
        return inercia;
    }

    public double calcularPureza() {
        int[][] contagem = getTabelaGruposPorPerfil();
        int somaVencedores = 0;
        for (int g = 0; g < k; g++) {
            int maiorNoGrupo = 0;
            for (int p = 0; p < Perfil.values().length; p++) {
                if (contagem[g][p] > maiorNoGrupo) {
                    maiorNoGrupo = contagem[g][p];
                }
            }
            somaVencedores += maiorNoGrupo;
        }
        return (double) somaVencedores / jogadoresOriginais.size() * 100.0;
    }

    public int[] getTamanhosGrupos() {
        int[] tamanhos = new int[k];
        for (int g : grupos) {
            tamanhos[g]++;
        }
        return tamanhos;
    }

    public double[][] getCentroidesEscalaOriginal() {
        double[][] originais = new double[k][];
        for (int g = 0; g < k; g++) {
            if (normalizador != null) {
                originais[g] = normalizador.desnormalizar(centroides[g]);
            } else {
                originais[g] = centroides[g].clone();
            }
        }
        return originais;
    }

    public int[][] getTabelaGruposPorPerfil() {
        int[][] tabela = new int[k][Perfil.values().length];
        for (int i = 0; i < jogadoresOriginais.size(); i++) {
            int g = grupos[i];
            int perfilIdx = jogadoresOriginais.get(i).getPerfil().ordinal();
            tabela[g][perfilIdx]++;
        }
        return tabela;
    }
}