package cz.krajcovic.tokenlibrary.tms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.iid.InstanceID;
import cz.krajcovic.tokenlibrary.utils.AppConfiguration;
import cz.monetplus.smartterminallibrary.tms.InfoGeneratorContext;
import cz.monetplus.smartterminallibrary.tms.TmsConnection;

public class TmsUtils {
    private static final String TAG = TmsUtils.class.getName();

    /**
     * Function prepares parameters for connection to TMS.
     *
     * @param ttkKeyId
     * @param tmsUri
     * @param tmsPort
     * @param tmsUser
     * @param tmsPass
     * @param applicationContext
     * @return
     */
    static public TmsConnection getTmsConnection(String fileDir, byte ttkKeyId, String tmsUri, int tmsPort, String tmsUser, String tmsPass, Context applicationContext) {
        try {
//            byte[] ttkKcv = pedControl.getKCV(EPedKeyType.TDK, ttkKeyId);
//            String ttkKcvString = InfoGeneratorContext.createTmsKeyTtk(ttkKcv, (byte) 0, ttkKeyId);// Hex.encodeHexString(ttkKcv) + "|" + "B0" + "S" + TTK_KEY_IX;
            String ttkKcvString = "E3CB56|B0S0";

            AppConfiguration config = AppConfiguration.getInstance(applicationContext, fileDir);
            SharedPreferences sharedPreferences = config.getSharedPreferences();

            TmsConnection connection = new TmsConnection(tmsUri,
                    tmsPort,
                    tmsUser,
                    tmsPass,
                    new InfoGeneratorContext(tmsUser,
                            sharedPreferences.getString("tms_vendor", "MONET"), sharedPreferences.getString("tms_keys_ttk", ttkKcvString), InstanceID.getInstance(applicationContext).getId()),
                    20000,
                    10000);

            return connection;
        } catch(Exception e) {
            Log.e(TAG, "Cannot create connection", e);
        }


        return null;
    }
}
