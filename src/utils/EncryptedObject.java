package utils;

import java.util.ArrayList;
import java.util.List;

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

    public byte[] getAllData() {
        List<Byte> bytesList = new ArrayList<>();

        // Se añaden los primeros bytes
        for (byte byte_ : getDataOne()) {
            bytesList.add(byte_);
        }
        // Se añaden los últimos bytes
        for (byte byte_ : getDataTwo()) {
            bytesList.add(byte_);
        }
        // Se convierte la lista en array para su retorno
        byte[] allbytes = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++) {
            allbytes[i] = bytesList.get(i);
        }
        return allbytes;
    }

}
