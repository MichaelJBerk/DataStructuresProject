package edu.yu.cs.com1320.project.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import java.util.Iterator;
import java.net.URI;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage4.*;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

public class TrieImpl implements Trie, Comparator<Document> {

    private int abcSize = 256;
    private Node root;

    private String cWord;

    public TrieImpl() {
        
    }

    private static class Node<Value>  {
        protected Value val;
        protected Node[] links = new Node[256];
    }

    @Override
    public void put(String key, Object val) {
        // TODO Auto-generated method stub
        //TODO see if this is what it should do
        if (val == null) {
            this.deleteAll(key);
        } else {
            this.root = put(this.root, key, val, 0);
        }
    }
    private Node put(Node x, String key, Object val, int d) {
        if (x == null || x.links == null) {
            x = new Node();
        }

        if (d == key.length()) {
            List<Document> listVal = new LinkedList<Document>();
            if (x.val != null) {
            listVal = (List<Document>) x.val;
        }
            listVal.add((Document) val);
            x.val = listVal;
            return x;
        }
        char c = key.charAt(d);
        
        // if (x.links[c] == null) {
        //     System.out.println( "hi");
        // }
        // if (x.links == null) {
        //     x.links = this.put(x, key, val, d)
        // }

        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    @Override
    public List getAllSorted(String key, Comparator comparator) {
        // TODO Auto-generated method stub
        key = key.toUpperCase();

        this.cWord = key;
        char[] ca = key.toCharArray();
        Node cNode = this.get(this.root, key, 0);
        if (cNode == null) {
            List<Document> list = new LinkedList<>();
            return list;
        }
        List<Document> docList = (List<Document>) cNode.val; //(List<Object>) Arrays.asList((Object)cNode.val);
        // Iterator docListI = docList.iterator();
        
        if (comparator != null && docList != null) {
            docList.sort((Comparator<Document>) comparator);
        }
        // docList.sort(comparator);
        return removeDupeDocs(docList);
    }
    private Node get(Node x, String key, int d) {
        if (x == null) {
            return null;
        } 
        if (key != null && d == key.length()) {
            return x;
        }

        char c = key.charAt(d);
        Node xl = null;
        if (x.links != null && x.links[c] != null) {
            xl = x.links[c];
        }
        return this.get(xl, key, d + 1);
    }
    private List<Document> docs = new LinkedList<>();

    private void getreclist(Node node) {
        if (node.val != null) {
            // docs.add((Document)node.val);
            List<Document> docList = (List<Document>) node.val;
            docList.forEach(doc -> docs.add(doc));
        }
        if (node.links != null) {
            for (Node n: node.links) {

                if (n != null) {
                    getreclist(n);
                }
            }
        }

    }

    @Override
    public List getAllWithPrefixSorted(String prefix, Comparator comparator) {
        // TODO Auto-generated method stub
        docs = new LinkedList<Document>();
        prefix = prefix.toUpperCase();
        this.cWord = prefix;
        List<Document> docList = new LinkedList<>();
        Node cNode = this.get(this.root, prefix, 0);
        if (cNode == null) {
            return docList;
        }
        getreclist(cNode);
        List<Document> d2 = docs;
        d2.sort((o1, o2) -> appearancesInDocArray(o1, o2));
        docs = null;
        return removeDupeDocs(d2);

    }
    /**
     * Returns the number of times a particular document appears in a list 
     * @param doc the document to search for
     * @param docs the list to search through
     * @return the number of times a particular document appears in a list 
     */
    private int occurancesInDocs(Document doc, List<Document> docs) {
        int i = 0;
        int occurrances = 0;
        while (i < docs.size()) {
            if (doc.getDocumentAsTxt().equals(docs.get(i).getDocumentAsTxt())) {
                occurrances++;
            }
            i++;
        }
        return occurrances;
    }
    /**
     * Removes duplicate instances of a document from a given list
     * @param docs a list of documents (with, i'd assume, at least one duplicate)
     * @return the list with only one instance per doc
     */
    private List<Document> removeDupeDocs(List<Document> docs) {
        List<Document> newDocList = new LinkedList<Document>();
        if (docs != null) {
                if (docs.size() > 1) {
                for (int i = 0; i < docs.size(); i++) {
                    Document cDoc = docs.get(i);

                    // if (cDoc.getDocumentAsTxt() != prevDoc.getDocumentAsTxt()) {
                    if (occurancesInDocs(cDoc, newDocList) == 0){
                        newDocList.add(cDoc);
                    }
                }
            } else {
                newDocList = docs;
            }
        }
        docs = null;
        return newDocList;
    }

    private int appearancesInDocArray(Document o1, Document o2) {
        int oo1 = 0;
        int oo2 = 0;
        for (Document d: docs) {
            if (o1 == d) {
                oo1++;
            }
            if (o2 == d) {
                oo2++;
            }
        }
        return Integer.compare(oo1, oo2);
    }

    @Override
    public Set deleteAllWithPrefix(String prefix) {
        // TODO Auto-generated method stub

        List docs = getAllWithPrefixSorted(prefix, null);
        Set uriSet = new HashSet<URI>();
        Document delVal = null;
        for (Object d: docs) {
            Document doc = (Document) d;
            // uriSet.add(
            this.root = (Node) deleteDocFromNode(doc, this.root);
            uriSet.add(doc.getKey());

        }
        cleanOutTrie();

        return uriSet;
    }

    @Override
    public Set deleteAll(String key) {
        // TODO Auto-generated method stub
        List<Document> docs = getAllSorted(key, reversed());
        HashSet<URI> uriSet = new HashSet<URI>();
        for (Document d: docs) {
            Document doc = (Document) d;
            this.root = (Node) deleteDocFromNode(doc, this.root);
            uriSet.add(doc.getKey());
        }
        // this.root = deleteAll(this.root, key, 0, null);
        cleanOutTrie();

        return uriSet;
    }

    private Node deleteAll(Node x, String key, int d, URI uri) {
        if (x == null) {
            return null;
        }

        
        if (key != null && d == key.length()) {
            DocumentImpl doc = (DocumentImpl) x.val;
            if (uri.equals(null)) {
                x.val = null;
            } else if (doc.getKey().equals(uri)) {
                x.val = null;
            }
        } else {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1, null);
        }
        if (x.val != null) {
            return x;
        }
        for (int c = 0; c < 256; c++) {
            if (x.links[c] != null) {
                return x;
            }
        }
        this.docs = null;
        return null;

    }

    @Override
    public Object delete(String key, Object val) {
        // TODO Auto-generated method stub
        DocumentImpl doc = (DocumentImpl) val;
        Node cNode = this.get(this.root, key, 0);
        Object newObj =  deleteDocFromNode(doc, this.root);
        Node newNode = null;
        if (newObj != null) {
            newNode = (Node) newObj;
        }
        this.root = newNode;

        cleanOutTrie();
        this.docs = new LinkedList<Document>();
        return val;
    }
    
    // Goes through the trie and removes empty lists 
    private void cleanOutTrie() {
        this.root = cleanOutTrieRec(this.root);
        if (this.root != null) {
            for (int i = 0; i < this.root.links.length; i++) {
                if (this.root.links[i] != null) {
                    Node n = this.root.links[i];
                    if (n != null && n.val == null && n.links == null) {
                        this.root.links[i] = null;
                    }
                }
            }
        }
        this.docs = null;
    }

    private boolean llIsEmpty(LinkedList list) {
        boolean empty = true;
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            if (iter.next() != null) {
                empty = false;
            }
        }
        return empty;
    }
    private void nullOutTrieRec(Node node) {
        // if (node != null) {
        //     if (node.val == null && node.links == null) {
        //         node = null;
        //         return;
        //     } else {
        //         if (node.links != null) {
        //             for (Node n: node.links) {
        //                 nullOutTrieRec(node);
        //             }
        //             return;
        //         }
        //     }
        // }
    }

    private Node cleanOutTrieRec(Node node) {
        if (node != null) {
           
            if (node.val != null) {
                if (node.val.getClass() == LinkedList.class) {
                    LinkedList nodeList = (LinkedList) node.val;
                    if (llIsEmpty(nodeList)) {
                        node.val = null;
                        if (node.links == null) {
                            node = null;
                            return null;
                        }
                    }
                }
            } 
        
            if (node.links != null) {
                boolean linksEmpty = true;
                // for (int i = 0; i < node.links.length; i ++) {
                //     if (node.links[i] != null) {
                //         node.links[i] = cleanOutTrieRec(node.links[i]);
                //         Node innerNode = node.links[i];
                //         if (innerNode != null) {
                //             linksEmpty = false;
                //         }
                //         if (linksEmpty) {
                //             node.links = null;
                //             if (node.val == null) {
                //                 node = null;
                //                 return null;
                //             }
                //         }

                //     }
                // }
                for (Node innerNode: node.links) {
                    innerNode = cleanOutTrieRec(innerNode);
                    if (innerNode != null) {
                        linksEmpty = false;
                    }
                }
                if (linksEmpty) {
                    node.links = null;
                    if (node.val == null) {
                        node = null;
                        return null;
                    }
                }
            }
            if (node.links == null && node.val == null) {
                return null;
            }
            return node;
        }
        return null;
    }

    private Object deleteDocFromNode(Document doc, Node node) {
        if (node != null) {
            if (node.val != null) {
                if (node.val.getClass() == LinkedList.class) {
                    LinkedList nodeList = (LinkedList) node.val;
                    Iterator iter = nodeList.iterator();
                    while (iter.hasNext()) {
                        Object n = iter.next();
                        if (n != null) {
                            if (n.getClass() == DocumentImpl.class) {
                                if (((DocumentImpl) n).getDocumentAsTxt().equals(doc.getDocumentAsTxt())) {
                                    // nodeList.remove(nodeList.indexOf(n));
                                    int i = nodeList.indexOf(n);
                                    nodeList.set(i, null);
                                }
                            } 
                        }
                    }
                    nodeList.removeIf(i -> i == null);
                    node.val = nodeList;
                }
            }

            if (node.links != null) { 
                for (Node node2: node.links) {
                    // if (((DocumentImpl) node2.val).getDocumentAsTxt().equals(doc.getDocumentAsTxt())) {
                    //     node2.val = null;
                    // }
                    node2 = (Node) deleteDocFromNode(doc, node2);
                }
            }
        }

        return node;
    }

    @Override
    public int compare(Document o1, Document o2) {
        // TODO Auto-generated method stub
        // if (o1.wordCount(cWord) > o2.wordCount(cWord)) {
        //     return 1;
        // }
        // if (o1.wordCount(cWord) == o2.wordCount(cWord)) {
        //     return 0;
        // }
        // if (o1.wordCount(cWord) < o2.wordCount(cWord)) {
        //     return -1;
        // }
        int wc1 = o1.wordCount(cWord);
        int wc2 = o2.wordCount(cWord);
        return Integer.compare(wc1, wc2);
        // if (o1.wordCount = )
        // return 0;
    }

}