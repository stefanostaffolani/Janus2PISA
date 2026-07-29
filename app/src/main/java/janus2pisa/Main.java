package janus2pisa;

import janus2pisa.codegen.CodeGenerationVisitor;
import janus2pisa.codegen.Interpreter;
import janus2pisa.codegen.PisaFormatter;
import janus2pisa.codegen.VisitResult;
import janus2pisa.semantic_analysis.DefinitionVisitor;
import janus2pisa.semantic_analysis.Scope;
import janus2pisa.semantic_analysis.SemanticAnalysisVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {
  public static void main(String[] args) throws Exception {
    InputStream is = Main.class.getResourceAsStream("/array.janus");
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

    // start cgen
    CodeGenerationVisitor cgv = new CodeGenerationVisitor(globalScope, 1000);
    VisitResult isa = cgv.visit(tree);

    Interpreter interpreter = new Interpreter(10000);

    try {
      String code = PisaFormatter.printLabeledInstructions(isa.instructions());

      Files.writeString(Path.of("output.pisa"), code);
      interpreter.cpu(isa.instructions());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
