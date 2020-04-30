package it.unipv.dao;

import it.unipv.model.Prenotation;

import java.util.List;

public interface PrenotationDao {
    List<Prenotation> retrievePrenotationList();
    void insertNewPrenotation(Prenotation toInsert);
    void deletePrenotation(Prenotation toDelete);

}
