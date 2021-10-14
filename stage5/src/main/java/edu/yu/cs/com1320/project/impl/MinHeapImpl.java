package edu.yu.cs.com1320.project.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl extends MinHeap  {

    public MinHeapImpl() {
        this.elements = new Comparable[1];
        this.elementsToArrayIndex = new HashMap<Comparable, Integer>();

    }

   
 
    @Override
    protected void swap(int i, int j) {
        Comparable iComp = elements[i];
        Comparable jComp = elements[j];
        super.swap(i, j);
        elementsToArrayIndex.put(jComp, i);
        elementsToArrayIndex.put(iComp, j);
    }



    @Override
    public void reHeapify(Comparable element) {
        // if (elementsToArrayIndex.get(element) == null) {
        //     return;
        // }
        boolean existsInArrayIndex = false;
        Iterator iter = elementsToArrayIndex.keySet().iterator();
        while (iter.hasNext()) {
            Object nextO = iter.next();
            if (element.equals(nextO)) {
                existsInArrayIndex = true;
            }
        }
        if (!existsInArrayIndex) {
            return;
        }
        int e = getArrayIndex(element);

        for (int i = 0; i < elements.length; i++) {
            
            Comparable eComp = this.elements[e];
            Comparable iComp = this.elements[i];
           if (this.isGreater(e, i)) {
            //    upHeap(e);
                if (eComp != null) {
                    downHeap(e);
                    if (i <=1) {
                        //    swap(e, i);
                    }
                }
            } else {
                if (iComp != null) {
                    upHeap(i);
                }
                // downHeap(e);
            }
        }
        System.out.println("gai(): " + getArrayIndex(element));


    }

    @Override
    protected int getArrayIndex(Comparable element) {
        // TODO Auto-generated method stub
        // Object o = elementsToArrayIndex.get(element);
        // Integer i = (Integer) elementsToArrayIndex.get(element);
        Integer returnInt = null;
        Iterator iter = elementsToArrayIndex.keySet().iterator();
        while (iter.hasNext()) {
            Object nextO = iter.next();
            if (element.equals(nextO)) {
                returnInt = (Integer) elementsToArrayIndex.get(nextO);

            }
        }
        



        return returnInt;
    }
    @Override 
    public void insert(Comparable x) {
        
        // TODO Auto-generated method stub
        super.insert(x);
        elementsToArrayIndex.put(x, count);
        reHeapify(x);
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