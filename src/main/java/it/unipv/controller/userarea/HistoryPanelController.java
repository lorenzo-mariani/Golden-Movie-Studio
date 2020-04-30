package it.unipv.controller.userarea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unipv.controller.common.IUserReservedAreaTrigger;
import it.unipv.db.*;
import it.unipv.dao.MovieDao;
import it.unipv.dao.PrenotationDao;
import it.unipv.dao.MovieDaoImpl;
import it.unipv.dao.PrenotationDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.controller.common.ICloseablePane;
import it.unipv.model.Movie;
import it.unipv.model.User;
import it.unipv.model.Prenotation;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

/**
 * Controller di resources/fxml/userarea/HistoryPanel.fxml
 * Questa classe viene utilizzata per mostrare lo storico delle prenotazioni e le relative fatture
 */
public class HistoryPanelController implements ICloseablePane {

    private User loggedUser;
    private static int rowCount = 0;
    private static int columnCount = 0;
    private static int columnMax;
    private List<Movie> movies = new ArrayList<>();
    private List<Prenotation> prenotations = new ArrayList<>();
    private GridPane grigliaFilm = new GridPane();
    private Stage oldestPrenotationStage;
    private MovieDao movieDao;
    private IUserReservedAreaTrigger areaRiservataController;
    private PrenotationDao prenotationDao;
    @FXML private ScrollPane historyPanel;
    @FXML private TextField searchBarTextfield;
    @FXML private Label searchButton;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param areaRiservataController -> serve per segnalare all'area riservata le operazioni effettuate
     * @param loggedUser -> l'utente connesso al sistema
     * @param dbConnection -> la connessione al database utilizzata per istanziare MovieDaoImpl e PrenotationDaoImpl
     */
    public void init(IUserReservedAreaTrigger areaRiservataController, User loggedUser, DBConnection dbConnection) {
        this.movieDao = new MovieDaoImpl(dbConnection);
        this.prenotationDao = new PrenotationDaoImpl(dbConnection);
        this.loggedUser = loggedUser;
        this.areaRiservataController = areaRiservataController;
        GUIUtils.setScaleTransitionOnControl(searchButton);
        columnMax = getColumnMaxFromPageWidth(historyPanel.getScene().getWindow().getWidth());

        createUI();
        checkPageDimension();
    }

    private void createUI() {
        areaRiservataController.triggerStartStatusEvent("Carico film visti in precedenza...");
        Platform.runLater(() -> {
            initMovieAndPrenotationList();
            createMovieGrid();
        });
        areaRiservataController.triggerEndStatusEvent("Lista film visti da " + loggedUser.getNome() + " caricata con successo!");
    }

    private void initMovieAndPrenotationList() {
        List<Prenotation> x = prenotationDao.retrievePrenotationList();
        for(Prenotation p : x) {
            if(p.getNomeUtente().equalsIgnoreCase(loggedUser.getNome())) {
                prenotations.add(p);
            }
        }
        Collections.sort(prenotations);

        List<Movie> y = movieDao.retrieveCompleteMovieList(130,0,true,true);
        String temp = "";
        for(Movie m : y) {
            for(Prenotation p : prenotations) {
                if(m.getCodice().equalsIgnoreCase(p.getCodiceFilm()) && ApplicationUtils.checkIfDateIsInThePast(p.getGiornoFilm())) {
                    if(!m.getTitolo().equalsIgnoreCase(temp)) {
                        movies.add(m);
                        temp = m.getTitolo();
                    }
                }
            }
        }
    }

    //Metodo che crea la griglia dello storico
    private void createMovieGrid() {
        grigliaFilm.getChildren().clear();

        for (Movie movie : movies) {
            createViewFromMoviesList(movie);
        }

        initRowAndColumnCount();
    }

    //Metodo che crea la singola cella della griglia, che contiene la locandina, il nome del film e il tasto per aprire il pannello delle fatture
    private void createViewFromMoviesList(Movie movie) {
        Label nomeFilmLabel = new Label(StringUtils.abbreviate(movie.getTitolo(), 17));
        if(movie.getTitolo().length()>17) {
            nomeFilmLabel.setTooltip(new Tooltip(movie.getTitolo()));
        }
        nomeFilmLabel.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 20));
        nomeFilmLabel.setTextFill(Color.WHITE);

        grigliaFilm.setHgap(80);
        grigliaFilm.setVgap(80);

        ImageView posterPreview = new ImageView(movie.getLocandina());
        posterPreview.setFitWidth(130);

        Label oldestPrenotationIcon = new Label();
        oldestPrenotationIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        oldestPrenotationIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Schedule.png")));
        oldestPrenotationIcon.setTooltip(new Tooltip("Storico fatture"));
        GUIUtils.setFadeInOutOnControl(oldestPrenotationIcon);

        AnchorPane pane = new AnchorPane();
        if(columnCount==columnMax) {
            columnCount=0;
            rowCount++;
        }
        grigliaFilm.add(pane, columnCount, rowCount);
        columnCount++;

        historyPanel.setContent(grigliaFilm);
        GridPane.setMargin(pane, new Insets(15,0,5,15));

        posterPreview.setLayoutX(48);

        nomeFilmLabel.setLayoutY(posterPreview.getLayoutY()+215);

        oldestPrenotationIcon.setLayoutY(nomeFilmLabel.getLayoutY());
        oldestPrenotationIcon.setLayoutX(nomeFilmLabel.getLayoutX()+200);
        oldestPrenotationIcon.setOnMouseClicked(e -> openOldestPrenotationWindow(movie));

        pane.getChildren().addAll(posterPreview);
        pane.getChildren().addAll(nomeFilmLabel);
        pane.getChildren().addAll(oldestPrenotationIcon);
    }

    //Listener al tasto di apertura del pannello delle fatture
    private boolean isAlreadyOpened = false;
    private void openOldestPrenotationWindow(Movie movie) {
        if(!isAlreadyOpened) {
            List<Prenotation> toInject = new ArrayList<>();
            for(Prenotation p : prenotations) {
                if(p.getCodiceFilm().equalsIgnoreCase(movie.getCodice())) {
                    toInject.add(p);
                }
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userarea/PrenotationList.fxml"));
                Parent p = loader.load();
                PrenotationListController opc = loader.getController();
                opc.init(toInject);
                oldestPrenotationStage = new Stage();
                oldestPrenotationStage.setScene(new Scene(p));
                oldestPrenotationStage.setTitle("Storico prenotazioni " + movie.getTitolo());
                oldestPrenotationStage.setOnCloseRequest(event -> isAlreadyOpened = false);
                oldestPrenotationStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                oldestPrenotationStage.show();
                isAlreadyOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    //Listener al tasto di ricerca, ricrea la griglia a seconda di ciò che l'utente inserisce nella barra di ricerca.
    @FXML private void searchButtonListener() {
        String searchedString = searchBarTextfield.getText();
        if(searchedString!=null) {
            grigliaFilm.getChildren().clear();
            for(Movie m : movies) {
                if( m.getTitolo().trim().toLowerCase().contains(searchedString.toLowerCase())){
                    createViewFromMoviesList(m);
                }
            }
            initRowAndColumnCount();
        } else {
            refreshUI();
        }
    }

    private void initRowAndColumnCount() {
        rowCount=0;
        columnCount=0;
    }

    private void refreshUI() { createMovieGrid(); }

    private int getColumnMaxFromPageWidth(double width) {
        if(width<800) {
            return 2;
        } else if(width>800 && width<=1360) {
            return 3;
        } else if(width>1360 && width<=1600) {
            return 4;
        } else if(width>1600) {
            return 5;
        } else {
            return 6;
        }
    }

    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) historyPanel.getScene().getWindow();
            stage.widthProperty().addListener(e -> {
                columnMax = getColumnMaxFromPageWidth(stage.getWidth());
                if (temp != columnMax) {
                    temp = columnMax;
                    refreshUI();
                }
            });
        });
    }

    /**
     * Metodo chiamato alla chiusura del progetto o dell'area riservata:
     *     permette di chiudere l'eventuale sottofinestra delle fatture
     */
    @Override
    public void closeAllSubWindows() {
        if(oldestPrenotationStage!=null) {
            if(oldestPrenotationStage.isShowing()) {
                oldestPrenotationStage.close();
                isAlreadyOpened = false;
            }
        }
    }
}
