package it.unipv.controller.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unipv.controller.common.IHomeTrigger;
import it.unipv.db.DBConnection;
import it.unipv.dao.UserDao;
import it.unipv.dao.UserDaoImpl;
import it.unipv.conversion.UserInfo;
import it.unipv.controller.common.GUIUtils;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Controller di resources/fxml/login/Login.fxml
 * Questa classe viene utilizzata per dare la possibilità all'utente di accedere al sistema del cinema.
 */
public class LoginController {

    @FXML private Label loginButton, passwordResetButton;
    @FXML private TextField usernameTextfield;
    @FXML private PasswordField passwordTextfield;
    @FXML private CheckBox rememberCheckbox;
    private IHomeTrigger homeController;
    private DBConnection dbConnection;
    private UserDao userDao;
    private List<User> userList = new ArrayList<>();

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param homeController -> serve per segnalare alla home (e alla statusBar) le operazioni effettuate;
     * @param dbConnection -> la connessione al database con la quale si istanzia userDaoImpl.
     */
    public void init(IHomeTrigger homeController, DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.userDao = new UserDaoImpl(dbConnection);
        this.homeController = homeController;
        GUIUtils.setScaleTransitionOnControl(passwordResetButton);
        initUserList();
    }

    private boolean isForgotPasswordStageOpened = false;
    @FXML private void passwordResetButtonListener() {
        if (!isForgotPasswordStageOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/ForgotPassword.fxml"));
                Parent p = loader.load();
                ForgotPasswordController fpc = loader.getController();
                fpc.init(this, dbConnection);
                Stage stage = new Stage();
                stage.setScene(new Scene(p));
                stage.setResizable(false);
                stage.setTitle("Resetta Password");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                stage.setOnCloseRequest( event -> isForgotPasswordStageOpened = false);
                stage.show();
                isForgotPasswordStageOpened = true;
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }
    }

    private void initUserList() { userList = userDao.retrieveUserList(); }

    //Listener al tasto ENTER: se si ha focus su una label e si preme ENTER viene eseguito il metodo di login
    @FXML private void enterKeyPressed(KeyEvent event) {
        if(event.getCode().equals(KeyCode.ENTER)){
            doLogin();
        }
    }

    //Listener al tasto di login, se premuto fa eseguire il metodo di login
    @FXML private void loginButtonListener() { doLogin(); }

    //Metodo che effettivamente si occupa del login; da errore se non si compila tutti i campi o se non esiste un utente con le credenziali inserite
    private void doLogin() {
        if(usernameTextfield.getText().equals("") || passwordTextfield.getText().equals("")){
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi compilare tutti i campi!");
        } else {
            homeController.triggerStartStatusEvent("Eseguo l'accesso...");
            User user = new User(usernameTextfield.getText().trim(), passwordTextfield.getText().trim());
            if(checkIfItIsAValidUserFromUserList(user)) {
                fullUserWithAllInfo(user);
                doRealLogin(user);

                if(rememberCheckbox.isSelected()) {
                    UserInfo.createUserInfoFileInUserDir( user.getNome()
                                                        , user.getPassword()
                                                        , user.getEmail()
                                                        , user.getCodice()
                                                        , DataReferences.INFOUSERDIR
                                                        , DataReferences.INFOUSERFILE);
                }

                homeController.triggerEndStatusEvent("Complimenti " + user.getNome() + ": accesso effettuato con successo!");
                doExit();
            } else {
                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Non esiste un utente con questo username!");
                homeController.triggerEndStatusEvent("Errore: utente non riconosciuto!");
            }
        }
    }

    //Metodo che segnala alla Home di impostare la UI per l'utente che si è appena connesso
    private void doRealLogin(User user) {
        homeController.triggerNewLogin(user);
    }

    //Metodo che si occupa di controllare se l'utente inserito esiste nella lista utenti
    private boolean checkIfItIsAValidUserFromUserList(User u) {
        boolean flag = false;
        for(User user : userList) {
            if( u.getNome().trim().equals(user.getNome())
             && u.getPassword().trim().equals(user.getPassword()) ) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    //Metodo che si occupa di inserire tutte le informazioni dell'utente inserito
    private void fullUserWithAllInfo(User u) {
        for(User user : userList) {
            if( u.getNome().trim().equals(user.getNome())
             && u.getPassword().trim().equals(user.getPassword()) ) {
                u.setEmail(user.getEmail());
                u.setCodice(user.getCodice());
                break;
            }
        }
    }

    //Listener al tasto "Annulla", permette la chiusura della finestra
    @FXML private void doCancel(){ doExit(); }

    private void doExit(){ ((Stage) loginButton.getScene().getWindow()).close(); }

    /** Notifica il reset di una password utente */
    void triggerResettedPasswordEvent() { initUserList(); }
}