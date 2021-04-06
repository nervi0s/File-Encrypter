package utils;

import java.util.ArrayList;
import java.util.List;

public class EncryptedObject {

    private final byte[] dataOne;
    private final byte[] dataTwo;
    public static final byte controlByte = (byte) 129;

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
        // Se añaden los bytes de control
        putControlBytes(bytesList);
        // Se convierte la lista en array para su retorno
        byte[] allbytes = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++) {
            allbytes[i] = bytesList.get(i);
        }
        return allbytes;
    }

    //Método para añadir al final del archivo bytes de control
    public void putControlBytes(List<Byte> bytesList) {
        //Se pondrá al final del archivo 3 bytes del tipo 10000001 (129) ó en Java (-127) que servirán como control para desencriptar
        bytesList.add(controlByte);
        bytesList.add(controlByte);
        bytesList.add(controlByte);
    }
}
