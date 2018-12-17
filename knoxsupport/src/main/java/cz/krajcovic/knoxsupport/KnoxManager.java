package cz.krajcovic.knoxsupport;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.util.Log;
import com.samsung.android.knox.EnterpriseKnoxManager;
import com.samsung.android.knox.keystore.TimaKeystore;

public class KnoxManager {


    private static final String TAG = KnoxManager.class.getName();

    public boolean isTimaKeystore(Context context) {
        TimaKeystore timaKeystorePolicy = EnterpriseKnoxManager.getInstance(context).getTimaKeystorePolicy();
        return timaKeystorePolicy.isTimaKeystoreEnabled();
    }

    public void enableTimaKeystore(Context context, boolean state) {
        EnterpriseKnoxManager ekm = EnterpriseKnoxManager.getInstance(context);
        try {
            ekm.getTimaKeystorePolicy().enableTimaKeystore(state);
        } catch (SecurityException e) {
            Log.w(TAG, "Exception" + e);
        }
    }

    public void enableTimaKeystorePerApp(Context context, boolean state) {
        EnterpriseKnoxManager ekm = EnterpriseKnoxManager.getInstance(context);
        try {
            ekm.getTimaKeystorePolicy().enableTimaKeystorePerApp(state);
        } catch (SecurityException e) {
            Log.w(TAG, "Exception" + e);
        }
    }

    // Check if the applcation is a Profile Owner
    public boolean isProfileOnwner(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        String packageName = context.getPackageName();
        return devicePolicyManager.isProfileOwnerApp(packageName);
    }
}
