package ml.knn;

import ml.MedidaDeDistancia;
import ml.Normalizador;
import model.Jogador;
import model.Perfil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class KNN {

    private static class Vizinho implements Comparable<Vizinho> {
        private final double distancia;
        private final Perfil perfil;

        public Vizinho(double distancia, Perfil perfil) {
            this.distancia = distancia;
            this.perfil = perfil;
        }

        @Override
        public int compareTo(Vizinho outro) {
            return Double.compare(this.distancia, outro.distancia);
        }
    }

    private final int k;
    private final MedidaDeDistancia medida;
    private final Normalizador normalizador;
    private final List<double[]> vetoresTreino;
    private final List<Perfil> rotulosTreino;

    public KNN(int k, MedidaDeDistancia medida, Normalizador normalizador, List<Jogador> dadosTreino) {
        if (k <= 0 || dadosTreino == null || k > dadosTreino.size()) {
            throw new IllegalArgumentException("Valor de k inválido para o tamanho do treino.");
        }
        this.k = k;
        this.medida = medida;
        this.normalizador = normalizador;
        this.vetoresTreino = new ArrayList<>();
        this.rotulosTreino = new ArrayList<>();

        for (Jogador j : dadosTreino) {
            if (normalizador != null) {
                vetoresTreino.add(normalizador.transformar(j));
            } else {
                vetoresTreino.add(j.getCaracteristicas());
            }
            this.rotulosTreino.add(j.getPerfil());
        }
    }

    /**
     * Classifica uma pessoa jogadora com base nos K vizinhos mais próximos.
     *
     * Regra de desempate documentada:
     * Caso dois ou mais perfis empatem com o maior número de votos entre os K vizinhos,
     * vence o perfil cujo vizinho mais próximo (menor distância ao alvo) aparecer primeiro
     * na lista ordenada por distância crescente.
     */
    public Perfil classificar(Jogador alvo) {
        double[] vetorAlvo = (normalizador != null)
                ? normalizador.transformar(alvo)
                : alvo.getCaracteristicas();

        List<Vizinho> vizinhos = new ArrayList<>();
        for (int i = 0; i < vetoresTreino.size(); i++) {
            double dist = medida.calcular(vetorAlvo, vetoresTreino.get(i));
            vizinhos.add(new Vizinho(dist, rotulosTreino.get(i)));
        }

        Collections.sort(vizinhos);

        Map<Perfil, Integer> votos = new EnumMap<>(Perfil.class);
        Map<Perfil, Double> menorDistanciaPorPerfil = new EnumMap<>(Perfil.class);

        for (int i = 0; i < k; i++) {
            Vizinho v = vizinhos.get(i);
            votos.put(v.perfil, votos.getOrDefault(v.perfil, 0) + 1);
            // Como vizinhos está ordenado por distância crescente, a 1ª ocorrência é a menor distância
            menorDistanciaPorPerfil.putIfAbsent(v.perfil, v.distancia);
        }

        Perfil vencedor = null;
        int maiorContagem = -1;
        double menorDistVencedor = Double.POSITIVE_INFINITY;

        for (Map.Entry<Perfil, Integer> entrada : votos.entrySet()) {
            Perfil perfilAtual = entrada.getKey();
            int contagemAtual = entrada.getValue();
            double distMinAtual = menorDistanciaPorPerfil.get(perfilAtual);

            if (contagemAtual > maiorContagem
                    || (contagemAtual == maiorContagem && distMinAtual < menorDistVencedor)) {
                maiorContagem = contagemAtual;
                menorDistVencedor = distMinAtual;
                vencedor = perfilAtual;
            }
        }

        return vencedor;
    }
}