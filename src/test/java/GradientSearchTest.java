import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GradientSearchTest {
    static String[] path = {"hdfs://jarvis:8020/test1.txt","hdfs://jarvis:8020/test2.txt","hdfs://jarvis:8020/test3.txt"};
    static double[] alpha = {0.09,0.09,0.01};
    static double[] eps = {1e-6,1e-6,1e-6};
    static int[] n = new int[path.length];
    static int[] m = new int[path.length];
    static JavaSparkContext sc;

    @Before
    public void tuning() {
        SparkConf conf = new SparkConf().setAppName("GradientSearchTest").setMaster("local");
        sc = new JavaSparkContext(conf);

        Configuration config = new Configuration();
        for (int i=0; i<path.length; i++) {
            try {
                FileSystem fs = FileSystem.get(URI.create(path[i]), config);
                FSDataInputStream in = fs.open(new Path(path[i]));
                n[i] = GradientSearch.determineDimension(in);
                m[i] = GradientSearch.getNumberOfLines(in) + 1;
                in.close();
                fs.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void dimensionAndNumberOfLines() {
        int[] nRes = {1,2,1};
        int[] mRes = {4,4,202};
        for (int i=0; i<path.length; i++) {
            System.out.print(n[i] + " ");
        }
        for (int i=0; i<path.length; i++) {
            System.out.print(m[i] + " ");
        }

        assertArrayEquals(nRes,n);
        assertArrayEquals(mRes,m);
    }

    @Test
    public void getPointTest() {
        JavaRDD<String> lines = sc.textFile(path[0]);
        JavaRDD<double[]> points = lines.map(new GetPoint(n[0]));

        boolean flag = true;
        List<double[]> pointsList = points.collect();
        double[][] data = {{0,0},{1,1},{2,2},{3,3}};
        List<double[]> dataList = Arrays.asList(data);

        for (int i=0; i<4; i++) {
            for (int j=0; j<2; j++) {
                if (dataList.get(i)[j] != pointsList.get(i)[j]) {
                    flag = false;
                    break;
                }
            }
        }
        assertTrue(flag);
    }

    @Test
    public void makeTermTest() {
        JavaRDD<String> lines = sc.textFile(path[1]);
        JavaRDD<double[]> points = lines.map(new GetPoint(n[1]));
        double[] theta = {5,2,3};
        JavaRDD<double[]> terms = points.map(new MakeTerm(n[1],theta));

        boolean flag = true;
        List<double[]> termsList = terms.collect();
        double[][] data = {{0,0},{-4,0},{0,-2},{-5,-5}};
        List<double[]> dataList = Arrays.asList(data);

        for (int i=0; i<4; i++) {
            for (int j=0; j<2; j++) {
                if (dataList.get(i)[j] != termsList.get(i)[j]) {
                    flag = false;
                break;
                }
            }
        }
        assertTrue(flag);
    }

    @Test
    public void sumTest() {
        JavaRDD<String> lines = sc.textFile(path[1]);
        JavaRDD<double[]> points = lines.map(new GetPoint(n[1]));
        double[] theta = {5,2,3};
        JavaRDD<double[]> terms = points.map(new MakeTerm(n[1],theta));
        double[] sum = terms.reduce(new Sum(n[1]));

        boolean flag = true;
        double[] data = {-9,-7,-12};
        for (int i=0; i<sum.length; i++){
            System.out.print(sum[i] + "  ");
        }

        for (int i=0; i<data.length; i++) {
            if (data[i] != sum[i]) {
                flag = false;
                break;
            }
        }
        assertTrue(flag);
    }

    @After
    public void close() {
        sc.close();
    }


   /* @Test
    public void test1() throws IOException {
        double[][] b = {{1,0}, {2, 1, 2}, {2.50639,0.03305}};

        for (int j=0; j<n; j++) {
            double[] a = new GradientSearch(path[j], alpha[j], eps[j]).go();
            boolean flag = true;

            for (int i = 0; i < a.length; i++) {
                if (Math.abs(a[i] - b[j][i]) >= 100 * eps[j]) {
                    flag = false;
                    break;
                }
            }
            assertTrue(flag);
        }
    }*/

    /*public static void main(String[] args) throws Exception {
        JUnitCore runner = new JUnitCore();
        Result result = runner.run(GradientSearchTest.class);
        System.out.println("run tests: " + result.getRunCount());
        System.out.println("failed tests: " + result.getFailureCount());
        System.out.println("ignored tests: " + result.getIgnoreCount());
        System.out.println("success: " + result.wasSuccessful());
    }*/
}
