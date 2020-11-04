package tomasulogui;

public class IntAlu extends FunctionalUnit{
  public static final int EXEC_CYCLES = 1;

  public IntAlu(PipelineSimulator sim) {
    super(sim);
  }


  public int calculateResult(int station) {
    int result=0;

    IssuedInst.INST_TYPE inst = this.stations[station].getFunction();
    int leftOperand = this.stations[station].getData1();
    int rightOperand = this.stations[station].getData2();

    switch (inst) {
      case ADD:
      case ADDI:
        result = leftOperand + rightOperand;
        break;
      case SUB:
        result = leftOperand - rightOperand;
        break;
      case AND:
      case ANDI:
        result = leftOperand & rightOperand;
        break;
      case OR:
      case ORI:
        result = leftOperand | rightOperand;
        break;
      case XOR:
      case XORI:
        result = leftOperand ^ rightOperand;
        break;
      case SLL:
        result = leftOperand << rightOperand; // Is this right??
        break;
      case SRL:
        result = leftOperand >> rightOperand;
        break;
      case SRA:
        result = leftOperand >> rightOperand;
        break;
      default:
        System.out.println("Int ALU got an invalid instruction, instruction is: " + inst);
        break;
    }
    
    return result;
  }

  public int getExecCycles() {
    return EXEC_CYCLES;
  }
}
