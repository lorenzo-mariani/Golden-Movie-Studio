package it.unipv;

import it.unipv.conversion.UserInfo;
import it.unipv.model.User;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

@RunWith(JUnit4.class)
public class InfoUserTester extends TestCase {

    //Rule permette di creare la cartella temporanea prima dell'esecuzione del metodo e di cancellarla alla fine dell'esecuzione.
    @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

    private User user = new User( "Antani"
                                , "ComeSeFosseAntani"
                                , "antani@gmail.com"
                                , "x1y3z");

    @Test
    public void testIfItIsTrulyCreatingUserInfoFile() {
        File tmpFile = getTmpFile();
        assertFalse(tmpFile.exists());

        createUser();

        assertTrue(tmpFile.exists());
    }

    @Test
    public void testIfItActuallyDeleteInfoFile() {
        File tmpFile = getTmpFile();

        createUser();
        assertTrue(tmpFile.exists());

        UserInfo.deleteUserInfoFileInUserDir(tmpFile.getPath());

        assertFalse(tmpFile.exists());
    }

    @Test
    public void testIfItActuallyCanGetInfoFromFile() {
        File tmpFile = getTmpFile();

        createUser();
        assertTrue(tmpFile.exists());

        User x = UserInfo.getUserInfo(tmpFile.getPath());

        assertEquals(user.getNome(), x.getNome());
        assertEquals(user.getPassword(), x.getPassword());
        assertEquals(user.getEmail(), x.getEmail());
        assertEquals(user.getCodice(), x.getCodice());
    }


    private void createUser() {
        UserInfo.createUserInfoFileInUserDir( user.getNome()
                                            , user.getPassword()
                                            , user.getEmail()
                                            , user.getCodice()
                                            , tmpFolder.getRoot().getAbsolutePath()
                                            , getTmpFile().getPath());
    }

    private File getTmpFile() {
        return new File( tmpFolder.getRoot().getAbsolutePath()
                       + File.separator
                       + "infoUser.txt");
    }

}
