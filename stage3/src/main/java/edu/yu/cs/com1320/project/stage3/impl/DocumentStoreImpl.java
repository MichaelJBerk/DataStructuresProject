package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.*;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.io.*;
import edu.yu.cs.com1320.project.Undoable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>(5);

    private StackImpl<Undoable> cmdStack = new StackImpl<Undoable>();

    public DocumentStoreImpl() {
    }
    
    private void addCMDToStack(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl currentVal = hashTable.get(uri);
        

        Function<URI, Boolean> func = docURI -> {
            boolean returnValue = false;
            DocumentImpl oldDoc = currentVal;
            DocumentImpl doc = hashTable.get(docURI);

            returnValue = this.delete(docURI);
            if (oldDoc != null) {
                this.hashTable.put(oldDoc.getKey(), oldDoc);
                setWordCountOfDoc(oldDoc);
            }
            
            return returnValue;
        };
        GenericCommand newCMD = new GenericCommand<URI>(uri, func);

        cmdStack.push(newCMD);
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        addCMDToStack(input, uri, format);
        
     
        if (input == null) {
            if (hashTable.get(uri) != null) {
                int hash = hashTable.get(uri).hashCode();
                deleteDocument(uri);
                return hash;
            } else {
                return 0;
            }
        }
        
       
        return addDocToHashTable(input, uri, format);
    }

    private int addDocToHashTable (InputStream input, URI uri, DocumentFormat format)  {
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
                hash = addTXTToHashTable(s, uri);
            } else if (format == format.PDF) {
                hash = addPDFToHashTable(s, uri, docData);
            }
            if (oldDoc != null) {
                hash = oldDoc.getDocumentAsTxt().hashCode();
            }
        } catch (IOException e) {
        }
        return hash;
    }
    private int addTXTToHashTable(String s, URI uri) {
        int txtHash = s.hashCode();
        DocumentImpl cDoc = new DocumentImpl(uri, s, txtHash);
        hashTable.put(uri, cDoc);
        setWordCountOfDoc(cDoc);
        return cDoc.getDocumentAsTxt().hashCode();
    }
    private int addPDFToHashTable(String s, URI uri, byte[] docData) {
        String pdfText = PDFString(docData);
        int txtHash = pdfText.hashCode();
        DocumentImpl cDoc = new DocumentImpl(uri, pdfText, txtHash, docData);
        hashTable.put(uri, cDoc);
        setWordCountOfDoc(cDoc);
        return txtHash;
    }

    private TrieImpl docTrie = new TrieImpl();

    private void setWordCountOfDoc(DocumentImpl document) {

        String text = document.getDocumentAsTxt().toUpperCase();
        String[] strArray = text.split(" ");

        HashTableImpl<String, Integer> wordMap = new HashTableImpl<String, Integer>(256);
        for (String word: strArray) {
            //Setting up the HashMap of words

            //Using some regex to remove non-alphanumeric chars
            word = word.replaceAll("\\W", "");
            int occurances = 0;
            if (wordMap.get(word) != null) {
                occurances = wordMap.get(word);
            }
            if (occurances == 0) {
                wordMap.put(word, 1);
            } if (occurances > 0) {
                wordMap.put(word, null);
                wordMap.put(word, occurances + 1);
            }

        docTrie.put(word, document);
           

            
        }
        document.wordMap = wordMap;        

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
        }
        return null;
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if (hashTable.get(uri) != null) {
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
    //used for deleting by URI
    private void deleteFromTrie( URI uri) {
        DocumentImpl doc = (DocumentImpl) getDocument(uri);
        if (doc != null) {
            // HashTable wordMap = doc.getWordMap();
            String[] strArray = doc.getDocumentAsTxt().split(" ");
            for (String word: strArray) {
                // docTrie.put(word, null);
                docTrie.delete(word, doc);
                //deleteAllWithURI(word, uri);

            }
        }

    }
    private boolean delete(URI uri) {
        Document d = getDocument(uri);
        boolean returnValue = false;
        if (hashTable.get(uri) != null) {
            returnValue = true;
        }
        if (d != null) {
            deleteFromTrie(uri);
            hashTable.put(uri, null);
        }
        return returnValue;
    }

    @Override
    public boolean deleteDocument(URI uri) {
        Document d = getDocument(uri);
        Function<Document, Boolean> func = doc -> {
            // boolean lambdaReturnValue = true;
            // DocumentImpl doc = ;
            // if (doc == null) {
            //     lambdaReturnValue = false;
            // }
            DocumentImpl di = (DocumentImpl) doc;
            boolean lambdaReturnValue;
            if (di != null) {
                this.hashTable.put(doc.getKey(), di);
                setWordCountOfDoc(di);
                lambdaReturnValue = true;
            } else {
                lambdaReturnValue = false;

            }
            return lambdaReturnValue;
        };
        GenericCommand newCMD = new GenericCommand(d, func);
        cmdStack.push(newCMD);

        // boolean returnValue = false;
        // if (hashTable.get(uri) != null) {
        //     returnValue = true;
        // }
        // if (d != null) {
        //     deleteFromTrie(uri);
        //     hashTable.put(uri, null);
        // }
        return delete(uri);
    }

    @Override
    public void undo() throws IllegalStateException {
        Undoable topCMD = (Undoable) cmdStack.pop();
        boolean boo = topCMD.undo();
        if (!boo) {
            throw new IllegalStateException();
        }
    

    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        StackImpl<Undoable> newStack = new StackImpl<Undoable>();
        GenericCommand newCMD = (GenericCommand) cmdStack.peek();

        while (newCMD.getTarget() != uri) {
            newStack.push(newCMD);
            cmdStack.pop();
            newCMD = (GenericCommand) cmdStack.peek();
        }
        newCMD.undo();
        while (newStack.peek() != null) {
            newCMD = (GenericCommand) newStack.peek();
            cmdStack.push(newCMD);
            newStack.pop();
        }
    }

    /**
     * @return the Document object stored at that URI, or null if there is no such
     *         Document
     */
    protected Document getDocument(URI uri) {
        Document cDocument = hashTable.get(uri);
        return cDocument;
    }

    @Override
    public List<String> search(String keyword) {

        List<Document> searchDocs = searchDocuments(keyword);//docTrie.getAllSorted(keyword, docTrie.reversed());
        return docToStringList(searchDocs);
    }
    private List<Document> searchDocuments(String keyword) {
        return docTrie.getAllSorted(keyword, docTrie.reversed());
    }

    private List<Document> searchDocumentsPrefix(String keywordPrefix) {
        return docTrie.getAllWithPrefixSorted(keywordPrefix, docTrie.reversed());
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        // TODO Auto-generated method stub
        List<Document> searchDocs = searchDocuments(keyword);
        return docToPDFList(searchDocs);
    }
    private List docToStringList(List<Document> docs) {
        List<String> stringList = new LinkedList<>();

        docs.forEach(doc -> stringList.add(doc.getDocumentAsTxt()));
        return stringList;
    }

    private List docToPDFList(List<Document> docs) {
        List<byte[]>  pdfList = new LinkedList<byte[]>();
        for (Document d: docs) {
            pdfList.add(d.getDocumentAsPdf());
        }
        return pdfList; 
    }

    @Override
    public List<String> searchByPrefix(String keywordPrefix) {

        List<Document> searchDocs = searchDocumentsPrefix(keywordPrefix);//docTrie.getAllWithPrefixSorted(keywordPrefix, docTrie.reversed());
        return docToStringList(searchDocs);
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) {
        // TODO Auto-generated method stub

        List<Document> docList = docTrie.getAllWithPrefixSorted(keywordPrefix, docTrie.reversed());
        return docToPDFList(docList);
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        //Use the set given by deleteAll to know what was deleted and thus delete them in other places
        //Another idea: get a list by searching, then delete those the normal way
        List<Document> searchDocs = searchDocuments(keyword);
        HashSet<URI> uriSet = new HashSet<URI>(); 
        CommandSet cSet = new CommandSet<>();
        for (Document d: searchDocs) {
            Function<URI, Boolean> func = docURI -> {
                boolean lambdaReturnValue = true;
                DocumentImpl doc = (DocumentImpl) d;
                if (doc == null) {
                    lambdaReturnValue = false;
                }
                hashTable.put(docURI, doc);
                setWordCountOfDoc(doc);
                return lambdaReturnValue;
            };
            uriSet.add(d.getKey());
            GenericCommand cmd = new GenericCommand(d.getKey(), func);
            cSet.addCommand(cmd);
        }
        docTrie.deleteAll(keyword);
        cmdStack.push(cSet);
       

        return uriSet;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        // TODO Auto-generated method stub
        // Set docSet = docTrie.deleteAllWithPrefix(keywordPrefix);
        List<Document> searchDocs = searchDocumentsPrefix(keywordPrefix);
        HashSet<URI> uriSet = new HashSet<URI>(); 
        CommandSet cSet = new CommandSet<>();
        for (Document d: searchDocs) {
            Function<URI, Boolean> func = docURI -> {
                boolean lambdaReturnValue = true;
                DocumentImpl doc = (DocumentImpl) d;
                if (doc == null) {
                    lambdaReturnValue = false;
                }
                hashTable.put(docURI, doc);
                setWordCountOfDoc(doc);
                return lambdaReturnValue;
            };
            uriSet.add(d.getKey());
            GenericCommand cmd = new GenericCommand(d.getKey(), func);
            cSet.addCommand(cmd);
        }
        docTrie.deleteAllWithPrefix(keywordPrefix);
        cmdStack.push(cSet);
        return uriSet;



        // return null;
    }
    
}