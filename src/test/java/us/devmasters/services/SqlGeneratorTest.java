package us.devmasters.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.devmasters.entities.Client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static us.devmasters.entities.Constants.JAR_BASE_PATH;
import static us.devmasters.services.SqlGenerator.Actions.*;

class SqlGeneratorTest {
    SqlGenerator sqlGenerator;

    private static final String CLIENT_NAME_DEF="client_name varchar(255) not null";
    private static final String UNIQUE_STRING_DEF="unique_string varchar(255) unique";
    private static final String ID_DEF="id BIGINT primary key auto_increment";

    @BeforeEach
    void setUp() {
        sqlGenerator=new SqlGenerator();
    }

    private void generateColumn(String fieldName,String expectedResult) throws NoSuchFieldException {
        Field field= Client.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        assertEquals(expectedResult,sqlGenerator.generateColumn(field));
    }
    @Test
    void generateColumn_skip_transient_field() throws NoSuchFieldException {
       generateColumn("transientField","");
    }

    @Test
    void generateColumn_skip_unsupported_types() throws NoSuchFieldException {
        generateColumn("field","");
    }

    @Test
    void generateColumn_id_primary_auto_increment() throws NoSuchFieldException {
      generateColumn("id",ID_DEF);
    }
    @Test
    void generateColumn_not_null() throws NoSuchFieldException {
        generateColumn("name",CLIENT_NAME_DEF);
    }

    @Test
    void generateColumn_unique() throws NoSuchFieldException {
        generateColumn("uniqueString",UNIQUE_STRING_DEF);
    }
    @Test
    void generateNewTable_success(){
        Class<?> client=Loader.load(JAR_BASE_PATH,Client.class.getName());
        String clientDDL="create table if not exists client_table (\n"
                +ID_DEF+",\n"
                +CLIENT_NAME_DEF+",\n"
                +UNIQUE_STRING_DEF+"\n"
                +");\n";
        assertEquals(clientDDL,sqlGenerator.generateNewTable(client,"client_table"));
    }

    @Test
    void dropColumns_success() throws NoSuchFieldException {
        String dropClientNameColumn="alter table client_table\n"+
                "drop column client_name,\n"
                +"drop column unique_string"
                +";\n";
        List<Field> removedFields= new ArrayList<>(2);
        removedFields.add(Client.class.getDeclaredField("name"));
        removedFields.add(Client.class.getDeclaredField("uniqueString"));
        assertEquals(dropClientNameColumn,sqlGenerator.alterTable(removedFields,"client_table", DROP_COLUMN.name()));
    }

    @Test
    void addColumns_success() throws NoSuchFieldException {
        String dropClientNameColumn="alter table client_table\n"+
                "add column "+CLIENT_NAME_DEF+",\n"
                +"add column "+UNIQUE_STRING_DEF
                +";\n";
        List<Field> removedFields= new ArrayList<>(2);
        removedFields.add(Client.class.getDeclaredField("name"));
        removedFields.add(Client.class.getDeclaredField("uniqueString"));
        assertEquals(dropClientNameColumn,sqlGenerator.alterTable(removedFields,"client_table", ADD_COLUMN.name()));
    }
    @Test
    void changeColumn_success() throws NoSuchFieldException {
        String dropClientNameColumn="alter table client_table\n"+
                "change column client_name "+CLIENT_NAME_DEF
                +";\n";
        List<Field> removedFields= new ArrayList<>(2);
        removedFields.add(Client.class.getDeclaredField("name"));
        assertEquals(dropClientNameColumn,sqlGenerator.alterTable(removedFields,"client_table", CHANGE_COLUMN.name()));
    }
}