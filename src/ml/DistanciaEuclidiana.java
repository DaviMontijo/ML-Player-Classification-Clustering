package ml;

public class DistanciaEuclidiana implements MedidaDeDistancia {
    @Override
    public double calcular(double[] a, double[] b){
        if (a == null || b == null || a.length != b.length) {
            throw new IllegalArgumentException("Os vetores devem ter o mesmo tamanho e não podem ser nulos");
        }
        double soma = 0.0;
        for(int i = 0; i < a.length; i++){
            double diferenca = a[i] - b[i];
            soma += diferenca * diferenca;
        }
        return Math.sqrt(soma);
    } 
}
