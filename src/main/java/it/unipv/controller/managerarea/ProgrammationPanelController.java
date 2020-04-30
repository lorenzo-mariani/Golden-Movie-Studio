package it.unipv.controller.managerarea;

import java.io.*;
import java.util.*;

import it.unipv.db.*;
import it.unipv.dao.MovieDao;
import it.unipv.dao.ScheduleDao;
import it.unipv.dao.MovieDaoImpl;
import it.unipv.dao.ScheduleDaoImpl;
import it.unipv.controller.common.*;
import it.unipv.model.Movie;
import it.unipv.model.Schedule;
import it.unipv.model.MovieStatusTYPE;
import it.unipv.utils.ApplicationException;
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
 * Controller di resources/fxml/managerarea/ProgrammationPanel.fxml
 * Questa classe viene utilizzata per:
 *    1) Mostrare la lista dei film attualmente programmati
 *    2) Modificare un film programmato già esistente
 *    3) Creare un nuovo film (alla creazione, è settato automaticamente programmabile)
 *    4) Cancellare un film già esistente
 *    5) Aprire il form di programmazione del film
 */
public class ProgrammationPanelController implements ICloseablePane {

    @FXML private Label nuovoFilmButton;
    @FXML private ScrollPane moviePanel;
    private GridPane grigliaFilm = new GridPane();
    private static int rowCount = 0;
    private static int columnCount = 0;
    private int columnMax;
    private List<Movie> movies = new ArrayList<>();
    private IManagerAreaTrigger managerHomeController;
    private Stage movieEditorStage, movieSchedulerStage;
    private MovieSchedulerController msc;
    private MovieDao movieDao;
    private ScheduleDao scheduleDao;
    private DBConnection dbConnection;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param managerHomeController -> controller della home del manager, al quale vengono segnalati cambiamenti nella lista film
     * @param dbConnection -> la connessione al database utilizzata per istanziare MovieDaoIMpl e ScheduleDaoImpl
     */
    public void init(IManagerAreaTrigger managerHomeController, DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.movieDao = new MovieDaoImpl(dbConnection);
        this.scheduleDao = new ScheduleDaoImpl(dbConnection);
        this.managerHomeController = managerHomeController;
        columnMax = getColumnMaxFromPageWidth(moviePanel.getScene().getWindow().getWidth());
        createUI();
        checkPageDimension();

    }

    private void createUI() {
        managerHomeController.triggerStartStatusEvent("Carico film attualmente programmati...");
        initMoviesList();
        createMovieGrid();
        managerHomeController.triggerEndStatusEvent("Film programmati correttamente caricati!");
    }

    private void initMoviesList() {
        movies = movieDao.retrieveCompleteMovieList(130, 0, true, true);
        Collections.sort(movies);
    }

    //Metodo che si occupa di creare la griglia dei film programmabili
    private void createMovieGrid() {
        grigliaFilm.getChildren().clear();

        for (Movie movie : movies) {
            if(movie.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                createViewFromMoviesList(movie);
            }
        }

        GUIUtils.setScaleTransitionOnControl(nuovoFilmButton);
        initRowAndColumnCount();
    }

    private void initRowAndColumnCount() {
        rowCount = 0;
        columnCount = 0;
    }

    /* La creazione della singola cella della griglia, contenente:
     *     1) Locandina cliccabile che porta alla modifica del film;
     *     2) Informazioni principali sul film;
     *     3) Pulsante che apre il pannello delle programmazioni;
     *     4) Pulsante che imposta non programmabile il film;
     *     5) Pulsante che avvia l'eliminazione del film e delle sue programmazioni,
    */
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

        posterPreview.setOnMouseClicked(e -> openMovieEditor(movie, false));

        Font infoFont = Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15);

        Label genereFilmLabel = new Label(StringUtils.abbreviate("Genere: " + movie.getGenere(), 28));
        genereFilmLabel.setFont(infoFont);
        genereFilmLabel.setTextFill(Color.WHITE);

        Label regiaFilmLabel = new Label(StringUtils.abbreviate("Regia: " + movie.getRegia(),28));
        regiaFilmLabel.setFont(infoFont);
        regiaFilmLabel.setTextFill(Color.WHITE);

        Label castFilmLabel = new Label(StringUtils.abbreviate("Cast: " + movie.getCast(), 28));
        castFilmLabel.setFont(infoFont);
        castFilmLabel.setTextFill(Color.WHITE);

        Label annoFilmLabel = new Label(StringUtils.abbreviate("Anno: " + movie.getAnno(),28));
        annoFilmLabel.setFont(infoFont);
        annoFilmLabel.setTextFill(Color.WHITE);

        Label deleteIcon = new Label();
        deleteIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Bin.png")));
        deleteIcon.setTooltip(new Tooltip("Elimina " + movie.getTitolo()));
        GUIUtils.setFadeInOutOnControl(deleteIcon);

        Label showSchedulesIcon = new Label();
        showSchedulesIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        showSchedulesIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Schedule.png")));
        showSchedulesIcon.setTooltip(new Tooltip("Programma " + movie.getTitolo()));
        GUIUtils.setFadeInOutOnControl(showSchedulesIcon);

        Label hideMovieIcon = new Label();
        hideMovieIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        hideMovieIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Hide.png")));
        hideMovieIcon.setTooltip(new Tooltip("Togli dalle programmazioni " + movie.getTitolo()));
        GUIUtils.setFadeInOutOnControl(hideMovieIcon);

        AnchorPane pane = new AnchorPane();
        if(columnCount==columnMax) {
            columnCount=0;
            rowCount++;
        }
        grigliaFilm.add(pane, columnCount, rowCount);
        columnCount++;

        moviePanel.setContent(grigliaFilm);
        GridPane.setMargin(pane, new Insets(15,0,5,15));

        nomeFilmLabel.setLayoutX(posterPreview.getLayoutX() + 150);
        nomeFilmLabel.setLayoutY(posterPreview.getLayoutY()-5);

        genereFilmLabel.setLayoutY(nomeFilmLabel.getLayoutY()+40);
        genereFilmLabel.setLayoutX(nomeFilmLabel.getLayoutX());

        regiaFilmLabel.setLayoutY(nomeFilmLabel.getLayoutY()+70);
        regiaFilmLabel.setLayoutX(nomeFilmLabel.getLayoutX());

        castFilmLabel.setLayoutY(nomeFilmLabel.getLayoutY()+100);
        castFilmLabel.setLayoutX(nomeFilmLabel.getLayoutX());

        annoFilmLabel.setLayoutY(nomeFilmLabel.getLayoutY()+130);
        annoFilmLabel.setLayoutX(nomeFilmLabel.getLayoutX());

        hideMovieIcon.setLayoutY(nomeFilmLabel.getLayoutY()+167);
        hideMovieIcon.setLayoutX(nomeFilmLabel.getLayoutX());
        hideMovieIcon.setOnMouseClicked(event -> doHideMovie(movie));

        showSchedulesIcon.setLayoutY(nomeFilmLabel.getLayoutY()+167);
        showSchedulesIcon.setLayoutX(nomeFilmLabel.getLayoutX()+40);
        showSchedulesIcon.setOnMouseClicked(e -> openMovieScheduler(movie));

        deleteIcon.setLayoutY(nomeFilmLabel.getLayoutY()+167);
        deleteIcon.setLayoutX(nomeFilmLabel.getLayoutX()+80);
        deleteIcon.setOnMouseClicked(e -> doDeleteMovie(movie));

        pane.getChildren().addAll( posterPreview
                                 , nomeFilmLabel
                                 , genereFilmLabel
                                 , regiaFilmLabel
                                 , castFilmLabel
                                 , annoFilmLabel
                                 , hideMovieIcon
                                 , deleteIcon
                                 , showSchedulesIcon);

        GUIUtils.setScaleTransitionOnControl(posterPreview);
    }

    //Listener al pulsante di eliminazione film
    private void doDeleteMovie(Movie movie) {
        Optional<ButtonType> option =
                GUIUtils.showConfirmationAlert( "Attenzione"
                                              , "Richiesta conferma:"
                                              , "Sei sicuro di voler eliminare il film " + movie.getTitolo() +"?");
        if(option.orElse(null)==ButtonType.YES) {
            managerHomeController.triggerStartStatusEvent("Elimino il film " + movie.getTitolo() + "...");
            removeAssociatedSchedules(movie);
            movieDao.deleteMovie(movie);
            managerHomeController.triggerToHomeNewMovieEvent();
            refreshUIandMovieList();
            managerHomeController.triggerEndStatusEvent(movie.getTitolo() + " correttamente eliminato!");
        }
    }

    //Rimuovo le programmazioni associate al film in questione se si cancella il film
    private void removeAssociatedSchedules(Movie movie) {
        List<Schedule> schedules = scheduleDao.retrieveMovieSchedules();
        for(Schedule ms : schedules) {
            if(movie.getCodice().equalsIgnoreCase(ms.getMovieCode())) {
                scheduleDao.deleteMovieSchedule(ms);
            }
        }
    }

    //Listener al pulsante che imposta non programmabile il film
    private void doHideMovie(Movie movie) {
        Optional<ButtonType> option =
                GUIUtils.showConfirmationAlert( "Attenzione"
                                              , "Richiesta conferma:"
                                              , "Sei sicuro di voler nascondere " + movie.getTitolo() + " dai film programmabili?");
        if(option.orElse(null)==ButtonType.YES) {
            managerHomeController.triggerStartStatusEvent("Nascondo " + movie.getTitolo() + " dai film programmabili...");
            movie.setStatus(MovieStatusTYPE.NOT_AVAILABLE);
            movieDao.updateMovieButNotPoster(movie);
            managerHomeController.triggerToHomeNewMovieEvent();
            refreshUIandMovieList();
            managerHomeController.triggerEndStatusEvent(movie.getTitolo() + " correttamente nascosto dai film programmabili!");
        }
    }

    //Listener al tasto di apertura del pannello delle programmazioni
    private boolean isMovieSchedulerAlreadyOpened = false;
    private void openMovieScheduler(Movie movie) {
        if(!isMovieSchedulerAlreadyOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/managerarea/MovieScheduler.fxml"));
                Parent p = loader.load();
                msc = loader.getController();
                msc.init(movie, dbConnection);
                movieSchedulerStage = new Stage();
                movieSchedulerStage.setScene(new Scene(p));
                movieSchedulerStage.setTitle("Programmazione " + movie.getTitolo());
                movieSchedulerStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                movieSchedulerStage.setOnCloseRequest(event -> {
                    isMovieSchedulerAlreadyOpened = false;
                    msc.closeAllSubWindows();
                });
                movieSchedulerStage.show();
                isMovieSchedulerAlreadyOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    //Metodo che apre l'editor film in modalità creazione se isANewFilm = true, se no lo apre in modalità modifica
    private boolean isMovieEditorAlreadyOpened = false;
    private void openMovieEditor(Movie movie, boolean isANewFilm) {
        if(!isMovieEditorAlreadyOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/managerarea/MovieEditor.fxml"));
                Parent p = loader.load();
                MovieEditorController mec = loader.getController();
                movieEditorStage = new Stage();
                movieEditorStage.setScene(new Scene(p));
                if(isANewFilm) {
                    mec.init(this);
                    movieEditorStage.setTitle("Editor Film");
                } else {
                    mec.init(movie, this);
                    movieEditorStage.setTitle("Modifica: " + movie.getTitolo());
                }
                movieEditorStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                movieEditorStage.setOnCloseRequest(event -> isMovieEditorAlreadyOpened=false);
                movieEditorStage.show();
                isMovieEditorAlreadyOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    //Listener del tasto "Nuovo Film"
    @FXML private void nuovoFilmButtonListener() {
       openMovieEditor(null, true);
    }

    /**
     * Segnala alla Home la creazione di un nuovo film
     * @param movie -> il film appena creato
     * @param posterStream -> la locandina del film appena creato
     */
    void triggerNewMovieEvent(Movie movie, FileInputStream posterStream) {
        managerHomeController.triggerStartStatusEvent("Inserisco " + movie.getTitolo() + " a sistema...");
        movieDao.insertNewMovie(movie, posterStream);
        managerHomeController.triggerToHomeNewMovieEvent();
        refreshUIandMovieList();
        managerHomeController.triggerEndStatusEvent(movie.getTitolo() + " correttamente inserito a sistema!");
    }

    /**
     * Segnala alla Home il cambiamento di un film che non interessa la sua locandina.
     * @param movie -> film interessato dal cambiamento
     */
    void triggerOverwriteMovieButNotPosterEvent(Movie movie) {
        triggerToHome(movie, null);
    }

    /**
     * Segnala alla Home il cambiamento di un film che interessa anche la sua locandina.
     * @param movie -> film interessato dal cambiamento
     * @param posterStream -> stream della locandina da salvare
     */
    void triggerOverwriteMovieEvent(Movie movie, FileInputStream posterStream) {
        triggerToHome(movie, posterStream);
    }

    //Metodo che effettivamente si occupa di aggiornare le informazioni del film e di segnalare alla Home il cambiamento
    private void triggerToHome(Movie movie, FileInputStream posterStream) {
        managerHomeController.triggerStartStatusEvent("Aggiorno " + movie.getTitolo() + "...");
        if(posterStream == null) {
            movieDao.updateMovieButNotPoster(movie);
            managerHomeController.triggerToHomeNewMovieEvent();
            refreshUIandMovieList();
        } else {
            movieDao.updateMovie(movie, posterStream);
            managerHomeController.triggerToHomeNewMovieEvent();
            refreshUIandMovieList();
        }
        managerHomeController.triggerEndStatusEvent(movie.getTitolo() + " correttamente aggiornato!");
    }

    private void refreshUIandMovieList() { createUI(); }

    private void refreshUI() { createMovieGrid(); }

    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) moviePanel.getScene().getWindow();
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
            return 1;
        } else if(width>800 && width<=1360) {
            return 2;
        } else if(width>1360 && width<=1600) {
            return 3;
        } else if(width>1600) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * Metodo chiamato alla chiusura del progetto o dell'area manager:
     *     permette di chiudere le eventuali sottofinestre del pannello delle programmazioni e dell'editor film
     */
    @Override
    public void closeAllSubWindows() {
        if(movieEditorStage != null) {
            if(movieEditorStage.isShowing()) {
                isMovieEditorAlreadyOpened = false;
                movieEditorStage.close();
            }
        }

        if(movieSchedulerStage != null) {
            if(movieSchedulerStage.isShowing()) {
                msc.closeAllSubWindows();
                movieSchedulerStage.close();
                isMovieSchedulerAlreadyOpened = false;
            }
        }
    }
}
