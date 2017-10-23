import org.apache.spark.api.java.function.Function;

// parse string of coordinates into array
public class GetPoint implements Function<String, double[]> {
    private int n;

    GetPoint(int n) {
        this.n = n;
    }

    public double[] call(String s) {
        String[] parts = s.split(" ");
        double[] res = new double[n+1];
        for (int i=0; i<n+1; i++) {
            res[i] = Double.parseDouble(parts[i]);
        }
        return res;
    }
}