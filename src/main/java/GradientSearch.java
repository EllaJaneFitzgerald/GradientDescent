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


public class GradientSearch implements Serializable{
    GradientSearch (String hdfsPath, double alpha, int maxIter, int numPartition) {
        this.hdfsPath = hdfsPath;
        this.alpha = alpha;
        this.maxIter = maxIter;
        this.numPartition = numPartition;

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

    // путь к файлу с данными HDFS
    private String hdfsPath;
    // n - размерность задачи
    private  int n;
    // teta - коэффициенты линейной регрессии
    private double[] theta;
    // tetaPrev - коэффициенты линейной регрессии на предыдущей итерации
    private double[] thetaPrev;
    // alpha - коэффициент обучения
    private double alpha;
    // m - объем выборки
    private int m;
    // maxIter - максимальное количество итераций
    private int maxIter;
    // numPartition - количество частей, на которое делятся данные
    private int numPartition;

    // определяет размерность задачи
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

    // определяет объем выборки
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
        SparkConf conf = new SparkConf().setAppName("GradientSearch");
        conf.setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> lines = sc.textFile(hdfsPath,numPartition);
        JavaRDD<double[]> points = lines.map(new GetPoint(n));
        points.cache();
        JavaRDD<double[]> terms;
        double[] sum;

        double oldError;
        double newError = 1e+100;
        int l=0;
        do {
            oldError = newError;
            System.arraycopy(theta,0,thetaPrev,0,n+1);
            terms = points.map(new MakeTerm(n,theta));
            sum = terms.reduce(new Sum(n));
            for (int j=0; j<n+1; j++) {
                theta[j] = theta[j] + alpha * sum[j] /m;
            }
            newError = sum[n+1]/m;
            System.out.println(newError);
            l++;
        } while ((l<=10 || oldError>newError) && (l<maxIter));
        sc.stop();

        System.out.println("Количество итераций: " + l);
        System.out.println("Объем выборки: " + m);
        return thetaPrev;
    }

    public static void main(String[] args) {
        //double[] resTeta = new GradientSearch(args[0],Double.parseDouble(args[1]), Integer.parseInt(args[2]),Integer.parseInt(args[3])).go();
        double[] resTeta = new GradientSearch("hdfs://jarvis:8020/test3.txt",0.1,30000, 4).go();
        for (int i=0; i<resTeta.length; i++) {
            System.out.print(resTeta[i] + " ");
        }
        System.out.println();
    }
}
