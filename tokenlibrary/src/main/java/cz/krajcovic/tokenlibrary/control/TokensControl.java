package cz.krajcovic.tokenlibrary.control;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import cz.krajcovic.tokenlibrary.devices.Terminal;
import cz.krajcovic.tokenlibrary.utils.EncryptedData;
import cz.krajcovic.tokenlibrary.utils.KeyStoreType;
import cz.krajcovic.tokenlibrary.utils.KeyUtil;
import cz.krajcovic.tokenlibrary.utils.Utils;
import cz.monetplus.smartterminallibrary.utils.Hex;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class TokensControl {
    private static final String TAG = TokensControl.class.getName();

    //    private static final String SECRET_TOKEN_KEY_MASTER = "SecretTokenKeyV0";
    private static final String SECRET_TOKEN_KEY = "SecretTokenKeyV0SecretTo";
    private static final String MONET_AES_DEVICE_UNIQ_KEY_ALIAS = "MonetAesDeviceKey_V0";

    public static final String ENC_TTK_KEY = "ENC_TTK.KEY";
    public static final String ENC_TTK_IV = "ENC_TTK.IV";

    public static final String ENC_TOK_TTK_KEY = "ENC_TOK_TTK.KEY";
    public static final String ENC_TOK_TTK_IV = "ENC_TOK_TTK.IV";
    private static final String CURRENT_SECURITY_PROVIDER = "AndroidOpenSSL";

    private Context context;
    private KeyUtil keyUtil;

    /**
     *
     * @param context
     * @throws NoSuchAlgorithmException
     */
    public TokensControl(Context context) throws NoSuchAlgorithmException {

        this.context = context;
        this.keyUtil = new KeyUtil(context, KeyStoreType.Recommended);

        Utils.LogDetailInfoProviders();
    }


    /**
     * Checks if external storage is available for read and write
     *
     * @return
     */
    protected boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     * @return
     */
    protected boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Delete keys from token library.
     * @throws IOException
     */
    public void deleteKeys() throws IOException {
        deleteFile(new File(ENC_TTK_KEY));
        deleteFile(new File(ENC_TOK_TTK_KEY));
        deleteFile(new File(ENC_TTK_IV));
        deleteFile(new File(ENC_TOK_TTK_IV));
    }

    /**
     * Upload keys from default folder (Environment.DIRECTORY_DOCUMENTS)
     *
     * @throws IOException
     */
    public void uploadKeys() throws IOException {
        File externalStoragePublicDirectory = isExternalStorageWritable() ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) : Environment.getDataDirectory();
        Log.i(TAG, "Upload files from " + externalStoragePublicDirectory.getAbsolutePath());

        uploadKeys(externalStoragePublicDirectory.getAbsolutePath());
    }

    // Upload keys from folder.

    /**
     * Upload keys from path.
     * Call uploadKeys, if you don't know what to do.
     *
     * @param path
     * @throws IOException
     */
    public void uploadKeys(String path) throws IOException {
        uploadKey(new File(path, ENC_TTK_KEY));
        uploadKey(new File(path, ENC_TOK_TTK_KEY));
        uploadKey(new File(path, ENC_TTK_IV));
        uploadKey(new File(path, ENC_TOK_TTK_IV));
    }

    /**
     *  Upload key from input file to context folder.
     *
     * @param inFile
     * @throws IOException
     */
    private void uploadKey(File inFile) throws IOException {
        Log.i(TAG, "Upload key: " + inFile.getAbsolutePath());

        File outFile = new File(context.getFilesDir(), inFile.getName());

        if (inFile.exists()) {
            Log.i(TAG, "Input file " + inFile.getAbsolutePath() + " found");

            if (outFile.exists()) {
                outFile.delete();
            }

            InputStream inStream = new FileInputStream(inFile);
            OutputStream outStream = new FileOutputStream((outFile));

            try {
                IOUtils.copy(inStream, outStream);
            } finally {
                IOUtils.closeQuietly(inStream);
                IOUtils.closeQuietly(outStream);
            }

            if (inFile.exists()) {
                inFile.delete();
            }
        } else {
            Log.i(TAG, "Input file " + inFile.getAbsolutePath() + " not found");
        }
    }

    /**
     * Delete file
     * @param inFile
     * @throws IOException
     */
    private void deleteFile(File inFile) throws IOException {

        File outFile = new File(context.getFilesDir(), inFile.getName());

        if (inFile.exists()) {
            inFile.delete();
        }
    }


    /**
     * Get token byt type;
     * @param type
     * @return
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws InvalidAlgorithmParameterException
     */
    private byte[] getTokenKey(TokenKeyType type) throws IOException, NoSuchPaddingException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        switch (type) {

            case Software:
                return getTokenKeyDebug();
            case StaticKnoxPerDevice:
                return decryptTokenKey();

        }

        throw new InvalidParameterException("Undefined TokenKeyType");
    }

    private byte[] decryptTokenKey() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, CertificateException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        // ttk
        EncryptedData encryptedData = new EncryptedData(
                IOUtils.toByteArray(new FileInputStream(new File(context.getFilesDir(), ENC_TTK_KEY))),
                null/*IOUtils.toByteArray(new FileInputStream(new File(context.getFilesDir(), ENC_TTK_IV)))*/);
        byte[] decryptedTtk = keyUtil.decrypt(MONET_AES_DEVICE_UNIQ_KEY_ALIAS, encryptedData);
        Log.i(TAG, "TTK KVC: " + Hex.encodeHexString(getKVC(decryptedTtk)));


        // ttkTok
        encryptedData = new EncryptedData(
                IOUtils.toByteArray(new FileInputStream(new File(context.getFilesDir(), ENC_TOK_TTK_KEY))),
                null/*IOUtils.toByteArray(new FileInputStream(new File(context.getFilesDir(), ENC_TOK_TTK_IV)))*/);
        byte[] decryptedTokTtk = keyUtil.decrypt(MONET_AES_DEVICE_UNIQ_KEY_ALIAS, encryptedData);
        Log.i(TAG, "TOK_TTK KVC: " + Hex.encodeHexString(getKVC(decryptedTokTtk)));

        byte[] decryptedTokenKey = decryptTokenKey(decryptedTtk, decryptedTokTtk);
        Log.i(TAG, "TOK KVC: " + Hex.encodeHexString(getKVC(decryptedTokenKey)));

        return decryptedTokenKey;
    }

    private byte[] decryptTokenKey(byte[] ttkKey, byte[] tokTtkKey) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        final int DES_SIZE = 8;

        byte[] tokTtk1 = Arrays.copyOfRange(tokTtkKey, 0, DES_SIZE);
        byte[] tokTtk2 = Arrays.copyOfRange(tokTtkKey, DES_SIZE, 2 * DES_SIZE);
        byte[] tokTtk3 = Arrays.copyOfRange(tokTtkKey, 2 * DES_SIZE, 3 * DES_SIZE);

        byte[] result = new byte[3 * DES_SIZE];

        System.arraycopy(SimpleDesDecrypt(ttkKey, tokTtk1), 0, result, 0, DES_SIZE);
        System.arraycopy(SimpleDesDecrypt(ttkKey, tokTtk2), 0, result, DES_SIZE, DES_SIZE);
        System.arraycopy(SimpleDesDecrypt(ttkKey, tokTtk3), 0, result, 2 * DES_SIZE, DES_SIZE);

        return result;
    }

    private byte[] SimpleDesDecrypt(byte[] ttk, byte[] tokTtkPart) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        KeySpec keySpec = new DESedeKeySpec(ttk);
        SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
        Cipher ecipher = Cipher.getInstance("DESede/CBC/NoPadding", Security.getProvider(CURRENT_SECURITY_PROVIDER));
        IvParameterSpec iv = new IvParameterSpec(new byte[8]/*ivString.getBytes()*/);
        ecipher.init(Cipher.DECRYPT_MODE, key, iv);
        return ecipher.doFinal(tokTtkPart);
    }

    private byte[] getKVC(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {

        Cipher kvcCipher = Cipher.getInstance("DESede/CBC/NoPadding", Security.getProvider(CURRENT_SECURITY_PROVIDER));
        SecretKey secretKey = SecretKeyFactory.getInstance("DESede").generateSecret(new DESedeKeySpec(key));

        kvcCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return kvcCipher.doFinal(new byte[8]);
    }

    private static byte[] getTokenKeyDebug() {
        return SECRET_TOKEN_KEY.getBytes();
    }

    /**
     * NOT SECURE
     * Count token from data, with SW secret token.
     * Token is counted only with algorithm, don't use secure element PED.
     *
     * @param data
     * @return
     */
    public byte[] encrypt(byte[] data) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        byte[] token = createToken(data);
        Log.i(TAG, "SW token: " + Hex.encodeHexString(token));

        return token;
    }

    public byte[] encrypt(Terminal terminal, byte[] data) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        return encrypt(data);
    }

//    /**
//     * SECURE
//     * Count token from data, calc DES key on index.
//     * Using PED.
//     *
//     * @param keyIndex
//     * @param data
//     * @return
//     */
//    public static byte[] encrypt(byte keyIndex, byte[] data) {
////        byte[] token = createToken(data);
////        Log.i(TAG, "SW token: " + Hex.encodeHexString(token));
//
//
//        byte[] token = createPedPosToken(keyIndex, data);
//        Log.i(TAG, "PAX token: " + Hex.encodeHexString(token));
//
//
//        return token;
//    }

//    private static byte[] createPedPosToken(byte mTDKIx, byte[] data) {
//        PedControl pedControl = PedControl.getInstance(EPedType.INTERNAL);
//        return pedControl.calcDes(mTDKIx, data, EPedDesMode.ENCRYPT_CBC);
//    }

//    private byte[] createWizarPosToken(byte[] data) {
//        byte[] token = null;
//        PINPadDevice device = (PINPadDevice) POSTerminal.getInstance(getApplicationContext()).getDevice("cloudpos.device.pinpad");
//        try {
//            device.open();
////            KeyInfo keyInfo = new KeyInfo(PINPadDevice.KEY_TYPE_FIX, MASTER_KEY_ID, USER_KEY_ID, AlgorithmConstants.ALG_3DES);
//            KeyInfo keyInfo = new KeyInfo(PINPadDevice.KEY_TYPE_MK_SK, 0, 0, AlgorithmConstants.ALG_3DES);
//            token = device.encryptData(keyInfo, data);
//// encryptedToken = device.encryptData(keyInfo,  data, PINPAD_ENCRYPT_STRING_MODE_CBC,  new byte[16], 16);
//
//        } catch (Exception e) {
//            Log.e(TAG, "PINPadDevice failed", e);
//        }
//
//        try {
//            device.close();
//        } catch (DeviceException e) {
//            Log.e(TAG, "PINPadDevice cannot close", e);
//        } catch (Exception e) {
//            Log.e(TAG, "PINPadDevice cannot close", e);
//        }
//
//        return token;
//    }

    private byte[] createToken(byte[] data) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        byte[] token = null;

        try {
            KeySpec keySpec = new DESedeKeySpec(getTokenKey(TokenKeyType.StaticKnoxPerDevice));

            SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
            IvParameterSpec iv = new IvParameterSpec(new byte[8]/*ivString.getBytes()*/);


            Cipher ecipher = Cipher.getInstance("DESede/CBC/NoPadding", Security.getProvider(CURRENT_SECURITY_PROVIDER));
            ecipher.init(Cipher.ENCRYPT_MODE, key, iv);
            token = ecipher.doFinal(data);

        } catch (InvalidKeyException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        } catch (BadPaddingException e) {
            Log.e(TAG, "Cannnot create SW token", e);
        }


        return token;
    }

    /**
     * Return counted HEX token
     *
     * @param terminal
     * @param cardNumber
     * @param expiration
     * @param seq
     * @return
     * @throws IllegalStateException
     */
    public String getToken(Terminal terminal, String cardNumber, Integer expiration, Character seq) throws IllegalStateException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {


        if (this.isReady(terminal.getActivity().getApplicationContext())) {

            byte[] binDecoded = TokensControl.prepare(cardNumber, expiration, seq);
            if (binDecoded != null) {
                byte[] encryptedToken = encrypt(binDecoded);
                byte[] token = null;

                if (encryptedToken != null) {
                    try {
                        token = TokensControl.digest(encryptedToken);
                    } catch (NoSuchAlgorithmException e) {
//                    Log.e(TAG, "Cannot init SHA256", e);
                        throw new IllegalStateException("Cannot init SHA256");
                    }
                }

                if (token != null) {
                    return TokensControl.hex(token);
                } else {
                    throw new IllegalStateException("Token not counted");
                }
            } else {
                throw new IllegalStateException("Invalid input parameters, cannot prepare data");
            }
        } else {
            throw new IllegalStateException("Missing token keys, call TokensControl.uploadKeys()");
        }
    }

    /**
     * Create formated string for count a token.
     *
     * @param pan
     * @param exp
     * @param seq
     * @return
     */
    private static byte[] prepare(String pan, Integer exp, Character seq) {
        StringBuilder builder = new StringBuilder();

        if (pan != null) {
            builder.append(pan);
            builder.append('D');
        }

        if (exp != null && exp != 0xFFFF) {
            builder.append(String.format("%04d", exp));
        } else {
            builder.append('D');
        }

        if (seq != null) {
            builder.append(String.format("%02X", seq));
        } else {
            builder.append('D');
        }

        if (builder.length() < 32) {
//            builder.append(String.format("%.*s", 32-builder.length(), "FFFFFFFFFFFFFFFF"));
            char[] padding = new char[32 - builder.length()];
            Arrays.fill(padding, 'F');
            builder.append(padding);
        }

        return Hex.decodeHexString(builder.toString());
    }

    /**
     * Digest sha 256 encrypted token.
     *
     * @param encryptedToken
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static byte[] digest(byte[] encryptedToken) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] shaToken = md.digest(encryptedToken);

        byte[] token = new byte[shaToken.length + 1];

        // Hight nibble typ sha(hashe),  1 = sha1
        // Low nibble je verze klice, 0 = Software
        //                            1 = Z AES keystore
        token[0] = 0x11;

        System.arraycopy(shaToken, 0, token, 1, shaToken.length);

        return token;
    }

    private static String hex(byte[] token) {
        if (token != null) {
            String hexToken = Hex.encodeHexString(token).replace(",", "");
            hexToken = Hex.bcdToStr(token);
            Log.i(TAG, "Token: " + hexToken);

            return hexToken;
        } else {
            return null;
        }
    }

    /**
     * Check if a keys are ready.
     *
     * @param context
     * @return
     */
    public boolean isReady(Context context) {

        File file = new File(context.getFilesDir(), ENC_TTK_KEY);
        if (!file.exists()) {
            Log.e(TAG, "Missing ENC_TTK.KEY");

            return false;
        }

//        file = new File(context.getFilesDir(), ENC_TTK_IV);
//        if (!file.exists()) {
//            Log.e(TAG, "Missing ENC_TTK.IV");
//
//            return false;
//        }

        file = new File(context.getFilesDir(), ENC_TOK_TTK_KEY);
        if (!file.exists()) {
            Log.e(TAG, "Missing ENC_TOK_TTK.KEY");

            return false;
        }

//        file = new File(context.getFilesDir(), ENC_TOK_TTK_IV);
//        if (!file.exists()) {
//            Log.e(TAG, "Missing ENC_TOK_TTK.IV");
//
//            return false;
//        }

        try {
            keyUtil.getSecretKey(MONET_AES_DEVICE_UNIQ_KEY_ALIAS);
        } catch (Exception e) {
            Log.e(TAG, "Cannot find AES key");

            return false;
        }

        return true;
    }
}
