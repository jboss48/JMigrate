package us.devmasters.services.types;

import java.lang.reflect.Field;

public class GeneratedValue {

    private final String strategy;

    private GeneratedValue(String strategy) {
        this.strategy = strategy;
    }

    public String strategy()
    {
        return strategy;
    }

    public static GeneratedValue resolveGeneratedValue(Field field)
    {
        javax.persistence.GeneratedValue  generatedValue=field.getAnnotation(javax.persistence.GeneratedValue.class);
        if(generatedValue!=null)
        {
            return new GeneratedValue(generatedValue.strategy().name());
        }
        jakarta.persistence.GeneratedValue jakartaGeneratedValue=field.getAnnotation(jakarta.persistence.GeneratedValue.class);
        if(jakartaGeneratedValue==null)
            return null;
        return new GeneratedValue(jakartaGeneratedValue.strategy().name());
    }
}
