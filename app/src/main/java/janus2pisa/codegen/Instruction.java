package janus2pisa.codegen;

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
