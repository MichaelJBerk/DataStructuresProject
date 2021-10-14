package edu.yu.cs.com1320.project;
import org.junit.*;
import java.net.URI;

public class TestHelpers {
    public String sampleString1 = "Hello, I am testing Stage 3. Stage Stage Hello is (1)";
    public String sampleString2 = "This is the second string being used to test stage 3. Stage Stage Stage. (2)";
    public String sampleString3 = "this is just a string. (3)";
    public String noSpaceString = "ThisHasNoSpaces";

    public URI uri1;
    public URI uri2;
    public URI uri3;

    @Before 
    public void makeURIs() {
        try {
            uri1 = new URI("mibe://www.yu.edu/documents/doc1");
            uri2 = new URI("mibe://uri2");
            uri3 = new URI("mibe://uri3");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}