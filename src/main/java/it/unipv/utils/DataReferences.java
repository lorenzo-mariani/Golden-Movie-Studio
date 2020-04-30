package it.unipv.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DataReferences {

    public final static String INFOUSERDIR = "data" + File.separator + "utenti";
    public final static String INFOUSERFILE = "data" + File.separator + "utenti" + File.separator + "info.txt";
    public final static int PAUSEAFTERMOVIE = 30; //min

    public final static String ADMINUSERNAME = "Admin";
    public final static String ADMINPASSWORD = "Admin";

    public final static int MYDRAGGABLESEATWIDTH = 30;
    public final static int MYDRAGGABLESEATHEIGTH = 25;
    public final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public final static String DBNAME = "z6xOH9WKhI";
    public final static String DBPASS = "NSpPIYAmt3";

    public final static List<String> HOMETIPS = Arrays.asList( "Benvenuto in Golden Movie Studio!"
                                                             , "Hai già visto le nuove uscite?"
                                                             , "Non riesci ad accedere? Prova a resettare la password!"
                                                             , "Vorresti contattarci? Scopri come nella pagina delle informazioni!"
                                                             , "Ricordati di non perdere il codice utente: potrebbe servirti per resettare la password!"
                                                             , "Ricordati che devi aver effettuato l'accesso per poter prenotare una data!");

    public final static List<String> USERRESERVEDAREATIPS = Arrays.asList( "Benvenuto nella tua area riservata!"
                                                                         , "Nella schermata Home potrai trovare i tuoi dati, tra cui il codice utente!"
                                                                         , "Nelle prenotazioni potrai trovare la fattura da stampare e portare in reception!"
                                                                         , "Nei film visti potrai vedere tutto ciò che hai visto e le relative fatture!"
                                                                         , "Nei suggerimenti potrai trovare tutti i film adatti a te attualmente in programmazione!"
                                                                         , "Ricordati che per visualizzare i suggerimenti devi aver visto almeno un film!");

    public final static List<String> MANAGERAREATIPS = Arrays.asList( "Benvenuto nell'area Manager!"
                                                                    , "In Modifica Sale potrai aggiungere nuove sale o modificare/rimuovere quelle già esistenti!"
                                                                    , "In Programmazione potrai aggiungere film o modificare/rimuovere quelli già in programmazione!"
                                                                    , "In Programmazione puoi impostare la visibilità di un film!"
                                                                    , "In Programmazione puoi creare le programmazioni per i singoli film!"
                                                                    , "In Lista Film puoi settare programmabile film che attualmente non lo sono!"
                                                                    , "In Lista Film puoi modificare/rimuovere tutti i film attualmente caricati!"
                                                                    , "In Lista Utenti puoi modificare la password di un utente o addirittura eliminarlo!"
                                                                    , "In Modifica Prezzi potrai modificare la politica prezzi del cinema!");
}
