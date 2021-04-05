package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encryptor {

    private final File fileOrigin;
    private final File fileDestiny;
    private final FileInputStream fis;

    public Encryptor(File file) throws FileNotFoundException {
        this.fileOrigin = file;
        this.fis = new FileInputStream(this.fileOrigin);
        this.fileDestiny = new File(fileOrigin.getAbsolutePath() + ".encrypt");
    }

    public EncryptedObject delegateTasks() {
        //Delegaremos a dos hilos que obtengan los bytes en crudo de un archivo
        //El primer hilo obtendrá la mitad y el segundo el resto
        ExecutorService executorService = Executors.newCachedThreadPool();

        //Obtenemos la longitud en bytes del archivo
        long totalBytesSize = fileOrigin.length();
        System.out.printf("El hilo delegador %s ha obtenido un archivo de %d bytes.%n", Thread.currentThread().getName(), totalBytesSize);
        //Obtenemos los bytes que leerá el primer hilo
        long bytesTaksOne = totalBytesSize / 2;
        //Obtenemos los bytes que leerá el segndo hilo
        long bytesTaskTwo = totalBytesSize - bytesTaksOne;

        //Creamos las tareas
        BytesReader readerOne = new BytesReader((int) bytesTaksOne, fileOrigin);
        BytesReader readerTwo = new BytesReader((int) bytesTaskTwo, fileOrigin, (int) bytesTaksOne);
        //Lanzamos las tareas
        Future<byte[]> futureOne = executorService.submit(readerOne);
        Future<byte[]> futureTwo = executorService.submit(readerTwo);

        EncryptedObject encryptedData = null;
        try {
            byte[] resultado1 = futureOne.get();
            byte[] resultado2 = futureTwo.get();
            encryptedData = new EncryptedObject(resultado1, resultado2);

            //System.out.println(Arrays.toString(encryptedData.getDataOne()));
            //System.out.println(Arrays.toString(encryptedData.getDataTwo()));
            //System.out.println(Arrays.toString(encryptedData.getAllData()));

            //Escribimos los nuevos bytes en el fichero de destino
            writeFile(encryptedData.getAllData());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executorService.shutdown(); //Se debe apagar el ExecutorService
        }
        return encryptedData;
    }

    private void writeFile(byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileDestiny);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getFileDestiny() {
        return fileDestiny.getAbsolutePath();
    }

}
