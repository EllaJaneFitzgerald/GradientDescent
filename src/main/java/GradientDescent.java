import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.*;
import java.net.URI;
import java.util.Arrays;


public class GradientDescent implements Serializable {
    GradientDescent(String hdfsPath, double alpha, int maxIter, int minPartitions, String maxCores) {
        this.hdfsPath = hdfsPath;
        this.alpha = alpha;
        this.maxIter = maxIter;
        this.minPartitions = minPartitions;
        this.maxCores = maxCores;

        Configuration conf = new Configuration();
        try {
            FileSystem fs = FileSystem.get(URI.create(hdfsPath), conf);
            FSDataInputStream in = fs.open(new Path(hdfsPath));

            this.n = determineDimension(in);
            theta = new double[n + 1];
            Arrays.fill(theta, 1.0);
            thetaPrev = new double[n + 1];
            Arrays.fill(thetaPrev, 0.0);
            m = getNumberOfLines(in)+1;

            in.close();
            fs.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // path to datafile on HDFS
    private String hdfsPath;
    // dimensionality of data
    private int n;
    // linear regression coefficients
    private double[] theta;
    // linear regression coefficients one iteration ago
    private double[] thetaPrev;
    // learning rate
    private double alpha;
    // sample size
    private int m;
    // maximum number of iterations
    private int maxIter;
    // minimum number of partitions which the dataset is split into
    private int minPartitions;
    // maximum number of active CPU cores
    private String maxCores;

    // determine dimensionality
    public static int determineDimension(FSDataInputStream in) {
        String firstLine = "";
        try {
           firstLine = in.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String[] parts = firstLine.split(" ");
        return parts.length - 1;
    }

    // determine sample size
    public static int getNumberOfLines(FSDataInputStream in) {
        int i=0;
        try {
            while ((in.readLine()) != null) {
                i++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return i;
    }

    public double[] go() {
        long start = System.currentTimeMillis();
        SparkConf conf = new SparkConf().setAppName("GradientDescent").set("spark.cores.max", maxCores);
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> lines = sc.textFile(hdfsPath, minPartitions);
        JavaRDD<double[]> points = lines.map(new GetPoint(n));
        points.cache();
        JavaRDD<double[]> terms;
        double[] sum;

        double oldError;
        double newError = 1e+100;
        int l = 0;
        do {
            oldError = newError;
            System.arraycopy(theta,0,thetaPrev,0,n+1);
            terms = points.map(new MakeTerm(n, theta));
            sum = terms.reduce(new Sum(n));
            for (int j=0; j<n+1; j++) {
                theta[j] = theta[j] + alpha * sum[j]/m;
            }
            newError = sum[n+1]/m;
            System.out.println(newError);
            l++;
        } while ((l<=10 || oldError>newError) && (l<maxIter));
        sc.stop();

        long finish = System.currentTimeMillis();
        System.out.println("Время выполнения: " + (finish-start));
        System.out.println("Количество итераций: " + l);
        System.out.println("Объем выборки: " + m);
        return thetaPrev;
    }

    public static void main(String[] args) {
        double[] resTheta = new GradientDescent(
                args[0],
                Double.parseDouble(args[1]),
                Integer.parseInt(args[2]),
                Integer.parseInt(args[3]),
                args[4]
        ).go();
        for (double t: resTheta) {
            System.out.print(t + " ");
        }
        System.out.println();
    }
}
