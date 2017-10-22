import org.apache.spark.api.java.function.Function;

// возвращает массив вида b = {(y-h(x))*x_0, ..., (y-h(x))*x_(n-1), y-h(x), (y-h(x))^2}
// последний столбец - для вычисления ошибки
public class MakeTerm implements Function<double[], double[]> {
    private int n;
    private double[] theta;

    MakeTerm(int n, double[] theta) {
        this.n = n;
        this.theta = theta;
    }

    // h_teta(x) = theta_0*x_0 + ... + theta_(n-1)*x_(n-1) + theta_n
    private double hLinear (double[] x) {
        double sum = 0;
        for (int i=0; i<n; i++) {
            sum += theta[i]*x[i];
        }
        return sum + theta[n];
    }

    public double[] call(double[] a) {
        double[] b = new double[n+2];
        double[] x = new double[n+1];
        System.arraycopy(a,0,x,0,n);
        x[n] = 1;

        for (int j=0; j<n+1; j++) {
            // (y - h_theta(x)) * x_j
            b[j] = (a[n] - hLinear(x)) * x[j];
        }
        b[n+1] = b[n]*b[n];
        return b;
    }
}
