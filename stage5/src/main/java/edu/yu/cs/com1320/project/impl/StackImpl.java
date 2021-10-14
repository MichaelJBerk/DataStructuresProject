package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.*;

public class StackImpl<T> implements Stack {
    private Object[] stack = new Object[5];
    private int top = 0;

    public StackImpl() {
        
    }

    private int topIndex() {
        return top-1;
    }

    @Override
    public void push(Object element) {
        if (top == stack.length) {
            doubleStack();
        }
        stack[top] = element;
        top++;

    }

    @Override
    public Object pop() {
        if (size() == 0) {
            return null;
        }
        Object element = stack[topIndex()];
        stack[topIndex()] = null;
        top--;
        return element;
    }

    @Override
    public Object peek() {
        if (size() == 0) {
            return null;
        }
        Object element = stack[topIndex()];

        return element;
    }

    @Override
    public int size() {
        return top;
    }

    private void doubleStack() {
        Object[] newStack = new Object[stack.length * 2];
        for (int i = 0; i < stack.length; i++) {
            newStack[i] = stack[i];
        }
        this.stack = newStack;
    }

}