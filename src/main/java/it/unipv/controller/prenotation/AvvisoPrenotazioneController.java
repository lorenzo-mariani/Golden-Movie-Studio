package it.unipv.controller.prenotation;

import it.unipv.controller.common.GUIUtils;
import it.unipv.controller.common.IHomeTrigger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller di resources/fxml/prenotation/AvvisoPrenotazione.fxml
 * Questa classe viene utilizzata per confermare la prenotazione di un film e rimandare all'area utenti.
 */
public class AvvisoPrenotazioneController {

    @FXML private Label areaRiservataButton;
    private IHomeTrigger homeController;

    /**
     * Metodo principale del controller, deve essere chiamato all'inizializzazione della classe;
     * @param homeController -> il controller della Home del cinema, alla quale si segnala di dover aprire l'area riservata.
     */
    public void init(IHomeTrigger homeController) {
        this.homeController = homeController;
        GUIUtils.setScaleTransitionOnControl(areaRiservataButton);
    }

    private void doClose() {
        Stage stage = (Stage) areaRiservataButton.getScene().getWindow();
        stage.close();
    }

    @FXML private void openReservedAreaListener() {
        homeController.triggerOpenReservedArea();
        doClose();
    }
    
}
