package it.unipv.model;

/**
 * AVAILABLE -> film programmabile, visibile nella Home e nel pannello "Programmazione" dell'area manager
 * NOT_AVAILABLE -> film non programmabile, non visibile nella Home e nel pannello "Programmazione" dell'area manager;
 *                      è possibile visualizzarlo nel pannello "Lista Film" dell'area manager.
 * NOT_RECOGNIZE -> film con uno stato non riconosciuto, non gestibile. È usato per segnalare eventuali errori.
 */
public enum MovieStatusTYPE {
      AVAILABLE
    , NOT_AVAILABLE
    , NOT_RECOGNIZE
}
