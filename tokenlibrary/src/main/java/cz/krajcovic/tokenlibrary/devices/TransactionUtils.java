package cz.krajcovic.tokenlibrary.devices;

import android.util.Log;
import cz.monetplus.smartterminallibrary.devices.TransactionType;
import cz.monetplus.smartterminallibrary.utils.TLVBuffer;
import cz.monetplus.smartterminallibrary.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TransactionUtils {


    private static final String TAG = TransactionUtils.class.getName();
    private static final Integer CURRENCY_ISO_CZK = 203;

    public static byte[] prepareTransData(TransactionType type, Integer amount, Integer currency, Date dateTime) {
        TLVBuffer tlv = new TLVBuffer();

        tlv.addTagBinary(0x009C, type.getValue());      // trans type

        tlv.addTagBinary(0x0081, Utils.toHexString(amount, 4));//"00000001");   // 1st bin		= 0,01
        tlv.addTagBinary(0x9F02, String.format("%012d", amount));//"000000000001");   // 1st num	= 0,01

        tlv.addTagBinary(0x9F03, "000000000000");   // 2nd num
        tlv.addTagBinary(0x9F04, "00000000");   // 2nd bin

        tlv.addTagBinary(0x5F2A, String.format("%04d", currency));   // currency
        tlv.addTagBinary(0x9F1A, String.format("%04d", currency));   // country

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        tlv.addTagBinary(0x009A, dateFormat.format(dateTime));   // date
        tlv.addTagBinary(0x9F21, timeFormat.format(dateTime));   // time

        tlv.debug();

        byte[] array = tlv.serialize();
        String data = Utils.bytesToHex(array);
        Log.i(TAG, "TransData: " + data);

        return array;
    }

    public static Transaction emptyTransaction() {
        return new Transaction(new Transaction.TransactionBuilder().transactionType(TransactionType.SALE).amount(0).currency(CURRENCY_ISO_CZK).dateTime(Calendar.getInstance().getTime()));
    }
}
