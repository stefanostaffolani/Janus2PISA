package janus2pisa.semantic_analysis;

import janus2pisa.JanusBaseVisitor;
import janus2pisa.JanusParser;

public class DefinitionVisitor extends JanusBaseVisitor<Void> {

  private Scope globalScope = new BlockScope(null);
  private int LabelCount = 0;
  private int offset = 0;

  public DefinitionVisitor() {}

  public Scope getGlobalScope() {
    return globalScope;
  }

  public String newLabel() {
    String base = "func";
    return base + LabelCount++;
  }

  @Override
  public Void visitSimpleDec(JanusParser.SimpleDecContext ctx) {
    String VarName = ctx.ID().getText();
    Symbol VarSym = globalScope.resolve(VarName);
    // check if ProcName already exists
    if (VarSym != null) {
      throw new RuntimeException("Semantic Error: Variable already defined -> " + VarName);
    } else {
      VarSym = new VariableSymbol(VarName, globalScope, offset++);
      globalScope.define(VarSym);
    }
    return null;
  }

  @Override
  public Void visitArrayDec(JanusParser.ArrayDecContext ctx) {
    String VarName = ctx.ID().getText();
    int len = Integer.parseInt(ctx.INT().getText());
    Symbol ArrSym = globalScope.resolve(VarName);
    // check if ProcName already exists
    if (ArrSym != null) {
      throw new RuntimeException("Semantic Error: Variable Array already defined -> " + VarName);
    } else {
      ArrSym = new ArraySymbol(VarName, globalScope, offset++, len);
      globalScope.define(ArrSym);
    }
    return null;
  }

  @Override
  public Void visitProc(JanusParser.ProcContext ctx) {
    String ProcName = ctx.ID().getText();
    Symbol procSym = globalScope.resolve(ProcName);
    // check if ProcName already exists
    if (procSym != null) {
      throw new RuntimeException("Semantic Error: Procedure already defined -> " + ProcName);
    } else {
      procSym = new ProcedureSymbol(ProcName, globalScope, 0, this.newLabel());
      globalScope.define(procSym);
    }
    return null;
  }
}
