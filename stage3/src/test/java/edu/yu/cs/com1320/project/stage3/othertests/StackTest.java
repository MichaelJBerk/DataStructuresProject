package edu.yu.cs.com1320.project.stage3.othertests;


import static org.junit.Assert.assertEquals;

import org.junit.*;
import edu.yu.cs.com1320.project.impl.*;

public class StackTest {
   
    StackImpl stack;

    String[] stackStrings = {"String1", "String2", "String3", "String4", "String5", "String6"};

    @Before 
    public void setupStacki() {
        stack = new StackImpl();
        for (int i = 0; i < 5; i++) {
            stack.push(stackStrings[i]);
        }
    }


    @Test 
    public void testWithStrings() {
        StackImpl newStack = new StackImpl();
        String cString = (String) stack.peek();
        while (cString != "String3") {
            newStack.push(cString);
            stack.pop();
            cString = (String) stack.peek();
        }
        System.out.println("newStack:");
        while(newStack.peek() != null) {
            System.out.println(newStack.peek());
            cString = (String) newStack.peek();
            stack.push(cString);
            newStack.pop();
        }
        System.out.println("hello");

        
    }

    @Test
    public void testPeekStacki() {
        assertEquals("String5", stack.peek());
    }

    @Test
    public void testPopStacki() {
        int s = stack.size();
        for (int i =4; i >= 0; i--) {
            assertEquals(stackStrings[i], stack.pop());
            s--;
            assertEquals(stack.size(), s);
        }
    }

    @Test
    public void testDoubleStacki() {
        stack.push(stackStrings[5]);
        for (int i = 5; i >= 0; i--) {
            assertEquals(stackStrings[i], stack.pop());
        }
    }

}