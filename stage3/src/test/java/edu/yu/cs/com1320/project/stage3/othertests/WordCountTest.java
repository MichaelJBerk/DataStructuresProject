package edu.yu.cs.com1320.project.stage3.othertests;

import org.junit.*;
import java.net.URI;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.List;

import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.stage3.stage3.Utils;

//Tests for new Stage 3 functionality
//This should really not be called wordCountTest anymore, but i'm too lazy to change it.
public class WordCountTest {

    String sampleString1 = "Hello, I am testing Stage 3. Stage Stage Hello is";
    String sampleString2 = "This is the second string being used to test stage 3. Stage Stage Stage.";
    String sampleString3 = "this is just a string.";
    DocumentStoreImpl ds;
    URI uri1;
    URI uri2;
    URI uri3;
    InputStream stringIS1;
    InputStream stringIS2;
    InputStream stringIS3;

    String[] strArray1;
    String[] strArray2;
    String[] strArray3;

    byte[] pdf1;
    byte[] pdf2;
    byte[] pdf3;
    InputStream pdfIS1;
    InputStream pdfIS2;
    InputStream pdfIS3;


    @Before
    public void setup() {
        ds = new DocumentStoreImpl();
        try {
            uri1 = new URI("mibe://uri1");
            uri2 = new URI("mibe://uri2");
            uri3 = new URI("mibe://uri3");
        } catch (Exception e) {
            System.out.println(e);
        }
        refreshInputStreams();

        strArray1 = sampleString1.split(" ");
        strArray2 = sampleString2.split(" ");
        strArray3 = sampleString3.split(" ");

    }

    public void refreshInputStreams() {
        stringIS1 = new ByteArrayInputStream(sampleString1.getBytes());
        stringIS2 = new ByteArrayInputStream(sampleString2.getBytes());
        stringIS3 = new ByteArrayInputStream(sampleString3.getBytes());

        try {
            pdf1 = Utils.textToPdfData(sampleString1);
            pdf2 = Utils.textToPdfData(sampleString2);
            pdf3 = Utils.textToPdfData(sampleString3);

            pdfIS1 = new ByteArrayInputStream(pdf1);
            pdfIS2 = new ByteArrayInputStream(pdf2);
            pdfIS3 = new ByteArrayInputStream(pdf3);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Test
    public void putD1() {
        ds.putDocument(stringIS1, uri1, DocumentFormat.TXT);
    }

    private void putD2() {
        ds.putDocument(stringIS2, uri2, DocumentFormat.TXT);
    }
    private void putD3() {
        ds.putDocument(stringIS3, uri3, DocumentFormat.TXT);
    }

    private void putPDF1() {
        ds.putDocument(pdfIS1, uri1, DocumentFormat.PDF);
    }
    private void putPDF2() {
        ds.putDocument(pdfIS2, uri2, DocumentFormat.PDF);
    }
    private void putPDF3() {
        ds.putDocument(pdfIS3, uri3, DocumentFormat.PDF);
    }

    // returns a list with the string for every document in the documentStore

    enum testStringNumber {
        STRING1, STRING2, STRING3

    }

    /**
    * @return true if the specified string exists in the document store
    */
    public boolean stringExists(testStringNumber stringNumber) {
        String checkString = "";
        switch (stringNumber) {
            //Each checkString below only occurs in that specific test string
            case STRING1:
                checkString = "am";
                break;
            case STRING2:
                checkString = "used";
                break;
            case STRING3:
                checkString = "just";
                break;
                // I really should have a default case, but since i'm using an enum, I don't see
                // the point - it's impossible for the value NOT to be one of the above.

        }
        List<String> list = ds.search("IS");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(checkString)) {
                return true;
            }
        }

        return false;
    }

    @Test
    public void testDelete() {

        putD1();
        putD2();
        ds.deleteDocument(uri1);

        assertEquals(false, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));

        //Now, put it back and test deleting the second doc
        refreshInputStreams();
        putD1();
        assertEquals(true, stringExists(testStringNumber.STRING1));
        ds.deleteDocument(uri2);
        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(false, stringExists(testStringNumber.STRING2));
    }

    @Test
    public void testDeletePDF() {
        putPDF1();
        putPDF2();

        ds.deleteDocument(uri1);

        assertEquals(false, stringExists(testStringNumber.STRING1));
    }



    @Test
    public void testPrefixDelete() {

        putD1();
        putD2();

        //The string "sec" only appears in string 2
        ds.deleteAllWithPrefix("sec");

        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(false, stringExists(testStringNumber.STRING2));
    }

    public void testSearch() {
        List<String> strList = ds.search("stage");
        //String 2 should be before 1, since "stage" appears more often in it
        for (int i = 0; i < strList.size(); i++) {
            String s = strList.get(i);
            if (i == 0) {
            }
            if (i == 1) {
                assertEquals(sampleString1, s);
            }
        }
    }

    public void testSearchPrefix() {
        List<String> preStrList = ds.searchByPrefix("st");
        for (int i = 0; i < preStrList.size(); i++) {
            String s = preStrList.get(i);
            switch (i) {
                case 0:
                assertEquals(sampleString3, s);
                break;
                case 1:
                assertEquals(sampleString1, s);
                break;
                case 2: 
                assertEquals(sampleString2, s);
                break;
                default:
                break;
            }
        }
    }

    @Test
    public void testSearchTXT() {
        refreshInputStreams();
        putD1();
        putD2();
        putD3();

        testSearch();
        testSearchPrefix();
    }

    @Test 
    public void testSearchPDF() {
        refreshInputStreams();
        putPDF1();
        putPDF2();
        putPDF3();

        testSearch();
        testSearchPrefix();



    }

    @Test
    public void testSearchPDFTXT() {
        refreshInputStreams();
        putPDF1();
        putPDF2();
        putD3();

        testSearch();
        testSearchPrefix();
    }

    @Test
    public void testCommandSet() {
        putD1();
        putD2();
        putD3();
        ds.undo();
        assertEquals(false, stringExists(testStringNumber.STRING3));

        ds.deleteAll("stage");
        assertEquals(false, stringExists(testStringNumber.STRING1));
        assertEquals(false, stringExists(testStringNumber.STRING2));

        ds.undo();
        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));

    }

    @Test
    public void testCommandSetWithPrefix() {
        putD1();
        putD2();
        putD3();
        ds.undo();
        assertEquals(false, stringExists(testStringNumber.STRING3));


        ds.deleteAllWithPrefix("sta");
        assertEquals(false, stringExists(testStringNumber.STRING1));
        assertEquals(false, stringExists(testStringNumber.STRING2));

        ds.undo();
        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));

    }
    @Test 
    public void testU() {
        putD3();
        ds.deleteDocument(uri3);
        ds.undo();
        assertEquals(true, stringExists(testStringNumber.STRING3));

        refreshInputStreams();
        putD1();
        ds.undo(uri1);
        assertEquals(false, stringExists(testStringNumber.STRING1));
    }

}