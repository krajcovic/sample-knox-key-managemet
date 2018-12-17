package cz.krajcovic.tokenlibrary.devices;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import cz.krajcovic.tokenlibrary.control.NfcReaderControl;
import cz.krajcovic.tokenlibrary.control.ReadersControl;
import cz.krajcovic.tokenlibrary.emv.TerminalCless;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;
import cz.krajcovic.tokenlibrary.readers.ThreadSettings;
import cz.monetplus.ogar.MonetClessEMVKernel;
import cz.monetplus.ogar.MonetContactEMVKernel;
import cz.monetplus.smartterminallibrary.devices.BeeperState;
import cz.monetplus.smartterminallibrary.devices.LedState;
import cz.monetplus.smartterminallibrary.devices.TerminalEvents;
import cz.monetplus.smartterminallibrary.devices.TextState;
import cz.monetplus.smartterminallibrary.tms.TmsParameters;
import cz.monetplus.smartterminallibrary.utils.ca.CaRecords;

import java.io.File;

public class Terminal {

    public static final String APPLICATION_FOLDER_NAME = "MPCA";
    private static final String TAG = cz.monetplus.smartterminallibrary.devices.Terminal.class.getName();
    private static final int KERNEL_DEBUG_LEVEL = 0;

    private final Activity activity;

    private final MessageHandler messageHandler;

    private TmsParameters tmsParameters;

    private CaRecords caRecords;

    private TerminalEvents terminalEvents;

    private ReadersControl readers;
    public TerminalCless cless;
//    public TerminalContacts contact;

    private Transaction transaction;

    public Terminal(Activity activity, TerminalEvents terminalEvents, MessageHandler messageHandler) throws IllegalAccessException {
        this.activity = activity;
        this.terminalEvents = terminalEvents;
        this.messageHandler = messageHandler;
        this.messageHandler.setTerminal(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        this.tmsParameters = TerminalUtils.loadParameters(this.activity.getApplicationContext(), this.activity.getFilesDir(), APPLICATION_FOLDER_NAME);
        this.caRecords = TerminalUtils.loadCaRecords(this.activity.getFilesDir(), APPLICATION_FOLDER_NAME);

        if (this.caRecords.getCaMap() == null || this.tmsParameters == null) {
            displayText(TextState.TEXT_KERNEL_NOT_INIT);
        } else {

            readers = new ReadersControl(messageHandler);
            //readers.stopDetect();
            //readers.initThread(new ThreadSettings(NfcReaderControl.getInstance(), 200)/*new ThreadSettings(MagReaderControl.getInstance(), 200), new ThreadSettings(PiccReaderControl.getInstance(EPiccType.INTERNAL), 200), new ThreadSettings(IccReaderControl.getInstance(), 200)*/);
            //readers.open();
            //readers.startDetect();

//            Transaction transaction = TransactionUtils.emptyTransaction();
//
//            cless = new TerminalCless(messageHandler, new MonetClessEMVKernel(), NfcAdapter.getDefaultAdapter(activity));
//            cless.init(this, transaction);

//            contact = new TerminalContacts(messageHandler, new MonetContactEMVKernel(), readers.thread(ReadersControl.Readers.Icc));
//            contact.init(this, transaction);
        }
    }

    public CaRecords getCaRecords() {
        return caRecords;
    }

    public TmsParameters getTmsParameters() {
        return tmsParameters;
    }

    public Activity getActivity() {
        return activity;
    }


    @NonNull
    private File getFile(String fileName) {
        return new File(activity.getFilesDir() + "/" + APPLICATION_FOLDER_NAME, fileName);
    }

//    @NonNull
//    public static File getFile(Activity activity, String fileName) {
//        return new File(activity.getFilesDir() + "/" + APPLICATION_FOLDER_NAME, fileName);
//    }

    @Override
    protected void finalize() throws Throwable {
        stopDetectCard();

        if (readers != null) {
            readers.close();
        }

        super.finalize();
    }

    public void startDetectCard(int maxLoop) throws IllegalAccessException {
        if (readers != null) {
//        readers.init(messageHandler);
            readers.stopDetect();

            readers.initThread(new ThreadSettings(NfcReaderControl.getInstance(), 200, maxLoop)/*new ThreadSettings(MagReaderControl.getInstance(), 200, maxLoop), new ThreadSettings(PiccReaderControl.getInstance(EPiccType.INTERNAL), 200, maxLoop), new ThreadSettings(IccReaderControl.getInstance(), 200, maxLoop)*/);
            readers.open();
            readers.startDetect();

            Transaction transaction = TransactionUtils.emptyTransaction();

            cless = new TerminalCless(messageHandler, new MonetClessEMVKernel());
            cless.init(this, transaction);
        }
        ;
    }

    public void stopDetectCard() {
        if (readers != null) {
            readers.stopDetect();
        }
    }


    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void displayText(final TextState id) {
        if (terminalEvents != null) {
            terminalEvents.display(id.getId());
        }
    }

    public void displayLogo() {
        if (terminalEvents != null) {
            terminalEvents.displayLogo();
        }
    }

    public void displayLeds(final LedState state) {
        if (terminalEvents != null) {

            terminalEvents.blink(state);
        }
    }

    public void beeper(BeeperState state) {
        if (terminalEvents != null) {
            terminalEvents.beeper(state);
        }
    }
}
