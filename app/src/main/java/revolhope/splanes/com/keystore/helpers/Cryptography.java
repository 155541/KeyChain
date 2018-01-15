package revolhope.splanes.com.keystore.helpers;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


/**
 * Created by splanes on 3/1/18.
 **/
 class Cryptography {

    private Cipher cipher;
    private SecretKey secKey;
    private static Cryptography crypto;

    private Cryptography(){
        initialize();
    }

    static Cryptography getInstance(){
        if(crypto == null) crypto = new Cryptography();
        return Cryptography.crypto;
    }

    private void initialize(){

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (!keyStore.containsAlias("KChain")) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
                keyGenerator.init(new
                        KeyGenParameterSpec.Builder("KChain",KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setKeySize(128)
                        .build()
                );
                secKey = keyGenerator.generateKey();
            }else{
                secKey = (SecretKey)keyStore.getKey("KChain", null);
            }

            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + "/" + KeyProperties.BLOCK_MODE_CBC
                    + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        }catch (KeyStoreException
                | CertificateException
                | UnrecoverableKeyException
                | IOException
                | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to get secKey in Cryptography class", e);
        } catch (NoSuchProviderException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher in Cryptography class", e);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    CryptoObj encrypt(String data){

        if(secKey != null){
            try{
                cipher.init(Cipher.ENCRYPT_MODE, secKey);
                CryptoObj cryptoObj = new CryptoObj();
                cryptoObj.setRawData(cipher.doFinal(data.getBytes()));
                cryptoObj.setIv(cipher.getIV());
                return cryptoObj;
            }catch(InvalidKeyException
                    | BadPaddingException
                    | IllegalBlockSizeException
                    | IllegalStateException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    @Nullable
    String decrypt(CryptoObj cryptoObj){
        if(secKey != null){
            try{
                cipher.init(Cipher.DECRYPT_MODE, secKey, new IvParameterSpec(cryptoObj.getIv()));
                return new String(cipher.doFinal(cryptoObj.getRawData()));

            }catch(InvalidKeyException
                    | InvalidAlgorithmParameterException
                    | BadPaddingException
                    | IllegalBlockSizeException
                    | IllegalStateException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    CryptoObj getNewCryptoObj() { return new CryptoObj(); }

    final class CryptoObj {

        private byte[] rawData;
        private byte[] iv;

        byte[] getRawData() {
            return rawData;
        }
        void setRawData(byte[] rawData) {
            this.rawData = rawData;
        }
        byte[] getIv() {
            return iv;
        }
        void setIv(byte[] iv) {
            this.iv = iv;
        }
    }
}
