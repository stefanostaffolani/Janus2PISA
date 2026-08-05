package janus2pisa.codegen;

import java.util.ArrayList;
import java.util.List;

public class Inverter {
  public Instruction invertSingleInstruction(Instruction instruction) {
    return switch (instruction) {
      case ADD i -> {
        yield new SUB(i.rd(), i.rs());
      }
      case SUB i -> {
        yield new ADD(i.rd(), i.rs());
      }
      case ADDI i -> {
        yield new SUBI(i.rd(), i.c());
      }
      case SUBI i -> {
        yield new ADDI(i.rd(), i.c());
      }
      default -> {
        yield instruction;
      }
    };
  }

  public List<LabeledInstruction> invertListofInstructions(List<LabeledInstruction> lil) {
    List<LabeledInstruction> isa = new ArrayList<>();
    for (LabeledInstruction li : lil) {
      isa.add(LabeledInstruction.of(invertSingleInstruction(li.instruction())));
    }
    return isa;
  }
}
