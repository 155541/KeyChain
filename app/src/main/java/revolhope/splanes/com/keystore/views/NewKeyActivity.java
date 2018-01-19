package revolhope.splanes.com.keystore.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.helpers.Database;
import revolhope.splanes.com.keystore.helpers.Firebase;
import revolhope.splanes.com.keystore.helpers.KeyGenAsyncTask;
import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.enums.EnumLength;
import revolhope.splanes.com.keystore.model.enums.EnumType;
import revolhope.splanes.com.keystore.model.interfaces.IOnAsyncTaskComplete;
import revolhope.splanes.com.keystore.model.KeyMetadata;

import static revolhope.splanes.com.keystore.helpers.Firebase.ACTION_CREATE;

/**
 * Created by splanes on 7/1/18.
 **/

public class NewKeyActivity extends AppCompatActivity implements IOnAsyncTaskComplete{

    private int parentId;
    private IOnAsyncTaskComplete callback;
    private KeyMetadata keyMetadata;
    private EditText editTextIDName;
    private EditText editTextUser;
    private EditText editTextBrief;
    static final String lengths[] =
            {String.valueOf(8),String.valueOf(12),String.valueOf(16),String.valueOf(24),String.valueOf(32)};
    static final String types[] =
            {"Only chars", "Only numbers", "Only symbols", "Chars + numbers", "Chars + symbols","Numbers + symbols","Everything"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_key);

        parentId = getIntent().hasExtra("IdParent") ? getIntent().getIntExtra("IdParent",0) : 0;

        editTextIDName = (EditText) findViewById(R.id.editTextKeyNameId);
        editTextUser = (EditText) findViewById(R.id.editTextUser);
        editTextBrief = (EditText) findViewById(R.id.editTextBrief);
        Spinner spinnerLength = (Spinner) findViewById(R.id.spinnerLength);
        Spinner spinnerType = (Spinner) findViewById(R.id.spinnerType);
        Button buttonCreate = (Button) findViewById(R.id.buttonCreateKey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings create new key");
        }

        ArrayAdapter<String> adapterSpinnerLength = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,lengths);
        ArrayAdapter<String> adapterSpinnerType = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,types);

        spinnerLength.setAdapter(adapterSpinnerLength);
        spinnerType.setAdapter(adapterSpinnerType);

        keyMetadata = new KeyMetadata();
        this.callback = this;

        spinnerLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        keyMetadata.setLength(EnumLength.LENGTH_8);
                        break;
                    case 1:
                        keyMetadata.setLength(EnumLength.LENGTH_12);
                        break;
                    case 2:
                        keyMetadata.setLength(EnumLength.LENGTH_16);
                        break;
                    case 3:
                        keyMetadata.setLength(EnumLength.LENGTH_24);
                        break;
                    case 4:
                        keyMetadata.setLength(EnumLength.LENGTH_32);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { keyMetadata.setLength(null); }
        });

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        keyMetadata.setType(EnumType.ONLY_CHAR);
                        break;
                    case 1:
                        keyMetadata.setType(EnumType.ONLY_NUMBER);
                        break;
                    case 2:
                        keyMetadata.setType(EnumType.ONLY_SYMBOL);
                        break;
                    case 3:
                        keyMetadata.setType(EnumType.CHAR_NUMBER);
                        break;
                    case 4:
                        keyMetadata.setType(EnumType.CHAR_SYMBOL);
                        break;
                    case 5:
                        keyMetadata.setType(EnumType.NUMBER_SYMBOL);
                        break;
                    case 6:
                        keyMetadata.setType(EnumType.CHAR_NUMBER_SYMBOL);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { keyMetadata.setType(null); }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkFields()) {
                    KeyGenAsyncTask asyncTask = new KeyGenAsyncTask();
                    asyncTask.setCallback(callback);
                    asyncTask.execute(keyMetadata);
                }
            }
        });
    }

    @Override
    public void onKeyGenerated(String key) {

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String timestamp = "\nLast update: " + new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(c.getTime());

        Content content = new Content();
        content.setPlainName(editTextIDName.getText().toString());
        if(editTextUser.getText() != null) content.setPlainUser(editTextUser.getText().toString());
        if(editTextBrief.getText() != null) content.setPlainBrief(editTextBrief.getText().toString() + timestamp);
        content.setPlainKey(key);
        content.setEnumLength(keyMetadata.getLength());
        content.setEnumType(keyMetadata.getType());
        content.setParentId(parentId);


        Database database = new Database(this);
        String msg;
        if(database.insertKey(content)) msg = "Generated key: "+key;
        else msg = "Ouch.. Key cannot be stored in database";
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
        Firebase firebase = new Firebase(this);
        firebase.pushEntry(content, ACTION_CREATE, false);
        finish();
    }

    private boolean checkFields(){

        boolean ok = true;

        if(editTextIDName.getText().toString().equals("")) ok = false;
        if(keyMetadata.getLength() == null) ok = false;
        if(keyMetadata.getType() == null) ok = false;

        return ok;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return true;
    }
}
