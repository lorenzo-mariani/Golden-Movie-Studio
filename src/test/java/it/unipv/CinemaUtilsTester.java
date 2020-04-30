package it.unipv;

import it.unipv.model.Schedule;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class CinemaUtilsTester extends TestCase {

    @Test
    public void testIfDateIsInThePast() {
        assertTrue(ApplicationUtils.checkIfDateIsInThePast("02/04/2019"));
        assertFalse(ApplicationUtils.checkIfDateIsInThePast("20/07/2400"));
    }

    @Test
    public void testMovieScheduleDateAndTimeComparator() {
        Schedule m1 = new Schedule();
        Schedule m2 = new Schedule();
        m1.setDate("03/04/2019");
        m1.setTime("22:00");
        m2.setDate("03/04/2019");
        m2.setTime("21:00");
        assertEquals(1, m1.compareTo(m2));
        assertEquals(-1, m2.compareTo(m1));
    }

    @Test
    public void testIfDateIsBeenFormatted() {
        assertEquals("30/04/2019", ApplicationUtils.formatDate("2019-04-30"));
    }

    @Test
    public void testIfHoursAreBeenFormatted() {
        assertEquals("22:30", ApplicationUtils.formatTime("22:30:23.11"));
    }

    @Test
    public void testIfSeatsAreActuallyBeenSplittedByMinus() {
        //Aggiungo 13 posti a caso
        List<String> listaPostiOccupati = new ArrayList<>();
        listaPostiOccupati.add("A1-A2-A3");
        listaPostiOccupati.add("B4-B8");
        listaPostiOccupati.add("B9-C2-C3");
        listaPostiOccupati.add("D1-D2-E3-E4");
        listaPostiOccupati.add("Z1");

        List<String> res = ApplicationUtils.splitter(listaPostiOccupati, "-");

        for(String s : res) { System.out.println(s); }

        //Controllo se sono 13 posti realmente
        assertEquals(13, res.size());
    }

    @Test
    public void testIfGenresAreActuallyBeenSplittedByComma() {
        //Aggiungo 6 generi a caso separati da ,
        List<String> listaGeneri = new ArrayList<>();
        listaGeneri.add("Azione, Commedia");
        listaGeneri.add("Anime, Fantascienza, Horror");
        listaGeneri.add("Drammatico");

        List<String> res = ApplicationUtils.splitter(listaGeneri, ",");

        for(String s : res ) { System.out.println(s); }

        //Controllo se sono 6 posti realmente
        assertEquals(6, res.size());

    }

    @Test
    public void testIfUserCodesAreTrulyRandomlyGenerated() {
        String codice1 = ApplicationUtils.getRandomCode(5, "0123456789abcdefghijklmnopqrstuvzxy");
        String codice2 = ApplicationUtils.getRandomCode(5, "0123456789abcdefghijklmnopqrstuvzxy");

        System.out.println(codice1 + "\n" + codice2);

        assertNotEquals(codice1, codice2);
    }

    //Metodo utilizzato per trovare i generi più visti dall'utente e dare così dei suggerimenti
    @Test
    public void testIfTheyAreTrulyTheMostRepeatedWordsInList() {
        List<String> words = new LinkedList<>(Arrays.asList( "Azione"
                                                           , "Commedia"
                                                           , "Azione"
                                                           , "Dramma"
                                                           , "Commedia"
                                                           , "Anime"
                                                           , "Dramma"
                                                           , "Fantascienza"));

        assertThat( ApplicationUtils.getListOfMostRepeatedWordsInList(3, words) //actual
                  , is(new LinkedList<>(Arrays.asList("Azione", "Commedia", "Dramma"))));   //expected
    }

    @Test
    public void testIfICanAddThisSchedule() {
        assertTrue(checkIfICanAddThisSchedule("17/05/2019 17:00", 60, 30, "17/05/2019 15:29", 60));
        assertTrue(checkIfICanAddThisSchedule("17/05/2019 17:00", 60, 30, "17/05/2019 18:31", 60));
        assertFalse(checkIfICanAddThisSchedule("17/05/2019 17:00", 60, 30, "17/05/2019 17:01", 60));
        assertFalse(checkIfICanAddThisSchedule("17/05/2019 17:00", 60, 30, "17/05/2019 16:59", 60));
    }

    private boolean checkIfICanAddThisSchedule(String existingScheduleDate, int existingMovieDuration, int pause, String incomingScheduleDate, int incomingMovieDuration) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar realIncomingScheduleDate = Calendar.getInstance();
        try {
            realIncomingScheduleDate.setTime(sdf.parse(incomingScheduleDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return realIncomingScheduleDate.before(getTimeOccupiedBySchedule(existingScheduleDate, incomingMovieDuration, pause, false))
                || realIncomingScheduleDate.after(getTimeOccupiedBySchedule(existingScheduleDate, existingMovieDuration, pause, true));
    }

    private Calendar getTimeOccupiedBySchedule(String existingScheduleDate, int movieDuration, int pause, boolean isItToAdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar result = Calendar.getInstance();
        try {
            result.setTime(sdf.parse((existingScheduleDate)));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
        if(isItToAdd) { result.add(Calendar.MINUTE, movieDuration+pause); }
        if(!isItToAdd) { result.add(Calendar.MINUTE, -(movieDuration+pause)); }
        return result;
    }
}
