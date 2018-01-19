package revolhope.splanes.com.keystore.model;

import java.io.Serializable;

import revolhope.splanes.com.keystore.model.interfaces.HeaderVisitor;

/**
 * Created by splanes on 15/1/18.
 **/

public abstract class Header implements Serializable{

    int id;
    String plainName;
    int parentId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlainName() {
        return plainName;
    }

    public void setPlainName(String plainName) {
        this.plainName = plainName;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public abstract <T> T checkClass(HeaderVisitor<T> visitor);
}
