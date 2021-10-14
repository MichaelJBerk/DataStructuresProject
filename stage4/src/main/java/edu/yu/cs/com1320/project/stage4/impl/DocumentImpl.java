package edu.yu.cs.com1320.project.stage4.impl;

import java.net.URI;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

import java.io.*;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage4.*;

public class DocumentImpl implements Document{

    private URI docURI;
    private String docText;
    private byte[] docData;
    private int txtHash;
    private long lastUseTime;

    public DocumentImpl(URI uri, String txt, int txtHash){
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
        setLastUseTime(java.lang.System.nanoTime());
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
        this.docData = pdfBytes;
        setLastUseTime(java.lang.System.nanoTime());
    }
    protected HashTableImpl<String, Integer> getWordMap() {
        return this.wordMap;
    }
    protected void setWordMap(HashTableImpl<String, Integer> map) {
        this.wordMap = map;
    }
    
    protected HashTableImpl<String, Integer> wordMap = new HashTableImpl<String, Integer>(5);

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

    @Override
    public int wordCount(String word) {
        setLastUseTime(java.lang.System.nanoTime());

        return wordMap.get(word.toUpperCase());
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
    }

    protected int getDocumentSize() {
        int size = this.getDocumentAsTxt().getBytes().length;
        if (this.docData != null) {
            size += this.docData.length;
        }
        return size;
    }
}