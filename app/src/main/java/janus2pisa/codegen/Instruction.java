package janus2pisa.codegen;

import java.util.ArrayList;
import java.util.List;
import janus2pisa.codegen.exceptions.InverterException;

/*
This file implements the PISA instructions.
A PISA instruction is made of a label (possibly null) and an instruction.
In the following code:
rd -> destination register
rs -> source register
c  -> integer constant
The class PisaFormatter generate the String used in CodeGenerationVisitor
*/

public sealed interface Instruction
    permits ADD,
        SUB,
        NEG,
        XOR,
        ADDI,
        SUBI,
        XORI,
        ORX,
        ANDX,
        SLTX,
        EXCH,
        BRA,
        RBRA,
        BEQ,
        BNE,
        BGEZ,
        SWAPBR,
        DATA,
        START,
        FINISH {}

record ADD(Register rd, Register rs) implements Instruction {}

record SUB(Register rd, Register rs) implements Instruction {}

record NEG(Register rd) implements Instruction {}

record XOR(Register rd, Register rs) implements Instruction {}

record ADDI(Register rd, int c) implements Instruction {}

record SUBI(Register rd, int c) implements Instruction {}

record XORI(Register rd, int c) implements Instruction {}

record ORX(Register rd, Register rs) implements Instruction {}

record ANDX(Register rd1, Register rd2, Register rs) implements Instruction {}

record SLTX(Register rd, Register rs, Register rt) implements Instruction {}

record EXCH(Register rd, Register rs) implements Instruction {}

record BRA(String label) implements Instruction {}

record RBRA(String label) implements Instruction {}

record BEQ(Register rd, Register rs, String label) implements Instruction {}

record BNE(Register rd, Register rs, String label) implements Instruction {}

record BGEZ(Register rd, String label) implements Instruction {}

record SWAPBR(Register rd) implements Instruction {}

record DATA(int value) implements Instruction {}

record START() implements Instruction {}

record FINISH() implements Instruction {}

record LabeledInstruction(String label, Instruction instruction) {

  public LabeledInstruction(String label, LabeledInstruction old) {
    this(label, old.instruction());
  }

  static LabeledInstruction of(Instruction i) {
    return new LabeledInstruction(null, i);
  }

  static LabeledInstruction labeled(String label, Instruction i) {
    return new LabeledInstruction(label, i);
  }
}

class Inverter{
  public Instruction invertSingleInstruction(Instruction instruction){
    return switch (instruction) {
      case  ADD i -> {
        yield new SUB(i.rd(),i.rs());
      }
      case SUB i -> {
        yield new ADD(i.rd(),i.rs());
      }
      case ADDI i ->{
        yield new SUBI(i.rd(),i.c());
      }
      case SUBI i -> {
        yield new ADDI(i.rd(), i.c());
      }
      case NEG i -> {
        yield new NEG(i.rd());
      }
      case XOR i -> {
        yield new XOR(i.rd(), i.rs());
      }
      case XORI i -> {
        yield new XORI(i.rd(), i.c());
      }
      case ORX i -> {
        yield new ORX(i.rd(), i.rs());
      }
      case ANDX i ->{
        yield new ANDX(i.rd1(), i.rd2(), i.rs());
      }
      case SLTX i ->{
        yield new SLTX(i.rd(), i.rs(), i.rt());
      }
      case EXCH i ->{
        yield new EXCH(i.rd(), i.rs());
      }
      case BRA i ->{
        yield new BRA(i.label());
      }
      case RBRA i -> {
        yield new RBRA(i.label());
      }
      case SWAPBR i ->{
        yield new SWAPBR(i.rd());
      }
      default -> {
        throw new InverterException("Unknown instruction");
      }

    };
  }

  public List<LabeledInstruction> invertListofInstructions(List<LabeledInstruction> lil){
    List<LabeledInstruction> isa = new ArrayList<>();
    for (LabeledInstruction li : lil){
      isa.add(LabeledInstruction.of(invertSingleInstruction(li.instruction())));
    }
    return isa;
  }

}

class PisaFormatter {

  static String format(Instruction instruction) {
    return switch (instruction) {
      case ADD a -> "ADD " + a.rd() + " " + a.rs();
      case SUB s -> "SUB " + s.rd() + " " + s.rs();
      case NEG n -> "NEG " + n.rd();
      case XOR x -> "XOR " + x.rd() + " " + x.rs();

      case ADDI a -> "ADDI " + a.rd() + " " + a.c();
      case SUBI s -> "SUBI " + s.rd() + " " + s.c();
      case XORI x -> "XORI " + x.rd() + " " + x.c();

      case ORX o -> "ORX " + o.rd() + " " + o.rs();
      case ANDX a -> "ANDX " + a.rd1() + " " + a.rd2() + " " + a.rs();
      case SLTX s -> "SLTX " + s.rd() + " " + s.rs() + " " + s.rt();

      case EXCH e -> "EXCH " + e.rd() + " " + e.rs();

      case BRA b -> "BRA " + b.label();
      case RBRA r -> "RBRA " + r.label();

      case BEQ b -> "BEQ " + b.rd() + " " + b.rs() + " " + b.label();
      case BNE b -> "BNE " + b.rd() + " " + b.rs() + " " + b.label();

      case BGEZ b -> "BGEZ " + b.rd() + " " + b.label();
      case SWAPBR s -> "SWAPBR " + s.rd();

      case DATA d -> "DATA " + d.value();

      case START ignored -> "START";
      case FINISH ignored -> "FINISH";
    };
  }

  static String printLabeledInstructions(List<LabeledInstruction> instructions) {
    StringBuilder sb = new StringBuilder();

    for (LabeledInstruction li : instructions) {
      String line = format(li.instruction());

      if (li.label() != null) {
        sb.append(li.label()).append(": ").append(line);
      } else {
        sb.append("\t").append(": ").append(line);
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
