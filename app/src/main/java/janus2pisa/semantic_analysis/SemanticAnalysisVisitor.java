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
    String op = ctx.OP_GEN().getText();
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
  }
}
