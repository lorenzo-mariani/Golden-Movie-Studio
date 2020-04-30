package it.unipv.controller.common;

import it.unipv.db.DBConnection;

public interface IHomeInitializer {
    void init(DBConnection dbConnection);
    void closeAll();
}
