package com.bugpatterns.parser;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
public class AppTest {
	/*
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
    */
	
	//Bug Pattern 1
    @Test
    public void testEqualsHashCodeMethod() {
    	String fileContent = "public class abc {\n" +
    			"@Override\n" +
    			"public void equals() {return;}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);
    }
//Bug pattern 1
@Test
    public void testEqualsHashCodeMethods() {
    	String fileContent = "public class abc {\n" +
    			"@Override\n" +
    			"public void equals() {return;}\n" +
    			"public void hashCode() {return;}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);
    }
//Bug pattern 2	
    @Test
    public void testStringEquals() {
    	String fileContent = "public class abc {\n" +
    			"public void fun() {\n" +
    				"if (\"a\" == \"a\") {return;}\n" +
    				"}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);	
    }
//Bug pattern 2
    @Test
    public void testStringEquals2() {
    	String fileContent = "public class abc {\n" +
    			"public void fun() {\n" +
    				"String a = \"a\"" +
    				"if (a == \"a\") {return;}\n" +
    				"}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);	
    }
    
	//Bug pattern 7
    @Test
    public void methodDeclaration() {
    	String fileContent = "public class abc {\n" +
    			"public static void main(String[] args) {System.out.println();}\n" +
    			"public void fun() {\n" +
    				"System.out.println();\n" +
    				"}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);
    }
    
	//Bug pattern 6
    @Test
    public void unneededComputation() {
    	String fileContent = "public class abc {\n" +
    			"public void main(String[] args) {\n" +
    				"int i = 2; int j = i + 3; j++;" +
    				"while(i<j) {int k = fun();}\n" +
    				"}\n" +
    			"public int fun() {return 3;}" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);	
    }
    
	//Bug pattern 4
    @Test
    public void conditionHasNoEffect() {
    	String fileContent = "public class abc {\n" +
    			"public void fun() {\n" +
    				"if(true) {return;}\n" +
    				"}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);	
    }
    // Bug pattern 3
    @Test
    public void failToCloseStream() {
    	String fileContent = "public class abc {\n" +
    			"public void fun() {" +
    				"File f = new File(\"\");\n" + 
    				"FileReader fr = new FileReader(f);\n" +
    				"}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);
    }
    //Bug pattern 10
    @Test
    public void catchSystemTermination() {
    	String fileContent = "public class abc {\n" +
    			"public void main(String[] args) {" +
    				"try{return;}\n" +
    				"catch(Exception e){System.out.println(\"Exception\");System.exit(0);}\n" +
    				"}\n" +
    			"}";
    	boolean result = true;
		try {
	    	result = Parser.parse(fileContent);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(result);
    }
}
