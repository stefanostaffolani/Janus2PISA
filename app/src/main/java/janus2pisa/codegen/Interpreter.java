package janus2pisa.codegen;

import janus2pisa.codegen.exceptions.InterpreterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Interpreter {
  private final int STACK_SIZE = 100;
  private static ArrayList<Integer> memory;
  private static Inverter inv;
  private static Register[] registers;
  private static Integer pc;
  private static Integer br;
  private static Integer dir;
  private static Map<String, Integer> label2loc;

  public Interpreter() {
    registers = new Register[32];
    for (int i = 0; i < registers.length; i++) {
      registers[i] = new Register("R" + i, 0);
    }
    // if (br=0) pc += dir else pc += BR*dir
    pc = 0;
    br = 0;
    dir = 1;
    inv = new Inverter();
    memory = new ArrayList<>();
    for (int i = 0; i < STACK_SIZE; i++) {
      memory.add(0);
    }
    label2loc = new HashMap<>();
  }

  private Integer getRegisterIndex(Register r) {
    String name = r.getName();
    return Integer.valueOf(name.replaceAll("\\D+", ""));
  }

  private void convLabel2Loc(List<LabeledInstruction> isa) {
    for (int i = 0; i < isa.size(); i++) {
      String lab = ((LabeledInstruction) isa.get(i)).label();
      if (lab != null) {
        label2loc.put(lab, i);
      }
    }
  }

  public void exec(Instruction li) {
    switch (li) {
      case ADD i -> {
        Integer idx1 = getRegisterIndex(i.rd());
        Register rd = registers[idx1];
        Integer idx2 = getRegisterIndex(i.rs());
        Register rs = registers[idx2];

        registers[idx1].setValue(rd.getValue() + rs.getValue());
      }
      case SUB i -> {
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Register rd = registers[idx1];
        Register rs = registers[idx2];

        registers[idx1].setValue(rd.getValue() - rs.getValue());
      }
      case NEG i -> {
        Integer idx = getRegisterIndex(i.rd());
        Register rd = registers[idx];
        registers[idx].setValue(-rd.getValue());
      }
      case XOR i -> {
        Integer idx = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Register rd = registers[idx];
        Register rs = registers[idx2];
        registers[idx].setValue(rd.getValue() ^ rs.getValue());
      }
      case ADDI i -> {
        Integer idx = getRegisterIndex(i.rd());
        Register rd = registers[idx];
        registers[idx].setValue(rd.getValue() + i.c());
      }
      case SUBI i -> {
        Integer idx = getRegisterIndex(i.rd());
        Register rd = registers[idx];
        registers[idx].setValue(rd.getValue() - i.c());
      }
      case XORI i -> {
        Integer idx = getRegisterIndex(i.rd());
        Register rd = registers[idx];
        registers[idx].setValue(rd.getValue() ^ i.c());
      }
      case ORX i -> {
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Integer idx3 = getRegisterIndex(i.rt());
        Register rd = registers[idx1];
        Register rs = registers[idx2];
        Register rt = registers[idx3];
        registers[idx1].setValue(rd.getValue() ^ (rs.getValue() | rt.getValue()));
      }
      case ANDX i -> {
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Integer idx3 = getRegisterIndex(i.rt());
        Register rd = registers[idx1];
        Register rs = registers[idx2];
        Register rt = registers[idx3];
        registers[idx1].setValue(rd.getValue() ^ (rs.getValue() & rt.getValue()));
      }
      case SLTX i -> {
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Integer idx3 = getRegisterIndex(i.rt());
        Register rd = registers[idx1];
        Register rs = registers[idx2];
        Register rt = registers[idx3];
        registers[idx1].setValue(rd.getValue() ^ (rs.getValue() < rt.getValue() ? 1 : 0));
      }
      case EXCH i -> {
        // mem[ra] <-> rd
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.ra());
        Register rd = registers[idx1];
        Register ra = registers[idx2];
        int memAddress = ra.getValue();
        if (memAddress < 0 || memAddress >= STACK_SIZE) {
          throw new InterpreterException("Stack Overflow/Underflow : " + memAddress);
        }
        Integer tmp = memory.get(ra.getValue());
        memory.set(ra.getValue(), rd.getValue());
        registers[idx1].setValue(tmp);
      }
      case BRA i -> {
        // offset of label
        // pc += offset, the offset is the label2loc[label] - pc
        Integer offset = label2loc.get(i.label()) - pc;
        br += offset;
      }
      case RBRA i -> {
        // offset of label
        // pc += offset e BR = -1
        System.out.println(i.label());
        Integer offset = label2loc.get(i.label()) - pc;
        br += offset;
        dir = -1;
      }
      case BEQ i -> {
        // branch if regs are equal
        Integer offset = label2loc.get(i.label()) - pc;
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Register rd = registers[idx1];
        Register rs = registers[idx2];

        if (rd.getValue() == rs.getValue()) {
          br += offset;
        }
      }

      case BNE i -> {
        // branch if regs are not equal
        Integer offset = label2loc.get(i.label()) - pc;
        Integer idx1 = getRegisterIndex(i.rd());
        Integer idx2 = getRegisterIndex(i.rs());
        Register rd = registers[idx1];
        Register rs = registers[idx2];
        if (rd.getValue() != rs.getValue()) {
          br += offset;
        }
      }
      case BGEZ i -> {
        // branch great eq 0
        Integer offset = label2loc.get(i.label()) - pc;
        Integer idx1 = getRegisterIndex(i.rd());
        Register rd = registers[idx1];
        if (rd.getValue() >= 0) {
          br += offset;
        }
      }
      case SWAPBR i -> {
        // rd <-> br
        Integer idx1 = getRegisterIndex(i.rd());
        Register rd = registers[idx1];
        Integer tmp = rd.getValue();
        registers[idx1].setValue(br);
        br = tmp;
      }
      case DATA i -> {
        memory.add(i.value());
      }

      case NOP i -> {}

      case FINISH i -> {}

      case START i -> {}

      case PANIC i -> {
        throw new InterpreterException("PANIC");
      }

      default -> {
        throw new InterpreterException("Unable to interpret " + li.toString());
      }
    }
    if (br == 0) {
      pc += dir;
    } else {
      pc += br;
    }
  }

  public void cpu(List<LabeledInstruction> isa) {
    this.convLabel2Loc(isa);
    Integer finishLoc = label2loc.get("finish");
    Integer startLoc = label2loc.get("start");
    System.out.print(label2loc);
    pc += startLoc;
    while (!Objects.equals(pc, finishLoc)) {
      LabeledInstruction ins = isa.get(pc);
      if (dir == -1) {
        this.exec(inv.invertSingleInstruction(ins.instruction()));
      } else {
        this.exec(ins.instruction());
      }
    }
    // Print all registers
    for (Register r : registers) {
      System.out.println(r.getName());
      System.out.println(r.getValue());
    }
    System.out.println(memory);
  }
}
