package it.unipv;

import com.lowagie.text.DocumentException;
import it.unipv.conversion.PrenotationToPDF;
import it.unipv.model.Prenotation;
import it.unipv.utils.ApplicationException;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

@RunWith(JUnit4.class)
public class PDFTester extends TestCase {

    //Non metto una cartella random perché è utile vedere fisicamente come viene il pdf aprendolo
    private static final String PDFPATH = "test.pdf";
    private static final String ENCODING = "windows-1252";

    @Test
    public void testIfPrenotationToPDFIsTrulyCreatingAPDF() {
        File pdf = new File(PDFPATH);
        if(pdf.exists()) {
            assertTrue(pdf.delete());
        }
        assertFalse(pdf.exists());
        createTestPDF();
        assertTrue(pdf.exists());
    }

    private void createTestPDF() {
        Prenotation prenotation = new Prenotation( "Andrea"
                                                 , "Avengers: Endgame"
                                                 , "blabla1"
                                                 , "26/05/2019"
                                                 , "11:36"
                                                 , "Sala 1"
                                                 , "D5-D6"
                                                 , "20€");
        try {
            PrenotationToPDF.generatePDF(PDFPATH, ENCODING, prenotation);
        } catch (IOException | DocumentException e) {
            throw new ApplicationException(e);
        }
    }
}
