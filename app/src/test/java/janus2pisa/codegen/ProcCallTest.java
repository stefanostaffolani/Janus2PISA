package janus2pisa.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import janus2pisa.JanusLexer;
import janus2pisa.JanusParser;
import janus2pisa.semantic_analysis.DefinitionVisitor;
import janus2pisa.semantic_analysis.Scope;
import janus2pisa.semantic_analysis.SemanticAnalysisVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ProcCallTest {
  private ParseTree tree;
  private CodeGenerationVisitor visitor;
  private VisitResult code;

  @BeforeEach
  void setUp() throws Exception {
    InputStream is = IfTest.class.getResourceAsStream("/fun_call.janus");
    if (is == null) {
      throw new IllegalArgumentException("File not found");
    }

    CharStream input = CharStreams.fromStream(is);
    JanusLexer lexer = new JanusLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JanusParser parser = new JanusParser(tokens);
    this.tree = parser.prog();

    DefinitionVisitor dfv = new DefinitionVisitor();
    dfv.visit(this.tree);
    Scope globalScope = dfv.getGlobalScope();
    SemanticAnalysisVisitor sav = new SemanticAnalysisVisitor(globalScope);
    sav.visit(this.tree);

    this.visitor = Mockito.spy(new CodeGenerationVisitor(globalScope, 1000));

    // Stubbing delle sotto-espressioni
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.ExprContext.class));
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.SimpleAssignStmContext.class));
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.ArrAssignStmContext.class));
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.SwapStmContext.class));
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.FromUntilStmContext.class));
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.IfThenStmContext.class));

    this.code = this.visitor.visit(this.tree);
  }

  @Test
  void testBranch() throws IOException {
    long bCount =
        this.code.instructions().stream()
            .map(li -> li.instruction())
            .filter(inst -> inst instanceof BRA)
            .count();

    assertEquals(bCount, 6);
  }

  @Test
  void testSWAPBR() throws IOException {
    long swapbrCount =
        this.code.instructions().stream()
            .map(li -> li.instruction())
            .filter(inst -> inst instanceof SWAPBR)
            .count();
    assertEquals(swapbrCount, 2);
  }

  @Test
  void testLabels() throws IOException {
    // System.out.println(PisaFormatter.printLabeledInstructions(this.code.instructions()));

    List<LabeledInstruction> lis =
        this.code.instructions().stream().filter(li -> li.label() != null).toList();
    long nMain = lis.stream().filter(li -> li.label().matches("^main$")).count();
    assertEquals(1, nMain);
    long nProcName = lis.stream().filter(li -> li.label().matches("^test$")).count();
    assertEquals(1, nProcName);
    long nTop = lis.stream().filter(li -> li.label().matches(".*_top\\d*$")).count();
    assertEquals(2, nTop);
    long nBot = lis.stream().filter(li -> li.label().matches(".*_bot\\d*$")).count();
    assertEquals(2, nBot);
  }
}
