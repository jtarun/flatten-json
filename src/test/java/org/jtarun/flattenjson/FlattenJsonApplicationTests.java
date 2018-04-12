package org.jtarun.flattenjson;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlattenJsonApplicationTests {

  @Autowired private FlattenJsonApplication application;
  @Autowired private JsonFlattener jsonFlattener;
  @Autowired private JsonParserProxy parser;
  @Autowired private FileUtil fileUtil;

  @Test
  public void contextLoads() {
  }

  @Test
  public void flattenUnFlattenMatchWithOriginalJson() throws IOException {
    String[] fileNames = {"accounts"};

    for (String fileName : fileNames) {
      String filePath = "classpath:" + fileName + ".json";

      Map<String, List<Map<String, Object>>> flattenedMap = new HashMap<>();
      jsonFlattener.flattenJsonMap(filePath, flattenedMap);
      flattenedMap.forEach((file, data) -> fileUtil.writeToFile(file, parser.toJsonString(data)));

      String unflattenedJson = jsonFlattener.unFlatten(fileName);

      String origJson = fileUtil.readFile(filePath);

      Assert.assertTrue(compareJson(origJson, unflattenedJson));
    }
  }

  private boolean compareJson(String json1, String json2) {
    Map<String, Object> map1 = parser.jsonStringToMap(json1);
    Map<String, Object> map2 = parser.jsonStringToMap(json2);
    return map1.equals(map2);
  }

}
