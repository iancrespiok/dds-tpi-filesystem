package fs;

public class Path {
  private String path;
  private LowLevelFileSystem lowLevelFileSystem;

  public Path(String path, LowLevelFileSystem lowLevelFileSystem) {
    this.path = path;
    this.lowLevelFileSystem = lowLevelFileSystem;
  }

  public String get() {
    return path;
  }

  public Boolean exists() {
    return lowLevelFileSystem.exists(path);
  }

  public Boolean isADirectory() {
    return lowLevelFileSystem.isDirectory(path);
  }

  public Boolean isARegularFile() {
    return lowLevelFileSystem.isRegularFile(path);
  }
}
