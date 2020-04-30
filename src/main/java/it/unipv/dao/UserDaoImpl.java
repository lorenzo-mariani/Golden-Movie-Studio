package it.unipv.dao;

import it.unipv.db.DBConnection;
import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Questa classe fa riferimento alla tabella USER
 * Si occupa di inserire/recuperare/aggiornare/eliminare i dati riguardanti gli utenti.
 */
public class UserDaoImpl implements UserDao {
    private DBConnection dbConnection;

    public UserDaoImpl(DBConnection dbConnection) { this.dbConnection = dbConnection; }

    /**
     * Recupera la lista degli utenti dal database.
     * @return -> lista degli utenti.
     */
    @Override public List<User> retrieveUserList() { return doRetrieveUserList(); }

    /**
     * Permette l'inserimento di un nuovo utente sul database.
     * @param toInsert -> nuovo utente da inserire.
     */
    @Override public void insertNewUser(User toInsert) { doInsertNewUser(toInsert); }

    /**
     * Permette di eliminare un utente dal database.
     * @param toDelete -> l'utente da eliminare.
     */
    @Override public void deleteUser(User toDelete) { doDeleteUser(toDelete); }

    /**
     * Permette di aggiornare solamente la password dell'utente.
     * @param toUpdate -> l'utente di cui si vuole aggiornare la password.
     */
    @Override public void updateUser(User toUpdate) { doUpdateUser(toUpdate); }

    private void doUpdateUser(User toUpdate) {
        String query = "UPDATE " + DataReferences.DBNAME + ".USER SET PASSWORD = ? WHERE CODICE = ?";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toUpdate.getPassword());
            ps.setString(2, toUpdate.getCodice());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doDeleteUser(User toDelete) {
        String query = "DELETE FROM " + DataReferences.DBNAME + ".USER where CODICE = ?";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toDelete.getCodice());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doInsertNewUser(User toInsert) {
        String query = "INSERT INTO " + DataReferences.DBNAME + ".USER (CODICE, NOME, PASSWORD, EMAIL) values (?,?,?,?)";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, toInsert.getCodice());
            ps.setString(2, toInsert.getNome());
            ps.setString(3, toInsert.getPassword());
            ps.setString(4, toInsert.getEmail());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<User> doRetrieveUserList() {
        try {
            return getUsersFromResultSet(dbConnection.getResultFromQuery("SELECT * FROM " + DataReferences.DBNAME + ".USER"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private List<User> getUsersFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            List<User> res = new ArrayList<>();
            while(resultSet.next()) {
                res.add(new User( resultSet.getString("NOME")
                                , resultSet.getString("PASSWORD")
                                , resultSet.getString("EMAIL")
                                , resultSet.getString("CODICE")));
            }
            return res;
        } finally {
            resultSet.close();
        }
    }
}
