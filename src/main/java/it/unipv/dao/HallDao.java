package it.unipv.dao;

import it.unipv.model.Seat;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface HallDao {
    List<Seat> retrieveSeats(String hallName);
    List<String> retrieveHallNames();
    Image retrieveHallPreviewAsImage(String hallName, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth);
    InputStream retrieveHallPreviewAsStream(String hallName);
    void updateHallSeats(String hallName, List<Seat> toUpdate);
    void updateHallPreview(String hallName, ByteArrayInputStream previewStream);
    void removeHallAndPreview(String hallName);
    void insertNewHall(String hallName, List<Seat> toInsert);
    void insertNewHallpreview(String hallName, ByteArrayInputStream previewStream);
    void renameHallAndPreview(String oldHallName, String newHallName);
}
