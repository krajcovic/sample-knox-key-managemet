package cz.krajcovic.tokenlibrary.emv;

import android.support.annotation.NonNull;
import android.util.Log;
import cz.monetplus.smartterminallibrary.utils.TLVBuffer;
import cz.monetplus.smartterminallibrary.utils.TagValue;
import cz.monetplus.smartterminallibrary.utils.Utils;

public class EmvUtils {
    private static final String TAG = EmvUtils.class.getName();

    @NonNull
    public static TLVBuffer parseEmvData(byte[] resp, int respLen) {
        TLVBuffer emvData = new TLVBuffer();
        emvData.load(resp, respLen);
        for (TagValue tagValue :
                emvData.getTags()) {
            Log.i(TAG, Long.toHexString(tagValue.tag) + ", Data=" + Utils.bytesToHex(tagValue.value));
        }
        return emvData;
    }
}
