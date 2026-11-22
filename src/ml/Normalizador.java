package ml;
import model.Jogador;

import java.util.ArrayList;
import java.util.List;

public class Normalizador {
    private double[] min;
    private double[] max;
    private boolean ajustado = false;

    public void ajustar(List<Jogador> dadosTreino) {
        if (dadosTreino == null || dadosTreino.isEmpty()) {
            throw new IllegalArgumentException("A lista não pode ser nula e nem vazia.");
        }

        double[] primeiro = dadosTreino.get(0).getCaracteristicas();
        this.min = primeiro.clone();
        this.max = primeiro.clone();

        for (int i = 1; i < dadosTreino.size(); i++) {
            double[] atual = dadosTreino.get(i).getCaracteristicas();
            for (int j = 0; j < atual.length; j++) {
                if(atual[j] < min[j]) min[j] = atual[j];
                else if(atual[j] > max[j]) max[j] = atual[j];
            }
        }
        this.ajustado = true;
    }

    public double[] transformar(Jogador jogador) {
        if (!ajustado) {
            throw new IllegalStateException("O normalizador precisa ser ajustado antes de transformar.");
        }
        double[] original = jogador.getCaracteristicas();
        double[] normalizado = new double[original.length];

        for (int j = 0; j < original.length; j++) {
            double amplitude = max[j] - min[j];
            if(amplitude == 0) normalizado[j] = 0.0;
            else normalizado[j] = (original[j] - min[j]) / amplitude;
        }
        return normalizado;
    }

    public List<double[]> transformarTodos(List<Jogador> jogadores) {
        List<double[]> listaNormalizada = new ArrayList<>();
        for (Jogador jogador : jogadores) {
            listaNormalizada.add(transformar(jogador));
        }
        return listaNormalizada;
    }

    public double[] desnormalizar(double[] vetorNormalizado) {
        if (!ajustado) {
            throw new IllegalStateException("O normalizador precisa ser ajustado.");
        }
        double[] original = new double[vetorNormalizado.length];
        for (int j = 0; j < vetorNormalizado.length; j++) {
            double amplitude = max[j] - min[j];
            original[j] = (vetorNormalizado[j] * amplitude) + min[j];
        }
        return original;
    }
}