package cz.krajcovic.tokenlibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AppConfiguration {
    public static final String SHARED_LAST_CARD_NUMBER = "shared_last_card_number";
    public static final String SHARED_LAST_EXPIRATION = "shared_last_expiration";
    public static final String SHARED_LAST_KEY_ID = "shared_last_key_id";

    public static final String SHARED_TMS_URI = "shared_tms_uri";
    public static final String SHARED_TMS_PORT = "shared_tms_port";
    public static final String SHARED_TMS_TERM_ID = "shared_tms_terminal_id";
    public static final String SHARED_TMS_PASS = "shared_tms_pass";
    public static final String SHARED_TMS_TTK_KEY_ID = "shared_tms_ttk_key_id";

    private static volatile AppConfiguration instance = null;

    private static final String TAG = AppConfiguration.class.getSimpleName();

    private static String FILE_DIR;
    private static final String CFG_FILE = "cfg.json";

    private static SharedPreferences mSharPreference;
    private static PersistentData mPerData;
    private static JSONObject mConfig = null;
    private static Context mContext;


    public static AppConfiguration getInstance(Context context, String fileDir) {
        if (instance == null) {
            synchronized (AppConfiguration.class) {
                if (instance == null) instance = new AppConfiguration();
            }
        }

        mContext = context;
        FILE_DIR = fileDir;
        mPerData = new PersistentData(mContext);

        mSharPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return instance;
    }

    public String loadConfiguration() {
        String fileName = Environment.getExternalStorageDirectory() + File.separator
                + FILE_DIR + File.separator + CFG_FILE;
        File file = new File(fileName);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        String sResp = "";
        try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file))) {
            buf.read(bytes, 0, bytes.length);
            mConfig = new JSONObject(new String(bytes));
            sResp = mConfig.toString();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Exception:" + e.getMessage());
            sResp = "File '" + fileName + "' not found.";
        } catch (IOException e) {
            Log.d(TAG, "Exception:" + e.getMessage());
            sResp = "Config read error.";
        } catch (JSONException e) {
            Log.d(TAG, "Exception:" + e.getMessage());
            sResp = "Config parse error.";
        }

        return sResp;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharPreference;
    }

    public List<String> getList(String key) {
        ArrayList<String> blockedList = new ArrayList<String>();

        try {
            JSONArray jsonArray = mConfig.getJSONArray(key);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    blockedList
                            .add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return blockedList;
    }

}
