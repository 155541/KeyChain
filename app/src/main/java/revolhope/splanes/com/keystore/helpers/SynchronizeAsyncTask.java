package revolhope.splanes.com.keystore.helpers;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import revolhope.splanes.com.keystore.model.FolderData;
import revolhope.splanes.com.keystore.model.HeaderWrapper;
import revolhope.splanes.com.keystore.model.interfaces.IFirebaseCallback;

/**
 * Created by splanes on 12/1/18.
 **/

class SynchronizeAsyncTask extends AsyncTask<SynchronizeAsyncTask.Wrapper,Long,Boolean> {

    private IFirebaseCallback callback;

    @Override
    protected Boolean doInBackground(SynchronizeAsyncTask.Wrapper[] objects) {

        ArrayList<HeaderWrapper> finalDataset = new ArrayList<>();


        final Database database = new Database(objects[0].getContext());
        DatabaseReference refFolders = objects[0].getRefFolder();
        DatabaseReference refKeys = objects[0].getRefContent();

        refFolders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot data : dataSnapshot.getChildren()){

                    try{
                        int folderId = Integer.parseInt(data.getKey());
                        FolderData folder = (FolderData)data.getValue();



                    }catch (NumberFormatException e){
                        e.printStackTrace();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        refKeys.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });





        return Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean sync) {

    }

    public void setCallback(IFirebaseCallback callback) { this.callback = callback; }

    Wrapper getWrapper(){
        return new Wrapper();
    }

    class Wrapper{

        private Context context;
        private DatabaseReference refFolder;
        private DatabaseReference refContent;

        public Context getContext() {
            return context;
        }
        public void setContext(Context context) {
            this.context = context;
        }
        DatabaseReference getRefFolder() {
            return refFolder;
        }
        void setRefFolder(DatabaseReference refFolder) {
            this.refFolder = refFolder;
        }
        DatabaseReference getRefContent() {
            return refContent;
        }
        void setRefContent(DatabaseReference refContent) {
            this.refContent = refContent;
        }

    }
}
