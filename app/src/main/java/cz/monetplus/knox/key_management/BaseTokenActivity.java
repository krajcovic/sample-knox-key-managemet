package cz.monetplus.knox.key_management;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cz.krajcovic.tokenlibrary.control.TokensControl;

import java.io.File;
import java.io.IOException;

abstract public class BaseTokenActivity extends Activity implements BaseTokenActivityInterface {
    protected static final String APP_FILE_DIR = "token_generator";
    private static final String TAG = BaseTokenActivity.class.getName();


    protected TokensControl tokensControl;

    /* Checks if external storage is available for read and write */
    protected boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    protected boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    protected void initTokensControl(TokensControl tokensControl) {
        if (!tokensControl.isReady(getApplicationContext())) {

            File externalStoragePublicDirectory = isExternalStorageWritable() ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) : Environment.getDataDirectory();
            try {
                tokensControl.uploadKeys(externalStoragePublicDirectory.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Cannot upload TOKEN keys", e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.token_menu, menu);
        return true;

        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.upload_token_keys:
                try {
                    tokensControl.uploadKeys();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot delete keys", e);
                }
                return true;

            case R.id.delete_token_keys:
                try {
                    tokensControl.deleteKeys();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot delete keys", e);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
