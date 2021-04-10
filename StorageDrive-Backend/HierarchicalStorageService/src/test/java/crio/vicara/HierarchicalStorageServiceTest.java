package crio.vicara;

import crio.vicara.service.AmazonSimpleStorageService;
import crio.vicara.service.HierarchicalStorageService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class HierarchicalStorageServiceTest {
    private static final HierarchicalStorageSystem storageService =
            HierarchicalStorageService.with(AmazonSimpleStorageService.getInstance());

    @BeforeEach
    public void clearStorageServiceBeforeEachTest() {
        storageService.clearAll();
    }

    @Test
    public void testCreateFolder() throws FileAlreadyExistsException {
        String rootId = storageService.createDirectory(null, "Test");
        assertThrows(FileAlreadyExistsException.class,
                () -> storageService.createDirectory(null, "Test")
        );
        assertEquals(storageService.getFile(rootId).getChildren().size(), 0);
    }

    @Test
    public void testFolderSize() throws FileAlreadyExistsException, FileNotFoundException {
        String test1Id = storageService.createDirectory(null, "Test1");
        java.io.File file = new java.io.File("./src/test/resources/test.txt");

        FileInputStream stream1 = new FileInputStream(file);
        FileInputStream stream2 = new FileInputStream(file);
        FileInputStream stream3 = new FileInputStream(file);

        storageService.uploadFile(test1Id, "test1.txt", stream1, file.length());
        assertEquals(file.length(), storageService.getLength(test1Id));
        storageService.uploadFile(test1Id, "test2.txt", stream2, file.length());
        assertEquals(file.length() * 2, storageService.getLength(test1Id));
        storageService.uploadFile(test1Id, "test3.txt", stream3);
        assertEquals(file.length() * 3, storageService.getLength(test1Id));

        String test2InsideTest1Id = storageService.createDirectory(test1Id, "Test2");
        FileInputStream stream4 = new FileInputStream(file);
        FileInputStream stream5 = new FileInputStream(file);
        FileInputStream stream6 = new FileInputStream(file);
        storageService.uploadFile(test2InsideTest1Id, "test1.txt", stream4);
        storageService.uploadFile(test2InsideTest1Id, "test2.txt", stream5);
        storageService.uploadFile(test2InsideTest1Id, "test3.txt", stream6);

        assertEquals(file.length() * 3, storageService.getLength(test2InsideTest1Id));
        assertEquals(file.length() * 6, storageService.getLength(test1Id));
    }

    @Test
    public void testRename() throws FileAlreadyExistsException {
        storageService.createDirectory(null, "Test1");
        String test2Id = storageService.createDirectory(null, "Test2");
        assertThrows(FileAlreadyExistsException.class,
                () -> storageService.rename(test2Id, "Test1")
        );
        assertDoesNotThrow(() -> storageService.rename(test2Id, "Test3"));
        assertDoesNotThrow(() -> storageService.rename(test2Id, "Test4"));
    }

    @Test
    public void testMove() throws FileAlreadyExistsException, FileNotFoundException {
        String sourceId = storageService.createDirectory(null, "Test1");
        String destinationId = storageService.createDirectory(null, "Test2");

        String duplicateTest1InDestinationId = storageService.createDirectory(destinationId, "Test1");

        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream stream = new FileInputStream(file);
        String uploadedFileId = storageService.uploadFile(destinationId, file.getName(), stream, file.length());


        assertThrows(NotDirectoryException.class, () -> storageService.move(sourceId, uploadedFileId, false));
        assertThrows(NotDirectoryException.class, () -> storageService.move(sourceId, uploadedFileId, true));

        assertThrows(FileAlreadyExistsException.class, () -> storageService.move(sourceId, destinationId, false));
        assertDoesNotThrow(() -> storageService.move(sourceId, destinationId, true));
        assertFalse(storageService.exists(duplicateTest1InDestinationId));

        File destination = storageService.getFile(destinationId);
        File movedFile = storageService.getFile(sourceId);

        assertEquals(destinationId, movedFile.getParentId());
        assertEquals(
                destination.getChildren()
                        .stream()
                        .map(ChildFile::getFileId)
                        .filter(id -> id.equals(sourceId))
                        .count()
                , 1);
    }

    @Test
    public void testDelete() throws FileAlreadyExistsException, FileNotFoundException {
        String karthik = storageService.createDirectory(null, "karthik");
        String movies = storageService.createDirectory(karthik, "movies");
        String music = storageService.createDirectory(karthik, "music");
        String documents = storageService.createDirectory(karthik, "documents");

        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream stream = new FileInputStream(file);
        String test = storageService.uploadFile(documents, "test.txt", stream);

        assertEquals(file.length(), storageService.getLength(documents));
        storageService.delete(test);
        assertEquals(0, storageService.getLength(documents));
        assertFalse(storageService.exists(test));
        storageService.delete(movies);
        assertFalse(storageService.exists(movies));
        storageService.delete(karthik);
        assertFalse(storageService.exists(karthik));
        assertFalse(storageService.exists(music));
    }

    @Test
    public void testUploadFileInsideFolder() throws FileAlreadyExistsException, FileNotFoundException {
        String rootId = storageService.createDirectory(null, "Test");

        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream stream1 = new FileInputStream(file);
        FileInputStream stream2 = new FileInputStream(file);
        String uploadedFileId = storageService.
                uploadFile(rootId, file.getName(), stream1, file.length());

        File rootFile = storageService.getFile(rootId);

        assertEquals(rootFile.getChildren().size(), 1);
        assertEquals(rootFile.getChildren().get(0).getFileId(), uploadedFileId);
        assertTrue(storageService.exists(uploadedFileId));
        assertEquals(storageService.getLength(uploadedFileId), file.length());

        assertThrows(FileAlreadyExistsException.class,
                () -> storageService.uploadFile(rootId, file.getName(), stream2)
        );

        Assertions.assertDoesNotThrow(() -> storageService.uploadFile(rootId, "test1.txt", stream2));
    }

    @Test
    public void testListChildrenInsideAFolder() throws FileAlreadyExistsException, FileNotFoundException {
        String rootId = storageService.createDirectory(null, "Karthik");
        storageService.createDirectory(rootId, "Movies");
        String documentsId = storageService.createDirectory(rootId, "Documents");

        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream stream = new FileInputStream(file);
        storageService.uploadFile(documentsId, file.getName(), stream, file.length());

        assertTrue(
                storageService.listChildren(rootId)
                        .stream()
                        .map(ChildFile::getFileName)
                        .collect(Collectors.toList())
                        .containsAll(List.of("Movies", "Documents"))
        );

        assertTrue(
                storageService.listChildren(documentsId).stream()
                        .map(ChildFile::getFileName)
                        .collect(Collectors.toList())
                        .contains("test.txt")
        );
    }

    @Test
    public void testDownloadFile() throws IOException {
        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream uploadStream = new FileInputStream(file);
        FileInputStream testStream = new FileInputStream(file);

        String uploadedFileId = storageService.uploadFile(null, file.getName(), uploadStream);
        InputStream fileDownloadStream = storageService.downloadFile(uploadedFileId);

        assertTrue(Arrays.equals(testStream.readAllBytes(), fileDownloadStream.readAllBytes()));
    }

    @Test
    public void testDownloadFileURL() throws IOException, InterruptedException {
        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream uploadStream = new FileInputStream(file);
        FileInputStream testStream = new FileInputStream(file);

        String uploadedFileId = storageService.uploadFile(null, file.getName(), uploadStream);

        URL downloadURL = storageService.downloadableFileURL(uploadedFileId, 10);
        assertTrue(Arrays.equals(testStream.readAllBytes(), downloadURL.openStream().readAllBytes()));

        Thread.sleep(10000);
        assertThrows(IOException.class, downloadURL::openStream);
    }

    @Test
    public void testGetFileIdByName() throws FileAlreadyExistsException {
        String test1Id = storageService.createDirectory(null, "Test1");
        assertEquals(test1Id, storageService.getFileIdByName(null, "Test1"));
        String test2Id = storageService.createDirectory(test1Id, "Test2");
        assertEquals(test2Id, storageService.getFileIdByName(test1Id, "Test2"));
    }

    @Test
    public void testFilepath() throws FileAlreadyExistsException, FileNotFoundException {
        String test1Id = storageService.createDirectory(null, "Test1");
        assertEquals("/Test1", storageService.getFilePath(test1Id));
        String test2Id = storageService.createDirectory(test1Id, "Test2");
        assertEquals("/Test1/Test2", storageService.getFilePath(test2Id));

        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream stream = new FileInputStream(file);

        String uploadedFileId = storageService.uploadFile(test2Id, file.getName(), stream, file.length());
        assertEquals("/Test1/Test2/test.txt", storageService.getFilePath(uploadedFileId));
    }

    @AfterEach
    public void clearStorageServiceAfterEachTest() {
        storageService.clearAll();
    }
}
