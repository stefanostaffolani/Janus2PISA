package janus2pisa.codegen;

import java.util.List;

public record VisitResult(List<LabeledInstruction> instructions, Register resultRegister) {}
