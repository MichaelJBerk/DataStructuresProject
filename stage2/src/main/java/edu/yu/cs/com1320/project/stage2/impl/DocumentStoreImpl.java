package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage2.*;
import edu.yu.cs.com1320.project.*;

import java.net.URI;
import java.util.function.Function;
import java.io.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>(5);

    private StackImpl cmdStack = new StackImpl();

    public DocumentStoreImpl() {

    }


    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl currentVal = hashTable.get(uri);
       
        Function <URI, Boolean> func = docURI -> {
            
            // return setupFunction(docURI);
            boolean returnValue = false;
        //If i put something there and want to remove it:
        DocumentImpl doc = hashTable.get(uri);
        // String docS = doc.getDocumentAsTxt();

        if (hashTable.get(uri) != null) {
            returnValue = true;
        } 
        hashTable.put(uri, currentVal);

        //What if something was overwritten?

        return returnValue;
            
        };
        Command newCMD = new Command(uri, func);
        cmdStack.push(newCMD);

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

            DocumentImpl cDoc;
            byte[] docData = new byte[input.available()];
            input.read(docData);
            String s = new String(docData);
            DocumentImpl oldDoc = hashTable.get(uri);
            

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

            if (oldDoc != null) {
                int nHash = oldDoc.getDocumentAsTxt().hashCode();
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
                pdf.close();
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
        Document d = getDocument(uri);
        Function <URI, Boolean> func = docURI -> {
           boolean lambdaReturnValue = true;
           DocumentImpl doc = (DocumentImpl) d;
           if (doc == null) {
               lambdaReturnValue = false;
           }
           hashTable.put(docURI, doc);
           return lambdaReturnValue;
        };
        Command newCMD = new Command(uri, func);
        cmdStack.push(newCMD);

        boolean returnValue = false;
        if (hashTable.get(uri) != null) {
            returnValue = true;
        } 
        hashTable.put(uri, null);
        return returnValue;
    }

    @Override
    public void undo() throws IllegalStateException {

        Command topCMD = (Command) cmdStack.pop();
        boolean boo = topCMD.undo();
        if (!boo) {
            throw new IllegalStateException();
        }

    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        StackImpl newStack = new StackImpl();
        Command newCMD = (Command) cmdStack.peek();
        
        while (newCMD.getUri() != uri) {
         newStack.push(newCMD);
         cmdStack.pop();
         newCMD = (Command) cmdStack.peek();
        }
        newCMD.undo();
        while (newStack.peek() != null) {
            newCMD = (Command) newStack.peek();
            cmdStack.push(newCMD);
            newStack.pop();
        }
    }


    

    /**
    * @return the Document object stored at that URI, or null if there is no such Document
    */
    protected Document getDocument(URI uri){
        Document cDocument = hashTable.get(uri);
        return cDocument;
    }
    
}