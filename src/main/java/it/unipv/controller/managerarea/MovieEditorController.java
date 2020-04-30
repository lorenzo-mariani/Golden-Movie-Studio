package it.unipv.controller.managerarea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import it.unipv.controller.common.GUIUtils;
import it.unipv.model.Movie;
import it.unipv.model.MovieStatusTYPE;
import it.unipv.model.MovieTYPE;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

/**
 * Controller di resources/fxml/managerarea/MovieEditor.fxml
 * Questa classe viene utilizzata per:
 *    1) Creare un nuovo film
 *    2) Modificare un film già esistente
 */
public class MovieEditorController {

    @FXML private TextField imgTextField;
    @FXML private Button searchButton;
    @FXML private TextField titleTextField;
    @FXML private TextField genreTextField;
    @FXML private TextField directionTextField;
    @FXML private TextField castTextField;
    @FXML private TextField timeTextField;
    @FXML private TextField yearTextField;
    @FXML private ComboBox movieTypeComboBox;
    @FXML private TextArea plotTextArea;
    @FXML private Label saveButton;
    private boolean wasItAlreadyCreated;
    private Movie movie;
    private ProgrammationPanelController programmationPanelController;
    private MovieListPanelController movieListPanelController;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     *     Questo init imposta l'editor per la creazione di un film
     * @param programmationPanelController -> il controller della pagina delle programmazioni
     */
    void init(ProgrammationPanelController programmationPanelController) {
        this.programmationPanelController = programmationPanelController;
        wasItAlreadyCreated = false;
        initMovieTypeComboBox();
        GUIUtils.setScaleTransitionOnControl(saveButton);
        setFileChooser();
        setMaxCharToPlotTextArea();
        setTextfieldToNumericOnlyTextfield(timeTextField, yearTextField);
        imgTextField.setEditable(false);
    }

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     *     Questo init imposta l'editor per la modifica di un film dal pannello delle programmazioni.
     * @param movie -> film da modificare
     * @param programmationPanelController -> il controller della pagina delle programmazioni
     */
    void init(Movie movie, ProgrammationPanelController programmationPanelController) {
        this.programmationPanelController = programmationPanelController;
        this.movie = movie;
        wasItAlreadyCreated = true;
        GUIUtils.setScaleTransitionOnControl(saveButton);
        initMovieTypeComboBox();
        setComponents();
        setFileChooser();
        setMaxCharToPlotTextArea();
        setTextfieldToNumericOnlyTextfield(timeTextField, yearTextField);
        imgTextField.setEditable(false);
    }

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     *     Questo init imposta l'editor per la modifica di un film dal pannello della lista film.
     * @param movie -> film da modificare
     * @param movieListPanelController -> il controller della pagina della lista film
     */
    void init(Movie movie, MovieListPanelController movieListPanelController) {
        this.movieListPanelController = movieListPanelController;
        this.movie = movie;
        wasItAlreadyCreated = true;
        GUIUtils.setScaleTransitionOnControl(saveButton);
        initMovieTypeComboBox();
        setComponents();
        setFileChooser();
        imgTextField.setEditable(false);
    }

    //Do un tetto massimo di caratteri inseribili come trama perché se no potrebbe appesantire l'UI e il caricamento dei dati
    private void setMaxCharToPlotTextArea() {
        plotTextArea.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 1100 ? change : null));

    }

    //I campi durata e anno devono contenere solo numeri e non anche caratteri
    private void setTextfieldToNumericOnlyTextfield(TextField... tf) {
        for(TextField t : tf) {
            t.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    t.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }
    }

    private void initMovieTypeComboBox() {
        movieTypeComboBox.getItems().clear();
        movieTypeComboBox.setItems(FXCollections.observableArrayList("2D", "3D"));
    }

    //Se si è in modalità modifica allora devo inizializzare i campi con le informazioni prese dal database
    private void setComponents() {
        titleTextField.setText(movie.getTitolo());
        genreTextField.setText(movie.getGenere());
        directionTextField.setText(movie.getRegia());
        castTextField.setText(movie.getCast());
        timeTextField.setText(movie.getDurata());
        yearTextField.setText(movie.getAnno());
        if(movie.getTipo().equals(MovieTYPE.TWOD)) {
            movieTypeComboBox.getSelectionModel().select("2D");
        } else {
            movieTypeComboBox.getSelectionModel().select("3D");
        }
        plotTextArea.setText(movie.getTrama());
    }

    //Il FileChooser è la finestra che permette di scegliere file dal pc (in questo caso locandine con formato jpg, png o gif)
    private void setFileChooser() {
        searchButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini JPEG", "*.jpg", "*.JPG", ".*JPEG"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini PNG", "*.png", "*.PNG"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini GIF", "*.gif", "*.GIF"));

            File file = fileChooser.showOpenDialog(null);
            if(file != null) {
                imgTextField.setText(file.getPath());
            }
        });
    }

    /* Listener al pulsante salva.
     * In modalità creazione da errore se rimangono campi non compilati.
     * In modalità modifica da errore se tutti i campi, ad esclusione della locandina, non vengono compilati:
     *     questo perché se si va a modificare un film non per forza si vuole modificare la locandina e, quindi,
     *     si può risparmiare sul tempo di upload dell'immagine.
     */
    @FXML private void saveButtonListener() {
        if( titleTextField.getText().trim().equalsIgnoreCase("")
         || genreTextField.getText().trim().equalsIgnoreCase("")
         || directionTextField.getText().trim().equalsIgnoreCase("")
         || castTextField.getText().trim().equalsIgnoreCase("")
         || timeTextField.getText().trim().equalsIgnoreCase("")
         || yearTextField.getText().trim().equalsIgnoreCase("")
         || movieTypeComboBox.getValue() == null
         || plotTextArea.getText().trim().equalsIgnoreCase("")){
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Devi compilare tutti i campi!");
        } else {
            if(!wasItAlreadyCreated) {
                if(imgTextField.getText().trim().equalsIgnoreCase("")) {
                    GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Devi compilare tutti i campi!");
                } else {
                    createNewMovie();
                    GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Successo", "Operazione riuscita: ", "Inserimento nuovo film riuscito con successo!");
                }
            } else {
                if(programmationPanelController==null) {
                    updateMovieAndTriggerToMovieListPanelController();
                    GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Successo", "Operazione riuscita: ", "Aggiornamento film riuscito con successo!");
                } else if(movieListPanelController==null) {
                    updateMovieAndTriggerToProgrammationListPanelController();
                    GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Successo", "Operazione riuscita: ", "Aggiornamento film riuscito con successo!");
                } else {
                    throw new ApplicationException("Unknown summoner!");
                }
            }
        }
    }

    //Segnala al pannello delle programmazioni l'inserimento di un nuovo film
    private void createNewMovie() {
        Movie m = getMovieFromTextFields();
        try {
            programmationPanelController.triggerNewMovieEvent(m, new FileInputStream(imgTextField.getText()));
        } catch (FileNotFoundException e) {
            throw new ApplicationException(e);
        }
        wasItAlreadyCreated = true;
        movie = m;
    }

    //Segnala al pannello delle programmazioni l'aggiornamento di un film
    private void updateMovieAndTriggerToProgrammationListPanelController() {
        if(imgTextField.getText().trim().equalsIgnoreCase("")) {
            programmationPanelController.triggerOverwriteMovieButNotPosterEvent(getMovieFromTextFields());
        } else {
            try {
                programmationPanelController.triggerOverwriteMovieEvent(getMovieFromTextFields(), new FileInputStream(imgTextField.getText()));
            } catch (FileNotFoundException e) {
                throw new ApplicationException(e);
            }
        }
    }

    //Segnala al pannello della lista film l'aggiornamento di un film
    private void updateMovieAndTriggerToMovieListPanelController() {
        if(imgTextField.getText().trim().equalsIgnoreCase("")) {
            movieListPanelController.triggerOverwriteMovieButNotPosterEvent(getMovieFromTextFields());
        } else {
            try {
                movieListPanelController.triggerOverwriteMovieEvent(getMovieFromTextFields(), new FileInputStream(imgTextField.getText()));
            } catch (FileNotFoundException e) {
                throw new ApplicationException(e);
            }
        }
    }

    //Tramite le informazioni inserite nelle textfield posso creare il film di conseguenza
    private Movie getMovieFromTextFields() {
        Movie m = new Movie();
        m.setTitolo(titleTextField.getText());
        m.setGenere(genreTextField.getText());
        m.setRegia(directionTextField.getText());
        m.setCast(castTextField.getText());
        m.setDurata(timeTextField.getText());
        m.setAnno(yearTextField.getText());
        m.setTrama(plotTextArea.getText());
        if(movieTypeComboBox.getValue().toString().equalsIgnoreCase("2D")) {
            m.setTipo(MovieTYPE.TWOD);
        } else if(movieTypeComboBox.getValue().toString().equalsIgnoreCase("3D")){
            m.setTipo(MovieTYPE.THREED);
        }

        if(!wasItAlreadyCreated) {
            m.setStatus(MovieStatusTYPE.AVAILABLE);
            m.setCodice(ApplicationUtils.getUUID());
        } else {
            m.setStatus(movie.getStatus());
            m.setCodice(movie.getCodice());
        }
        return m;
    }

}
