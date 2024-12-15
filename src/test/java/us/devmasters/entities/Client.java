package us.devmasters.entities;

import javax.persistence.*;
import java.lang.reflect.Field;

@Entity
@Table(name = "client_table")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name",nullable = false )
    private String name;

    @Column(unique = true )
    private String uniqueString;
    @Transient
    private String transientField;

    private Field field;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
