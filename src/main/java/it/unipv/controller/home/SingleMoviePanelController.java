package it.unipv.controller.home;

import it.unipv.controller.common.IHomeTrigger;
import it.unipv.db.DBConnection;
import it.unipv.dao.ScheduleDao;
import it.unipv.dao.ScheduleDaoImpl;
import it.unipv.controller.prenotation.MoviePrenotationController;
import it.unipv.controller.common.GUIUtils;
import it.unipv.controller.common.ICloseablePane;
import it.unipv.model.Movie;
import it.unipv.model.Schedule;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import it.unipv.utils.DataReferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller di resources/fxml/home/sinlgeMoviePanel.fxml
 * Questa classe viene utilizzata per mostrare, nella Home, le informazioni riguardanti il singolo film e i giorni in cui esso viene proiettato:
 *     viene mostrata la locandina e le informazioni del film, con anche le date delle proiezioni che, se cliccate, portano al form di prenotazione.
 */
public class SingleMoviePanelController implements ICloseablePane {
    private AnchorPane singleFilmPane = new AnchorPane();
    private IHomeTrigger homeController;
    private Movie movie;
    private User loggedUser;
    private DBConnection dbConnection;
    private MoviePrenotationController mpc;
    private Stage prenotationStage;
    private ScheduleDao scheduleDao;
    private Font font = new Font("Bebas Neue Regular", 24);
    @FXML private ScrollPane singleMovieScroll;
    @FXML private Label goBackToProgrammationButton;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param homeController -> serve per segnalare alla home (statusBar) le operazioni effettuate;
     * @param movie -> è il singolo film che viene mostrato;
     * @param loggedUser -> l'utente che è attualmente loggato, è null se non c'è nessuno loggato;
     * @param dbConnection -> la connessione al database con la quale si istanzia scheduleDaoImpl.
     */
    public void init(IHomeTrigger homeController, Movie movie, User loggedUser, DBConnection dbConnection) {
        this.homeController = homeController;
        this.movie = movie;
        this.loggedUser = loggedUser;
        this.dbConnection = dbConnection;
        this.scheduleDao = new ScheduleDaoImpl(dbConnection);
        createUI();
    }

    private void createUI() {
        homeController.triggerStartStatusEvent("Recupero le informazioni di " + movie.getTitolo() + "...");
        Platform.runLater(this::populateSingleFilmPane);
        homeController.triggerEndStatusEvent("Informazioni riguardanti " + movie.getTitolo() + " correttamente recuperate!");
    }

    //Metodo utilizzato per impostare le informazioni delle label che mostrano le informazioni del film
    private void setLabelParameter(Label toSet, String text, Color color, double layoutX, double layoutY) {
        toSet.setText(text);
        toSet.setTextFill(color);
        toSet.setLayoutX(layoutX);
        toSet.setLayoutY(layoutY);
        toSet.setFont(font);
    }

    //Metodo utilizzato per popolare la schermata con tutte le informazioni del film
    private void populateSingleFilmPane() {
        singleFilmPane.getChildren().clear();

        ImageView poster = new ImageView(movie.getLocandina());
        poster.setPreserveRatio(true);
        poster.setFitWidth(350);
        poster.setLayoutX(50);

        Label title = new Label();
        setLabelParameter( title
                         , "TITOLO: "
                         , Color.valueOf("db8f00")
                         , poster.getLayoutX() + poster.getFitWidth() + 20
                         , poster.getLayoutY() + 25);

        Label movieTitle = new Label();
        setLabelParameter( movieTitle
                         , movie.getTitolo()
                         , Color.WHITE
                         , title.getLayoutX() + 110
                         , title.getLayoutY());

        Label genre = new Label();
        setLabelParameter( genre
                         , "GENERE: "
                         , Color.valueOf("db8f00")
                         , title.getLayoutX()
                         , title.getLayoutY() + 50);

        Label movieGenre = new Label();
        setLabelParameter( movieGenre
                         , movie.getGenere()
                         , Color.WHITE
                         , movieTitle.getLayoutX()
                         , genre.getLayoutY());

        Label direction = new Label();
        setLabelParameter( direction
                         , "REGIA: "
                         , Color.valueOf("db8f00")
                         , title.getLayoutX()
                         , genre.getLayoutY() + 50);

        Label movieDirection = new Label();
        setLabelParameter( movieDirection
                         , movie.getRegia()
                         , Color.WHITE
                         , movieTitle.getLayoutX()
                         , direction.getLayoutY());

        Label cast = new Label();
        setLabelParameter( cast
                         , "CAST: "
                         , Color.valueOf("db8f00")
                         , title.getLayoutX()
                         , direction.getLayoutY() + 50);

        TextArea movieCast = new TextArea();
        movieCast.setText(StringUtils.abbreviate(movie.getCast(),170));
        if(movie.getCast().length()>170) {
            movieCast.setTooltip(new Tooltip(getFormattedTooltipText(movie)));
        }
        setupTextArea(movieCast, movieDirection.getLayoutX()-15, cast.getLayoutY()-8, "movieCastTA");

        singleFilmPane.getChildren().addAll(cast, movieCast);
        singleMovieScroll.setContent(singleFilmPane);
        singleMovieScroll.applyCss();
        singleFilmPane.applyCss();
        singleMovieScroll.layout();
        singleFilmPane.layout();

        Label time = new Label();
        setLabelParameter( time
                         , "DURATA: "
                         , Color.valueOf("db8f00")
                         , cast.getLayoutX()
                         , movieCast.getLayoutY() + movieCast.prefHeightProperty().getValue() +1);

        Label movieTime = new Label();
        setLabelParameter( movieTime
                         , movie.getDurata() + " minuti"
                         , Color.WHITE
                         , movieTitle.getLayoutX()
                         , time.getLayoutY());

        Label year = new Label();
        setLabelParameter( year
                         , "ANNO: "
                         , Color.valueOf("db8f00")
                         , title.getLayoutX()
                         , time.getLayoutY() + 50);

        Label movieYear = new Label();
        setLabelParameter( movieYear
                         , movie.getAnno()
                         , Color.WHITE
                         , movieTitle.getLayoutX()
                         , year.getLayoutY());

        Label programmationsLabel = new Label();
        setLabelParameter( programmationsLabel
                         , "PROGRAMMATO PER: "
                         , Color.valueOf("db8f00")
                         , title.getLayoutX()
                         , year.getLayoutY() + 60);

        createHourLabels(title.getLayoutX()+20, programmationsLabel.getLayoutY()+40);

        Label synopsis = new Label();
        setLabelParameter( synopsis
                         , "TRAMA: "
                         , Color.valueOf("db8f00")
                         , poster.getLayoutX() + 15
                         , poster.getLayoutY() + 515);

        TextArea movieSynopsis = new TextArea();
        movieSynopsis.setText(movie.getTrama());
        setupTextArea(movieSynopsis, synopsis.getLayoutX()-15, synopsis.getLayoutY()+30, "movieSynopsisTA");
        movieSynopsis.setPrefWidth(1400);

        singleFilmPane.getChildren().addAll( title, movieTitle
                                           , genre, movieGenre
                                           , direction, movieDirection
                                           , time, movieTime
                                           , year, movieYear
                                           , programmationsLabel
                                           , poster
                                           , synopsis, movieSynopsis);

        GUIUtils.setScaleTransitionOnControl(goBackToProgrammationButton);
        goBackToProgrammationButton.getStylesheets().add("css/BebasNeue.css");
        goBackToProgrammationButton.setOnMouseClicked(event -> homeController.triggerOpenProgrammationPanel());

    }

    /* Metodo utilizzato per inizializzare le TextArea; visto che le informazioni possono variare da film a film,
     *     non posso impostare una grandezza fissa per le textarea della trama e del cast, quindi esse si devono
     *     ridimensionare in base al contenuto.
    */
    private void setupTextArea(TextArea textArea, double layoutX, double layoutY, String styleClass) {
        textArea.getStylesheets().add("css/TextAreaStyle.css");
        textArea.sceneProperty().addListener((observableNewScene, oldScene, newScene) -> {
            if (newScene != null) {
                textArea.applyCss();
                Node text = textArea.lookup(".text");

                textArea.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> textArea.getFont().getSize() + text.getBoundsInLocal().getHeight(), text.boundsInLocalProperty()));

                text.boundsInLocalProperty().addListener((observableBoundsAfter, boundsBefore, boundsAfter) -> Platform.runLater(textArea::requestLayout)
                );
            }
        });
        textArea.getStyleClass().add(styleClass);
        textArea.setWrapText(true);
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setLayoutX(layoutX);
        textArea.setLayoutY(layoutY);
    }

    // Metodo che crea le label contenenti i giorni delle proiezioni. Al click su una di esse, porta alla prenotazione di quella data specifica.
    private void createHourLabels(double initialX, double initialY) {
        double x = initialX;
        double y = initialY;
        int count = 0;
        int i = 0;

        //Metto massimo 10 date perché se no si sovrappongono con la trama.. tanto ogni giorno toglie una data vecchia e ne aggiunge una nuova
        List<Schedule> schedules = getProgrammationListFromMovie(movie);
        for(Schedule ms : schedules) {
            if(i<10) {
                if(!ApplicationUtils.checkIfDateIsInThePast(ms.getDate())) {
                    Label scheduleLabel = new Label();
                    scheduleLabel.setText("  " + ms.getDate() + "  ");
                    scheduleLabel.setTextFill(Color.WHITE);
                    if(count>=5) {
                        y+=50;
                        x = initialX;
                        count = 0;
                    }
                    scheduleLabel.setLayoutY(y);
                    scheduleLabel.setLayoutX(x);
                    scheduleLabel.setFont(font);
                    scheduleLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
                    scheduleLabel.setOnMouseEntered(event -> {
                        scheduleLabel.setBorder(new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
                        scheduleLabel.setCursor(Cursor.HAND);
                    });
                    scheduleLabel.setOnMouseExited(event -> {
                        scheduleLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));
                        scheduleLabel.setCursor(Cursor.DEFAULT);
                    });

                    scheduleLabel.setOnMouseClicked(event -> {
                        if(loggedUser==null) {
                            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Devi aver effettuato il login per poter accedere alla prenotazione!");
                        } else if (!isHimANormalUser(loggedUser)){
                            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Non puoi effettuare una prenotazione con questo account!");
                        } else {
                            openPrenotationStage(movie, scheduleLabel);
                        }
                    });
                    x += 190;
                    count++;
                    singleFilmPane.getChildren().add(scheduleLabel);
                }
            }
            i++;
        }
    }

    //Metodo che controlla se l'utente loggato è un admin: in questo caso, non si può effettuare una prenotazione.
    private boolean isHimANormalUser(User user) {
        return !( user.getNome().equalsIgnoreCase(DataReferences.ADMINUSERNAME)
                &&  user.getPassword().equalsIgnoreCase(DataReferences.ADMINPASSWORD));
    }

    //Metodo utilizzato per formattare il tooltip per non renderlo così lungo da uscire dalla schermata.
    private String getFormattedTooltipText(Movie movie) {
        StringBuilder res = new StringBuilder();
        char[] temp = movie.getCast().toCharArray();
        int cont = 0;
        for(int i=0; i<movie.getCast().length();i++) {
            if(cont == 5) {
                res.append("\n").append(temp[i]);
                cont = 0;
            } else {
                res.append(temp[i]);
            }
            if(temp[i] == ',') {
                cont++;
            }
        }
        return res.toString();
    }

    private boolean isPrenotationAreaOpened = false;
    private void openPrenotationStage(Movie movie, Label scheduleLabel) {
        if(!isPrenotationAreaOpened) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prenotation/MoviePrenotation.fxml"));
                Parent p = loader.load();
                mpc = loader.getController();
                mpc.init(homeController, scheduleLabel.getText().trim(), movie, loggedUser, dbConnection);
                prenotationStage = new Stage();
                prenotationStage.setScene(new Scene(p));
                prenotationStage.setResizable(false);
                prenotationStage.setTitle("Prenotazione " + movie.getTitolo() + " " + scheduleLabel.getText().trim());
                prenotationStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
                prenotationStage.setOnCloseRequest( event -> isPrenotationAreaOpened = false);
                prenotationStage.show();
                isPrenotationAreaOpened = true;
            } catch (IOException ex) {
                throw new ApplicationException(ex);
            }
        }
    }

    private List<Schedule> getProgrammationListFromMovie(Movie m) {
        String date = "";
        List<Schedule> allSchedules = scheduleDao.retrieveMovieSchedules();
        Collections.sort(allSchedules);
        List<Schedule> res = new ArrayList<>();
        for(Schedule ms : allSchedules) {
            if(ms.getMovieCode().equals(m.getCodice())) {
                if(!date.equals(ms.getDate())) {
                    res.add(ms);
                    date = ms.getDate();
                }
            }
        }
        return res;
    }

    /** Metodo chiamato in chiusura del progetto:
     *      permette di chiudere la sottofinestra della prenotazione, se aperta, comprese le sue eventuali sottofinestre aggiuntive.
    */
    @Override
    public void closeAllSubWindows() {
        if(prenotationStage!=null) { prenotationStage.close(); }
        if(mpc!=null) { mpc.closeAllSubWindows(); }
    }
}
