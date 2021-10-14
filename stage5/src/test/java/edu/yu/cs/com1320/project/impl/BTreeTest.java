package edu.yu.cs.com1320.project.impl;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.io.*;

import edu.yu.cs.com1320.project.TestHelpers;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreTestHelpers;

public class BTreeTest extends TestHelpers {

    BTreeImpl<URI, Document> btree = new BTreeImpl<URI, Document>();
    Document doc1;
    DocumentPersistenceManager pm;
    
    public DocumentStoreTestHelpers dsth = new DocumentStoreTestHelpers();

    @Before
    public void setup() {
        dsth.makeURIs();
        dsth.setup();
        doc1 = dsth.doc1;
        String path = "/Users/michaelberk/Documents/CS Work/";
        try {
            File pathFile = new File(path);
            pm = new DocumentPersistenceManager(pathFile);
            btree.setPersistenceManager(pm);
        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
       
    }
    @Test 
    public void testPut() {
        btree.put(uri1, doc1);
    }

    @Test
    public void testGet() {
        testPut();
        Document doc = btree.get(uri1);
        assertEquals(doc1, doc);
    }

    @Test
    public void getWithNothing() {
        btree.get(uri1);
    }

    @Test
    public void testWriteToDisk() {
        testPut();
        try {
            btree.moveToDisk(uri1);
        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
        Document noDoc = btree.get(uri1);
        assertEquals(doc1.getDocumentAsTxt(), noDoc.getDocumentAsTxt());
        
        System.out.println("hello");

    }

    @After 
    public void cleanup() {
        btree.delete(uri1);
    }
}