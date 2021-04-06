package utils;

import exceptions.BytesControlException;
import exceptions.FileExtensionException;
import java.io.File;
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
        //Se obtiene la ruta del fichero target y se le quita el extensión ".encrypt"
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

        //Leer todos los bytes execpto los 3 últimos
        //Aplicar algotirmo a los bytes obtenidos
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
                if (bytes[i] != EncryptedObject.controlByte) {
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
    public EncryptedObject delegateTaks() {
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
        //Añadimos los primeros bytes
        for (Byte aByte : objEncrypted.getDataOne()) {
            byteList.add(aByte);
        }
        //Añadimos los últimos bytes
        for (Byte aByte : objEncrypted.getDataTwo()) {
            byteList.add(aByte);
        }
        //Convertimos la lista en un array y la devolvemos
        byte[] allBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            allBytes[i] = byteList.get(i);
        }
        return allBytes;
    }

    //Se reonstruye el archivo original
    public void restoreFile(byte[] bytesToWite) {

    }
}
