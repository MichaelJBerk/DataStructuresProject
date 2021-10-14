package edu.yu.cs.com1320.project.stage3;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import org.junit.Test;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test.
     */
    @Test
    public void testApp() {
        assertTrue(true);
    }

    private HashTable<String,String> table;

    @Before
    public void initTable(){
        this.table = new HashTableImpl<>();
        this.table.put("Key1", "Value1");
        this.table.put("Key2","Value2");
        this.table.put("Key3","Value3");
        this.table.put("Key4","Value4");
        this.table.put("Key5","Value5");
        this.table.put("Key6","Value6");
    }
    @Test
    public void testGet() {
        assertEquals("Value1",this.table.get("Key1"));
        assertEquals("Value2",this.table.get("Key2"));
        assertEquals("Value3",this.table.get("Key3"));
        assertEquals("Value4",this.table.get("Key4"));
        assertEquals("Value5",this.table.get("Key5"));
    }
    
}
