package it.unipv.controller.common;

import it.unipv.model.Movie;
import it.unipv.model.User;

public interface IHomeTrigger {
    void triggerNewLogin(User user);
    void triggerNewMovieEvent();
    void triggerNewHallEvent();
    void triggerMovieClicked(Movie movie);
    void triggerOpenProgrammationPanel();
    void triggerOpenReservedArea();
    void triggerStartStatusEvent(String text);
    void triggerEndStatusEvent(String text);
}
