package com.bugpatterns.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 
public class Parser {
 
	//static boolean check = false;
	static boolean equalsPresent = false;
	static boolean hashCodePresent = false;
	//use ASTParse to parse string
	public static void parse(String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		final Map<Integer, String> comments = new HashMap<Integer, String>();
 
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);			
		
		for (Comment comment : (List<Comment>) cu.getCommentList()) {

		    comment.accept(new CommentParser(cu, str.split("\n"), comments));
		}
		
		cu.accept(new ASTVisitor() {
			
			Set<String> names = new HashSet<String>();
			Set<SimpleName> methodNames = new HashSet<SimpleName>();
			
			public boolean visit(ClassDefinition node){
				return true;
			}
 
			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				names.add(name.getIdentifier());
				System.out.println("Declaration of '" + name + "' at line"
						+ cu.getLineNumber(name.getStartPosition()));
				return true; // do not continue 
			}
			
			// Bug Pattern 1
			public boolean visit(MethodDeclaration method) {
		        //IMethod iMethod = (IMethod) method.resolveBinding().getJavaElement();
				SimpleName name = method.getName();
				methodNames.add(name);
				//System.out.println(name+" "+method.modifiers());
				if(name.toString().equals("equals"))
					for(Object e: method.modifiers())
						if(e.toString().equals("@Override")) {
							System.out.println("Found equals method");
							Parser.equalsPresent = true;
						}
				if(name.toString().equals("hashCode")) {
					for(Object e: method.modifiers())
						if(e.toString().equals("@Override")) {
							System.out.println("Found hashCode method");
							Parser.hashCodePresent = true;
						}
				}
					
				//System.out.println(name);
				return true;
			}
			
			// Bug Pattern 2
			public boolean visit(InfixExpression node) {
				if(node.getOperator() == Operator.EQUALS || node.getOperator() == Operator.NOT_EQUALS) {
					System.out.println(node.getLeftOperand() instanceof StringLiteral);
					if(node.getLeftOperand() instanceof StringLiteral && node.getRightOperand() instanceof StringLiteral)
						System.out.println("Line Number: " + (cu.getLineNumber(node.getStartPosition())-1) + " - Consider using the equals(Object) method instead");
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
			
			public boolean visit(TryStatement node) {
				return true;				
			}
			
			public boolean visit(CatchClause node) {				
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
			
			@Override
			public boolean visit(IfStatement node) {
				node.getExpression();
				System.out.println("If Statement " + ASTNode.BOOLEAN_LITERAL);
				if(node.getExpression().toString().equals("true")) {
					System.out.println("Error");
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
			System.out.println("HashCode method not found: The class may violate the invariant that equal objects must have equal hashcodes");
		}
		else{
			System.out.println("1 ok");
		}
 
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
	public static void ParseFilesInDir() throws IOException{
		File dirs = new File(".");
		String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator+"resources"+File.separator; 
		System.out.println(dirPath);
		File root = new File(dirPath);
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
		ParseFilesInDir();
	}
}