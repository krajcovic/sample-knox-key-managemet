package cz.krajcovic.tokenlibrary.utils;

public class EncryptedData {
    private byte[] data;
    private byte[] iv;

    public EncryptedData(byte[] data, byte[] iv) {
        this.data = data;
        this.iv = iv;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
