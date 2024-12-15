package us.devmasters.services.types;

import java.lang.reflect.Field;

public class Column {

    private final boolean unique;

    private final boolean nullable;

    private Column(boolean unique, boolean nullable) {
        this.unique = unique;
        this.nullable = nullable;
    }

    public static Column resolveColumn(Field field)
    {
        javax.persistence.Column  column=field.getAnnotation(javax.persistence.Column.class);
        if(column!=null)
        {
            return new Column(column.unique(),column.nullable());
        }
        jakarta.persistence.Column jakartaColumn=field.getAnnotation(jakarta.persistence.Column.class);
        if(jakartaColumn==null)
            return null;
        return new Column(jakartaColumn.unique(),jakartaColumn.nullable());
    }
    public boolean unique() {
        return unique;
    }

    public boolean nullable() {
        return nullable;
    }
}
