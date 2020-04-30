package it.unipv.dao;

import it.unipv.db.DBConnection;
import it.unipv.model.Prenotation;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Questa classe fa riferimento alla tabella PRENOTATION
 * Si occupa di inserire/recuperare/eliminare i dati riguardanti le prenotazioni.
 */
public class PrenotationDaoImpl implements PrenotationDao {
    private DBConnection dbConnection;

    public PrenotationDaoImpl(DBConnection dbConnection) { this.dbConnection = dbConnection; }

    /**
     * Recupera la lista delle prenotazioni dal database.
     * @return -> lista delle prenotazioni correttamente inizializzata.
     */
    @Override public List<Prenotation> retrievePrenotationList() { return doRetrievePrenotationList(); }

    /**
     * Inserisce le informazioni di una nuova prenotazione nel database.
     * @param toInsert -> la nuova prenotazione da inserire.
     */
    @Override public void insertNewPrenotation(Prenotation toInsert) { doInsertNewPrenotation(toInsert); }

    /**
     * Rimuove le informazioni di una prenotazione dal database.
     * @param toDelete -> la prenotazione da rimuovere.
     */
    @Override public void deletePrenotation(Prenotation toDelete) { doDeletePrenotation(toDelete); }

    private void doDeletePrenotation(Prenotation toDelete) {
        String query = "DELETE FROM " + DataReferences.DBNAME + ".PRENOTATION where "
                                                                                     + "NOMEUTENTE = ? AND "
                                                                                     + "NOMEFILM = ? AND "
                                                                                     + "CODICEFILM = ? AND "
                                                                                     + "GIORNOFILM = ? AND "
                                                                                     + "ORAFILM = ? AND "
                                                                                     + "SALAFILM = ? AND "
                                                                                     + "POSTISELEZIONATI = ? AND "
                                                                                     + "COSTOTOTALE = ?";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toDelete.getNomeUtente());
            ps.setString(2, toDelete.getNomeFilm());
            ps.setString(3, toDelete.getCodiceFilm());
            ps.setString(4, toDelete.getGiornoFilm());
            ps.setString(5, toDelete.getOraFilm());
            ps.setString(6, toDelete.getSalaFilm());
            ps.setString(7, toDelete.getPostiSelezionati());
            ps.setString(8, toDelete.getCostoTotale());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doInsertNewPrenotation(Prenotation toInsert) {
        String query = "INSERT INTO " + DataReferences.DBNAME + ".PRENOTATION (NOMEUTENTE, NOMEFILM, CODICEFILM, GIORNOFILM, ORAFILM, SALAFILM, POSTISELEZIONATI, COSTOTOTALE) values (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toInsert.getNomeUtente());
            ps.setString(2, toInsert.getNomeFilm());
            ps.setString(3, toInsert.getCodiceFilm());
            ps.setString(4, toInsert.getGiornoFilm());
            ps.setString(5, toInsert.getOraFilm());
            ps.setString(6, toInsert.getSalaFilm());
            ps.setString(7, toInsert.getPostiSelezionati());
            ps.setString(8, toInsert.getCostoTotale());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<Prenotation> doRetrievePrenotationList() {
        try {
            return getPrenotationsFromResultSet(dbConnection.getResultFromQuery("SELECT * FROM " + DataReferences.DBNAME + ".PRENOTATION"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<Prenotation> getPrenotationsFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            List<Prenotation> res = new ArrayList<>();
            while(resultSet.next()) {
                res.add(new Prenotation( resultSet.getString("NOMEUTENTE")
                                       , resultSet.getString("NOMEFILM")
                                       , resultSet.getString("CODICEFILM")
                                       , resultSet.getString("GIORNOFILM")
                                       , resultSet.getString("ORAFILM")
                                       , resultSet.getString("SALAFILM")
                                       , resultSet.getString("POSTISELEZIONATI")
                                       , resultSet.getString("COSTOTOTALE")));
            }
            return res;
        } finally {
            resultSet.close();
        }
    }
}
