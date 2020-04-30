package it.unipv.controller.managerarea;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import it.unipv.db.*;
import it.unipv.dao.HallDao;
import it.unipv.dao.MovieDao;
import it.unipv.dao.ScheduleDao;
import it.unipv.dao.HallDaoImpl;
import it.unipv.dao.MovieDaoImpl;
import it.unipv.dao.ScheduleDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.model.Movie;
import it.unipv.model.Schedule;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import it.unipv.utils.DataReferences;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

/**
 * Controller di resources/fxml/managerarea/MovieSchedulerEditor.fxml
 * Questa classe viene utilizzata per creare una programmazione per un film.
 * La pausa tra un film e l'altro è di 30 minuti.
 * Casi in cui una programmazione non è accettata dal sistema:
 *     1) Se si inserisce stessa ora e stessa sala di una prenotazione già esistente;
 *     2) Se si inserisce un giorno nel passato;
 *     3) Se un film dura 120 minuti ed è programmato alle 12:00, non si può programmare un secondo film se non dopo 120+30 min
 *     4) Se esiste un film programmato alle 12, non posso programmare un secondo film alle 11.50:
 *            devo verificare che, prima di un film già salvato, ci siano (durata film da aggiungere + pausa) minuti disponibili
 */
public class MovieScheduleEditorController {

    @FXML private DatePicker datePicker;
    @FXML private AnchorPane timeSpinnerContainer;
    @FXML private ComboBox hallComboBox;
    @FXML private Label salvaProgrammazioneButton;
    private CustomTimeSpinner timeSpinner;
    private Movie movie;
    private MovieSchedulerController moviePanelController;
    private List<Schedule> schedules;
    private List<Movie> movies;
    private MovieDao movieDao;
    private HallDao hallDao;
    private ScheduleDao scheduleDao;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe.
     * @param movieSchedulerController -> controller da cui viene evocato questo form al quale si segnala la
     *                                        creazione di una nuova programmazione
     * @param movie -> film che si vuole programmare
     * @param dbConnection -> la connessione al database utilizzata per istanziare MovieDaoImpl, HallDaoImpl e ScheduleDaoImpl.
     */
    void init(MovieSchedulerController movieSchedulerController, Movie movie, DBConnection dbConnection) {
        this.movieDao = new MovieDaoImpl(dbConnection);
        this.hallDao = new HallDaoImpl(dbConnection);
        this.scheduleDao = new ScheduleDaoImpl(dbConnection);
        this.moviePanelController = movieSchedulerController;
        this.movie = movie;
        GUIUtils.setScaleTransitionOnControl(salvaProgrammazioneButton);
        initScheduleList();
        initMovieList();
        initTimePicker();
        initHallSelector();
    }

    private void initScheduleList() { schedules = scheduleDao.retrieveMovieSchedules(); }

    private void initMovieList() { movies =  movieDao.retrieveMovieListWithoutPoster(); }

    //Inizializzo la combobox delle sale, prendendo i nomi tramite database (hallDao)
    private void initHallSelector() {
        hallComboBox.getItems().clear();
        List<String> hallNames = hallDao.retrieveHallNames();
        Collections.sort(hallNames);
        hallComboBox.setItems(FXCollections.observableList(hallNames));
    }

    //Inizializzo il CustomTimeSpinner (unico elemento generato dal codice, il resto è fxml)
    private void initTimePicker() {
        timeSpinner = new CustomTimeSpinner();
        timeSpinnerContainer.getChildren().addAll(timeSpinner);
        timeSpinner.prefWidthProperty().bind(timeSpinnerContainer.widthProperty());
        timeSpinner.prefHeightProperty().bind(timeSpinnerContainer.heightProperty());
    }

    //Listener al pulsante salva, qua vengono effettuati i vari controlli sulle date
    @FXML private void initSaveButtonListener() {
        String date = datePicker.getValue() == null ? "" : ApplicationUtils.formatDate(datePicker.getValue().toString());
        String time = timeSpinner.getValue() == null ? "" : ApplicationUtils.formatTime(timeSpinner.getValue().toString());
        String hall = hallComboBox.getValue() == null ? "" : hallComboBox.getValue().toString();

        if( date.trim().equalsIgnoreCase("")
         || time.trim().equalsIgnoreCase("")
         || hall.trim().equalsIgnoreCase("") ){
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Devi compilare tutti i campi!");
        } else if (ApplicationUtils.checkIfDateIsInThePast(date + " " + time)) {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Non puoi programmare un film nel passato!");
        } else if(checkIfICanAddThisSchedule(hall, date + " " + time, Integer.parseInt(movie.getDurata())) ) {
            GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "C'è già una programmazione in questo periodo!");
        } else {
            doSave(date, time, hall);
            moviePanelController.triggerNewScheduleEvent();
            GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Successo", "Operazione riuscita: ", "Salvataggio riuscito correttamente!");
            initScheduleList();
            initMovieList();
        }
    }

    private void doSave(String date, String time, String hall) {
        Schedule schedule = new Schedule();
        schedule.setMovieCode(movie.getCodice());
        schedule.setDate(date);
        schedule.setTime(time);
        schedule.setHallName(hall);
        scheduleDao.insertNewMovieSchedule(schedule);
    }

    //Metodo che controlla se la programmazione inserita dal manager è coerente con le altre programmazioni esistenti
    private boolean checkIfICanAddThisSchedule(String hall, String incomingScheduleDate, int incomingMovieDuration) {
        for(Schedule ms : schedules) {
            if( (ms.getDate().trim().equalsIgnoreCase(incomingScheduleDate) && ms.getHallName().trim().equalsIgnoreCase(hall))
             || (ms.getHallName().trim().equalsIgnoreCase(hall)) ) {
                int existingMovieDuration = 0;
                for(Movie m : movies) {
                    if(ms.getMovieCode().equalsIgnoreCase(m.getCodice())) {
                        existingMovieDuration = Integer.parseInt(m.getDurata());
                        break;
                    }
                }
                if(!checkIfSomethingIsAlreadyScheduledInThatTemporalGap(ms.getDate() + " " + ms.getTime(), existingMovieDuration, incomingScheduleDate, incomingMovieDuration)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* Metodo che controlla se è possibile posizionare una programmazione nel range di tempo deciso dal manager
     * Non va bene posizionare una programmazione prima della fine di un film (più pausa di 30 min)
     * Non va bene posizionare una programmazione poco prima della programmazione di un altro film:
     *     bisogna che ci siano (durata del film da programmare + pausa) minuti disponibili prima del film successivo
    */
    private boolean checkIfSomethingIsAlreadyScheduledInThatTemporalGap(String existingScheduleDate, int existingMovieDuration, String incomingScheduleDate, int incomingMovieDuration) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar realIncomingScheduleDate = Calendar.getInstance();
        try {
            realIncomingScheduleDate.setTime(sdf.parse(incomingScheduleDate));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }

        return realIncomingScheduleDate.before(getTimeOccupiedBySchedule(existingScheduleDate, incomingMovieDuration, false))
                || realIncomingScheduleDate.after(getTimeOccupiedBySchedule(existingScheduleDate, existingMovieDuration, true));
    }

    //Metodo che calcola il tempo occupato da una programmazione
    private Calendar getTimeOccupiedBySchedule(String existingScheduleDate, int movieDuration, boolean isItToAdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar result = Calendar.getInstance();
        try {
            result.setTime(sdf.parse((existingScheduleDate)));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
        if(isItToAdd) { result.add(Calendar.MINUTE, movieDuration+DataReferences.PAUSEAFTERMOVIE); }
        if(!isItToAdd) { result.add(Calendar.MINUTE, -(movieDuration+DataReferences.PAUSEAFTERMOVIE)); }
        return result;
    }
}
