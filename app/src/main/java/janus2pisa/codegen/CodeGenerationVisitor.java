package janus2pisa.codegen;

import janus2pisa.JanusBaseVisitor;
import janus2pisa.JanusParser;
import janus2pisa.JanusParser.DecContext;
import janus2pisa.JanusParser.ProcContext;
import janus2pisa.codegen.exceptions.CodeGenerationException;
import janus2pisa.semantic_analysis.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CodeGenerationVisitor extends JanusBaseVisitor<VisitResult> {

  private final Scope scope;
  private final RegisterAllocator regAllocator;
  private final Inverter inv;
  private int offset = 0;
  private int labelCount = 0;
  private final Register r0, rsp, rro;

  public CodeGenerationVisitor(Scope globalScope) {
    this.scope = globalScope;
    this.regAllocator = new RegisterAllocator(32);
    /*
        init free Registers we reserve 3 reserved register and the others for general purpose :
        - r0 : constant 0
        - sp : stack pointer
        - ro : return offset
    */
    r0 = this.regAllocator.getFreeRegister();
    rsp = this.regAllocator.getFreeRegister();
    rro = this.regAllocator.getFreeRegister();
    this.regAllocator.commitRegister(r0);
    this.regAllocator.commitRegister(rsp);
    this.regAllocator.commitRegister(rro);

    this.inv = new Inverter();
  }

  private String newLabel(String prefix) {
    return prefix + labelCount++;
  }

  private VisitResult ClearGarbage() {
    List<LabeledInstruction> isa = new ArrayList<>();
    for (Register r : this.regAllocator.GetGarbageRegisters()) {
      isa.add(LabeledInstruction.of(new XOR(r, r)));
      this.regAllocator.freeRegister(r);
    }
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitIntegerExpr(JanusParser.IntegerExprContext ctx) {
    // get a destination register
    Register rd = regAllocator.getFreeRegister();
    List<LabeledInstruction> isa = new ArrayList<>();
    int value = Integer.parseInt(ctx.INT().getText());
    isa.add(LabeledInstruction.of(new ADDI(rd, value)));
    regAllocator.commitRegister(rd);
    return new VisitResult(isa, rd);
  }

  @Override
  public VisitResult visitSimpleDec(JanusParser.SimpleDecContext ctx) {
    // the variable can be accessed using offset or later if needed we can add a label
    List<LabeledInstruction> isa = new ArrayList<>();
    isa.add(LabeledInstruction.of(new DATA(0)));
    this.offset += 1;
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitArrayDec(JanusParser.ArrayDecContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    String name = ctx.ID().getText();
    ArraySymbol v = (ArraySymbol) this.scope.resolve_recursively(name);
    int len = v.getLength();
    for (int i = 0; i < len; i++) {
      isa.add(LabeledInstruction.of(new DATA(0)));
    }
    this.offset += len;
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitVariableExpr(JanusParser.VariableExprContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    String name = ctx.ID().getText();
    VariableSymbol var = (VariableSymbol) scope.resolve_recursively(name);
    int offset = var.getOffset();
    // use EXCH-XOR-EXCH pattern for variable use
    Register ra = this.regAllocator.getFreeRegister();
    Register rd = this.regAllocator.getFreeRegister();
    Register rv = this.regAllocator.getFreeRegister();
    isa.add(LabeledInstruction.of(new ADDI(ra, offset)));
    isa.add(LabeledInstruction.of(new EXCH(rv, ra)));
    isa.add(LabeledInstruction.of(new XOR(rd, rv)));
    isa.add(LabeledInstruction.of(new EXCH(rv, ra)));
    isa.add(LabeledInstruction.of(new SUBI(ra, offset)));
    this.regAllocator.freeRegister(ra);
    this.regAllocator.freeRegister(rv);
    this.regAllocator.commitRegister(rd);
    return new VisitResult(isa, rd);
  }

  @Override
  public VisitResult visitArrayExpr(JanusParser.ArrayExprContext ctx) {
    // put the value of array at index in rd
    List<LabeledInstruction> isa = new ArrayList<>();
    String name = ctx.ID().getText();
    ArraySymbol var = (ArraySymbol) scope.resolve_recursively(name);
    int offset = var.getOffset();
    // we use the visitor for getting the index, that is saved in a register
    VisitResult idx = visit(ctx.expr());

    Register ra = this.regAllocator.getFreeRegister();
    Register rd = this.regAllocator.getFreeRegister();
    Register rv = this.regAllocator.getFreeRegister();
    isa.addAll(idx.instructions());
    isa.add(LabeledInstruction.of(new ADDI(ra, offset)));
    isa.add(LabeledInstruction.of(new ADD(ra, idx.resultRegister())));
    isa.add(LabeledInstruction.of(new EXCH(rv, ra)));
    isa.add(LabeledInstruction.of(new XOR(rd, rv)));
    isa.add(LabeledInstruction.of(new EXCH(rv, ra)));
    isa.add(LabeledInstruction.of(new SUB(ra, idx.resultRegister())));
    isa.add(LabeledInstruction.of(new SUBI(ra, offset)));
    this.regAllocator.freeRegister(ra);
    this.regAllocator.freeRegister(rv);
    this.regAllocator.toGarbage(idx.resultRegister());
    this.regAllocator.commitRegister(rd);
    return new VisitResult(isa, rd);
  }

  @Override
  public VisitResult visitGenericOP(JanusParser.GenericOPContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    VisitResult left = visit(ctx.expr(0));
    VisitResult right = visit(ctx.expr(1));
    String op = ctx.OP_GEN().getText();
    isa.addAll(left.instructions());
    isa.addAll(right.instructions());
    return switch (op) {
      case "+" -> {
        isa.add(LabeledInstruction.of(new ADD(left.resultRegister(), right.resultRegister())));
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(left.resultRegister());
        yield new VisitResult(isa, left.resultRegister());
      }
      case "-" -> {
        isa.add(LabeledInstruction.of(new SUB(left.resultRegister(), right.resultRegister())));
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(left.resultRegister());
        yield new VisitResult(isa, left.resultRegister());
      }
      case "^" -> {
        isa.add(LabeledInstruction.of(new XOR(left.resultRegister(), right.resultRegister())));
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(left.resultRegister());
        yield new VisitResult(isa, left.resultRegister());
      }
      case "*", "/", "*/" -> {
        throw new CodeGenerationException("Multiplication not supported");
      }
      case ">" -> {
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new SLTX(r, right.resultRegister(), left.resultRegister())));
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "<" -> {
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new SLTX(r, left.resultRegister(), right.resultRegister())));
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "<=" -> {
        // x <= y  iff !(x > y)
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new SLTX(r, right.resultRegister(), left.resultRegister())));
        isa.add(LabeledInstruction.of(new XORI(r, 1)));
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case ">=" -> {
        // x <= y  iff !(x > y)
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new SLTX(r, left.resultRegister(), right.resultRegister())));
        isa.add(LabeledInstruction.of(new XORI(r, 1)));
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "&&" -> {
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new ANDX(r, left.resultRegister(), right.resultRegister())));
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.freeRegister(left.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "||" -> {
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new ORX(r, left.resultRegister())));
        isa.add(LabeledInstruction.of(new ORX(r, right.resultRegister())));
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "&" -> {
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new ANDX(r, left.resultRegister(), right.resultRegister())));
        this.regAllocator.freeRegister(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "|" -> {
        Register r = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new ORX(r, left.resultRegister())));
        isa.add(LabeledInstruction.of(new ORX(r, right.resultRegister())));
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }

      case "=" -> {
        // x=y iff !(x < y) && !(y < x)
        Register r = this.regAllocator.getFreeRegister();
        Register rt = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new SLTX(r, left.resultRegister(), right.resultRegister())));
        isa.add(LabeledInstruction.of(new SLTX(rt, right.resultRegister(), left.resultRegister())));
        isa.add(LabeledInstruction.of(new ORX(r, rt)));
        isa.add(LabeledInstruction.of(new XORI(r, 1)));
        this.regAllocator.freeRegister(rt);
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      case "!=" -> {
        Register r = this.regAllocator.getFreeRegister();
        Register rt = this.regAllocator.getFreeRegister();
        isa.add(LabeledInstruction.of(new SLTX(r, left.resultRegister(), right.resultRegister())));
        isa.add(LabeledInstruction.of(new SLTX(rt, right.resultRegister(), left.resultRegister())));
        isa.add(LabeledInstruction.of(new ORX(r, rt)));
        this.regAllocator.freeRegister(rt);
        this.regAllocator.toGarbage(left.resultRegister());
        this.regAllocator.toGarbage(right.resultRegister());
        this.regAllocator.commitRegister(r);
        yield new VisitResult(isa, r);
      }
      default -> throw new CodeGenerationException("Unknown operator " + op);
    };
  }

  @Override
  public VisitResult visitSkipStm(JanusParser.SkipStmContext ctx) {
    return new VisitResult(null, null);
  }

  @Override
  public VisitResult visitSwapStm(JanusParser.SwapStmContext ctx) {
    String name1 = ctx.ID(0).getText();
    String name2 = ctx.ID(1).getText();
    VariableSymbol v1 = (VariableSymbol) scope.resolve_recursively(name1);
    VariableSymbol v2 = (VariableSymbol) scope.resolve_recursively(name2);
    int offset1 = v1.getOffset();
    int offset2 = v2.getOffset();
    List<LabeledInstruction> isa = new ArrayList<>();
    Register ra = this.regAllocator.getFreeRegister();
    Register rb = this.regAllocator.getFreeRegister();
    Register rc = this.regAllocator.getFreeRegister();
    isa.add(LabeledInstruction.of(new ADDI(ra, offset1)));
    isa.add(LabeledInstruction.of(new ADDI(rb, offset2)));
    isa.add(LabeledInstruction.of(new EXCH(ra, rc)));
    isa.add(LabeledInstruction.of(new EXCH(rb, rc)));
    isa.add(LabeledInstruction.of(new EXCH(ra, rc)));
    isa.add(LabeledInstruction.of(new EXCH(ra, rc)));
    isa.add(LabeledInstruction.of(new SUBI(ra, offset1)));
    isa.add(LabeledInstruction.of(new SUBI(rb, offset2)));
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitProg(JanusParser.ProgContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    VisitResult decs, procs;
    for (DecContext dc : ctx.dec()) {
      decs = visit(dc);
      isa.addAll(decs.instructions());
    }
    for (ProcContext pc : ctx.proc()) {
      procs = visit(pc);
      isa.addAll(procs.instructions());
    }
    isa.add(LabeledInstruction.labeled("start", new START()));
    isa.add(LabeledInstruction.of(new ADDI(this.rsp, this.offset)));
    isa.add(LabeledInstruction.of(new BRA("main")));
    isa.add(LabeledInstruction.of(new SUBI(rsp, offset)));
    isa.add(LabeledInstruction.labeled("finish", new FINISH()));

    // error routine label

    isa.add(LabeledInstruction.labeled("error", new BRA("error_loop")));
    isa.add(LabeledInstruction.labeled("error_loop", new BRA("error")));

    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitSimpleAssignStm(JanusParser.SimpleAssignStmContext ctx) {
    // x op e1

    Register rd = this.regAllocator.getFreeRegister();
    Register ra = this.regAllocator.getFreeRegister();

    String name = ctx.ID().getText();
    VariableSymbol arr = (VariableSymbol) scope.resolve_recursively(name);
    int offset = arr.getOffset();

    List<LabeledInstruction> isa = new ArrayList<>();

    isa.add(LabeledInstruction.of(new ADDI(ra, offset)));

    VisitResult e = visit(ctx.expr());
    Register re = e.resultRegister();
    isa.addAll(e.instructions()); // invert expression
    List<LabeledInstruction> e1 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e1);

    isa.add(LabeledInstruction.of(new EXCH(rd, ra)));

    // here we should check for rop assign
    // += -> ADD
    // -= -> SUB
    // ^= -> XOR

    switch (ctx.ROP_ASS().getText()) {
      case "+=" -> {
        isa.add(LabeledInstruction.of(new ADD(ra, re)));
      }
      case "-=" -> {
        isa.add(LabeledInstruction.of(new SUB(ra, re)));
      }
      case "^=" -> {
        isa.add(LabeledInstruction.of(new XOR(ra, re)));
      }
      default -> {
        throw new CodeGenerationException("Unknown ROP Assign Symbol " + ctx.ROP_ASS().getText());
      }
    }

    // update variable entry
    isa.add(LabeledInstruction.of(new EXCH(rd, ra)));

    // subtract offset
    isa.add(LabeledInstruction.of(new SUBI(ra, offset)));

    // add inverse of expr
    isa.addAll(this.inv.invertListofInstructions(e1));
    isa.addAll(this.ClearGarbage().instructions());

    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitArrAssignStm(JanusParser.ArrAssignStmContext ctx) {
    // x[e1] op = e2

    Register rd = this.regAllocator.getFreeRegister();
    // 1. generate code for ra <- e1

    List<LabeledInstruction> isa = new ArrayList<>();
    VisitResult e = visit(ctx.expr(0));
    Register ra = e.resultRegister();
    List<LabeledInstruction> e1 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e1);
    isa.addAll(e.instructions());

    // 2. add base address to ra
    String name = ctx.ID().getText();
    ArraySymbol arr = (ArraySymbol) scope.resolve_recursively(name);
    int offset = arr.getOffset();
    isa.add(LabeledInstruction.of(new ADDI(ra, offset)));

    // 3. generate code for e2
    e = visit(ctx.expr(1));
    Register re = e.resultRegister();
    isa.addAll(e.instructions());
    List<LabeledInstruction> e2 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e2);

    // 4. swap array entry in rd
    isa.add(LabeledInstruction.of(new EXCH(rd, ra)));

    // 5. update array entry
    // here we should check for rop assign
    // += -> ADD
    // -= -> SUB
    // ^= -> XOR

    switch (ctx.ROP_ASS().getText()) {
      case "+=" -> {
        isa.add(LabeledInstruction.of(new ADD(ra, re)));
      }
      case "-=" -> {
        isa.add(LabeledInstruction.of(new SUB(ra, re)));
      }
      case "^=" -> {
        isa.add(LabeledInstruction.of(new XOR(ra, re)));
      }
      default -> {
        throw new CodeGenerationException("Unknown ROP Assign Symbol " + ctx.ROP_ASS().getText());
      }
    }

    // 6. swap back array entry
    isa.add(LabeledInstruction.of(new EXCH(rd, ra)));

    // 7. remove garbage of e2 (inverse of 3.)
    isa.addAll(inv.invertListofInstructions(e1));
    isa.addAll(this.ClearGarbage().instructions());
    // 8. subtract base address
    isa.add(LabeledInstruction.of(new SUBI(ra, offset)));

    // 9. remove garbage of e1 (inverse of 1.)
    isa.addAll(inv.invertListofInstructions(e2));
    isa.addAll(this.ClearGarbage().instructions());
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitIfThenStm(JanusParser.IfThenStmContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    Register rt = this.regAllocator.getFreeRegister();

    String test_label = this.newLabel("test");
    String test_false_label = this.newLabel("test_false");
    String assert_true_label = this.newLabel("assert_true");
    String assert_label = this.newLabel("assert");

    isa.add(LabeledInstruction.of(new BNE(rt, this.r0, "error")));

    VisitResult e = visit(ctx.expr(0));
    Register re = e.resultRegister();

    List<LabeledInstruction> e1 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e1);

    isa.addAll(e.instructions());

    isa.add(LabeledInstruction.of(new XOR(rt, re)));

    isa.addAll(inv.invertListofInstructions(e1));
    isa.addAll(this.ClearGarbage().instructions());

    isa.add(LabeledInstruction.labeled(test_label, new BEQ(rt, this.r0, test_false_label)));

    isa.add(LabeledInstruction.of(new XORI(rt, 1)));

    VisitResult s1 = visit(ctx.stmBlk(0));
    isa.addAll(s1.instructions());

    isa.add(LabeledInstruction.of(new XORI(rt, 1)));

    isa.add(LabeledInstruction.labeled(assert_true_label, new BRA(assert_label)));

    isa.add(LabeledInstruction.labeled(test_false_label, new BRA(test_label)));

    VisitResult s2 = visit(ctx.stmBlk(1));
    isa.addAll(s2.instructions());

    isa.add(LabeledInstruction.labeled(assert_label, new BNE(rt, r0, assert_true_label)));

    e = visit(ctx.expr(0));
    re = e.resultRegister();

    List<LabeledInstruction> e2 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e2);

    isa.addAll(e.instructions());

    isa.add(LabeledInstruction.of(new XOR(rt, re)));
    isa.addAll(inv.invertListofInstructions(e2));
    isa.addAll(this.ClearGarbage().instructions());
    isa.add(LabeledInstruction.of(new BNE(rt, r0, "error")));

    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitFromUntilStm(JanusParser.FromUntilStmContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    Register rt = this.regAllocator.getFreeRegister();

    String entry_label = this.newLabel("entry");
    String test_label = this.newLabel("test");
    String assert_label = this.newLabel("assert");
    String exit_label = this.newLabel("exit");

    // 1. set rt=1
    isa.add(LabeledInstruction.of(new XORI(rt, 1)));

    // 2. receive jump
    isa.add(LabeledInstruction.labeled(entry_label, new BEQ(rt, r0, assert_label)));

    // 3. code for e1
    VisitResult e = visit(ctx.expr(0));
    Register re = e.resultRegister();
    isa.addAll(e.instructions());

    // 4. clear rt = [e1]
    isa.add(LabeledInstruction.of(new XOR(rt, re)));

    // 5. uneval e1
    List<LabeledInstruction> e1 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e1);
    isa.addAll(inv.invertListofInstructions(e1));
    isa.addAll(this.ClearGarbage().instructions());
    // 6. error check
    isa.add(LabeledInstruction.of(new BNE(rt, r0, "error")));

    // 7. code stm1
    VisitResult s1 = visit(ctx.stmBlk(0));
    isa.addAll(s1.instructions());

    // 8. error check
    isa.add(LabeledInstruction.of(new BNE(rt, r0, "error")));

    // 9. eval e2
    e = visit(ctx.expr(1));
    re = e.resultRegister();
    isa.addAll(e.instructions());

    // 10. set rt = [e2]
    isa.add(LabeledInstruction.of(new XOR(rt, re)));

    // 11. uneval e2
    List<LabeledInstruction> e2 =
        e.instructions().stream()
            .map(old -> new LabeledInstruction(old.label(), old))
            .collect(Collectors.toCollection(ArrayList::new));
    Collections.reverse(e2);

    isa.addAll(inv.invertListofInstructions(e2));
    isa.addAll(this.ClearGarbage().instructions());
    // 12. exit if [e2]=1
    isa.add(LabeledInstruction.labeled(test_label, new BNE(rt, r0, exit_label)));

    // 13. code for stm2
    VisitResult s2 = visit(ctx.stmBlk(1));
    isa.addAll(s2.instructions());

    // 14. jump to top
    isa.add(LabeledInstruction.labeled(assert_label, new BRA(entry_label)));

    // 15. receive jump
    isa.add(LabeledInstruction.labeled(exit_label, new BRA(test_label)));

    // 16. clear rt
    isa.add(LabeledInstruction.of(new XORI(rt, 1)));

    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitProc(JanusParser.ProcContext ctx) {
    String name = ctx.ID().getText();
    String label_top = this.newLabel(name + "_top");
    String label_bottom = this.newLabel(name + "_bot");
    List<LabeledInstruction> isa = new ArrayList<>();

    isa.add(LabeledInstruction.labeled(label_top, new BRA(label_bottom)));
    isa.add(LabeledInstruction.labeled(name, new SUBI(this.rsp, 1)));
    isa.add(LabeledInstruction.of(new EXCH(this.rro, this.rsp)));
    isa.add(LabeledInstruction.of(new SWAPBR(this.rro)));
    isa.add(LabeledInstruction.of(new NEG(this.rro)));
    isa.add(LabeledInstruction.of(new EXCH(this.rro, this.rsp)));
    isa.add(LabeledInstruction.of(new ADDI(this.rsp, 1)));

    VisitResult stm_isa = visit(ctx.stmBlk());
    isa.addAll(stm_isa.instructions());
    isa.add(LabeledInstruction.labeled(label_bottom, new BRA(label_top)));
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitCallStm(JanusParser.CallStmContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    String name = ctx.ID().getText();
    isa.add(LabeledInstruction.of(new RBRA(name)));
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitUncallStm(JanusParser.UncallStmContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();
    String name = ctx.ID().getText();
    isa.add(LabeledInstruction.of(new BRA(name)));
    return new VisitResult(isa, null);
  }

  @Override
  public VisitResult visitStmBlk(JanusParser.StmBlkContext ctx) {
    List<LabeledInstruction> isa = new ArrayList<>();

    for (JanusParser.StmContext stm : ctx.stm()) {
      VisitResult res = visit(stm);
      if (res != null) {
        isa.addAll(res.instructions());
      }
    }

    return new VisitResult(isa, null);
  }
}
