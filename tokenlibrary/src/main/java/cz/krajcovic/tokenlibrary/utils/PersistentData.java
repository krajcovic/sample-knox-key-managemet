package cz.krajcovic.tokenlibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

class PersistentData {

    private SharedPreferences mPrefs = null;

    public PersistentData(Context mContext) {
        ApplicationInfo applicationInfo = mContext.getApplicationInfo();
        mPrefs = mContext.getSharedPreferences(applicationInfo.name, mContext.MODE_PRIVATE);
    }

    public String getKey(String key, String defValue) {
        return mPrefs.getString(key, defValue);
    }
}
