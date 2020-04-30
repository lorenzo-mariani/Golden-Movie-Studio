package it.unipv.conversion;

import it.unipv.model.User;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.CloseableUtils;
import it.unipv.utils.DataReferences;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilizzata per diversi scopi:
 *    1) salvare all'interno di un file .txt le informazioni riguardanti un utente di cui si è deciso di ricordarsi i dati
 *    2) controllare se già esiste un file di informazioni salvate
 *    3) eliminare, se già esistente, un file di informazioni salvate
 */
public class UserInfo {

    /**
     * Metodo che si occupa di salvare all'interno di data/utenti il file di info; se le cartelle non esistono, vengono create.
     * @param username -> nickname dell'utente
     * @param password -> password dell'utente
     * @param email -> email dell'utente
     * @param codice -> codice dell'utente
     * @param infoUserDir -> percorso directory utente da creare
     * @param  infoUserFile -> percorso file utente da creare
     */
    public static void createUserInfoFileInUserDir( String username
                                                  , String password
                                                  , String email
                                                  , String codice
                                                  , String infoUserDir
                                                  , String infoUserFile) {
        PrintWriter writer = null;
        try {
            File dir = new File(infoUserDir);
            if(!dir.exists()) {
                Files.createDirectories(Paths.get(infoUserDir));
            }
            writer = new PrintWriter(infoUserFile, "UTF-8");
            writer.println(username);
            writer.println(password);
            writer.println(email);
            writer.println(codice);
        } catch (FileNotFoundException e) {
            throw new ApplicationException("File " + infoUserFile + " non trovato!", e);
        } catch (IOException e) {
            throw new ApplicationException(e);
        } finally {
            CloseableUtils.close(writer);
        }
    }

    /**
     * Metodo utilizzato per rimuovere il file di informazioni salvate, se esiste.
     * È utilizzato quando si fa il logout: a questo punto non serve più ricordarsi dell'utente.
     * @param infoUserFilePath -> percorso file utente da rimuovere
     */
    public static void deleteUserInfoFileInUserDir(String infoUserFilePath) {
        File info = new File(infoUserFilePath);
        if(info.exists()) {
            if(info.delete()) {
                System.out.println("File " + info.getPath() + " rimosso con successo!");
            } else {
                throw new ApplicationException("Errore durante la rimozione del file " + info.getPath());
            }
        }
    }

    /**
     * Metodo utilizzato per verificare l'esistenza del file di info e, quindi, l'esistenza di un utente da ricordarsi.
     * @param infoUserFilePath -> percorso file di cui controllarne l'esistenza
     * @return -> true se esiste, false altrimenti
     */
    public static boolean checkIfUserInfoFileExists(String infoUserFilePath) {
        return new File(infoUserFilePath).exists();
    }


    /**
     * Il metodo ritorna un utente istanziato con le informazioni salvate nel file delle info, se esistente.
     * @param infoUserFilePath -> percorso del file utente da cui prendere le informazioni
     * @return -> ritorna l'utente instanziato se il file .txt di info esiste
     */
    public static User getUserInfo(String infoUserFilePath) {
        if(checkIfUserInfoFileExists(infoUserFilePath)) {
            return getUserInfoFromFile(infoUserFilePath);
        } else {
            throw new ApplicationException("Impossibile trovare info user perché non esiste il file delle info!");
        }
    }

    private static User getUserInfoFromFile(String infoUserFilePath) {
        BufferedReader br = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            User res = new User();
            fis = new FileInputStream(infoUserFilePath);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            List<String> infoFromFile = new ArrayList<>();
            String line = br.readLine();

            while (line != null) {
                infoFromFile.add(line.trim());
                line = br.readLine();
            }

            if(infoFromFile.size()!=4) {
                throw new ApplicationException("Impossibile, formato file inatteso!");
            } else {
                res.setNome(infoFromFile.get(0));
                res.setPassword(infoFromFile.get(1));
                res.setEmail(infoFromFile.get(2));
                res.setCodice(infoFromFile.get(3));
            }

            return res;
        } catch (IOException e) {
            throw new ApplicationException(e);
        } finally {
            CloseableUtils.close(br, isr, fis);
        }
    }
}
