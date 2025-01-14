package us.devmasters.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

import static us.devmasters.services.Utils.isEntity;

public class ChangesDetector {

    private final Logger logger= LoggerFactory.getLogger(ChangesDetector.class);
    private ResultSet tables;
    private DatabaseMetaData metaData;
    private final Properties dbProperties;
    private final MigrationWriter migrationWriter;
    private final String migrationPath;
    private final SqlGenerator sqlGenerator;
    private  Connection connection;
    private boolean testENV=false;

    public ChangesDetector(Properties dbProperties, MigrationWriter migrationWriter, String migrationPath, SqlGenerator sqlGenerator) {
        this.dbProperties = dbProperties;
        this.migrationWriter = migrationWriter;
        this.migrationPath = migrationPath;
        this.sqlGenerator = sqlGenerator;
        loadMetaData();
    }
    public ChangesDetector(Properties dbProperties, MigrationWriter migrationWriter, String migrationPath, SqlGenerator sqlGenerator,boolean testENV) {
        this.dbProperties = dbProperties;
        this.migrationWriter = migrationWriter;
        this.migrationPath = migrationPath;
        this.sqlGenerator = sqlGenerator;
        this.testENV=testENV;
        loadMetaData();
    }
    private void loadMetaData()
    {
        connection=getDbConnection(dbProperties);
        if(connection==null)
            System.exit(0);
        try {
            metaData=connection.getMetaData();
            tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        }catch (SQLException sqlException)
        {
            logger.error("Unable to load DB meta data",sqlException);
            cleanUpExit(0);
        }
    }

    private Connection getDbConnection(Properties dbProperties)
    {
        String url=dbProperties.getProperty("url");
        try{
            return DriverManager.getConnection(url,dbProperties);
        }catch (SQLException sqlException)
        {
            logger.error("Unable to connect to db at url {} ",url,sqlException);
        }
        return null;
    }

    private void cleanUpExit(int status)
    {
        if(connection!=null)
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Unable to close connect with DB",e);
            }
        if(!testENV)
            System.exit(status);
    }
    public void detect(ProjectMetaData projectMetaData)
    {
        String compiledClassesBaseDir= projectMetaData.compiledClassesBaseDir();
        Path basePath= Paths.get(compiledClassesBaseDir);
        if(!Files.isDirectory(basePath)) {
            logger.error("Not a directory :{}", compiledClassesBaseDir);
            return;
        }
        try {
            Files.walk(basePath)
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class"))
                    .forEach(file -> {
                        String relativePath = basePath.relativize(file).toString();
                        StringBuilder classQualifiedNameBuilder=new StringBuilder();
                        int lastDotIndex=0;
                        for(int i=0;i<relativePath.length();i++)
                        {
                            char c=relativePath.charAt(i);
                            if(c=='.')
                                break;
                            else if(c==File.separatorChar) {
                                classQualifiedNameBuilder.append(".");
                                lastDotIndex=i;
                            }
                            else
                                classQualifiedNameBuilder.append(c);
                        }
                        if (classQualifiedNameBuilder.substring(0,lastDotIndex).equals(projectMetaData.entitiesPackage()))
                            doEntityDirtyCheck(compiledClassesBaseDir,classQualifiedNameBuilder.toString());
                    });
        } catch (IOException e) {
            logger.error("Unable to detect classes from base dire {} and base package {} due to ",compiledClassesBaseDir,projectMetaData.entitiesPackage(),e);
        }
        cleanUpExit(1);
    }

    public void doEntityDirtyCheck(String entitiesPath, String classQualifiedName) {
        URL[] urls ;
        try{
            urls=new URL[]{Paths.get(entitiesPath).toUri().toURL()};
        }catch (MalformedURLException malformedURLException)
        {
            logger.error("Unable to construct a url from path {}",entitiesPath,malformedURLException);
            return;
        }

        Class<?> loadedClass ;
        try (URLClassLoader classLoader = new URLClassLoader(urls)){
            loadedClass= classLoader.loadClass(classQualifiedName);
        }catch (Exception exception)
        {
            logger.error("Unable to load class with name {} from path {}",classQualifiedName,entitiesPath,exception);
            return;
        }
        if(!isEntity(loadedClass))
            return;
        try {
            buildActions(loadedClass);
        }catch (Exception exception)
        {
            logger.error("Unable to build actions due to ",exception);
            cleanUpExit(0);
        }
    }

    public void buildActions(Class<?> entity) throws SQLException {
        String sql;
        String tableName=Utils.extractTableName(entity);
        boolean newTable=true;
        while (tables.next() && newTable)
        {
            if(tableName.equals(tables.getString("TABLE_NAME")))
            {
                newTable=false;
            }
        }
        if(newTable) {
            sql=sqlGenerator.generateNewTable(entity, tableName);
            migrationWriter.write(sql,migrationPath);
            return;
        }
//        ResultSet tableColumns=metaData.getColumns(null,null,tableName,"%");
//        while (tableColumns.next())
//        {
//            Column c=new Column();
//            c.setName(tableColumns.getString("COLUMN_NAME"));
//            c.setNullable(tableColumns.getBoolean("IS_NULLABLE"));
//            c.setType(tableColumns.getString("DATA_TYPE"));
//            c.setSize(tableColumns.getInt("DATA_TYPE"));
//            c.set_default(tableColumns.getString("COLUMN_DEFAULT"));
//        }


    }


}
