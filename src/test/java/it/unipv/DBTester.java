package it.unipv;

import it.unipv.db.DBConnection;
import it.unipv.utils.DataReferences;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class DBTester extends TestCase {

    private DBConnection dbConnection;

    @Before
    public void before() {
        dbConnection = new DBConnection();
    }

    @After
    public void after() {
        dbConnection.close();
    }

    @Test
    public void testQuery() throws SQLException {
        List<String> result = new ArrayList<>();

        try (ResultSet resultSet = dbConnection.getResultFromQuery("select distinct NOME_SALA from " + DataReferences.DBNAME + ".MAPS")) {
            while (resultSet.next()) {
                result.add(resultSet.getString("NOME_SALA"));
            }
        }

        for(String s : result) {
            System.out.println(s);
        }

        assertTrue(result.size()>0);
    }

    @Test
    public void checkConnection() throws SQLException {
        assertFalse(dbConnection.getConnection().isClosed());

        dbConnection.close();
        assertTrue(dbConnection.getConnection().isClosed());
    }
}
