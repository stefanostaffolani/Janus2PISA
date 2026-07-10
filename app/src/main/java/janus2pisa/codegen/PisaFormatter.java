package janus2pisa.codegen;

import java.util.List;

public class PisaFormatter {

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
      case ANDX a -> "ANDX " + a.rd() + " " + a.rt() + " " + a.rs();
      case SLTX s -> "SLTX " + s.rd() + " " + s.rs() + " " + s.rt();

      case EXCH e -> "EXCH " + e.rd() + " " + e.ra();

      case BRA b -> "BRA " + b.label();
      case RBRA r -> "RBRA " + r.label();

      case BEQ b -> "BEQ " + b.rd() + " " + b.rs() + " " + b.label();
      case BNE b -> "BNE " + b.rd() + " " + b.rs() + " " + b.label();

      case BGEZ b -> "BGEZ " + b.rd() + " " + b.label();
      case SWAPBR s -> "SWAPBR " + s.rd();

      case DATA d -> "DATA " + d.value();

      case START ignored -> "START";
      case FINISH ignored -> "FINISH";
      case NOP ignored -> "NOP";
      case PANIC ignored -> "PANIC";
    };
  }

  public static String printLabeledInstructions(List<LabeledInstruction> instructions) {
    StringBuilder sb = new StringBuilder();

    int max_len = 0;
    for (LabeledInstruction li : instructions) {
      if (li.label() != null && li.label().length() > max_len) {
        max_len = li.label().length();
      }
    }

    for (LabeledInstruction li : instructions) {
      String line = format(li.instruction());

      if (li.label() != null) {
        sb.append(li.label())
            .append(" ".repeat(max_len - li.label().length()))
            .append(": ")
            .append(line);
      } else {
        sb.append(" ".repeat(max_len)).append(": ").append(line);
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
