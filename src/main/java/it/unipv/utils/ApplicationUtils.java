package it.unipv.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApplicationUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static boolean checkIfDateIsInThePast(String toCheck){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dateToCheck;
        try {
            dateToCheck = sdf.parse(toCheck);
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
        return dateToCheck.before(new Date());
    }

    /** Metodo che formatta la data nel formato dd/MM/yyyy italiano. */
    public static String formatDate(String toFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
            return sdf1.format(sdf.parse(toFormat));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
    }

    /** Metodo che formatta l'ora nel formato HH:mm. */
    public static String formatTime(String toFormat) {
        String[] time = toFormat.split(":");
        if(time.length>2) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
                return sdf1.format(sdf.parse(toFormat));
            } catch (ParseException e) {
                throw new ApplicationException(e);
            }
        } else {
            return toFormat;
        }
    }

    /**
     * Metodo che prende in input una lista di stringhe, i quali campi sono separati da *regex*
     *     e che ritorna una lista di stringhe contenente il singolo campo.
     * Esempio:
     *     Input -> A1-B1-D1 A2-B2-D2
     *     Output -> A1 B1 D1 A2 B2 D2 (in singole stringhe)
     * @param toSplit -> lista di stringhe i quali campi sono separati da *regex*
     * @param regex -> il separatore dei campi, può essere qualsiasi cosa, come - o ,
     * @return -> lista di stringhe contenente il singolo campo
     */
    public static List<String> splitter(List<String> toSplit, String regex) {
        List<String> res = new ArrayList<>();
        for(String s : toSplit) {
            String[] supp = s.split(regex);
            if(supp.length>0) {
                for(String x : supp) {
                    res.add(x.trim());
                }
            } else {
                res.add(s.trim());
            }
        }
        return res;
    }

    /**
     * Metodo che ritorna un codice univoco di maxChar a partire da una lista di chars
     * @param maxChar -> numero massimo di caratteri del codice
     * @param chars -> i caratteri che possono essere utilizzati
     * @return -> codice casuale di maxChar
     */
    public static String getRandomCode(int maxChar, String chars) {
        return RandomStringUtils.random(maxChar, chars).toUpperCase();
    }

    /**
     * Metodo che ritorna una lista di *maxOfWords* parole ricorrenti in una lista di *words*
     * @param maxOfWords -> numero massimo di elementi da inserire in lista
     * @param words -> lista di stringhe di cui trovare le *maxOfWords* parole più ricorrenti
     * @return -> la lista delle *maxOfWords* parole più ricorrenti nella lista di *words*
     */
    public static List<String> getListOfMostRepeatedWordsInList(int maxOfWords, List<String> words) {
        List<String> res = new ArrayList<>();
        Map.Entry<String, Integer> mostRepeated;
        for(int i=0; i<maxOfWords; i++) {
            mostRepeated = getMostRepeatedWordInList(words);
            for(int j=0; j<mostRepeated.getValue(); j++) {
                words.remove(mostRepeated.getKey());
            }
            res.add(mostRepeated.getKey());
        }

        Collections.sort(res);
        return res;
    }


    //Metodo che ricava la parola più ricorrente in una lista di stringhe
    private static Map.Entry<String, Integer> getMostRepeatedWordInList(List<String> words) {
        Map<String, Integer> stringsCount = new HashMap<>();

        for(String string: words) {
            Integer count = stringsCount.get(string);
            if(count == null)  { count = 0; }
            count++;
            stringsCount.put(string,count);
        }

        Map.Entry<String,Integer> mostRepeated = null;
        for(Map.Entry<String, Integer> e: stringsCount.entrySet()) {
            if(mostRepeated == null || mostRepeated.getValue()<e.getValue())
                mostRepeated = e;
        }

        return mostRepeated;
    }
}
