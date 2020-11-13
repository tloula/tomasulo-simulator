package tomasulogui;

public class IssueUnit {
  private enum EXEC_TYPE {
    NONE, LOAD, ALU, MULT, DIV, BRANCH} ;

    PipelineSimulator simulator;
    IssuedInst issuee;
    EXEC_TYPE instType;
    Object fu;

    public IssueUnit(PipelineSimulator sim) {
      simulator = sim;
    }

    public void execCycle() {
      // an execution cycle involves:
      // check if ROB and Reservation Station have available slots
      if (this.simulator.getROB().isFull()) return;

      // Get instruction from memory
      issuee = IssuedInst.createIssuedInst(this.simulator.getMemory().getInstAtAddr(this.simulator.getPC()));
      issuee.setPC(this.simulator.getPC());

      // Issue it to the proper FU based on the type IFF it has an available slot
      IssuedInst.INST_TYPE opcode = issuee.getOpcode();

      if (opcode == IssuedInst.INST_TYPE.DIV) {
        if(this.simulator.getDivider().isFull()) return;
        this.instType = EXEC_TYPE.DIV;
      } else if (opcode == IssuedInst.INST_TYPE.MUL) {
        if(this.simulator.getMult().isFull()) return;
        this.instType = EXEC_TYPE.MULT;
      } else if (opcode == IssuedInst.INST_TYPE.LOAD) {
        if (this.simulator.loader.isFull()) return;
        this.instType = EXEC_TYPE.LOAD;
      } else if (issuee.isBranch()) {
        if (this.simulator.getBranchUnit().isFull()) return;
        this.instType = EXEC_TYPE.BRANCH; 
      } else if ( opcode == IssuedInst.INST_TYPE.NOP ||
                  opcode == IssuedInst.INST_TYPE.HALT || 
                  opcode == IssuedInst.INST_TYPE.STORE) {
        this.instType = EXEC_TYPE.NONE;
      } else { // ALU OPS           
        if (this.simulator.getALU().isFull()) return;
        this.instType = EXEC_TYPE.ALU; 
      }

      // We send this to the ROB, which fills in the data fields
      this.simulator.getROB().updateInstForIssue(issuee);

      // We then send this to the correct reservation station
      if (instType == EXEC_TYPE.ALU){
        this.simulator.getALU().acceptIssue(issuee);
      } else if (instType == EXEC_TYPE.BRANCH){
        this.simulator.getBranchUnit().acceptIssue(issuee);
      } else if (instType == EXEC_TYPE.MULT){
        this.simulator.getMult().acceptIssue(issuee);
      } else if (instType == EXEC_TYPE.DIV){
        this.simulator.getDivider().acceptIssue(issuee);
      } else if (instType == EXEC_TYPE.LOAD) {
        this.simulator.getLoader().acceptIssue(issuee);
      }
    }
  }
