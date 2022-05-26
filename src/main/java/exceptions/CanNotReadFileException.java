package exceptions;

public class CanNotReadFileException extends RuntimeException {
  public CanNotReadFileException() {
    super("No se pudo leer el archivo");
  }
}
