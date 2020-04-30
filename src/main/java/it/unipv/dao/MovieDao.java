package it.unipv.dao;

import it.unipv.model.Movie;

import java.io.FileInputStream;
import java.util.List;

public interface MovieDao {
    List<Movie> retrieveCompleteMovieList(double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth);
    List<Movie> retrieveMovieListWithoutPoster();
    void insertNewMovie(Movie toInsert, FileInputStream posterStream);
    void updateMovieButNotPoster(Movie toUpdate);
    void updateMovie(Movie toUpdate, FileInputStream posterStream);
    void deleteMovie(Movie toDelete);
}
