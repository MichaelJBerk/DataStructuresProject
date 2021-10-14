package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.TrackerHelper;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.util.Arrays;


//TODO: Implement Sentinal
public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {

    protected int BTreeMAX = 4;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode; //Sentinel?
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private PersistenceManager pm;
    protected TrackerHelper tracker;


    //B-tree node data type
    private class Node
    {
        private int entryCount; // number of entries
        private Entry<Value>[] entries = new Entry[4]; // the array of children
        private Node next;
        private Node previous;

        // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }

        private void setNext(Node next)
        {
            this.next = next;
        }
        private Node getNext()
        {
            return this.next;
        }
        private void setPrevious(Node previous)
        {
            this.previous = previous;
        }
        private Node getPrevious()
        {
            return this.previous;
        }

        private Entry[] getEntries()
        {
            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    private class Entry<Value>
    {
        private Key key;
        private Value val;
        private Node child;

        public Entry(Key key, Value val, Node child)
        {
            this.key = key;
            this.val = val;
            this.child = child;
        }
        public Value getValue()
        {
            return this.val;
        }
        public Key getKey()
        {
            return this.key;
        }
    }

    public BTreeImpl()
    {
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
    }
    


     /**
     * Returns the value associated with the given key.
     *
     * @param key the key
     * @return the value associated with the given key if the key is in the
     *         symbol table and {@code null} if the key is not in the symbol
     *         table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    @Override
    public Value get(Key key)
    {
        return get(key, true); 
    }
    //I'm splitting up this method like this so that I can test if something's in the BTree without having it read from the disk and load into memory.
    protected Value get (Key key, boolean readFromDisk) {
        if (key == null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }

        Entry entry = this.get(this.root, key, this.height);
        if(entry != null)
        {
            if (entry.val == null && readFromDisk) {
                
                Value val = readFromDisk(key);
                return val;
            }
            
            return (Value)entry.val;
        }
        return null;

    }
   

    private Entry get(Node currentNode, Key key, int height, Boolean remove)
    {
        Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0)
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                if(isEqual(key, entries[j].key))
                {
                    //found desired key. Return its value
                    Entry returnValue = entries[j];
                    //Stuff I did to really delete URI and push back entries to beginning of list
                    //Turns out, I don't need it. 
                    // if (remove) {
                    //     entries[j] = null;
                    //     //loop - push entries back to the beginning of the list
                    //     int k = j + 1;
                    //     while (k < entries.length) {
                    //         entries[k-1] = entries[k];
                    //         entries[k] = null;
                    //         k++;
                    //     }
                    //     currentNode.entryCount--;
                       
                    // }
                    return returnValue;
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
                {
                   
                    Entry returnEntry = this.get(entries[j].child, key, height - 1, remove);
                    return returnEntry;
                }
            }
            //didn't find the key
            return null;
        }
    }
    private Entry get(Node currentNode, Key key, int height) {
        return get(currentNode, key, height, false);
    }

/**
     * Inserts the key-value pair into the symbol table, overwriting the old
     * value with the new value if the key is already in the symbol table. If
     * the value is {@code null}, this effectively deletes the key from the
     * symbol table.
     *
     * @param key the key
     * @param val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    @Override
    public Value put(Key key, Value value) {
        return put(key, value, true);
    }

    protected Value put(Key key, Value val, boolean fromDisk)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("argument key to put() is null");
        }


        //if the key already exists in the b-tree, simply replace the value
        Entry alreadyThere = this.get(this.root, key, this.height);
        if(alreadyThere != null)
        {
            if (fromDisk) {
                Value read = readFromDisk(key);
                if (read != null) {
                    return read;
                } 
            }
            alreadyThere.val = val;
            return (Value) alreadyThere.val;
        }

        Node newNode = this.put(this.root, key, val, this.height);
        this.n++;
        if (newNode == null)
        {
            return null;
        }

        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return null;
        
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node put(Node currentNode, Key key, Value val, int height)
    {
        int j;
        Entry newEntry = new Entry(key, val, null);


        //external node
        if (height == 0)
        {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++)
            {
                if (less(key, currentNode.entries[j].key))
                {
                    break;
                }
            }
        }

        // internal node
        else
        {
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key))
                {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null)
                    {
                        return null;
                    }
                    if (newEntry == null) {
                        System.out.println("null");
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--)
        {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeMAX)
        {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else
        {
            //will have to create new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    /**
     * split node in half
     * @param currentNode
     * @return new node
     */
    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeMAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeMAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeMAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeMAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    /**
     *
     * @param key
     */
    protected void delete(Key key)
    { 
       
        
        
        try {
            //Deserializing will remove the file from disk
           pm.deserialize(key);
        }catch (NullPointerException e) {

        } catch (Exception e) {
            throw new Error(e.getMessage());
        }
        put(key, null, false);
        
    }

    protected boolean isOverLimits(Object object) {
        if (tracker == null) {
            return false;
        }
        return tracker.isOverDocLimit(1) || tracker.isOverByteLimit(object);

    }

    protected Value readFromDisk(Key k) {
        Value val = null;
        try {
            val = (Value) pm.deserialize(k);
            
        } catch (Exception e) {
            return null;
        }
        if (val != null) {
            boolean olim;
            if (tracker != null) {
                while (isOverLimits(val) ) {
                    olim = isOverLimits(val);
                    Key keyToRemove = (Key) tracker.getLastUsedKey();
                    Value valToRemove = this.get(keyToRemove, false);
                    tracker.writeToDisk(valToRemove);

                    if (!isOverLimits(val)) {
                        break;
                    }
                }
            }
            if (tracker != null) {
                tracker.readFromDisk(val);
            }
           
            this.put(k, val, false);
        } 
        return val;

    }

     // comparison functions - make Comparable instead of Key to avoid casts
     private static boolean less(Comparable k1, Comparable k2)
     {
         return k1.compareTo(k2) < 0;
     }
 
     private static boolean isEqual(Comparable k1, Comparable k2)
     {
         return k1.compareTo(k2) == 0;
     }


     @Override
     public void moveToDisk(Key k) throws Exception {

        
         Document d = (Document) get(k, false);
         if (tracker != null) {
            tracker.writeToDisk(d);
        }
        
         //TODO: have this delete the document once it's able to update the minHeap 
         this.delete(k);
         
         pm.serialize(k, d);
     }

     @Override
     public void setPersistenceManager(PersistenceManager pm) {
        this.pm = pm;

     }
    
}