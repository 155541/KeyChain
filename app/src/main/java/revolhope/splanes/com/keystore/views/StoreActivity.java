package revolhope.splanes.com.keystore.views;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.controller.HeaderAdapter;
import revolhope.splanes.com.keystore.dialogs.DialogHeaderAction;
import revolhope.splanes.com.keystore.dialogs.DialogNewDir;
import revolhope.splanes.com.keystore.dialogs.DialogRenameFolder;
import revolhope.splanes.com.keystore.dialogs.DialogShowKey;
import revolhope.splanes.com.keystore.helpers.Database;
import revolhope.splanes.com.keystore.helpers.Firebase;
import revolhope.splanes.com.keystore.model.HeaderWrapper;
import revolhope.splanes.com.keystore.model.interfaces.IDialogCallbacks;
import revolhope.splanes.com.keystore.model.interfaces.IFirebaseCallback;
import revolhope.splanes.com.keystore.model.interfaces.IOnClickCallback;
import revolhope.splanes.com.keystore.model.KeyData;

/**
 * Created by splanes on 2/1/18.
 **/

public class StoreActivity extends AppCompatActivity implements IDialogCallbacks, IOnClickCallback, IFirebaseCallback{

    private IDialogCallbacks dialogCallbacks;
    private int parentID;
    private HeaderAdapter headerAdapter;
    private Database db;
    private HeaderWrapper selectedHeader;
    private Vibrator v;
    private Context context = this;

    private Firebase firebase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new Database(this);
        dialogCallbacks = this;
        parentID = 0;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        headerAdapter = new HeaderAdapter(this, this);
        headerAdapter.setDataset(db.getHeaders(parentID));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(headerAdapter);

        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        ImageView ivNewFolder = (ImageView) findViewById(R.id.imageViewNewFolder);
        ivNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(30, 100));
                } else {
                    v.vibrate(30);
                }
                DialogNewDir dialogNewDir = new DialogNewDir();
                dialogNewDir.setCallback(dialogCallbacks);
                dialogNewDir.show(getSupportFragmentManager(), "NewDirDialog");
            }
        });

        ImageView ivNewKey = (ImageView) findViewById(R.id.imageViewNewKey);
        ivNewKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(30, 100));
                } else {
                    v.vibrate(30);
                }

                Intent i = new Intent(getApplicationContext(), NewKeyActivity.class);
                i.putExtra("IdParent", parentID);
                startActivity(i);
            }
        });

        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<HeaderWrapper> searchedHeaders = db.findAll(query);
                if(searchedHeaders == null) Toast.makeText(context,"No keys/folders matched..",Toast.LENGTH_LONG).show();
                else{
                    headerAdapter.setDataset(searchedHeaders);
                    headerAdapter.notifyDataSetChanged();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                onResume();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(parentID!=0);
            if(selectedHeader != null){
                getSupportActionBar().setTitle("/"+selectedHeader.getName());
            }else{
                getSupportActionBar().setTitle("KeyChain");
            }
        }
        headerAdapter.setDataset(db.getHeaders(parentID));
        headerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateFolder(String name) {

        Database db = new Database(this);
        if(db.insertFolder(name,parentID)){
            headerAdapter.setDataset(db.getHeaders(parentID));
            headerAdapter.notifyDataSetChanged();
        }else{
            Toast.makeText(this, "Ouch..Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onHeaderHolderClick(HeaderWrapper object) {

        if(object.isFolder()) {
            selectedHeader = object;
            parentID = object.getId();
            onResume();
        }else{
            DialogShowKey dialogShowKey = new DialogShowKey();
            KeyData keyData = db.getKey(object.getId());
            dialogShowKey.setKeyData(keyData);
            dialogShowKey.show(getSupportFragmentManager(),"ShowKeyDialog");
        }
    }

    @Override
    public void onHeaderHolderLongClick(HeaderWrapper object) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, 100));
        }else{
            v.vibrate(50);
        }
        selectedHeader = object;
        DialogHeaderAction dialogHeaderAction = new DialogHeaderAction();
        dialogHeaderAction.setCallback(this);
        Bundle bundle = new Bundle();
        bundle.putInt("id",object.getId());
        bundle.putBoolean("isFolder",object.isFolder());
        bundle.putString("name",object.getName());
        dialogHeaderAction.setArguments(bundle);
        dialogHeaderAction.show(getSupportFragmentManager(),"DialogHeaderAction");
    }

    @Override
    public void onDropHeaderWrapper() {

        if(selectedHeader.isFolder()){

            db.dropFolder(selectedHeader.getId());

            onResume();

            if(parentID == 0) selectedHeader = null;
            else{
                selectedHeader = db.getHeader(parentID);
            }

        }else{
            db.dropKey(selectedHeader.getId());
            onResume();
        }
    }

    @Override
    public void onRenameFolder(int id, String oldName) {

        DialogRenameFolder dialogRenameFolder = new DialogRenameFolder();

        Bundle bundle = new Bundle();
        bundle.putInt("id",id);
        bundle.putString("oldName",oldName);
        dialogRenameFolder.setCallback(dialogCallbacks);
        dialogRenameFolder.setArguments(bundle);
        dialogRenameFolder.show(getSupportFragmentManager(),"RenameFolderDialog");

    }

    @Override
    public void onRenamedFolder(int id, String newName) {
        String msg =  db.updateFolderName(id,newName) ? "The name folder was updated correctly" : "Ouch.. Something went wrong.. try again";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        onResume();
    }

    @Override
    public void onMovedElement() {
        onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_store, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_log_database){
            db.log();
        }
        if(id == R.id.action_drop){
            db.drop();
        }
        if(id == android.R.id.home){

            if (selectedHeader != null )parentID = selectedHeader.getParentId();
            if(parentID == 0) selectedHeader = null;
            else{
                selectedHeader = db.getHeader(parentID);
            }
            onResume();
        }
        if(id == R.id.action_push_test){

            firebase = new Firebase(this);
            firebase.setCallback(this);
            firebase.signIn();
        }

        return true;
    }

    @Override
    public void onSignIn(boolean result) {

        /*
        FolderData folder = new FolderData();
        folder.setId(1);
        folder.setIdParent(0);
        folder.setToEncodeName(new byte[]{0xa});
        firebase.pushFolder(folder);
        */

        if(!result){
            // TODO: DIALOG-> INDICAR QUE NO HI HA CONN AMB FIREBASE I NO ES SYNC LES DADES
        }
        else{
            // TODO OBTENIR DATASET DE FIREBASE,
            firebase.synchronize();
        }
    }

    @Override
    public void onSynchronized(ArrayList<HeaderWrapper> finalDataset) {

    }
}
