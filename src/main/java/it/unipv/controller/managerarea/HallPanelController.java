package it.unipv.controller.managerarea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import it.unipv.controller.common.IManagerAreaTrigger;
import it.unipv.dao.ScheduleDao;
import it.unipv.dao.ScheduleDaoImpl;
import it.unipv.db.DBConnection;
import it.unipv.dao.HallDao;
import it.unipv.dao.HallDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.controller.common.ICloseablePane;
import it.unipv.model.Schedule;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Controller di resources/fxml/managerarea/hallPanel.fxml
 * Questa classe viene utilizzata per:
 *    1) Aggiungere una nuova sala
 *    2) Modificare una sala già esistente
 *    3) Rinominare una sala già esistente
 *    4) Cancellare una sala già esistente
 */
public class HallPanelController implements ICloseablePane {

    @FXML private ScrollPane hallPanel;
    @FXML private Label nuovaSalaButton;
    private GridPane grigliaSale = new GridPane();
    private static int rowCount = 0;
    private static int columnCount = 0;
    private int columnMax;
    private List<String> hallNames = new ArrayList<>();
    private List<Image> previews = new ArrayList<>();
    private int hallNamesSize = 0;
    private IManagerAreaTrigger managerHomeController;
    private HallEditor hallEditor;
    private HallDao hallDao;
    private DBConnection dbConnection;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param managerHomeController -> serve per segnalare all'Area Manager le operazioni effettuate
     * @param dbConnection -> la connessione al database utilizzata per istanziare HallDaoImpl
     */
    public void init(IManagerAreaTrigger managerHomeController, DBConnection dbConnection) {
        this.managerHomeController = managerHomeController;
        this.dbConnection = dbConnection;
        hallDao = new HallDaoImpl(dbConnection);

        columnMax = getColumnMaxFromPageWidth(nuovaSalaButton.getScene().getWindow().getWidth());

        createUI();

        checkPageDimension();
    }

    private void createUI() {
        managerHomeController.triggerStartStatusEvent("Carico le informazioni sulle sale...");
        initHallNameList();
        initPreview();
        createHallGrid();
        managerHomeController.triggerEndStatusEvent("Informazioni sulle sale correttamente caricate!");
    }

    private void initHallNameList() {
        hallNames = hallDao.retrieveHallNames();
        Collections.sort(hallNames);
        hallNamesSize = hallNames.size();
    }

    private void initPreview() {
        previews.clear();
        for(int i = 0; i<hallNamesSize; i++) {
            previews.add(hallDao.retrieveHallPreviewAsImage(hallNames.get(i), 150, 0, true, true));
        }
    }

    //Metodo utilizzato per creare la griglia delle sale, a partire dai nomi e dalle previews delle stesse
    private void createHallGrid() {
        grigliaSale.getChildren().clear();

        for(int i = 0; i<hallNamesSize; i++) {
            createViewFromPreviews(hallNames.get(i), previews.get(i));
        }

        GUIUtils.setScaleTransitionOnControl(nuovaSalaButton);

        rowCount = 0;
        columnCount = 0;
    }

    //Metodo utilizzato per creare la singola cella della griglia; una cella contiene la preview, il nome sala, icona rinomina ed icona cancella.
    private void createViewFromPreviews(String hallName, Image preview) {
        Label nomeSalaLabel = new Label(hallName);
        nomeSalaLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15));
        hallNames.add(nomeSalaLabel.getText());
        nomeSalaLabel.setTextFill(Color.WHITE);

        grigliaSale.setHgap(80);
        grigliaSale.setVgap(80);

        ImageView snapHallView = new ImageView(preview);
        snapHallView.setFitWidth(150);

        Label deleteIcon = new Label();
        deleteIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Bin.png")));
        deleteIcon.setTooltip(new Tooltip("Elimina " + nomeSalaLabel.getText().trim()));
        GUIUtils.setFadeInOutOnControl(deleteIcon);

        Label renameIcon = new Label();
        renameIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        renameIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Edit.png")));
        renameIcon.setTooltip(new Tooltip("Rinomina " + nomeSalaLabel.getText().trim()));
        GUIUtils.setFadeInOutOnControl(renameIcon);

        AnchorPane pane = new AnchorPane();
        if (columnCount == columnMax) {
            columnCount = 0;
            rowCount++;
        }
        grigliaSale.add(pane, columnCount, rowCount);
        columnCount++;

        hallPanel.setContent(grigliaSale);
        GridPane.setMargin(pane, new Insets(15, 0, 0, 15));

        nomeSalaLabel.setLayoutY(snapHallView.getLayoutY() + 100);

        deleteIcon.setLayoutY(nomeSalaLabel.getLayoutY() - 2);
        deleteIcon.setLayoutX(nomeSalaLabel.getLayoutX() + 126);

        renameIcon.setLayoutY(nomeSalaLabel.getLayoutY() - 2);
        renameIcon.setLayoutX(nomeSalaLabel.getLayoutX() + 93);

        pane.getChildren().addAll(snapHallView, nomeSalaLabel, deleteIcon, renameIcon);

        snapHallView.setOnMouseClicked(event -> {
            hallEditor = new HallEditor(nomeSalaLabel.getText(), this, true, dbConnection);
            hallEditor.setAlwaysOnTop(true);
        });

        GUIUtils.setScaleTransitionOnControl(snapHallView);
        renameIcon.setOnMouseClicked(event -> renameHall(nomeSalaLabel.getText(), nomeSalaLabel, renameIcon, deleteIcon));
        deleteIcon.setOnMouseClicked(event -> removeHall(nomeSalaLabel.getText()));
    }

    //Listener al tasto di rimozione sala
    private void removeHall(String hallName) {
        if (!checkIfIsOccupiedFromProgrammations(hallName)) {
            Optional<ButtonType> option =
                    GUIUtils.showConfirmationAlert("Attenzione"
                                                  , "Richiesta conferma:"
                                                  , "Vuoi davvero eliminare la piantina " + hallName + "?");
            if (option.orElse(null) == ButtonType.YES) {
                doRemove(hallName);
            }
        } else {
            GUIUtils.showAlert( Alert.AlertType.ERROR
                              , "Errore"
                              , "Sala occupata: "
                              , "Impossibile cancellare la sala, è attualmente occupata da delle programmazioni.\nUna volta terminate, sarà possibile cancellarla.");
        }

    }

    //Metodo che si occupa della vera rimozione e dell'aggiornamento delle liste
    private void doRemove(String hallName) {
        managerHomeController.triggerStartStatusEvent("Rimuovo " + hallName + "...");
        hallDao.removeHallAndPreview(hallName);
        initHallNameList();
        initPreview();
        managerHomeController.triggerToHomeNewHallEvent();
        refreshUIandHallList();
        managerHomeController.triggerEndStatusEvent("Piantina " + hallName + " correttamente eliminata!");
    }

    //Listener al tasto di rinominazione sala
    private void renameHall(String hallName, Label labelToModify, Label renameIcon, Label deleteIcon) {
        if(!checkIfIsOccupiedFromProgrammations(hallName)) {
            String newHallName = GUIUtils.showInputAlert("Rinomina Sala", "Rinomina " + hallName, "Inserisci il nuovo nome della sala").orElse(null);
            if(newHallName!=null) {
                if(!newHallName.trim().equalsIgnoreCase("")) {
                    if(checkIfItIsFree(newHallName)) {
                            doRename(hallName, labelToModify, renameIcon, deleteIcon, newHallName);
                    } else {
                        GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Esiste già una sala con questo nome!");
                    }
                } else {
                    GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi compilare il campo!");
                }
            }
        } else {
            GUIUtils.showAlert( Alert.AlertType.ERROR
                    , "Errore"
                    , "Sala occupata: "
                    , "Impossibile rinominare la sala, è attualmente occupata da delle programmazioni.\nUna volta terminate, sarà possibile rinominarla.");
        }

    }

    //Metodo che si occupa di rinominare realmente la sala e di segnalare il cambiamento
    private void doRename(String hallName, Label labelToModify, Label renameIcon, Label deleteIcon, String newHallName) {
        managerHomeController.triggerStartStatusEvent("Rinomino " + hallName + " in " + newHallName + "...");
        labelToModify.setText(newHallName);
        renameIcon.setTooltip(new Tooltip("Rinomina " + newHallName));
        deleteIcon.setTooltip(new Tooltip("Elimina " + newHallName));

        hallDao.renameHallAndPreview(hallName, newHallName);
        initHallNameList();
        initPreview();

        managerHomeController.triggerToHomeNewHallEvent();
        GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Informazione", "Operazione riuscita: ", "Sala rinominata con successo!");
        managerHomeController.triggerEndStatusEvent(hallName + " correttamente rinominata!");
    }

    private boolean checkIfItIsFree(String name) {
        boolean status = true;
        for(String s : hallNames) {
            if(name.trim().equalsIgnoreCase(s)) {
                status = false;
                break;
            }
        }
        return status;
    }

    private boolean checkIfIsOccupiedFromProgrammations(String hallName) {
        ScheduleDao scheduleDao = new ScheduleDaoImpl(dbConnection);
        List<Schedule> schedules = scheduleDao.retrieveMovieSchedules();
        for(Schedule s : schedules) {
            if(s.getHallName().equals(hallName)) {
                if(!ApplicationUtils.checkIfDateIsInThePast(s.getDate())) {
                    return true;
                }
            }
        }
        return false;
    }

    //Listener al tasto "Nuova Sala"
    @FXML private void newHallListener() {
        String nomeSala = GUIUtils.showInputAlert("Nuova Sala", "Stai creando una nuova sala:", "Inserisci il nome della sala").orElse(null);
        if(nomeSala!=null) {
            if(nomeSala.equalsIgnoreCase("") || nomeSala.trim().length()==0) {
                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi inserire un nome!");
            } else if(!nomeSala.equalsIgnoreCase("")) {
                if(checkIfItIsFree(nomeSala)) {
                    Optional<ButtonType> option =
                            GUIUtils.showConfirmationAlert( "Attenzione"
                                                          , "Richiesta conferma:"
                                                          , "Vuoi creare una griglia preimpostata?");
                    if(option.orElse(null)==ButtonType.NO) {
                        hallEditor = new HallEditor(nomeSala, this, false, dbConnection);
                        hallEditor.setAlwaysOnTop(true);
                    } else {
                        Optional<Pair<String, String>> dialogMenu = configureRowAndColumnDialogRequest();
                        dialogMenu.ifPresent(rowsAndcolumns -> {
                            int rows =  Integer.parseInt(rowsAndcolumns.getKey());
                            int columns = Integer.parseInt(rowsAndcolumns.getValue());

                            if(rows<27) {
                                hallEditor = new HallEditor(nomeSala, this, rows, columns, dbConnection);
                                hallEditor.setAlwaysOnTop(true);
                            } else {
                                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Numero massimo di righe 26!");
                            }
                        });
                    }
                } else {
                    GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Esiste già una sala con questo nome!");
                }
            }
        }
    }

    //Metodo utilizzato per creare un Alert contente due textfield per l'inserimento del numero di righe e colonne per la griglia iniziale di una nuova sala
    private Optional<Pair<String, String>> configureRowAndColumnDialogRequest() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Nuova sala");
        dialog.setHeaderText("Inserisci numero di righe e colonne");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Stage s = (Stage) dialog.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField rows = new TextField();
        makeTextFieldFillableByOnlyDigits(rows);
        rows.setPromptText("0");
        TextField columns = new PasswordField();
        makeTextFieldFillableByOnlyDigits(columns);
        columns.setPromptText("0");

        grid.add(new Label("Righe:"), 0, 0);
        grid.add(rows, 1, 0);
        grid.add(new Label("Colonne:"), 0, 1);
        grid.add(columns, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(rows::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(rows.getText(), columns.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    //Le textfield dell'alert devono contenere solo numeri
    private void makeTextFieldFillableByOnlyDigits(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Segnala all'area manager che si è verificato un cambiamento alla lista delle sale
     */
    void triggerModificationToHallList() {
        refreshUIandHallList();
        managerHomeController.triggerToHomeNewHallEvent();
    }

    /**
     * Segnala alla status bar dell'area manager che si è verificato l'inizio di un nuovo evento
     * @param text -> descrizione dell'evento
     */
    void triggerStartEventToManagerHome(String text) {
        managerHomeController.triggerStartStatusEvent(text);
    }

    /**
     * Segnala alla status bar dell'area manager che si è verificata la fine di un evento
     * @param text -> descrizione dell'evento
     */
    void triggerEndEventToManagerHome(String text) {
        managerHomeController.triggerStartStatusEvent(text);
    }

    private void refreshUIandHallList() { createUI(); }

    private void refreshUI() {
        createHallGrid();
    }

    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) nuovaSalaButton.getScene().getWindow();
            stage.widthProperty().addListener(e -> {
                columnMax = getColumnMaxFromPageWidth(stage.getWidth());
                if (temp != columnMax) {
                    temp = columnMax;
                    refreshUI();
                }
            });
        });
    }

    private int getColumnMaxFromPageWidth(double width) {
        if(width<800) {
            return 2;
        } else if(width>=800 && width<=1200) {
            return 3;
        } else if(width>1200 && width<=1500) {
            return 4;
        } else if(width>1500 && width<=1700) {
            return 5;
        } else if(width>1700){
            return 6;
        } else {
            throw new ApplicationException("Impossibile settare numero colonne per width: " + width);
        }
    }

    /**
     * Metodo chiamato alla chiusura del progetto o dell'area manager:
     *     permette di chiudere l'eventuale sottofinestra del tool di modifica/creazione sale
     */
    @Override
    public void closeAllSubWindows() {
        if(hallEditor!=null) {
            hallEditor.dispose();
        }
    }
}
