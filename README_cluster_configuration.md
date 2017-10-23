- Set up DNS (forward and reverse).
- Set up HDFS:
  - master:
    - `etc/hadoop/hadoop-env.sh`

    ```
    ...
    export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
    ...
    ```
    
    - `etc/hadoop/core-site.xml`:

    ```
    <configuration>
      <property>
        <name>fs.defaultFS</name>
        <value>hdfs://0.0.0.0:8020</value>
      </property>
    </configuration>
    ```

    - `etc/hadoop/hdfs-site.xml`:

    ```
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
    ```

    - `mkdir /srv/dfs/{data,name}`
    - `chown ella /srv/dfs/{data,name}`
    - `bin/hdfs namenode -format`

  - slave:
    - `etc/hadoop/hadoop-env.sh`

    ```
    ...
    export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
    ...
    ```

    - `etc/hadoop/core-site.xml`:

    ```
    <configuration>
      <property>
        <name>fs.defaultFS</name>
        <value>hdfs://jarvis:8020</value>
      </property>
    </configuration>
    ```

    - `etc/hadoop/hdfs-site.xml`:

    ```
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
    ```

    - `mkdir /srv/dfs/data`
    - `chown xio /srv/dfs/data`
- Launch HDFS: 
  - master: 
    - `sbin/start-dfs.sh`
    - `bin/hdfs dfs -put ~/test1.txt /`
  - slave: 
    - `sbin/hadoop-daemon.sh start datanode`  
- Launch Spark:
  - master:
    - `sbin/start-master.sh -h 0.0.0.0`
    - `sbin/start-slave.sh spark://master:7077`
  - slave:
    - `sbin/start-slave.sh spark://master:7077`
