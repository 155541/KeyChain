package revolhope.splanes.com.keystore.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
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

import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.FireContentObj;
import revolhope.splanes.com.keystore.model.FireFolderObj;
import revolhope.splanes.com.keystore.model.Folder;
import revolhope.splanes.com.keystore.model.Header;
import revolhope.splanes.com.keystore.model.interfaces.IFirebaseCallback;

/**
 * Created by splanes on 11/1/18.
 **/

public class Firebase {

    public static final int OWNER = 556;

    private static final String S_MAIL = "sr748XvXN5KR9MvBe@gmail.com";
    private static final String S_PASS = "s094XY2q862xxEb1X";

    private static final String ROOT = "root";
    private static final String FOLDERS = "folder";
    private static final String KEYS = "content";

    public static final int ACTION_CREATE = 0;
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_REMOVE = 2;

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

    public boolean pushEntry(Header header, int action, boolean isFolder){

        String dir = isFolder ? FOLDERS : KEYS;
        FireFolderObj fireObj;

        if(isFolder){

            fireObj = new FireFolderObj();
            fireObj.id = header.getId();
            fireObj.idParent = header.getParentId();
            fireObj.name = header.getPlainName(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
            fireObj.action = action;

        }else{

            Content content = (Content) header;
            fireObj = new FireContentObj();
            fireObj.id = content.getId();
            fireObj.idParent = content.getParentId();
            fireObj.name = content.getPlainName(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
            fireObj.action = action;

            ((FireContentObj)fireObj).brief = content.getPlainBrief(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
            ((FireContentObj)fireObj).user = content.getPlainUser(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
            ((FireContentObj)fireObj).content = content.getPlainKey(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
            ((FireContentObj)fireObj).enumLength = content.getEnumLength().toString(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
            ((FireContentObj)fireObj).enumType = content.getEnumType().toString(); // todo: ENCRYPT! AND USE BASE64 TO ENCODE
        }

        DatabaseReference dRef = databaseReference.child(ROOT).child(dir);
        Task<Void> pushTask = dRef.child(String.valueOf(fireObj.id)).setValue(fireObj);
        pushTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                success = task.isSuccessful();
            }
        });

        return success;

    }

    public void synchronize(){

        DatabaseReference refFolder = databaseReference.child(ROOT).child(FOLDERS);
        DatabaseReference refContent = databaseReference.child(ROOT).child(KEYS);

        refFolder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SynchronizeAsyncTask synchronizeAsyncTask = new SynchronizeAsyncTask();
                synchronizeAsyncTask.setDatabase(new Database(context));
                synchronizeAsyncTask.setIsFolder(true);
                synchronizeAsyncTask.setIFirebaseCallback(callback);
                synchronizeAsyncTask.execute(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        refContent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SynchronizeAsyncTask synchronizeAsyncTask = new SynchronizeAsyncTask();
                synchronizeAsyncTask.setDatabase(new Database(context));
                synchronizeAsyncTask.setIsFolder(false);
                synchronizeAsyncTask.setIFirebaseCallback(callback);
                synchronizeAsyncTask.execute(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void setCallback(IFirebaseCallback callback) { this.callback = callback; }

    /*
    * Toast.makeText(context, "Ouch.. we've got a problem during sync..\nWe coudn't sync all data", Toast.LENGTH_SHORT).show();
    */
}
