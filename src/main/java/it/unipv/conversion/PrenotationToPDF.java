package it.unipv.conversion;

import com.itextpdf.text.pdf.BaseFont;
import com.lowagie.text.DocumentException;
import it.unipv.model.Prenotation;
import it.unipv.utils.ApplicationException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.*;
import java.nio.file.FileSystems;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

/**
 * Il PDF viene generato automaticamente a partire da una Prenotation. Le librerie utilizzate sono:
 *    itextpdf -> creazione del PDF effettiva
 *    tidy -> trasformazione da html a xhtml
 *    thymeleaf -> template builder con cui si crea il template del pdf, in HTML.
 */
public class PrenotationToPDF {

    /**
     * Metodo che si occupa della creazione del PDF a partire da:
     * @param outputfilePath -> percorso di destinazione del file PDF generato
     * @param encoding -> tipologia di encoding che si preferisce utilizzare (Es.: UTF-8)
     * @param prenotation -> dati che verranno inseriti nel PDF
     * @throws IOException -> lanciata se si hanno problemi relativi al file (Es.: se cerchiamo di sovrascrivere un file aperto)
     * @throws DocumentException -> lanciata se si verificano problemi a livello di template e di creazione PDF
     */
    public static void generatePDF(String outputfilePath, String encoding, Prenotation prenotation) throws IOException, DocumentException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setCharacterEncoding(encoding);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("prenotation", prenotation);

        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont("font/BebasNeueRegular.ttf", encoding, BaseFont.EMBEDDED);

        String baseUrl = FileSystems
                .getDefault()
                .getPath("src", "main", "resources", "images", "font")
                .toUri()
                .toURL()
                .toString();
        renderer.setDocumentFromString(convertHTMLToXHTML(templateEngine.process("template/template", context), encoding), baseUrl);
        renderer.layout();

        OutputStream outputStream = new FileOutputStream(outputfilePath);
        renderer.createPDF(outputStream);
        outputStream.close();
    }

    //Questo metodo viene utilizzato perché itextpdf si aspetta in input un xhtml e non un html
    private static String convertHTMLToXHTML(String html, String encoding) {
        try {
            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setInputEncoding(encoding);
            tidy.setOutputEncoding(encoding);
            tidy.setXHTML(true);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(encoding));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            tidy.parseDOM(inputStream, outputStream);
            return outputStream.toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new ApplicationException(e);
        }
    }
}
