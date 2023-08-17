package io.perfwise.local.pluginsmanager.service;

import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.util.List;

public interface UploadService {
    String handleFileUpload(List<FileItem> items) throws IOException;
}
