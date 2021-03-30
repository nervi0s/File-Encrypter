package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Encryptor {

    private final File file;
    private final FileInputStream fis;

    public Encryptor(File file) throws FileNotFoundException {
        this.file = file;
        this.fis = new FileInputStream(this.file);
    }

    private void delegateTasks() {
        //Delegaremos a dos hilos que obtengan los bytes en crudo de un archivo
        //El primer hilo obtendrá la mitad y el segundo el resto
        ExecutorService executorService = Executors.newCachedThreadPool();
    }

}
