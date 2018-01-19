package revolhope.splanes.com.keystore.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.Folder;
import revolhope.splanes.com.keystore.model.Header;

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
    private final static String FOLDER_PARENT = "FOLDER_PARENT";
    private final static String FOLDER_OBJ = "FOLDER_OBJ";
    private final static String FOLDER_WRAPPED = "FOLDER_WRAPPED";
    private final static String FOLDER_IV = "FOLDER_IV";

    // KEYS COLUMNS
    private final static String KEY_ID = "KEY_ID";
    private final static String KEY_PARENT = "KEY_PARENT";
    private final static String KEY_OBJ = "KEY_OBJ";
    private final static String KEY_WRAPPED = "KEY_WRAPPED";
    private final static String KEY_IV = "KEY_IV";

    // DEFAULT OPTIONS COLUMNS
    private final static String DEFAULT_OPT_LENGTH = "DEFAULT_OPT_LENGTH";
    private final static String DEFAULT_OPT_TYPE = "DEFAULT_OPT_TYPE";

    // CREATE FOLDER TABLE
    private final static String CREATE_TABLE_FOLDERS =
            "CREATE TABLE " + TABLE_FOLDERS + "("
                    + FOLDER_ID + " INTEGER PRIMARY KEY,"
                    + FOLDER_PARENT + " INTEGER NOT NULL,"
                    + FOLDER_OBJ + " BLOB UNIQUE,"
                    + FOLDER_WRAPPED + " BLOB NOT NULL,"
                    + FOLDER_IV + " BLOB NOT NULL)";

    // CREATE KEYS TABLE
    private final static String CREATE_TABLE_KEYS =
            "CREATE TABLE " + TABLE_KEYS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_PARENT + " INTEGER NOT NULL,"
                    + KEY_OBJ + " BLOB UNIQUE,"
                    + KEY_WRAPPED + " BLOB NOT NULL,"
                    + KEY_IV + " BLOB NOT NULL)";

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


    /*
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$    FOLDERS  & HEADERS   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     */

    public boolean insertFolder(Folder folder){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Cryptography cryptography = Cryptography.getInstance();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {

            folder.setId(generateId());
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(folder);
            os.close();
            Cryptography.CryptoObj cryptoObj = cryptography.encrypt(bos.toByteArray());
            bos.close();

            if(cryptoObj == null){
                db.close();
                return false;
            }

            values.put(FOLDER_ID,folder.getId());
            values.put(FOLDER_OBJ,cryptoObj.getData());
            values.put(FOLDER_WRAPPED, cryptoObj.getWrap());
            values.put(FOLDER_IV, cryptoObj.getIv());
            values.put(FOLDER_PARENT,folder.getParentId());

            boolean result = db.insert(TABLE_FOLDERS,null,values) != -1;
            db.close();
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }

    public boolean updateFolder(Folder folder){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Cryptography cryptography = Cryptography.getInstance();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {

            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(folder);
            os.close();
            Cryptography.CryptoObj cryptoObj = cryptography.encrypt(bos.toByteArray());
            bos.close();

            if(cryptoObj == null){
                db.close();
                return false;
            }

            values.put(FOLDER_ID,folder.getId());
            values.put(FOLDER_OBJ,cryptoObj.getData());
            values.put(FOLDER_WRAPPED, cryptoObj.getWrap());
            values.put(FOLDER_IV, cryptoObj.getIv());
            values.put(FOLDER_PARENT, folder.getParentId());

            boolean result = db.update(TABLE_FOLDERS,values,FOLDER_ID + " = ?", new String[]{ String.valueOf(folder.getId())}) == 1;
            db.close();
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }

    public boolean removeFolder(Folder folder){

        removeRecursive(folder.getId());
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOLDERS,FOLDER_ID + " = ? ", new String[]{String.valueOf(folder.getId())});
        db.delete(TABLE_KEYS,KEY_PARENT + " = ? ", new String[]{String.valueOf(folder.getId())});
        db.close();
        return true;

    }

    private void removeRecursive(int id){

        if(folderHasChild(id)){
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_PARENT + " = ?";
            try(Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})){
                if(cursor.moveToFirst()){
                    do{
                        removeRecursive(cursor.getInt(cursor.getColumnIndex(FOLDER_ID)));
                    }while(cursor.moveToNext());
                    db.delete(TABLE_FOLDERS,FOLDER_PARENT + " = ? ", new String[]{String.valueOf(id)});
                    db.delete(TABLE_KEYS,KEY_PARENT + " = ? ", new String[]{String.valueOf(id)});
                    db.close();
                }
            }

        }else{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_FOLDERS,FOLDER_PARENT + " = ? ", new String[]{String.valueOf(id)});
            db.delete(TABLE_KEYS,KEY_PARENT + " = ? ", new String[]{String.valueOf(id)});
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

    private int generateId(){

        boolean existsIdF;
        boolean existsIdK;
        int rand;

        do{
            rand = ThreadLocalRandom.current().nextInt();

            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_ID + " = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(rand)})){
                existsIdF = cursor.getCount() != 0;
            }
            query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_ID + " = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(rand)})){
                existsIdK = cursor.getCount() != 0;
            }

        } while(existsIdF || existsIdK);

        return rand;
    }

    @Nullable
    public ArrayList<Header> selectHeaders(int parent){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_PARENT + " = ?";
        Cryptography cryptography = Cryptography.getInstance();
        ArrayList<Header> result = new ArrayList<>();

        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(parent)})){

            if(cursor.moveToFirst()){
                do{
                    Cryptography.CryptoObj cryptoObj = cryptography.getCryptoObjInstance();
                    cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(FOLDER_OBJ)));
                    cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(FOLDER_WRAPPED)));
                    cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(FOLDER_IV)));

                    byte[] rawObj = cryptography.decrypt(cryptoObj);
                    if(rawObj == null) return null;
                    ByteArrayInputStream bis = new ByteArrayInputStream(rawObj);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    bis.close();
                    Folder folder = (Folder) ois.readObject();
                    ois.close();
                    result.add(folder);
                }while (cursor.moveToNext());
            }

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_PARENT + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(parent)})){

            if(!cursor.moveToFirst()) {
                db.close();
                return result;
            }

            do{
                Cryptography.CryptoObj cryptoObj = cryptography.getCryptoObjInstance();
                cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(KEY_OBJ)));
                cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(KEY_WRAPPED)));
                cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(KEY_IV)));

                byte[] rawObj = cryptography.decrypt(cryptoObj);
                if(rawObj == null) return null;
                ByteArrayInputStream bis = new ByteArrayInputStream(rawObj);
                ObjectInputStream ois = new ObjectInputStream(bis);
                bis.close();
                Content content = (Content) ois.readObject();
                ois.close();
                result.add(content);
            }while (cursor.moveToNext());

            db.close();
            return result;

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            db.close();
            return null;
        }
    }

    @Nullable
    public Header selectHeader(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_ID + " = ?";
        Cryptography cryptography = Cryptography.getInstance();

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})){

            if(!cursor.moveToFirst() || cursor.getCount() != 1){
                db.close();
                return null;
            }

            Cryptography.CryptoObj cryptoObj = cryptography.getCryptoObjInstance();
            cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(FOLDER_OBJ)));
            cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(FOLDER_WRAPPED)));
            cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(FOLDER_IV)));

            byte[] rawObj = cryptography.decrypt(cryptoObj);
            if(rawObj == null) return null;
            ByteArrayInputStream bis = new ByteArrayInputStream(rawObj);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Folder folder = (Folder) ois.readObject();
            bis.close();
            ois.close();
            db.close();
            return folder;

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            db.close();
            return null;
        }

    }

    @Nullable
    Folder selectFolder(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + KEY_ID + " = ?";
        Cryptography cryptography = Cryptography.getInstance();

        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(id)})){

            if(!cursor.moveToFirst() || cursor.getCount()!=1){
                db.close();
                return null;
            }

            Cryptography.CryptoObj cryptoObj = cryptography.getCryptoObjInstance();
            cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(FOLDER_OBJ)));
            cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(FOLDER_WRAPPED)));
            cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(FOLDER_IV)));

            byte[] rawObj = cryptography.decrypt(cryptoObj);
            if(rawObj == null) return null;
            ByteArrayInputStream bis = new ByteArrayInputStream(rawObj);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Folder folder = (Folder)ois.readObject();
            ois.close();
            db.close();
            return folder;

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            db.close();
            return null;
        }
    }

    boolean existsFolder(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS + " WHERE " + FOLDER_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})){
            boolean result = cursor.getCount() > 0;
            db.close();
            return result;
        }
    }

    /*
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    KEYS    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     */

    public boolean insertKey(Content content){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        Cryptography cryptography = Cryptography.getInstance();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {

            content.setId(generateId());
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(content);
            os.close();
            Cryptography.CryptoObj cryptoObj = cryptography.encrypt(bos.toByteArray());
            bos.close();

            if(cryptoObj == null){
                db.close();
                return false;
            }

            values.put(KEY_ID,content.getId());
            values.put(KEY_OBJ,cryptoObj.getData());
            values.put(KEY_WRAPPED, cryptoObj.getWrap());
            values.put(KEY_IV, cryptoObj.getIv());
            values.put(KEY_PARENT,content.getParentId());

            boolean result = db.insert(TABLE_KEYS,null,values) != -1;
            db.close();
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }

    public boolean updateKey(Content content){

        SQLiteDatabase db = this.getWritableDatabase();
        Cryptography cryptography = Cryptography.getInstance();
        ContentValues values = new ContentValues();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {

            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(content);
            os.close();
            Cryptography.CryptoObj cryptoObj = cryptography.encrypt(bos.toByteArray());
            bos.close();

            if(cryptoObj == null){
                db.close();
                return false;
            }

            values.put(KEY_ID, content.getId());
            values.put(KEY_PARENT, content.getParentId());
            values.put(KEY_OBJ, cryptoObj.getData());
            values.put(KEY_WRAPPED, cryptoObj.getWrap());
            values.put(KEY_IV, cryptoObj.getIv());

            boolean result = db.update(TABLE_KEYS,values, KEY_ID + " = ?", new String[]{ String.valueOf(content.getId())}) == 1;
            db.close();
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }

    public boolean removeKey(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = db.delete(TABLE_KEYS,KEY_ID + " = ? ", new String[]{String.valueOf(id)}) == 1;
        db.close();
        return result;
    }

    @Nullable
    public Content selectKey(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        Cryptography cryptography = Cryptography.getInstance();
        String query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(id)})){

            if(!cursor.moveToFirst() || cursor.getCount() != 1){
                db.close();
                return null;
            }

            Cryptography.CryptoObj cryptoObj = cryptography.getCryptoObjInstance();
            cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(KEY_OBJ)));
            cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(KEY_WRAPPED)));
            cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(KEY_IV)));

            byte[] rawObj = cryptography.decrypt(cryptoObj);
            if(rawObj == null) return null;
            ByteArrayInputStream bis = new ByteArrayInputStream(rawObj);
            ObjectInputStream ois = new ObjectInputStream(bis);
            bis.close();
            Content content = (Content)ois.readObject();
            ois.close();
            db.close();
            return content;

        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            db.close();
            return null;
        }
    }

    boolean existsKey(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_KEYS + " WHERE " + KEY_ID + " = ?";
        try(Cursor c = db.rawQuery(query, new String[]{ String.valueOf(id)})){
            boolean result = c.getCount() == 1;
            db.close();
            return result;
        }
    }

    /*
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    FINDER    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     */


    public ArrayList<Header> findAll(String query){
        return null;
    }

    /*
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    LOG & DROP    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     *  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
     */

    public void log(){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FOLDERS;
        Cryptography crypto = Cryptography.getInstance();
        ByteArrayInputStream bis;
        ObjectInputStream ois;

        System.out.println(" ");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$    FOLDERS  & HEADERS   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println(" ");
        System.out.println(" ");
        try (Cursor cursor = db.rawQuery(query, null)){
            if(cursor.moveToFirst()) {

                do {
                    Cryptography.CryptoObj cryptoObj = crypto.getCryptoObjInstance();
                    cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(FOLDER_OBJ)));
                    cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(FOLDER_WRAPPED)));
                    cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(FOLDER_IV)));

                    byte[] rawObj = crypto.decrypt(cryptoObj);
                    bis = new ByteArrayInputStream(rawObj);
                    ois = new ObjectInputStream(bis);
                    Folder folder = (Folder) ois.readObject();
                    System.out.println(" :......: "+ folder.getId() +" :......: " + folder.toString());
                    System.out.println(" ");

                }while(cursor.moveToNext());

            }
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        query = "SELECT * FROM " + TABLE_KEYS;
        System.out.println(" ");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    KEYS    $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println(" ");
        System.out.println(" ");
        try (Cursor cursor = db.rawQuery(query, null)){
            if(cursor.moveToFirst()) {

                do {
                    Cryptography.CryptoObj cryptoObj = crypto.getCryptoObjInstance();
                    cryptoObj.setData(cursor.getBlob(cursor.getColumnIndex(KEY_OBJ)));
                    cryptoObj.setWrap(cursor.getBlob(cursor.getColumnIndex(KEY_WRAPPED)));
                    cryptoObj.setIv(cursor.getBlob(cursor.getColumnIndex(KEY_IV)));

                    byte[] rawObj = crypto.decrypt(cryptoObj);
                    bis = new ByteArrayInputStream(rawObj);
                    ois = new ObjectInputStream(bis);
                    Content content = (Content) ois.readObject();
                    System.out.println(" :......: "+ content.getId() +" :......: " + content.toString());
                    System.out.println(" ");

                }while(cursor.moveToNext());

            }
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void drop(){
        this.close();
        context.deleteDatabase(DATABASE_NAME);
        Toast.makeText(context,"DB deleted",Toast.LENGTH_LONG).show();
    }
}
