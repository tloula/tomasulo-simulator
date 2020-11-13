package tomasulogui;

public class CDB {
  PipelineSimulator simulator;
  int dataTag = -1;
  int dataValue = -1;
  boolean dataValid = false;

  public CDB(PipelineSimulator sim) {
    simulator = sim;
  }

  public boolean getDataValid () {
    return dataValid;
  }
  public void setDataValid(boolean valid) {
    dataValid = valid;
  }

  public int getDataTag () {
    return dataTag;
  }
  public void setDataTag (int tag) {
    dataTag = tag;
  }

  public int getDataValue() {
    return dataValue;
  }
  public void setDataValue (int value) {
    dataValue = value;
  }

  public void squashAll() {
    dataValid = false;
  }

  private boolean CDB_WasSetBy(FunctionalUnit fu) {
    if (fu.getActivity() == FunctionalUnit.state.RAISING_HAND) {
      this.setDataTag(fu.getResultTag());
      this.setDataValue(fu.getResult());
      this.setDataValid(true);

      fu.setStateCDB();
      return true;
    }
    return false;
  }

  public void updateCDB() {
    this.setDataValid(false);

    // Poll functional units to see if they want to write to CDB (starting with longest running, i.e. div)
    if (CDB_WasSetBy(this.simulator.getDivider())) return;
    if (CDB_WasSetBy(this.simulator.getMult())) return;

    // Poll Load Unit before ALU unit so it wont' starve
    if (this.simulator.getLoader().isRequestingWriteback()) {
      this.setDataTag(this.simulator.getLoader().getWriteTag());
      this.setDataValue(this.simulator.getLoader().getWriteData());
      this.setDataValid(true);
      this.simulator.getLoader().setCanWriteback();
      return;
    }

    if (CDB_WasSetBy(this.simulator.getALU())) return;
  }
}
