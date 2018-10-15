package com.bugpatterns.parser;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;

public class CommentParser extends ASTVisitor {

    CompilationUnit compilationUnit;

    private String[] source;
    
    Map<Integer, String> comments; 

    public CommentParser(CompilationUnit compilationUnit, String[] source, Map<Integer, String> comments) {

        super();
        this.compilationUnit = compilationUnit;
        this.source = source;
        this.comments = comments;
    }

    public boolean visit(LineComment node) {

        int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition()) - 1;
        String lineComment = source[startLineNumber].trim();

//        System.out.println(lineComment);
        
        comments.put(startLineNumber, lineComment);
        
        return true;
    }

    public boolean visit(BlockComment node) {

        int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition()) - 1;
        int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength()) - 1;

        StringBuffer blockComment = new StringBuffer();

        for (int lineCount = startLineNumber ; lineCount<= endLineNumber; lineCount++) {

            String blockCommentLine = source[lineCount].trim();
            blockComment.append(blockCommentLine);
            if (lineCount != endLineNumber) {
                blockComment.append("\n");
            }
        }
        
        comments.put(startLineNumber, blockComment.toString());

//        System.out.println(blockComment.toString());

        return true;
    }

    public void preVisit(ASTNode node) {

    }
}