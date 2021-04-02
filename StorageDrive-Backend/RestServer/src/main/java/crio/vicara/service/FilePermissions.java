package crio.vicara.service;

import java.util.List;

public class FilePermissions {
    String fileId;
    String parentId;
    String ownerEmail;

    List<String> modifiers;
    List<String> accessors;
}
