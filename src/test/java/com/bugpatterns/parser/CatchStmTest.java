package com.bugpatterns.parser;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class CatchStmTest extends TestCase {
	
	protected CatchClauseStm testCatchClauseStm = new CatchClauseStm();
	
	protected void setUp() throws IOException {
		File dirs = new File("." );
		String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator+"resources"+File.separator; 
		CatchClauseStm catchClauseStm = new CatchClauseStm();
		catchClauseStm.getTryStm(new File(dirPath));
		catchClauseStm.getConsecutiveCatchDuplStms();
    }
	
	 public void testSize() {
	        assertEquals("Count of error", 4, testCatchClauseStm.getConsecutiveCatchDuplStms());
	    }


}
