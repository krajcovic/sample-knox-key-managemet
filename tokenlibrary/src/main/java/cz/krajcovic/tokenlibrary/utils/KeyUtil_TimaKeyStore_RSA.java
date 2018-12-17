package cz.krajcovic.tokenlibrary.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import cz.krajcovic.knoxsupport.KnoxManager;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

class KeyUtil_TimaKeyStore_RSA implements KeyUtilInterface {

    private static final String KNOX_KEY_STORE = "TimaKeyStore";

    private static final String TAG = KeyUtil_TimaKeyStore_RSA.class.getName();
    private static final int KEY_SIZE = 2048;
    private static final int TAG_LENGTH = 128;
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/NoPadding";

    private KeyPair keyPair = null;

    private Context context;
    private KnoxManager knoxManager = new KnoxManager();

    public KeyUtil_TimaKeyStore_RSA(Context context) throws NoSuchAlgorithmException {
        this.context = context;

        //if(!knoxManager.isTimaKeystore(context)) {
        knoxManager.enableTimaKeystore(context, true);
        knoxManager.enableTimaKeystorePerApp(context, false);
        //}

        Log.i(TAG, "TIMA Keystore status: " + knoxManager.isTimaKeystore(context));
    }

    @Override
    public SecretKey generateKey(String alias) throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableKeyException {
        Utils.LogDetailInfoProviders();

        KeyStore keyStore = KeyStore.getInstance(KNOX_KEY_STORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(alias)) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(KEY_SIZE);
            keyPair = kpg.generateKeyPair();

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyPair.getPublic().getEncoded(), "RSA");
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKeySpec);

            // Unsupported
            ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(getPassword());
            keyStore.setEntry(alias, secretKeyEntry, protectionParameter);

            return (SecretKey) keyStore.getKey(alias, getPassword());
        } else {
            throw new KeyStoreException("Alias exist");
        }
    }

    private char[] getPassword() {
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.
                TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId().toCharArray();
    }

    @Override
    public void deleteUniqKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KNOX_KEY_STORE);
        keyStore.load(null);

        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias);
        } else {
            throw new KeyStoreException("Alias not exist");
        }
    }

    @Override
    public Key getKey(String alias, char[] password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(KNOX_KEY_STORE);
        keyStore.load(null);
        if (keyStore.containsAlias(alias)) {
            return keyStore.getKey(alias, password);
        } else {
            return null;
        }
    }

    public Key getKey(String alias) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        return getKey(alias, getPassword());
    }

    @Override
    public EncryptedData encrypt(String alias, byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidParameterSpecException, UnrecoverableKeyException {

        if (input != null && input.length != (KEY_SIZE / 8)) {
            //throw new IndexOutOfBoundsException("Invalid size of encrypt data");
            Log.w(TAG, "Invalid size of encrypt data");
        }

        //PrivateKey aPrivate = keyPair.getPrivate();
        if (keyPair != null) {
            PrivateKey key = keyPair.getPrivate();
            if (key != null) {
                final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);

                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] paddedInput = Arrays.copyOf(input, KEY_SIZE / 8);
                byte[] output = cipher.doFinal(paddedInput);

                String encryptedBase64Encoded = Base64.encodeToString(output, Base64.DEFAULT);
                Log.d(TAG, encryptedBase64Encoded);

                return new EncryptedData(output, null);
            } else {
                throw new KeyStoreException("Alias not exist");
            }
        } else {
            SecretKey secretKey = (SecretKey) getKey(alias, getPassword());
            if (secretKey == null) {
                throw new KeyStoreException("Alias not exist");
            } else {
                throw new KeyStoreException("Key exist, first delete old.");
            }
        }
    }

    @Override
    public byte[] decrypt(String alias, EncryptedData encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        SecretKey secretKey = (SecretKey) getKey(alias, getPassword());

        if (secretKey != null) {
            PublicKey publicKey =
                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(secretKey.getEncoded()));

            final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);

            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(encryptedData.getData());

            String encryptedBase64Encoded = Base64.encodeToString(output, Base64.DEFAULT);
            Log.d(TAG, encryptedBase64Encoded);

            return output;
        } else {
            throw new KeyStoreException("Alias not exist");
        }
    }
}
