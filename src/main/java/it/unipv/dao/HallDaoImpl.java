package it.unipv.dao;

import it.unipv.db.DBConnection;
import it.unipv.model.Seat;
import it.unipv.model.SeatTYPE;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.CloseableUtils;
import it.unipv.utils.DataReferences;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Questa classe fa riferimento alle tabelle MAPS e MAPPREVIEWS
 * Si occupa di inserire/recuperare/aggiornare/eliminare i dati riguardanti le sale, come i posti a sedere (Seat) e le immagini di anteprima.
 */
public class HallDaoImpl implements HallDao {

    private DBConnection dbConnection;

    public HallDaoImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Ricava i posti di una sala a partire dal nome della stessa.
     * @param hallName -> il nome della sala di cui trovare i posti.
     * @return -> la lista dei posti correttamente inizializzata.
     */
    @Override
    public List<Seat> retrieveSeats(String hallName) {
        try {
            return retrieveSeatsFromResultSet(dbConnection.getResultFromQuery("select * from " + DataReferences.DBNAME + ".MAPS where NOME_SALA = '" + hallName + "';"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Ricava i nomi delle sale disponibili a sistema.
     * @return -> lista di stringhe rappresentante i nomi delle sale correttamente inizializzate.
     */
    @Override
    public List<String> retrieveHallNames() {
        try {
            return retrieveHallNamesFromResultSet(dbConnection.getResultFromQuery("select distinct NOME_SALA from " + DataReferences.DBNAME + ".MAPS"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Recupera la preview delle sale come Image con dimensioni passate come parametri.
     * @param hallName -> la sala di cui si vuole recuperare l'anteprima;
     * @param requestedWidth -> la larghezza che vogliamo dare alla nostra Image;
     * @param requestedHeight -> l'altezza che voglia dare alla nostra Image;
     * @param preserveRatio -> decidere se mantenere l'aspect ratio o meno
     * @param smooth -> indica in generale se applicare un algoritmo di miglioramento dell'Image finale;
     * @return -> ritorna la preview della sala come Image con i parametri da noi specificati
     */
    @Override
    public Image retrieveHallPreviewAsImage(String hallName, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        try {
            return getHallPreviewFromResultSetAsImage( dbConnection.getResultFromQuery("select PREVIEW from " + DataReferences.DBNAME + ".MAPPREVIEWS where NOME_SALA = '" + hallName + "';")
                                                     , requestedWidth
                                                     , requestedHeight
                                                     , preserveRatio
                                                     , smooth);
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Recupera la preview delle sale come stream: rispetto l'Image, lo stream rappresenta l'immagine "vera e propria".
     * @param hallName -> la sala di cui si vuole recuperare la preview come stream.
     * @return -> ritorna la preview della sala come InputStream.
     */
    @Override
    public InputStream retrieveHallPreviewAsStream(String hallName) {
        try {
            return retrieveHallPreviewFromResultSetAsStream(dbConnection.getResultFromQuery("select PREVIEW from " + DataReferences.DBNAME + ".MAPPREVIEWS where NOME_SALA = '" + hallName + "';"));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private InputStream retrieveHallPreviewFromResultSetAsStream(ResultSet resultSet) throws SQLException {
        try {
            Blob blob = null;
            while(resultSet.next()) {
                blob = resultSet.getBlob("PREVIEW");
            }
            return blob.getBinaryStream(1, Objects.requireNonNull(blob).length());
        } finally {
            resultSet.close();
        }
    }

    /**
     * Cancella tutta la sala e la ricrea: non è un vero e proprio update:
     * possono capitare volte in cui modifico posti già esistenti, ma anche volte in cui inserisco nuovi posti
     * @param hallName -> sala da aggiornare
     * @param toUpdate -> lista posti aggiornati da inserire nuovamente
     */
    @Override
    public void updateHallSeats(String hallName, List<Seat> toUpdate) {
        try {
            doRemoveSeats(hallName);
            doInsertSeats(hallName, toUpdate);
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Aggiorna l'anteprima della sala.
     * @param hallName -> nome della sala di cui si deve aggiornare l'anteprima;
     * @param previewStream -> stream della preview da caricare sul database.
     */
    @Override
    public void updateHallPreview(String hallName, ByteArrayInputStream previewStream) {
        try {
            doUpdateHallPreview(hallName, previewStream);
        } finally {
            CloseableUtils.close(previewStream);
        }
    }

    /**
     * Rimuove sia l'anteprima della sala, sia le informazioni riguardanti i posti della sala.
     * @param hallName -> nome della sala di cui si deve rimuovere ogni informazione.
     */
    @Override
    public void removeHallAndPreview(String hallName) {
        doRemoveSeats(hallName);
        doRemovePreview(hallName);
    }

    /**
     * Aggiunge tutte le informazioni della nuova sala sul database.
     * @param hallName -> nome della nuova sala;
     * @param toInsert -> lista dei posti a sedere della nuova sala.
     */
    @Override
    public void insertNewHall(String hallName, List<Seat> toInsert) {
        try{
            doInsertSeats(hallName, toInsert);
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Aggiunge la preview di una nuova sala sul database.
     * @param hallName -> nome della nuova sala;
     * @param previewStream -> stream della preview della nuova sala.
     */
    @Override
    public void insertNewHallpreview(String hallName, ByteArrayInputStream previewStream) {
        try {
            doInsertHallPreview(hallName, previewStream);
        } finally {
            CloseableUtils.close(previewStream);
        }
    }

    /**
     * Permette di aggiornare il nome della sala sul database.
     * @param oldHallName -> vecchio nome della sala sul quale basarci per ritrovare le informazioni;
     * @param newHallName -> nuovo nome della sala.
     */
    @Override
    public void renameHallAndPreview(String oldHallName, String newHallName) {
        doRenameHall(oldHallName, newHallName);
        doRenamePreview(oldHallName, newHallName);
    }

    private void doRenameHall(String oldHallName, String newHallName) {
        String query = "UPDATE " + DataReferences.DBNAME + ".MAPS SET NOME_SALA = ? WHERE NOME_SALA = '" + oldHallName + "';";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, newHallName);
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doRenamePreview(String oldHallName, String newHallName) {
        String query = "UPDATE " + DataReferences.DBNAME + ".MAPPREVIEWS SET NOME_SALA = ? WHERE NOME_SALA = '" + oldHallName + "';";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, newHallName);
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doInsertHallPreview(String hallName, ByteArrayInputStream previewStream) {
        String query = "insert into " + DataReferences.DBNAME + ".MAPPREVIEWS (NOME_SALA, PREVIEW) values (?,?)";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setString(1, hallName);
            ps.setBinaryStream(2, previewStream, previewStream.available());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doRemovePreview(String hallName) {
        String query = "delete from "+ DataReferences.DBNAME + ".MAPPREVIEWS where NOME_SALA = '" + hallName + "';";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }

    }

    private void doUpdateHallPreview(String hallName, ByteArrayInputStream previewStream) {
        String query = "UPDATE " + DataReferences.DBNAME + ".MAPPREVIEWS SET PREVIEW = ? WHERE NOME_SALA = '" + hallName + "';";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.setBinaryStream(1, previewStream, previewStream.available());
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private void doInsertSeats(String hallName, List<Seat> toUpdate) throws SQLException {
        PreparedStatement ps = null;
        try {
            String query = "INSERT INTO " + DataReferences.DBNAME + ".MAPS(NOME_SALA,NOME_POSTO,COORD_X, COORD_Y, TIPO_POSTO) values (?,?,?,?,?)";
            for(Seat s : toUpdate) {
                ps = dbConnection.getPreparedStatementFromQuery(query);
                ps.setString(1, hallName);
                ps.setString(2, s.getText());
                ps.setInt(3, s.getX());
                ps.setInt(4, s.getY());

                switch(s.getType()) {
                    case NORMALE:
                        ps.setString(5, "NORMALE");
                        break;

                    case DISABILE:
                        ps.setString(5, "DISABILE");
                        break;

                    case VIP:
                        ps.setString(5, "VIP");
                        break;

                    case OCCUPATO:
                        ps.setString(5, "OCCUPATO");
                        break;

                    default:
                        throw new ApplicationException("Tipo " + s.getType() + " non riconosciuto!");
                }

                ps.execute();
            }
        } catch (SQLException e) {
            throw new ApplicationException(e);
        } finally {
            if(ps!=null) { ps.close(); }
        }
    }

    private void doRemoveSeats(String hallName) {
        String query = "delete from "+ DataReferences.DBNAME + ".MAPS where NOME_SALA = '" + hallName + "';";
        try (PreparedStatement ps = dbConnection.getPreparedStatementFromQuery(query)) {
            ps.execute();
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private Image getHallPreviewFromResultSetAsImage(ResultSet resultSet, double requestedWidth, double requestedHeight, boolean preserveRation, boolean smooth) throws SQLException{
        try {
            Blob blob = null;
            while(resultSet.next()) {
                blob = resultSet.getBlob("PREVIEW");
            }
            InputStream in = blob.getBinaryStream(1, Objects.requireNonNull(blob).length());
            Image result = new Image(in, requestedWidth, requestedHeight, preserveRation, smooth);
            CloseableUtils.close(in);
            return result;
        } finally {
            resultSet.close();
        }

    }

    private List<String> retrieveHallNamesFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            List<String> res = new ArrayList<>();
            while(resultSet.next()) {
                res.add(resultSet.getString("NOME_SALA"));
            }
            return res;
        } finally {
            resultSet.close();
        }
    }

    private List<Seat> retrieveSeatsFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            List<Seat> res = new ArrayList<>();
            while(resultSet.next()) {
                Seat s;
                switch (resultSet.getString("TIPO_POSTO")) {
                    case "NORMALE":
                        s = new Seat(resultSet.getInt("COORD_X"), resultSet.getInt("COORD_Y"), SeatTYPE.NORMALE);
                        break;

                    case "VIP":
                        s = new Seat(resultSet.getInt("COORD_X"), resultSet.getInt("COORD_Y"), SeatTYPE.VIP);
                        break;

                    case "DISABILE":
                        s = new Seat(resultSet.getInt("COORD_X"), resultSet.getInt("COORD_Y"), SeatTYPE.DISABILE);
                        break;

                    case "OCCUPATO":
                        s = new Seat(resultSet.getInt("COORD_X"), resultSet.getInt("COORD_Y"), SeatTYPE.OCCUPATO);
                        break;

                    default:
                        throw new ApplicationException("Tipo " + resultSet.getString("TIPO_POSTO") + " non riconosciuto!");
                }
                s.setText(resultSet.getString("NOME_POSTO"));
                res.add(s);
            }
            return res;
        } finally {
            resultSet.close();
        }

    }
}
