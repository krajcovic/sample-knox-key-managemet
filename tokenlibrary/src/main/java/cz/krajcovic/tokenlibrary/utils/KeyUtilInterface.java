package cz.krajcovic.tokenlibrary.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

public interface KeyUtilInterface {
    SecretKey generateKey(String alias) throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableKeyException;

    void deleteUniqKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException;

    Key getKey(String alias, char[] password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException;

    Key getKey(String alias) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException;


    EncryptedData encrypt(String alias, byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidParameterSpecException;

    byte[] decrypt(String alias, EncryptedData encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException, InvalidAlgorithmParameterException, InvalidKeySpecException;
}
