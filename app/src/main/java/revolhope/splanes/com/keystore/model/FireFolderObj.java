package revolhope.splanes.com.keystore.model;

import revolhope.splanes.com.keystore.helpers.Firebase;

public class FireFolderObj{

    public int action;
    public String name;
    public int id;
    public int idParent;
    public int owner;

    public FireFolderObj(){
        this.owner = Firebase.OWNER;
    }
}
