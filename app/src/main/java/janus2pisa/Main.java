package janus2pisa;

import janus2pisa.semantic_analysis.DefinitionVisitor;
import janus2pisa.semantic_analysis.Scope;
import janus2pisa.semantic_analysis.SemanticAnalysisVisitor;
import java.io.InputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
  public static void main(String[] args) throws Exception {
    InputStream is = Main.class.getResourceAsStream("/fib.janus");
    CharStream input = CharStreams.fromStream(is);
    // CharStream input = CharStreams.fromFileName("fib.janus");

    // 1. Lexer
    JanusLexer lexer = new JanusLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // 2. Parser
    JanusParser parser = new JanusParser(tokens);

    // 3. first rule
    ParseTree tree = parser.prog();

    // 4. print ast
    System.out.println("AST:");
    System.out.println(tree.toStringTree(parser));

    DefinitionVisitor dfv = new DefinitionVisitor();
    dfv.visit(tree);
    Scope globalScope = dfv.getGlobalScope();
    System.out.println(globalScope.toString());

    SemanticAnalysisVisitor sav = new SemanticAnalysisVisitor(globalScope);
    sav.visit(tree);
  }
}
