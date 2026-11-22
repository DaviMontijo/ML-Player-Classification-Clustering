package ml;

public class DistanciaDeManhattan implements MedidaDeDistancia {
    
    @Override
    public double calcular(double[] a, double[] b){
        if (a == null || b == null || a.length != b.length) {
            throw new IllegalArgumentException("Os vetores devem ter o mesmo tamanho e não podem ser nulos");
        }
        double soma = 0.0;
        for(int i = 0; i < a.length; i++){
            soma += Math.abs(a[i] - b[i]);
        }
        return soma;
    } 
}
