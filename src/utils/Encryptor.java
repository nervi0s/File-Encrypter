package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encryptor {

    private final File file;
    private final FileInputStream fis;

    public Encryptor(File file) throws FileNotFoundException {
        this.file = file;
        this.fis = new FileInputStream(this.file);
        delegateTasks();
    }

    private void delegateTasks() {
        //Delegaremos a dos hilos que obtengan los bytes en crudo de un archivo
        //El primer hilo obtendrá la mitad y el segundo el resto
        ExecutorService executorService = Executors.newCachedThreadPool();

        //Obtenemos la longitud en bytes del archivo
        long totalBytesSize = file.length();
        System.out.printf("El hilo delegador %s ha obtenido un archivo de %d bytes.%n", Thread.currentThread().getName(), totalBytesSize);
        //Obtenemos los bytes que leerá el primer hilo
        long bytesTaksOne = totalBytesSize / 2;
        //Obtenemos los bytes que leerá el segndo hilo
        long bytesTaskTwo = totalBytesSize - bytesTaksOne;

        //Creamos las tareas
        BytesReader readerOne = new BytesReader((int) bytesTaksOne, file);
        BytesReader readerTwo = new BytesReader((int) bytesTaskTwo, file, (int) bytesTaksOne);
        //Lanzamos las tareas
        Future<byte[]> futureOne = executorService.submit(readerOne);
        Future<byte[]> futureTwo = executorService.submit(readerTwo);

        try {
            byte[] resultado1 = futureOne.get();
            byte[] resultado2 = futureTwo.get();
            System.out.println(Arrays.toString(resultado1));
            System.out.println(Arrays.toString(resultado2));
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executorService.shutdown();
        }

    }

}
