package cz.krajcovic.tokenlibrary.utils;

import android.util.Log;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

public class Utils {
    private static final String TAG = Utils.class.getName();

    public static void LogDetailInfoProviders() {
        Provider[] providers = Security.getProviders();
        Log.i(TAG, "Providers:" + Arrays.toString(providers));

        for (Provider p : providers) {
            Log.d(TAG, p.getName());
            for (Provider.Service s : p.getServices()) {
                Log.d(TAG, "    " + s.getAlgorithm());
            }
        }

        return;

    }
}
