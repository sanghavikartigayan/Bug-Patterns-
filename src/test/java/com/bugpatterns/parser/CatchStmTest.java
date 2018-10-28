package com.bugpatterns.parser;

import junit.framework.TestCase;

public class CatchStmTest extends TestCase {
	
	protected CatchClauseStm testCatchClauseStm = new CatchClauseStm();
	
	protected void setUp() {
		testCatchClauseStm.main(null);
    }
	
	 public void testSize() {
	        assertEquals("Count of error", 4, testCatchClauseStm.getConsecutiveCatchDuplStms());
	    }


}
