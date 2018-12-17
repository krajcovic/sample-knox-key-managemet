package cz.krajcovic.tokenlibrary.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.util.Base64;
import android.util.Log;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;

class KeyUtil_AndroidKeyStoreStatic implements KeyUtilInterface {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String DEFAULT_KEY_STORE = ANDROID_KEY_STORE;

    private static final String TAG = KeyUtil_AndroidKeyStoreStatic.class.getName();
    private static final int KEY_SIZE = 192;
    private static final int TAG_LENGTH = 128;

    private byte[] staticKey = null;

    public KeyUtil_AndroidKeyStoreStatic(byte[] staticKey) {
        this.staticKey = staticKey;
    }

    @Override
    public SecretKey generateKey(String alias) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {


//        TelephonyManager telephonyManager;
//        telephonyManager = (TelephonyManager) context.getSystemService(Context.
//                TELEPHONY_SERVICE);


        KeyStore keyStore = KeyStore.getInstance(DEFAULT_KEY_STORE);
        keyStore.load(null);

        SecretKeySpec secretKeySpec = new SecretKeySpec(staticKey, "AES");
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKeySpec);

        // Unsupported
        //ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(telephonyManager.getDeviceId().toCharArray());
        ProtectionParameter protectionParameter = new KeyProtection.Builder(
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setUserAuthenticationRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();

        keyStore.setEntry(alias, secretKeyEntry, protectionParameter);

        return (SecretKey) keyStore.getKey(alias, null);
    }


    @Override
    public void deleteUniqKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(DEFAULT_KEY_STORE);
        keyStore.load(null);

        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias);
        } else {
            throw new KeyStoreException("Alias not exist");
        }
    }

    private void saveKey(SecretKey key, OutputStream outputStream) throws IOException {
        try (ObjectOutputStream oout = new ObjectOutputStream(outputStream)) {
            oout.writeObject(key);
        }

    }

    private SecretKey loadKey(InputStream inputStream, String algorithm) throws IOException, ClassNotFoundException {
        try (ObjectInputStream oin = new ObjectInputStream(inputStream)) {
            SecretKey key = (SecretKey) oin.readObject();
            return key;
        }
    }

    @Override
    public  Key getKey(String alias, char[] password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(DEFAULT_KEY_STORE);
        keyStore.load(null);
        if (keyStore.containsAlias(alias)) {
            return keyStore.getKey(alias, password);
        } else {
            return null;
        }
    }

    @Override
    public Key getKey(String alias) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        return getKey(alias, null);
    }

    @Override
    public EncryptedData encrypt(String alias, byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidParameterSpecException {
        SecretKey secretKey = (SecretKey) getKey(alias);

        if (secretKey != null) {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            IvParameterSpec ivParams = cipher.getParameters().getParameterSpec(IvParameterSpec.class);
            byte[] output = cipher.doFinal(input);

            String encryptedBase64Encoded = Base64.encodeToString(output, Base64.DEFAULT);
            Log.d(TAG, encryptedBase64Encoded);

            return new EncryptedData(output, ivParams.getIV());
        } else {
            throw new KeyStoreException("Alias not exist");
        }
    }

    @Override
    public byte[] decrypt(String alias, EncryptedData encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidAlgorithmParameterException {
        SecretKey secretKey = (SecretKey) getKey(alias);

        if (secretKey != null) {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

           // IvParameterSpec ivParams = new IvParameterSpec(IV_ZERO_12);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, encryptedData.getIv());

//            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(encryptedData.getIv()));
            cipher.init(Cipher.DECRYPT_MODE, secretKey,gcmParameterSpec);
            byte[] output = cipher.doFinal(encryptedData.getData());

            String encryptedBase64Encoded = Base64.encodeToString(output, Base64.DEFAULT);
            Log.d(TAG, encryptedBase64Encoded);

            return output;
        } else {
            throw new KeyStoreException("Alias not exist");
        }
    }
}
