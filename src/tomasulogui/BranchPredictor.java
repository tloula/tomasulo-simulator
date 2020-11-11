package tomasulogui;

public class BranchPredictor {
  PipelineSimulator simulator;
  int[] counters = new int[2500];
  int[] tgt = new int[2500];

  public BranchPredictor(PipelineSimulator sim) {
    simulator = sim;
    for (int i = 0; i < 2500; i++) {
      tgt[i] = -1;
      // not taken
      counters[i] = 0;
    }
  }

  public void predictBranch(IssuedInst issued) {
    // called knowing this is a control flow inst, but could be called on J
    // our responsibility is to:
    //  1.  perform prediction
    //  2.  annotate the instruction with prediction and tgtAddress
    //  3.  update PC if taken to tgtAddress

    int pcOffset = issued.getPC() / 4;

    boolean predictTaken = false;

    if(issued.isBranch()){
      return;
    }

    if (issued.getOpcode() == IssuedInst.INST_TYPE.J ||
        issued.getOpcode() == IssuedInst.INST_TYPE.JR ||
        issued.getOpcode() == IssuedInst.INST_TYPE.JAL ||
        issued.getOpcode() == IssuedInst.INST_TYPE.JALR) {
      predictTaken = true;
    }
    else {
      if (counters[pcOffset] > 1 && tgt[pcOffset] != -1) { // Changed tgt[pcOffset] comparison to -1 (from 1)
        predictTaken = true;
      }
    }
    issued.setBranchPrediction(predictTaken);

    int tgtAddress = -1;

    // now tgt
    if (issued.getOpcode() == IssuedInst.INST_TYPE.J ||
        issued.getOpcode() == IssuedInst.INST_TYPE.JAL) {
      tgtAddress = issued.getPC() + 4 + issued.getImmediate();
    }
    else if (issued.getOpcode() == IssuedInst.INST_TYPE.JR ||
             issued.getOpcode() == IssuedInst.INST_TYPE.JALR) {
      if (tgt[pcOffset] != -1) {
        tgtAddress = tgt[pcOffset];
      }
      else {
        tgtAddress = issued.getPC() + 4;
      }
    }
    else if (tgt[pcOffset] != -1) {
      tgtAddress = tgt[pcOffset];
    }
    else {
      tgtAddress = issued.getPC() + 4 + issued.getImmediate();
    }

    issued.setBranchTgt(tgtAddress);

    int newPC = predictTaken ? tgtAddress : issued.getPC() + 4;
    simulator.setPC(newPC);
  }

  public void setBranchAddress(int pc, int addr) {
    tgt[pc / 4] = addr;
  }

  public void setBranchResult(int pc, boolean taken) {
    int counter = counters[pc / 4];

    if (taken) {
      if ( (counter == 1) || (counter == 2)) {
        counter = 3;
      }
      else if (counter == 0) {
        counter = 1;
      }
    }
    else {
      if ( (counter == 1) || (counter == 2)) {
        counter = 0;
      }
      else if (counter == 3) {
        counter = 2;
      }
    }
    counters[pc/4] = counter;
  }
}
