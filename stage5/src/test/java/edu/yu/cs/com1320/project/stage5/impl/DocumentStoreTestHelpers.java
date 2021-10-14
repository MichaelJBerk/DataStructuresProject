package edu.yu.cs.com1320.project.stage5.impl;

import org.junit.*;
import java.net.URI;


import java.io.*;
import java.util.List;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.TestHelpers;
import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.impl.BTreeImplTest;


public class DocumentStoreTestHelpers extends TestHelpers{

    public String noSpaceString = "ThisHasNoSpaces";
    public DocumentStoreImpl ds;
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

    public Document doc1;
    public Document doc2;
    public Document doc3;

    public Document pdfDoc1;
    public Document pdfDoc2;
    public Document pdfDoc3;

    public BTreeImplTest bTreeTester;


    @Before
    public void setup() {
        
        ds = new DocumentStoreImpl();
        
        refreshInputStreams();

        doc1 = ds.makeTXTDoc(sampleString1, uri1);
        doc2 = ds.makeTXTDoc(sampleString2, uri2);
        doc3 = ds.makeTXTDoc(sampleString3, uri3);


        pdfDoc1 = ds.makePDFDoc(sampleString1, uri1, pdf1);
        pdfDoc2 = ds.makePDFDoc(sampleString2, uri2, pdf2);
        pdfDoc3 = ds.makePDFDoc(sampleString3, uri3, pdf3);


        strArray1 = sampleString1.split(" ");
        strArray2 = sampleString2.split(" ");
        strArray3 = sampleString3.split(" ");
        
        bTreeTester = new BTreeImplTest(ds.bTree);

    }

    @After 
    public void cleanup() {
        // ds.deleteDocument(uri1);
        // ds.deleteDocument(uri2);
        // ds.deleteDocument(uri3);
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

    public boolean DocExistsInBtree(URI uri) {
        return bTreeTester.getWithoutReadingFromDisk(uri) != null;
    }

    /**
    * @return true if the specified string exists in the document store
    */
    public boolean docExists(testStringNumber stringNumber) {
        // String checkString = "";
        URI checkURI = null;
        switch (stringNumber) {

            //Each checkString below only occurs in that specific test string
            case STRING1:
                // checkString = "am";
                checkURI = uri1;
                break;
            case STRING2:
                // checkString = "used";
                checkURI = uri2;
                break;
            case STRING3:
                // checkString = "just";
                checkURI = uri3;
                break;
                // I really should have a default case, but since i'm using an enum, I don't see
                // the point - it's impossible for the value NOT to be one of the above.

        }
        return ds.getDocument(checkURI) != null;
        // return DocExistsInBtree(checkURI);
        // List<Document> docList = ds.allDocs(null);
        // List<String> list = ds.docToStringList(docList);
        // for (int i = 0; i < list.size(); i++) {
        //     if (list.get(i).contains(checkString)) {
        //         return true;
        //     }
        // }

        // return false;
    }
}