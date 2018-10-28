package com.bugpatterns.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
public class AppTest {
    @Test
    public void testMain() {
        String[] expected = new String[]{
            "Yeah! This is a simple", "Hello World!!" }; 
        String[] results = 
            AbstractMainTests.executeMain("com.bugpatterns.parser.Parser", null);
        Boolean isEmptyExceptionTest = false;
        Boolean unfinishedExceptionTest = false;
        for(Integer i = 0; i < results.length; i++) {
        	if(results[i].contains("Empty Exception")) {
        		isEmptyExceptionTest = true;
        	}
        	if(results[i].contains("Unfinished exception")) {
        		unfinishedExceptionTest = true;
        	}
        }
        assertEquals(isEmptyExceptionTest, true);
        assertEquals(unfinishedExceptionTest, true);
    }
}