package com.keeper.homepage.global.util.file.server;

import static com.keeper.homepage.global.util.file.server.FileServerConstants.DEFAULT_FILE_PATH;
import static com.keeper.homepage.global.util.file.server.FileServerConstants.ROOT_PATH;
import static java.io.File.separator;

import com.keeper.homepage.domain.file.dao.FileRepository;
import com.keeper.homepage.domain.file.entity.FileEntity;
import com.keeper.homepage.global.util.file.FileUtil;
import com.keeper.homepage.global.util.file.exception.FileSaveFailedException;
import com.keeper.homepage.global.util.web.ip.WebUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
class FileServerUtil extends FileUtil {

  private final FileRepository fileRepository;
  private final WebUtil webUtil;

  static {
    createDirectoryWhenIsNotExist(DEFAULT_FILE_PATH);
  }

  private static void createDirectoryWhenIsNotExist(String path) {
    try {
      Files.createDirectories(Paths.get(path));
    } catch (IOException ignore) {
    }
  }

  @Transactional
  @Override
  protected FileEntity save(@NonNull MultipartFile file) {
    try {
      LocalDateTime fileUploadTime = LocalDateTime.now();
      File newFile = saveFileInServer(file, fileUploadTime);
      return saveFileEntity(file, newFile, fileUploadTime);
    } catch (IOException | RuntimeException e) {
      throw new FileSaveFailedException(e);
    }
  }

  private static File saveFileInServer(MultipartFile file, LocalDateTime now)
      throws IOException {
    LocalDate fileUploadDate = now.toLocalDate();
    String fileDirectoryPath = getFileDirectoryPath(fileUploadDate);
    String fileName = generateRandomFilename(file);
    File newFile = new File(fileDirectoryPath + fileName);
    file.transferTo(newFile);
    return newFile;
  }

  private static String getFileDirectoryPath(LocalDate fileUploadDate) {
    String fileUploadDirectory = DEFAULT_FILE_PATH + fileUploadDate + separator;
    createDirectoryWhenIsNotExist(fileUploadDirectory);
    return fileUploadDirectory;
  }

  private static String generateRandomFilename(@NonNull MultipartFile file) {
    String filename = file.getOriginalFilename();
    String ext = filename.substring(filename.lastIndexOf("."));
    return UUID.randomUUID() + ext;
  }

  private FileEntity saveFileEntity(MultipartFile file, File newFile, LocalDateTime now) {
    String ipAddress = webUtil.getUserIP();
    return fileRepository.save(
        FileEntity.builder()
            .fileName(file.getOriginalFilename())
            .filePath(getFileUrl(newFile))
            .fileSize(file.getSize())
            .uploadTime(now)
            .ipAddress(ipAddress)
            .build());
  }

  private static String getFileUrl(File newFile) {
    String path = newFile.getPath();
    return path.substring(ROOT_PATH.length() + 1);
  }

  @Override
  protected void deleteFile(FileEntity fileEntity) {

  }

  @Override
  protected void deleteEntity(FileEntity fileEntity) {

  }
}
