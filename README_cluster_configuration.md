1. Set up DNS (forward and reverse).
2. Set up HDFS:
	2.1 master:
		2.1.1. etc/hadoop/hadoop-env.sh
		`
		...
		export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
		...
		`
		2.1.2. etc/hadoop/core-site.xml:
		`
		<configuration>
		    <property>
			<name>fs.defaultFS</name>
			<value>hdfs://0.0.0.0:8020</value>
		    </property>
		</configuration>
		`
		2.1.3. etc/hadoop/hdfs-site.xml:
		`
		<configuration>
		    <property>
			<name>dfs.replication</name>
			<value>1</value>
		    </property>
		    <property>
			<name>dfs.namenode.name.dir</name>
			<value>/srv/dfs/name</value>
		    </property>
		    <property>
			<name>dfs.datanode.data.dir</name>
			<value>/srv/dfs/data</value>
		    </property>
		</configuration>
		`
		2.1.4. mkdir /srv/dfs/{data,name}
		2.1.5. chown ella /srv/dfs/{data,name}
		2.1.6. bin/hdfs namenode -format

	2.2. slave:
		2.2.1. etc/hadoop/hadoop-env.sh
		`
		...
		export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
		...
		`
		2.2.2. etc/hadoop/core-site.xml:
		`
		<configuration>
		    <property>
			<name>fs.defaultFS</name>
			<value>hdfs://jarvis:8020</value>
		    </property>
		</configuration>
		`
		2.2.3. etc/hadoop/hdfs-site.xml:
		`
		<configuration>
		    <property>
			<name>dfs.replication</name>
			<value>1</value>
		    </property>
		    <property>
			<name>dfs.datanode.data.dir</name>
			<value>/srv/dfs/data</value>
		    </property>
		</configuration>
		`
		2.2.4. mkdir /srv/dfs/data
		2.2.5. chown xio /srv/dfs/data
3. Launch HDFS: 
	3.1. master: 
		3.1.1. sbin/start-dfs.sh
		3.1.2. bin/hdfs dfs -put ~/test1.txt /
   	3.2. slave: 
   		3.2.1. sbin/hadoop-daemon.sh start datanode		
4. Launch Spark:
	4.1. master:
		4.1.1. sbin/start-master.sh -h 0.0.0.0
		4.1.2. sbin/start-slave.sh spark://master:7077
	4.2. slave:
		4.2.1. sbin/start-slave.sh spark://master:7077