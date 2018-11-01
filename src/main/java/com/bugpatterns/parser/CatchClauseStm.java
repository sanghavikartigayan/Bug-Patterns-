package com.bugpatterns.parser;

import java.io.File;
import java.io.IOException;
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
	public static HashMap<Integer, List<CatchClause>> CatchConsutiveMap1 = new HashMap<Integer, List<CatchClause>>();
	public static HashMap<Integer, List<CatchClause>> multiCatchwithTry = new HashMap<Integer, List<CatchClause>>();

	public void getTryStm(File projectDir) {
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			System.out.println(path);
			System.out.println(Strings.repeat("=", path.length()));
			try {
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
				System.out.println(); // empty line
			} catch (ParseException | IOException e) {

				new RuntimeException(e);
			}
		}).explore(projectDir);
	}

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		 String path =  System.getProperty("user.dir");
//		 System.out.println("path "+ path);
//		File projectDir = new File("C:\\Users\\ParmjitSingh\\Desktop\\BugPatterns\\Bug5validationcode");
//		getTryStm(projectDir);
//		getConsecutiveCatchDuplStms();
//
//	}

	public int getConsecutiveCatchDuplStms() {
		int dupStmsCount = 0;
		HashMap<Integer, List<CatchClause>> finalCatchBlock = new HashMap<>();

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
