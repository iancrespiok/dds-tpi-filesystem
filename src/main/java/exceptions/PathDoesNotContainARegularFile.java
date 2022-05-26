package exceptions;

public class PathDoesNotContainARegularFile extends RuntimeException {
  public PathDoesNotContainARegularFile(String path) {
    super("El path \"" + path + "\" no contiene ningun archivo regular.");
  }
}
