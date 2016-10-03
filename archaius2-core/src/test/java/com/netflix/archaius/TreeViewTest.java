package com.netflix.archaius;

import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

public class TreeViewTest {
    @Test
    public void test() {
        SortedMap<String, Integer> values = new TreeMap<String, Integer>();
        values.put("a.a", 1);
        values.put("a.b", 2);
        values.put("bb", 10);
        values.put("b.a", 3);
        values.put("b.b", 4);
        values.put("b.c", 5);
        values.put("c.a", 1);
        values.put("c.b", 2);
        
        SortedMap<String, Integer> sub = values.subMap("b.", "b." + "\uffff");
        System.out.println(sub);
        
    }
}
