package crio.vicara;

import crio.vicara.service.AmazonSimpleStorageService;
import crio.vicara.service.HierarchialStorageService;
import org.junit.Test;

import java.nio.file.FileAlreadyExistsException;

public class HierarchialStorageServiceTest {
    private static final HierarchialStorageService storageService = HierarchialStorageService.with(AmazonSimpleStorageService.getInstance());

    @Test
    public void testCreateFolder() {
        try {
            String rootId = storageService.createDirectory(null, "Karthik");
            String moviesId = storageService.createDirectory(rootId, "Movies");
            assert (storageService.listChildren(rootId).stream()
                    .map(ChildFile::getFileName)
                    .allMatch(fileName -> fileName.equals("Movies"))
            );
        } catch (FileAlreadyExistsException e) {
        }
    }
}
