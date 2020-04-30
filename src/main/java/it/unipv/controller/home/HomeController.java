package it.unipv.controller.home;

import it.unipv.controller.common.*;
import it.unipv.db.DBConnection;
import it.unipv.conversion.UserInfo;
import it.unipv.model.Movie;
import it.unipv.controller.login.LoginController;
import it.unipv.controller.login.RegistrazioneController;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller di resources/fxml/home/home.fxml
 * Questa classe è il controller principale da cui parte tutto il resto; è composta graficamente da:
 *     Pulsanti di Login/Logout
 *     Menu
 *     Panello principale (homePanel) che si aggiorna richiamando gli altri controller a seconda di ciò che viene clickato nel menu.
 *     Status bar, che contiene la label dei suggerimenti (tipsLabel), la label dello stato (statusLabel) e la progress bar dello status (statusPBar)
 * I suggerimenti sono animati da tipsThread, il quale viene stoppato al termine dell'esecuzione del programma;
 * Ci sono diversi trigger per l'aggiornamento delle pagine (se il Manager effettua cambiamenti è giusto che la home si aggiorni adeguatamente),
 *     e per la progress bar dello stato, poiché se vengono effettuate operazioni dal database è giusto mostrare all'utente una sorta di caricamento;
 * Implementa IHomeInitializer, invocato da chi deve avviare la classe (it.unipv.main.Home);
 * Implementa IHomeTrigger, invocato da chi deve utilizzare uno dei suoi trigger (i controller che vengono istanziati a seconda di ciò che si sceglie di fare).
 */
public class HomeController implements IHomeTrigger, IHomeInitializer {
    @FXML private Rectangle rectangleMenu;
    @FXML private AnchorPane menuWindow, menuContainer;
    @FXML private Label logLabel, nonRegistratoQuestionLabel, registerButton, areaRiservataButton, statusLabel, tipsLabel;
    @FXML private ProgressBar statusPBar;
    @FXML private AnchorPane logoutPane;
    @FXML private BorderPane homePanel;
    private final Stage stageRegistrazione = new Stage();
    private DBConnection dbConnection;
    private final Stage stageLogin = new Stage();
    private User loggedUser;
    private IManagerAreaInitializer managerHomeController;
    private IUserReservedAreaInitializer areaRiservataInitializer;
    private HallListPanelController hallListPanelController;
    private MovieListPanelController movieListPanelController;
    private Stage reservedAreaStage, managerAreaStage;
    private List<ICloseablePane> iCloseablePanes = new ArrayList<>();
    private Thread tipsThread;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * Imposta la situazione di partenza del programma: verifica se ci siano utenti salvati nelle informazioni
     *     per eseguire un login automatico; imposta lo stato di visibilità degli elementi (ad esempio, se
     *     esiste un utente precedentemente salvato mostra il pulsante di logout, viceversa lo nasconde);
     *     infine fa partire il thread dei suggerimenti, che viene stoppato alla chiusura del programma.
     * @param dbConnection -> è la connessione al database che viene istanziata all'apertura del programma da
     *                        it.unipv.main.Home e che verrà poi passata a tutti gli altri controller invocati.
     */
    @Override
    public void init(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        if(checkIfThereIsAlreadyUserSaved()) {
            loggedUser = UserInfo.getUserInfo(DataReferences.INFOUSERFILE);
            setupLoggedUser();
        } else {
            logoutPane.setVisible(false);
            areaRiservataButton.setVisible(false);
        }
        initWelcomePage(loggedUser);
        menuWindow.setVisible(false);
        menuWindow.setPickOnBounds(false);
        menuContainer.setPickOnBounds(false);
        statusLabel.setVisible(false);
        statusPBar.setVisible(false);
        animateTipsLabel();
    }

    // Metodo che gestisce il thread dei suggerimenti della status bar;
    private void animateTipsLabel() {
        tipsThread = GUIUtils.getTipsThread(DataReferences.HOMETIPS, tipsLabel, 10000);
        tipsThread.start();
    }

    //La pagina di benvenuto viene inizializzata quando si avvia il programma e ogni volta che si effettua un logout
    private void initWelcomePage(User user) {
        homePanel.getChildren().clear();
        WelcomePanelController wpc = openNewPanel("/fxml/home/welcome.fxml").getController();
        wpc.init(user, dbConnection, stageRegistrazione);
    }

    /* *********************************************************** METODI RIGUARDANTI IL MENÙ *********************************************************** */
    //Animazione di apertura del menu. È richiamato al click (mousePressed) sull'imageView delle tre righe del menu.
    @FXML
    private void openMenu() {
        if(!menuWindow.isVisible()) {
            menuWindow.setOpacity(0);
            menuWindow.setVisible(true);
            new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(rectangleMenu.widthProperty(), rectangleMenu.getWidth() +81))).play();
            new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(rectangleMenu.heightProperty(), rectangleMenu.getHeight()+244))).play();

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), menuWindow);
            fadeIn.setDelay(Duration.seconds(0.2));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    //Animazione di chiusura del menu. Viene richiamato ogni qual volta il menu dovrebbe essere chiuso.
    private void closeMenu() {
        if(menuWindow.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.1), menuWindow);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.play();
            menuWindow.setVisible(false);

            new Timeline(new KeyFrame(Duration.seconds(0.15), new KeyValue(rectangleMenu.heightProperty(), rectangleMenu.getHeight() - 244))).play();
            new Timeline(new KeyFrame(Duration.seconds(0.15), new KeyValue(rectangleMenu.widthProperty(), rectangleMenu.getWidth() - 81))).play();
        }
    }

    //Listener al click esterno al menu: in questo caso, se io clicco esternamente al menu aperto, è meglio che esso si chiuda.
    @FXML
    private void panelCloseMenuListener() {
        if(menuWindow.isVisible()) { closeMenu(); }
    }

    /* Metodo invocato al click del pulsante "Programmazione" del menu:
     *     ci sono due frame perché bisogna dare il tempo al menù di richiudersi finendo la sua animazione
     *     dopo che il menù si è chiuso parte il frame che inizializza la lista dei film.
    */
    @FXML
    private void programmazioneClick() {
        closeAllSubWindows();
        KeyFrame kf1 = new KeyFrame(Duration.millis(100), e -> closeMenu());
        KeyFrame kf2 = new KeyFrame(Duration.millis(290), e -> openProgrammationPanel());
        Platform.runLater(new Timeline(kf1,kf2)::play);
    }

    /* Metodo invocato al click del pulsante "Lista Sale" del menu:
     *     ci sono due frame perché bisogna dare il tempo al menù di richiudersi finendo la sua animazione
     *     dopo che il menù si è chiuso parte il frame che inizializza la lista delle sale.
    */
    @FXML
    private void salaClick() {
        closeAllSubWindows();
        KeyFrame kf1 = new KeyFrame(Duration.millis(100), e -> closeMenu());
        KeyFrame kf2 = new KeyFrame(Duration.millis(290), e -> openHallList());
        Platform.runLater(new Timeline(kf1,kf2)::play);
    }

    /* Metodo invocato al click del pulsante "Info e Orari" del menu
     * Qua non ho bisogno dei due frame perché le informazioni non vengono prese dal database
     *     e quindi non ho un tempo di attesa che potrebbe bloccarmi la UI;
    */
    @FXML
    private void infoClick() {
        closeAllSubWindows();
        openInfo();
        closeMenu();
    }

    private void openProgrammationPanel() {
        movieListPanelController = openNewPanel("/fxml/home/movieList.fxml").getController();
        movieListPanelController.init(this, dbConnection);
    }

    private void openHallList() {
        hallListPanelController = openNewPanel("/fxml/home/hallList.fxml").getController();
        hallListPanelController.init(this, dbConnection);
        if(!iCloseablePanes.contains(hallListPanelController)) { iCloseablePanes.add(hallListPanelController); }
    }

    private void openInfo() {
        openNewPanel("/fxml/home/info.fxml");
    }

    //Metodo che serve per caricare un nuovo pannello
    private FXMLLoader openNewPanel(String fxmlpath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlpath));
            AnchorPane pane = loader.load();
            pane.prefWidthProperty().bind(homePanel.widthProperty());
            pane.prefHeightProperty().bind(homePanel.heightProperty());
            homePanel.setCenter(pane);
            return loader;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    /* ************************************************************************************************************************************************** */

    /* *********************************************************** APERTURA/CHIUSURA LOGIN-REGISTRAZIONE-AREARISERVATA *********************************************************** */
    @FXML private void areaRiservataClick() {
        openReservedArea();
        closeMenu();
    }

    @FXML private void registrationWindow(){
        if(!stageRegistrazione.isShowing()){
            if(loggedUser==null) {
                openRegistrazione();
                closeMenu();
            } else {
                doLogout();
                closeMenu();
            }
        }
    }

    //Metodo invocato al click del tasto "Login" (visibile se non si è loggati)
    @FXML private void loginWindow(){
        if(!stageLogin.isShowing()){
            if(loggedUser==null) {
                openLogin();
            } else {
                openReservedArea();
            }
        }
    }

    //Metodo invocato al click del tasto "Logout" (visibile se si è loggati)
    @FXML private void logoutListener() { doLogout(); }

    private void openReservedArea() {
        if(isHimAnAdmin(loggedUser)) {
            doOpenManagerArea();
        } else {
            doOpenReservedArea();
        }
    }

    /* Metodo che effettivamente apre l'area manager; ne apre una alla volta, nel senso che se è già aperta non ne apre un'altra.
     *     Alla chiusura dell'area manager, vengono chiuse anche tutte le sottofinestre ad esse relativa.
     * FXML caricato -> resources/fxml/managerarea/ManagerHome.fxml
    */
    private boolean isManagerAreaOpened = false;
    private void doOpenManagerArea() {
        if(!isManagerAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/managerarea/ManagerHome.fxml"));
                Parent root = loader.load();
                managerHomeController = loader.getController();
                managerHomeController.init(this, dbConnection);
                managerAreaStage = new Stage();
                managerAreaStage.setScene(new Scene(root));
                managerAreaStage.setTitle("Area Manager");
                managerAreaStage.setMinHeight(710);
                managerAreaStage.setMinWidth(1020);
                managerAreaStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                managerAreaStage.setOnCloseRequest( event -> {
                    isManagerAreaOpened = false;
                    managerHomeController.closeAllSubWindows();
                });
                managerAreaStage.show();
                isManagerAreaOpened = true;
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }

    }

    /* Metodo che effettivamente apre l'area riservata utente; ne apre una alla volta, nel senso che se è già aperta non ne apre un'altra.
     *     Alla chiusura dell'area riservata, vengono chiuse anche tutte le sottofinestre ad esse relativa.
     * FXML caricato -> resources/fxml/userarea/AreaRiservataHome.fxml
    */
    private boolean isReservedAreaOpened = false;
    private void doOpenReservedArea() {
        if(!isReservedAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userarea/AreaRiservataHome.fxml"));
                Parent p = loader.load();
                areaRiservataInitializer = loader.getController();
                areaRiservataInitializer.init(loggedUser, dbConnection);
                reservedAreaStage = new Stage();
                reservedAreaStage.setScene(new Scene(p));
                reservedAreaStage.setMinHeight(710);
                reservedAreaStage.setMinWidth(1020);
                reservedAreaStage.setTitle("Area riservata di " + loggedUser.getNome());
                reservedAreaStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                reservedAreaStage.setOnCloseRequest( event -> {
                    isReservedAreaOpened = false;
                    areaRiservataInitializer.closeAllSubWindows();
                });
                reservedAreaStage.show();
                isReservedAreaOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    //Metodo che effettivamente apre la pagina di registrazione, caricando resources/fxml/login/Registrazione.fxml
    private void openRegistrazione(){
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

    //Metodo che effettivamente apre la pagina di login, caricando resources/fxml/login/Login.fxml
    private void openLogin(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/Login.fxml"));
            Parent p = loader.load();
            LoginController lc = loader.getController();
            lc.init(this, dbConnection);
            stageLogin.setScene(new Scene(p));
            stageLogin.setTitle("Login");
            stageLogin.setResizable(false);
            stageLogin.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
            stageLogin.show();
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    //Metodo utilizzato per chiudere l'area manager, compresa ogni sua sottofinestra, richiamato alla chiusura del cinema
    private void closeManagerArea() {
        if(managerAreaStage != null) {
            if(managerAreaStage.isShowing()) {
                isManagerAreaOpened = false;
                managerHomeController.closeAllSubWindows();
                managerAreaStage.close();
            }
        }
    }

    //Metodo utilizzato per chiudere l'area riservata, compresa ogni sua sottofinestra, richiamato alla chiusura del cinema
    private void closeReservedArea() {
        if(reservedAreaStage != null) {
            if(reservedAreaStage.isShowing()) {
                isReservedAreaOpened = false;
                areaRiservataInitializer.closeAllSubWindows();
                reservedAreaStage.close();
            }
        }
    }

    //Metodo utilizzato per chiudere ogni sottofinestra del programma principale
    private void closeAllSubWindows() {
        for(ICloseablePane i : iCloseablePanes) {
            i.closeAllSubWindows();
        }
    }

    /* Metodo utilizzato per chiudere tutto ciò che riguarda il cinema, utilizzato dalla classe principale
     *     it.unipv.main.Home alla chiusura totale del sistema
    */
    @Override
    public void closeAll() {
        if(tipsThread!=null) { tipsThread.interrupt(); }
        closeReservedArea();
        closeManagerArea();
        closeAllSubWindows();
    }
    /* *************************************************************************************************************************************************************************** */


    /* *********************************************************** SETUP LOGIN/LOGOUT *********************************************************** */
    /* Metodo utilizzato per impostare il programma una volta registrato un evento di login:
     *     viene settata la label "Login" con il nome dell'utente loggato ed al click permette l'apertura dell'area riservata a seconda del tipo di utente;
     *     viene mostrata e settata la label del menù "areaRiservata" con il nome dell'area riservata relativa all'utente che si è loggato;
     *     il tasto di registrazione del menu viene settato come tasto di logout;
     *     infine viene mostrata la pagina di welcome del cinema.
    */
    private void setupLoggedUser() {
        logLabel.setText(loggedUser.getNome());
        logoutPane.setVisible(true);
        nonRegistratoQuestionLabel.setText("Vuoi uscire?");
        registerButton.setText("logout");
        if(isHimAnAdmin(loggedUser)) {
            areaRiservataButton.setText("Area Manager");
        } else {
            areaRiservataButton.setText("Area Riservata");
        }
        areaRiservataButton.setVisible(true);
        initWelcomePage(loggedUser);
    }

    /* Metodo utilizzato per impostare il programma una volta registrato l'evento di logout:
     *    viene settata la label (che prima conteneva il nome dell'utente e che portava alla sua area riservata) come label che ora apre il form di login ;
     *    vengono mostrati i tasti di registrazione (del menu e della schermata di welcome);
     *    viene cancellato il file delle informazioni utente, visto che si è sloggato ;
     *    vengono chiuse tutte le sottofinestre relative al sistema intero.
    */
    private void doLogout() {
        triggerStartStatusEvent("Disconnessione in corso...");
        logLabel.setText("effettua il login");
        nonRegistratoQuestionLabel.setText("non sei registrato?");
        registerButton.setText("Registrati");
        areaRiservataButton.setVisible(false);
        logoutPane.setVisible(false);
        loggedUser = null;

        if(checkIfThereIsAlreadyUserSaved()) {
            UserInfo.deleteUserInfoFileInUserDir(DataReferences.INFOUSERFILE);
        }

        closeReservedArea();
        closeManagerArea();
        closeAllSubWindows();

        initWelcomePage(loggedUser);
        triggerEndStatusEvent("Disconnessione avvenuta con successo!");
    }

    private boolean isHimAnAdmin(User user) {
        return user.getNome().equalsIgnoreCase(DataReferences.ADMINUSERNAME)
            && user.getPassword().equalsIgnoreCase(DataReferences.ADMINPASSWORD);
    }

    /* Metodo che controlla se esiste un file di informazioni utente in data/utenti;
     *    se esiste vuol dire che l'utente ha scelto di ricordarsi i dati al login
     *    e che quindi il sistema deve accedere automaticamente all'avvio.
    */
    private boolean checkIfThereIsAlreadyUserSaved() {
        return UserInfo.checkIfUserInfoFileExists(DataReferences.INFOUSERFILE);
    }
    /* ****************************************************************************************************************************************** */

    /* *********************************************************** TRIGGER UTILIZZATI DA ALTRE CLASSI *********************************************************** */
    /** Metodo invocato dal form di login per segnalare alla Home che si è verificato un evento di Login. */
    @Override
    public void triggerNewLogin(User user) {
        closeAllSubWindows();
        loggedUser = user;
        setupLoggedUser();
    }

    /** Metodo utilizzato per segnalare alla Home e, di conseguenza, al panel dei film, un cambiamento nella lista film. */
    @Override
    public void triggerNewMovieEvent() {
        if(movieListPanelController !=null) {
            movieListPanelController.triggerNewMovieEvent();
        }
    }

    /** Metodo utilizzato per segnalare alla home e, di conseguenza, al panel delle sale, un cambiamento nella lista sale. */
    @Override
    public void triggerNewHallEvent() {
        if(hallListPanelController !=null) {
            hallListPanelController.triggerNewHallEvent();
        }
    }

    /** Metodo utilizzato per segnalare alla Home che l'utente ha clickato su un film:
     *     al click viene aperta la schermata di visualizzazione delle informazioni del singolo film.
    */
    @Override public void triggerMovieClicked(Movie movie) {
        openSingleMoviePanel(movie);
    }

    private void openSingleMoviePanel(Movie movie) {
        SingleMoviePanelController smpc = openNewPanel("/fxml/home/singleMoviePanel.fxml").getController();
        smpc.init(this, movie, loggedUser, dbConnection);
        if(!iCloseablePanes.contains(smpc)) { iCloseablePanes.add(smpc); }
    }

    /** Metodo utilizzato per segnalare alla Home di ritornare alla lista dei film. */
    @Override public void triggerOpenProgrammationPanel() { openProgrammationPanel(); }

    /** Metodo utilizzato per segnalare alla Home di aprire l'area riservata utente. */
    @Override public void triggerOpenReservedArea() { doOpenReservedArea(); }

    /** Metodo utilizzato per segnalare lo status iniziale dell'operazione in corso. */
    @Override
    public void triggerStartStatusEvent(String text) {
        statusLabel.setVisible(true);
        statusPBar.setVisible(true);
        statusLabel.setText(text);
        statusPBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }

    /** Metodo utilizzato per segnalare lo status finale dell'operazione in corso. */
    private static Timeline timeline;
    @Override
    public void triggerEndStatusEvent(String text) {
        if(timeline!=null) { timeline.stop(); }

        KeyFrame kf1 = new KeyFrame(Duration.millis(100), event -> {
            statusLabel.setText(text);
            statusPBar.setProgress(100);
        });

        KeyFrame kf2 = new KeyFrame(Duration.seconds(5), e -> {
            statusLabel.setVisible(false);
            statusPBar.setVisible(false);
        });

        timeline = new Timeline(kf1, kf2);

        Platform.runLater(timeline::play);
    }
    /* ********************************************************************************************************************************************************** */
}
