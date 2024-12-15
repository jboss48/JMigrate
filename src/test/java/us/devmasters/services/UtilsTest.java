package us.devmasters.services;

import org.junit.jupiter.api.Test;
import us.devmasters.entities.Client;
import us.devmasters.entities.Constants;
import us.devmasters.entities.ProductOrder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void extractTableName_success() {
        Class<?> productOrderClass=Loader.load(Constants.JAR_BASE_PATH, ProductOrder.class.getName());
        assertEquals("product_order",Utils.extractTableName(productOrderClass));
    }
    @Test
    void extractTableName_success_with_table_annotation() {
        Class<?> clientClass=Loader.load(Constants.JAR_BASE_PATH, Client.class.getName());
        assertEquals("client_table",Utils.extractTableName(clientClass));
    }

    @Test
    void extractColumnName_success() throws NoSuchFieldException {
        Field field=ProductOrder.class.getDeclaredField("productOrder");
        field.setAccessible(true);
        assertEquals("product_order",Utils.extractColumnName(field));
    }
    @Test
    void extractColumnName_success_with_column_annotation() throws NoSuchFieldException {
        Field field=Client.class.getDeclaredField("name");
        field.setAccessible(true);
        assertEquals("client_name",Utils.extractColumnName(field));
    }
}