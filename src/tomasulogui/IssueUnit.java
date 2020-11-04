package tomasulogui;

public class IssueUnit {
  private enum EXEC_TYPE {
    NONE, LOAD, ALU, MULT, DIV, BRANCH} ;

    PipelineSimulator simulator;
    IssuedInst issue;
    EXEC_TYPE instType;
    Object fu;

    public IssueUnit(PipelineSimulator sim) {
      simulator = sim;
    }

    public void execCycle() {
      // an execution cycle involves:
      // 1. check if ROB and Reservation Station have available slots
      if(this.simulator.getROB().isFull()){
        return;
      } else {
        // Get instruction from memory
        issue = IssuedInst.createIssuedInst(this.simulator.getMemory().getInstAtAddr(this.simulator.getPC()));
        
        // Issue it to the proper FU based on the type IFF it has an available slot

        IssuedInst.INST_TYPE opcode = issue.getOpcode();

        if (opcode == IssuedInst.INST_TYPE.DIV) {
          if(this.simulator.getDivider().isFull()){
            return;
          }
          this.instType = EXEC_TYPE.DIV;
        } else if (opcode == IssuedInst.INST_TYPE.MUL) {
          if(this.simulator.getMult().isFull()){
            return;
          }
          this.instType = EXEC_TYPE.MULT;
        } else if (opcode == IssuedInst.INST_TYPE.LOAD) {
          // TODO: Take into account that we don't check if load buffer is full???
          //if(this.simulator.loader.isFull()){
          //  return;
          //}
          this.instType = EXEC_TYPE.LOAD;
        } else if  (opcode == IssuedInst.INST_TYPE.BEQ || 
                    opcode == IssuedInst.INST_TYPE.BNE || 
                    opcode == IssuedInst.INST_TYPE.BLTZ || 
                    opcode == IssuedInst.INST_TYPE.BLEZ || 
                    opcode == IssuedInst.INST_TYPE.BGEZ || 
                    opcode == IssuedInst.INST_TYPE.BGTZ || 
                    opcode == IssuedInst.INST_TYPE.JAL || 
                    opcode == IssuedInst.INST_TYPE.JR || 
                    opcode == IssuedInst.INST_TYPE.JALR || 
                    opcode == IssuedInst.INST_TYPE.J) {
          if(this.simulator.getBranchUnit().isFull()){
            return;
          }
          this.instType = EXEC_TYPE.BRANCH; 
        } else if ( opcode == IssuedInst.INST_TYPE.NOP ||
                    opcode == IssuedInst.INST_TYPE.HALT) {
          this.instType = EXEC_TYPE.NONE;
        } else { // ALU OPS           
          if(this.simulator.getALU().isFull()){
            return;
          }
          this.instType = EXEC_TYPE.ALU; 
        }
      }
      // 2. issuing to reservation station, if no structural hazard
      // to issue, we make an IssuedInst, filling in what we know
      // We check the BTB, and put prediction if branch, updating PC
      //     if pred taken, incr PC otherwise

      // TODO: Finish
      // Update PC
      //Handle branches
      //else
      //Increment normallly
      this.simulator.setPC(this.simulator.getPC() + 4);

      // We then send this to the ROB, which fills in the data fields
      this.simulator.getROB().updateInstForIssue(issue);

      // We then check the CDB, and see if it is broadcasting data we need,
      //    so that we can forward during issue
      if (this.simulator.getCDB().getDataValid()){
        if (this.simulator.getCDB().getDataTag() == issue.getRegSrc1Tag()) {
          issue.setRegSrc1Valid();
          issue.setRegSrc1Value(this.simulator.getCDB().getDataValue());
        } else if (this.simulator.getCDB().getDataTag() == issue.getRegSrc2Tag()){
          issue.setRegSrc2Valid();
          issue.setRegSrc2Value(this.simulator.getCDB().getDataValue());
        }
      }

      // We then send this to the FU, who stores in reservation station
      if (instType == EXEC_TYPE.ALU){
        this.simulator.getALU().acceptIssue(issue);
      } else if (instType == EXEC_TYPE.BRANCH){
        this.simulator.getBranchUnit().acceptIssue(issue);        
      } else if (instType == EXEC_TYPE.MULT){
        this.simulator.getMult().acceptIssue(issue);        
      } else if (instType == EXEC_TYPE.DIV){
        this.simulator.getDivider().acceptIssue(issue);  
      } else if (instType == EXEC_TYPE.NONE){
        // TODO: What about wack instructions like NOP and HALT? 
      } else {
        System.out.println("How did you...what? ....How did you get here?");
      }
    }
  }
