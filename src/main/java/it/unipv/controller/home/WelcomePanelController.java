package it.unipv.controller.home;

import it.unipv.db.DBConnection;
import it.unipv.controller.login.RegistrazioneController;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller di resources/fxml/home/welcome.fxml
 * Questa classe viene utilizzata per mostrare, nella Home, un messaggio di benvenuto:
 *     viene mostrato il messaggio di benvenuto e, se non ci sono utenti loggati, mostra anche un tasto aggiuntivo di registrazione.
 */
public class WelcomePanelController {

    @FXML private AnchorPane welcomeFooter;
    @FXML private Label welcomeLabel, registerLabel;
    private Stage stageRegistrazione;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param loggedUser -> l'utente che è attualmente loggato, è null se non c'è nessuno loggato
     * @param dbConnection -> la connessione al database che serve al form di registrazione
     * @param stageRegistrazione -> lo stage di registrazione della home: utilizzo questo al posto di crearne uno nuovo
     *                                  soprattutto perché sia che lo apro da qua, sia che lo apro dalla home, viene
     *                                  segnalato che lo stage è aperto; questo perché è giusto che si apra solo un
     *                                  form di registrazione alla volta.
     */
    public void init(User loggedUser, DBConnection dbConnection, Stage stageRegistrazione) {
        this.stageRegistrazione = stageRegistrazione;
        if(loggedUser==null) {
            registerLabel.setOnMouseExited(event -> registerLabel.setTextFill(Color.WHITE));
            registerLabel.setOnMouseEntered(event -> registerLabel.setTextFill(Color.valueOf("db8f00")));
            registerLabel.setOnMouseClicked(event -> openRegisterPage(dbConnection));
        } else {
            welcomeFooter.setVisible(false);
            welcomeLabel.setText(loggedUser.getNome() + ", bentornato in Golden Movie Studio!");
        }
    }

    private void openRegisterPage(DBConnection dbConnection) {
        if(!stageRegistrazione.isShowing()) {
            doOpenRegisterPage(dbConnection);
        }
    }

    private void doOpenRegisterPage(DBConnection dbConnection) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/Registrazione.fxml"));
            Parent p = loader.load();
            RegistrazioneController rc = loader.getController();
            rc.init(dbConnection);
            stageRegistrazione.setScene(new Scene(p));
            stageRegistrazione.setResizable(false);
            stageRegistrazione.setTitle("Registrazione");
            stageRegistrazione.show();
            stageRegistrazione.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }
}
