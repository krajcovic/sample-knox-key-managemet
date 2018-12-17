package cz.krajcovic.tokenlibrary.handler;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import cz.krajcovic.tokenlibrary.devices.Transaction;
import cz.krajcovic.tokenlibrary.utils.AppConfiguration;
import android.os.Handler;
import android.os.Message;
import cz.krajcovic.tokenlibrary.devices.Terminal;
import cz.monetplus.smartterminallibrary.devices.BeeperState;
import cz.monetplus.smartterminallibrary.utils.TLVBuffer;

import java.io.IOException;

public class MessageHandler extends Handler {

    private static final String TAG = MessageHandler.class.getName();
    private final UserEvents userEvents;
    private Terminal terminal;
    private AppConfiguration config;
    private int maxLoopCounter = 0;

    public MessageHandler(UserEvents userEvents, AppConfiguration config) {
        this.userEvents = userEvents;
        this.config = config;
    }

    /**
     * @param msg
     */
    public void sendMessage(Messages msg) {
        this.sendMessage(this.obtainMessage(msg.getId()));
    }

    /**
     * @param msg
     */
    public void sendMessage(Messages msg, Object obj) {
        Message message = this.obtainMessage(msg.getId());
        message.obj = obj;
        this.sendMessage(message);
    }

    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);

        Messages message = Messages.valueOf(msg.what);
        switch (message) {
            case TERM_START_DETECT_CARD: {
                int maxLoop = msg.obj == null ? 0 : (Integer) (msg.obj);

                maxLoopCounter = 0;


                try {
                    terminal.startDetectCard(maxLoop);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Detect card failed. ", e);
                    this.sendMessage(this.obtainMessage(Messages.DIS_SHOW_EVENT_MESSAGE.getId(), "Detect card failed. Read exception"));
                }
            }
            break;

            case DIS_SHOW_EVENT_MESSAGE:
                userEvents.eventMessage(msg.obj.toString());
//                terminal.displayText(msg.obj.toString());

                break;

//            case DIS_SHOW_TOAST_SHORT:
//                userEvents.eventMessage(msg.obj.toString());
//                break;

            case DIS_SET_CARD_NUMBER:
                userEvents.setCardNumber(msg.obj.toString());
                break;

            case DIS_SET_EXPIRATION:
                try {
                    userEvents.setCardExpiration(Integer.valueOf(msg.obj.toString()));
                } catch (Exception e) {
                    Log.e(TAG, "Cannot parse expiration", e);
                }
                break;

//            case MAG_OPEN:
//                break;
//            case MAG_CLOSE:
//                break;
//            case MAG_READ_PREPARED:
//                break;
//            case MAG_READING:
//                break;
//            case MAG_READED:
//                // Zastav ostatni cteni okamzite
//                terminal.stopDetectCard();
//
//                TrackData trackData = (TrackData) msg.obj;
//                if (trackData != null) {
//                    this.sendMessage(this.obtainMessage(Messages.DIS_SHOW_EVENT_MESSAGE.getId(), trackData.getTrack2()));
//                    String[] split = trackData.getTrack2().split("=");
//                    this.sendMessage(this.obtainMessage(Messages.DIS_SET_CARD_NUMBER.getId(), split[0]));
//                    this.sendMessage(this.obtainMessage(Messages.DIS_SET_EXPIRATION.getId(), split[1].substring(0, 4)));
//                } else {
//                    this.sendMessage(this.obtainMessage(Messages.DIS_SHOW_EVENT_MESSAGE.getId(), "MAG Track2 null!"));
//                }
//
////                terminal.startReaders();
//                this.sendMessage(Messages.TERM_START_DETECT_CARD);
//
//                break;
            case PICC_MAX_LOOP_REACHED:
//            case IICC_MAX_LOOP_REACHED:
//            case MAG_MAX_LOOP_REACHED:
                if (++maxLoopCounter > 2) {
                    // TODO: zkontrolovat jestli je karta odstranena nebo ne, a podle toho rozhodnout jeslti cist obecne a nebo kontrlovat
                    this.sendMessage(Messages.TERM_START_DETECT_CARD);
                }
                break;
//
            case PICC_OPEN:
                break;
            case PICC_CARD_STRING:
                String cardData = (String) msg.obj;

                if (cardData != null) {
                    this.sendMessage(Messages.DIS_SHOW_EVENT_MESSAGE, cardData);
                } else {
                    this.sendMessage(Messages.DIS_SHOW_EVENT_MESSAGE, "PICC data null!");
                }
                break;
            case PICC_DETECTED: {

                IsoDep isoDep = (IsoDep) msg.obj;

                //this.terminal.startTransaction(TransactionUtils.emptyTransaction());
                //Transaction transaction = (Transaction) msg.obj;
//                Transaction transaction = TransactionUtils.emptyTransaction();

                if (isoDep != null) {
                    this.terminal.cless.doTransaction(terminal.getTransaction(), isoDep);
                }

                // Zastav ostatni cteni okamzite
                terminal.stopDetectCard();
            }
            break;

            case TERM_SET_TRANSACTION:
                terminal.setTransaction((Transaction) (msg.obj));
                break;

            case TERM_REMOVE_CARD:
//                terminal.checkDetectCard(); // Udelat asi nejakou kontrolu
                userEvents.removeCard();
                break;

            case PICC_READED: {
//            case IICC_READED: {
                Transaction transaction = (Transaction) msg.obj;
                emvReaded(transaction);

                this.sendMessage(Messages.TERM_REMOVE_CARD);
            }
            break;
//
//            case IICC_DETECTED: {
//                // Zastav ostatni cteni
//                terminal.stopDetectCard();
//
////                Transaction transaction = (Transaction) msg.obj;
////                Transaction transaction = TransactionUtils.emptyTransaction();
//                try {
//                    this.terminal.contact.doTransaction(terminal.getTransaction());
//                } catch (IllegalAccessException e) {
//                    Log.e(TAG, "Cannot do transaction", e);
//                }
//            }
//            break;
//
            case PICC_NOT_SUPPORTED_CARD: {
                terminal.stopDetectCard();

                IsoDep isoDep = (IsoDep) msg.obj;

                userEvents.notSupportedCard(isoDep);

                if(isoDep != null) {
                try {
                    if (isoDep.isConnected()) {
                        isoDep.close();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Cannot close isoDep");
                }
                }

                //terminal.beeper(BeeperState.BEEPER_ERROR);

                this.sendMessage(Messages.TERM_START_DETECT_CARD);
            }
            break;
//
            case PICC_FAILED: {
//            case IICC_FAILED: {
                terminal.stopDetectCard();
                userEvents.readingFailed();
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        activity.cardReadingFailed();
//                    }
//                });
//                terminal.stopDetectCard();
                terminal.beeper(BeeperState.BEEPER_ERROR);

                this.sendMessage(Messages.TERM_START_DETECT_CARD);
            }
            break;
        }
    }

    private void emvReaded(Transaction transaction) {
        if (transaction != null) {
            TLVBuffer data = transaction.getEmvData();
            this.sendMessage(this.obtainMessage(Messages.DIS_SHOW_EVENT_MESSAGE.getId(), transaction.getPan()));
            this.sendMessage(this.obtainMessage(Messages.DIS_SET_CARD_NUMBER.getId(), transaction.getPan()));
            this.sendMessage(this.obtainMessage(Messages.DIS_SET_EXPIRATION.getId(), transaction.getExpiration()));
        } else {
            this.sendMessage(this.obtainMessage(Messages.DIS_SHOW_EVENT_MESSAGE.getId(), "PICC data are null!"));
        }
    }


    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Terminal getTerminal() {
        return this.terminal;
    }
}
