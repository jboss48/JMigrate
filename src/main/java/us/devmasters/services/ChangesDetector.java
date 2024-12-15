package us.devmasters.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Stream;

import static us.devmasters.services.Utils.isEntity;

public class ChangesDetector {

    private final Logger logger= LoggerFactory.getLogger(ChangesDetector.class);
    private ResultSet tables;
    private DatabaseMetaData metaData;
    private final Properties dbProperties;
    private final MigrationWriter migrationWriter;
    private final String migrationPath;
    private final SqlGenerator sqlGenerator;

    public ChangesDetector(Properties dbProperties, MigrationWriter migrationWriter, String migrationPath, SqlGenerator sqlGenerator) {
        this.dbProperties = dbProperties;
        this.migrationWriter = migrationWriter;
        this.migrationPath = migrationPath;
        this.sqlGenerator = sqlGenerator;
        loadMetaData();
    }
    private void loadMetaData()
    {
        Connection connection=getDbConnection(dbProperties);
        if(connection==null)
            System.exit(0);
        try {
            metaData=connection.getMetaData();
            tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        }catch (SQLException sqlException)
        {
            logger.error("Unable to load DB meta data",sqlException);
            System.exit(0);
        }
    }

    private Connection getDbConnection(Properties dbProperties)
    {
        String url=dbProperties.getProperty("url");
        try( Connection connection=DriverManager.getConnection(url,dbProperties)){
            return connection;
        }catch (SQLException sqlException)
        {
            logger.error("Unable to connect to db at url {} ",url,sqlException);
        }
        return null;
    }


    public void detect(String entitiesPath)
    {
        if(!Files.isDirectory(Paths.get(entitiesPath))) {
            logger.error("Not a directory :{}", entitiesPath);
            return;
        }
        Stream.of(new File(entitiesPath).listFiles())
                .filter(file -> file.getName().endsWith(".class"))
                .forEach(cClass-> doEntityDirtyCheck(entitiesPath,cClass.getName()));
    }

    public void doEntityDirtyCheck(String entitiesPath, String fileName) {
        URL[] urls ;
        try{
            urls=new URL[]{URI.create(entitiesPath).toURL()};
        }catch (MalformedURLException malformedURLException)
        {
            logger.error("Unable to construct a url from path {}",entitiesPath,malformedURLException);
            return;
        }

        Class<?> loadedClass ;
        try (URLClassLoader classLoader = new URLClassLoader(urls)){
            loadedClass= classLoader.loadClass(fileName);
        }catch (Exception exception)
        {
            logger.error("Unable to load class with name {} from path {}",fileName,entitiesPath,exception);
            return;
        }
        if(!isEntity(loadedClass))
            return;
        try {
            buildActions(loadedClass);
        }catch (Exception exception)
        {
            logger.error("Unable to build actions due to ",exception);
            System.exit(0);
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
