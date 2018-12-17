package cz.krajcovic.knoxsupport;

import android.content.Context;
import android.telephony.TelephonyManager;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyUtil {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String TAG = KeyUtil.class.getName();

    public static SecretKey generateStaticAesKey(Context context, String alias, byte[] key) throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {


        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.
                TELEPHONY_SERVICE);


        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKeySpec);

        //com.samsung.android.knox.keystore.KnoxKeyProtection
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(telephonyManager.getDeviceId().toCharArray());

        keyStore.setEntry(alias, secretKeyEntry, protectionParameter);

        return secretKeyEntry.getSecretKey();
    }
}
