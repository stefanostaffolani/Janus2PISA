package janus2pisa.codegen;

import janus2pisa.codegen.exceptions.InverterException;
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
        yield new ORX(i.rd(), i.rs(), i.rt());
      }
      case ANDX i -> {
        yield new ANDX(i.rd(), i.rs(), i.rt());
      }
      case SLTX i -> {
        yield new SLTX(i.rd(), i.rs(), i.rt());
      }
      case EXCH i -> {
        yield new EXCH(i.rd(), i.ra());
      }
      case BRA i -> {
        yield new BRA(i.label());
      }
      case RBRA i -> {
        yield new RBRA(i.label());
      }
      case SWAPBR i -> {
        yield new SWAPBR(i.rd());
      }
      default -> {
        throw new InverterException("Unknown instruction");
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
