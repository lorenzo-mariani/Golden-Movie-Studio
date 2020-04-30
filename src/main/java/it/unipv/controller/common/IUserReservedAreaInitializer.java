package it.unipv.controller.common;

import it.unipv.db.DBConnection;
import it.unipv.model.User;

public interface IUserReservedAreaInitializer {
    void init(User loggedUser, DBConnection dbConnection);
    void closeAllSubWindows();
}
