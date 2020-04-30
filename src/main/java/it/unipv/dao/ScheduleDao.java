package it.unipv.dao;

import it.unipv.model.Schedule;

import java.util.List;

public interface ScheduleDao {
    List<Schedule> retrieveMovieSchedules();
    void insertNewMovieSchedule(Schedule toInsert);
    void deleteMovieSchedule(Schedule toDelete);
}
