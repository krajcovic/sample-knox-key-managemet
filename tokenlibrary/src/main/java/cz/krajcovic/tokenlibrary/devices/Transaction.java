package cz.krajcovic.tokenlibrary.devices;

import android.nfc.tech.IsoDep;
import cz.monetplus.smartterminallibrary.devices.TransactionType;
import cz.monetplus.smartterminallibrary.utils.TLVBuffer;
import cz.monetplus.smartterminallibrary.utils.Utils;

import java.util.Date;

public class Transaction {

    private TransactionType transactionType;

    private Date dateTime;

    private Integer amount;

    private Integer currency;

    private TLVBuffer emvData;

    public Transaction(TransactionBuilder builder) {
        this.transactionType = builder.transactionType;
        this.dateTime = builder.dateTime;
        this.amount = builder.amount;
        this.currency = builder.currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public void setEmvData(TLVBuffer emvData) {
        this.emvData = emvData;
    }

    public TLVBuffer getEmvData() {
        return emvData;
    }

    // TODO: prepsat prase.
    public String getPan() {
        if (emvData != null) {
            byte[] expectedTag = emvData.findTag(0x5a);
            if (expectedTag != null && expectedTag.length > 0) {
                return Utils.fromNumericElement(emvData.findTag(0x5a));
            } else {
                expectedTag = emvData.findTag(0x57);
                if (expectedTag != null && expectedTag.length > 0) {
                    String track2 = Utils.fromNumericElement(emvData.findTag(0x57));
                    if (track2 != null) {
                        int pos = Math.max(track2.indexOf('='), track2.indexOf('D'));
                        if (pos > 0) {
                            return track2.substring(0, pos);
                        }
                    } else {
                        expectedTag = emvData.findTag(0x9f6b);
                        if (expectedTag != null && expectedTag.length > 0) {
                            track2 = Utils.fromNumericElement(emvData.findTag(0x57));
                            if (track2 != null) {
                                int pos = Math.max(track2.indexOf('='), track2.indexOf('D'));
                                if (pos > 0) {
                                    return track2.substring(0, pos);
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }


    public String getExpiration() {
        if (emvData != null) {
            String track2 = Utils.fromNumericElement(emvData.findTag(0x57));
            if (track2 != null) {
                int pos = Math.max(track2.indexOf('='), track2.indexOf('D'));
                if (pos > 0) {
                    return track2.substring(pos + 1, pos + 1 + 4);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static class TransactionBuilder {
        private TransactionType transactionType;

        private Date dateTime;

        private Integer amount;

        private Integer currency;

        public TransactionBuilder() {

        }

        public TransactionBuilder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public TransactionBuilder dateTime(Date dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public TransactionBuilder amount(Integer amount) {
            this.amount = amount;
            return this;
        }

        public TransactionBuilder currency(Integer currency) {
            this.currency = currency;
            return this;
        }

    }

}
