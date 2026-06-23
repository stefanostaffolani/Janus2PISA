package janus2pisa.codegen;

import java.util.List;

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

  static LabeledInstruction of(Instruction i) {
    return new LabeledInstruction(null, i);
  }

  static LabeledInstruction labeled(String label, Instruction i) {
    return new LabeledInstruction(label, i);
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
