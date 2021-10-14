package edu.yu.cs.com1320.project.stage5.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.*;

import java.net.URI;
import java.util.List;


import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;

import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreTestHelpers;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl.DocumentURI;

public class ProtectedDocStoreImplTest extends DocumentStoreTestHelpers {

    @Test 
    public void testDocCount() {
        putD1();
        putD2();
        assertEquals(2, ds.numberOfDocs());
        ds.undo();
        assertEquals(1, ds.numberOfDocs());
        putD3();
        assertEquals(2, ds.numberOfDocs());
        ds.deleteDocument(uri3);
        assertEquals(1, ds.numberOfDocs());
    }

    @Test 
    public void nsDoc() {
        putNoSpaceDoc();
    }

    @Test 
    public void testGetAllDocs() {
        putD1();
        putD2();
        putNoSpaceDoc();

        List<Document> dlist = ds.allDocs(null);
        assertEquals(2, dlist.size());

    }

    @Test
    public void testOldest() {

        putD1();
        putD2();
        putD3();

        DocumentURI cURI = (DocumentURI)  ds.heap.removeMin();
        Document doc = ds.getDocument(cURI.uri);
        assertEquals(sampleString1, doc.getDocumentAsTxt());
    }

    @Test(expected = IllegalStateException.class)
    public void testTooFarUndo() {
        putD1();
        ds.undo();
        ds.undo();
    }
    @Test
    public void testMaxDocCountMax1() {
        ds.setMaxDocumentCount(1);

        putD1();


        assertEquals(true, docExists(testStringNumber.STRING1));

        putD2();
        System.out.println(DocExistsInBtree(uri1));


        assertEquals(false, DocExistsInBtree(uri1));
        assertEquals(true, DocExistsInBtree(uri2));

        // putD1();
        ds.delete(uri1);
        
    }


    @Test
    public void testMaxDocCountMax2() {
        ds.setMaxDocumentCount(2);

        // putD1();
        // putD2();
        putPDF1();
        putPDF2();

        // assertEquals(true, docExists(testStringNumber.STRING1));
        // assertEquals(true, docExists(testStringNumber.STRING2));

        //Ensure Doc2 is the latest so Doc1 will be deleted when inserting Doc3
        // ds.getDocumentAsTxt(uri2);


        // putD3();
        putPDF3();
        // assertNull("wrong", ds.getDocument(uri1));
        assertEquals(false, docExists(testStringNumber.STRING1));
        assertEquals(true, docExists(testStringNumber.STRING2));
        assertEquals(true, docExists(testStringNumber.STRING3));
        
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

        assertEquals(true, docExists(testStringNumber.STRING1));
        assertEquals(true, docExists(testStringNumber.STRING2));

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
            long lsu = d.getLastUseTime();
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

        DocumentImpl d1 = ds.bTree.get(uri1);
        DocumentImpl d2 = ds.bTree.get(uri2);

        assertEquals(d1.getLastUseTime(), d2.getLastUseTime());
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
     //TODO: Update test - ensure stuff is kicked out in order to read files from disk.
     @Test
     public void testByteSizeTooBig() {
         ds.setMaxDocumentBytes(150);
         putD1();
         putD2();
 
         assertEquals(true, docExists(testStringNumber.STRING1));
         assertEquals(true, docExists(testStringNumber.STRING2));
         putD3();
         System.out.println("hey");
         assertEquals(false, docExists(testStringNumber.STRING1));
         assertEquals(true, docExists(testStringNumber.STRING2));
         assertEquals(true, docExists(testStringNumber.STRING3));
 
     }

    @After
    public void removeEverything() {
        // putD1();
        // ds.putDocument(stringIS1, uri1, DocumentFormat.TXT);
        // putD2();
        // putD3();

        ds.delete(uri1);
        ds.delete(uri2);
        ds.delete(uri3);

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

        DocumentURI curi1 = ds.uriToDocURI(uri1);
        DocumentURI curi2 = ds.uriToDocURI(uri2);
        ds.heap.reHeapify(curi2);

        d1.setLastUseTime(java.lang.System.nanoTime());
        ds.heap.reHeapify(curi2);

        DocumentURI removedURI = (DocumentURI) ds.heap.removeMin();
        DocumentImpl removedDoc = (DocumentImpl) ds.bTree.get(removedURI.uri);

        assertEquals(d2, removedDoc);


    }

   

    @Test
    public void testDocWritingToDisk() {


        ds.setMaxDocumentCount(1);
        putD1();
        putD2();

        Document gDoc1 = ds.readAndGetDocument(uri1);

        assertEquals(doc1.getDocumentAsTxt(), gDoc1.getDocumentAsTxt());

    }

    @Test
    public void dontDeleteFromTrieWhenWritingToDisk() {
        putD1();
        putD2();
        // putD3();
        
        System.out.println("hello");
    }



}