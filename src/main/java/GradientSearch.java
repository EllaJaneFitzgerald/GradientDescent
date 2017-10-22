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
    GradientSearch (String hdfsPath, double alpha, double eps) {
        this.hdfsPath = hdfsPath;
        this.alpha = alpha;
        this.eps = eps;

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
    // eps - точность
    private double eps;
    // m - объем выборки
    private int m;

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

    // критерий остановки
    private boolean termination () {
        boolean flag = true;
        for (int i=0; i<n+1; i++) {
            if (Math.abs(theta[i] - thetaPrev[i])>=eps) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public double[] go() {
        SparkConf conf = new SparkConf().setAppName("GradientSearch");
        //conf.setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> lines = sc.textFile(hdfsPath);
        JavaRDD<double[]> points = lines.map(new GetPoint(n));
        JavaRDD<double[]> terms;
        double[] sum;

        int l=0;
        while (l<30000 && !termination()) {
            System.arraycopy(theta,0,thetaPrev,0,n+1);
            terms = points.map(new MakeTerm(n,theta));
            sum = terms.reduce(new Sum(n));
            for (int j=0; j<n+1; j++) {
                theta[j] = theta[j] + alpha * sum[j] /m;
                //System.out.print(teta[j] + " ");
            }
            //System.out.println();
            l++;
        }
        sc.stop();

        System.out.println("Количество итераций: " + l);
        System.out.println("Объем выборки: " + m);
        return theta;
    }

    public static void main(String[] args) {
        double[] resTeta = new GradientSearch(args[0],Double.parseDouble(args[1]),Double.parseDouble(args[2])).go();
        //double[] resTeta = new GradientSearch("hdfs://jarvis:8020/test1.txt",0.09,0.000001).go();
        for (int i=0; i<resTeta.length; i++) {
            System.out.print(resTeta[i] + " ");
        }
        System.out.println();
    }

}
