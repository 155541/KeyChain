package revolhope.splanes.com.keystore.helpers;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
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
    private KeyPair keyPair;
    private static Cryptography crypto;

    private Cryptography(){
        init();
    }

    static Cryptography getInstance(){
        if(crypto == null) crypto = new Cryptography();
        return Cryptography.crypto;
    }

    private void init(){
        try{

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias("KChain-v2")) {

                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,"AndroidKeyStore");
                keyPairGenerator.initialize(
                        new KeyGenParameterSpec.Builder("KChain-v2",KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                                //.setKeySize(4096) // Check if fails, si no xuta, eliminar i dejar en default(2048 i guess)
                                .build()
                );
                keyPair = keyPairGenerator.generateKeyPair();
            }else{
                Key key = keyStore.getKey("KChain-v2",null);
                if(key instanceof PrivateKey){
                    Certificate cert = keyStore.getCertificate("KChain-v2");
                    PublicKey publicKey = cert.getPublicKey();
                    keyPair = new KeyPair(publicKey,(PrivateKey)key);

                }else{
                    keyPair = null;
                    cipher = null;
                }
            }

            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA
                    + "/" + KeyProperties.BLOCK_MODE_ECB
                    + "/" + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);

        }catch (KeyStoreException
                | CertificateException
                | UnrecoverableKeyException
                | IOException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    @Nullable
    CryptoObj encrypt(byte[] raw){

        if(cipher == null || keyPair == null || raw == null) return null;

        try {

            KeyGenerator keyGen = KeyGenerator.getInstance( KeyProperties.KEY_ALGORITHM_AES);
            keyGen.init(256, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();

            Cipher c = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                                        + KeyProperties.BLOCK_MODE_CBC + "/"
                                        + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            c.init(Cipher.ENCRYPT_MODE,secretKey);
            cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());

            byte[] encryptedData = c.doFinal(raw);
            byte[] encryptedKey = cipher.wrap(secretKey);
            byte[] ivUsed = c.getIV();

            CryptoObj cryptoObj = new CryptoObj();
            cryptoObj.setData(encryptedData);
            cryptoObj.setWrap(encryptedKey);
            cryptoObj.setIv(ivUsed);

            return cryptoObj;

        } catch(InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | IllegalStateException
                | NoSuchAlgorithmException
                | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    byte[] decrypt(CryptoObj cryptoObj){

        if(cipher == null || keyPair == null || cryptoObj == null) return null;

        try {

            cipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
            final Key key = cipher.unwrap(cryptoObj.getWrap(), "AES/CBC/"+ KeyProperties.ENCRYPTION_PADDING_PKCS7, Cipher.SECRET_KEY);
            Cipher c = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(cryptoObj.getIv()));
            return c.doFinal(cryptoObj.getData());

        } catch(InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | IllegalStateException
                | NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

    class CryptoObj{
        private byte[] data;
        private byte[] wrap;
        private byte[] iv;

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] getWrap() {
            return wrap;
        }

        public void setWrap(byte[] wrap) {
            this.wrap = wrap;
        }

        public byte[] getIv() {
            return iv;
        }

        public void setIv(byte[] iv) {
            this.iv = iv;
        }
    }

    public CryptoObj getCryptoObjInstance(){
        return new CryptoObj();
    }

}
