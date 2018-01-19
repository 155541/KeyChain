package revolhope.splanes.com.keystore.model;

import java.io.Serializable;

import revolhope.splanes.com.keystore.model.interfaces.HeaderVisitor;

/**
 * Created by splanes on 15/1/18.
 **/

public class Folder extends Header implements Serializable{

    public <T> T checkClass(HeaderVisitor<T> visitor){
        return visitor.isFolder(this);
    }

    @Override
    public String toString() {
        return    "NAME.........-> " + plainName + "\n"
                + "PARENT ID....-> " + parentId;
    }
}
