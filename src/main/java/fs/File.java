package fs;

import exceptions.CanNotReadFileException;
import exceptions.FileAlreadyClosed;
import java.util.function.Consumer;

public class File {
  private LowLevelFileSystem fileSystem;
  private int fileDescriptor = -1;
  private Boolean opened;

  public File(LowLevelFileSystem fileSystem, int fileDescriptor, Boolean opened) {
    this.fileSystem = fileSystem;
    this.fileDescriptor = fileDescriptor;
    this.opened = opened;
  }

  public Boolean isOpened() {
    return opened;
  }

  public int getDescriptor() {
    return fileDescriptor;
  }

  public void close() {
    verificarSiEstaAbierto();
    fileSystem.closeFile(fileDescriptor);
    opened = false;
  }

  private void verificarSiEstaAbierto() {
    if (!opened) {
      throw new FileAlreadyClosed();
    }
  }

  public void read(Buffer buffer) {
    verificarSiEstaAbierto();
    int readBytes = fileSystem.syncReadFile(fileDescriptor,
        buffer.getBytes(),
        buffer.getStart(),
        buffer.getEnd());
    verificarLectura(readBytes);
  }

  public void write(Buffer buffer) {
    verificarSiEstaAbierto();
    fileSystem.syncWriteFile(fileDescriptor, buffer.getBytes(), buffer.getStart(), buffer.getEnd());
  }

  private void verificarLectura(int readBytes) {
    if (readBytes < 0) {
      throw new CanNotReadFileException();
    }
  }

  public void asyncRead(Consumer<Buffer> callback, Buffer buffer) {
    fileSystem.asyncReadFile(fileDescriptor,
        buffer.getBytes(),
        buffer.getStart(),
        buffer.getEnd(),
        readBytes -> {
          buffer.limit(readBytes);
          callback.accept(buffer);
        });
  }

  public void asyncWrite(Runnable callback) {
    Buffer buffer = new Buffer(100);
    fileSystem.asyncWriteFile(fileDescriptor,
        buffer.getBytes(),
        buffer.getStart(),
        buffer.getEnd(),
        callback);
  }
}
