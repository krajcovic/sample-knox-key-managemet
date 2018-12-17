package cz.krajcovic.tokenlibrary.control;

import android.util.Log;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;
import cz.krajcovic.tokenlibrary.handler.Messages;
import cz.krajcovic.tokenlibrary.readers.BaseReaderThread;
import cz.krajcovic.tokenlibrary.readers.NfcReaderThread;
import cz.krajcovic.tokenlibrary.readers.ThreadSettings;


public class ReadersControl {
    private static final String TAG = ReadersControl.class.getName();

    private MessageHandler messageHandler;

//    public enum Readers {Magnetic, Picc, Icc}
//    private MagReaderThread magReaderThread;
//    private PiccReaderThread piccReaderThread;
//    private IiccReaderThread iiccReaderThread;
    private NfcReaderThread nfcReaderThread;

    public ReadersControl(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

//    public void initThread(long magSleepTimeout, long piccSleepTimeout, long iccSleepTimeout) {
//        magReaderThread = new MagReaderThread(messageHandler, MagReaderControl.getInstance(), magSleepTimeout);
//        piccReaderThread = new PiccReaderThread(messageHandler, PiccReaderControl.getInstance(EPiccType.INTERNAL), piccSleepTimeout);
//        iiccReaderThread = new IiccReaderThread(messageHandler, IccReaderControl.getInstance(), iccSleepTimeout);
//    }

//    public void initThread(ThreadSettings magThread, ThreadSettings piccThread, ThreadSettings iccThread) {
//        magReaderThread = new MagReaderThread(messageHandler, magThread); //MagReaderControl.getInstance();
//        piccReaderThread = new PiccReaderThread(messageHandler, piccThread); //PiccReaderControl.getInstance(EPiccType.INTERNAL), piccSleepTimeout
//        iiccReaderThread = new IiccReaderThread(messageHandler, iccThread); //IccReaderControl.getInstance(), iccSleepTimeout
//    }

    public void initThread(ThreadSettings nfcThread) {
        nfcReaderThread = new NfcReaderThread(messageHandler, nfcThread);
    }

//    public void initThread() {
//
//    }

    public void open()
    {
        nfcReaderThread.control().open(messageHandler.getTerminal().getActivity());
        nfcReaderThread.sendMessage(Messages.PICC_OPEN);
//        magReaderThread.control().open();
//        messageHandler.sendMessage(Messages.MAG_OPEN);
//
//        if(!piccReaderThread.control().isOpen()) {
//            piccReaderThread.control().open();
//            messageHandler.sendMessage(Messages.PICC_OPEN);
//        }
//
//        // is opened always.
//        messageHandler.sendMessage(Messages.IICC_OPEN);

    }

    public void close() {
        nfcReaderThread.control().close();
//        magReaderThread.control().close();
//        messageHandler.sendMessage(Messages.MAG_CLOSE);
//
//        piccReaderThread.control().close();
//        messageHandler.sendMessage(Messages.PICC_CLOSE);
//
//        iiccReaderThread.control().close(iiccReaderThread.control().getSlot());
//        messageHandler.sendMessage(Messages.IICC_CLOSE);

    }

    @Override
    protected void finalize() throws Throwable {
        stopDetect();
        close();

        super.finalize();


    }

//    public BaseReaderThread thread(Readers reader) {
//        BaseReaderThread ret = null;
//        switch (reader) {
//
//            case Magnetic:
//                ret = magReaderThread;
//                break;
//            case Picc:
//                ret = piccReaderThread;
//                break;
//            case Icc:
//                ret = iiccReaderThread;
//                break;
//        }
//
//        return ret;
//    }

//    public BasePaxControl control(Readers reader) {
//        BasePaxControl ret = null;
//        switch (reader) {
//
//            case Magnetic:
//                ret = magReaderThread.control();
//                break;
//            case Picc:
//                ret = piccReaderThread.control();
//                break;
//            case Icc:
//                iiccReaderThread.control();
//                break;
//        }
//
//        return ret;
//    }

    public void startDetect() {
        nfcReaderThread.start();
//        magReaderThread.start();
//        piccReaderThread.start();
//        iiccReaderThread.start();
    }

    public void stopDetect() {
        stopThread(nfcReaderThread);
//        stopThread(magReaderThread);
//        magReaderThread = null;
//
//        stopThread(piccReaderThread);
//        piccReaderThread = null;
//
//        stopThread(iiccReaderThread);
//        iiccReaderThread = null;
    }

    private void stopThread(BaseReaderThread thread) {
        if (thread != null) {

            while(thread.isRunning()) {
                // Ten debilni thred se proste ukoncit musi. A dokud se neukonci tak mi to rozbiji dalsi komunikaci s kartou, takze se proste ukoncit musi!!!
                thread.finish();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Cannot finish thread!");
                }
            }

            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }
}
