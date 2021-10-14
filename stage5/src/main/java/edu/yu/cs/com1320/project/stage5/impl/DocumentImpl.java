package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

import java.io.*;

import edu.yu.cs.com1320.project.stage5.*;

public class DocumentImpl implements Document {

    private URI docURI;
    private String docText;
    private byte[] docData;
    private int txtHash;
    private long lastUseTime;

    protected class hey {

        public void heyfunc() {

        }
        public void heyfunc2() {
            
        }
        public void heyfunc3() {
            
        }
        public void heyfunc4() {
            
        }
    }

    public DocumentImpl(URI uri, String txt, int txtHash) {
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
        makeWordMap();
        setLastUseTime(java.lang.System.nanoTime());
        // commonInit(uri, txt, txtHash);
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
        this.docData = pdfBytes;
        makeWordMap();
        // commonInit(uri, txt, txtHash);
        setLastUseTime(java.lang.System.nanoTime());
    }

    private void commonInit(URI uri, String txt, int txtHash) {
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
        // makeWordMap();
        setLastUseTime(java.lang.System.nanoTime());
    }
    protected Map<String, Integer> wordMap = new HashMap<String, Integer>(5);

    @Override
    public byte[] getDocumentAsPdf() {
        setLastUseTime(java.lang.System.nanoTime());
        if (docData == null) {
            try {
                PDDocument pdf = new PDDocument();
                PDPage page = new PDPage();
                PDFont font = PDType1Font.HELVETICA;
                pdf.addPage(page);
                PDPageContentStream content = new PDPageContentStream(pdf, page);
                content.beginText();
                content.setFont(font, 12);
                content.showText(this.docText);
                content.endText();
                content.close();

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                pdf.save(os);
                byte[] ba = os.toByteArray();
                return ba;

            } catch (Exception e) {
                return null;
            }
        }

        return docData;
    }

    @Override
    public String getDocumentAsTxt() {
        setLastUseTime(java.lang.System.nanoTime());
        return docText;

    }

    @Override
    public int getDocumentTextHashCode() {
        setLastUseTime(java.lang.System.nanoTime());
        return txtHash;
    }

    @Override
    public URI getKey() {
        setLastUseTime(java.lang.System.nanoTime());
        return docURI;
    }

    // protected HashMap<String, Integer> setWordMap() {
    protected void makeWordMap() {
        String text = this.getDocumentAsTxt().toUpperCase();
        String[] strArray = text.split(" ");
        HashMap<String, Integer> newWordMap = new HashMap<String, Integer>(256);

        for (String word : strArray) {
            // Setting up the HashMap of words

            // Using some basic regex to remove non-alphanumeric chars, because it's just much eaiser this way
            word = word.replaceAll("\\W", "");
            int occurances = 0;
            if (newWordMap.get(word) != null) {
                occurances = newWordMap.get(word);
            }
            if (occurances == 0) {
                newWordMap.put(word, 1);
            }
            if (occurances > 0) {
                newWordMap.put(word, null);
                newWordMap.put(word, occurances + 1);
            }

        }
        this.wordMap = newWordMap;

    }

    @Override
    public int wordCount(String word) {
        setLastUseTime(java.lang.System.nanoTime());
        String upperWord = word.toUpperCase();
        int wInt;
        try {
            wInt = wordMap.get(upperWord);
        } catch (NullPointerException e) {
            wInt = 0;
        }
        return wInt;
    }

    @Override
    public int compareTo(Document o) {
        if (o != null) {
            if (getLastUseTime() < o.getLastUseTime()) {
                return -1;
            }
            if (getLastUseTime() > o.getLastUseTime()) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public long getLastUseTime() {

        return lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        lastUseTime = timeInNanoseconds;
        System.out.println("hello");
    }

    protected int getDocumentSize() {
        int size = 0;
        // int size = this.getDocumentAsTxt().getBytes().length;
        if (this.docData != null) {
            size += this.docData.length;
        } else {
            size = this.getDocumentAsTxt().getBytes().length;
        }
        return size;
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.wordMap;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordMap = wordMap;
    }
}