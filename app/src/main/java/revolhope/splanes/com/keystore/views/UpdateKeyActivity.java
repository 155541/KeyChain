package revolhope.splanes.com.keystore.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.jetbrains.annotations.Contract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.helpers.Database;
import revolhope.splanes.com.keystore.helpers.KeyGenAsyncTask;
import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.enums.EnumLength;
import revolhope.splanes.com.keystore.model.enums.EnumType;
import revolhope.splanes.com.keystore.model.interfaces.IOnAsyncTaskComplete;
import revolhope.splanes.com.keystore.model.KeyMetadata;

import static revolhope.splanes.com.keystore.views.NewKeyActivity.lengths;
import static revolhope.splanes.com.keystore.views.NewKeyActivity.types;

/**
 * Created by splanes on 8/1/18.
 **/

public class UpdateKeyActivity extends AppCompatActivity implements IOnAsyncTaskComplete {

    private Content oldContent;
    private EditText editText_Name;
    private EditText editText_User;
    private EditText editText_Brief;
    private IOnAsyncTaskComplete callback;
    private KeyMetadata keyMetadata;
    private boolean updatingKey;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_key);

        editText_Name = findViewById(R.id.editTextKeyNameId);
        editText_User = findViewById(R.id.editTextUser);
        editText_Brief = findViewById(R.id.editTextBrief);
        EditText editText_Key = findViewById(R.id.editText_key);

        if(getIntent().hasExtra("oldContent")){

            oldContent = (Content) Objects.requireNonNull(getIntent().getExtras()).getSerializable("oldContent");

            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle("Update " + oldContent.getPlainName() + " entry");
                getSupportActionBar().setSubtitle("You can update fields and/or the key");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            editText_Name.setText(oldContent.getPlainName());
            editText_User.setText(oldContent.getPlainUser());
            editText_Brief.setText(oldContent.getPlainBrief());
            editText_Key.setText(oldContent.getPlainKey());

            Spinner spinnerLength = findViewById(R.id.spinnerLength);
            Spinner spinnerType = findViewById(R.id.spinnerType);
            ArrayAdapter<String> adapterSpinnerLength = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,lengths);
            ArrayAdapter<String> adapterSpinnerType = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,types);
            spinnerLength.setAdapter(adapterSpinnerLength);
            spinnerType.setAdapter(adapterSpinnerType);

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

            spinnerLength.setSelection(getLengthIndex(oldContent.getEnumLength()));
            spinnerType.setSelection(getTypeIndex(oldContent.getEnumType()));

            keyMetadata = new KeyMetadata();
            this.callback = this;

            findViewById(R.id.buttonUpdateFields).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatingKey = false;
                    onKeyGenerated(oldContent.getPlainKey());
                }
            });

            findViewById(R.id.buttonUpdateKey).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkFields()) {
                        updatingKey = true;
                        KeyGenAsyncTask asyncTask = new KeyGenAsyncTask();
                        asyncTask.setCallback(callback);
                        asyncTask.execute(keyMetadata);
                    }
                }
            });


        }else{
            Toast.makeText(this,"Ouch.. Something went wrong", Toast.LENGTH_LONG).show();
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle("Error...");
                getSupportActionBar().setSubtitle("Try to go back and try it again");
            }
        }
    }

    @Override
    public void onKeyGenerated(String key) {

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        String timestamp = "\nLast update: " + new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(c.getTime());

        String brief = editText_Brief.getText().toString();
        String[] parts = brief.split("\nLast update: ");
        brief = parts[0] + timestamp;

        Content content = new Content();
        content.setId(oldContent.getId());
        content.setPlainName(editText_Name.getText().toString());
        if(editText_User.getText() != null) content.setPlainUser(editText_User.getText().toString());
        if(editText_Brief.getText() != null) content.setPlainBrief(brief);
        content.setPlainKey(key);
        content.setEnumType(keyMetadata.getType());
        content.setEnumLength(keyMetadata.getLength());
        content.setParentId(oldContent.getParentId());

        Database database = new Database(this);
        String msg;
        if(database.updateKey(content)) msg = updatingKey ? "Updated key, new value: "+key : "Fields have been updated";
        else msg = "Ouch.. Key cannot be stored in database";
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean checkFields(){

        boolean ok = true;

        if(editText_Name.getText().toString().equals("")) ok = false;
        if(keyMetadata.getLength() == null) ok = false;
        if(keyMetadata.getType() == null) ok = false;

        return ok;
    }

    @Contract(pure = true)
    private int getLengthIndex(EnumLength length){
        switch (length){
            case LENGTH_8:
                return 0;
            case LENGTH_12:
                return 1;
            case LENGTH_16:
                return 2;
            case LENGTH_24:
                return 3;
            case LENGTH_32:
                return 4;
            default: return 0;
        }
    }

    @Contract(pure = true)
    private int getTypeIndex(EnumType type){
        switch (type){
            case ONLY_CHAR:
                return 0;
            case ONLY_NUMBER:
                return 1;
            case ONLY_SYMBOL:
                return 2;
            case CHAR_NUMBER:
                return 3;
            case CHAR_SYMBOL:
                return 4;
            case NUMBER_SYMBOL:
                return 5;
            case CHAR_NUMBER_SYMBOL:
                return 6;
            default: return 0;
        }
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
