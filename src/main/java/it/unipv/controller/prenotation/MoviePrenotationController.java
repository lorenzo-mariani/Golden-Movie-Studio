package it.unipv.controller.prenotation;

import java.io.IOException;
import java.util.*;

import it.unipv.controller.common.IHomeTrigger;
import it.unipv.db.*;
import it.unipv.dao.HallDao;
import it.unipv.dao.PrenotationDao;
import it.unipv.dao.PricesDao;
import it.unipv.dao.ScheduleDao;
import it.unipv.dao.HallDaoImpl;
import it.unipv.dao.PrenotationDaoImpl;
import it.unipv.dao.PricesDaoImpl;
import it.unipv.dao.ScheduleDaoImpl;
import it.unipv.controller.common.*;
import it.unipv.model.*;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * Controller di resources/fxml/prenotation/MoviePrenotation.fxml
 * Questa classe viene utilizzata per dare la possibilità all'utente di prenotare la visione di un film.
 * Passaggi per la prenotazione:
 *     1) L'utente sceglie l'ora;
 *     2) L'utente sceglie la sala;
 *     3) L'utente sceglie i posti nella sala;
 *     4) L'utente può vedere un piccolo sommario prima della conferma;
 *     5) L'utente conferma.
 */
public class MoviePrenotationController implements ICloseablePane {

    private List<Schedule> schedules = new ArrayList<>();
    private List<Prenotation> prenotations;
    private List<Seat> selectedMDS = new ArrayList<>();
    private List<String> completeHallNameList = new ArrayList<>();
    private GridPane grigliaSale = new GridPane();
    private static int rowCount = 0, columnCount = 0;
    private Movie movie;
    private Prices prices;
    private String scheduleDate, clickedHour, clickedHall;
    private Prenotation finalPrenotation;
    private User user;
    private boolean opened = false;
    private IHomeTrigger homeController;
    private HallViewer hallViewer;
    private PricesDao pricesDao;
    private HallDao hallDao;
    private ScheduleDao scheduleDao;
    private PrenotationDao prenotationDao;
    private DBConnection dbConnection;
    @FXML private Label closeButton, confirmButton;
    @FXML private AnchorPane orariPanel, salaHeader, summaryPanel;
    @FXML private ScrollPane salaPanel;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe;
     * @param homeController -> è il controller della Home del sistema, utilizzato per inizializzare AvvisoPrenotazioneController
     * @param date -> la data che l'utente ha scelto dal pannello "SingleMoviePanel";
     * @param movie -> il film che l'utente ha scelto di prenotare;
     * @param user -> l'utente che vuole effettuare la prenotazione;
     * @param dbConnection -> la connessione al database utilizzata per istanziare PricesDaoImpl, HallDaoImpl, ScheduleDaoImpl e PrenotationDaoImpl.
     */
    public void init(IHomeTrigger homeController, String date, Movie movie, User user, DBConnection dbConnection) {
        this.homeController = homeController;
        this.movie = movie;
        this.scheduleDate = date;
        this.user = user;
        this.dbConnection = dbConnection;
        this.pricesDao = new PricesDaoImpl(dbConnection);
        this.hallDao = new HallDaoImpl(dbConnection);
        this.scheduleDao = new ScheduleDaoImpl(dbConnection);
        this.prenotationDao = new PrenotationDaoImpl(dbConnection);
        initSchedules(date, movie);
        initListOfHallNames();
        initPrices();
        initPrenotationList();
        createUI();
    }

    private void initSchedules(String date, Movie movie) {
        List<Schedule> allSchedules = scheduleDao.retrieveMovieSchedules();
        Collections.sort(allSchedules);
        for (Schedule ms : allSchedules) {
            if (ms.getDate().equals(date) && ms.getMovieCode().equals(movie.getCodice())) {
                schedules.add(ms);
            }
        }
    }

    private void initListOfHallNames() {
        completeHallNameList = hallDao.retrieveHallNames();
        Collections.sort(completeHallNameList);
    }
    private void initPrices() { prices = pricesDao.retrievePrices(); }

    private void initPrenotationList() { prenotations = prenotationDao.retrievePrenotationList(); }

    private void createUI() {
        Font infoFont = new Font("Bebas Neue", 24);

        Label disponibleHoursLabel = new Label("ORARI DISPONIBILI: ");
        disponibleHoursLabel.setFont(infoFont);
        disponibleHoursLabel.setTextFill(Color.valueOf("db8f00"));
        disponibleHoursLabel.setLayoutY(50);
        disponibleHoursLabel.setLayoutX(50);

        createHourLabels(infoFont, disponibleHoursLabel.getLayoutX());

        orariPanel.getChildren().add(disponibleHoursLabel);

        GUIUtils.setScaleTransitionOnControl(closeButton);
        closeButton.setOnMouseClicked(event -> doClose());

        GUIUtils.setScaleTransitionOnControl(confirmButton);
        confirmButton.setOnMouseClicked(event -> confirmButtonListener());
    }

    private List<Label> listOfHourLabels = new ArrayList<>();

    //Metodo che crea le label degli orari disponibili per la prenotazione, basandosi sulla lista delle prenotazioni
    private void createHourLabels(Font font, double initalX) {
        double x = initalX + 230;
        double y = 50;
        int count = 0;
        String ora = "";
        for (Schedule ms : schedules) {
            if(!ms.getTime().equals(ora)) {
                Label hourLabel = new Label("  " + ms.getTime() + "  ");
                hourLabel.setTextFill(Color.WHITE);
                hourLabel.setFont(font);
                hourLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));

                setHourLabelListener(font, hourLabel);

                if (count >= 5) {
                    y += 50;
                    x = initalX + 230;
                    count = 0;
                }

                hourLabel.setLayoutX(x);
                hourLabel.setLayoutY(y);
                x += 100;
                count++;
                listOfHourLabels.add(hourLabel);
                orariPanel.getChildren().add(hourLabel);
            }
            ora = ms.getTime();
        }
    }

    /* Setto i listener delle label degli orari:
     *    quando il mouse entra nella label, il bordo si colora di giallo;
     *    quando il mouse esce dalla label, il bordo si colora di bianco;
     *    quando si clicca sulla label, il bordo si colora di giallo e quello di tutte le altre rimane bianco
     *        inoltre, al click, viene popolata la griglia delle sale disponibili
     */
    private void setHourLabelListener(Font font, Label hourLabel) {
        hourLabel.setOnMouseEntered(event -> {
            hourLabel.setBorder(new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
            hourLabel.setCursor(Cursor.HAND);
        });

        hourLabel.setOnMouseExited(event -> {
            hourLabel.setCursor(Cursor.DEFAULT);
            if (!hourLabel.getText().trim().equalsIgnoreCase(clickedHour)) {
                hourLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
            }

        });

        hourLabel.setOnMouseClicked(event -> {
            summaryPanel.getChildren().clear();
            selectedMDS.clear();
            finalPrenotation = null;

            hourLabel.setBorder(new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
            setToWhiteBorderOtherLabels(hourLabel.getText().trim());

            createHallGrid(font, hourLabel);
            clickedHour = hourLabel.getText().trim();
        });
    }

    /* Se clicco su una label, le altre devono necessariamente essere settate a bianco; se non ci fosse il metodo,
     *     se io cliccassi su una label e poi su un'altra ce ne sarebbero due colorate e non avrebbe senso.
     */

    private void setToWhiteBorderOtherLabels(String nameToExclude) {
        for (Label l : listOfHourLabels) {
            if (!l.getText().trim().toLowerCase().equalsIgnoreCase(nameToExclude.trim().toLowerCase())) {
                l.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
            }
        }
    }

    //Al click sull'orario, viene mostrato all'utente la griglia delle sale disponibili
    private void createHallGrid(Font font, Label hourLabel) {
        salaHeader.getChildren().clear();
        grigliaSale.getChildren().clear();
        Label hallListLabel = new Label("LISTA SALE DISPONIBILI: ");
        hallListLabel.setFont(font);
        hallListLabel.setTextFill(Color.valueOf("db8f00"));
        hallListLabel.setLayoutY(50);
        hallListLabel.setLayoutX(50);
        salaHeader.getChildren().add(hallListLabel);

        List<String> hallNames = getHallsInvolvedInThatHour(hourLabel.getText().trim());
        for (String s : completeHallNameList) {
            if (hallNames.contains(s)) {
                createHallGridCell(s, hallDao.retrieveHallPreviewAsImage(s, 150, 0, true, true));
            }
        }
        rowCount = 0;
        columnCount = 0;
    }

    private List<String> getHallsInvolvedInThatHour(String orario) {
        List<String> res = new ArrayList<>();
        for (Schedule ms : schedules) {
            if (ms.getTime().equals(orario)) {
                res.add(ms.getHallName());
            }
        }
        Collections.sort(res);
        return res;
    }

    //La singola cella della griglia delle sale, contiene la preview ed il nome della sala
    private void createHallGridCell(String nomeSala, Image image) {
        Label nomeSalaLabel = new Label(nomeSala);
        nomeSalaLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15));
        nomeSalaLabel.setTextFill(Color.WHITE);

        grigliaSale.setHgap(5);
        grigliaSale.setVgap(50);

        ImageView snapHallView = new ImageView(image);
        snapHallView.setFitWidth(150);

        AnchorPane pane = new AnchorPane();
        if (columnCount == 3) {
            columnCount = 0;
            rowCount++;
        }
        grigliaSale.add(pane, columnCount, rowCount);
        columnCount++;

        salaPanel.setContent(grigliaSale);
        GridPane.setMargin(pane, new Insets(15, 0, 0, 15));

        snapHallView.setLayoutX(80);
        nomeSalaLabel.setLayoutY(snapHallView.getLayoutY() + 100);
        nomeSalaLabel.setLayoutX(snapHallView.getLayoutX() + 50);

        snapHallView.setOnMouseClicked(event -> {
            if (!opened) {
                clickedHall = nomeSalaLabel.getText().trim();
                if (selectedMDS.size() > 0) {
                    hallViewer = new HallViewer(this, nomeSalaLabel.getText().trim(), selectedMDS, getOccupiedSeatNames(), dbConnection);
                    hallViewer.setAlwaysOnTop(true);
                } else {
                    hallViewer = new HallViewer(this, nomeSalaLabel.getText().trim(), getOccupiedSeatNames(), dbConnection);
                    hallViewer.setAlwaysOnTop(true);
                }
                opened = true;
            }
        });

        pane.getChildren().addAll(snapHallView);
        pane.getChildren().addAll(nomeSalaLabel);

        GUIUtils.setScaleTransitionOnControl(snapHallView);
    }

    //Metodo che ricava, se presenti, i posti che sono già stati occupati per quella determinata programmazione
    private List<String> getOccupiedSeatNames() {
        List<String> occupiedSeat = new ArrayList<>();
        for (Prenotation p : prenotations) {
            if ( p.getGiornoFilm().equalsIgnoreCase(scheduleDate.trim())
              && p.getOraFilm().equalsIgnoreCase(clickedHour.trim())
              && p.getNomeFilm().equalsIgnoreCase(movie.getTitolo())
              && p.getCodiceFilm().equalsIgnoreCase(movie.getCodice())
              && p.getSalaFilm().equalsIgnoreCase(clickedHall.trim())) {
                occupiedSeat.add(p.getPostiSelezionati());
            }
        }
        return ApplicationUtils.splitter(occupiedSeat, "-");
    }

    //Creo il pannello del riepilogo finale (dopo che l'utente ha scelto i posti)
    private void createSummaryPanel() { Platform.runLater(this::doCreateSummaryPanel); }

    private void doCreateSummaryPanel() {
        summaryPanel.getChildren().clear();
        if (selectedMDS.size() > 0) {
            Label summaryLabel = new Label("RIEPILOGO: ");
            summaryLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15));
            summaryLabel.setTextFill(Color.valueOf("db8f00"));
            summaryLabel.setLayoutX(50);

            Label actualSummaryLabel = new Label();
            actualSummaryLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15));
            actualSummaryLabel.setTextFill(Color.valueOf("20b510"));
            actualSummaryLabel.setLayoutX(summaryLabel.getLayoutX() + 100);
            String text = movie.getTitolo() + "    "
                        + scheduleDate + "    "
                        + clickedHour + "    "
                        + clickedHall + ": ";

            StringBuilder selectedMDSName = new StringBuilder(selectedMDS.get(0).getText());
            for (int i = 1; i < selectedMDS.size(); i++) {
                selectedMDSName.append("-").append(selectedMDS.get(i).getText());
            }
            text = text + selectedMDSName + "    Prezzo totale: " + calculateTotalPrices() + "€";
            actualSummaryLabel.setText(StringUtils.abbreviate(text, 94));
            if (text.length() > 94) {
                actualSummaryLabel.setTooltip(new Tooltip(text));
            }
            summaryPanel.getChildren().addAll(summaryLabel, actualSummaryLabel);
            finalPrenotation = new Prenotation( user.getNome()
                                              , movie.getTitolo()
                                              , movie.getCodice()
                                              , scheduleDate
                                              , clickedHour
                                              , clickedHall
                                              , selectedMDSName.toString()
                                              , calculateTotalPrices() + "€");
        }
    }

    private String calculateTotalPrices() {
        double res = 0;
        for (Seat mds : selectedMDS) {
            switch (mds.getType()) {
                case NORMALE:
                    res += prices.getBase();
                    break;

                case DISABILE:
                    res += prices.getReduced();
                    break;

                case VIP:
                    res += prices.getBase() + prices.getVip();
                    break;
            }

            if (movie.getTipo().equals(MovieTYPE.THREED)) {
                res += prices.getThreed();
            }
        }

        return res % 1 == 0 ? String.valueOf((int) res) : res + "0";
    }

    /**
     * Segnala al form di prenotazione che sono stati selezionati e confermati dei posti dall'utente.
     * @param selectedMDS -> la lista dei posti selezionati e confermati dall'utente
     */
    void triggerSelectedSeats(List<Seat> selectedMDS) {
        this.selectedMDS = selectedMDS;
        createSummaryPanel();
    }

    /** Segnala al form di prenotazione che è stata chiuso il selettore dei posti. */
    void triggerClosingHallViewer() {
        opened = false;
        hallViewer.dispose();
    }

    private void confirmButtonListener() {
        if (finalPrenotation != null) {
            prenotationDao.insertNewPrenotation(finalPrenotation);
            openAvvisoPrenotazioneController();
            doClose();
        } else {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore: ", "Non è ancora stata creata una prenotazione!");
        }
    }

    private void doClose() {
        Stage stage = (Stage) orariPanel.getScene().getWindow();
        stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        stage.close();
    }

    //Se l'utente conferma i posti selezionati, allora si deve aprire la schermata di conferma, che rimanda all'area privata
    private void openAvvisoPrenotazioneController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prenotation/AvvisoPrenotazione.fxml"));
            Parent p = loader.load();
            AvvisoPrenotazioneController apc = loader.getController();
            apc.init(homeController);
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setResizable(false);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
            stage.setTitle("Grazie per la prenotazione!");
            stage.show();
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    /**
     * Metodo chiamato alla chiusura del progetto:
     *     permette di chiudere l'eventuale sottofinestra del selettore dei posti
     */
    @Override
    public void closeAllSubWindows() {
        if (hallViewer != null) {
            hallViewer.dispose();
        }
    }
}
