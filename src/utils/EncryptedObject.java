package utils;

public class EncryptedObject {

    private final byte[] dataOne;
    private final byte[] dataTwo;

    public EncryptedObject(byte[] data1, byte[] data2) {
        dataOne = data1;
        dataTwo = data2;
    }

    public byte[] getDataOne() {
        return dataOne;
    }

    public byte[] getDataTwo() {
        return dataTwo;
    }

}
