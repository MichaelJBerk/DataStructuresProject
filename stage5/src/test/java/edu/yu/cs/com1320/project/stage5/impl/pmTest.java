package edu.yu.cs.com1320.project.stage5.impl;
import org.junit.*;
import edu.yu.cs.com1320.project.stage5.Document;

import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager.*;

import static org.junit.Assert.assertEquals;

import java.io.File;


public class pmTest extends DocumentStoreTestHelpers {

    DocumentPersistenceManager pm;
    File baseDir = new File(System.getProperty("user.dir"));
    DocSerializer docSerial;
    DocDeserializer docDeserial;

    @Before 
    public void testPersistenceManager() {
        pm = new DocumentPersistenceManager(baseDir);
        docSerial = pm.serializer;
        docDeserial = pm.deserializer;
    }
    
    @Test 
    public void testSerialDeSerial() {
        String d1String = docSerial.docToString(doc1);

        Document d1DeS = docDeserial.stringToDoc(d1String);

        assertEquals(doc1.getDocumentAsTxt(), d1DeS.getDocumentAsTxt());
        assertEquals(doc1.getKey(), d1DeS.getKey());
        assertEquals(doc1.getDocumentTextHashCode(), d1DeS.getDocumentTextHashCode());
        assertEquals(doc1.getWordMap(),d1DeS.getWordMap());

    }

    @Test 
    public void testReadWriteFile() {
        try {
            pm.serialize(uri1, doc1);

            String readString = pm.readFile(uri1);
            System.out.println(readString);
        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
    }

    


}