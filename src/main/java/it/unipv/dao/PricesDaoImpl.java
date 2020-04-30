package it.unipv.dao;

import it.unipv.db.DBConnection;
import it.unipv.model.Prices;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Questa classe fa riferimento alla tabella PRICES
 * Si occupa di recuperare/aggiornare i dati riguardanti i prezzi del cinema.
 */
public class PricesDaoImpl implements PricesDao {
    private DBConnection dbConnection;

    public PricesDaoImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Recupera le informazioni riguardanti i prezzi del cinema.
     * @return -> l'oggetto Prices correttamente inizializzato con i prezzi del cinema.
     */
    @Override public Prices retrievePrices() { return doRetrievePrices(); }

    /**
     * Aggiorna le informazioni riguardanti i prezzi del cinema.
     * @param p -> l'oggetto Prices contenente le informazioni da aggiornare.
     */
    @Override
    public void updatePrices(Prices p) {
        doTruncate();
        doInsert(p);
    }

    private Prices doRetrievePrices() {
        try {
            return retrievePricesFromResultSet(dbConnection.getResultFromQuery("select * from " + DataReferences.DBNAME + ".PRICES"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private Prices retrievePricesFromResultSet(ResultSet resultSet) throws SQLException {
        Prices res = null;
        try {
            while(resultSet.next()) {
                res = new Prices( resultSet.getDouble("BASE")
                        , resultSet.getDouble("VIP")
                        , resultSet.getDouble("THREED")
                        , resultSet.getDouble("REDUCED"));
            }
            return res;
        } finally {
            resultSet.close();
        }
    }

    private void doInsert(Prices p){
        String query = "INSERT INTO " + DataReferences.DBNAME + ".PRICES(BASE, VIP, THREED, REDUCED) values (?,?,?,?)";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setDouble(1, p.getBase());
            ps.setDouble(2, p.getVip());
            ps.setDouble(3, p.getThreed());
            ps.setDouble(4, p.getReduced());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doTruncate() {
        String query = "TRUNCATE "+ DataReferences.DBNAME + ".PRICES";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }
}
