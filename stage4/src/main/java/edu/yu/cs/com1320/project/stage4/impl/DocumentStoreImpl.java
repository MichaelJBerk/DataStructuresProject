package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.*;
import edu.yu.cs.com1320.project.*;

import java.util.Comparator;
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
import edu.yu.cs.com1320.project.impl.HashTableImpl;

public class DocumentStoreImpl implements DocumentStore {

    protected HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>(5);
    private StackImpl<Undoable> cmdStack = new StackImpl<Undoable>();
    protected int numberOfDocs;
    protected int numberOfBytes;
    protected int maxNumberOfDocs;
    protected int maxNumberOfBytes;
    protected MinHeapImpl heap = new MinHeapImpl();

    public DocumentStoreImpl() {
    }

    private void addCMDToStack(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl currentVal = hashTable.get(uri);

        Function<URI, Boolean> func = docURI -> {
            boolean returnValue = false;
            DocumentImpl oldDoc = currentVal;
            DocumentImpl doc = hashTable.get(docURI);

            returnValue = this.delete(docURI);
            if (input == null) {
                returnValue = true;
            }
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
                delete(uri);
                return hash;
            } else {
                return 0;
            }
        }

        return setUpDocForHashTable(input, uri, format);
    }

    private int setUpDocForHashTable(InputStream input, URI uri, DocumentFormat format) {
        int hash = 0;
        if (hashTable.get(uri) != null) {
            hash = hashTable.get(uri).hashCode();
        } else {
            hash = 0;
        }
        try {
            byte[] docData = new byte[input.available()];
            input.read(docData);
            String s = new String(docData);
            DocumentImpl oldDoc = hashTable.get(uri);
            DocumentImpl cDoc = null;
            if (format == format.TXT) {
                cDoc = makeTXTDoc(s, uri);
                addDoc(cDoc, uri);
                hash = cDoc.getDocumentAsTxt().hashCode();
            } else if (format == format.PDF) {
                cDoc = makePDFDoc(s, uri, docData);
                addDoc(cDoc, uri);
                hash = cDoc.getDocumentAsTxt().hashCode();
                // hash = PDFString(docData).hashCode();
            }
            if (oldDoc != null) {
                hash = oldDoc.getDocumentAsTxt().hashCode();
            }
        } catch (IOException e) {
        }
        return hash;
    }

    private DocumentImpl makeTXTDoc(String s, URI uri) {
        int txtHash = s.hashCode();
        DocumentImpl cDoc = new DocumentImpl(uri, s, txtHash);
        return cDoc;
    }
    

    private DocumentImpl makePDFDoc(String s, URI uri, byte[] docData) {
        String pdfText = PDFString(docData);
        int txtHash = pdfText.hashCode();
        DocumentImpl cDoc = new DocumentImpl(uri, pdfText, txtHash, docData);
        return cDoc;
    }
    /**
     * This is the final step to add the Document to the HashTable
    */
    private void addDoc(DocumentImpl doc, URI uri) {
        if (maxNumberOfDocs != 0) {
            while(numberOfDocs == maxNumberOfDocs) {
                Document oldD = (Document) heap.removeMin();
                delete(oldD.getKey());
                // List<Document> oldList = oldestDocs();
                // for (Document d: oldList) {
                //     // delete(d.getKey());
                //     if (numberOfDocs != maxNumberOfDocs) {
                //         break;
                //     }
                // }
            }
        }
        DocumentImpl di = (DocumentImpl) doc;
        int newBytes = totalDocumentBytes() + di.getDocumentSize(); 
        int cBytes = totalDocumentBytes();
        while(totalDocumentBytes() != 0 && maxNumberOfBytes != 0 &&  maxNumberOfBytes < newBytes) {
            Document oldD = (Document) heap.removeMin();
            delete(oldD.getKey());
            if (totalDocumentBytes() <= maxNumberOfBytes) {
                break;
            }
        }
        hashTable.put(uri, doc);
        setWordCountOfDoc(doc);
        numberOfDocs++;
        heap.insert(doc);
        numberOfBytes = numberOfBytes + doc.getDocumentSize();
    }

    private TrieImpl docTrie = new TrieImpl();

    private void setWordCountOfDoc(DocumentImpl document) {

        String text = document.getDocumentAsTxt().toUpperCase();
        String[] strArray = text.split(" ");

        HashTableImpl<String, Integer> wordMap = new HashTableImpl<String, Integer>(256);
        for (String word : strArray) {
            // Setting up the HashMap of words

            // Using some basic regex to remove non-alphanumeric chars, because it's just much eaiser this way
            word = word.replaceAll("\\W", "");
            int occurances = 0;
            if (wordMap.get(word) != null) {
                occurances = wordMap.get(word);
            }
            if (occurances == 0) {
                wordMap.put(word, 1);
            }
            if (occurances > 0) {
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

    // used for deleting by URI
    private void deleteFromTrie(URI uri) {
        DocumentImpl doc = (DocumentImpl) getDocument(uri);
        if (doc != null) {
            // HashTable wordMap = doc.getWordMap();
            String[] strArray = doc.getDocumentAsTxt().split(" ");
            for (String word : strArray) {
                docTrie.delete(word, doc);
            }
        }

    }

    private boolean delete(URI uri) {
        Document d = getDocument(uri);
        boolean returnValue = false;
        DocumentImpl di = hashTable.get(uri);
        if (hashTable.get(uri) != null) {
            returnValue = true;
        }
        
        if (d != null) {
            deleteFromTrie(uri);
            d.setLastUseTime(Integer.MAX_VALUE);
            heap.removeMin();
            hashTable.put(uri, null);
            numberOfDocs--;
            numberOfBytes = numberOfBytes - ((DocumentImpl) d ).getDocumentSize();
        }
        return returnValue;
    }

    @Override
    public boolean deleteDocument(URI uri) {
        Document d = getDocument(uri);
        Function<Document, Boolean> fn = doc -> {

            DocumentImpl di = (DocumentImpl) doc;
            boolean lambdaReturnValue;
            if (di != null) {
                addDoc(di, uri);
                lambdaReturnValue = true;
            } else {
                lambdaReturnValue = false;

            }
            return lambdaReturnValue;
        };
        GenericCommand newCMD = new GenericCommand(d, fn);
        cmdStack.push(newCMD);
        return delete(uri);
    }

    @Override
    public void undo() throws IllegalStateException {
        Undoable topCMD = (Undoable) cmdStack.pop();
        if (topCMD != null) {
            boolean boo = topCMD.undo();
            if (!boo ) {
                throw new IllegalStateException();
            }
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
        if (cDocument != null) {
            cDocument.setLastUseTime(java.lang.System.nanoTime());
        }

        return cDocument;
    }

    @Override
    public List<String> search(String keyword) {

        List<Document> searchDocs = searchDocuments(keyword);// docTrie.getAllSorted(keyword, docTrie.reversed());
        return docToStringList(searchDocs);
    }

    protected List<Document> searchDocuments(String keyword) {
        List<Document> docs = docTrie.getAllSorted(keyword, docTrie.reversed());
        docs = setLastUsedTimeOnList(docs);
        // long ctime = java.lang.System.nanoTime();
        // for (Document d: docs) {
        //     d.setLastUseTime(ctime);
        // }
        return docs;
    }

    private List<Document> setLastUsedTimeOnList(List<Document> docs) {
        long ctime = java.lang.System.nanoTime();
        for (Document d: docs) {
            d.setLastUseTime(ctime);
        }
        return docs;
    }

    protected List<Document> searchDocumentsPrefix(String keywordPrefix) {
        List<Document> docs = docTrie.getAllWithPrefixSorted(keywordPrefix, docTrie.reversed());
        docs = setLastUsedTimeOnList(docs);
        return docs;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        List<Document> searchDocs = searchDocuments(keyword);
        return docToPDFList(searchDocs);
    }

    /*
    * Converts a List of Documents to a List of Strings
    */
    protected List<String> docToStringList(List<Document> docs) {
        List<String> stringList = new LinkedList<>();

        docs.forEach(doc -> {
            doc.setLastUseTime(java.lang.System.nanoTime());
            stringList.add(doc.getDocumentAsTxt());
        });
        return stringList;
    }

    protected List docToPDFList(List<Document> docs) {
        List<byte[]> pdfList = new LinkedList<byte[]>();
        for (Document d : docs) {
            d.setLastUseTime(java.lang.System.nanoTime());
            pdfList.add(d.getDocumentAsPdf());
        }
        return pdfList;
    }

    @Override
    public List<String> searchByPrefix(String keywordPrefix) {

        List<Document> searchDocs = searchDocumentsPrefix(keywordPrefix);
        return docToStringList(searchDocs);
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) {

        List<Document> docList = docTrie.getAllWithPrefixSorted(keywordPrefix, docTrie.reversed());
        return docToPDFList(docList);
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        // Use the set given by deleteAll to know what was deleted and thus delete them
        // in other places
        // Another idea: get a list by searching, then delete those the normal way
        List<Document> searchDocs = searchDocuments(keyword);
        HashSet<URI> uriSet = new HashSet<URI>();
        CommandSet cSet = new CommandSet<>();
        long cTime = java.lang.System.nanoTime();
        for (Document d : searchDocs) {
            Function<URI, Boolean> func = docURI -> {
                boolean lambdaReturnValue = true;
                DocumentImpl doc = (DocumentImpl) d;
                if (doc == null) {
                    lambdaReturnValue = false;
                }
                
                
                hashTable.put(docURI, doc);
                setWordCountOfDoc(doc);
                doc.setLastUseTime(cTime);
                
                return lambdaReturnValue;
            };
            
            uriSet.add(d.getKey());
            GenericCommand cmd = new GenericCommand(d.getKey(), func);
            cSet.addCommand(cmd);
        }
        for (URI uri: uriSet) {
            delete(uri);
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
        long cTime = java.lang.System.nanoTime();
        for (Document d : searchDocs) {
            Function<URI, Boolean> func = docURI -> {
                
                boolean lambdaReturnValue = true;
                DocumentImpl doc = (DocumentImpl) d;
                if (doc == null) {
                    lambdaReturnValue = false;
                }
                hashTable.put(docURI, doc);
                setWordCountOfDoc(doc);
                doc.setLastUseTime(cTime);
                return lambdaReturnValue;
            };
            uriSet.add(d.getKey());
            GenericCommand cmd = new GenericCommand(d.getKey(), func);
            cSet.addCommand(cmd);
        }
        docTrie.deleteAllWithPrefix(keywordPrefix);
        cmdStack.push(cSet);
        return uriSet;

    }

    

    @Override
    public void setMaxDocumentCount(int limit) {
        maxNumberOfDocs = limit;

    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        maxNumberOfBytes = limit;
    }

    protected int totalDocumentBytes() {
        List<Document> docs = allDocs(null);
        int totalSize = 0;
        for (Document d: docs) {
            DocumentImpl di = (DocumentImpl) d;
            // System.out.println(di.getDocumentSize());
            totalSize += di.getDocumentSize();
        }
        return totalSize;
    }

    protected List<Document> allDocs(Comparator<Document> comparator) {
        Comparator<Document> docComp;
        if (comparator == null) {
            docComp = docTrie.reversed();
        } else {
            docComp = comparator.reversed();
        }

        List<Document> docs = docTrie.getAllWithPrefixSorted("", docComp.reversed());
        return docs;
    }

    protected List<Document> oldestDocs() {

        Comparator<Document> docComp = Comparator.comparing(Document::getLastUseTime);

        List<Document> docs = docTrie.getAllWithPrefixSorted("", docComp.reversed());

        return docs;
    }

    protected Document oldestDoc() {
        List<Document> docs = oldestDocs();
        if (docs.size() > 0) {
            return docs.get(0);
        } else {
            return null;
        }
    }

}