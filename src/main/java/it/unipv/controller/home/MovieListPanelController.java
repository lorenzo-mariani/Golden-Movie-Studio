package it.unipv.controller.home;

import it.unipv.db.DBConnection;
import it.unipv.dao.MovieDao;
import it.unipv.dao.MovieDaoImpl;
import it.unipv.controller.common.*;
import it.unipv.model.Movie;
import it.unipv.model.MovieStatusTYPE;
import it.unipv.model.MovieTYPE;
import it.unipv.utils.ApplicationException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller di resources/fxml/home/movieList.fxml
 * Questa classe viene utilizzata per mostrare, nella Home, la lista dei film programmati attualmente presenti a sistema:
 *     viene mostrata la locandina ed al click su di essa si apre il pannello delle informazioni del film.
 */
public class MovieListPanelController {
    private MovieDao movieDao;
    private GridPane filmGrid = new GridPane();
    private List<Movie> movies = new ArrayList<>();
    private List<Movie> filteredMovies = new ArrayList<>();
    private static int rowCount = 0;
    private static int columnCount = 0;
    private static int columnMax = 0;
    private boolean isGridFiltered = false;
    private IHomeTrigger homeController;
    @FXML private ScrollPane movieScroll;
    @FXML private Rectangle rectangleGenere, rectangle2D3D;
    @FXML private AnchorPane filterContainer, genereWindow;
    @FXML private Line lineGenere;
    @FXML private Label genreLabel;
    @FXML private ImageView homeImage;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param homeController -> serve per segnalare alla home (statusBar) le operazioni effettuate.
     * @param dbConnection -> la connessione al database con la quale si istanzia MovieDaoImpl.
     */
    public void init(IHomeTrigger homeController, DBConnection dbConnection) {
        this.movieDao = new MovieDaoImpl(dbConnection);
        this.homeController = homeController;

        rectangle2D3D.setVisible(false);
        lineGenere.setVisible(false);
        genereWindow.setVisible(false);
        genereWindow.setPickOnBounds(false);
        filterContainer.setPickOnBounds(false);
        columnMax = getColumnMaxFromPageWidth(movieScroll.getScene().getWindow().getWidth());

        setHomeIconListener();
        createUI();
        checkPageDimension();
    }

    private void createUI() {
        homeController.triggerStartStatusEvent("Carico i film programmati...");
        Platform.runLater(() -> {
            initMovieList();
            initMovieGrid(movies);
        });
        homeController.triggerEndStatusEvent("Film programmati correttamente caricati!");
    }

    private void setHomeIconListener() {
        GUIUtils.setScaleTransitionOnControl(homeImage);
        homeImage.setOnMouseClicked(e -> {
            if(isGridFiltered) {
                homeController.triggerOpenProgrammationPanel();
                isGridFiltered = false;
            }
        });
    }

    private void initMovieList() {
        movies = movieDao.retrieveCompleteMovieList(1000, 0, true, true);
        Collections.sort(movies);
    }

    //Metodo che crea la visualizzazione in griglia dei film
    private void initMovieGrid(List<Movie> movies){
        filmGrid.getChildren().clear();
        filmGrid.setHgap(120);
        filmGrid.setVgap(80);

        for (Movie movie : movies) {
            if(movie.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                addMovie(movie);
            }
        }

        initRowAndColumnCount();
    }

    private void initRowAndColumnCount() {
        rowCount = 0;
        columnCount = 0;
    }

    //Metodo che crea la singola cella della griglia, contenete la locandina del singolo film
    private void addMovie(Movie movie){
        ImageView posterPreview = new ImageView(movie.getLocandina());
        posterPreview.setPreserveRatio(true);
        posterPreview.setFitWidth(200);

        Label posterPreviewLabel = new Label();
        posterPreviewLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        posterPreviewLabel.setGraphic(posterPreview);
        posterPreviewLabel.setTooltip(new Tooltip(movie.getTitolo() + " (" + getRealMovieType(movie.getTipo()) + ")"));

        GUIUtils.setScaleTransitionOnControl(posterPreviewLabel);

        AnchorPane anchor = new AnchorPane();

        if (columnCount == columnMax) {
            columnCount = 0;
            rowCount++;
        }

        filmGrid.add(anchor, columnCount, rowCount);
        columnCount++;

        movieScroll.setContent(filmGrid);
        GridPane.setMargin(anchor, new Insets(15, 0, 5, 15));

        anchor.getChildren().addAll(posterPreviewLabel);
        posterPreviewLabel.setLayoutX(30);
        if (rowCount == 0) {
            posterPreviewLabel.setLayoutY(20);
        }

        //Segnalo alla home che l'utente ha cliccato sul film e quindi bisogna mostrare le informazioni riguardanti quel film
        posterPreviewLabel.setOnMouseClicked(e -> homeController.triggerMovieClicked(movie));
    }

    private String getRealMovieType(MovieTYPE type) {
        return type.equals(MovieTYPE.TWOD) ? "2D" : "3D";
    }

    //Metodo che si occupa dell'animazione del menu a tendina dei generi
    public void animationGenere(){
        if(!genereWindow.isVisible()){
            genereWindow.setOpacity(0);
            genereWindow.setVisible(true);
            new Timeline(new KeyFrame(javafx.util.Duration.seconds(0.3), new KeyValue(rectangleGenere.heightProperty(), rectangleGenere.getHeight()+240))).play();
            FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.seconds(0.4), genereWindow);
            fadeIn.setDelay(javafx.util.Duration.seconds(0.2));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.seconds(0.1), genereWindow);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.play();
            genereWindow.setVisible(false);
            new Timeline(new KeyFrame(javafx.util.Duration.seconds(0.3), new KeyValue(rectangleGenere.heightProperty(), rectangleGenere.getHeight() - 240))).play();
        }
    }

    private MovieTYPE type;

    //Metodo che si occupa di spostare e ridimensionare la linea che segnala il genere sul quale abbiamo fermato il puntatore
    @FXML private void hoverGenereEnter(MouseEvent event){
        Label label = (Label) event.getSource();
        if(genereWindow.isVisible()){
            lineGenere.setVisible(true);
        }

        lineGenere.setLayoutY(label.getLayoutY()+30);
        lineGenere.setStartX(-label.getWidth()/2);
        lineGenere.setEndX(label.getWidth()/2);
    }

    //Metodo che si occupa del click sul genere: filtra la griglia dei film mostrando solo quelli che appartengono al genere cliccato
    @FXML private void genereClicked(MouseEvent event) {
        Label l = (Label)event.getSource();
        genreLabel.setText(l.getText());

        if(type==null) {
            filterMoviesByMovieGenre(genreLabel.getText());
        } else {
            filterMoviesByMovieTYPEAndMovieGenre(type, genreLabel.getText());
        }

        animationGenere();
    }

    //Metodo che si occupa di spostare il rettangolo da 2D a 3D e di filtrare la griglia dei film in base a ciò che viene cliccato dall'utente;
    @FXML private void click2D3D(MouseEvent event){
        filter2D3D(animation2D3D(event));
    }

    //Metodo che effettivamente si occupa di animare il rettangolo 2D 3D, spostandolo a seconda di ciò che l'utente clicca.
    private Label animation2D3D(MouseEvent event) {
        rectangle2D3D.setVisible(true);
        TranslateTransition transition = new TranslateTransition(javafx.util.Duration.seconds(0.2), rectangle2D3D);
        Label label = (Label) event.getSource();
        transition.setToX(label.getLayoutX()-rectangle2D3D.getLayoutX()-rectangle2D3D.getWidth()/4);
        transition.play();
        return label;
    }

    //Metodo che effettivamente si occupa di filtrare per 2D o 3D, in base a ciò che l'utente sceglie.
    private void filter2D3D(Label label) {
        switch(label.getText()) {
            case "2D":
                if(genreLabel.getText().toLowerCase().equalsIgnoreCase("genere")) {
                    filterMoviesByMovieTYPE(MovieTYPE.TWOD);
                } else {
                    filterMoviesByMovieTYPEAndMovieGenre(MovieTYPE.TWOD, genreLabel.getText());
                }
                type = MovieTYPE.TWOD;
                break;
            case "3D":
                if(genreLabel.getText().toLowerCase().equalsIgnoreCase("genere")) {
                    filterMoviesByMovieTYPE(MovieTYPE.THREED);
                } else {
                    filterMoviesByMovieTYPEAndMovieGenre(MovieTYPE.THREED, genreLabel.getText());
                }
                type = MovieTYPE.THREED;
                break;
        }
    }

    //Metodo che crea la griglia a seconda del tipo (2D o 3D) cliccato (in questo caso, il genere non è già stato scelto)
    private void filterMoviesByMovieTYPE(MovieTYPE type) {
        filteredMovies.clear();
        filmGrid.getChildren().clear();
        for(Movie m : movies) {
            if(m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                if(m.getTipo().equals(type)) {
                    addMovie(m);
                    filteredMovies.add(m);
                }
            }
        }
        initRowAndColumnCount();
        isGridFiltered = true;
    }

    //Metodo che crea la griglia a seconda del genere cliccato (in questo caso il tipo 2D o 3D non è già stato scelto)
    private void filterMoviesByMovieGenre(String genere) {
        filmGrid.getChildren().clear();
        filteredMovies.clear();
        for(Movie m : movies) {
            if(m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                if(m.getGenere().toLowerCase().contains(genere.toLowerCase())) {
                    addMovie(m);
                    filteredMovies.add(m);
                }
            }
        }
        initRowAndColumnCount();
        isGridFiltered = true;
    }

    //Metodo che crea la griglia a seconda del genere e del tipo cliccato
    private void filterMoviesByMovieTYPEAndMovieGenre(MovieTYPE type, String genere) {
        filmGrid.getChildren().clear();
        filteredMovies.clear();
        for(Movie m : movies) {
            if(m.getStatus().equals(MovieStatusTYPE.AVAILABLE)) {
                if(m.getGenere().toLowerCase().contains(genere.toLowerCase()) && m.getTipo().equals(type)) {
                    addMovie(m);
                    filteredMovies.add(m);
                }
            }
        }
        initRowAndColumnCount();
        isGridFiltered = true;
    }

    /** Metodo invocato dalla Home quando viene segnalato un evento riguardante la lista film; a questo punto si ricrea l'interfaccia */
    void triggerNewMovieEvent() { createUI(); }

    //Metodo utilizzato per monitorare la dimensione della finestra e modificare la UI in base ai cambiamenti
    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) movieScroll.getScene().getWindow();
            stage.widthProperty().addListener(e -> {
                columnMax = getColumnMaxFromPageWidth(stage.getWidth());
                if (temp != columnMax) {
                    temp = columnMax;
                    if(isGridFiltered) {
                        initMovieGrid(filteredMovies);
                    } else {
                        initMovieGrid(movies);
                    }
                }
            });
        });
    }

    private int getColumnMaxFromPageWidth(double width) {
        if(width<800) {
            return 2;
        } else if(width>=800 && width<=1360) {
            return 3;
        } else if(width>1360 && width<=1400) {
            return 3;
        } else if(width>1400 && width<=1700) {
            return 4;
        } else if(width>1700){
            return 5;
        } else {
            throw new ApplicationException("Impossibile settare numero colonne per width: " + width);
        }
    }
}
