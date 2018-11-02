package com.bugpatterns.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 
public class Parser {
 
	//static boolean check = false;
	static boolean equalsPresent = false;
	static boolean hashCodePresent = false;
	static boolean rt = true;
	
	//use ASTParse to parse string
	public static boolean parse(String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Parser.rt = true;
		
		final ArrayList<String> streamVars = new ArrayList<String>();
		final Map<Integer, String> comments = new HashMap<Integer, String>();
 
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);			
		
		for (Comment comment : (List<Comment>) cu.getCommentList()) {

		    comment.accept(new CommentParser(cu, str.split("\n"), comments));
		}
		
		cu.accept(new ASTVisitor() {
			
			Set<String> names = new HashSet<String>();
			Set<SimpleName> methodNames = new HashSet<SimpleName>();
			Set<SimpleName> invokedMethodNames=new HashSet<SimpleName>();
			
			HashMap<String, Integer> variablesDeclarations = new HashMap<String, Integer>();
			HashMap<String, String> functionReturnVals = new HashMap<String, String>();
			
			
			public boolean visit(ClassDefinition node){
				return true;
			}
 
			// Bug Pattern 3,4 and 6
			public boolean visit(Assignment node) {
				if(node.getLeftHandSide().getClass().getName().equals("SimpleName") == true) {
					SimpleName sn = (SimpleName) node.getLeftHandSide();
					if((node.getRightHandSide().getClass().getSimpleName().equals("BooleanLiteral") == true)) {
						variablesDeclarations.put(sn.toString(), cu.getLineNumber(sn.getStartPosition()));
					}
					else {
						if(variablesDeclarations.containsKey(sn.toString())) {
							variablesDeclarations.remove(sn.toString());
						}
					}
				}
				
				if(node.getRightHandSide().getClass().getName().equals("SimpleName") == true) {
					SimpleName sn = (SimpleName) node.getRightHandSide();
					if(functionReturnVals.containsKey(sn.toString())) {
						functionReturnVals.remove(sn.toString());
					}
				}
				return true;
			}
			
			// Bug Pattern 3,4
			public boolean visit(VariableDeclarationStatement node) {
				//if(node.getType() instanceof File) {
				if(node.getType().isSimpleType()) {
					SimpleType stp = (SimpleType) node.getType();
					if((stp.getName().toString().equals("FileReader")) ||
							(stp.getName().toString().equals("InputStream")) ||
							(stp.getName().toString().equals("OutputStream")) ||
							(stp.getName().toString().equals("FileInputStream")) ||
							(stp.getName().toString().equals("FileOutputStream")) ||
							(stp.getName().toString().equals("InputStreamReader")) ||
							(stp.getName().toString().equals("FileWriter")) ||
							(stp.getName().toString().equals("OutputStreamWriter")) ){
						streamVars.add(stp.getName().toString());
					}
				}
				return true;
			}
			
			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				names.add(name.getIdentifier());
				System.out.println("Declaration of '" + name + "' at line"
						+ cu.getLineNumber(name.getStartPosition()));
				variablesDeclarations.put(name.toString(), cu.getLineNumber(name.getStartPosition()));
				
				return true; // do not continue 
			}
			
			// Bug Pattern 1
			public boolean visit(MethodDeclaration method) {
				SimpleName name = method.getName();
				methodNames.add(name);
				if(name.toString().equals("equals"))
					for(Object e: method.modifiers())
						if(e.toString().equals("@Override")) {
							// "Found equals method"
							Parser.equalsPresent = true;
						}
				if(name.toString().equals("hashCode")) {
					for(Object e: method.modifiers())
						if(e.toString().equals("@Override")) {
							// "Found hashCode method"
							Parser.hashCodePresent = true;
						}
				}
				return true;
			}
			
			// Bug Pattern 7, 6
			public boolean visit(MethodInvocation method)
			{
				SimpleName name=method.getName();
				invokedMethodNames.add(name);
				for(SimpleName n: methodNames)
				{
					if(!invokedMethodNames.contains(n))
					{
						if(!isMainMethod(n))
						{
							System.out.println("Found Bug Pattern: Unused Methods");
							System.out.println("Line Number: "+(cu.getLineNumber(n.getStartPosition())) + " "+ n.toString() +" is not invoked anywhere within project");
						}
					}
				}
				
				ASTNode n = method.getParent();
				
				if(n.getClass().getSimpleName().toString().equals("Assignment")) {
					Assignment es = (Assignment) n;
					System.out.println(es.getLeftHandSide().toString());
					if(variablesDeclarations.containsKey(es.getLeftHandSide().toString())) {
						variablesDeclarations.remove(es.getLeftHandSide().toString());
					}
				}
				
				if(n.getClass().getSimpleName().toString().equals("VariableDeclarationFragment")){
					VariableDeclarationFragment es = (VariableDeclarationFragment) n;
					
					if(variablesDeclarations.containsKey(es.getName().toString())) {
						variablesDeclarations.remove(es.getName().toString());
					}
					
					//if(es.getName())
					functionReturnVals.put(es.getName().toString(), name.toString());
					if(es.getParent().getParent().getClass().getSimpleName().toString().equals("Block")) {
						Block b = (Block) es.getParent().getParent();
						if((b.getParent().getClass().getSimpleName().equals("WhileStatement")) ||
								(b.getParent().getClass().getSimpleName().equals("ForStatement")) ||
								(b.getParent().getClass().getSimpleName().equals("EnhancedForStatement"))) {
							List<Statement> statements = b.statements();
							for(int i = 0; i < statements.size(); i++) {
								Statement tmpSt = statements.get(i);
								String stateName = statements.get(i).getClass().getSimpleName().toString();
								if(stateName.equals("ExpressionStatement")) {
									ExpressionStatement es2 = (ExpressionStatement)statements.get(i);
									Iterator itr = functionReturnVals.keySet().iterator();
									while(itr.hasNext()) {
										String retVar = (String)itr.next();
										if(es2.toString().contains(retVar)) {
											functionReturnVals.remove(retVar);
											break;
										}
									}
								}
							}
							if(functionReturnVals.size() > 0) {
								System.out.println("Bug Pattern found:Unneeded computation in loops.");
								System.out.println("Expression: " + node.getExpression() + ", Line Number: " + cu.getLineNumber(node.getStartPosition()) + " Possibility that the loop contains unneeded computation");
								Parser.rt = false;
							}
						}
					}
					
				}
				
				return true;
			}
			public boolean isMainMethod(SimpleName n)
			{
				if (!"main".equals(n.toString())) {
				      return false;
				    }
				return true;
			}
			// Bug Pattern 2
			public boolean visit(InfixExpression node) {
				if(node.getOperator() == Operator.EQUALS || node.getOperator() == Operator.NOT_EQUALS) {
					if((! node.getRightOperand().toString().equals("null") && ! node.getRightOperand().equals("null")) && (node.getLeftOperand().getClass().getName() instanceof String) && (node.getRightOperand().getClass().getName() instanceof String)) {
						System.out.println("Bug Pattern found: ES_COMPARING_STRINGS_WITH_EQ");
						System.out.println("Line Number: " + (cu.getLineNumber(node.getStartPosition())) + " - Consider using the equals(Object) method instead");
					}
					Parser.rt = false;
				}
			    return true;
			}
 
			public boolean visit(SimpleName node) {
				if (this.names.contains(node.getIdentifier())) {
					System.out.println("Usage of '" + node + "' at line "
							+ cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}
			
			// Bug Pattern 3
			public boolean visit(TryStatement node) {
				Block b = node.getFinally();
				if(b == null) {
					return true;
				}
				List<Statement> stm = b.statements();
				
				Iterator it2 = stm.iterator();
				while(it2.hasNext()) {
					Iterator itr = streamVars.iterator();
					while(itr.hasNext()) {
						String streamVar = (String) itr.next();
						if(it2.toString().contains(streamVar + ".close")) {
							streamVars.remove(streamVar);
						}
					}
				}
				return true;				
			}
			
			public boolean visit(CatchClause node) {
				SingleVariableDeclaration sd=node.getException();
				//System.out.println(sd.getType()+"eh");
				checkOverCatchException(node.getBody(),sd);
				int startLineNumber = cu.getLineNumber(node.getStartPosition()) - 1;
		        int endLineNumber = cu.getLineNumber(node.getStartPosition() + node.getLength()) - 1;
		        parseSubTree(node.getBody(), startLineNumber);
		        checkUnfinishedExceptionHandling(startLineNumber, endLineNumber);
		        
				return true;				
			}
			
			public void checkUnfinishedExceptionHandling(int startLineNumber, int endLineNumber) {
				for(Integer i : comments.keySet()) {
					if(i >= startLineNumber && i <= endLineNumber) {
						if(comments.get(i).indexOf("TODO") >= 0 || comments.get(i).indexOf("FIXME") >= 0) {
							System.out.println("Line number: " + startLineNumber + " - Unfinished exception handling code: There's a TODO or a FIXME in the catch block");
						}
					}
				}
			}
			
			//Bug Pattern 10
			public void checkOverCatchException(Block node,SingleVariableDeclaration sd)
			{
				String exceptionType=sd.getType().toString();
				if(exceptionType.equals("Exception")||exceptionType.equals("RunTimeException")||exceptionType.equals(("Throwable")))
				{
					for(Object s:node.statements())
					{
						if(s instanceof ExpressionStatement) {
							ExpressionStatement e= (ExpressionStatement) s;
							String expression=e.getExpression().toString();
							if((expression.indexOf("System.exit")>=0))
							{
								System.out.println("Found Bug Pattern: Over-catching an exception with system-termination");
								System.out.println("Line Number: "+(cu.getLineNumber(node.getStartPosition())) + " Donot terminate system when catching very high level exceptions");
								Parser.rt = false;
							}
						}
					}
				}
			}
			
			public boolean parseSubTree(Block node, int startLineNumber) {
				boolean isEmptyException = true;
				for(Object s : node.statements()) {
					if(s instanceof ExpressionStatement) {						
						ExpressionStatement e = (ExpressionStatement) s;
						String expression = e.getExpression().toString();
						if((expression.indexOf("System.out.print") >= 0)) {
							isEmptyException = true;
							return true;
						}
						else if((expression.indexOf("printStackTrace") >= 0)) {
							isEmptyException = true;
							return true;
						}
					}
					else if(s instanceof ThrowStatement) {
						return true;
					}
					else {
						continue;
					}
				}
				if(isEmptyException) {
					System.out.println("Line number: " + startLineNumber + " - Empty Exception: There is no debug when an exception occurs");
				}	
				else {
					System.out.println("Debug present");
				}
				return true;
			}
			
			// Bug Pattern 4
			@Override
			public boolean visit(IfStatement node) {
				node.getExpression();
				System.out.println("If Statement " + ASTNode.BOOLEAN_LITERAL);
				if(node.getExpression().toString().equals("true")) {
					System.out.println("Error");
				}
				
				boolean condCheckFlag = false;
				if(node.getExpression().getClass().getSimpleName().toString().equals("InfixExpression")) {
					InfixExpression expr = (InfixExpression) node.getExpression();
					if(expr.getLeftOperand().getClass().getSimpleName().toString().equals("SimpleName")) {
						SimpleName sn = (SimpleName) expr.getLeftOperand();
						if(variablesDeclarations.containsKey(sn.toString()) == true) {
							condCheckFlag = true;
						}
					}
					if(condCheckFlag == true) {
						if(expr.getRightOperand().getClass().getSimpleName().toString().equals("SimpleName")) {
							SimpleName sn = (SimpleName) expr.getRightOperand();
							if(variablesDeclarations.containsKey(sn.toString()) == true) {
								System.out.println("Bug Pattern found: UC_USELESS_CONDITION");
								System.out.println("Expression: " + node.getExpression() + ", Line Number: " + cu.getLineNumber(node.getStartPosition()) + " - Possibility that this condition has no effect");
								Parser.rt = false;
								condCheckFlag = true;
							}
						}
					}
				}
				else if(node.getExpression().getClass().getSimpleName().equals("BooleanLiteral") == true) {
					System.out.println("Bug Pattern found: UC_USELESS_CONDITION");
					System.out.println("Expression: " + node.getExpression() + ", Line Number: " + cu.getLineNumber(node.getStartPosition()) + " - Possibility that this condition has no effect");
					Parser.rt = false;
				}
				else if(node.getExpression().getClass().getSimpleName().equals("PrefixExpression") == true) {
					PrefixExpression pe = (PrefixExpression) node.getExpression();
					if(pe.getOperand().getClass().getSimpleName().equals("SimpleName") == true) {
						SimpleName se = (SimpleName) pe.getOperand();
						if(variablesDeclarations.containsKey(se.toString())) {
							System.out.println("Bug Pattern found: UC_USELESS_CONDITION");
							System.out.println("Expression: " + node.getExpression() + ", Line Number: " + cu.getLineNumber(node.getStartPosition()) + " - Possibility that this condition has no effect");
							Parser.rt = false;
						}
					}
				}
				else {
					System.out.println(node.getExpression().getClass().getSimpleName());
				}
				
			    Statement thenBranch = node.getThenStatement(); 
			    if (thenBranch != null) {
			        thenBranch.accept(new ASTVisitor(false) {
			            @Override
			            public boolean visit(MethodInvocation node) {
			                // handle method invocation in the then branch
			                return true; // false, if nested method invocations should be ignored
			            }
			        });
			    }

			    Statement elseBranch = node.getElseStatement(); 
			    if (elseBranch != null) {
			        elseBranch.accept(new ASTVisitor(false) {
			            @Override
			            public boolean visit(MethodInvocation node) {
			                // handle method invocation in the else branch
			                return true; // false, if nested method invocations should be ignored
			            }
			        });
			    }

			    return true; // false, if nested if statements should be ignored
			}
		});
		
		if(!Parser.hashCodePresent && Parser.equalsPresent){
			System.out.println("Bug Pattern found: HE_EQUALS_NO_HASHCODE");
			System.out.println("HashCode method not found: The class may violate the invariant that equal objects must have equal hashcodes");
			Parser.rt = false;
		}
		else{
			System.out.println("1 ok");
		}
        
		// Bug Pattern 3
		if(streamVars.size() > 0) {
			System.out.println("Expression: " + node.getExpression() + ", Line Number: " + cu.getLineNumber(node.getStartPosition()) + "Possibility of stream left opened");
			Parser.rt = false;
		}
		
		return Parser.rt;
	}
   
	//read file content into a string
	public static String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			System.out.println("Num read");
			System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}
 
	//loop directory to get file list
	public static void ParseFilesInDir(String dir) throws IOException {
		File dirs = new File(dir != null ? dir : "." );
		String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator+"resources"+File.separator; 
		System.out.println(dirPath);
		File root = new File(dirPath);
//		CatchClauseStm catchClauseStm = new CatchClauseStm();
//		catchClauseStm.getTryStm(new File(dirPath));
//		catchClauseStm.getConsecutiveCatchDuplStms();
		//System.out.println(rootDir.listFiles());
		File[] files = root.listFiles ( );
		String filePath = null;

		 for (File f : files ) {
			 filePath = f.getAbsolutePath();
			 if(f.isFile()){
				 parse(readFileToString(filePath));
			 }
		 }
	}
 
	public static void main(String[] args) throws IOException {
		if(args != null) {
			if(args.length > 1) {
				ParseFilesInDir(args[0]);
			}
			else {
				ParseFilesInDir(null);
			}
		}
		else {
			ParseFilesInDir(null);
		}
	}
}
