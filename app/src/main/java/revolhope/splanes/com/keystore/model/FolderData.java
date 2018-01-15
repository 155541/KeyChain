package revolhope.splanes.com.keystore.model;

import android.util.Base64;
/**
 * Created by splanes on 11/1/18.
 **/

public class FolderData {

    private int id;
    private String encodedName;
    private int idParent;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getEncodedName() {
        return encodedName;
    }
    public void setToEncodeName(byte[] rawName) {
        this.encodedName = Base64.encodeToString(rawName,Base64.DEFAULT);
    }
    public int getIdParent() {
        return idParent;
    }
    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }
}
