package exceptions;

public class FileAlreadyClosed extends RuntimeException {
  public FileAlreadyClosed() {
    super("El archivo ya ha sido cerrado previamente.");
  }
}
