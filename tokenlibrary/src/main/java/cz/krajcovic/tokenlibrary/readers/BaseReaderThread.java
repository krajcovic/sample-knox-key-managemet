package cz.krajcovic.tokenlibrary.readers;

import cz.krajcovic.tokenlibrary.control.BaseReaderControl;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;

public class BaseReaderThread extends Thread {
    protected BaseReaderControl baseControl;

//    protected Transaction transaction;

    protected MessageHandler messageHandler;

    public boolean isRunning = false;

    public BaseReaderThread(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void finish() {
        isRunning = false;
    }

    public boolean isRunning() {
        return !Thread.interrupted() && isRunning;
    }

//    public void setTransaction(Transaction transaction) {
//        this.transaction = transaction;
//    }

    public BaseReaderControl getControl() {
        return baseControl;
    }
}
