package us.devmasters.services.types;

import java.lang.reflect.Field;

public class Id {

    private Id() {
    }
    public static Id resolveId(Field field)
    {
        javax.persistence.Id  id=field.getAnnotation(javax.persistence.Id.class);
        if(id!=null)
        {
            return new Id();
        }
        jakarta.persistence.Id jakartaId=field.getAnnotation(jakarta.persistence.Id.class);
        if(jakartaId==null)
            return null;
        return new Id();
    }
}
