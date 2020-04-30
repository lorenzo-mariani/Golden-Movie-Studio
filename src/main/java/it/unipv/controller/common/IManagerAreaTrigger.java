package it.unipv.controller.common;

public interface IManagerAreaTrigger {
    void triggerToHomeNewMovieEvent();
    void triggerToHomeNewHallEvent();
    void triggerStartStatusEvent(String text);
    void triggerEndStatusEvent(String text);
}
