package cz.krajcovic.tokenlibrary.emv;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import cz.krajcovic.tokenlibrary.devices.Terminal;
import cz.krajcovic.tokenlibrary.devices.Transaction;
import cz.krajcovic.tokenlibrary.devices.TransactionUtils;
import cz.krajcovic.tokenlibrary.handler.MessageHandler;
import cz.krajcovic.tokenlibrary.handler.Messages;
import cz.monetplus.ogar.MonetClessEMVKernel;
import cz.monetplus.smartterminallibrary.devices.TextState;
import cz.monetplus.smartterminallibrary.emv.CaKeys;
import cz.monetplus.smartterminallibrary.emv.KernelStatus;
import cz.monetplus.smartterminallibrary.tms.TmsParCrdSections;
import cz.monetplus.smartterminallibrary.tms.TmsParameters;
import cz.monetplus.smartterminallibrary.utils.Hex;
import cz.monetplus.smartterminallibrary.utils.TLVBuffer;
import cz.monetplus.smartterminallibrary.utils.Utils;
import cz.monetplus.smartterminallibrary.utils.ca.CaRecords;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

public class TerminalCless {

    private static final String TAG = TerminalCless.class.getName();


    private final MessageHandler messageHandler;
    private final MonetClessEMVKernel kernel;
    private ClessKernelCallbacksImpl kernelCallbacks;

    //private BaseReaderControl readerControl;
//    private NfcAdapter nfcAdapter;

    public TerminalCless(MessageHandler messageHandler, MonetClessEMVKernel kernel/*, BaseReaderControl readerControl*/) {
        this.kernel = kernel;
//        this.nfcAdapter = adapter;
        this.messageHandler = messageHandler;
    }

    public void init(final Terminal terminal, final Transaction transaction) throws IllegalAccessException {
        if (initKernel(0, terminal)) {
            if (preprocesorKernel(transaction)) {
                if (startTransaction(transaction)) {
                    messageHandler.sendMessage(Messages.TERM_SET_TRANSACTION, transaction);
                    // OK
                } else {
                    throw new IllegalAccessException("Transaction cannot by started.");
                }
            } else {
                throw new IllegalAccessException("Kernel isn't accessible.");
            }
        } else {
            throw new IllegalAccessException("You have to call TmsUpdateTask first!");
        }
    }

//    private void handleNfcTagIntent(Intent intent, Terminal terminal, Transaction transaction) {
//        // Obtaining information from intents.
//        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//            Parcelable[] rawMessages =
//                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            if (rawMessages != null) {
//                NdefMessage[] messages = new NdefMessage[rawMessages.length];
//                for (int i = 0; i < rawMessages.length; i++) {
//                    messages[i] = (NdefMessage) rawMessages[i];
//                    Log.i(TAG, messages[i].toString());
//                }
//                // Process the messages array.
//            }
//        }
//
//        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
//            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            Log.i(TAG, tagFromIntent.toString());
//        }
//
//        if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
//            byte[] result = null;
//            Tag tagFromInteTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            if (tagFromInteTag != null) {
//                Log.i(TAG, tagFromInteTag.toString());
//
//                IsoDep isoDep = IsoDep.get(tagFromInteTag);
//                if (isoDep != null) {
//                    try {
//                        if (!isoDep.isConnected()) {
//                            isoDep.connect();
//                        }
//
//                        if (isoDep.isConnected()) {
//                            doTransaction(transaction,isoDep);
//
//                        }
//                    } catch (IOException e) {
//                        Log.e(TAG, "ISO dep connection: ", e);
//                    } finally {
//                        try {
//                            if (isoDep.isConnected()) {
//                                isoDep.close();
//                            }
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } else {
//                    display(TextState.TEXT_UNSUPPORTED_CARD.getId());
//                }
//            }
//        }
//    }

    public boolean startTransaction(Transaction tran) {
        byte[] array = TransactionUtils.prepareTransData(tran.getTransactionType(), tran.getAmount(), tran.getCurrency(), tran.getDateTime());
        int result = kernel.startTransaction(array);
        return (result >= 0);
    }

    private boolean preprocesorKernel(Transaction tran) {
        byte[] array = TransactionUtils.prepareTransData(tran.getTransactionType(), tran.getAmount(), tran.getCurrency(), tran.getDateTime());
        int result = kernel.doPreprocess(array);
        return (result >= 0);
    }

    public boolean initKernel(Integer kernelDebugLevel, Terminal terminal) {
        boolean isKernelInit = false;

        try {
            if (kernel != null) {
                kernel.setDebug(kernelDebugLevel);      // 8 bit loguje vse
                this.kernelCallbacks = new ClessKernelCallbacksImpl(terminal/*, (PiccReaderControl) readerThread.getControl()*/);
                isKernelInit = kernel.init(kernelCallbacks);
            }


            if (!isKernelInit) {
                this.display(TextState.TEXT_KERNEL_NOT_INIT.getId());
            } else {
                if (setKernelConfig(terminal.getTmsParameters(), terminal.getCaRecords())) {
                    isKernelInit = true; // OK
                } else {
                    isKernelInit = false;
                }
            }
        } catch (Exception e) {
            this.display(TextState.TEXT_KERNEL_FAILED.getId());
        }
        return isKernelInit;

    }

    public void doTransaction(Transaction transaction, IsoDep isoDep) {
        try {
//            IsoDep isoDep = IsoDep.get(tag);
            kernelCallbacks.setIsoDep(isoDep, 300);
            int ret = kernel.doTransaction();
            String message = "Kernel response: " + Integer.toHexString(ret);

            KernelStatus kernelStatus = KernelStatus.valueOf(ret);
            //this.log(TAG, kernelStatus.toString());
            Log.i(TAG, kernelStatus.toString());

            switch (kernelStatus) {
                case MONEMV_CLS_KERNEL_STATUS_OK:
                case MONEMV_CLS_KERNEL_STATUS_SERVICE_NOT_AVAILABLE:
                case MONEMV_CLS_KERNEL_STATUS_DATABASE_ERROR:
                case MONEMV_CLS_KERNEL_STATUS_INVALID_INPUT_DATA:
                case MONEMV_CLS_KERNEL_STATUS_NOT_SUPPORTED:
                case MONEMV_CLS_KERNEL_STATUS_LACK_OF_MEMORY:
                case MONEMV_CLS_KERNEL_STATUS_COMMUNICATION_ERROR:
                case MONEMV_CLS_KERNEL_STATUS_MISSING_INPUT_DATA:
                case MONEMV_CLS_KERNEL_STATUS_ICC_MISSING_DATA:
                case MONEMV_CLS_KERNEL_STATUS_ICC_INVALID_DATA:
                case MONEMV_CLS_KERNEL_STATUS_ICC_REDUNDANT_DATA:
                case MONEMV_CLS_KERNEL_STATUS_ICC_DATA_FORMAT_ERROR:
                case MONEMV_CLS_KERNEL_STATUS_TERM_MISSING_DATA:
                case MONEMV_CLS_KERNEL_STATUS_CARD_BLOCKED:
                case MONEMV_CLS_KERNEL_STATUS_APPLICATION_BLOCKED:
                case MONEMV_CLS_KERNEL_STATUS_REMOVE_AID:
                case MONEMV_CLS_KERNEL_STATUS_UNKNOWN_SW:
                case MONEMV_CLS_KERNEL_STATUS_COND_OF_USE_NOT_SATISFIED:
                case MONEMV_CLS_KERNEL_STATUS_OFFLINE_APPROVED:
                case MONEMV_CLS_KERNEL_STATUS_OFFLINE_DECLINED:
                case MONEMV_CLS_KERNEL_STATUS_ONLINE_AUTHORISATION:
                case MONEMV_CLS_KERNEL_STATUS_CANCELLED:
                case MONEMV_CLS_KERNEL_STATUS_USE_CONTACT_INTERFACE:
                case MONEMV_CLS_KERNEL_STATUS_NOT_ALLOWED:
                case MONEMV_CLS_KERNEL_STATUS_CONTINUE:
                case MONEMV_CLS_KERNEL_STATUS_SUSPEND:
                case MONEMV_CLS_KERNEL_STATUS_INTERNAL_ERROR:
                case MONEMV_CLS_KERNEL_STATUS_LIB_INTERFACE_ERROR:
                case MONEMV_CLS_KERNEL_STATUS_EXPIRED_CERTIFICATE:
                case MONEMV_CLS_KERNEL_STATUS_REVOKED_CERTIFICATE:
                case MONEMV_CLS_KERNEL_STATUS_CARD_UNKNOWN:
                case MONEMV_CLS_KERNEL_STATUS_MOBILE:
                case MONEMV_CLS_KERNEL_STATUS_UNKNOWN:

                    // OK
                    byte[] resp = new byte[2048 * 4];
                    int respLen = kernel.getAllData(resp);
                    Log.i(TAG, "Len: " + respLen + " Data: " + Hex.encodeHexString(resp, respLen));

                    transaction.setEmvData(EmvUtils.parseEmvData(resp, respLen));

                    messageHandler.sendMessage(Messages.PICC_READED, transaction);

                    try {
                        if (isoDep.isConnected()) {
                            isoDep.close();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Cannot close isoDep");
                    }

                    break;

                default:
                    messageHandler.sendMessage(Messages.PICC_NOT_SUPPORTED_CARD, isoDep);
                    break;

            }


        } catch (Exception e) {
            Log.e(TAG, "doTransaction failed: ", e);
            //messageHandler.sendMessage(Messages.PICC_FAILED);
            messageHandler.sendMessage(Messages.PICC_NOT_SUPPORTED_CARD, isoDep);
        }
    }

    private void display(int id) {
    }

    private boolean setKernelConfig(TmsParameters parameters, CaRecords caRecords) {
        boolean back = true;

        kernel.clearAID(null);

        setTerminalConfig();

        {
            try {
                List<String> clsAids = parameters.getPropertiesSingle(TmsParCrdSections.CLSAID);
                for (String entry : clsAids) {
                    String[] value = entry.split(",");
                    if (value.length > 0) {
                        Log.i(TAG, value.toString());

                        String aid = value[0];
                        Integer kernel = Integer.valueOf(value[2]);

                        setAIDConfig(Hex.decodeHexString(aid), kernel, CaKeys.list(caRecords, Hex.decodeHexString(aid)));
                    } else {
                        Log.w(TAG, "CLSAID is empty");
                    }
                }
            } catch (InvalidPropertiesFormatException e) {
                Log.e(TAG, "CLSAID not defined.", e);
            }
        }

        return back;
    }

    private boolean setTerminalConfig() {
        TLVBuffer tlv = new TLVBuffer();

        tlv.addTagBinary(0xDF0B, "01");                // TRM always

        tlv.addTagBinary(0x9F35, "22");    // type
        tlv.addTagBinary(0x9F40, "6000F0B003");    // add. capabilities

        tlv.addTagBinary(0x9F15, "4199");            // MCC

        tlv.addTagString(0x9F4E, "Merchant name and location :-)");

        byte[] array = tlv.serialize();
        String data = Utils.bytesToHex(array);
        Log.i(TAG, "TermData: " + data);

        int result = kernel.addAID(null, array);
        return (result >= 0);
    }

    private boolean setAIDConfig(byte[] aid, int kernelType, byte[] cakeyslist) {
        TLVBuffer tlv = new TLVBuffer();

        tlv.addTagInt(MonetClessEMVKernel.MONEMV_CLS_KERNEL_TO_USE, 2, kernelType);

        tlv.addTagBinary(MonetClessEMVKernel.MONEMV_CLS_TRANSACTION_LIMIT, "000000000000");    // special "999999999999");
        tlv.addTagBinary(MonetClessEMVKernel.MONEMV_CLS_FLOOR_LIMIT, "000000050000");
        tlv.addTagBinary(MonetClessEMVKernel.MONEMV_CLS_CVM_REQUIRED_LIMIT, "000000050000");

        if (kernelType == MonetClessEMVKernel.MONEMV_CLS_KERNEL_VISA) {
            tlv.addTagBinary(0x9F09, "008C");                // Application Version Number - Terminal
            tlv.addTagBinary(0x9F66, "22204000");        // VISA TTQ (qVSDC, cip VSDC, pin, sign, ODA)
        }
        if (kernelType == MonetClessEMVKernel.MONEMV_CLS_KERNEL_MASTERCARD) {
            tlv.addTagString(0x9F53, "T");                // TC
        }

        if (kernelType == MonetClessEMVKernel.MONEMV_CLS_KERNEL_PURE) {
            // (chip, online, sign, pin) () (mobil) () (SDA, DDA, CDA, CMV-list, TRM )
            tlv.addTagBinary(0xC7, "3E004000F9");                    // TTPI binary !!
        }

        if ((kernelType == MonetClessEMVKernel.MONEMV_CLS_KERNEL_MASTERCARD) ||
                (kernelType == MonetClessEMVKernel.MONEMV_CLS_KERNEL_PURE) ||
                (kernelType == MonetClessEMVKernel.MONEMV_CLS_KERNEL_PRIVATE)) {
            tlv.addTagBinary(0x9F09, "0002");            // Application Version Number - Terminal

            tlv.addTagBinary(MonetClessEMVKernel.MONEMV_CLS_TERMCAP_CVM_REQ, "E068C8");
            tlv.addTagBinary(MonetClessEMVKernel.MONEMV_CLS_TERMCAP_NO_CVM_REQ, "E008C8");

            tlv.addTagBinary(0xDF03, "D84000A800");    // TAC default
            tlv.addTagBinary(0xDF04, "0010000000");    // TAC denial
            tlv.addTagBinary(0xDF05, "D84004F800");    // TAC online
        }

        if (cakeyslist != null) {
            tlv.addTag(MonetClessEMVKernel.MONEMV_CLS_CAPK_INDEX_LIST, cakeyslist);
        }

        byte[] array = tlv.serialize();
        String data = Utils.bytesToHex(array);
        Log.i(TAG, "AIDData: " + data);

        int result = kernel.addAID(aid, array);
        return (result >= 0);
    }
}
