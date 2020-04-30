package it.unipv.controller.login;

import java.util.ArrayList;
import java.util.List;

import it.unipv.db.DBConnection;
import it.unipv.dao.UserDao;
import it.unipv.dao.UserDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller di resources/fxml/login/ForgotPassword.fxml
 * Questa classe viene utilizzata dare la possibilità all'utente di resettare la propria password, a patto però
 *     che si ricordi il codice utente fornitogli al momento della registrazione.
 */
public class ForgotPasswordController {

    @FXML private TextField usernameTextField, userCodeTextfield;
    @FXML private PasswordField passwordTextfield, retryPasswordTextfield;
    @FXML private Label cancelButton, infoButton;
    private List<User> users = new ArrayList<>();
    private LoginController loginController;
    private UserDao userDao;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param loginController -> il controller del login, utilizzato per segnalargli che una password è stata resettata
     *                               e che quindi si ha la necessità di ricaricare le informazioni degli utenti;
     * @param dbConnection -> la connessione al database con la quale si istanzia userDaoImpl.
     */
    public void init(LoginController loginController, DBConnection dbConnection) {
        this.loginController = loginController;
        this.userDao = new UserDaoImpl(dbConnection);
        initUserList();
        GUIUtils.setScaleTransitionOnControl(infoButton);
    }

    private void initUserList() { users = userDao.retrieveUserList(); }

    //Listener al tasto "Chiudi" della finestra, permette di chiudere la finestra
    @FXML private void doCancel() { close(); }

    private void close() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
    }

    //Listener al tasto di informazioni, apre un alert che consiglia cosa fare in caso di smarrimento del codice utente
    @FXML private void infoButtonListener() {
        GUIUtils.showAlert( Alert.AlertType.INFORMATION
                          , "Informazione"
                          , "Hai dimenticato il codice utente?"
                          , "Contattaci al numero 0256914783 o all'indirizzo studio@goldenmovie.com.\n"
                                  +  "Uno dei nostri tecnici si occupererà di eseguire il reset della password e di informati a procedura completata!");
    }

    //Listener al tasto di conferma, permette di confermare il reset della password (in caso di problemi esce il messaggio di errore)
    @FXML private void doConfirm() {
        if(checkIfAllTextfieldAreCompiled()) {
            if(checkIfExistAnUserLikeThat()) {
                if(checkIfPasswordContentAreEquals()) {
                    User user = getUserFromTextfield();
                    user.setPassword(passwordTextfield.getText());

                    userDao.updateUser(user);

                    GUIUtils.showAlert( Alert.AlertType.INFORMATION
                                       , "Informazione"
                                       , "Operazione riuscita: "
                                       , "La password dell'utente " + user.getNome() + " è stata correttamente cambiata in: " + user.getPassword());
                    loginController.triggerResettedPasswordEvent();
                    close();
                } else {
                    GUIUtils.showAlert(Alert.AlertType.ERROR,  "Errore", "Si è verificato un errore:", "Le password non coincidono!");
                }
            } else {
                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Non esiste un utente con questi dati!");
            }
        } else {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi compilare tutti i campi!");
        }
    }

    private boolean checkIfPasswordContentAreEquals() {
        return passwordTextfield.getText().equals(retryPasswordTextfield.getText());
    }

    private boolean checkIfExistAnUserLikeThat() {
        for(User u : users) {
            if(u.getNome().equals(usernameTextField.getText()) && u.getCodice().equals(userCodeTextfield.getText())) {
                return true;
            }
        }
        return false;
    }

    //Metodo che cerca l'utente in lista a partire dal suo nickname e dal codice utente
    private User getUserFromTextfield() {
        User res = null;
        for(User u : users) {
            if(u.getNome().equals(usernameTextField.getText()) && u.getCodice().equals(userCodeTextfield.getText())) {
                res = u;
                break;
            }
        }
        return res;
    }

    private boolean checkIfAllTextfieldAreCompiled() {
        return !usernameTextField.getText().equals("")
            && !userCodeTextfield.getText().equals("")
            && !passwordTextfield.getText().equals("")
            && !retryPasswordTextfield.getText().equals("");
    }
}
