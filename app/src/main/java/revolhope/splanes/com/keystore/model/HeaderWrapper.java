package revolhope.splanes.com.keystore.model;

/**
 * Created by splanes on 3/1/18.
 **/

public class HeaderWrapper{

    private int id;
    private String name;
    private int parentId;
    private boolean isFolder;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName(){ return this.name;}
    public void setName(String name) { this.name = name; }
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) {this.parentId = parentId; }
    public boolean isFolder() { return this.isFolder; }
    public void isFolder(boolean isFolder) { this.isFolder = isFolder; }
}
