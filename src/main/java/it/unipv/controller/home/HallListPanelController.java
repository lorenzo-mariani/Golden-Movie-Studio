package it.unipv.controller.home;

import it.unipv.controller.common.IHomeTrigger;
import it.unipv.db.DBConnection;
import it.unipv.dao.HallDao;
import it.unipv.dao.HallDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.controller.common.ICloseablePane;
import it.unipv.model.Seat;
import it.unipv.model.SeatTYPE;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.CloseableUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller di resources/fxml/home/hallList.fxml
 * Questa classe viene utilizzata per mostrare, nella Home, la lista delle sale presenti a sistema:
 *     viene mostrata l'anteprima e le informazioni riguardanti i posti a sedere presenti nella sala;
 *     se si clicca sull'anteprima si apre una finestra che mostra l'immagine a dimensioni "reali".
 */
public class HallListPanelController implements ICloseablePane {

    @FXML private ScrollPane hallPanel;
    private HallDao hallDao;
    private List<String> hallNames = new ArrayList<>();
    private List<Image> previews = new ArrayList<>();
    private int hallNamesSize = 0;
    private static int hallRowCount = 0;
    private static int hallColumnCount = 0;
    private static int columnMax;
    private GridPane grigliaSale = new GridPane();
    private Stage hallPreviewStage;
    private IHomeTrigger homeController;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param homeController -> serve per segnalare alla home (statusBar) le operazioni effettuate.
     * @param dbConnection -> la connessione al database con la quale si istanzia HallDaoImpl.
     */
    public void init(IHomeTrigger homeController, DBConnection dbConnection) {
        this.homeController = homeController;
        this.hallDao = new HallDaoImpl(dbConnection);
        columnMax = getColumnMaxFromPageWidth(hallPanel.getScene().getWindow().getWidth());
        createUI();
        checkPageDimension();
    }

    //Disegna l'interfaccia segnalando alla Home lo status (progressBar).
    private void createUI() {
        homeController.triggerStartStatusEvent("Carico le informazioni sulle sale...");
        Platform.runLater(() -> {
            initHallNameList();
            initPreview();
            initHallGrid();
        });
        homeController.triggerEndStatusEvent("Informazioni sulle sale correttamente caricate!");
    }

    //Istanzio la lista dei nomi delle sale
    private void initHallNameList() {
        hallNames = hallDao.retrieveHallNames();
        Collections.sort(hallNames);
        hallNamesSize = hallNames.size();
    }

    //Istanzio la lista delle preview, grazie alla lista dei nomi delle sale
    private void initPreview() {
        previews.clear();
        for(int i = 0; i<hallNamesSize; i++) {
            previews.add(hallDao.retrieveHallPreviewAsImage(hallNames.get(i), 220, 395, true, true));
        }
    }

    //Creo la visualizzazione in griglia delle sale
    private void initHallGrid() {
        grigliaSale.getChildren().clear();

        for(int i = 0; i<hallNamesSize; i++) {
            createViewFromPreviews(hallNames.get(i), previews.get(i));
        }

        hallRowCount = 0;
        hallColumnCount = 0;
    }

    //Creo la singola cella della griglia, contenete Preview e informazioni della singola sala
    private void createViewFromPreviews(String hallName, Image preview) {
        Font font = Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15);

        Label nomeSalaLabel = new Label(hallName);
        nomeSalaLabel.setFont(font);
        nomeSalaLabel.setTextFill(Color.WHITE);

        List<Seat> seatList = initDraggableSeatsList(nomeSalaLabel.getText().trim());

        Label numPostiTotaliLabel = new Label("Capienza: " + seatList.size() + " posti");
        numPostiTotaliLabel.setFont(font);
        numPostiTotaliLabel.setTextFill(Color.WHITE);

        int numPostiVIP = getSeatNumberPerType(seatList, SeatTYPE.VIP);
        int numPostiDisabili = getSeatNumberPerType(seatList, SeatTYPE.DISABILE);

        Label numPostiDisabiliLabel = new Label("Posti per disabili: " + numPostiDisabili);
        numPostiDisabiliLabel.setFont(font);
        numPostiDisabiliLabel.setTextFill(Color.WHITE);
        if (numPostiDisabili == 0) { numPostiDisabiliLabel.setVisible(false); }

        Label numPostiVIPLabel = new Label("Posti VIP: " + numPostiVIP);
        numPostiVIPLabel.setFont(font);
        numPostiVIPLabel.setTextFill(Color.WHITE);
        if (numPostiVIP == 0) { numPostiVIPLabel.setVisible(false); }

        grigliaSale.setHgap(150);
        grigliaSale.setVgap(60);

        ImageView snapHallView = new ImageView(preview);
        snapHallView.setOnMouseClicked(event -> openHallPreview(nomeSalaLabel.getText()));

        AnchorPane pane = new AnchorPane();
        if (hallColumnCount == columnMax) {
            hallColumnCount = 0;
            hallRowCount++;
        }
        grigliaSale.add(pane, hallColumnCount, hallRowCount);
        hallColumnCount++;

        hallPanel.setContent(grigliaSale);
        GridPane.setMargin(pane, new Insets(15, 0, 0, 15));

        nomeSalaLabel.setLayoutY(snapHallView.getLayoutY() + 133);
        numPostiTotaliLabel.setLayoutY(nomeSalaLabel.getLayoutY() + 15);
        numPostiDisabiliLabel.setLayoutY(numPostiTotaliLabel.getLayoutY() + 15);
        numPostiVIPLabel.setLayoutY(numPostiDisabiliLabel.getLayoutY() + 15);

        pane.getChildren().addAll( snapHallView
                                 , nomeSalaLabel
                                 , numPostiTotaliLabel
                                 , numPostiDisabiliLabel
                                 , numPostiVIPLabel);

        GUIUtils.setScaleTransitionOnControl(snapHallView);
    }

    //Metodo utilizzato al click su una preview: mostra, in una nuova finestra, l'immagine della sala a dimensioni "reali".
    private void openHallPreview(String nomeSala) {
        BorderPane borderPane = new BorderPane();

        Image image;
        InputStream fis = null;
        try {
            fis = hallDao.retrieveHallPreviewAsStream(nomeSala);
            image = new Image(fis);
        } finally {
            CloseableUtils.close(fis);
        }

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        borderPane.setCenter(imageView);
        hallPreviewStage = new Stage();
        hallPreviewStage.setTitle(nomeSala);
        Scene scene = new Scene(borderPane);
        hallPreviewStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        hallPreviewStage.setScene(scene);
        hallPreviewStage.show();
    }

    //Inizializzo la lista dei posti a sedere, così da ricavare le informazioni da mettere sotto alle preview
    private List<Seat> initDraggableSeatsList(String nomeSala) { return hallDao.retrieveSeats(nomeSala); }

    //Ricavo il numero di posti a sedere (di un determinato tipo) di una sala
    private int getSeatNumberPerType(List<Seat> mdsList, SeatTYPE type) {
        int res = 0;
        for(Seat mds : mdsList) {
            if(mds.getType().equals(type)) {
                res++;
            }
        }
        return res;
    }

    /** Se si verifica l'evento di una modifica o aggiunta di sala dalla parte manager, ricreo la UI ricaricando le informazioni */
    void triggerNewHallEvent() { createUI(); }

    //Metodo utilizzato per monitorare la dimensione della finestra e modificare la UI in base ai cambiamenti
    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) hallPanel.getScene().getWindow();
            stage.widthProperty().addListener(e -> {
                columnMax = getColumnMaxFromPageWidth(stage.getWidth());
                if (temp != columnMax) {
                    temp = columnMax;
                    initHallGrid();
                }
            });
        });
    }

    //Supporta fino ai 1080p
    private int getColumnMaxFromPageWidth(double width) {
        if(width<800) {
            return 2;
        } else if(width>=800 && width<=1360) {
            return 3;
        } else if(width>1360 && width<=1600) {
            return 4;
        } else if(width>1600) {
            return 5;
        } else {
            throw new ApplicationException("Impossibile settare numero colonne per width: " + width);
        }
    }

    /** Metodo chiamato in chiusura del progetto: permette di chiudere la sottofinestra della preview, se aperta */
    @Override
    public void closeAllSubWindows() {
        if(hallPreviewStage!=null) { hallPreviewStage.close(); }
    }
}
