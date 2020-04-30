package it.unipv.dao;

import it.unipv.db.DBConnection;
import it.unipv.model.Schedule;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Questa classe fa riferimento alla tabella SCHEDULE
 * Si occupa di inserire/recuperare/eliminare i dati riguardanti le programmazioni dei film.
 */
public class ScheduleDaoImpl implements ScheduleDao {
    private DBConnection dbConnection;

    public ScheduleDaoImpl(DBConnection dbConnection) { this.dbConnection = dbConnection; }

    /**
     * Recupera le informazioni delle programmazioni dal database.
     * @return -> lista completa delle programmazioni.
     */
    @Override public List<Schedule> retrieveMovieSchedules() {
        return doRetrieveMovieSchedules();
    }

    /**
     * Permette di inserire una nuova programmazione nel database.
     * @param toInsert -> la nuova programmazione da inserire.
     */
    @Override public void insertNewMovieSchedule(Schedule toInsert) {
        doInsertNewMovieSchedule(toInsert);
    }

    /**
     * Permette di eliminare una programmazione dal database.
     * @param toDelete -> la programmazione da eliminare.
     */
    @Override public void deleteMovieSchedule(Schedule toDelete) {
        doDeleteMovieSchedule(toDelete);
    }

    private void doDeleteMovieSchedule(Schedule toDelete) {
        String query = "DELETE FROM "+ DataReferences.DBNAME + ".SCHEDULE where CODICE_FILM = ? AND DATA = ? AND ORA = ? AND SALA = ?";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toDelete.getMovieCode());
            ps.setString(2, toDelete.getDate());
            ps.setString(3, toDelete.getTime());
            ps.setString(4, toDelete.getHallName());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doInsertNewMovieSchedule(Schedule toInsert) {
        String query = "INSERT INTO " + DataReferences.DBNAME + ".SCHEDULE (CODICE_FILM, DATA, ORA, SALA) values (?,?,?,?)";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toInsert.getMovieCode());
            ps.setString(2, toInsert.getDate());
            ps.setString(3, toInsert.getTime());
            ps.setString(4, toInsert.getHallName());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<Schedule> doRetrieveMovieSchedules() {
        try {
            return getMovieSchedulesFromResultSet(dbConnection.getResultFromQuery("SELECT * FROM " + DataReferences.DBNAME + ".SCHEDULE"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<Schedule> getMovieSchedulesFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            List<Schedule> res = new ArrayList<>();
            while(resultSet.next()) {
                Schedule toAdd = new Schedule();
                toAdd.setMovieCode(resultSet.getString("CODICE_FILM"));
                toAdd.setDate(resultSet.getString("DATA"));
                toAdd.setTime(resultSet.getString("ORA"));
                toAdd.setHallName(resultSet.getString("SALA"));
                res.add(toAdd);
            }
            return res;
        } finally {
            resultSet.close();
        }
    }
}
