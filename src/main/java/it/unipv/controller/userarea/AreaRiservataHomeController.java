package it.unipv.controller.userarea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unipv.controller.common.GUIUtils;
import it.unipv.controller.common.IUserReservedAreaInitializer;
import it.unipv.controller.common.IUserReservedAreaTrigger;
import it.unipv.db.DBConnection;
import it.unipv.controller.common.ICloseablePane;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * Controller di resources/fxml/userarea/AreaRuservataHome.fxml
 * Questa classe viene utilizzata per mostrare l'interfaccia principale dell'area riservata utente; permette di:
 *     1) Entrare nel pannello "Home" per visualizzare le informazioni utente, soprattutto il Codice Utente fornito in registrazione;
 *     2) Entrare nel pannello delle "Prenotazioni", per poter scaricare la fattura;
 *     3) Entrare nel pannello dei "Film Visti", per poter vedere lo storico dei film e delle fatture relative;
 *     4) Entrare nel pannello dei "Suggerimenti", per poter vedere una lista di film che potrebbero interessare all'utente;
 */
public class AreaRiservataHomeController implements IUserReservedAreaInitializer, IUserReservedAreaTrigger {

    @FXML private BorderPane mainPanel;
    @FXML private Label homeLabel, prenotationsLabel, seenMoviesLabel, tipsLabel, exitLabel, animatedTipsLabel, statusLabel;
    @FXML private ProgressBar statusPBar;
    private List<Label> labels = new ArrayList<>();
    private User loggedUser;
    private List<ICloseablePane> iCloseablePanes = new ArrayList<>();
    private DBConnection dbConnection;
    private Thread animatedTipsThread;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe;
     * @param loggedUser -> l'utente che richiede di vedere la propria area riservata;
     * @param dbConnection -> la connessione al database che verrà condivisa a tutte le funzioni dell'area riservata.
     */
    @Override
    public void init(User loggedUser, DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.loggedUser = loggedUser;

        statusLabel.setVisible(false);
        statusPBar.setVisible(false);

        addLabelsToList();
        setOnMouseEnteredToLabels();
        setOnMouseExitedToLabels();
        initHomePane();
        animateTipsLabel();
    }

    // Metodo che gestisce il thread dei suggerimenti della status bar;
    private void animateTipsLabel() {
        animatedTipsThread = GUIUtils.getTipsThread(DataReferences.USERRESERVEDAREATIPS, animatedTipsLabel, 5000);
        animatedTipsThread.start();
    }

    private void addLabelsToList() {
        labels.add(homeLabel);
        labels.add(prenotationsLabel);
        labels.add(seenMoviesLabel);
        labels.add(tipsLabel);
        labels.add(exitLabel);
    }

    private String openedPane = "";

    private void setOnMouseEnteredToLabels() {
        for(Label l : labels) {
            l.setOnMouseEntered(e -> {
                l.setCursor(Cursor.HAND);
                if(!openedPane.toLowerCase().equalsIgnoreCase(l.getText().toLowerCase())) {
                    l.setStyle("-fx-background-color:#efc677;");
                }
            });
        }

    }

    private void setOnMouseExitedToLabels() {
        for(Label l : labels) {
            l.setCursor(Cursor.DEFAULT);
            l.setOnMouseExited(e -> {
                if(!openedPane.toLowerCase().equalsIgnoreCase(l.getText().toLowerCase())) {
                    l.setStyle("-fx-background-color:transparent;");
                }
            });
        }
    }

    //A seconda di quale label viene cliccata nel menu a sinistra, essa viene colorata per poi aprire il pannello corrispondente
    @FXML
    private void filterByOptions(MouseEvent event){
        Label label = (Label)event.getSource();

        switch(label.getText()) {
            case "HOME": {
                initHomePane();
                break;
            }

            case "PRENOTAZIONI":
                initPrenotationPane();
                break;

            case "FILM VISTI":
                openHistoryPanel();
                break;

            case "SUGGERIMENTI":
                openTipsPanel();
                break;

            case "ESCI":
                doExit();
                break;

            default:
                break;
        }
    }

    private void initHomePane() {
        if (!openedPane.equalsIgnoreCase("HOME")) {
            HomePanelController hpc = openNewPanel("HOME", homeLabel, "/fxml/userarea/HomePanel.fxml").getController();
            hpc.init(loggedUser);
        }
    }

    private void initPrenotationPane() {
        if(!openedPane.equals("PRENOTAZIONI")) {
            CurrentPrenotationPanelController cppc =
                    openNewPanel("PRENOTAZIONI", prenotationsLabel, "/fxml/userarea/CurrentPrenotationPanel.fxml").getController();
            cppc.init(this, loggedUser, dbConnection);
        }
    }

    private void openHistoryPanel() {
        if(!openedPane.equals("FILM VISTI")) {
            HistoryPanelController hpc = openNewPanel("FILM VISTI", seenMoviesLabel, "/fxml/userarea/HistoryPanel.fxml").getController();
            hpc.init(this, loggedUser, dbConnection);
            if(!iCloseablePanes.contains(hpc)) {
                iCloseablePanes.add(hpc);
            }
        }
    }

    private void openTipsPanel() {
        if(!openedPane.equals("SUGGERIMENTI")){
            TipsPanelController tpc = openNewPanel("SUGGERIMENTI", tipsLabel, "/fxml/userarea/TipsPanel.fxml").getController();
            tpc.init(this, loggedUser, dbConnection);
        }
    }

    //In chiusura chiudo tutte le sottofinestre e fermo il thread dei suggerimenti
    private void doExit() {
        Stage stage = (Stage) mainPanel.getScene().getWindow();
        stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        closeAllSubWindows();
        stage.close();
    }

    private FXMLLoader openNewPanel(String name, Label label, String fxmlpath) {
        try {
            label.setStyle("-fx-background-color:#db8f00");
            setTransparentOtherLabels(name);
            mainPanel.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlpath));
            AnchorPane pane = loader.load();
            mainPanel.setCenter(pane);
            openedPane = name;
            return loader;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private void setTransparentOtherLabels(String nameToExclude) {
        for(Label l : labels) {
            if(!l.getText().toLowerCase().equalsIgnoreCase(nameToExclude.toLowerCase())) {
                l.setStyle("-fx-background-color:transparent");
            }
        }
    }

    /**
     * Se evocato va a chiudere tutte le eventuali sottofinestre e ferma il thread dei suggerimenti dell'Area Riservata
     */
    @Override
    public void closeAllSubWindows() {
        for(ICloseablePane i : iCloseablePanes) {
            i.closeAllSubWindows();
        }
        if(animatedTipsThread != null) { animatedTipsThread.interrupt(); }
    }

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

        KeyFrame kf2 = new KeyFrame(Duration.seconds(4), e -> {
            statusLabel.setVisible(false);
            statusPBar.setVisible(false);
        });

        timeline = new Timeline(kf1, kf2);

        Platform.runLater(timeline::play);
    }
}
