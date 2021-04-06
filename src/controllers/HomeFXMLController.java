/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import exceptions.BytesControlException;
import exceptions.FileExtensionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import utils.Decryptor;
import utils.EncryptedObject;
import utils.Encryptor;

/**
 * FXML Controller class
 *
 * @author wdal9
 */
public class HomeFXMLController implements Initializable {

    @FXML
    private RadioButton radioButtonEncrypt;
    @FXML
    private RadioButton radioButtonDecrypt;
    @FXML
    private TextField textFieldFileLocation;
    @FXML
    private TextArea textArea;

    // Se usará esta clase para la obteción del archivo
    private FileChooser fileChooser;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Generamos un ToggleGroup para los radio buttons   
        ToggleGroup radioGroup = new ToggleGroup();
        radioGroup.getToggles().addAll(radioButtonEncrypt, radioButtonDecrypt);

        fileChooser = new FileChooser();
    }

    @FXML
    private void goAction(ActionEvent event) {
        // Se obtiene la ruta del fichero elegido
        String pathTofile = textFieldFileLocation.getText();
        File fileTarget = new File(pathTofile);
        // Se comprueba la existencia del fichero con el if-else
        if (!fileTarget.exists()) {
            textArea.setText("El fichero que has indicado no existe");
        } else {
            if (!radioButtonDecrypt.isSelected() && !radioButtonEncrypt.isSelected()) {
                textArea.setText("Debes seleccionar una opción [Encrypt/Decrypt]");
            } else if (radioButtonEncrypt.isSelected()) {
                textArea.setText("");

                //Lanzamos un nuevo hilo para que la aplicación no sea bloqueante
                Thread threadEncryptor = new Thread(() -> {
                    try {
                        // Se inician las acciones para encriptar el archivo
                        Encryptor encryptor = new Encryptor(fileTarget);
                        EncryptedObject data = encryptor.delegateTasks(); //Cuando esta acción acabe tendremos los resultados
                        //Sincronizado para evitar escrituras inconsistentes
                        synchronized (textArea) {
                            textArea.setText("Fichero encriptado guardado en: " + encryptor.getFileDestiny());
                        }

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(HomeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                });
                threadEncryptor.start(); // Lanzamos el hilo creado

                //Lanzamos un hilo para mostrar info mientras se esté encriptando el archivo
                new Thread(() -> {

                    while (threadEncryptor.isAlive()) {
                        //Sincronizado para evitar escrituras inconsistentes
                        synchronized (textArea) {
                            textArea.setText(textArea.getText() + "Encrypting\n");
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(HomeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }).start();

            } else if (radioButtonDecrypt.isSelected()) {
                textArea.setText("");

                //Lanzamos un nuevo hilo para que la aplicación no sea bloqueante
                Thread threadDecryptor = new Thread(() -> {
                    try {
                        // Se inician las acciones para desencriptar el archivo
                        Decryptor decryptor = new Decryptor(fileTarget);
                        EncryptedObject data = decryptor.delegateTasks(); //Cuando esta acción acabe tendremos los resultados
                        //Restauramos los bytes del archivo
                        byte[] clearBytes = decryptor.restoreBytes(data);
                        //Creamos el archivo limpio
                        decryptor.restoreFile(clearBytes);
                        //Sincronizado para evitar escrituras inconsistentes
                        synchronized (textArea) {
                            textArea.setText("Fichero encriptado guardado en: " + decryptor.getFileDestiny());
                        }

                    } catch (FileExtensionException ex) {
                        Logger.getLogger(HomeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        textArea.setText(ex.getMessage());
                    } catch (BytesControlException ex) {
                        Logger.getLogger(HomeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        textArea.setText(ex.getMessage());
                    }

                });
                threadDecryptor.start(); // Lanzamos el hilo creado

                //Lanzamos un hilo para mostrar info mientras se esté encriptando el archivo
                new Thread(() -> {

                    while (threadDecryptor.isAlive()) {
                        //Sincronizado para evitar escrituras inconsistentes
                        synchronized (textArea) {
                            textArea.setText(textArea.getText() + "Decrypting\n");
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(HomeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }).start();
            }
        }
    }

    @FXML
    private void chooseFile(ActionEvent event) {
        File selectedFile = fileChooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
        if (selectedFile != null) { // Si se ha seleccionado algún archivo procedemos con las acciones
            textFieldFileLocation.setText(selectedFile.getAbsolutePath());
        }
    }
}
