package edu.yu.cs.com1320.project.stage4;

import org.junit.*;
import java.net.URI;

import static org.junit.Assert.assertEquals;

import java.io.*;

import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.stage4.stage4.Utils;

public class UndoTest {

    String sampleString = "Hello, World! This is just a test for Stage 2! (1)";
    String sampleString2 = "This is another sample string (2)";
    String sampleString3 = "This is the 3rd sample string (3)";
    DocumentStoreImpl ds;
    URI uri1;
    URI uri2;
    URI uri3;
    InputStream stringIS1;
    InputStream stringIS2;
    InputStream stringIS3;
    

    InputStream pdfIS1;
    InputStream pdfIS2;
    InputStream pdfIS3;

    byte[] pdf1;
    byte[] pdf2;
    byte[] pdf3;

    @Before 
    public void setupDS() {
        ds = new DocumentStoreImpl();
        try {
            uri1 = new URI("mibe://cleverStringGoesHere");
            uri2 = new URI("mibe://yetAnotherCleverString");
            uri3 = new URI ("mibe://imRunningOutOfThingsToPutHere");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void refreshInputStreams() {
        stringIS1 = new ByteArrayInputStream(sampleString.getBytes());
        stringIS2 = new ByteArrayInputStream(sampleString2.getBytes());
        stringIS3 = new ByteArrayInputStream(sampleString3.getBytes());
        try {
            pdf1 = Utils.textToPdfData(sampleString);
            pdf2 = Utils.textToPdfData(sampleString2);
            pdf3 = Utils.textToPdfData(sampleString3);

            pdfIS1 = new ByteArrayInputStream(pdf1);
            pdfIS2 = new ByteArrayInputStream(pdf2);
            pdfIS3 = new ByteArrayInputStream(pdf3);
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public void putGenericTXT() {
        refreshInputStreams();
        ds.putDocument(stringIS1, uri1, DocumentFormat.TXT);
        String gottenTXT = ds.getDocumentAsTxt(uri1);
        assertEquals(sampleString, gottenTXT);
    }

    public void putGenericTXT2() {
        refreshInputStreams();
        ds.putDocument(stringIS2, uri2, DocumentFormat.TXT);
        String gottenTXT = ds.getDocumentAsTxt(uri2);
        assertEquals(sampleString2, gottenTXT);
    }

    public void putGenericTXT3() {
        refreshInputStreams();
        ds.putDocument(stringIS3, uri3, DocumentFormat.TXT);
        String gottenTXT = ds.getDocumentAsTxt(uri3);
        assertEquals(sampleString3, gottenTXT);
    }

    public void putGenericPDF() {
        refreshInputStreams();
        ds.putDocument(pdfIS1, uri1, DocumentFormat.PDF);
        String gottenTXT = ds.getDocumentAsTxt(uri1);
        assertEquals(gottenTXT, ds.getDocumentAsTxt(uri1));
    }

    public void putGenericPDF2() {
        refreshInputStreams();
        ds.putDocument(pdfIS2, uri2, DocumentFormat.PDF);
        String gottenTXT = ds.getDocumentAsTxt(uri2);
        assertEquals(gottenTXT, ds.getDocumentAsTxt(uri2));
    }
    public void putGenericPDF3() {
        refreshInputStreams();
        ds.putDocument(pdfIS3, uri3, DocumentFormat.PDF);
        String gottenTXT = ds.getDocumentAsTxt(uri3);
        assertEquals(gottenTXT, ds.getDocumentAsTxt(uri3));
    }


    @Test
    public void testPutPDF1() {
        putGenericPDF();
    }

    @Test 
    public void testPutPDFUndo() {
        putGenericPDF();
        ds.undo();
        String gTXT2 = ds.getDocumentAsTxt(uri1);
        assertEquals(null, gTXT2);
    }

    @Test
    public void testPutUndo() {
        putGenericTXT();
        ds.undo();
        String gTXT2 = ds.getDocumentAsTxt(uri1);
        assertEquals(null, gTXT2);
    }

    @Test
    public void testPutTXTPDFUndo() {
        putGenericTXT();
        putGenericPDF();

        ds.undo();
        String txt = ds.getDocumentAsTxt(uri2);
        assertEquals(null, txt);
        ds.undo();
        String pdf = ds.getDocumentAsTxt(uri1);
        assertEquals(null, pdf);

    }

    @Test
    public void testDeleteUndo() {
        putGenericTXT();
        ds.deleteDocument(uri1);
        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));

    }

    @Test
    public void testDeleteUndoPDF() {
        putGenericPDF();
        ds.deleteDocument(uri1);
        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
    }

    @Test
    public void testDeleteUndoTXTPDF() {
        putGenericTXT();
        putGenericPDF2();
        ds.deleteDocument(uri1);
        ds.deleteDocument(uri2);
        ds.undo();
        assertEquals(sampleString2, ds.getDocumentAsTxt(uri2));
        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
    }

    @Test 
    public void testMultipleUndo() {
        putGenericTXT();
        putGenericTXT2();
        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri1));


        putGenericTXT();
        putGenericTXT2();
        ds.deleteDocument(uri1);
        ds.deleteDocument(uri2);
        ds.undo();
        assertEquals(sampleString2, ds.getDocumentAsTxt(uri2));
        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        
    }


    @Test 
    public void testMultipleUndoPDF() {
        putGenericPDF();
        putGenericPDF2();
        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri1));


        putGenericPDF();
        putGenericPDF2();
        ds.deleteDocument(uri1);
        ds.deleteDocument(uri2);
        ds.undo();
        assertEquals(sampleString2, ds.getDocumentAsTxt(uri2));
        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        
    }


    @Test 
    public void testMultipleUndoTXTPDF() {
        putGenericTXT();
        putGenericTXT2();
        putGenericPDF3();
        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri3));
        assertEquals(sampleString2, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));

        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));

        ds.undo();
        assertEquals(null, ds.getDocumentAsTxt(uri1));

        

        putGenericTXT();
        putGenericTXT2();
        putGenericPDF3();
        ds.deleteDocument(uri1);
        ds.deleteDocument(uri2);
        ds.deleteDocument(uri3);
        ds.undo();
        assertEquals(sampleString3, ds.getDocumentAsTxt(uri3));
        ds.undo();
        assertEquals(sampleString2, ds.getDocumentAsTxt(uri2));
        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        
    }

    @Test
    public void testUndoURI() {
        putGenericTXT();
        putGenericTXT2();
        putGenericTXT3();
        ds.undo(uri2);
        assertEquals(null, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        assertEquals(sampleString3, ds.getDocumentAsTxt(uri3));

        
    }

    @Test
    public void testUndoPDFURI() {
        putGenericPDF();
        putGenericPDF2();
        putGenericPDF3();
        ds.undo(uri2);
        assertEquals(null, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        assertEquals(sampleString3, ds.getDocumentAsTxt(uri3));
    }

    @Test
    public void testUndoTXTPDFURI() {
        putGenericPDF();
        putGenericPDF2();
        putGenericTXT3();
        ds.undo(uri2);
        assertEquals(null, ds.getDocumentAsTxt(uri2));
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        assertEquals(sampleString3, ds.getDocumentAsTxt(uri3));
    }

    @Test
    public void overrwriteTest() {
        putGenericTXT();
        ds.putDocument(stringIS2, uri1, DocumentFormat.TXT);

        assertEquals(sampleString2, ds.getDocumentAsTxt(uri1));
    

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        System.out.println(ds.getDocumentAsTxt(uri1));


    }
    @Test
    public void overrwriteTestPDF() {
        putGenericPDF();
        refreshInputStreams();
        ds.putDocument(pdfIS2, uri1, DocumentFormat.PDF);

        assertEquals(sampleString2, ds.getDocumentAsTxt(uri1));
    

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        System.out.println(ds.getDocumentAsTxt(uri1));
    }

    @Test
    public void overrwriteTestPDFTXT() {
        putGenericTXT();
        refreshInputStreams();
        ds.putDocument(pdfIS2, uri1, DocumentFormat.PDF);

        assertEquals(sampleString2, ds.getDocumentAsTxt(uri1));
    

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        System.out.println(ds.getDocumentAsTxt(uri1));


    }

    @Test
    public void overrwriteTestTXTWithPDF() {
        putGenericTXT();
        refreshInputStreams();
        ds.putDocument(pdfIS1, uri1, DocumentFormat.PDF);

        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
    

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
        System.out.println(ds.getDocumentAsTxt(uri1));


    }

    @Test
    public void testNullInputStream() {
        ds.putDocument(null, uri1, DocumentFormat.TXT);
        ds.undo();

    }
    @Test
    public void testNullInputStreamPDF() {
        ds.putDocument(null, uri1, DocumentFormat.PDF);
        ds.undo();
    }

    @Test
    (expected = IllegalStateException.class)
    public void testNullInputStreamDeleted() {
        ds.putDocument(null, uri1, DocumentFormat.PDF);
        ds.deleteDocument(uri1);
        ds.undo();
    }

    //TODO: See if this is how it should be or not 
    @Test 
    public void testOverwriteWithNull() {
        putGenericTXT();
        ds.putDocument(null, uri1, DocumentFormat.TXT);

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
    }

    @Test 
    public void testOverwritePDFWithNull() {
        putGenericPDF();
        ds.putDocument(null, uri1, DocumentFormat.PDF);

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
    }

    @Test 
    public void testOverwritePDFWithNullTXT() {
        putGenericPDF();
        ds.putDocument(null, uri1, DocumentFormat.TXT);

        ds.undo();
        assertEquals(sampleString, ds.getDocumentAsTxt(uri1));
    }

}