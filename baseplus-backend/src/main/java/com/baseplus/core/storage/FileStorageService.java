package com.baseplus.core.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile saveImage(MultipartFile file, String subdirectory, long maxSizeBytes);

    Resource loadByUrl(String url);

    void deleteByUrl(String url);
}
