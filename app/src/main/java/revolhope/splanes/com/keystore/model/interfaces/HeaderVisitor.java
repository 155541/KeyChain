package revolhope.splanes.com.keystore.model.interfaces;

import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.Folder;

/**
 * Created by splanes on 16/1/18.
 **/

public interface HeaderVisitor<T> {
    T isFolder(Folder folder);
    T isContent(Content content);
}
