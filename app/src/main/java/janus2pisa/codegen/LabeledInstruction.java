package janus2pisa.codegen;

public record LabeledInstruction(String label, Instruction instruction) {

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
