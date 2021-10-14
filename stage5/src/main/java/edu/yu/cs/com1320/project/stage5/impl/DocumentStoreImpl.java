package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.*;

import java.util.Comparator;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import java.util.HashMap;
import java.util.NoSuchElementException;

import java.io.*;
import java.io.File;
import edu.yu.cs.com1320.project.Undoable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


public class DocumentStoreImpl implements DocumentStore {
    protected class DocTrackerHelper implements TrackerHelper {
        protected mhitwo heap;
        protected DocumentStoreImpl docStore;

        protected void setHeap (mhitwo heap) {
            this.heap = heap;
        }


        @Override
        public Comparable getLastUsedKey() {
            Comparable minElement = null;
            int dbytes = docStore.maxNumberOfBytes;
            try {
                minElement = heap.getMin();
            } catch (Exception e) {
                return null;
            }
           
           if (minElement instanceof DocumentURI) {
               minElement = ((DocumentURI) minElement).uri;
           }
           return minElement;
        }

        @Override
        public boolean isOverDocLimit(int docsToAdd) {
            if (maxNumberOfDocs == 0) {
                return false;
            }
            return docStore.numberOfDocs() + docsToAdd > docStore.maxNumberOfDocs;
        }

        @Override
        public boolean isOverByteLimit(Object objectToAdd) {
            if (objectToAdd instanceof Document && maxNumberOfBytes != 0) {
                DocumentImpl di = (DocumentImpl) objectToAdd;
                int bytesToAdd = di.getDocumentSize();
                return docStore.totalDocumentBytes() + bytesToAdd > docStore.maxNumberOfBytes;
            }
            return false;
            
        }

        @Override
        public void writeToDisk(Object object) {
            if (object instanceof Document) {
                Document document = (Document) object;
                removeFromTracker(document);
                docStore.writeDocToDisk(document);
            }
            
        }

        @Override
        public void readFromDisk(Object object) {
            Document document = (Document) object;
            DocumentURI docURI = docStore.uriToDocURI(document.getKey());
            docStore.heap.insert(docURI);
        }

        @Override
        public void removeFromTracker (Object object) {
            if (object instanceof Document) {
                deleteFromHeap((Document) object);
            }
        }
        protected void deleteFromHeap(Document doc) {
            doc.setLastUseTime((long) Integer.MAX_VALUE);
            if (heap.getCount() != 0) {
                heap.removeMin();
            }
        }

    }

    protected class DocumentBTree extends BTreeImpl<URI, DocumentImpl> {
        protected void setTrackerHelper(TrackerHelper th) {
            this.tracker = th;
        }

        protected DocumentImpl getDocument(URI key, boolean readFromDisk) {
            return this.get(key, readFromDisk);
        }

        protected void deleteDocument(URI uri) {
            this.delete(uri);

        }
    }

    protected class DocumentURI implements Comparable {
        protected URI uri;
        protected DocumentBTree btree;
        protected DocumentURI(URI uri, DocumentBTree btree) {
            this.uri = uri;
            this.btree = btree;
        }



        @Override
        public int compareTo(Object o) {
            int returnInt = 0;
            if (o != null) {
                try {
                    DocumentURI uriToCompareTo = (DocumentURI) o;
                    DocumentImpl docToCompareTo = (DocumentImpl) getFromBTreeWithoutPut(uriToCompareTo.uri);
                    DocumentImpl thisDoc = (DocumentImpl) getFromBTreeWithoutPut(this.uri);

                    // DocumentImpl docToCompareTo = bTree.get(uriToCompareTo.uri);
                    // DocumentImpl thisDoc = (DocumentImpl) btree.get(this.uri);
                    returnInt = thisDoc.compareTo(docToCompareTo);
                } catch (NullPointerException e) {
                    return 0;
                }
                
            }

            return returnInt;
        }

        private Document getFromBTreeWithoutPut(URI uri) {
            Document cDoc = null;
            cDoc = pm.readFileAsDocument(uri);
            if (cDoc == null) {
                cDoc = btree.getDocument(uri, false);
            }

            return cDoc;
        }

        @Override
        public boolean equals(Object obj) {
            String thisSTR = this.uri.toString();
            if (obj instanceof DocumentURI) {
                DocumentURI otherCompURI = (DocumentURI) obj;
                String otherSTR = otherCompURI.uri.toString();
                return thisSTR.equals(otherSTR);
            } 
            return false;
        }

    }


    protected class mhitwo extends MinHeapImpl {
        //TODO: See if can be removed

        protected DocumentBTree btree;
        protected void setBtree(DocumentBTree btree) {
            this.btree = btree;
        } 

        protected int getCount() {
            return this.count;
        }

        protected int getSize() {
            int size = 0;
            for (int i = 0; i < this.elements.length; i++) {
                Comparable cElement = elements[i];
                if (cElement instanceof DocumentURI) {
                    DocumentURI docURI = (DocumentURI) cElement;
                    DocumentImpl cDoc = (DocumentImpl) bTree.getDocument(docURI.uri, false);
                    if (cDoc != null) {
                        size += cDoc.getDocumentSize();
                    } 
                }
            }
            return size;
        }

        protected Comparable getMin() {
            if (isEmpty())
            {
                throw new NoSuchElementException("Heap is empty");
            }
            return this.elements[1];
        }
    }

    protected class DocumentTrie extends TrieImpl {
        protected void setTracker(TrackerHelper tracker) {
            this.tracker = tracker;
        }
    }


    private StackImpl<Undoable> cmdStack = new StackImpl<Undoable>();
    protected int numberOfBytes;
    protected int maxNumberOfDocs;
    protected int maxNumberOfBytes;
    protected mhitwo heap = new mhitwo();
    protected DocumentPersistenceManager pm;
    //TODO: See if it should be generic
    protected DocumentBTree bTree = new DocumentBTree();
    protected DocTrackerHelper tracker;
    protected DocumentTrie docTrie = new DocumentTrie();

    public DocumentStoreImpl() {
        heap.setBtree(this.bTree);
        tracker = new DocTrackerHelper();
        tracker.docStore = this;
        tracker.heap = heap;


        //TODO: Allow for custom base dir
        File baseDir = new File(System.getProperty("user.dir"));
        pm = new DocumentPersistenceManager(baseDir);
        bTree.setPersistenceManager(pm);
        bTree.setTrackerHelper(tracker);
        docTrie.setTracker(tracker);

    }



    private void  addCMDToStack(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl currentVal = bTree.get(uri);

        Function<URI, Boolean> func = docURI -> {
            boolean returnValue = false;
            DocumentImpl oldDoc = currentVal;
            //It's pretty safe to assume that whenever this method is called, the document is NOT being read from disk
            DocumentImpl doc = bTree.getDocument(docURI, false);

            returnValue = this.delete(docURI);
            if (input == null) {
                returnValue = true;
            }
            if (oldDoc != null) {
                bTree.put(oldDoc.getKey(), oldDoc);
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

                if (bTree.get(uri) != null) {
                int hash = bTree.get(uri).hashCode();
                delete(uri);
                return hash;
            } else {
                return 0;
            }
        }

        return setUpDocForBTree(input, uri, format);
    }

    private int setUpDocForBTree(InputStream input, URI uri, DocumentFormat format) {
        int hash = 0;
        if (bTree.get(uri) != null) {
            hash = bTree.get(uri).hashCode();
        } else {
            hash = 0;
        }
        try {
            byte[] docData = new byte[input.available()];
            input.read(docData);
            String s = new String(docData);
            DocumentImpl oldDoc = bTree.get(uri);
            DocumentImpl cDoc = null;
            if (format == format.TXT) {
                cDoc = makeTXTDoc(s, uri);
                addDoc(cDoc, uri);
                hash = cDoc.getDocumentAsTxt().hashCode();
            } else if (format == format.PDF) {
                cDoc = makePDFDoc(s, uri, docData);
                addDoc(cDoc, uri);
                hash = cDoc.getDocumentAsTxt().hashCode();
            }
            if (oldDoc != null) {
                hash = oldDoc.getDocumentAsTxt().hashCode();
            }
        } catch (IOException e) {
        }
        return hash;
    }

    protected DocumentImpl makeTXTDoc(String s, URI uri) {
        int txtHash = s.hashCode();
        DocumentImpl cDoc = new DocumentImpl(uri, s, txtHash);
        return cDoc;
    }
    

    protected DocumentImpl makePDFDoc(String s, URI uri, byte[] docData) {
        String pdfText = PDFString(docData);
        int txtHash = pdfText.hashCode();
        DocumentImpl cDoc = new DocumentImpl(uri, pdfText, txtHash, docData);
        return cDoc;
    }

    private void writeDocToDisk(Document oldD) {
        try {
            //Calling delete(URI) before writing will remove it from the heap, and it won't be able get the Document to move it to disk.
            //Calling it after will delete the serialized file.
            // heap.removeMin();
            // bTree.moveToDisk(oldD.getKey());
            // numberOfBytes = numberOfBytes - ((DocumentImpl) oldD ).getDocumentSize();
            
            System.out.println("hey");
        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
    }

    /**
     * This is the final step to add the Document to the HashTable
    */
    private void addDoc(DocumentImpl doc, URI uri) {
        if (maxNumberOfDocs != 0) {
            int numD = numberOfDocs();
            while(numberOfDocs() == maxNumberOfDocs) {
                deleteOldestDocument();
            }
        }
        DocumentImpl di = (DocumentImpl) doc;
        int newBytes = totalDocumentBytes() + di.getDocumentSize(); 
        int cBytes = totalDocumentBytes();
        while(totalDocumentBytes() != 0 && maxNumberOfBytes != 0 &&  maxNumberOfBytes < newBytes) {
            // Document oldD = (Document) heap.removeMin();
            // delete(oldD.getKey());
            deleteOldestDocument();
            if (totalDocumentBytes() <= maxNumberOfBytes) {
                break;
            }
        }
        doc.setLastUseTime(java.lang.System.nanoTime());
        bTree.put(uri, doc);
        setWordCountOfDoc(doc);
        DocumentURI compareURI = new DocumentURI(uri, bTree);
        heap.insert(compareURI);
        heap.reHeapify(compareURI);
        numberOfBytes = numberOfBytes + doc.getDocumentSize();
    }

    private Document deleteOldestDocument() {
        DocumentURI compURI = (DocumentURI) heap.getMin();
        Document oldD = bTree.get(compURI.uri);
        
        // writeDocToDisk(oldD);
        try {
            bTree.moveToDisk(oldD.getKey());
        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
       
       
       
        return oldD;

    }

    private void setWordCountOfDoc(Document document) {

        String text = document.getDocumentAsTxt().toUpperCase();
        String[] strArray = text.split(" ");

        HashMap<String, Integer> wordMap = new HashMap<String, Integer>(256);

        for (String word : strArray) {
            // Setting up the HashMap of words

            // Using some basic regex to remove non-alphanumeric chars, because it's just much eaiser this way
            word = word.replaceAll("\\W", "");
            DocumentURI docURI = uriToDocURI(document.getKey());
            
            docTrie.put(word, docURI);
        }
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

        if (bTree.get(uri) != null) {
            DocumentImpl doc = bTree.get(uri);
            return doc.getDocumentAsPdf();
        }
        return null;

    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        // DocumentImpl doc = hashTable.get(uri);
        DocumentImpl doc = bTree.get(uri);
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
        deleteDocFromTrie(doc);
    }

    private void deleteDocFromTrie(Document doc) {
        if (doc != null) {
            String[] strArray = doc.getDocumentAsTxt().split(" ");
            for (String word : strArray) {
                docTrie.delete(word, doc);
            }
        }
    }

    
   

    protected boolean delete(URI uri) {
        Document d = bTree.get(uri);
        boolean returnValue = false;
        DocumentImpl di = bTree.get(uri);
        if (bTree.get(uri) != null) {
            returnValue = true;
        }
        
        if (d != null) {
            deleteFromTrie(uri);
            tracker.removeFromTracker(d);
            
            bTree.deleteDocument(uri);
            // numberOfDocs--;
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
        } else {
            throw new IllegalStateException();

        }

    }

    private void checkUndoURI(URI uri, Undoable undo) {

    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        StackImpl<Undoable> newStack = new StackImpl<Undoable>();
        
        boolean hasURI = false;
        GenericCommand currentCMD;
        Undoable newUndo = (Undoable) cmdStack.peek();
        Boolean undoIt = false;
        
        while(!hasURI) {
           
            if (newUndo == null) {
                throw new IllegalStateException();

            }
            if (newUndo instanceof CommandSet) {
                CommandSet cmdSet = (CommandSet) newUndo;
                hasURI = cmdSet.containsTarget(uri);
                if (hasURI) {
                    cmdSet.undo(uri);
                    break;
                }

            } else {
                currentCMD = (GenericCommand) cmdStack.peek();
                Object target = currentCMD.getTarget();
                if (target instanceof Document) {
                    Document cDoc = (Document) target;
                    hasURI = cDoc.getKey() == uri;
                } else {
                    if (target instanceof URI) {
                        URI cURI = (URI) target;
                        hasURI = cURI == uri;
                    }
                }
                
               
                if (hasURI) {
                    currentCMD.undo();
                    break;
                }
            }
            newStack.push(newUndo);
            cmdStack.pop();
            newUndo = (Undoable) cmdStack.peek();
        }
        
        while(newStack.peek() != null) {
            newUndo = (Undoable) newStack.peek();
            cmdStack.push(newUndo);
            newStack.pop();
        }
    }

    protected Document readAndGetDocument(URI uri) {
        return bTree.get(uri);
    }

    /**
     * @return the Document object stored at that URI, or null if there is no such
     *         Document
     */
    protected Document getDocument(URI uri) {

        Document cDocument = bTree.getDocument(uri, false);

        return cDocument;
    }

    @Override
    public List<String> search(String keyword) {

        List<Document> searchDocs = searchDocuments(keyword);
        return docToStringList(searchDocs);
    }

    protected List<Document> searchDocuments(String keyword) {
        List<Document> docs = docURIListToDocument(docTrie.getAllSorted(keyword, Comparator.reverseOrder()));

        docs = setLastUsedTimeOnList(docs);
        return docs;
    }

    /**
     * Convert a URI to a DocumentURI
     * @param uri
     * @return
     */
    protected DocumentURI uriToDocURI(URI uri) {
        DocumentURI newURI = new DocumentURI(uri, this.bTree);
        return newURI;
    }

    private List<Document> setLastUsedTimeOnList(List<Document> docs) {
        long ctime = java.lang.System.nanoTime();
        List<Document> newList = new LinkedList<>();
        for (Document d: docs) {
            // d.setLastUseTime(ctime);
            // DocumentImpl di = (DocumentImpl) d;
            // d = setLastUseTimeOnDocument(d);
            URI docURI = d.getKey();
            d = setLastUseTimeOnDocument(d, ctime);
            DocumentURI cURI = uriToDocURI(docURI);
            heap.reHeapify(cURI);
            newList.add(d);

        }
        return newList;
    }
    private Document setLastUseTimeOnDocument(Document doc) {
        // DocumentImpl di = (DocumentImpl) doc;

        // Document newDoc = (Document) di;
        Document newDoc = setLastUseTimeOnDocument(doc, java.lang.System.nanoTime());
        return newDoc;

    }
    private Document setLastUseTimeOnDocument(Document doc, Long time) {
        // DocumentImpl di = (DocumentImpl) doc;
        doc.setLastUseTime(time);
        // heap.reHeapify(di);
        // Document newDoc = (Document) di;
        return doc;

    }


    protected List<Document> searchDocumentsPrefix(String keywordPrefix) {
        List<Document> docs = docURIListToDocument(docTrie.getAllWithPrefixSorted(keywordPrefix, Comparator.reverseOrder()));
        docs = setLastUsedTimeOnList(docs);
        return docs;
    }

    private List<Document> docURIListToDocument(List list, boolean readFromDisk) {
        List<Document> docList = new LinkedList<>();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof DocumentURI) {
                DocumentURI nextURI = (DocumentURI) next;
                Document cDoc = bTree.getDocument(nextURI.uri, readFromDisk);
                if (cDoc != null) {
                    docList.add(cDoc);
                }
            }
        } 
        return docList;
    }

    //TODO: would be nicer to replace this with the method above
    protected List<Document> docURIListToDocument(List list) {
        return docURIListToDocument(list, true);
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
            doc = setLastUseTimeOnDocument(doc);
            // doc.setLastUseTime(java.lang.System.nanoTime());
            stringList.add(doc.getDocumentAsTxt());
        });
        return stringList;
    }

    protected List docToPDFList(List<Document> docs) {
        List<byte[]> pdfList = new LinkedList<byte[]>();
        for (Document d : docs) {
            // d.setLastUseTime(java.lang.System.nanoTime());
            d = setLastUseTimeOnDocument(d);
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

        List<Document> docList = docURIListToDocument(docTrie.getAllWithPrefixSorted(keywordPrefix, Comparator.reverseOrder()));
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
                bTree.put(docURI, doc);
                setWordCountOfDoc(doc);
                doc.setLastUseTime(cTime);
                // doc =  (DocumentImpl) setLastUseTimeOnDocument(doc);
                
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
                // hashTable.put(docURI, doc);
                bTree.put(docURI, doc);
                setWordCountOfDoc(doc);
                // doc = (DocumentImpl) setLastUseTimeOnDocument(doc);
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
    protected int numberOfDocs() {
        return heap.getCount();
    }

    protected int totalDocumentBytes() {
        // List<Document> docs = allDocs(null);
        // int totalSize = 0;
        // for (Document d: docs) {
        //     DocumentImpl di = (DocumentImpl) d;
        //     totalSize += di.getDocumentSize();
        // }
        // return totalSize;
        return heap.getSize();
    }

    protected List<Document> allDocs(Comparator<Document> comparator) {
        Comparator<Document> docComp;
        if (comparator == null) {
            docComp = Comparator.reverseOrder();
        } else {
            docComp = comparator.reversed();
        }

        List<Document> docs = docURIListToDocument(docTrie.getAllWithPrefixSorted("", Comparator.reverseOrder()), false);
        return docs;
    }

    protected List<Document> oldestDocs() {

        Comparator<Document> docComp = Comparator.comparing(Document::getLastUseTime);

        List<Document> docs = docURIListToDocument(docTrie.getAllWithPrefixSorted("", docComp.reversed()));

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