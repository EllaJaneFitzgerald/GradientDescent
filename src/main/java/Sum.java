import org.apache.spark.api.java.function.Function2;

// суммирует таблицу по столбцам
// последний элемент - ошибка
public class Sum  implements Function2<double[], double[], double[]> {
    private int n;

    Sum (int n) {
        this.n = n;
    }

    public double[] call(double[] a, double[] b) {
        double c[] = new double[n+2];
        for (int j=0; j<n+2; j++) {
            c[j] = a[j] + b[j];
        }
        return c;
    }
}