package it.unipv.controller.common;

import it.unipv.db.DBConnection;

public interface IManagerAreaInitializer {
    void init(IHomeTrigger homeController, DBConnection dbConnection);
    void closeAllSubWindows();
}
