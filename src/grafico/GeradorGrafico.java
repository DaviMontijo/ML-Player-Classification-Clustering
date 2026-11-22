package grafico;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GeradorGrafico {

    public static void salvarGraficoCotovelo(int[] valoresK, double[] inercias, String caminhoArquivo) {
        int largura = 800;
        int altura = 550;
        int margem = 80;

        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = imagem.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fundo branco
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, largura, altura);

        // Título
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Método do Cotovelo - K-Means (Inércia vs K)", margem, 40);

        // Eixos X e Y
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(margem, altura - margem, largura - margem, altura - margem); // Eixo X
        g2.drawLine(margem, margem, margem, altura - margem);                     // Eixo Y

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.drawString("Número de Grupos (K)", largura / 2 - 60, altura - 25);
        g2.drawString("Inércia", 15, margem - 15);

        // Escala máxima de Y
        double maxInercia = 0;
        for (double v : inercias) {
            if (v > maxInercia) maxInercia = v;
        }
        if (maxInercia == 0) maxInercia = 1;

        int areaLargura = largura - 2 * margem;
        int areaAltura = altura - 2 * margem;

        int[] pontosX = new int[valoresK.length];
        int[] pontosY = new int[valoresK.length];

        for (int i = 0; i < valoresK.length; i++) {
            pontosX[i] = margem + (int) ((double) i / (valoresK.length - 1) * areaLargura);
            pontosY[i] = (altura - margem) - (int) ((inercias[i] / maxInercia) * areaAltura);
        }

        // Linha azul conectando os pontos
        g2.setColor(new Color(30, 100, 200));
        g2.setStroke(new BasicStroke(3f));
        for (int i = 0; i < valoresK.length - 1; i++) {
            g2.drawLine(pontosX[i], pontosY[i], pontosX[i + 1], pontosY[i + 1]);
        }

        // Pontos vermelhos e rótulos dos valores
        for (int i = 0; i < valoresK.length; i++) {
            g2.setColor(new Color(210, 40, 40));
            g2.fillOval(pontosX[i] - 5, pontosY[i] - 5, 10, 10);

            g2.setColor(Color.DARK_GRAY);
            g2.drawString("K=" + valoresK[i], pontosX[i] - 12, altura - margem + 20);
            g2.drawString(String.format("%.2f", inercias[i]), pontosX[i] - 15, pontosY[i] - 10);
        }

        g2.dispose();

        try {
            ImageIO.write(imagem, "png", new File(caminhoArquivo));
            System.out.println("Gráfico salvo com sucesso em: " + caminhoArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar gráfico: " + e.getMessage());
        }
    }
}