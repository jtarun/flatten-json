package org.jtarun.flattenjson;

import org.jtarun.flattenjson.exception.RuntimeIOException;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

@Component
public class FileUtil {

  private FileSystemResourceLoader resourceLoader = new FileSystemResourceLoader();

  public String readFile(String filePath)
      throws IOException {
    Resource fileResource = resourceLoader.getResource(filePath);
    InputStream inputStream = fileResource.getInputStream();
    StringBuilder contentBuilder = new StringBuilder();

    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        contentBuilder.append(line).append("\n");
      }
    }

    return contentBuilder.toString();
  }

  public void writeToFile(String fileName, String data) throws RuntimeIOException {

    try {
      WritableResource writableResource = (WritableResource) resourceLoader
          .getResource(fileName);

      try (OutputStream outputStream = writableResource.getOutputStream()) {
        outputStream.write(data.getBytes());
      }
    } catch (IOException e) {
      throw new RuntimeIOException("IOException while writing to file: " + fileName);
    }
  }

  public File[] getFilesWithPrefix(String dirPath, String filePrefix) {
    File directory = new File(dirPath);
    return directory.listFiles((dir, name) -> name.startsWith(filePrefix));
  }
}
