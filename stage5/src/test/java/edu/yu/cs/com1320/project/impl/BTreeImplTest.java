package edu.yu.cs.com1320.project.impl;


import edu.yu.cs.com1320.project.TestHelpers;
import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;

public class BTreeImplTest<Key extends Comparable, Value> extends TestHelpers {
    BTreeImpl btree = null;

    public BTreeImplTest(BTreeImpl btree) {
        this.btree = btree;
    }

   


    public Object getWithoutReadingFromDisk(Key key) {
        return btree.get(key, false);
    }





}