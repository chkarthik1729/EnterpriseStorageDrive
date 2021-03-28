package crio.vicara;

import crio.vicara.service.AmazonSimpleStorageService;
import crio.vicara.service.HierarchialStorageService;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;

public class HierarchialStorageServiceTest {
    private static final HierarchialStorageSystem storageService =
            HierarchialStorageService.with(AmazonSimpleStorageService.getInstance());

    @Test
    public void testCreateFolder() throws FileAlreadyExistsException, FileNotFoundException {
        String rootId = storageService.createDirectory(null, "Karthik");
        String filesId = storageService.createDirectory(rootId, "Files");
        File file = new File("./src/test/resources/Resume.pdf");

        FileInputStream stream = new FileInputStream(file);
        storageService.uploadFile(filesId, file.getName(), stream, file.length());
    }
}
