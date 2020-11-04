package tomasulogui;

public abstract class FunctionalUnit {
  PipelineSimulator simulator;
  ReservationStation[] stations = new ReservationStation[2];
  public enum state { INACTIVE, EXECUTING, RAISING_HAND, CDB }
  state activity = state.INACTIVE;
  int currentSlot = 0;
  int timeLeft;
  int result = -1;

  public FunctionalUnit(PipelineSimulator sim) {
    simulator = sim;
  }
 
  public void squashAll() {
    // todo fill in
  }

  public abstract int calculateResult(int station);

  public abstract int getExecCycles();

  public state getActivity() {
    return this.activity;
  }

  public int getResultTag() {
    return this.stations[currentSlot].getDestTag();
  }

  public int getResult() {
    return this.result;
  }

  public void setStateCDB() {
    this.activity = state.CDB;
  }

  public boolean isFull(){
    if(stations[0] != null && stations[1] != null){
      return true; 
    }
    else return false;
  }

  public void execCycle(CDB cdb) {
    //todo - start executing, ask for CDB, etc.

    // if no one is executing, and if there are instructions to execute, pick one
    if (activity == state.INACTIVE && this.stations[currentSlot] != null) {
      activity = state.EXECUTING;
      timeLeft = this.getExecCycles();
    }
    timeLeft--; // This makes sense

    // Execute an instruction if you can
    if (timeLeft == 0) {
      this.result = this.calculateResult(currentSlot);
      this.activity = state.RAISING_HAND;
    }
    // if an instruction got put on the cdb, set the res station to null
    if (activity == state.CDB){
      activity = state.INACTIVE;
      stations[currentSlot] = null;
      currentSlot = (currentSlot + 1) % 2;
    }
  }

  public void acceptIssue(IssuedInst inst) {
    // todo - fill in reservation station (if available) with data from inst
    int slot = (this.stations[0] == null) ? 0 : 1;

    this.stations[slot].tag1 = inst.getRegSrc1Tag();
    this.stations[slot].tag2 = inst.getRegSrc2Tag();
    this.stations[slot].data1 = inst.getRegSrc1Value();
    this.stations[slot].data2 = inst.getRegSrc2Value();
    this.stations[slot].data1Valid = inst.getRegSrc1Valid();
    this.stations[slot].data2Valid = inst.getRegSrc2Valid();
    this.stations[slot].destTag = inst.getRegDestTag();
    this.stations[slot].function = inst.getOpcode();

    // Branch stuff
  }
}
