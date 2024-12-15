package us.devmasters.services;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static final Map<String,Column> typeMapper=new HashMap<>();
    static {
        typeMapper.put("boolean",new Column("BIT",1));
        typeMapper.put("byte",new Column("BIT",8));
        typeMapper.put("int",new Column("int"));
        typeMapper.put("long",new Column("BIGINT"));
        typeMapper.put("float",new Column("FLOAT"));
        typeMapper.put("double",new Column("DOUBLE"));
        typeMapper.put("bigdecimal",new Column("DECIMAL"));
        typeMapper.put("localdateTime",new Column("DATETIME"));
        typeMapper.put("localdate",new Column("DATE"));
        typeMapper.put("string",new Column("varchar",255));
    }
    private Utils(){}

    public static Map<String, Column> getTypeMapper() {
        return typeMapper;
    }

    public static String extractTableName(Class<?> loadedClass) {
        Table table=loadedClass.getAnnotation(Table.class);
        if(table!=null && !table.name().isEmpty())
            return table.name();
        return extractByConvention(loadedClass.getSimpleName());
    }

    public static boolean isEntity(Class<?> loadedClass)
    {
        return loadedClass.getAnnotation(Entity.class)!=null || loadedClass.getAnnotation(jakarta.persistence.Entity.class)!=null;
    }
    public static boolean isTransient(Field field)
    {
        return field.getAnnotation(Transient.class)!=null || field.getAnnotation(jakarta.persistence.Transient.class)!=null;
    }
    public static String extractColumnName(Field field)
    {
        javax.persistence.Column column=field.getAnnotation(javax.persistence.Column.class);
        if(column!=null && !column.name().isEmpty())
            return column.name();
        return extractByConvention(field.getName());
    }
    private static String extractByConvention(String name)
    {
        StringBuilder tableName=new StringBuilder();
        final int UPPER_CASES_START=65;
        final int UPPER_CASES_END=90;
        for(int i=0;i<name.length();i++)
        {
            char c=name.charAt(i);
            if(c >=UPPER_CASES_START && c <=UPPER_CASES_END) {
                if(i!=0)
                    tableName.append("_");
                tableName.append((char) (c + 32));
                continue;
            }
            tableName.append(c);
        }
        return tableName.toString();
    }
}
