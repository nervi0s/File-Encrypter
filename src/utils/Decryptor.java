package utils;

import exceptions.BytesControlException;
import exceptions.FileExtensionException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Decryptor {

    private final File fileOrigin;
    private final File fileDestiny;

    public Decryptor(File file) throws FileExtensionException, BytesControlException {
        this.fileOrigin = file;
        //Se obtiene la ruta del fichero target y se le quita la extensión ".encrypt"
        String nameOriginFile = fileOrigin.getAbsolutePath();
        //Comprobamos que tenga la extensión correcta
        if (!nameOriginFile.endsWith(".encrypt")) {
            throw new FileExtensionException("El archivo no tiene la extensión correcta");
        } else {
            //Controlamos que tengan los bytes de control 
            if (!controlBytesPresent()) {
                throw new BytesControlException("El arcvhivo no contiene los datos correctos");
            }
            //Borramos la extensión ".encrypt"
            int index = nameOriginFile.indexOf(".encrypt");
            String posibleFileFinalName = nameOriginFile.substring(0, index);
            //Comprobamos que no existe ya un archivo en esa ruta sin la extensión
            //Se usa un archivo temporal para comprobar si existe o no el archivo en esa ruta sin la extensión
            File tempFile = new File(posibleFileFinalName);
            if (tempFile.exists()) { //Si ya hay un archivo con ese nombre
                //Buscamos el último "." del nombre
                int lastIndex = -1;
                for (int i = 0; i < posibleFileFinalName.length(); i++) {
                    if (posibleFileFinalName.charAt(i) == '.') {
                        lastIndex = i;
                    }
                }
                //Actualizamos el nombre
                String extension = posibleFileFinalName.substring(lastIndex, posibleFileFinalName.length());
                String body = posibleFileFinalName.substring(0, lastIndex);
                body += "DECRYPTED";
                String fileFinalName = body + extension;
                this.fileDestiny = new File(fileFinalName);
            } else { //En caso de no haberlo usamos ese nombre;
                this.fileDestiny = tempFile;
            }
        }
    }

    //Se controlan que los 3 últimos bytes sean los de control
    private boolean controlBytesPresent() {
        //Analizamos los 3 últimos bytes
        int bytesToSkip = (int) (fileOrigin.length() - 3);

        BytesReader bytesReader = new BytesReader(3, fileOrigin, bytesToSkip);
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<byte[]> future = executorService.submit(bytesReader);

        try {
            byte[] bytes = future.get(); //Obtenemos los 3 últmos bytes
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] != EncryptedObject.CONTROL_BYTE) {
                    return false;
                }
            }
            return true;
        } catch (InterruptedException ex) {
            Logger.getLogger(Decryptor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Decryptor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executorService.shutdown();
        }
        return false;
    }

    //Leer todos los bytes execpto los 3 últimos
    public EncryptedObject delegateTasks() {
        //Delegaremos a dos hilos que obtengan los bytes cifrados de un archivo
        //El primer hilo obtendrá la mitad y el segundo el resto
        ExecutorService executorService = Executors.newCachedThreadPool();

        //Obtenemos la longitud en bytes del archivo quitando los bytes de control
        long totalBytesSize = fileOrigin.length() - 3;
        System.out.printf("El hilo delegador-desencriptador %s ha obtenido un archivo de %d bytes.%n", Thread.currentThread().getName(), totalBytesSize);
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
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executorService.shutdown(); //Se debe apagar el ExecutorService
        }
        return encryptedData;
    }

    //Devolvemos los bytes restaurados para la posterior reconstrucción del archivo
    public byte[] restoreBytes(EncryptedObject objEncrypted) {
        //Primero recreamos los bytes en un solo objeto
        List<Byte> byteList = new ArrayList<>();
        //Añadimos los primeros bytes pero ya modificados para ser como los originales
        for (Byte aByte : modifyBytes(objEncrypted.getDataOne())) {
            byteList.add(aByte);
        }
        //Añadimos los últimos bytes también modificados para ser como los originales
        for (Byte aByte : modifyBytes(objEncrypted.getDataTwo())) {
            byteList.add(aByte);
        }
        //Convertimos la lista en un array 
        byte[] allBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            allBytes[i] = byteList.get(i);
        }
        //Devolvemos el bytes originales
        return allBytes;
    }

    //Se reonstruye el archivo original
    public void restoreFile(byte[] bytesToWrite) {
        try {
            BufferedOutputStream bof = new BufferedOutputStream(new FileOutputStream(fileDestiny));
            bof.write(bytesToWrite);
            bof.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Decryptor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Decryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Método para obtener la ruta del fichero descriptado
    public String getFileDestiny() {
        return fileDestiny.getAbsolutePath();
    }

    //Método para restaurar los bytes a su estado original
    private byte[] modifyBytes(byte[] encriptedBytes) {
        for (int i = 0; i < encriptedBytes.length; i++) {
            byte byteActual = encriptedBytes[i];

            if (i % 2 == 0) { // Si el byte actual ocupa una posicón par en el array
                byteActual -= 3;
            } else { // Si el byte actual ocupa una posicón impar en el array
                byteActual += 5;
            }
            // El byte actual quedará modificado
            encriptedBytes[i] = byteActual;
        }
        //Devolvemos el bytes originales
        return encriptedBytes;
    }

}
