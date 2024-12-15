package us.devmasters.services;

public class Column {
    private String name;
    private String type;
    private int size;
    private String _default;
    private boolean isNotNullable;

    private boolean isUnique;
    public Column() {
    }



    public Column(String type) {
        this.type = type;
    }
    public Column(String type, int size) {
        this.type = type;
        this.size = size;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public boolean isNotNullable() {
        return isNotNullable;
    }

    public void setNotNullable(boolean notNullable) {
        isNotNullable = notNullable;
    }





    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String get_default() {
        return _default;
    }

    public void set_default(String _default) {
        this._default = _default;
    }

}
