package cz.krajcovic.tokenlibrary.readers;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import cz.krajcovic.tokenlibrary.control.NfcReaderControl;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;
import cz.krajcovic.tokenlibrary.handler.Messages;
import cz.monetplus.smartterminallibrary.auth.AuthorizationEvent;
import cz.monetplus.smartterminallibrary.auth.TransactionResult;
import cz.monetplus.smartterminallibrary.devices.TextState;
import cz.monetplus.smartterminallibrary.devices.TransactionExecutor;
import cz.monetplus.smartterminallibrary.emv.KernelStatus;
import cz.monetplus.smartterminallibrary.tms.TmsParCrdSections;
import cz.monetplus.smartterminallibrary.utils.Hex;

import java.io.IOException;

import static cz.krajcovic.tokenlibrary.handler.Messages.DIS_SHOW_EVENT_MESSAGE;


public class NfcReaderThread extends BaseReaderThread {
    private static final String TAG = NfcReaderThread.class.getName();
    private long sleepTimeout;
    private int maxLoop;

    public NfcReaderThread(MessageHandler messageHandler, ThreadSettings settings) {
        super(messageHandler);

        this.baseControl = settings.getControl();
        this.sleepTimeout = settings.getSleepTimeout();
        this.maxLoop  = settings.getMaxLoop();
    }

    public NfcReaderControl control() {
        return (NfcReaderControl) baseControl;
    }

    @Override
    protected void finalize() throws Throwable {
        isRunning = false;
//        this.control().close();
        this.control().close();

        super.finalize();
    }

    @Override
    public void run() {
        super.run();

        isRunning = true;
        int counter = 0;

        NfcAdapter.ReaderCallback readerCallback = new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                Intent i = new Intent().putExtra(NfcAdapter.EXTRA_TAG, tag);
                i.setAction(NfcAdapter.ACTION_TAG_DISCOVERED); // Fake budu se tvarit jako ze je to systemovy intent.

                handleNfcTagIntent(i);
            }
        };

        control().init(readerCallback);

//        while(isRunning()) {
//            counter++;
//
//
//
////            PiccCardInfo cardInfo = control().detect(EDetectMode.ISO14443_AB);
////            if(cardInfo == null) {
////                cardInfo = control().detect(EDetectMode.ONLY_M);
////            }
//
////            if(cardInfo != null) {
////                // detected
////                messageHandler.sendMessage(Messages.PICC_DETECTED, cardInfo);
////                break;
////            }
//
//
//
//            if ((maxLoop > 0) && (counter >= maxLoop)) {
//                Log.i(TAG, "Magnetic Maxloop reached");
//                messageHandler.sendMessage(Messages.PICC_MAX_LOOP_REACHED);
//                break;
//            }
//
//            try {
//                sleep(sleepTimeout);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
//
//        Log.i(TAG, "PiccReaderControl.thread is not running");
    }

    private void handleNfcTagIntent(Intent intent/*, TransactionExecutor transactionExecutor, AuthorizationEvent events*/) {
        // Obtaining information from intents.
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                    Log.i(TAG, messages[i].toString());
                }
                // Process the messages array.
            }
        }

        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i(TAG, tagFromIntent.toString());

        }

        if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            byte[] result = null;
            Tag tagFromInteTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tagFromInteTag != null) {
                Log.i(TAG, "onTagDiscoverd.id = " + Hex.bcdToStr(tagFromInteTag.getId()));
                Log.i(TAG, tagFromInteTag.toString());


                IsoDep isoDep = IsoDep.get(tagFromInteTag);
                if (isoDep != null) {
                    try {
                        if (!isoDep.isConnected()) {
                            isoDep.connect();
                        }

                        if (isoDep.isConnected()) {
                            messageHandler.sendMessage(Messages.PICC_DETECTED, isoDep);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "ISO dep connection: ", e);
                    }
                } else {
                    messageHandler.sendMessage(Messages.PICC_FAILED);
                    this.sendMessage(DIS_SHOW_EVENT_MESSAGE, "Unsupported card - IsoDep not found.");
//                    display(TextState.TEXT_UNSUPPORTED_CARD.getId());
                }
            }
        }
    }

    public void sendMessage(Messages msg) {
        messageHandler.sendMessage(msg);
    }
    public void sendMessage(Messages msg, Object obj) {
        Message message = messageHandler.obtainMessage(msg.getId());
        message.obj = obj;
        this.sendMessage(msg);
    }

}
