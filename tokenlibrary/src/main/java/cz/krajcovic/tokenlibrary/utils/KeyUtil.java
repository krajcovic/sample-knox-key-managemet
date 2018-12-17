package cz.krajcovic.tokenlibrary.utils;

import android.content.Context;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

public class KeyUtil {

    private KeyUtilInterface keyUtil;

    public KeyUtil(Context context, KeyStoreType type) throws NoSuchAlgorithmException {

        switch (type) {


            case Recommended:
            case TimaKeyStore_RSA:
                keyUtil = new KeyUtil_TimaKeyStore_RSA(context);
                break;
            case AndroidKeyStore:
                keyUtil = new KeyUtil_AndroidKeyStore();
                break;
            case AndroidKeyStoreStatic:
                keyUtil = new KeyUtil_AndroidKeyStoreStatic("TESTOVACI_KLIC_MONET".getBytes());
                break;
            case TimaKeyStore_AES:
                keyUtil = new KeyUtil_TimaKeyStore_AES(context);
                break;
        }

//        keyUtil = new KeyUtil_AndroidKeyStore();
//        keyUtil = new KeyUtil_AndroidKeyStoreStatic();

//        keyUtil = new KeyUtil_TimaKeyStore_RSA(context);
        //keyUtil = new KeyUtil_TimaKeyStore_AES(context);
    }

    public SecretKey generateKey(String alias) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, UnrecoverableKeyException {
        return keyUtil.generateKey(alias);
    }

    public SecretKey generateKey(String alias, byte[] password) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        Object obj = keyUtil.getClass();
        if (obj instanceof KeyUtil_AndroidKeyStoreStatic) {
            return keyUtil.generateKey(alias);
        }

        throw new InvalidClassException("Can be used only with static key compilation");
    }

    public void deleteUniqKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyUtil.deleteUniqKey(alias);
    }

    public Key getSecretKey(String alias, char[] password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        return keyUtil.getKey(alias, password);
    }

    public Key getSecretKey(String alias) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        return keyUtil.getKey(alias);
    }

    public EncryptedData encrypt(String alias, byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidParameterSpecException {
        return keyUtil.encrypt(alias, input);
    }

    public byte[] decrypt(String alias, EncryptedData encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        return keyUtil.decrypt(alias, encryptedData);
    }
}
