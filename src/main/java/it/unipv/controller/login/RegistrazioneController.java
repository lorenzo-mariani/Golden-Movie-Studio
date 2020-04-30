package it.unipv.controller.login;

import java.util.List;
import java.util.regex.Pattern;

import it.unipv.db.DBConnection;
import it.unipv.dao.UserDao;
import it.unipv.dao.UserDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.model.User;
import it.unipv.utils.ApplicationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Controller di resources/fxml/login/Registrazione.fxml
 * Questa classe viene utilizzata per dare la possibilità all'utente di potersi registrare.
 */
public class RegistrazioneController {

    @FXML private TextField usernameTextfield, emailTextfield;
    @FXML private PasswordField passwordTextfield, retryPasswordTextfield;
    private List<User> users;
    private UserDao userDao;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param dbConnection -> la connessione al database con la quale si istanzia userDaoImpl.
     */
    public void init(DBConnection dbConnection) {
        userDao = new UserDaoImpl(dbConnection);
        initUserList();
    }

    private void initUserList() {
        users = userDao.retrieveUserList();
    }

    /* Listener al tasto di registrazione, si occupa di verificare una serie di casi in cui non è possibile effettuare la registrazione
     *    e di dare il via alla registrazione vera e propria. I casi in cui non è possibile registrarsi sono:
     *        1) Non vengono compilati tutti i campi
     *        2) Il nome utente esiste già in lista
     *        3) La mail esiste già in lista
     *        4) Viene inserita una E-mail non valida (non conforme allo standard)
     *        5) Le password non coincidono
     */
    @FXML private void doRegister() {
        if( usernameTextfield.getText().equalsIgnoreCase("")
         || emailTextfield.getText().equalsIgnoreCase("")
         || passwordTextfield.getText().equalsIgnoreCase("")
         || retryPasswordTextfield.getText().equalsIgnoreCase("")) {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi compilare tutti i campi!");
        } else {
            if(!isAlreadyThereThisUsername()) {
                if(!isAlreadyThereThisEmail()) {
                    if(isEmailValid()) {
                        if(passwordTextfield.getText().equals(retryPasswordTextfield.getText())) {
                            doRealRegistrationAndShowConfirm();
                            doExit();
                        } else {
                            showError("Le password non coincidono!", passwordTextfield, retryPasswordTextfield);
                        }
                    } else {
                        showError("Non hai inserito una E-mail valida!", emailTextfield, passwordTextfield, retryPasswordTextfield);
                    }
                } else {
                    showError("E-mail già esistente!", emailTextfield, passwordTextfield, retryPasswordTextfield);
                }
            } else {
                showError("Nome utente già esistente!", usernameTextfield, passwordTextfield, retryPasswordTextfield);
            }
        }
    }

    //Metodo che si occupa di effettuare la registrazione vera e propria al database, assegnando prima all'utente il suo codice
    private void doRealRegistrationAndShowConfirm() {
        String codice = getUserCode();
        userDao.insertNewUser(new User( usernameTextfield.getText()
                                      , passwordTextfield.getText()
                                      , emailTextfield.getText()
                                      , codice));
        GUIUtils.showAlert( Alert.AlertType.INFORMATION
                          , "Info"
                          , "Informazione:"
                          , "Registrazione avvenuta con successo!\n"
                                   + "Codice utente: " + codice + ". Ricordati di non smarrirlo, potrebbe esserti utile per reimpostare la password!");
    }

    private void showError(String dialogMessageText, TextField... toClear) {
        GUIUtils.showAlert(Alert.AlertType.ERROR,  "Errore", "Si è verificato un errore:", dialogMessageText);
        clearTextField(toClear);
    }

    //Metodo che genera un codice univoco e casuale di 5 caratteri alfanumerici da assegnare all'utente che si è registrato
    private String getUserCode() {
        boolean shouldDie = false;
        String res = "";
        while(!shouldDie) {
            String codice = ApplicationUtils.getRandomCode(5, "0123456789abcdefghijklmnopqrstuvzxy");
            boolean status = false;
            for(User u : users) {
                if(u.getCodice().equalsIgnoreCase(codice)) {
                    status = true;
                    break;
                }
            }
            if(!status) {
                res = codice;
                shouldDie = true;
            }
        }
        return res;
    }

    private boolean isAlreadyThereThisUsername() {
        boolean status = false;
        for(User u : users) {
            if(u.getNome().equalsIgnoreCase(usernameTextfield.getText())) {
                status = true;
                break;
            }
        }
        return status;
    }

    private boolean isAlreadyThereThisEmail() {
        boolean status = false;
        for(User u : users) {
            if(u.getEmail().equalsIgnoreCase(emailTextfield.getText())) {
                status = true;
                break;
            }
        }
        return status;
    }

    private void clearTextField(TextField... toClear) {
        for(TextField tf : toClear) {
            tf.setText("");
        }
    }

    /* Metodo che si occupa di verificare se una E-mail è valida;
     * Esempi di mail accettate:
     *     nome@dominio.it
     *     nome.cognome@dominio.com
     *     nome123@dominio.net
     *     123nome@dominio.org
     *     !nome@dominio.org
     * Esempi di mail NON accettate:
     *     ..@dominio.it
     *     nome.it
     *     nome@dominio
     *     nome
     */
    private boolean isEmailValid() {
        return Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
                              , Pattern.CASE_INSENSITIVE)
                .matcher(emailTextfield.getText()).matches();
    }


    //Listener al tasto "Annulla", permette la chiusura della finestra
    @FXML private void doCancel() { doExit(); }

    private void doExit(){ ((Stage) usernameTextfield.getScene().getWindow()).close(); }
}
