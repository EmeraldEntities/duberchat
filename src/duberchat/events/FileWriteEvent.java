package duberchat.events;

/**
 * A {@code FileWriteEvent} is an event that is created when a file needs to be updated. 
 * <p>
 * This is from any change to any server-side user or channel.
 * 
 * <p>
 * Since <b>2020-12-08</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class FileWriteEvent {
  private Object toWrite;
  private String filePath;

  /**
   * Constructs a new {@code FileWriteEvent}.
   * 
   * @param toWrite The object that needs to be written
   * @param filePath The filepath dictating the file to be written to
   */
  public FileWriteEvent(Object toWrite, String filePath) {
    this.toWrite = toWrite;
    this.filePath = filePath;
  }

  /**
   * Retrieves the object associated with this event.
   * 
   * @return a {@code Object} representing the associated object to write.
   */
  public Object getObjectToWrite() {
    return this.toWrite;
  }

  /**
   * Retrieves the file path associated with this event.
   * 
   * @return a {@code String} representing the associated filePath.
   */
  public String getFilePath() {
    return this.filePath;
  }
}
