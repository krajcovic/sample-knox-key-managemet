package cz.krajcovic.tokenlibrary.control;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;


public class NfcReaderControl extends BaseReaderControl implements IReadersControl {

    private static final String TAG = NfcReaderControl.class.getName();
    private static volatile NfcReaderControl control;

    private NfcAdapter nfcAdapter;
    private Activity activity;
    private boolean isOpen;

    public static NfcReaderControl getInstance() {
        if (control == null) {
            synchronized (NfcReaderControl.class) {
                control = new NfcReaderControl();
            }
        }
        return control;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public void init(NfcAdapter.ReaderCallback readerCallback) {
        Bundle options = new Bundle();
//        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 10000);

        nfcAdapter.enableReaderMode(activity, readerCallback, NfcAdapter.FLAG_READER_NFC_A |
                NfcAdapter.FLAG_READER_NFC_B |
                NfcAdapter.FLAG_READER_NFC_F |
                NfcAdapter.FLAG_READER_NFC_V |
                NfcAdapter.FLAG_READER_NFC_BARCODE |
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK |
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, options);
    }

    
    public void close() {
        if(isOpen()) {
            nfcAdapter.disableReaderMode(activity);
        }

        isOpen = false;
        nfcAdapter = null;
    }

    public void open(Activity a) {
        this.activity = a;
        this.nfcAdapter = (NfcAdapter.getDefaultAdapter(activity));

        if (this.nfcAdapter == null) {
            Log.e(TAG, "No NFC adapter");
            isOpen = false;
        } else {
            isOpen = true;
        }
    }
}
