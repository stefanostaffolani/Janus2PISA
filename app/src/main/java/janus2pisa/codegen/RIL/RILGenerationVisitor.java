package janus2pisa.codegen.RIL;

import janus2pisa.JanusBaseVisitor;
import janus2pisa.JanusParser;
import janus2pisa.semantic_analysis.Scope;
import java.util.ArrayList;
import java.util.List;

public class RILGenerationVisitor extends JanusBaseVisitor<String> {

  private final Scope scope;
  private Integer tempCounter;
  private Integer labelCounter;
  private List<String> code = new ArrayList<>();

  /* Code generation idea
   During each visit we return the identifier or the temporary
  */

  private String newTemp() {
    return "t" + String.valueOf(tempCounter++);
  }

  private String newLabel() {
    return "L" + String.valueOf(labelCounter++);
  }

  public RILGenerationVisitor(Scope globalScope) {
    this.scope = globalScope;
    tempCounter = 0;
    labelCounter = 0;
  }

  @Override
  public String visitIntegerExpr(JanusParser.IntegerExprContext ctx) {
    return ctx.INT().getText();
  }

  @Override
  public String visitGenericOP(JanusParser.GenericOPContext ctx) {
    String left = visit(ctx.expr(0));
    String right = visit(ctx.expr(1));
    String temp = newTemp();
    String op = ctx.OP_GEN().getText();
    code.add(temp + " = " + left + ("=".equals(op) ? " == " : " " + op + " ") + right);
    return temp;
  }

  @Override
  public String visitSimpleAssignStm(JanusParser.SimpleAssignStmContext ctx) {
    String id = ctx.ID().getText();
    String result = visit(ctx.expr());
    code.add(id + " " + ctx.ROP_ASS().getText() + " " + result);
    return id;
  }

  @Override
  public String visitVariableExpr(JanusParser.VariableExprContext ctx) {
    return ctx.ID().getText();
  }

  @Override
  public String visitArrayExpr(JanusParser.ArrayExprContext ctx) {
    String id = ctx.ID().getText();
    String t = visit(ctx.expr());
    return id + "[" + t + "]";
  }

  @Override
  public String visitArrAssignStm(JanusParser.ArrAssignStmContext ctx) {
    String id = ctx.ID().getText();
    String t1 = visit(ctx.expr(0));
    String t2 = visit(ctx.expr(1));
    code.add(id + "[" + t1 + "]" + ctx.ROP_ASS().getText() + " " + t2);
    return id;
  }

  @Override
  public String visitSwapStm(JanusParser.SwapStmContext ctx) {
    String id1 = ctx.ID(0).getText();
    String id2 = ctx.ID(1).getText();
    code.add(id1 + "<->" + id2);
    return null;
  }

  @Override
  public String visitSkipStm(JanusParser.SkipStmContext ctx) {
    return null;
  }

  @Override
  public String visitIfThenStm(JanusParser.IfThenStmContext ctx) {
    String t1 = visit(ctx.expr(0));
    String f_label = newLabel();
    String t_label = newLabel();
    String at_label = newLabel();
    String af_label = newLabel();
    code.add(t1 + " == 0 -> " + f_label + ";" + t_label);
    code.add("<- " + t_label);
    visit(ctx.stmBlk(0));
    code.add("-> " + at_label);
    code.add("<- " + f_label);
    visit(ctx.stmBlk(1));
    code.add("-> " + af_label);
    String t2 = visit(ctx.expr(1));
    code.add(t2 + " == 0 <- " + af_label + ";" + at_label);
    return null;
  }

  @Override
  public String visitFromUntilStm(JanusParser.FromUntilStmContext ctx) {
    String l1 = newLabel();
    String l2 = newLabel();
    String l3 = newLabel();
    String l4 = newLabel();
    String t1 = visit(ctx.expr(0));
    code.add("-> " + l1);
    code.add(l1 + ";" + l3 + " <- " + t1 + " == 0");
    visit(ctx.stmBlk(0));
    String t2 = visit(ctx.expr(1));
    code.add(t1 + "!=0 ->" + l4 + ";" + l2);
    code.add(l2 + " <-");
    visit(ctx.stmBlk(1));
    code.add("-> " + l3);
    code.add(l4 + " <-");
    return null;
  }

  @Override
  public String visitCallStm(JanusParser.CallStmContext ctx) {
    String l1 = this.newLabel();
    String l2 = this.newLabel();
    String f_label = ctx.ID().getText();
    code.add("-> " + l1);
    code.add(l1 + " <-");
    code.add("call " + f_label);
    code.add("-> " + l2);
    code.add(l2 + " <-");
    return f_label;
  }

  @Override
  public String visitUncallStm(JanusParser.UncallStmContext ctx) {
    String l1 = this.newLabel();
    String l2 = this.newLabel();
    String f_label = ctx.ID().getText();
    code.add("-> " + l1);
    code.add(l1 + " <-");
    code.add("uncall " + f_label);
    code.add("-> " + l2);
    code.add(l2 + " <-");
    return f_label;
  }

  @Override
  public String visitProc(JanusParser.ProcContext ctx) {
    String label = ctx.ID().getText();
    code.add("begin " + label);
    visit(ctx.stmBlk());
    code.add("end " + label);
    return label;
  }

  @Override
  public String visitProg(JanusParser.ProgContext ctx) {
    for (JanusParser.ProcContext pctx : ctx.proc()) {
      visit(pctx);
    }
    String ril = String.join("\n", code);
    return ril;
  }

  @Override
  public String visitDec(JanusParser.DecContext ctx) {
    return null;
  }
}
