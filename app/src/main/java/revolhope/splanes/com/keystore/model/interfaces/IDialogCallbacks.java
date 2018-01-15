package revolhope.splanes.com.keystore.model.interfaces;

/**
 * Created by splanes on 3/1/18.
 */

public interface IDialogCallbacks {
    void onCreateFolder(String name);
    void onDropHeaderWrapper();
    void onRenameFolder(int id, String oldName);
    void onMovedElement();
    void onRenamedFolder(int id, String newName);
}
