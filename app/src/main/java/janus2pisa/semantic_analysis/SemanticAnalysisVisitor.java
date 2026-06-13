package janus2pisa.semantic_analysis;

import janus2pisa.JanusBaseVisitor;
import janus2pisa.JanusParser;

public class SemanticAnalysisVisitor extends JanusBaseVisitor<Void> {

  private final Scope scope;

  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public SemanticAnalysisVisitor(Scope globalScope) {
    this.scope = globalScope;
  }

  @Override
  public Void visitProg(JanusParser.ProgContext ctx) {
    boolean hasMain = ctx.proc().stream().anyMatch(p -> p.ID().getText().equals("main"));
    if (!hasMain) {
      throw new RuntimeException("main not found in prog");
    }
    return null;
  }

  @Override
  public Void visitIntegerExpr(JanusParser.IntegerExprContext ctx) {
    if (!isInteger(ctx.INT().getText())) {
      throw new RuntimeException("Semantic Error: Not Integer");
    }
    return null;
  }

  @Override
  public Void visitVariableExpr(JanusParser.VariableExprContext ctx) {
    String name = ctx.ID().getText();
    Symbol V = this.scope.resolve_recursively(name);
    if (V == null) {
      throw new RuntimeException("Undefined Variable : " + name);
    }
    return null;
  }

  @Override
  public Void visitArrayExpr(JanusParser.ArrayExprContext ctx) {
    String name = ctx.ID().getText();
    Symbol V = this.scope.resolve_recursively(name);
    if (V == null) {
      throw new RuntimeException("Undefined Variable : " + name);
    }
    return visit(ctx.expr());
  }

  @Override
  public Void visitGenericOP(JanusParser.GenericOPContext ctx) {
    // String op = ctx.OP_GEN().getText();
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
  }

  @Override
  public Void visitCallStm(JanusParser.CallStmContext ctx) {
    String name = ctx.ID().getText();
    Symbol F = this.scope.resolve_recursively(name);
    if (F == null) {
      throw new RuntimeException("Undefined Variable : " + name);
    }
    return null;
  }

  @Override
  public Void visitUncallStm(JanusParser.UncallStmContext ctx) {
    String name = ctx.ID().getText();
    Symbol F = this.scope.resolve_recursively(name);
    if (F == null) {
      throw new RuntimeException("Undefined Variable : " + name);
    }
    return null;
  }

  @Override
  public Void visitIfThenStm(JanusParser.IfThenStmContext ctx) {
    // check the expressions
    visit(ctx.expr(0));
    visit(ctx.expr(1));

    // check statements
    visit(ctx.stmBlk(0));
    visit(ctx.stmBlk(1));

    // this will change when we will add more scopes
    return null;
  }

  @Override
  public Void visitFromUntilStm(JanusParser.FromUntilStmContext ctx) {
    // check the expressions
    visit(ctx.expr(0));
    visit(ctx.expr(1));

    // check statements
    visit(ctx.stmBlk(0));
    visit(ctx.stmBlk(1));

    return null;
  }

  @Override
  public Void visitSwapStm(JanusParser.SwapStmContext ctx) {
    String name1 = ctx.ID(0).getText();
    String name2 = ctx.ID(1).getText();

    Symbol S1 = scope.resolve_recursively(name1);
    Symbol S2 = scope.resolve_recursively(name2);

    if (S1 == null || S2 == null) {
      // TODO: do cases
      throw new RuntimeException("Undeclared Variables " + name1 + name2);
    }
    return null;
  }

  @Override
  public Void visitArrAssignStm(JanusParser.ArrAssignStmContext ctx) {
    String name = ctx.ID().getText();
    Symbol S = scope.resolve_recursively(name);
    if (S == null) {
      throw new RuntimeException("Undeclared Variable : " + name);
    }
    // check the expressions

    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
  }

  @Override
  public Void visitSimpleAssignStm(JanusParser.SimpleAssignStmContext ctx) {
    String name = ctx.ID().getText();
    Symbol S = scope.resolve_recursively(name);
    if (S == null) {
      throw new RuntimeException("Undeclared Variable : " + name);
    }
    visit(ctx.expr());
    return null;
  }
}
