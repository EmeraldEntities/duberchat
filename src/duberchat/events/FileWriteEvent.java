package duberchat.events;

public class FileWriteEvent {
  private Object toWrite;
  private String filePath;

  public FileWriteEvent(Object toWrite, String filePath) {
    this.toWrite = toWrite;
    this.filePath = filePath;
  }

  public Object getObjectToWrite() {
    return this.toWrite;
  }
  public String getFilePath() {
    return this.filePath;
  }
}
