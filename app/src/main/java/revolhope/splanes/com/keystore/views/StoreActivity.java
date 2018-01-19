package revolhope.splanes.com.keystore.views;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
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

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.controller.HeaderAdapter;
import revolhope.splanes.com.keystore.dialogs.DialogHeaderAction;
import revolhope.splanes.com.keystore.dialogs.DialogNewDir;
import revolhope.splanes.com.keystore.dialogs.DialogRenameFolder;
import revolhope.splanes.com.keystore.dialogs.DialogShowKey;
import revolhope.splanes.com.keystore.helpers.Database;
import revolhope.splanes.com.keystore.helpers.Firebase;
import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.Folder;
import revolhope.splanes.com.keystore.model.Header;
import revolhope.splanes.com.keystore.model.interfaces.HeaderVisitor;
import revolhope.splanes.com.keystore.model.interfaces.IDialogCallbacks;
import revolhope.splanes.com.keystore.model.interfaces.IFirebaseCallback;
import revolhope.splanes.com.keystore.model.interfaces.IOnClickCallback;

/**
 * Created by splanes on 2/1/18.
 **/

public class StoreActivity extends AppCompatActivity implements IDialogCallbacks, IOnClickCallback, IFirebaseCallback{

    private IDialogCallbacks dialogCallbacks;
    private int parentID;
    private HeaderAdapter headerAdapter;
    private Database db;
    private Header selectedHeader;
    private Vibrator v;
    private Context context = this;

    private Firebase firebase;

    private HeaderVisitor<Boolean> headerVisitorFolder = new HeaderVisitor<Boolean>() {
        @Contract(pure = true)
        @Override
        public Boolean isFolder(Folder folder) {
            return Boolean.TRUE;
        }

        @Contract(pure = true)
        @Override
        public Boolean isContent(Content content) {
            return Boolean.FALSE;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new Database(this);
        dialogCallbacks = this;
        parentID = 0;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        headerAdapter = new HeaderAdapter(this, this);
        headerAdapter.setDataSet(db.selectHeaders(parentID));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(headerAdapter);

        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        ImageView ivNewFolder = findViewById(R.id.imageViewNewFolder);
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

        ImageView ivNewKey = findViewById(R.id.imageViewNewKey);
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


        final SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Header> searchedHeaders = db.findAll(query);
                if(searchedHeaders == null) Toast.makeText(context,"No keys/folders matched..",Toast.LENGTH_LONG).show();
                else{
                    headerAdapter.setDataSet(searchedHeaders);
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

        firebase = new Firebase(context);
        firebase.setCallback(this);
        firebase.signIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(parentID!=0);
            if(selectedHeader != null){
                getSupportActionBar().setTitle("/"+selectedHeader.getPlainName());
            }else{
                getSupportActionBar().setTitle("KeyChain");
            }
        }
        headerAdapter.setDataSet(db.selectHeaders(parentID));
        headerAdapter.notifyDataSetChanged();
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$    CREATE FOLDER    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    @Override
    public void onCreateFolder(String name) {

        Database db = new Database(this);
        Folder folder = new Folder();
        folder.setParentId(parentID);
        folder.setPlainName(name);
        if(db.insertFolder(folder)){
            headerAdapter.setDataSet(db.selectHeaders(parentID));
            headerAdapter.notifyDataSetChanged();
        }else{
            Toast.makeText(this, "Ouch..Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$    HEADER'S ON CLICK     $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    @Override
    public void onHeaderHolderClick(Header object) {

        boolean isFolder = object.checkClass(headerVisitorFolder);

        if(isFolder) {
            selectedHeader = object;
            parentID = object.getId();
            onResume();
        }else{
            DialogShowKey dialogShowKey = new DialogShowKey();
            Content content = db.selectKey(object.getId());
            dialogShowKey.setKeyData(content);
            dialogShowKey.show(getSupportFragmentManager(),"ShowKeyDialog");
        }
    }

    @Override
    public void onHeaderHolderLongClick(Header object) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, 100));
        }else{
            v.vibrate(50);
        }
        selectedHeader = object;
        parentID = object.getParentId();
        DialogHeaderAction dialogHeaderAction = new DialogHeaderAction();
        dialogHeaderAction.setCallback(this);
        Bundle bundle = new Bundle();
        bundle.putInt("id",object.getId());
        bundle.putBoolean("isFolder",object.checkClass(headerVisitorFolder));
        bundle.putString("name",object.getPlainName());
        
        dialogHeaderAction.setArguments(bundle);
        dialogHeaderAction.show(getSupportFragmentManager(),"DialogHeaderAction");
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    DROP HEADER    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    @Override
    public void onDropHeaderWrapper() {

        if(selectedHeader.checkClass(headerVisitorFolder)){

            db.removeFolder((Folder)selectedHeader);
            onResume();

            if(parentID == 0) selectedHeader = null;
            else{
                selectedHeader = db.selectHeader(parentID);
            }
        }else{
            if(!db.removeKey(selectedHeader.getId()))
                Toast.makeText(this,"Ouch.. we've got some problems while dropping", Toast.LENGTH_SHORT).show();
            onResume();
        }
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$    RENAME FOLDER    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    @Override
    public void onRenameFolder(int id, String oldName) {

        DialogRenameFolder dialogRenameFolder = new DialogRenameFolder();

        Bundle bundle = new Bundle();
        bundle.putInt("id",id);
        bundle.putString("oldName",oldName);
        bundle.putInt("idParent",parentID);
        dialogRenameFolder.setCallback(dialogCallbacks);
        dialogRenameFolder.setArguments(bundle);
        dialogRenameFolder.show(getSupportFragmentManager(),"RenameFolderDialog");

    }

    @Override
    public void onRenamedFolder(Folder folder) {

        String msg =  db.updateFolder(folder) ? "The name folder was updated correctly" : "Ouch.. Something went wrong.. try again";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        onResume();
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    MOVE HEADER    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    @Override
    public void onMovedElement() {
        onResume();
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    MENU    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

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

            if (selectedHeader != null ) parentID = selectedHeader.getParentId();
            
            if(parentID == 0) selectedHeader = null;
            else{
                selectedHeader = db.selectHeader(parentID);
            }
            onResume();
        }
        if(id == R.id.action_push_test){
        }

        return true;
    }


    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    FIREBASE  //todo: to implement  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    @Override
    public void onSignIn(boolean result) {
        if(!result){
            onSynchronized(false);
        }
        else{
            Snackbar.make(findViewById(R.id.root), "Starting sync", BaseTransientBottomBar.LENGTH_SHORT)
                    .show();
            firebase.synchronize();
        }
    }

    @Override
    public void onSynchronized(Boolean sync) {

        String msg = sync ?
                "All changes have been synchronized. All up to date ;)" :
                "Ouch.. We've got some problems during synchronization.\nData is not sync., try later";
        Snackbar.make(findViewById(R.id.root), msg, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {}
                })
                .show();
    }
}
