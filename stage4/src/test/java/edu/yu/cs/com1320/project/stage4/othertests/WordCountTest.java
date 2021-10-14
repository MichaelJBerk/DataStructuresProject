package edu.yu.cs.com1320.project.stage4.othertests;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import edu.yu.cs.com1320.project.stage4.impl.TestHelpers;

import java.util.List;

//Tests for new Stage 3 functionality
//This should really not be called wordCountTest anymore, but i'm too lazy to change it.
public class WordCountTest extends TestHelpers {

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