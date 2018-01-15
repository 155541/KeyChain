package revolhope.splanes.com.keystore.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import revolhope.splanes.com.keystore.model.HeaderWrapper;
import revolhope.splanes.com.keystore.model.KeyData;

/**
 * Created by splanes on 3/1/18.
 **/

public class Database extends SQLiteOpenHelper {

    private Context context;

    private final static String DATABASE_NAME = "KChain";
    private static int DATABASE_VERSION = 1;

    private final static String TABLE_FOLDERS = "FOLDERS";
    private final static String TABLE_KEYS = "KEYS";
    private final static String TABLE_DEFAULT_OPTIONS = "DEFAULT_OPTIONS";

    // FOLDER'S COLUMNS
    private final static String FOLDER_ID = "FOLDER_ID";
    private final static String FOLDER_NAME = "FOLDER_NAME";
    private final static String FOLDER_PARENT = "FOLDER_PARENT";
    private final static String FOLDER_IV = "FOLDER_IV";

    // KEYS COLUMNS
    private final static String KEY_ID = "KEY_ID";
    private final static String KEY_NAME = "KEY_NAME";
    private final static String KEY_NAME_IV = "KEY_NAME_IV";
    private final static String KEY_BRIEF = "KEY_BRIEF";
    private final static String KEY_BRIEF_IV = "KEY_BRIEF_IV";
    private final static String KEY_USER = "KEY_USER";
    private final static String KEY_USER_IV = "KEY_USER_IV";
    private final static String KEY_KEY = "KEY_KEY";
    private final static String KEY_KEY_IV = "KEY_KEY_IV";
    private final static String KEY_ID_FOLDER = "KEY_ID_FOLDER";

    // DEFAULT OPTIONS COLUMNS
    private final static String DEFAULT_OPT_LENGTH = "DEFAULT_OPT_LENGTH";
    private final static String DEFAULT_OPT_TYPE = "DEFAULT_OPT_TYPE";

    // CREATE FOLDER TABLE
    private final static String CREATE_TABLE_FOLDERS =
            "CREATE TABLE " + TABLE_FOLDERS + "("
            + FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FOLDER_NAME + " BLOB UNIQUE,"
            + FOLDER_PARENT + " INTEGER DEFAULT 0,"
            + FOLDER_IV + " BLOB NOT NULL)";

    // CREATE KEYS TABLE
    private final static String CREATE_TABLE_KEYS =
            "CREATE TABLE " + TABLE_KEYS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_NAME + " BLOB UNIQUE,"
                    + KEY_NAME_IV + " BLOB NOT NULL,"
                    + KEY_USER + " BLOB DEFAULT NULL,"
                    + KEY_USER_IV + " BLOB DEFAULT NULL,"
                    + KEY_BRIEF + " BLOB DEFAULT NULL,"
                    + KEY_BRIEF_IV + " BLOB DEFAULT NULL,"
                    + KEY_KEY + " BLOB NOT NULL,"
                    + KEY_KEY_IV + " BLOB NOT NULL,"
                    + KEY_ID_FOLDER + " INTEGER)";

    // CREATE DEFAULT OPTIONS TABLE
    private final static String CREATE_TABLE_DEFAULT_OPTIONS =
            "CREATE TABLE " + TABLE_DEFAULT_OPTIONS + "("
                    + DEFAULT_OPT_LENGTH + " INTEGER DEFAULT 0,"
                    + DEFAULT_OPT_TYPE + " INTEGER DEFAULT -1)";


    public Database(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FOLDERS);
        db.execSQL(CREATE_TABLE_KEYS);
        db.execSQL(CREATE_TABLE_DEFAULT_OPTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_OPTIONS);

        DATABASE_VERSION = i1;
        onCreate(db);
    }

    /** $$$$ start folders $$$$ **/

    public boolean insertFolder(String name, int parent){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        Cryptography crypto = Cryptography.getInstance();
        boolean insert;


        Cryptography.CryptoObj cryptoObj = crypto.encrypt(name);
        if(cryptoObj != null) {

            values.put(FOLDER_NAME, cryptoObj.getRawData());
            values.put(FOLDER_PARENT, parent);
            values.put(FOLDER_IV, cryptoObj.getIv());
            insert = db.insert(TABLE_FOLDERS, null, values) != -1;

        }else insert = false;

        db.close();
        return insert;
    }

    public boolean insertFolder(){



        return false;
    }

    public ArrayList<HeaderWrapper> getHeaders(int parent){

        ArrayList<HeaderWrapper> dataset = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cryptography crypto = Cryptography.getInstance();

        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_PARENT + " = ?";
        try(Cursor c = db.rawQuery(query, new String[]{String.valueOf(parent)})){

            if(c.moveToFirst()) {
                do {
                    byte[] rawData = c.getBlob(c.getColumnIndex(FOLDER_NAME));
                    byte[] iv = c.getBlob(c.getColumnIndex(FOLDER_IV));

                    Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
                    cryptoObj.setRawData(rawData);
                    cryptoObj.setIv(iv);
                    String name = crypto.decrypt(cryptoObj);

                    if (name != null) {
                        HeaderWrapper header = new HeaderWrapper();
                        header.setId(c.getInt(c.getColumnIndex(FOLDER_ID)));
                        header.setName(name);
                        header.setParentId(c.getInt(c.getColumnIndex(FOLDER_PARENT)));
                        header.isFolder(true);
                        dataset.add(header);
                    }
                } while (c.moveToNext());
            }
        }

        query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_ID_FOLDER + " = ?";
        try(Cursor c = db.rawQuery(query, new String[]{String.valueOf(parent)})){

            if(!c.moveToFirst()){
                c.close();
                db.close();
                return dataset;
            }
            do{
                byte[] rawData = c.getBlob(c.getColumnIndex(KEY_NAME));
                byte[] iv = c.getBlob(c.getColumnIndex(KEY_NAME_IV));

                Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
                cryptoObj.setRawData(rawData);
                cryptoObj.setIv(iv);
                String name = crypto.decrypt(cryptoObj);
                if(name != null){
                    HeaderWrapper header = new HeaderWrapper();
                    header.setName(name);
                    header.isFolder(false);
                    header.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                    header.setParentId(c.getInt(c.getColumnIndex(KEY_ID_FOLDER)));
                    dataset.add(header);
                }
            }while(c.moveToNext());
        }

        db.close();
        return dataset;
    }

    public HeaderWrapper getHeader(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        Cryptography crypto = Cryptography.getInstance();

        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_ID + " = ?";
        try(Cursor c = db.rawQuery(query, new String[]{String.valueOf(id)})) {
            if(!c.moveToFirst() || c.getCount()!=1){
                c.close();
                db.close();
                return null;
            }
            byte[] rawData = c.getBlob(c.getColumnIndex(FOLDER_NAME));
            byte[] iv = c.getBlob(c.getColumnIndex(FOLDER_IV));

            Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
            cryptoObj.setRawData(rawData);
            cryptoObj.setIv(iv);
            String name = crypto.decrypt(cryptoObj);
            if(name != null){
                HeaderWrapper header = new HeaderWrapper();
                header.setId(id);
                header.setName(name);
                header.setParentId(c.getInt(c.getColumnIndex(FOLDER_PARENT)));
                header.isFolder(true);
                c.close();
                db.close();
                return header;
            }else{
                c.close();
                db.close();
                return null;
            }
        }
    }

    public boolean dropFolder(int id){


        dropRecursive(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOLDERS,FOLDER_ID + " = ? ", new String[]{String.valueOf(id)});
        db.delete(TABLE_KEYS,KEY_ID_FOLDER + " = ? ", new String[]{String.valueOf(id)});
        db.close();
        return true;
    }

    private void dropRecursive(int id){


        if(folderHasChild(id)){
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_PARENT + " = ?";
            try(Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})){
                if(cursor.moveToFirst()){
                    do{
                        dropRecursive(cursor.getInt(cursor.getColumnIndex(FOLDER_ID)));
                    }while(cursor.moveToNext());
                    db.delete(TABLE_FOLDERS,FOLDER_PARENT + " = ? ", new String[]{String.valueOf(id)});
                    db.delete(TABLE_KEYS,KEY_ID_FOLDER + " = ? ", new String[]{String.valueOf(id)});
                    db.close();
                }
            }

        }else{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_FOLDERS,FOLDER_PARENT + " = ? ", new String[]{String.valueOf(id)});
            db.delete(TABLE_KEYS,KEY_ID_FOLDER + " = ? ", new String[]{String.valueOf(id)});
            db.close();
        }
    }

    private boolean folderHasChild(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_PARENT + " = ?";

        try (Cursor c = db.rawQuery(query,new String[]{String.valueOf(id)})){

            if(c.moveToFirst()){
                c.close();
                db.close();
                return false;
            }
            boolean b = c.getCount() != 0;
            db.close();
            return b;
        }
    }

    public boolean updateFolderParent(int id, int newParent){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FOLDER_PARENT,newParent);
        boolean ok = db.update(TABLE_FOLDERS,values,FOLDER_ID + " = ? ",new String[]{String.valueOf(id)}) == 1;
        db.close();
        return ok;
    }

    public boolean updateFolderName(int id, String newName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Cryptography cryptography = Cryptography.getInstance();
        Cryptography.CryptoObj obj = cryptography.encrypt(newName);
        if(obj!=null){
            values.put(FOLDER_NAME,obj.getRawData());
            values.put(FOLDER_IV,obj.getIv());
            boolean ok = db.update(TABLE_FOLDERS,values,FOLDER_ID + " = ? ",new String[]{String.valueOf(id)}) == 1;
            db.close();
            return ok;
        }
        else{
            db.close();
            return false;
        }
    }

    boolean existsFolder(String encodedName){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_NAME + " LIKE '?'";
        try(Cursor c = db.rawQuery(query,new String[]{encodedName})){
            return c.getCount() != 0;
        }
    }

    /** $$$$ end folders $$$$ **/

    /** $$$$ start keys $$$$ **/

    public boolean insertKey(KeyData keyData){

        SQLiteDatabase db = this.getWritableDatabase();
        Cryptography crypto = Cryptography.getInstance();
        ContentValues values = new ContentValues();

        Cryptography.CryptoObj cryptoObj;
        cryptoObj = crypto.encrypt(keyData.getNameID());
        if (cryptoObj == null) { db.close(); return false; }
        values.put(KEY_NAME,cryptoObj.getRawData());
        values.put(KEY_NAME_IV,cryptoObj.getIv());

        cryptoObj = crypto.encrypt(keyData.getUser());
        if (cryptoObj == null) { db.close(); return false; }
        values.put(KEY_USER,cryptoObj.getRawData());
        values.put(KEY_USER_IV,cryptoObj.getIv());

        cryptoObj = crypto.encrypt(keyData.getBrief());
        if (cryptoObj == null) { db.close(); return false; }
        values.put(KEY_BRIEF,cryptoObj.getRawData());
        values.put(KEY_BRIEF_IV,cryptoObj.getIv());

        cryptoObj = crypto.encrypt(keyData.getKey());
        if (cryptoObj == null) { db.close(); return false; }
        values.put(KEY_KEY,cryptoObj.getRawData());
        values.put(KEY_KEY_IV,cryptoObj.getIv());

        values.put(KEY_ID_FOLDER,keyData.getIdParent());
        boolean result = db.insert(TABLE_KEYS,null,values) != -1;
        db.close();
        return result;
    }

    @Nullable
    public KeyData getKey(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_ID + " = ?";
        Cryptography crypto = Cryptography.getInstance();

        try( Cursor c = db.rawQuery(query,new String[]{String.valueOf(id)})){


            if(c.moveToFirst() && c.getCount() == 1){

                KeyData keyData = new KeyData();
                Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();

                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_NAME_IV)));
                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_NAME)));
                keyData.setNameID(crypto.decrypt(cryptoObj));

                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_USER_IV)));
                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_USER)));
                keyData.setUser(crypto.decrypt(cryptoObj));

                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_BRIEF_IV)));
                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_BRIEF)));
                keyData.setBrief(crypto.decrypt(cryptoObj));

                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_KEY_IV)));
                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_KEY)));
                keyData.setKey(crypto.decrypt(cryptoObj));

                keyData.setIdParent(c.getInt(c.getColumnIndex(KEY_ID_FOLDER)));
                keyData.setId(c.getInt(c.getColumnIndex(KEY_ID)));

                db.close();
                return keyData;

            }else{
                c.close();
                db.close();
                return null;
            }
        }

    }

    @Nullable
    public ArrayList<KeyData> getKeys(int parent){

        ArrayList<KeyData> keys = new ArrayList<>();
        Cryptography crypto = Cryptography.getInstance();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_ID_FOLDER + " = ?";

        try (Cursor c = db.rawQuery(query, new String[]{String.valueOf(parent)})){

            if(!c.moveToFirst()){
                c.close();
                db.close();
                return null;
            }
            Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
            do{

                KeyData keyData = new KeyData();

                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_NAME)));
                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_NAME_IV)));
                keyData.setNameID(crypto.decrypt(cryptoObj));

                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_USER)));
                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_USER_IV)));
                keyData.setUser(crypto.decrypt(cryptoObj));

                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_BRIEF)));
                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_BRIEF_IV)));
                keyData.setBrief(crypto.decrypt(cryptoObj));

                cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_KEY)));
                cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_KEY_IV)));
                keyData.setKey(crypto.decrypt(cryptoObj));

                keyData.setIdParent(c.getInt(c.getColumnIndex(KEY_ID_FOLDER)));

                keys.add(keyData);
            }while (c.moveToNext());
            return keys;
        }
    }

    public boolean updateKey(KeyData newValues){

        SQLiteDatabase db = this.getWritableDatabase();
        Cryptography crypto = Cryptography.getInstance();
        ContentValues values = new ContentValues();
        Cryptography.CryptoObj obj;

        obj = crypto.encrypt(newValues.getNameID());
        if(obj != null){
            values.put(KEY_NAME,obj.getRawData());
            values.put(KEY_NAME_IV,obj.getIv());
        }

        obj = crypto.encrypt(newValues.getUser());
        if(obj != null){
            values.put(KEY_USER,obj.getRawData());
            values.put(KEY_USER_IV,obj.getIv());
        }

        obj = crypto.encrypt(newValues.getBrief());
        if(obj != null){
            values.put(KEY_BRIEF,obj.getRawData());
            values.put(KEY_BRIEF_IV,obj.getIv());
        }

        obj = crypto.encrypt(newValues.getKey());
        if(obj != null){
            values.put(KEY_KEY,obj.getRawData());
            values.put(KEY_KEY_IV,obj.getIv());
        }
        values.put(KEY_ID_FOLDER,newValues.getIdParent());

        boolean reslt = db.update(TABLE_KEYS,values,KEY_ID + " = ?",new String[]{String.valueOf(newValues.getId())}) == 1;
        db.close();
        return reslt;
    }

    public boolean updateKeyParent(int id, int newParent){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID_FOLDER,newParent);
        boolean ok = db.update(TABLE_KEYS,values,KEY_ID + " = ? ",new String[]{String.valueOf(id)}) == 1;
        db.close();
        return ok;
    }

    public boolean dropKey(int id){

        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_KEYS,KEY_ID + " = ? ", new String[]{String.valueOf(id)}) == 1;
        db.close();
        return result;
    }

    /** $$$$ end keys $$$$ **/

    /** $$$$ start search keys & folders $$$$ **/

    public ArrayList<HeaderWrapper> findAll(String matchName){

        ArrayList<HeaderWrapper> dataset = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cryptography crypto = Cryptography.getInstance();

        String query = "SELECT * FROM " + TABLE_FOLDERS;
        try(Cursor c = db.rawQuery(query, null)){

            if(c.moveToFirst()) {
                do {
                    byte[] rawData = c.getBlob(c.getColumnIndex(FOLDER_NAME));
                    byte[] iv = c.getBlob(c.getColumnIndex(FOLDER_IV));

                    Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
                    cryptoObj.setRawData(rawData);
                    cryptoObj.setIv(iv);
                    String name = crypto.decrypt(cryptoObj);

                    if (name != null && name.toLowerCase().contains(matchName.toLowerCase())) {
                        HeaderWrapper header = new HeaderWrapper();
                        header.setId(c.getInt(c.getColumnIndex(FOLDER_ID)));
                        header.setName(name);
                        header.setParentId(c.getInt(c.getColumnIndex(FOLDER_PARENT)));
                        header.isFolder(true);
                        dataset.add(header);
                    }
                } while (c.moveToNext());
            }
        }

        query = "SELECT * FROM " + TABLE_KEYS;
        try(Cursor c = db.rawQuery(query, null)){

            if(!c.moveToFirst()){
                c.close();
                db.close();
                return dataset;
            }
            do{
                byte[] rawData = c.getBlob(c.getColumnIndex(KEY_NAME));
                byte[] iv = c.getBlob(c.getColumnIndex(KEY_NAME_IV));

                Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
                cryptoObj.setRawData(rawData);
                cryptoObj.setIv(iv);
                String name = crypto.decrypt(cryptoObj);
                if(name != null && name.toLowerCase().contains(matchName.toLowerCase())){
                    HeaderWrapper header = new HeaderWrapper();
                    header.setName(name);
                    header.isFolder(false);
                    header.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                    header.setParentId(c.getInt(c.getColumnIndex(KEY_ID_FOLDER)));
                    dataset.add(header);
                }
            }while(c.moveToNext());
        }

        db.close();
        return dataset;
    }

    /** $$$$ search keys & folders $$$$ **/

    public void log(){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS;
        Cryptography crypto = Cryptography.getInstance();

        try(Cursor c = db.rawQuery(query,null)){
            if(c.moveToFirst()){
                System.out.println(" ------- START FOLDERS: ENCRYPTED ------- ");
                do{
                    System.out.println(" ");
                    System.out.println(" ------- FOLDERS__id: " + c.getInt(c.getColumnIndex(FOLDER_ID)));
                    System.out.println(" ------- FOLDERS__name: " + c.getBlob(c.getColumnIndex(FOLDER_NAME)));
                    System.out.println(" ------- FOLDERS__parent: " + c.getInt(c.getColumnIndex(FOLDER_PARENT)));
                }while(c.moveToNext());
                System.out.println(" ");
                System.out.println(" ------- END FOLDERS: ENCRYPTED ------- ");
                System.out.println(" ");
            }
        }
        try(Cursor c = db.rawQuery(query,null)){
            if(c.moveToFirst()){
                System.out.println(" ------- START FOLDERS: DECRYPTED ------- ");
                do{

                    System.out.println(" ------- FOLDERS__id: " + c.getInt(c.getColumnIndex(FOLDER_ID)));

                    Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();
                    cryptoObj.setRawData(c.getBlob(c.getColumnIndex(FOLDER_NAME)));
                    cryptoObj.setIv(c.getBlob(c.getColumnIndex(FOLDER_IV)));

                    System.out.println(" ------- FOLDERS__name: " + crypto.decrypt(cryptoObj));
                    System.out.println(" ------- FOLDERS__parent: " + c.getInt(c.getColumnIndex(FOLDER_PARENT)));

                }while(c.moveToNext());
                System.out.println(" ------- END FOLDERS: DECRYPTED ------- ");
                System.out.println(" ");
            }
        }

        query = "SELECT * FROM " + TABLE_KEYS;

        try(Cursor c = db.rawQuery(query,null)){
            if(c.moveToFirst()){
                System.out.println(" ------- START KEYS: ENCRYPTED ------- ");
                do{
                    System.out.println(" ");
                    System.out.println(" ------- KEYS__ID: " + c.getInt(c.getColumnIndex(KEY_ID)));
                    System.out.println(" ------- KEYS__NAME_ID: " + Arrays.toString(c.getBlob(c.getColumnIndex(KEY_NAME))));
                    System.out.println(" ------- KEYS__USER: " + Arrays.toString(c.getBlob(c.getColumnIndex(KEY_USER))));
                    System.out.println(" ------- KEYS__BRIEF: " + Arrays.toString(c.getBlob(c.getColumnIndex(KEY_BRIEF))));
                    System.out.println(" ------- KEYS__KEY: " + Arrays.toString(c.getBlob(c.getColumnIndex(KEY_KEY))));
                    System.out.println(" ------- KEYS__PARENT: " + c.getInt(c.getColumnIndex(KEY_ID_FOLDER)));
                    System.out.println(" ------- { ALERT: NOT SHOWING IV } -------");
                }while(c.moveToNext());
                System.out.println(" ");
                System.out.println(" ------- END KEYS: ENCRYPTED ------- ");
                System.out.println(" ");
            }
        }
        try(Cursor c = db.rawQuery(query,null)){
            if(c.moveToFirst()){
                System.out.println(" ------- START KEYS: DECRYPTED ------- ");
                do{

                    Cryptography.CryptoObj cryptoObj = crypto.getNewCryptoObj();

                    System.out.println(" ------- KEYS__ID: " + c.getInt(c.getColumnIndex(KEY_ID)));
                    cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_NAME)));
                    cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_NAME_IV)));
                    System.out.println(" ------- KEYS__NAME_ID: " + crypto.decrypt(cryptoObj));
                    cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_USER)));
                    cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_USER_IV)));
                    System.out.println(" ------- KEYS__USER: " + crypto.decrypt(cryptoObj));
                    cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_BRIEF)));
                    cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_BRIEF_IV)));
                    System.out.println(" ------- KEYS__BRIEF: " + crypto.decrypt(cryptoObj));
                    cryptoObj.setRawData(c.getBlob(c.getColumnIndex(KEY_KEY)));
                    cryptoObj.setIv(c.getBlob(c.getColumnIndex(KEY_KEY_IV)));
                    System.out.println(" ------- KEYS__KEY: " + crypto.decrypt(cryptoObj));
                    System.out.println(" ------- KEYS__PARENT: " + c.getInt(c.getColumnIndex(KEY_ID_FOLDER)));
                    System.out.println(" ------- { ALERT: NOT SHOWING IV } -------");


                }while(c.moveToNext());
                System.out.println(" ------- END KEYS: DECRYPTED ------- ");
                System.out.println(" ");
            }
        }
        db.close();
    }

    public void drop(){
        this.close();
        context.deleteDatabase(DATABASE_NAME);
        Toast.makeText(context,"DB deleted",Toast.LENGTH_LONG).show();
    }
}
