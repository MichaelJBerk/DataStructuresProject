package edu.yu.cs.com1320.project.impl;

import java.util.Arrays;
import java.util.HashMap;

import edu.yu.cs.com1320.project.MinHeap;
public class MinHeapImpl extends MinHeap  {

    public MinHeapImpl() {
        this.elements = new Comparable[1];
        this.elementsToArrayIndex = new HashMap<Comparable, Integer>();

    }
 
    @Override
    protected void swap(int i, int j) {
        // TODO Auto-generated method stub
        Comparable iComp = elements[i];
        Comparable jComp = elements[j];
        super.swap(i, j);
        elementsToArrayIndex.put(jComp, i);
        elementsToArrayIndex.put(iComp, j);
    }

    @Override
    public void reHeapify(Comparable element) {
        // TODO Auto-generated method stub
        
        
        for (int i = 0; i < elements.length; i++) {
            int e = getArrayIndex(element);
           if (this.isGreater(e, i)) {
            //    upHeap(e);
            downHeap(e);
               if (i <=1) {
                //    swap(e, i);
               }
            } else {
                upHeap(i);
                // downHeap(e);
            }
        }
        System.out.println("gai(): " + getArrayIndex(element));


    }

    @Override
    protected int getArrayIndex(Comparable element) {
        // TODO Auto-generated method stub
        Object o = elementsToArrayIndex.get(element);
        Integer i = (Integer) elementsToArrayIndex.get(element);
        return i;
    }
    @Override 
    public void insert(Comparable x) {
        // TODO Auto-generated method stub
        super.insert(x);
        elementsToArrayIndex.put(x, count);
    }

    @Override
    protected void doubleArraySize() {
        this.elements = Arrays.copyOf(this.elements, this.elements.length * 2);

    }

    @Override
    public Comparable removeMin() {
        // TODO Auto-generated method stub
        Comparable min = super.removeMin();
        elementsToArrayIndex.remove(min);
        return min;
    }

}