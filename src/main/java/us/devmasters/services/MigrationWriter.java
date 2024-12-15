package us.devmasters.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.UUID;

public class MigrationWriter {
    private static final String MIGRATION="migration";
    private final Logger logger= LoggerFactory.getLogger(MigrationWriter.class);
    public void write(String sql,String path)
    {
        String fileName= LocalDateTime.now()+"_"+ UUID.randomUUID() +"_"+MIGRATION+".sql";
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path+ File.separator+fileName))){
            bufferedWriter.write(sql);
        } catch (Exception e) {
            logger.error("Unable to write sql file ",e);
        }

    }
}
