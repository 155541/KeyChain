package revolhope.splanes.com.keystore.helpers;


import android.os.AsyncTask;
import com.google.firebase.database.DataSnapshot;
import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.FireContentObj;
import revolhope.splanes.com.keystore.model.FireFolderObj;
import revolhope.splanes.com.keystore.model.Folder;
import revolhope.splanes.com.keystore.model.interfaces.IFirebaseCallback;

import static revolhope.splanes.com.keystore.helpers.Firebase.ACTION_CREATE;
import static revolhope.splanes.com.keystore.helpers.Firebase.ACTION_REMOVE;
import static revolhope.splanes.com.keystore.helpers.Firebase.ACTION_UPDATE;
import static revolhope.splanes.com.keystore.helpers.Firebase.OWNER;


public class SynchronizeAsyncTask extends AsyncTask<DataSnapshot,Integer,Boolean> {

    private Database database;
    private boolean isFolder;
    private IFirebaseCallback callback;

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        callback.onSynchronized(aBoolean);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Boolean doInBackground(DataSnapshot... dataSnapshots) {


        boolean result = true;

        for(DataSnapshot data : dataSnapshots[0].getChildren()) {

            try {

                FireFolderObj fireObj =
                        isFolder ?
                        data.getValue(FireFolderObj.class) : data.getValue(FireContentObj.class);

                if(fireObj != null && fireObj.owner != OWNER){

                    switch (fireObj.action){
                        case ACTION_CREATE:

                            if(isFolder) {
                                if (!database.existsFolder(fireObj.id)) {
                                    Folder folder = new Folder();
                                    folder.setId(fireObj.id);
                                    folder.setPlainName(fireObj.name); // todo: parse to byte[] with base64 + decrypt it
                                    folder.setParentId(fireObj.idParent);

                                    if (!database.insertFolder(folder)) result = false;
                                }
                            }else{
                                if (!database.existsKey(fireObj.id)) {
                                    FireContentObj contentObj = (FireContentObj)fireObj;
                                    Content content = new Content();
                                    content.setId(contentObj.id);
                                    content.setPlainName(contentObj.name); // todo: parse to byte[] with base64 + decrypt it
                                    content.setPlainUser(contentObj.getUser()); // todo: parse to byte[] with base64 + decrypt it
                                    content.setPlainBrief(contentObj.getBrief()); // todo: parse to byte[] with base64 + decrypt it
                                    content.setPlainKey(contentObj.getContent()); // todo: parse to byte[] with base64 + decrypt it
                                    //content.setEnumLength(contentObj.getEnumLength()); // todo: parse String from contentObj to EnumLength
                                    //content.setEnumType(contentObj.getEnumType()); // todo:  parse String from contentObj to EnumType
                                    content.setParentId(contentObj.idParent);

                                    if (!database.insertKey(content)) result = false;
                                }
                            }
                            break;
                        case ACTION_UPDATE:
                            if(isFolder) {
                                if (database.existsFolder(fireObj.id)) {
                                    Folder folder = new Folder();
                                    folder.setId(fireObj.id);
                                    folder.setPlainName(fireObj.name); // todo: parse to byte[] with base64 + decrypt it
                                    folder.setParentId(fireObj.idParent);

                                    if (!database.updateFolder(folder)) result = false;
                                }
                            }else {
                                if (!database.existsKey(fireObj.id)) {
                                    FireContentObj contentObj = (FireContentObj) fireObj;
                                    Content content = new Content();
                                    content.setId(contentObj.id);
                                    content.setPlainName(contentObj.name); // todo: parse to byte[] with base64 + decrypt it
                                    content.setPlainUser(contentObj.getUser()); // todo: parse to byte[] with base64 + decrypt it
                                    content.setPlainBrief(contentObj.getBrief()); // todo: parse to byte[] with base64 + decrypt it
                                    content.setPlainKey(contentObj.getContent()); // todo: parse to byte[] with base64 + decrypt it
                                    //content.setEnumLength(contentObj.getEnumLength()); // todo: parse String from contentObj to EnumLength
                                    //content.setEnumType(contentObj.getEnumType()); // todo:  parse String from contentObj to EnumType
                                    content.setParentId(contentObj.idParent);

                                    if (!database.updateKey(content)) result = false;
                                }
                            }
                            break;
                        case ACTION_REMOVE:
                            if(isFolder) {
                                if (database.existsFolder(fireObj.id)) {
                                    Folder folder = new Folder();
                                    folder.setId(fireObj.id);
                                    folder.setPlainName(fireObj.name); // todo: parse to byte[] with base64 + decrypt it
                                    folder.setParentId(fireObj.idParent);

                                    if (!database.removeFolder(folder)) result = false;
                                }
                            }else if(!database.removeKey(fireObj.id)) result = false;
                            break;
                    }
                }
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }


        return result ? Boolean.TRUE : Boolean.FALSE;
    }

    void setDatabase(Database database) { this.database = database; }

    void setIsFolder(boolean isFolder) { this.isFolder = isFolder; }

    void setIFirebaseCallback(IFirebaseCallback callback) { this.callback = callback; }
}
