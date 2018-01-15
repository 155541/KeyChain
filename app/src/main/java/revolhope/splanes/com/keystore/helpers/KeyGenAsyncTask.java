package revolhope.splanes.com.keystore.helpers;

import android.os.AsyncTask;
import java.util.concurrent.ThreadLocalRandom;
import revolhope.splanes.com.keystore.model.enums.EnumLength;
import revolhope.splanes.com.keystore.model.interfaces.IOnAsyncTaskComplete;
import revolhope.splanes.com.keystore.model.KeyMetadata;

public class KeyGenAsyncTask extends AsyncTask<KeyMetadata, Integer, String> {
    
    private int length;
    private IOnAsyncTaskComplete callback;


    public void setCallback(IOnAsyncTaskComplete callback){
        this.callback = callback;
    }

    protected String doInBackground(KeyMetadata... metadata){
        
        setLength(metadata[0].getLength());
        StringBuilder builder = new StringBuilder(length);

        switch (metadata[0].getType()){
            case ONLY_CHAR:
                for(int i = 0; i < length ; i++) builder.append(getChar());
                break;
            case ONLY_NUMBER:
                for(int i = 0; i < length ; i++) builder.append(getNumber());
                break;
            case ONLY_SYMBOL:
                for(int i = 0; i < length ; i++) builder.append(getSymbol());
                break;
            case CHAR_NUMBER:

                for(int i = 0 ; i < length ; i++){
                    if(ThreadLocalRandom.current().nextInt()%2==0) builder.append(getChar());
                    else builder.append(getNumber());
                }
                break;
            case CHAR_SYMBOL:

                for(int i = 0 ; i < length ; i++){
                    if(ThreadLocalRandom.current().nextInt()%2==0) builder.append(getChar());
                    else builder.append(getSymbol());
                }
                break;
            case NUMBER_SYMBOL:

                for(int i = 0 ; i < length ; i++){
                    if(ThreadLocalRandom.current().nextInt()%2==0) builder.append(getNumber());
                    else builder.append(getSymbol());
                }
                break;
            case CHAR_NUMBER_SYMBOL:
                for(int i = 0 ; i < length ; i++){
                    if(ThreadLocalRandom.current().nextInt()%2==0) builder.append(getChar());
                    else if(ThreadLocalRandom.current().nextInt()%3==0) builder.append(getNumber());
                    else builder.append(getSymbol());
                }
                break;
        }
        return builder.toString();
    }

    protected void onPostExecute(String key) {
        if(callback != null)
            callback.onKeyGenerated(key);
    }
    
    private void setLength(EnumLength enumLength){

        switch(enumLength){
            case LENGTH_8:
                this.length = 8;
                break;
            case LENGTH_12:
                this.length = 12;
                break;
            case LENGTH_16:
                this.length = 16;
                break;
            case LENGTH_24:
                this.length = 24;
                break;
            case LENGTH_32:
                this.length = 32;
                break;
        }
    }
    
    private char getChar(){

        int rand = ThreadLocalRandom.current().nextInt()%2==0 ?
                ThreadLocalRandom.current().nextInt(65, 90 + 1) : // May
                ThreadLocalRandom.current().nextInt(97, 122 + 1); // Min

        return (char)rand;
    }
    
    private char getNumber(){
        return (char)ThreadLocalRandom.current().nextInt(48, 57 + 1);
    }
    
    private char getSymbol(){

        int rand;

        if(ThreadLocalRandom.current().nextInt() % 2 == 0){
            rand = ThreadLocalRandom.current().nextInt(33, 47 + 1);
        }else if(ThreadLocalRandom.current().nextInt() % 3 == 0){
            rand = ThreadLocalRandom.current().nextInt(58, 64 + 1);
        }else{
            rand = ThreadLocalRandom.current().nextInt(91, 96 + 1);
        }

        return (char)rand;
    }
}