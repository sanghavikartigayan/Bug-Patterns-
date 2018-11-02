package com.bugpatterns.parser;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class CatchStmTest extends TestCase {
	
	protected CatchClauseStm testCatchClauseStm = new CatchClauseStm();
	protected String dirPath = null;
	
	protected void setUp() throws IOException {
		File dirs = new File("." );
		dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator+"resources"+File.separator; 
		
		
    }
	
	 public void testSize() {
	        assertEquals("Count of error", 10, testCatchClauseStm.getDupCatchStm(new File(dirPath)));
	    }


}
