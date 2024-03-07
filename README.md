Build and run project with Maven
1) Download Maven: https://maven.apache.org/download.cgi 
   (apache-maven-3.9.6-bin.zip or apache-maven-3.9.6-bin.tar.gz)
2) Unzip and add bin folder to path
3) To test maven installation: 
   ```
   $ mvn -v
   ```
4) Compile java code, run tests and package the code up in a JAR file:
   ```
   $ mvn package
   ```
5) Build project
   ```
   $ mvn clean install
   ```
6) Execute JAR file:
   ```
   $ java -jar target/[filename].jar
   ```


```
$ mvn exec:java -Dexec.mainClass=skademlia.KademliaServer
$ mvn exec:java -Dexec.mainClass=skademlia.KademliaClient
```