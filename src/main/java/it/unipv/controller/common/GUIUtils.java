package it.unipv.controller.common;

import it.unipv.conversion.PrenotationToPDF;
import it.unipv.model.Prenotation;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.CloseableUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static java.lang.Thread.sleep;

/** Questa classe è un contenitore di metodi utili a tutti i controller. */
public class GUIUtils {

    /**
     * Imposta una animazione di fade in/out ad un nodo non specificato (quindi può essere applicato a tutto ciò che estende Node);
     *     normalmente, il Node verrà mostrato al 50% della sua opacità; se ci si posiziona con il mouse sul Node allora parte l'animazione
     *     di fadeIn che lo porta al 100% della sua opacità; quando si esce con il puntatore dalla sua area, parte l'animazione
     *     di fadeOut che lo riporta al 50%^della sua opacità.
     * @param control -> l'elemento che estende Node su cui vogliamo porre l'animazione.,
     */
    public static void setFadeInOutOnControl(Node control) {
        final FadeTransition fadeIn = new FadeTransition(Duration.millis(100));
        fadeIn.setNode(control);
        fadeIn.setToValue(1);
        control.setOnMouseEntered(e -> {
            fadeIn.playFromStart();
            control.setCursor(Cursor.HAND);
        });

        final FadeTransition fadeOut = new FadeTransition(Duration.millis(100));
        fadeOut.setNode(control);
        fadeOut.setToValue(0.5);
        control.setOnMouseExited(e -> {
            fadeOut.playFromStart();
            control.setCursor(Cursor.DEFAULT);
        } );

        control.setOpacity(0.5);
    }

    /**
     * Imposta una animazione di tipo scaleTransition ad un nodo non specificato (quindi può essere applicato a tutto ciò che estende Node);
     *     normalmente, il Node verrà mostrato alla sua dimensione naturale; se ci si posiziona su di esso con il puntatore, esso
     *     viene ingrandito di 1.2 volte e, una volta tolto il mouse, esso tornerà alle sue dimensioni standard.
     * @param control -> l'elemento che estende Node su cui vogliamo porre l'animazione.
     */
    public static void setScaleTransitionOnControl(Node control) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), control);
        scaleTransition.setCycleCount(1);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        DoubleProperty expandToMaxProperty = new SimpleDoubleProperty(1.2);

        control.setOnMouseEntered(event -> {
            control.setCursor(Cursor.HAND);
            scaleTransition.setFromX(control.getScaleX());
            scaleTransition.setFromY(control.getScaleY());
            scaleTransition.setToX(expandToMaxProperty.get());
            scaleTransition.setToY(expandToMaxProperty.get());
            scaleTransition.playFromStart();
        });


        control.setOnMouseExited(event -> {
            control.setCursor(Cursor.DEFAULT);
            scaleTransition.setFromX(control.getScaleX());
            scaleTransition.setFromY(control.getScaleY());
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.playFromStart();
        } );
    }

    /**
     * Metodo utilizzabile per creare una ImageView da un InputStream; è possibile utilizzarlo per creare icone 25x25
     * @param iconInputStream -> l'inputStream dell'icona
     * @return -> una ImageView dell'icona 25x25
     */
    public static ImageView getIconView(InputStream iconInputStream) {
        ImageView view;
        try {
            view = new ImageView(new Image(iconInputStream));
            view.setFitWidth(25);
            view.setFitHeight(25);
        } finally {
            CloseableUtils.close(iconInputStream);
        }
        return view;
    }

    /**
     * Metodo utilizzabile per mostrare un Alert (corrispettivo di JOPtionPane di awt)
     * @param type -> la tipologia di alert che si intende mostrare
     * @param title -> il titolo dell'alert
     * @param headerText -> l'intestazione dell'alert
     * @param message -> il messaggio dell'alert
     */
    public static void showAlert(Alert.AlertType type, String title, String headerText, String message) {
        Alert alert = new Alert(type);
        Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Metodo utilizzabile per mostrare un Alert di conferma, comprendente i tasti Sì/No
     * @param title -> il titolo dell'alert
     * @param headerText -> l'intestazione dell'alert
     * @param message -> il messaggio dell'alert
     * @return -> ritorna la tipologia di tasti presente nell'alert, così da ricavare ciò che l'utente ha cliccato.
     */
    public static Optional<ButtonType> showConfirmationAlert(String title, String headerText, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.NO, ButtonType.YES);
        Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Metodo utilizzabile per mostrare un Alert in cui poter inserire una stringa
     * @param title -> il titolo dell'alert
     * @param headerText -> l'intestazione dell'alert
     * @param message -> il messaggio dell'alert
     * @return -> ritorna la tipologia di tasti presente nell'alert, così da ricavare ciò che l'utente ha cliccato.
     */
    public static Optional<String> showInputAlert(String title, String headerText, String message) {
        TextInputDialog inputDialog = new TextInputDialog();
        Stage s = (Stage) inputDialog.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        inputDialog.setTitle(title);
        inputDialog.setHeaderText(headerText);
        inputDialog.setContentText(message);
        return inputDialog.showAndWait();
    }

    /**
     * Metodo utilizzabile per la creazione del Thread dei suggerimenti (presente nella status bar delle finestre principali)
     * @param tips -> lista di stringhe che rappresenta i suggerimenti da visualizzare in successione
     * @param tipsLabel -> la label che mostra il suggerimento
     * @param sleepTime -> tempo in millisecondi che esprime la pausa tra un suggerimento e l'altro
     * @return -> il Thread dei suggerimenti correttamente inizializzato.
     */
    public static Thread getTipsThread(List<String> tips, Label tipsLabel, int sleepTime) {
        return new Thread(() -> {
            boolean shouldDie = false;
            while (!shouldDie) {
                try {
                    for (String s : tips) {
                        Platform.runLater(() -> {
                            setFadeOutOnLabel(tipsLabel);
                            tipsLabel.setText(s);
                            setFadeInOnLabel(tipsLabel);
                        });
                        sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    shouldDie = true;
                }
            }
        });
    }

    //Metodo utilizzato per dare un'animazione di fadeIn al suggerimento quando arriva il suo turno
    private static void setFadeInOnLabel(Label label) {
        final FadeTransition fadeIn = new FadeTransition(Duration.millis(1000));
        fadeIn.setNode(label);
        fadeIn.setToValue(1);
        fadeIn.playFromStart();
    }

    //Metodo utilizzato per dare un'animazione di fadeOut al suggerimento quando scade il suo turno.
    private static void setFadeOutOnLabel(Label label) {
        final FadeTransition fadeOut = new FadeTransition(Duration.millis(1000));
        fadeOut.setNode(label);
        fadeOut.setToValue(0);
        fadeOut.playFromStart();
        label.setOpacity(0);
    }

    /**
     * Metodo utilizzabile per aprire il FileChooser in modalità di salvataggio per il PDF delle fatture
     * @param toSave -> prenotazione da salvare
     */
    public static void openPDFFileSaver(Prenotation toSave) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documenti PDF", "*.pdf"));

        fileChooser.setInitialFileName( "Prenotazione "
                                      + toSave.getNomeFilm().replaceAll("[-+.^:,]","")
                                      + " - " + (toSave.getGiornoFilm()+toSave.getOraFilm()).replaceAll("[-+/.^:,]","")
                                      + " - " + toSave.getNomeUtente()
                                      + ".pdf");
        File f = fileChooser.showSaveDialog(null);

        try{
            if(f!=null) {
                PrenotationToPDF.generatePDF(f.getPath(), "windows-1252", toSave);
                showAlert(Alert.AlertType.CONFIRMATION, "Conferma", "Operazione riuscita:", "Prenotazione correttamente salvata!\nPer pagare presentarsi con la fattura alla reception!");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore durante la creazione del PDF: ", ex.getMessage());
            throw new ApplicationException(ex);
        }
    }
}
