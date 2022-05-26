package exceptions;

public class CanNotOpenFileException extends RuntimeException {
  public CanNotOpenFileException(String path) {
    super("No se puede abrir el archivo cuya ruta es " + path + ". Intentelo nuevamente.");
  }
}
