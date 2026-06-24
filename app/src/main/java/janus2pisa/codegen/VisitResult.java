package janus2pisa.codegen;

import java.util.Collections;
import java.util.List;

public record VisitResult(List<LabeledInstruction> instructions, Register resultRegister) {

  public VisitResult {
    if (instructions == null) {
      instructions = Collections.emptyList();
    }
  }
}
