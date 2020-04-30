package it.unipv.db;

import it.unipv.utils.ApplicationException;
import it.unipv.utils.DataReferences;

import java.sql.*;

/**
 * Questa classe viene invocata all'apertura del programma e viene utilizzata per stabilire una connessione con
 *    il database mysql hostato su remotemysql. La connessione viene poi chiusa una volta chiuso il programma:
 *    questo perché il sito da a disposizione un numero limitato di connessioni e di richieste e, pertanto, è meglio
 *    condividere una singola connessione con tutte le classi, piuttosto che aprirla e chiuderla continuamente.
 * Infine, la logica di base del programma viene gestito tutto in locale, sempre per evitare attese infinite
 *    per eventuali richiami di select (con join) tra tabelle per il recupero di dati.
 */
public class DBConnection {
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    /**
     * Permette la connessione al database mysql hostato su remotemysql.com.
     */
    public DBConnection() { connect(); }

    private void connect(){
        try {
            connection = DriverManager.getConnection("jdbc:mysql://remotemysql.com?" + "user=" + DataReferences.DBNAME + "&password=" + DataReferences.DBPASS);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new ApplicationException("Errore durante la connessione al Database", e);
        }
    }

    /**
     * Permette la chiusura della connessione al database, se esistente.
     */
    public void close() {
        try {
            if (connection != null) { connection.close(); }
            if (statement != null) { statement.close(); }
            if (resultSet != null) { resultSet.close(); }
        } catch (SQLException e) {
            throw new ApplicationException("Errore durante la chiusura della connessione al Database", e);
        }
    }

    /**
     * Permette di eseguire una query e ritorna il ResultSet risultante.
     * @param query -> query da eseguire.
     * @return -> ResultSet risultante dalla query.
     */
    public ResultSet getResultFromQuery(String query) {
        try {
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new ApplicationException("Errore durante l'esecuzione della query", e);
        }
    }

    public PreparedStatement getPreparedStatementFromQuery(String query) {
        try{
            return connection.prepareStatement(query);
        } catch (SQLException e) {
            throw new ApplicationException("Errore durante l'esecuzione della query", e);
        }
    }

    public Connection getConnection() { return this.connection; }

}
