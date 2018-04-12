package org.jtarun.flattenjson;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootApplication
@Slf4j
public class FlattenJsonApplication implements CommandLineRunner {

  @Autowired private JsonParserProxy parser;
  @Autowired private FileUtil fileUtil;
  @Autowired private JsonFlattener jsonFlattener;

  public static void main(String[] args) {
    SpringApplication.run(FlattenJsonApplication.class, args);
  }

  public void run(String... args) throws IOException {
    if (args.length < 1) {
      log.error("Provide filename as argument.");
      return;
    }

    Map<String, List<Map<String, Object>>> flattenedMap = new HashMap<>();
    jsonFlattener.flattenJsonMap(args[0], flattenedMap);
    System.out.println(flattenedMap);
    flattenedMap.forEach((file, data) -> fileUtil.writeToFile(file, parser.toJsonString(data)));
  }


}
