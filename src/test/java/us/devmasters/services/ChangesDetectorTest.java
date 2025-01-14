package us.devmasters.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import us.devmasters.entities.newtable.NewEntity;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ChangesDetectorTest {

     ChangesDetector changesDetector;
     MySQLContainer<?> mysqlContainer;
    Logger logger= LoggerFactory.getLogger(ChangesDetectorTest.class);
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
        changesDetector=new ChangesDetector(properties,new MigrationWriter(),resultDir.toString(),new SqlGenerator(),true);
    }

    @Test
    void testDetectNewTable() throws IOException {
        // arrange
        URL testClassesPath = NewEntity.class.getProtectionDomain().getCodeSource().getLocation();
        ProjectMetaData projectMetaData=new ProjectMetaData(testClassesPath.getPath(),NewEntity.class.getPackage().getName());

        //act
        changesDetector.detect(projectMetaData);

        // resultDir will only contain a single migration file
        //assert
        try (Stream<Path> paths = Files.list(resultDir)) {
            Path migrationPath=paths.findFirst().get();
            try {
                 String actual = new String(Files.readAllBytes(migrationPath));
                 String expected="""
                         create table if not exists new_entity (\n
                         id BIGINT primary key auto_increment,\n
                         label varchar(255) not null\n
                         );\n""";
                 assertEquals(expected,actual);
            }
            catch (IOException e) {
                logger.error("Failed to read file: {}" , migrationPath.getFileName(),e);
            }
        }
    }
    @AfterEach
    void tearDown() {
        mysqlContainer.close();
    }
}