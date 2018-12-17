package cz.monetplus.knox.key_management;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.util.IOUtils;
import cz.krajcovic.tokenlibrary.utils.EncryptedData;
import cz.krajcovic.tokenlibrary.utils.KeyStoreType;
import cz.krajcovic.tokenlibrary.utils.KeyUtil;
import cz.monetplus.smartterminallibrary.utils.Hex;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class KeyManagementActivity extends AppCompatActivity {

    private static final String MONET_AES_DEVICE_UNIQ_KEY_ALIAS = "MonetAesDeviceKey_V0";
    //private static final String MONET_AES_DEVICE_STATIC_KEY_ALIAS = "MonetAesDeviceStaticKey_V0";

    private static final String MONET_AES_DEVICE_KEY_ALIAS = MONET_AES_DEVICE_UNIQ_KEY_ALIAS;

    private static final String TAG = KeyManagementActivity.class.getName();

    @Deprecated
    private static final int REQUEST_CODE_INTENT_OPEN_DOCUMENT_TREE = 1;

    private byte[] aesStaticKey = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1};
    // 32
    // private static byte[] EMPTY_BLOCK = new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    //16
    //private static byte[] EMPTY_BLOCK = new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] EMPTY_BLOCK = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6};


    private TextView tvBase64Kvc;
    private TextView tvHexKvc;

    private KeyUtil keyUtil ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_management);

        try {
            keyUtil = new KeyUtil(getApplicationContext(), KeyStoreType.Recommended);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Cannot use keyUtil", e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        TextView tvAlias = (TextView) findViewById(R.id.tvAlias);
        tvAlias.setText(MONET_AES_DEVICE_KEY_ALIAS);

        String base64Kvc = "Not found";
        String hexKvc = "Not found";
        try {
            SecretKey secretKey = (SecretKey) keyUtil.getSecretKey(MONET_AES_DEVICE_KEY_ALIAS);
            if (secretKey != null) {
                EncryptedData encryptedData = keyUtil.encrypt(MONET_AES_DEVICE_KEY_ALIAS, EMPTY_BLOCK);
                base64Kvc = Base64.encodeToString(encryptedData.getData(), Base64.DEFAULT);
                hexKvc = Hex.encodeHexString(encryptedData.getData());
            }
        } catch (Exception e) {
            Log.i(TAG, "AES key not found", e);
            showToast(e.getMessage());
        }

        tvBase64Kvc = (TextView) findViewById(R.id.tvBase64Kvc);
        tvHexKvc = (TextView) findViewById(R.id.tvHexKvc);
        DisplayKvc(base64Kvc, hexKvc);


        Button btnGenerate = (Button) findViewById(R.id.btnGenerateAesKey);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    SecretKey secretKey;

                    //secretKey = keyUtil.generateAesKey(MONET_AES_DEVICE_KEY_ALIAS, "MONET_2018".getBytes());
                    secretKey = keyUtil.generateKey(MONET_AES_DEVICE_KEY_ALIAS);

//                    if(MONET_AES_DEVICE_KEY_ALIAS== MONET_AES_DEVICE_STATIC_KEY_ALIAS) {
//                        secretKey = keyUtil.generateAesKey(MONET_AES_DEVICE_KEY_ALIAS, aesStaticKey);
//                    }
//                    else {
//                        secretKey = keyUtil.generateAesKey(MONET_AES_DEVICE_KEY_ALIAS);
//                    }

                    if (secretKey != null) {
                        try {
                            EncryptedData encryptedData = keyUtil.encrypt(MONET_AES_DEVICE_KEY_ALIAS, EMPTY_BLOCK);
                            DisplayKvc(Base64.encodeToString(encryptedData.getData(), Base64.DEFAULT), Hex.encodeHexString(encryptedData.getData()));
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot count KVC", e);
                            showToast("Cannot count KVC");
                        }
                    } else {
                        DisplayKvc("Cannot use key!!!");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Cannot generate key!!!", e);
                    showToast("Cannot generate key!!!");
                }


            }
        });

        Button btnDelete = (Button) findViewById(R.id.btnDeleteAesKey);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    keyUtil.deleteUniqKey(MONET_AES_DEVICE_KEY_ALIAS);
                    DisplayKvc("Cannot use key!!!");
                } catch (Exception e) {
                    Log.e(TAG, "Cannot delete key!", e);
                    showToast(e.getMessage());
                }
            }
        });

        Button btnEncryptAssets = (Button) findViewById(R.id.btnEncryptAssets);
        btnEncryptAssets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                i.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivityForResult(Intent.createChooser(i, "Choose directory"), REQUEST_CODE_INTENT_OPEN_DOCUMENT_TREE);

                File externalStoragePublicDirectory = isExternalStorageWritable() ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) : Environment.getDataDirectory();

                TextView tvEncryptedPath = (TextView) findViewById(R.id.tvEncryptedPath);

                tvEncryptedPath.setText(externalStoragePublicDirectory.getAbsolutePath());
                encryptAssetFile("TTK.KEY", externalStoragePublicDirectory.getAbsolutePath());
                encryptAssetFile("TOK_TTK.KEY", externalStoragePublicDirectory.getAbsolutePath());
            }
        });
    }

    private void DisplayKvc(String message) {
        tvBase64Kvc.setText(message);
        tvHexKvc.setText(message);
    }

    private void DisplayKvc(String base64, String hex) {
        tvBase64Kvc.setText(base64);
        tvHexKvc.setText(hex);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_INTENT_OPEN_DOCUMENT_TREE:
                if (resultCode == -1) {

                    TextView tvEncryptedPath = (TextView) findViewById(R.id.tvEncryptedPath);

//                    Uri treeUri = data.getData();
//                    DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);


                    File externalStoragePublicDirectory = isExternalStorageWritable() ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) : Environment.getDataDirectory();

                    tvEncryptedPath.setText(externalStoragePublicDirectory.getAbsolutePath());
                    encryptAssetFile("TTK.KEY", externalStoragePublicDirectory.getAbsolutePath());
                    encryptAssetFile("TOK_TTK.KEY", externalStoragePublicDirectory.getAbsolutePath());
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void encryptAssetFile(String fileName, String path) {
        try {
            EncryptedData encryptedData = keyUtil.encrypt(MONET_AES_DEVICE_KEY_ALIAS, IOUtils.toByteArray(getAssets().open(fileName)));

            SaveFile(fileName, path, encryptedData.getData());
            if(encryptedData.getIv() != null) {
                SaveFile(fileName.replace(".KEY", ".IV"), path, encryptedData.getIv());
            }

        } catch (Exception e) {
            Log.e(TAG, "Cannot encrypt " + fileName + " file", e);
            showToast("Cannot encrypt " + fileName + " file");
            showToast(e.getMessage());
        }
    }

    private void SaveFile(String fileName, String path, byte[] data) throws IOException {
        new File(path).mkdirs();
        File nFile = new File(path, "ENC_" + fileName);
        if (nFile.exists()) {
            nFile.delete();
        }
        nFile.createNewFile();
        try (FileOutputStream outStream = new FileOutputStream(nFile)) {
            outStream.write(data);
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
