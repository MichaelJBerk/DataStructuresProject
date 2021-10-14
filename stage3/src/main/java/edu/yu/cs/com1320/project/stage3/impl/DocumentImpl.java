package edu.yu.cs.com1320.project.stage3.impl;

import java.net.URI;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

import java.io.*;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage3.*;

public class DocumentImpl implements Document{

    private URI docURI;
    private String docText;
    private byte[] docData;
    private int txtHash;

    public DocumentImpl(URI uri, String txt, int txtHash){
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
        this.docURI = uri;
        this.docText = txt;
        this.txtHash = txtHash;
        this.docData = pdfBytes;
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
        return docText;

    }

    @Override
    public int getDocumentTextHashCode() {
        return txtHash;
    }

    @Override
    public URI getKey() {
        return docURI;
    }

    @Override
    public int wordCount(String word) {
        // TODO Auto-generated method stub

        return wordMap.get(word.toUpperCase());
        // return 0;
    }

}