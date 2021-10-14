package edu.yu.cs.com1320.project.stage4.impl;

import org.junit.*;
import java.net.URI;


import java.io.*;
import java.util.List;

import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage4.stage4.Utils;


public class TestHelpers {

    public String sampleString1 = "Hello, I am testing Stage 3. Stage Stage Hello is (1)";
    public String sampleString2 = "This is the second string being used to test stage 3. Stage Stage Stage. (2)";
    public String sampleString3 = "this is just a string. (3)";
    public String noSpaceString = "ThisHasNoSpaces";
    public DocumentStoreImpl ds;
    public URI uri1;
    public URI uri2;
    public URI uri3;
    public InputStream stringIS1;
    public InputStream stringIS2;
    public InputStream stringIS3;
    public InputStream noSpaceIS;
    

    public String[] strArray1;
    public String[] strArray2;
    public String[] strArray3;

    public byte[] pdf1;
    public byte[] pdf2;
    public byte[] pdf3;
    public InputStream pdfIS1;
    public InputStream pdfIS2;
    public InputStream pdfIS3;

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
        noSpaceIS = new ByteArrayInputStream(noSpaceString.getBytes());
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

    public void putD2() {
        ds.putDocument(stringIS2, uri2, DocumentFormat.TXT);
    }
    public void putD3() {
        ds.putDocument(stringIS3, uri3, DocumentFormat.TXT);
    }
    public void putNoSpaceDoc() {
        ds.putDocument(noSpaceIS, uri1, DocumentFormat.TXT);
    }

    public void putPDF1() {
        ds.putDocument(pdfIS1, uri1, DocumentFormat.PDF);
    }
    public void putPDF2() {
        ds.putDocument(pdfIS2, uri2, DocumentFormat.PDF);
    }
    public void putPDF3() {
        ds.putDocument(pdfIS3, uri3, DocumentFormat.PDF);
    }

    // returns a list with the string for every document in the documentStore

    public enum testStringNumber {
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
        List<Document> docList = ds.allDocs(null);
        List<String> list = ds.docToStringList(docList);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(checkString)) {
                return true;
            }
        }

        return false;
    }
}