package us.devmasters.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import us.devmasters.entities.newtable.NewEntity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;


class ChangesDetectorTest {

    ChangesDetector changesDetector;
     MySQLContainer<?> mysqlContainer;

     Path resultDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        resultDir=tempDir;

        String username="jboss48";
        String password="jboss48";
        String dbName="jmigrate";


        // Start the MySQL container
        mysqlContainer = new MySQLContainer<>("mysql:5.7")
                .withDatabaseName(dbName)
                .withUsername(username)
                .withPassword(password)
                .waitingFor(Wait.forLogMessage(".*Giving user jboss48 access to schema jmigrate.*\\n", 1));
        mysqlContainer.start();

        // setup change detector
        Properties properties=new Properties();
        properties.setProperty("url","jdbc:mysql://localhost:"+mysqlContainer.getMappedPort(3306)+"/"+dbName);
        properties.setProperty("user",username);
        properties.setProperty("password",password);
        changesDetector=new ChangesDetector(properties,new MigrationWriter(),resultDir.toString(),new SqlGenerator());
    }

    @Test
    void testDetectNewTable() throws IOException {
        URL testClassesPath = NewEntity.class.getProtectionDomain().getCodeSource().getLocation();
        String newEntityPath=testClassesPath.getPath()+NewEntity.class.getPackage().getName().replace('.', File.separatorChar);
        //act

        changesDetector.detect(newEntityPath);
        try (Stream<Path> paths = Files.list(resultDir)) {
            paths.forEach(path -> {
                if (Files.isRegularFile(path)) { // Only process regular files
                    try {
                        String content = new String(Files.readAllBytes(path));
                        System.out.println("File: " + path.getFileName());
                        System.out.println("Content: " + content);
                        System.out.println("------");
                    } catch (IOException e) {
                        System.err.println("Failed to read file: " + path.getFileName());
                        e.printStackTrace();
                    }
                }
            });

        }
    }
    @AfterEach
    void tearDown() {
        mysqlContainer.close();
    }
}