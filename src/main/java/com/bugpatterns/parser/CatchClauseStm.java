package com.bugpatterns.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.bugpatterns.util.DirExplorer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;

public class CatchClauseStm {
	//Bug Pattern 5
	static int errorcount = 0;
	public static int getDupCatchStm(File projectDir) {
		
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			System.out.println(path);
			System.out.println(Strings.repeat("=", path.length()));
			try {
				HashMap<Integer, List<CatchClause>> CatchConsutiveMap1 = new HashMap<Integer, List<CatchClause>>();
				int errorcountOneFile =0;
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(TryStmt n, final Object arg) { // TryStmt

						n.getTryBlock().accept(this, arg);
						if (n.getCatchs() != null) {
							for (CatchClause c : n.getCatchs()) {
								super.visit(n, arg);
								// System.out.println("catch statement ");
								CatchConsutiveMap1.put(n.getBeginLine(), n.getCatchs());

							}
						}
					}

				}.visit(JavaParser.parse(file), null);
				errorcountOneFile =getConsecutiveCatchDuplStms(CatchConsutiveMap1);
				errorcount = errorcountOneFile+errorcount;
				System.out.println(); // empty line
				
			} catch (ParseException | IOException e) {

				new RuntimeException(e);
			}
		}).explore(projectDir);
		return errorcount;
	}

	/*public static void main(String[] args) {
//		// TODO Auto-generated method stub
		 String path =  System.getProperty("user.dir");
	 System.out.println("path "+ path);
	File projectDir = new File("C:\\Users\\iNdZ\\Documents\\workspace\\Misc-workspace\\BugPatterns-master\\src\\resources");
	getTryStm(projectDir);
		

}*/

	public static int getConsecutiveCatchDuplStms(HashMap<Integer, List<CatchClause>> CatchConsutiveMap1) {
		int dupStmsCount = 0;	
		
		//remove empty statment 
		for (Entry<Integer, List<CatchClause>> entry1 : CatchConsutiveMap1.entrySet()) {
			//----
			if(entry1.getValue().size()>1) {
				
				//System.out.println("values: "+entry1.getKey()  + " work "+entry1.getValue());
				
				List<CatchClause> lstCatch = new ArrayList<CatchClause>();
				lstCatch = entry1.getValue();
				
				//System.out.println("get list value : "+lstCatch);
				
				for (int i = 0; i < lstCatch.size(); i++) {
					
					CatchClause testCatch = lstCatch.get(i);
					List<Statement> statements = new ArrayList<Statement>();
					statements = testCatch.getCatchBlock().getStmts();
					
					if(statements == null) {
						//System.out.println("Null Statement");
						entry1.getValue().remove(i);
					}
					else {
						//System.out.println("NOT Null Statement "+testCatch);
						
					}
				}
			}
		}
		HashMap<Integer, List<CatchClause>> multiCatchwithTry = new HashMap<Integer, List<CatchClause>>();
		for (Entry<Integer, List<CatchClause>> entry1 : CatchConsutiveMap1.entrySet()) {
			
			if (entry1.getValue().size() > 1) {
				// System.out.println("Campare map :::: "+CatchConsutiveMap1);	
				int size = entry1.getValue().size();	
				
				multiCatchwithTry.put(entry1.getKey(), entry1.getValue());	
				
			}
		}
		// System.out.println("************************Final Catches******************
		// \n"+multiCatchwithTry);

		for (Entry<Integer, List<CatchClause>> entry2 : multiCatchwithTry.entrySet()) {

			BlockStmt singleCatch1 = null;
			BlockStmt singleCatch2 = null;
			int listSize = entry2.getValue().size();

			System.out.println();

			for (int i = 0; i <= listSize - 1; i++) {
				String stmCatch1 = "";
				String stmCatch2 = "";
				singleCatch1 = entry2.getValue().get(i).getCatchBlock();

				Iterator<Statement> itBST1 = singleCatch1.getStmts().iterator();
				while (itBST1.hasNext()) {

					stmCatch1 = itBST1.next().toString();
					// System.out.println("string stmCatch1 " + stmCatch1);

					for (int k = i + 1; k < listSize; k++) {
						singleCatch2 = entry2.getValue().get(k).getCatchBlock();
						Iterator<Statement> itBST2 = singleCatch2.getStmts().iterator();
						while (itBST2.hasNext()) {
							stmCatch2 = itBST2.next().toString();
							// System.out.println("string stmCatch2 " + stmCatch2);
							if (stmCatch1.equals(stmCatch2)) {
								System.out.println(
										"Inadequate logging information on  line-" + singleCatch2.getBeginLine() + "!");
								System.out.println("The description is " + stmCatch2);
								dupStmsCount = dupStmsCount + 1;

							}
							

						}
						

					}
				}

			}

		}

		return dupStmsCount;
	}

}
