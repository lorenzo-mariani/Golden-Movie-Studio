package it.unipv.controller.userarea;

import it.unipv.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller di resources/fxml/userarea/HomePanel.fxml
 * Questa classe viene utilizzata per mostrare le informazioni principali dell'utente,
 *     soprattutto il codice fornito in fase di registrazione
 */
public class HomePanelController {

    @FXML private Label usernameInjectedLabel;
    @FXML private Label emailInjectedLabel;
    @FXML private Label codeInjectedLabel;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param loggedUser -> l'utente connesso al sistema di cui mostrare le informazioni
     */
    public void init(User loggedUser) {
        usernameInjectedLabel.setText(loggedUser.getNome());
        emailInjectedLabel.setText(loggedUser.getEmail());
        codeInjectedLabel.setText(loggedUser.getCodice());
    }
    
}
