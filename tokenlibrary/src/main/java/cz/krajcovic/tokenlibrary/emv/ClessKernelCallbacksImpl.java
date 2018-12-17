package cz.krajcovic.tokenlibrary.emv;

import android.nfc.tech.IsoDep;
import android.util.Log;
import cz.krajcovic.tokenlibrary.devices.Terminal;
import cz.monetplus.ogar.MonetClessEMVKernel;
import cz.monetplus.smartterminallibrary.devices.BeeperState;
import cz.monetplus.smartterminallibrary.devices.Devices;
import cz.monetplus.smartterminallibrary.devices.LedState;
import cz.monetplus.smartterminallibrary.devices.TextState;
import cz.monetplus.smartterminallibrary.utils.Hex;
import cz.monetplus.smartterminallibrary.utils.TLVBuffer;
import cz.monetplus.smartterminallibrary.utils.ca.CaKey;
import cz.monetplus.smartterminallibrary.utils.ca.CaValue;

import java.io.IOException;

public class ClessKernelCallbacksImpl implements MonetClessEMVKernel.KernelCallbacks {
    private static final String TAG = ClessKernelCallbacksImpl.class.getName();

    private Terminal terminal;

    private IsoDep isoDep;

    public ClessKernelCallbacksImpl(Terminal terminal) {
        this.terminal = terminal;
    }

    public void setIsoDep(IsoDep isoDep, int timeout) {
        this.isoDep = isoDep;
        if(isoDep != null) {
            Log.i(TAG, "ISO DEP timeout: " + timeout + "ms.");
            this.isoDep.setTimeout(timeout);
        }
    }

    public int powerOn(int cold, byte[] resp) {
        Log.i(TAG, "Called powerOn. cold: " + cold + " resp: " + Hex.encodeHexString(resp));

        byte[] data = isoDep.getHistoricalBytes();
        if (data != null) {
            System.arraycopy(data, 0, resp, 0, data.length);
            return data.length;
        } else {
            data = isoDep.getHiLayerResponse();
            if (data != null) {
                System.arraycopy(data, 0, resp, 0, data.length);
                return data.length;
            }
        }


        return 0;

    }

    public int powerOff() {
        Log.i(TAG, "Called powerOff.");
        return 0;
    }

    public int processAPDU(byte[] req, byte[] resp) {
        Log.i(TAG, "Called processAPDU. req: " + Hex.encodeHexString(req));
        byte[] isoResp = null;
        try {
            if (isoDep != null) {
                isoResp = isoDep.transceive(req);
                System.arraycopy(isoResp, 0, resp, 0, isoResp.length);
                Log.i(TAG, "Called processAPDU. resp: " + Hex.encodeHexString(isoResp));
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot receive isoDep", e);
        }

        return isoResp == null ? 0 : isoResp.length;
    }

    public boolean checkBlacklist(byte[] pan, byte[] seq) {
        Log.i(TAG, "Called checkBlacklist. pan: " + Hex.encodeHexString(pan) + " seq: " + Hex.encodeHexString(seq));
        return false;
    }

    public int getCAKey(byte[] keyInfo, byte[] keyValue) {
        Log.i(TAG, "Called getCAKey. keyInfo: " + Hex.encodeHexString(keyInfo));
        TLVBuffer tlvBuffer = new TLVBuffer();
        tlvBuffer.load(keyInfo);
        CaKey key = new CaKey(tlvBuffer.findTag(0x9F06), tlvBuffer.findTag(0x8F)[0]);
        int keyValueLength = 0;

        try {
            //CaValue value = this.terminal.getCaRecords().get(key);
            CaValue value = this.terminal.getCaRecords().get(key);
            tlvBuffer = new TLVBuffer();
            tlvBuffer.addTag(0xDF7F, value.getExp());
            tlvBuffer.addTag(0xDF1F, value.getMod());
            byte[] tmp = tlvBuffer.serialize();
            keyValueLength = tmp.length;
            System.arraycopy(tmp, 0, keyValue, 0, tmp.length);
        } catch (Exception var8) {
            Log.e(TAG, "Cannot get ca records", var8);
        }

        Log.i(TAG, "Called getCAKey. keyValue: " + Hex.encodeHexString(keyValue, keyValueLength));
        return keyValueLength;
    }

    public void changeGUI(int d, int state) throws InterruptedException {
        Devices device = Devices.valueOf(d);
        Log.i(TAG, "device: " + device + " state: " + state);
        switch (device) {
            case DEVICE_TEXT:
                this.deviceText(TextState.valueOf(state));
                break;
            case DEVICE_LOGO:
                this.deviceLogo();
                break;
            case DEVICE_LEDS:
                this.deviceLeds(LedState.valueOf(state));
                break;
            case DEVICE_BEEPER:
                this.deviceBeeper(BeeperState.valueOf(state));
                break;
            default:
                Log.e(TAG, "Invalid device type");
        }

    }

    private void deviceLeds(LedState state) {
        Log.i(TAG, "deviceLeds: " + state);
        try {
            this.terminal.displayLeds(state);
        } catch (Exception e) {
            Log.e(TAG, "Cannot set terminal leds", e);
        }
    }

    private void deviceLogo() {
        Log.i(TAG, "deviceLogo");
        try {
            this.terminal.displayLogo();
        } catch (Exception e) {
            Log.e(TAG, "Cannot display terminal logo", e);
        }

    }

    private void deviceText(TextState state) {
        Log.i(TAG, "deviceText: " + state);
        try {
            this.terminal.displayText(state);
        } catch (Exception e) {
            Log.e(TAG, "Cannot display terminal text", e);
        }

    }

    private void deviceBeeper(BeeperState state) {
        Log.i(TAG, "deviceBeeper: " + state);
        try {
            this.terminal.beeper(state);
        } catch (Exception e) {
            Log.e(TAG, "Cannot terminal beep", e);
        }

    }
}
