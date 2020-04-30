package it.unipv.model;

import it.unipv.utils.ApplicationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Oggetto che rappresenta la prenotazione di un film da parte di un utente.
 */
public class Prenotation implements Comparable<Prenotation> {
    private String nomeUtente;
    private String nomeFilm;
    private String codiceFilm;
    private String giornoFilm;
    private String oraFilm;
    private String salaFilm;
    private String postiSelezionati;
    private String costoTotale;

    public Prenotation(String nomeUtente, String nomeFilm, String codiceFilm, String giornoFilm, String oraFilm, String salaFilm, String postiSelezionati, String costoTotale) {
        this.nomeUtente = nomeUtente;
        this.nomeFilm = nomeFilm;
        this.codiceFilm = codiceFilm;
        this.giornoFilm = giornoFilm;
        this.oraFilm = oraFilm;
        this.salaFilm = salaFilm;
        this.postiSelezionati = postiSelezionati;
        this.costoTotale = costoTotale;
    }

    public String getNomeUtente() { return nomeUtente; }
    public String getNomeFilm() { return nomeFilm; }
    public String getCodiceFilm() { return codiceFilm; }
    public String getGiornoFilm() { return giornoFilm; }
    public String getOraFilm() { return oraFilm; }
    public String getSalaFilm() { return salaFilm; }
    public String getPostiSelezionati() { return postiSelezionati; }
    public String getCostoTotale() { return costoTotale; }
    public void setCostoTotale(String costoTotale) { this.costoTotale = costoTotale; }

    @Override
    public int compareTo(Prenotation o) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        try {
            cal1.setTime(sdf.parse(this.getGiornoFilm() + " " + this.getOraFilm()));
            cal2.setTime(sdf.parse(o.getGiornoFilm() + " " + o.getOraFilm()));
        } catch (ParseException e) {
            throw new ApplicationException(e);
        }
        return cal1.compareTo(cal2);
    }
}
