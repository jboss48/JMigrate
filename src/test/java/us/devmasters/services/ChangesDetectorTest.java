package us.devmasters.services;

import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

class ChangesDetectorTest {

    ChangesDetector changesDetector;

    @BeforeEach
    void setUp() {
        Properties properties=new Properties();
        properties.setProperty("url","jdbc:mysql://localhost:3036/jmigrate");
        properties.setProperty("username","root");
        properties.setProperty("password","root");
        changesDetector=new ChangesDetector(properties,new MigrationWriter(),"C:\\Users\\MohammedMHADA\\Desktop",new SqlGenerator());
    }
}