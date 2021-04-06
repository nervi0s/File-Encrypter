package utils;

import exceptions.FileExtensionException;
import java.io.File;

public class Decryptor {

    private final File fileOrigin;
    private final File fileDestiny;

    public Decryptor(File file) throws FileExtensionException {
        this.fileOrigin = file;
        //Se obtiene la ruta del fichero target y se le quita el extensión ".encrypt"
        String nameOriginFile = fileOrigin.getAbsolutePath();
        //Comprobamos que tenga la extensión correcta
        if (!nameOriginFile.endsWith(".encrypt")) {
            throw new FileExtensionException("El archivo no tiene la extensión correcta");
        } else {
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
}
