package cz.krajcovic.tokenlibrary.readers;

import cz.krajcovic.tokenlibrary.control.BaseReaderControl;

public class ThreadSettings {
    private long sleepTimeout;
    private int maxLoop;
    private BaseReaderControl control;

    public ThreadSettings(BaseReaderControl control, long sleepTimeout) {
        this.control = control;
        this.sleepTimeout = sleepTimeout;
        this.maxLoop = 0;
    }

    public ThreadSettings(BaseReaderControl control, long sleepTimeout, int maxLoop) {
        this.control = control;
        this.sleepTimeout = sleepTimeout;
        this.maxLoop = maxLoop;
    }

    public BaseReaderControl getControl() {
        return control;
    }

    public void setControl(BaseReaderControl control) {
        this.control = control;
    }

    public long getSleepTimeout() {
        return sleepTimeout;
    }

    public void setSleepTimeout(long sleepTimeout) {
        this.sleepTimeout = sleepTimeout;
    }

    public int getMaxLoop() {
        return maxLoop;
    }

    public void setMaxLoop(int maxLoop) {
        this.maxLoop = maxLoop;
    }
}
