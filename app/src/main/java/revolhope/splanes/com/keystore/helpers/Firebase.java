package revolhope.splanes.com.keystore.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import revolhope.splanes.com.keystore.model.FolderData;
import revolhope.splanes.com.keystore.model.KeyData;
import revolhope.splanes.com.keystore.model.interfaces.IFirebaseCallback;

/**
 * Created by splanes on 11/1/18.
 **/

public class Firebase {

    private static final int OWNER = 556;

    private static final String S_MAIL = "sr748XvXN5KR9MvBe@gmail.com";
    private static final String S_PASS = "s094XY2q862xxEb1X";

    private static final String ROOT = "root";
    private static final String FOLDERS = "folder";
    private static final String KEYS = "content";

    private static final int ACTION_CREATE = 0;
    private static final int ACTION_UPDATE = 1;
    private static final int ACTION_REMOVE = 2;

    private Context context;
    private FirebaseAuth mAuth;
    private boolean success;
    private FirebaseUser mUser;
    private DatabaseReference databaseReference;
    private IFirebaseCallback callback;

    public Firebase(Context context){
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public boolean createUser(){
        mAuth.createUserWithEmailAndPassword(S_MAIL,S_PASS)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            success = true;
                        } else {
                            Toast.makeText(context, "User creation failed",Toast.LENGTH_SHORT).show();
                            success = false;
                        }
                    }
                });
        return success;
    }

    public boolean deleteUser(){

        mUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                success = task.isComplete();
            }
        });
        return success;
    }

    public void signIn(){

        mAuth.signInWithEmailAndPassword(S_MAIL,S_PASS)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            success = true;
                            callback.onSignIn(true);
                        } else {
                            Toast.makeText(context, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            success = false;
                            callback.onSignIn(false);
                        }
                    }
                });
    }

    public void signOut(){
        mAuth.signOut();
    }

    public boolean pushKey(KeyData keyData){

        DatabaseReference dRef = databaseReference.child(ROOT).child(KEYS);
        Task<Void> pushTask = dRef.child(String.valueOf(keyData.getId())).setValue(keyData);
        pushTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                success = task.isSuccessful();
            }
        });

        return success;
    }

    public boolean pushFolder(FolderData folder){

        DatabaseReference dRef = databaseReference.child(ROOT).child(FOLDERS);
        Task<Void> pushTask = dRef.child(String.valueOf(folder.getId())).setValue(folder);
        pushTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                success = task.isSuccessful();
            }
        });

        return success;
    }

    public boolean updateKey(){


        return success;
    }

    public boolean updateFolder(){

        return success;
    }

    public boolean dropKey(){

        return success;
    }

    public boolean dropFolder(){

        return success;
    }

    public void synchronize(){

        final Database database = new Database(context);
        DatabaseReference refFolder = databaseReference.child(ROOT).child(FOLDERS);
        DatabaseReference refContent = databaseReference.child(ROOT).child(KEYS);

        refFolder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot data : dataSnapshot.getChildren()) {

                    try {
                        int folderId = Integer.parseInt(data.getKey());
                        FirebaseFolderObj folderObj = (FirebaseFolderObj)data.getValue();

                        if(folderObj == null) return;
                        if(folderObj.getOwner() != OWNER){

                            boolean exists = database.existsFolder(folderObj.getEncodedName());
                            switch (folderObj.getAction()){
                                case ACTION_CREATE:
                                    
                                    if(!exists){

                                        
                                        database.insertFolder(); // todo: Implement method
                                    }
                                    break;
                                case ACTION_UPDATE:
                                    
                                    if(exists){
                                        database.updateFolder(); // todo: Implement method
                                    }
                                    break;
                                case ACTION_REMOVE:
                                    
                                    if(exists){
                                        database.removeFolder(folderId); // todo: Implement method
                                    }
                                    break;
                            }
                            
                            // todo: remove entry in firebase
                            //data.getRef().removeValue();  //???? ->> check in ChappCript
                        }


                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        refContent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                
                for(DataSnapshot data : dataSnapshot.getChildren()) {

                    try {
                        int contentId = Integer.parseInt(data.getKey());
                        FirebaseContentObj contentObj = (FirebaseContentObj)data.getValue();
                        
                        if(contentObj.getOwner() != OWNER){
                            switch (contentObj.getAction()){
                                case ACTION_CREATE:
                                    
                                    if(!database.existsKey(contentId)){
                                        database.insertKey(); // todo: Implement method
                                    }
                                    break;
                                case ACTION_UPDATE:
                                    
                                    if(database.existsKey(contentId)){
                                        database.updateKey(); // todo: Implement method
                                    }
                                    break;
                                case ACTION_REMOVE:
                                    
                                    if(database.existsKey(contentId)){
                                        database.removeKey(contentId);
                                    }
                                    break;
                            }
                            
                            // todo: remove entry in firebase
                            // data.getRef().removeValue()  ???? ->> check in ChappCript
                        }


                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void setCallback(IFirebaseCallback callback) { this.callback = callback; }

    private class FirebaseFolderObj{

        private String encodedName;
        private int idParent;
        private int id;
        private int action;
        private int owner;

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
        public void setAction(int action) { this.action = action; }
        public int getAction() { return this.action; }
        public int getOwner() {
            return owner;
        }
        public void setOwner(int owner) {
            this.owner = owner;
        }
    }

    private class FirebaseContentObj{
        private int owner;
        private int action;

        public int getOwner() {
            return owner;
        }
        public void setOwner(int owner) {
            this.owner = owner;
        }
        public void setAction(int action) { this.action = action; }
        public int getAction() { return this.action; }
    }
}
