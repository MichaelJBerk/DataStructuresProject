package edu.yu.cs.com1320.project;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.junit.*;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage1.impl.DocumentStoreImpl;

public class AppTest {

    URI fileUri;
    String sampleString = "Hello, World! This is just a test for Stage 1!";
    String pdfString = "Hello, World! I've found myself in a PDF! How cool is that?";
    URI uriForTXT;
    URI uriForPDF;
    InputStream stringInputStream;

    HashTableImpl<String, String> hashTable;
    DocumentStoreImpl ds;
    // URI fileURI;


    @Before
    public void htSetup() {
        hashTable = new HashTableImpl<String, String>(5);
        hashTable.put("Shalom", "Hello");
        hashTable.put("Computer", "Science");
    }

    @Before
    public void docStoreSetup() {
        ds = new DocumentStoreImpl();
        try {
            uriForTXT = new URI("mibe://cleverStringGoesHere");
            uriForPDF = new URI("file:notUsingAdobeReaderAndProudOfIt");
            stringInputStream = new ByteArrayInputStream(sampleString.getBytes());
        } catch (URISyntaxException e) {
            System.out.println(e);
        }
    }

    @After
    public void cleanup() {
        ds = null;
        hashTable = null;
    }

    @Test 
    public void testHT() {
        hashTable.get("Shalom");
        
    }

    @Test
    public void testSeparateChaining() {
        assertEquals("Science", hashTable.get("Computer"));
        hashTable.put("Computer", "Stuff");
        assertEquals("Stuff", hashTable.get("Computer"));
        hashTable.put("Computer", null);
        assertEquals(null, hashTable.get("Computer"));
    }
  
    

    public void putTXT() {
        ds.putDocument(stringInputStream, uriForTXT, DocumentFormat.TXT);
    }

    @Test
    public void testGetTXT() throws FileNotFoundException {
        putTXT();
        String s = ds.getDocumentAsTxt(uriForTXT);
        assertEquals(sampleString, s);
    }

    @Test
    public void uriAlreadyExists() {
        // putTXT();
        ds.putDocument(stringInputStream, uriForTXT, DocumentFormat.TXT);
        String newString = "a new string";
        InputStream is = new ByteArrayInputStream(newString.getBytes());
        ds.putDocument(is, uriForTXT, DocumentFormat.TXT);
        String docString = ds.getDocumentAsTxt(uriForTXT);
        assertEquals(newString, docString);
    }

    @Test 
    public void valueAlreadyExists() {
        ds.putDocument(stringInputStream, uriForPDF, DocumentFormat.TXT);
        try {
            URI newURI = new URI("mibe://newURI");
            ds.putDocument(stringInputStream, newURI, DocumentFormat.TXT);
        } catch (Exception e) {
            System.out.print(e);
        }     
    }

    @Test
    public void testRemoveTXT() throws FileNotFoundException {
        ds.putDocument(null, uriForTXT, DocumentFormat.TXT);
        String s = ds.getDocumentAsTxt(uriForTXT);
        assertEquals(s, null);
    }

    //No real way to test this, other than it not crashing.
    @Test
    public void testDupeTXT() throws FileNotFoundException {
        putTXT();
        putTXT();
    }

    // @Test
    public byte[] createGenericPDFFile() {
        try {
            PDDocument pdf = new PDDocument();
            PDPage page = new PDPage();
            PDFont font = PDType1Font.HELVETICA;
            pdf.addPage(page);
            PDPageContentStream content = new PDPageContentStream(pdf, page);
            content.beginText();
            content.setFont(font, 12);
            content.showText(pdfString);
            content.endText();
            content.close();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            pdf.save(os);
            byte[] ba = os.toByteArray();
            return ba;

        } catch (Exception e) {
            System.out.print(e);
            return null;
        }
    }

    public void putPDF() {
        byte[] PDFBytes = createGenericPDFFile();
        InputStream is = new ByteArrayInputStream(PDFBytes);
        ds.putDocument(is, uriForPDF, DocumentFormat.PDF);

    }

    public void getPDF() {
        String pdfText = ds.getDocumentAsTxt(uriForPDF);
        String pText = pdfText.replaceAll("\\P{Print}","");
        String pString = pdfString.replaceAll("\\P{Print}","");
        assertEquals(true, pText.equals(pString));
    }

    @Test
    public void testGetPDF() {
        putPDF();
        getPDF();
    }
    

    public void savePDFFromDoc(URI uri) throws FileNotFoundException {
        String docString = ds.getDocumentAsTxt(uri);
        docString = docString.replace("\n", "").replace("\r", "");
        try {
            PDDocument pdf = new PDDocument();
            PDPage page = new PDPage();
            pdf.addPage(page);

            PDPageContentStream content = new PDPageContentStream(pdf, page);
            content.beginText();
            PDFont font = PDType1Font.HELVETICA;
            content.setFont(font, 12);
            content.newLine();
            content.showText(docString);
            content.endText();
            File fileToSave = new File("myPDF2.pdf");
            content.close();
            pdf.save(fileToSave);


        } catch (Exception e) {
            System.out.print(e);
        }
    }

}
