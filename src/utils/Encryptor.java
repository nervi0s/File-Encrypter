package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class Encryptor {

    private final File file;
    private final RandomAccessFile rFile;

    public Encryptor(File file) throws FileNotFoundException {
        this.file = file;
        this.rFile = new RandomAccessFile(file, "rw");
    }

}
