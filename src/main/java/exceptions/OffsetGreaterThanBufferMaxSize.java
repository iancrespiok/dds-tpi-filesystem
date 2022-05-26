package exceptions;

public class OffsetGreaterThanBufferMaxSize extends RuntimeException {
  public OffsetGreaterThanBufferMaxSize() {
    super("El tamaño de desplazamiento supera el tamaño maximo del buffer.");
  }
}
