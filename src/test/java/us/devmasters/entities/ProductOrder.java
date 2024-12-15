package us.devmasters.entities;

import javax.persistence.Entity;

@Entity
public class ProductOrder {
    public String productOrder;

    public String getProductOrder() {
        return productOrder;
    }

    public void setProductOrder(String productOrder) {
        this.productOrder = productOrder;
    }
}
