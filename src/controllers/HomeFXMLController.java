/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

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

    }

    @FXML
    private void chooseFile(ActionEvent event) {

        File selectedFile = fileChooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            textFieldFileLocation.setText(selectedFile.getAbsolutePath());
        } else {

        }
    }

}
