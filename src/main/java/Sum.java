import org.apache.spark.api.java.function.Function2;

// суммирует таблицу по столбцам
public class Sum  implements Function2<double[], double[], double[]> {
    private int n;

    Sum (int n) {
        this.n = n;
    }

    public double[] call(double[] a, double[] b) {
        double c[] = new double[n+1];
        for (int j=0; j<n+1; j++) {
            c[j] = a[j] + b[j];
        }
        return c;
    }
}