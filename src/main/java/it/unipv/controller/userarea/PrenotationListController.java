package it.unipv.controller.userarea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unipv.controller.common.GUIUtils;
import it.unipv.model.Prenotation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller di resources/fxml/userarea/PrenotationList.fxml
 * Questa classe viene utilizzata per mostrare lo storico delle fatture per un relativo film
 */
public class PrenotationListController {

    private List<Prenotation> prenotations = new ArrayList<>();
    @FXML private ScrollPane prenotationPanel;
    @FXML private Label closeButton;
    private GridPane grigliaPrenotazioni = new GridPane();
    private static int rowCount = 0;
    private static int columnCount = 0;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param prenotations -> lista delle prenotazioni effettuate per quel determinato film
     */
    public void init(List<Prenotation> prenotations) {
        this.prenotations = prenotations;
        Collections.sort(prenotations);
        GUIUtils.setScaleTransitionOnControl(closeButton);
        createPrenotationListGrid();
    }

    //Metodo che crea la griglia delle prenotazioni
    private void createPrenotationListGrid() {
        grigliaPrenotazioni.getChildren().clear();

        for (Prenotation p : prenotations) {
            createGridCellFromPrenotation(p);
        }

        initRowAndColumnCount();
    }

    private void initRowAndColumnCount() {
        rowCount=0;
        columnCount=0;
    }

    //Metodo che crea la singola cella della griglia, mostra la prenotazione ed il tasto per il download della fattura relativa
    private void createGridCellFromPrenotation(Prenotation p) {
        Label dayLabel = new Label(p.getGiornoFilm());
        dayLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 20));
        dayLabel.setTextFill(Color.WHITE);

        grigliaPrenotazioni.setHgap(15);
        grigliaPrenotazioni.setVgap(15);

        Label invoceIcon = new Label();
        invoceIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        invoceIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/PDFIcon.png")));
        invoceIcon.setTooltip(new Tooltip("Scarica fattura del " + p.getGiornoFilm()));
        GUIUtils.setFadeInOutOnControl(invoceIcon);

        AnchorPane pane = new AnchorPane();
        if(columnCount==1) {
            columnCount=0;
            rowCount++;
        }
        grigliaPrenotazioni.add(pane, columnCount, rowCount);
        columnCount++;

        prenotationPanel.setContent(grigliaPrenotazioni);
        GridPane.setMargin(pane, new Insets(5,5,5,5));


        invoceIcon.setLayoutY(dayLabel.getLayoutY());
        invoceIcon.setLayoutX(dayLabel.getLayoutX()+140);
        invoceIcon.setOnMouseClicked(event -> GUIUtils.openPDFFileSaver(p));


        pane.getChildren().addAll(dayLabel, invoceIcon);
    }

    //Listener al tasto "Chiudi"
    @FXML
    private void doClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
    }
}
