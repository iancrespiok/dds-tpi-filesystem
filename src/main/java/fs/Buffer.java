package fs;

import exceptions.OffsetGreaterThanBufferMaxSize;

public class Buffer {
  private byte[] bytes;
  private int start;
  private int end;
  private int maxSize;
  private int currentSize;

  public Buffer(int size) {
    start = 0;
    end = size - 1;
    bytes = new byte[size];
    currentSize = size;
    maxSize = size;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public int getCurrentSize() {
    return currentSize;
  }

  public void limit(int offset) {
    validarOffset(offset);
    end = start + offset - 1;
    currentSize = offset;
  }

  private void validarOffset(int offset) {
    if (offset > maxSize) {
      throw new OffsetGreaterThanBufferMaxSize();
    }
  }

  public boolean llegoAlFinal() {
    return end == currentSize;
  }

  public byte[] readBytes(int inicio, int fin) {
    byte[] bytesResultado = new byte[fin - inicio];
    int j = 0;
    for (int i = inicio; i < fin; i++) {
      bytesResultado[j] = bytes[i];
      j++;
    }
    return bytesResultado;
  }

  public void addBytes(byte[] bytesAAgregar) {
    int posActual = bytes.length;
    for (int i = 0; i < bytesAAgregar.length; i++) {
      bytes[posActual] = bytesAAgregar[i];
      posActual++;
    }
  }

  public void addByte(byte b) {
    int posActual = bytes.length;
    bytes[posActual] = b;
  }
}
