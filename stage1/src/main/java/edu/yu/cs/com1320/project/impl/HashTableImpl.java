package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

    class HashTableRow<Key, Value> {
        Key key;
        ValList value;
        HashTableRow(Key k, ValList v) {
            if (k == null || v == null) {
                throw new IllegalArgumentException();
            }
            key = k;
            value = v;
        }
    }

    private class ListElement {
        Key key;
        Value value;
        ListElement next;
        ListElement head;
    }

    private class ValList {
        ListElement head;

        private void put(Key k, Value v) {
            if (v == null) {
                remove(k);
            } else {
                ListElement element = new ListElement();
                element.value = v;
                element.key = k;
                
                if (this.head != null && this.head.key != k) {
                    ListElement cElement = this.head;
                    while (cElement.next != null && cElement.next.key != k) {
                        cElement = cElement.next;
                    }
                    cElement.next = element;
                    
                    


                } else {
                    this.head = element;
                    element.head = this.head;
                }
            }
        }

        private Value get(Key k) {
            ListElement cElement = this.head;
            if (cElement.key == null) {
                return null;
            }
            while (cElement.key != k) {
                if (cElement.next == null) {
                    return null;
                }
                cElement = cElement.next;
            }
            return cElement.value; 
        }   

        private void remove(Key k) {
            ListElement cElement = this.head;
            while (cElement.key != k) {
                cElement = cElement.next;
            }
            cElement.value = null;
            cElement.key = null;

        }
    }



    private HashTableRow<?, ?>[] table;

    public HashTableImpl() {
        this.table = new HashTableRow[5];
    }

    public HashTableImpl(int size) {
        this.table = new HashTableRow[size];
    }

    private int hashFunction(Object key) {
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }

    @Override
    public Value get(Key k) {
        if (k == null) {
            return null;
        }
        int i = this.hashFunction(k);
        HashTableRow cRow = this.table[i];
        if (cRow != null) {
            ValList list = (ValList)cRow.value;
            return list.get(k);
        }
        return null;
    }

    @Override
    public Value put(Key k, Value v) {
        int i = this.hashFunction(k);
        Value old = null;
        if (this.table[i] != null) {
            ValList ov = this.table[i].value;
            Value gotten = ov.get(k);
            if (gotten != null) {
                old = gotten;
            }
        }
        if (v == null ) {
            this.table[i] = null;
            return null;
        } 
        ValList list;
        if (this.table[i] != null) {
            list = this.table[i].value;
            list.put(k, v);
        } else {
            list = new ValList();
            list.put(k, v);
        }

        HashTableRow<Key, ValList> newRow = new HashTableRow<Key, ValList>(k, list);
        this.table[i] = newRow;
        if (old != null) {
            Value oldV = old;
            return oldV;
        } else {
            return null;
        }

    }

}