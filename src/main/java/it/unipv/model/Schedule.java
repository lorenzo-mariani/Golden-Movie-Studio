package it.unipv.model;

import it.unipv.utils.ApplicationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Oggetto che rappresenta la programmazione di un film
 */
public class Schedule implements Comparable<Schedule> {
    private String movieCode;
    private String date;
    private String time;
    private String hall;

    public String getMovieCode() { return movieCode; }

    public void setMovieCode(String movieCode) { this.movieCode = movieCode; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getHallName() { return hall; }

    public void setHallName(String hall) { this.hall = hall; }

    @Override
    public int compareTo(Schedule o) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        try {
            cal1.setTime(sdf.parse(this.getDate() + " " + this.getTime()));
            cal2.setTime(sdf.parse(o.getDate() + " " + o.getTime()));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
        return cal1.compareTo(cal2);
    }
}
