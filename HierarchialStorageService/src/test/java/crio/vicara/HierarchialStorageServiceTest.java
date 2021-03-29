package crio.vicara;

import crio.vicara.service.AmazonSimpleStorageService;
import crio.vicara.service.HierarchialStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;

import static org.junit.jupiter.api.Assertions.*;

public class HierarchialStorageServiceTest {
    private static final HierarchialStorageSystem storageService =
            HierarchialStorageService.with(AmazonSimpleStorageService.getInstance());

    @BeforeEach
    public void clearStorageService() {
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
    public void testRename() throws FileAlreadyExistsException {
        storageService.createDirectory(null, "Test1");
        String test2Id = storageService.createDirectory(null, "Test2");
        assertThrows(FileAlreadyExistsException.class,
                () -> storageService.rename(test2Id, "Test1")
        );
        assertDoesNotThrow(() -> storageService.rename(test2Id, "Test3"));
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
    public void testUploadFileInsideFolder() throws FileAlreadyExistsException, FileNotFoundException {
        String rootId = storageService.createDirectory(null, "Test");

        java.io.File file = new java.io.File("./src/test/resources/test.txt");
        FileInputStream stream = new FileInputStream(file);
        String uploadedFileId = storageService.
                uploadFile(rootId, file.getName(), stream, file.length());

        File rootFile = storageService.getFile(rootId);

        assertEquals(rootFile.getChildren().size(), 1);
        assertEquals(rootFile.getChildren().stream().findFirst().get().getFileId(), uploadedFileId);
        assertTrue(storageService.exists(uploadedFileId));
        assertEquals(storageService.getLength(uploadedFileId), file.length());
    }
}
