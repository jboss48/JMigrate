package us.devmasters.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.devmasters.services.types.GeneratedValue;
import us.devmasters.services.types.Id;

import javax.persistence.GenerationType;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static us.devmasters.services.SqlGenerator.Actions.*;
import static us.devmasters.services.Utils.isTransient;
import static us.devmasters.services.types.Column.resolveColumn;
import static us.devmasters.services.types.GeneratedValue.resolveGeneratedValue;
import static us.devmasters.services.types.Id.resolveId;


public class SqlGenerator {

    private final Logger logger= LoggerFactory.getLogger(SqlGenerator.class);

    public enum Actions{
        ADD_COLUMN,DROP_COLUMN,CHANGE_COLUMN
    }
    private interface Action{
         String perform(Field field);
    }
    private  final Map<String,Action> actions=new HashMap<>();

    public SqlGenerator() {
        actions.put(ADD_COLUMN.name(),this::addColumn);
        actions.put(DROP_COLUMN.name(),this::dropColumn);
        actions.put(CHANGE_COLUMN.name(),this::changeColumn);
    }

    public String generateNewTable(Class<?> entity, String tableName)
    {
        String baseQuery="create table if not exists %s (\n%s);\n";
        StringBuilder columns=new StringBuilder();
        Field []fields=entity.getDeclaredFields();
        for (Field field : fields) {
            String col = generateColumn(field);
            if (col.isEmpty())
                continue;
            columns.append(col)
                    .append(",").append("\n");
        }
        columns.replace(columns.length()-2,columns.length()-1,"");
        return String.format(baseQuery, tableName,columns);
    }
    public String alterTable(List<Field> fields, String tableName,String action)
    {
        StringBuilder columns=new StringBuilder();
        for (Field field : fields) {
            Action ac=actions.get(action);
            if(ac==null)
                continue;
            columns.append(ac.perform(field));
        }
        columns.replace(columns.length()-2,columns.length(),"");
        String alterTableFormat = "alter table %s\n%s;\n";
        return String.format(alterTableFormat, tableName,columns);
    }

    private String changeColumn(Field field)
    {
        return "change column "+ Utils.extractColumnName(field)+" "+generateColumn(field)+ ",\n";
    }
    private String dropColumn(Field field)
    {
        return "drop column " + Utils.extractColumnName(field) + ",\n";
    }
    private String addColumn(Field field)
    {
       String colDef=generateColumn(field);
       if(colDef.isEmpty())
           return colDef;
       return "add column "+colDef+",\n";
    }
    public String generateColumn(Field field)
    {
        if(isTransient(field))
            return "";
        Column column=Utils.getTypeMapper().get(field.getType().getSimpleName().toLowerCase());
        us.devmasters.services.types.Column columnAnnot= resolveColumn(field);
        if(columnAnnot!=null)
        {
            column.setNotNullable(!columnAnnot.nullable());
            column.setUnique(columnAnnot.unique());
        }
        if(column==null ) {
            logger.warn("Type {} not supported for mapping",field.getType().getSimpleName());
            return "";
        }
        StringBuilder builder=new StringBuilder();
        builder.append(Utils.extractColumnName(field))
                .append(' ')
                .append(column.getType());
        if(column.getSize()!=0)
            builder.append('(').append(column.getSize()).append(')');
        if(column.isNotNullable())
            builder.append(" not null");
        if(column.isUnique())
            builder.append(" unique");
        if(column.get_default()!=null)
            builder.append(" default").append(column.get_default());
        Id id= resolveId(field);
        if(id!=null)
        {
            builder.append(" primary key");
            GeneratedValue generatedValue=resolveGeneratedValue(field);
            if(generatedValue!=null && GenerationType.IDENTITY.name().equals(generatedValue.strategy()))
                builder.append(" auto_increment");
        }
        return builder.toString();
    }


}
