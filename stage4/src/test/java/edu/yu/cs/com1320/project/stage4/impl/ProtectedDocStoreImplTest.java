package edu.yu.cs.com1320.project.stage4.impl;

import static org.junit.Assert.assertEquals;

import org.junit.*;
import java.util.List;


import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;

import edu.yu.cs.com1320.project.stage4.impl.TestHelpers;


public class ProtectedDocStoreImplTest extends TestHelpers {

    @Test 
    public void testDocCount() {
        putD1();
        putD2();
        assertEquals(2, ds.numberOfDocs);
        ds.undo();
        assertEquals(1, ds.numberOfDocs);
        putD3();
        assertEquals(2, ds.numberOfDocs);
        ds.deleteDocument(uri3);
        assertEquals(1, ds.numberOfDocs);
    }

    @Test 
    public void testGetAllDocs() {
        putD1();
        putD2();
        putNoSpaceDoc();

        List<Document> dlist = ds.allDocs(null);
        assertEquals(3, dlist.size());

    }

    @Test
    public void testOldest() {

        putD1();
        putD2();
        putD3();

        Document doc = (Document) ds.heap.removeMin();
        assertEquals(sampleString1, doc.getDocumentAsTxt());
    }

    @Test 
    public void testTooFarUndo() {
        putD1();
        ds.undo();
        ds.undo();
    }

    @Test
    public void testMaxDocCount() {
        ds.setMaxDocumentCount(2);

        putD1();
        putD2();

        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));

        putD3();
        assertEquals(false, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));
        assertEquals(true, stringExists(testStringNumber.STRING3));
        
    }

    // @Test 
    public void getMaxSize() {

        // Doc1 Size: 905
        // Doc2 Size: 946
        // Doc3 Size: 862

        putD1();
        putD2();
        putD3();
        DocumentImpl d1 = (DocumentImpl) ds.getDocument(uri1);
        DocumentImpl d2 = (DocumentImpl) ds.getDocument(uri2);
        DocumentImpl d3 = (DocumentImpl) ds.getDocument(uri3);

        System.out.println("Doc1 Size: " + d1.getDocumentSize());
        System.out.println("Doc2 Size: " + d2.getDocumentSize());
        System.out.println("Doc3 Size: " + d3.getDocumentSize());



    }

    @Test
    public void testMaxSize() {
        ds.setMaxDocumentBytes(2000);
        putD1();
        putD2();

        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));

        putD3();
        System.out.println();
    }

    @Test 
    public void testDocsHaveSameTime() {
        putD1();
        putD2();
        putD3();

        List<Document> docList = ds.searchDocuments("stage");
        long time = docList.get(0).getLastUseTime();
        for (Document d: docList) {
            assertEquals(time, d.getLastUseTime());
        }

    }

    @Test
    public void testPDFHaveSameTime() {
        putPDF1();
        putPDF2();
        putPDF3();

        List<Document> docList = ds.searchDocuments("stage");
        long time = docList.get(0).getLastUseTime();
        for (Document d: docList) {
            assertEquals(time, d.getLastUseTime());
        }

    }

    @Test 
    public void docPrefixSameTime() {
        putD1();
        putD2();
        putD3();

        List<Document> docList = ds.searchDocumentsPrefix("stag");
        long time = docList.get(0).getLastUseTime();
        for (Document d: docList) {
            assertEquals(time, d.getLastUseTime());
        }

    }

    @Test 
    public void testPDFPrefixSameTime() {
        putPDF1();
        putPDF2();
        putPDF3();

        List<Document> docList = ds.searchDocumentsPrefix("stag");
        long time = docList.get(0).getLastUseTime();
        for (Document d: docList) {
            assertEquals(time, d.getLastUseTime());
        }

    }



    @Test
    public void testUndoMultipleDocs() {
        ds.setMaxDocumentCount(3);
        putD1();
        putD2();
        putPDF3();

        ds.deleteAll("stage");

        ds.undo();

        DocumentImpl d1 = ds.hashTable.get(uri1);
        DocumentImpl d2 = ds.hashTable.get(uri2);

        assertEquals(d1.getLastUseTime(), d2.getLastUseTime());
    }

    @Test
    public void testByteSizeTooBig() {
        ds.setMaxDocumentBytes(150);
        putD1();
        putD2();

        assertEquals(true, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));
        putD3();
        System.out.println("hey");
        assertEquals(false, stringExists(testStringNumber.STRING1));
        assertEquals(true, stringExists(testStringNumber.STRING2));
        assertEquals(true, stringExists(testStringNumber.STRING3));

    }

    @Test 
    public void overrideByteTotal() {

        ds.setMaxDocumentBytes(150);
        putD1();
        putD2();
        DocumentImpl d1 = (DocumentImpl) ds.getDocument(uri1);
        DocumentImpl d2 = (DocumentImpl) ds.getDocument(uri2);
        ds.putDocument(stringIS3, uri1, DocumentFormat.TXT);
        System.out.println(d1.getDocumentSize());
        System.out.println(d2.getDocumentSize());

        System.out.println(ds.totalDocumentBytes());

        // assertEquals(expected, actual);


    }

    @Test
    public void removeEverything() {
        putD1();
        putD2();
        putD3();

        ds.deleteDocument(uri1);
        ds.deleteDocument(uri2);
        ds.deleteDocument(uri3);

        System.out.println("");
    }

    @Test
    public void testReheapify() {
        
        
        putD1();
        putD2();
        
       
        DocumentImpl d1 =  (DocumentImpl) ds.getDocument(uri1);
        DocumentImpl d2 = (DocumentImpl) ds.getDocument(uri2);
    
        d2.setLastUseTime(java.lang.System.nanoTime());
        
        System.out.println("d1 use time: " + d1.getLastUseTime());

        

        ds.heap.reHeapify(d2);

        d1.setLastUseTime(java.lang.System.nanoTime());
        ds.heap.reHeapify(d2);

        assertEquals(d2, (DocumentImpl)ds.heap.removeMin());


    }
}