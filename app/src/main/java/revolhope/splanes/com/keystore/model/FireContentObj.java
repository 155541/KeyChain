package revolhope.splanes.com.keystore.model;

import revolhope.splanes.com.keystore.helpers.Firebase;

public class FireContentObj extends FireFolderObj {

    public String user;
    public String brief;
    public String content;
    public String enumLength;
    public String enumType;

    public FireContentObj(){
        this.owner = Firebase.OWNER;
    }

    public String getUser() {
        return user;
    }

    public String getBrief() {
        return brief;
    }

    public String getContent() {
        return content;
    }

    public String getEnumLength() {
        return enumLength;
    }

    public String getEnumType() {
        return enumType;
    }
}
