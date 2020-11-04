package tomasulogui;

public abstract class FunctionalUnit {
  PipelineSimulator simulator;
  ReservationStation[] stations = new ReservationStation[2];
  
  public FunctionalUnit(PipelineSimulator sim) {
    simulator = sim;
    
  }

 
  public void squashAll() {
    // todo fill in
  }

  public abstract int calculateResult(int station);

  public abstract int getExecCycles();

  public boolean isFull(){
    if(stations[0] != null && stations[1] != null){
      return true; 
    }
    else return false;
  }

  public void execCycle(CDB cdb) {
    //todo - start executing, ask for CDB, etc.
  }



  public void acceptIssue(IssuedInst inst) {
  // todo - fill in reservation station (if available) with data from inst
  }

}
