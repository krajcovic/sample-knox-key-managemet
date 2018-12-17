package cz.krajcovic.tokenlibrary.control;

import android.util.Log;

abstract public class BaseReaderControl {

    private String childName = "";
//    private Transaction transaction;

    public void logTrue(String method) {
        childName = getClass().getSimpleName() + ".";
        String trueLog = childName + method;
        Log.i("IPPITest", trueLog);
        //clear();
//        GetObj.logStr += ("true:"+trueLog + "\n");
    }

    public void logErr(String method, String errString) {
        childName = getClass().getSimpleName() + ".";
        String errorLog = childName + method + "   errorMessage：" + errString;
        Log.e("IPPITest", errorLog);
        //clear();
//        GetObj.logStr += ("error:"+errorLog + "\n");
    }

    public void clear() {
//        GetObj.logStr = "";
    }

    public String getLog() {
//        return GetObj.logStr.equals("") ? "Log" : GetObj.logStr;
        return "Log";
    }

//    public Transaction getTransaction() {
//        return transaction;
//    }

//    public void setTransaction(Transaction transaction) {
//        this.transaction = transaction;
//    }
}
