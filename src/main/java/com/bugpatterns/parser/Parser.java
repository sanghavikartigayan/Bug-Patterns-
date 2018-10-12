package com.bugpatterns.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 
public class Parser {
 
	//use ASTParse to parse string
	public static void parse(String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);			
		
		for (Comment comment : (List<Comment>) cu.getCommentList()) {

		    comment.accept(new CommentParser(cu, str.split("\n")));
		}
		
		cu.accept(new ASTVisitor() {
			 
			Set names = new HashSet();		
 
			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				this.names.add(name.getIdentifier());
				System.out.println("Declaration of '" + name + "' at line"
						+ cu.getLineNumber(name.getStartPosition()));
				return true; // do not continue 
			}
			
			public boolean visit(MethodDeclaration inv) {
				SimpleName name = inv.getName();
				System.out.println(name);
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
				parseSubTree(node.getBody());
				String body = "";
				System.out.println("Empty Exception: There is no debug when an exception occurs");
				if(body.indexOf("TODO") >= 0 || body.indexOf("FIXME") >= 0) {
					System.out.println("Unfinished exception handling code: There's a TODO or a FIXME in the catch block");
				}
				return true;				
			}		
			
			public boolean parseSubTree(Block node) {
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
					System.out.println("Empty Exception: There is no debug when an exception occurs");
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