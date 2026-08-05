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

public class IfTest {

  private ParseTree tree;
  private CodeGenerationVisitor visitor;
  private VisitResult code;

  @BeforeEach
  void setUp() throws Exception {
    InputStream is = IfTest.class.getResourceAsStream("/if_then.janus");
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
        .visit(any(JanusParser.CallStmContext.class));
    doReturn(new VisitResult(null, null))
        .when(this.visitor)
        .visit(any(JanusParser.UncallStmContext.class));

    this.code = this.visitor.visit(this.tree);
  }

  @Test
  void testBranchEQ() throws IOException {
    long beqCount =
        this.code.instructions().stream()
            .map(li -> li.instruction())
            .filter(inst -> inst instanceof BEQ)
            .count();

    assertEquals(beqCount, 1);
  }

  @Test
  void testBranch() throws IOException {
    long bneCount =
        this.code.instructions().stream()
            .map(li -> li.instruction())
            .filter(inst -> inst instanceof BNE)
            .count();

    assertEquals(bneCount, 3);
  }

  @Test
  void testLabels() throws IOException {
    List<LabeledInstruction> lis =
        this.code.instructions().stream().filter(li -> li.label() != null).toList();
    long nAssertTrue = lis.stream().filter(li -> li.label().contains("assert_true")).count();
    assertEquals(1, nAssertTrue);
    long nTest = lis.stream().filter(li -> li.label().matches("^test\\d*$")).count();
    assertEquals(1, nTest);
    long nAssert = lis.stream().filter(li -> li.label().matches("^assert\\d*$")).count();
    assertEquals(1, nAssert);
    long nTestFalse = lis.stream().filter(li -> li.label().contains("test_false")).count();
    assertEquals(1, nTestFalse);
  }
}
