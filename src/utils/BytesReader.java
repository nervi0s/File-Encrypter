package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;

public class BytesReader implements Callable<byte[]> {

    private final int numberBytesToRead; // Bytes a leer
    private final byte[] chunkData; // Cantidad de datos a almacenar
    private final File file; // Archivo target
    private final Integer bytesToSkip; // Puede que tenga que saltar bytes para leer la siguiente parte del archivo

    public BytesReader(int bytesAmount, File f) { // Constructor si no hay que saltar bytes
        numberBytesToRead = bytesAmount;
        chunkData = new byte[numberBytesToRead];
        file = f;
        bytesToSkip = null;
    }

    public BytesReader(int bytesAmount, File f, Integer bytesToSkip) { // Cosntructor para saltar bytes
        numberBytesToRead = bytesAmount;
        chunkData = new byte[numberBytesToRead];
        file = f;
        this.bytesToSkip = bytesToSkip;
    }

    @Override
    public byte[] call() throws Exception {
        System.out.printf("Hilo %s ha empezado a leer %d bytes del fichero.%n", Thread.currentThread().getName(), numberBytesToRead);
        Thread.sleep(5000); // Simulación de timepo prolongado de trabajo/lectura para esta tarea
        readBytesFromFile(file);
        return chunkData;
    }

    private void readBytesFromFile(File file) throws FileNotFoundException, IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        if (bytesToSkip != null) {
            raf.seek(bytesToSkip); // Saltamos bytes
        }
        int sizeDataReaded = raf.read(chunkData); // Almacenamos en chunkData la cantidad de chunkData.lenght bytes
        System.out.printf("El hilo %s ha leído %d bytes.%n", Thread.currentThread().getName(), sizeDataReaded);
    }

}
