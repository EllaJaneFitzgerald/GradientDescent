To compile the project into an executable jar in the project folder run:
	$ mvn package -Dmaven.test.skip=true

To execute the program you need Spark and HDFS. If they're not installed, read README_cluster_configuration.md

Run the program with the following parameters:

	- hdfs uri (hdfs://master:8020/test1.txt)
	- alpha (0.09)
	- maxIter (30000)
	- minPartitions (8)
	- minCores (4)

master $ bin/spark-submit --master spark://master:7077 --deploy-mode client ~/IdeaProjects/GS/target/GradientSearch-1.0-SNAPSHOT.jar hdfs://master:8020/test1.txt 0.09 30000 8 4

To monitor the execution use Spark UI and HDFS UI.

To generate a sample use Generate Generate_Training_Data.py in tools/.