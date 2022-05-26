package fs;

import exceptions.CanNotOpenFileException;
import exceptions.PathDoesNotContainARegularFile;

public class HighLevelFileSystem {
  private LowLevelFileSystem lowLevelFileSystem;

  public HighLevelFileSystem(LowLevelFileSystem lowLevelFileSystem) {
    this.lowLevelFileSystem = lowLevelFileSystem;
  }

  public Path newPath(String path) {
    return new Path(path, lowLevelFileSystem);
  }

  public File open(Path path) {
    verificarPath(path);
    int fileDescriptor = lowLevelFileSystem.openFile(path.get());
    verificarFD(fileDescriptor, path);
    File file = new File(lowLevelFileSystem, fileDescriptor, true);
    return file;
  }

  private void verificarPath(Path path) {
    if (!path.isARegularFile()) {
      throw new PathDoesNotContainARegularFile(path.get());
    }
  }

  private void verificarFD(int fileDescriptor, Path path) {
    if (fileDescriptor < 0) {
      throw new CanNotOpenFileException(path.get());
    }
  }


}
