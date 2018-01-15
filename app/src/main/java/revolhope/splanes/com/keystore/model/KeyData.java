package revolhope.splanes.com.keystore.model;

import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by splanes on 4/1/18.
 **/

public class KeyData implements Serializable{

    private KeyMetadata keyMetadata;
    private String nameID;
    private String user;
    private String brief;
    private String key;
    private int idParent;
    private int id;

    public KeyMetadata getKeyMetadata() {
        return keyMetadata;
    }
    public void setKeyMetadata(KeyMetadata keyMetadata) {
        this.keyMetadata = keyMetadata;
    }
    public String getNameID() {
        return nameID;
    }
    public void setNameID(String nameID) {
        this.nameID = nameID;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getBrief() {
        return brief;
    }
    public void setBrief(String brief) {
        this.brief = brief;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public int getIdParent() {
        return idParent;
    }
    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
