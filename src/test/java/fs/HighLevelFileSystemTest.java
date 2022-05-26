package fs;

import exceptions.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.Arrays;
import java.util.function.Consumer;
import static org.mockito.Mockito.*;

class HighLevelFileSystemTest {

  private LowLevelFileSystem lowLevelFileSystem;
  private HighLevelFileSystem fileSystem;
  private Path path;
  private Path pathMalito;
  private Path pathEjemplo;
  private Path pathSinArchivo;

  @BeforeEach
  void initFileSystem() {
    lowLevelFileSystem = mock(LowLevelFileSystem.class);
    fileSystem = new HighLevelFileSystem(lowLevelFileSystem);

    path = fileSystem.newPath("unArchivo.txt");
    when(lowLevelFileSystem.isRegularFile(path.get())).thenReturn(true);

    pathMalito = fileSystem.newPath("archivoMalito.txt");
    when(lowLevelFileSystem.isRegularFile(pathMalito.get())).thenReturn(true);

    pathEjemplo = fileSystem.newPath("ejemplo.txt");
    when(lowLevelFileSystem.isRegularFile(pathEjemplo.get())).thenReturn(true);

    pathSinArchivo = fileSystem.newPath("ruta/rutab");
    when(lowLevelFileSystem.isRegularFile(pathSinArchivo.get())).thenReturn(false);
  }

  @Test
  void sePuedeAbrirUnArchivo() {
    when(lowLevelFileSystem.openFile(path.get())).thenReturn(42);

    File file = fileSystem.open(path);
    Assertions.assertEquals(file.getDescriptor(), 42);
  }

  @Test
  void noSePuedeAbrirUnArchivoDeUnPathSinArchivo() {
    Assertions.assertThrows(PathDoesNotContainARegularFile.class, ()->fileSystem.open(pathSinArchivo));
  }

  @Test
  void siLaAperturaFallaUnaExcepcionEsLanzada() {
    when(lowLevelFileSystem.openFile(path.get())).thenReturn(-1);
    Assertions.assertThrows(CanNotOpenFileException.class, () -> fileSystem.open(path));
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoNoHayNadaParaLeer() {
    Buffer buffer = new Buffer(100);

    when(lowLevelFileSystem.openFile(pathEjemplo.get())).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), buffer.getStart(), buffer.getEnd())).thenReturn(0);

    File file = fileSystem.open(pathEjemplo);
    file.read(buffer);

    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(99, buffer.getEnd());
    Assertions.assertEquals(100, buffer.getCurrentSize());
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoHayAlgoParaLeer() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile(path.get())).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 9)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 0, 4, (byte) 3);
      return 4;
    });

    File file = fileSystem.open(path);
    file.read(buffer);

    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(9, buffer.getEnd());
    Assertions.assertEquals(10, buffer.getCurrentSize());
    Assertions.assertArrayEquals(buffer.getBytes(), new byte[] {3, 3, 3, 3, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void siLaLecturaSincronicaFallaUnaExcepciónEsLanzada() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile(pathMalito.get())).thenReturn(13);
    when(lowLevelFileSystem.syncReadFile(anyInt(), any(), anyInt(), anyInt())).thenReturn(-1);

    File file = fileSystem.open(pathMalito);

    Assertions.assertThrows(CanNotReadFileException.class, () -> file.read(buffer));
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoHayNoHayNadaParaEscribir() {
    Buffer buffer = new Buffer(0);
    when(lowLevelFileSystem.openFile(path.get())).thenReturn(35);

    File file = fileSystem.open(path);
    file.write(buffer);

    Mockito.verify(lowLevelFileSystem,times(1)).syncWriteFile(35,buffer.getBytes(),buffer.getStart(),buffer.getEnd());
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoHayAlgoParaEscribir() {
    Buffer buffer = new Buffer(100);
    when(lowLevelFileSystem.openFile(pathEjemplo.get())).thenReturn(30);

    File file = fileSystem.open(pathEjemplo);
    file.write(buffer);

    Mockito.verify(lowLevelFileSystem,times(1)).syncWriteFile(30,buffer.getBytes(),buffer.getStart(),buffer.getEnd());
  }

  @Test
  void sePuedeLeerAsincronicamenteUnArchivo() {
    final int bytesLeidos = 5;
    Buffer buffer = new Buffer(100);
    Consumer<Integer> consumerMock = mock(Consumer.class);
    Consumer<Buffer> consumerBufferMock = mock(Consumer.class);
    when(lowLevelFileSystem.openFile(pathEjemplo.get())).thenReturn(10);

    doAnswer(invocation->{
      final Consumer<Integer> callback = invocation.getArgument(4);
      callback.accept(bytesLeidos);
      return null;
    }).when(lowLevelFileSystem).asyncReadFile(anyInt(), any(), anyInt(), anyInt(), any());

    File file = fileSystem.open(pathEjemplo);
    file.asyncRead(consumerBufferMock, buffer);

    verify(consumerBufferMock,times(1)).accept(buffer);

    //Mockito.verify(lowLevelFileSystem, times(1)).asyncReadFile(10,buffer.getBytes(),buffer.getStart(), buffer.getEnd(), consumerBufferMock);
  }

  @Test
  void sePuedeEscribirAsincronicamenteUnArchivo() {
    Buffer buffer = new Buffer(100);
    Runnable callback = mock(Runnable.class);
    when(lowLevelFileSystem.openFile(pathEjemplo.get())).thenReturn(10);

    File file = fileSystem.open(pathEjemplo);
    file.asyncWrite(callback);

    Mockito.verify(lowLevelFileSystem, times(1)).
        asyncWriteFile(10,
            buffer.getBytes(),
            buffer.getStart(),
            buffer.getEnd(),
            callback);
  }

  @Test
  void sePuedeCerrarUnArchivo() {
    when(lowLevelFileSystem.openFile(pathEjemplo.get())).thenReturn(10);
    File file = fileSystem.open(pathEjemplo);

    file.close();
    Mockito.verify(lowLevelFileSystem, times(1)).closeFile(10);

    Assertions.assertFalse(file.isOpened());
  }

  @Test
  void noSePuedeCerrarUnArchivoCerrado() {
    when(lowLevelFileSystem.openFile(pathEjemplo.get())).thenReturn(10);
    File file = fileSystem.open(pathEjemplo);

    file.close();
    Mockito.verify(lowLevelFileSystem, times(1)).closeFile(10);

    Assertions.assertThrows(FileAlreadyClosed.class, ()->file.close());
  }

  @Test
  void sePuedeSaberSiUnPathEsUnArchivoRegular() {
    when(lowLevelFileSystem.isRegularFile(path.get())).thenReturn(true);
    Assertions.assertTrue(path.isARegularFile());
  }

  @Test
  void sePuedeSaberSiUnPathEsUnDirectorio() {
    when(lowLevelFileSystem.isDirectory(path.get())).thenReturn(true);
    Assertions.assertTrue(path.isARegularFile());
  }

  @Test
  void sePuedeSaberSiUnPathExiste() {
    when(lowLevelFileSystem.exists(path.get())).thenReturn(true);
    Assertions.assertTrue(path.isARegularFile());
  }
/*
  @Test
  void pedidoTP() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile(path.get())).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 9)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 0, 4, (byte) 100);
      Arrays.fill(buffer.getBytes(), 4, 5, (byte) 101);
      Arrays.fill(buffer.getBytes(), 5, 10, (byte) 102);
      return 10;
    });

    File file = fileSystem.open(path);
    file.read(buffer);

    Assertions.assertArrayEquals(buffer.getBytes(), new byte[]{100,100,100,100,101,102,102,102,102,102});

    byte[] C0 = buffer.readBytes(0,4);
    byte[] C1 = buffer.readBytes(4,5);
    byte[] C2 = buffer.readBytes(5,10);

    Buffer newBuffer = new Buffer(13);

    newBuffer.addBytes(C0);
    newBuffer.addByte((byte) 0x0);
    newBuffer.addByte((byte) 0x10);
    newBuffer.addByte((byte) 0x0);
    newBuffer.addBytes(C1);
    newBuffer.addBytes(C2);

    Assertions.assertArrayEquals(newBuffer.getBytes(), new byte[]{100,100,100,100,0x0,0x10,0x0,101,102,102,102,102,102});
  }

 */
}
