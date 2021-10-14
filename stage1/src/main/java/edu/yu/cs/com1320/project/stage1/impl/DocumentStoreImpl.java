package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

import java.net.URI;
import java.io.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>(5);

    public DocumentStoreImpl() {
    
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if (input == null) {
            if (hashTable.get(uri) != null){
            int hash = hashTable.get(uri).hashCode();
            deleteDocument(uri);
            return hash;
            } else {
                return 0;
            }
        }
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        int hash = 0;
        if (hashTable.get(uri) != null) {
            hash = hashTable.get(uri).hashCode();
        } else {
            hash = 0;
        }
        try {
            DocumentImpl oldDoc = hashTable.get(uri);
           

            DocumentImpl cDoc;
            byte[] docData = new byte[input.available()];
            input.read(docData);
            String s = new String(docData);


            int txtHash = 0;
            if (format == format.TXT) {
                txtHash = s.hashCode();
                cDoc = new DocumentImpl(uri, s, txtHash);
                hashTable.put(uri, cDoc);
                hash = cDoc.getDocumentAsTxt().hashCode();
               
            } else if (format == format.PDF) {
                String pdfText = PDFString(docData);
                txtHash = pdfText.hashCode();
                cDoc = new DocumentImpl(uri, pdfText, txtHash, docData);
                hashTable.put(uri, cDoc);
                hash = txtHash;
            }

            // if (hash == 0) {
            //     hash = txtHash;
            // }
            if (oldDoc != null) {
                hash = oldDoc.getDocumentAsTxt().hashCode();
            }

        } catch (IOException e) {

        }
        return hash;
    }

    private String PDFString(byte[] docData) {
        try {
            PDDocument pdf;
            pdf = PDDocument.load(docData);
            if (pdf.getCurrentAccessPermission().canExtractContent()) {
                PDFTextStripper strip = new PDFTextStripper();
                String text = strip.getText(pdf).trim();
                return text;

            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            System.out.print(e);
        }
        return null;
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {

        if (hashTable.get(uri)!= null) {
            DocumentImpl doc = hashTable.get(uri); 
            return doc.getDocumentAsPdf();
        }
        return null;
       
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        DocumentImpl doc = hashTable.get(uri);
        String s;
        if (doc == null) {
            return null;
        } else {
            s = doc.getDocumentAsTxt();
            return s;
        }
        
    }

    @Override
    public boolean deleteDocument(URI uri) {
        boolean returnValue = false;
        if (hashTable.get(uri) != null) {
            returnValue = true;
        } 
        hashTable.put(uri, null);
        return returnValue;
    }

}