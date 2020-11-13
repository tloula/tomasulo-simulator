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
    int pcOffset = issued.getPC() / 4;
    boolean predictTaken = false;
    int tgtAddress = -1;

    // We obviously don't want to predict on non branch instructions
    if(!issued.determineIfBranch()){
      simulator.setPC(issued.getPC() + 4);
      return;
    }

    // Conditions where we predict taken
    if (issued.getOpcode() == IssuedInst.INST_TYPE.J ||
        issued.getOpcode() == IssuedInst.INST_TYPE.JAL)
      predictTaken = true;
    else if (counters[pcOffset] > 1)
      predictTaken = true;

    issued.setBranchPrediction(predictTaken);

    // Calculate and set target and PC
    tgtAddress = issued.getPC() + 4 + issued.getImmediate();
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
      if ((counter == 1) || (counter == 2)) counter = 3;
      else if (counter == 0)                counter = 1;
    } else {
      if ((counter == 1) || (counter == 2)) counter = 0;
      else if (counter == 3)                counter = 2;
    }
    counters[pc/4] = counter;
  }
}
