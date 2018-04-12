package org.jtarun.flattenjson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JsonFlattener {
  private static final String ID_FIELD = "id";
  private static final String INDEX_FIELD = "__index";
  private static final String FILE_SEP = "_";

  @Autowired private JsonParserProxy parser;
  @Autowired private FileUtil fileUtil;


  public String unFlatten(String filePrefix) throws IOException {
    File[] files = fileUtil.getFilesWithPrefix(".", filePrefix);

    Map<String, Object[]> flattenedMap = new HashMap<>();

    for (File file : files) {
      String contentJson = fileUtil.readFile(file.getAbsolutePath());
      String fileName = file.getName();
      Object[] content = parser.toObjectList(contentJson);
      flattenedMap.put(fileName, content);
    }

    Map<String, Object> unflattenedMap = new HashMap<>();
    dfs(flattenedMap, unflattenedMap, 0, "");

    return parser.toJsonString(unflattenedMap);
  }

  private void dfs(Map<String, Object[]> flattenedMap,
                   Map<String, Object> unflattenedMap, int depth, String keyPrefix) {
    Set<String> kDepthKeys = getKDepthKeys(flattenedMap, depth, keyPrefix);

    for (String flattenedKey : kDepthKeys) {
      String key = flattenedKey.substring(keyPrefix.length());
      if (key.startsWith(FILE_SEP)) {
        key = key.substring(FILE_SEP.length());
      }

      List<Object> children = getChildren(flattenedMap, flattenedKey);
      if (children == null) {
        continue;
      }
      for (Object child : children) {
        dfs(flattenedMap, (Map<String, Object>) child, depth + 1, flattenedKey);
      }

      //Remove __index from each child if present.
      //Remove id from each child node.
      boolean isArray = false;
      for (Object child : children) {
        Map<String, Object> childMap = ((Map<String, Object>) child);
        if (childMap.containsKey(INDEX_FIELD)) {
          isArray = true;
          childMap.remove(INDEX_FIELD);
        }

        if (depth > 0) {
          childMap.remove(ID_FIELD);
        }
      }

      // Check if it is simple map or list of map.
      if (!isArray) {
        unflattenedMap.put(key, children.get(0));
      } else {
        unflattenedMap.put(key, children);
      }
    }
  }

  private List<Object> getChildren(Map<String, Object[]> flattenedMap, String flattenedKey) {
    Object[] flattenedObj = flattenedMap.get(flattenedKey);
    if (flattenedObj == null) {
      return null;
    }

    List<Object> children = new ArrayList<>();
    int prevIndex = 0;
    for (Object obj : flattenedObj) {
      Map<String, Object> child = ((Map<String, Object>) obj);
      if (!child.containsKey(INDEX_FIELD)) {
        children.add(child);
        prevIndex++;
        break;
      } else if (prevIndex == Integer.parseInt((String) child.get(INDEX_FIELD))) {
        prevIndex++;
        children.add(child);
      } else {
        break;
      }
    }

    int totalSisters = flattenedObj.length - prevIndex;
    Object[] sisters = new Object[totalSisters];
    for (int i = 0; i < totalSisters; i++) {
      sisters[i] = flattenedObj[prevIndex + i];
    }
    flattenedMap.put(flattenedKey, sisters);

    return children;
  }

  private Set<String> getKDepthKeys(Map<String, Object[]> flattenedMap,
                                    int depth, String keyPrefix) {

    return flattenedMap.keySet().stream()
        .filter(key -> key.startsWith(keyPrefix))
        .filter(key -> key.length() > keyPrefix.length())
        .map(key -> probableKey(flattenedMap, key, depth, keyPrefix))
        .collect(Collectors.toSet());
  }

  private String probableKey(Map<String, Object[]> flattenedMap, String key, int depth,
                             String keyPrefix) {
    String[] keys = key.split(FILE_SEP);
    StringBuilder strBuilder = new StringBuilder(keyPrefix);
    int i = keyPrefix.isEmpty() ? 0 : keyPrefix.split(FILE_SEP).length;
    while (i < keys.length) {
      if (i > 0) {
        strBuilder.append(FILE_SEP);
      }
      strBuilder.append(keys[i++]);
      if (flattenedMap.containsKey(strBuilder.toString())) {
        break;
      }
    }

    return strBuilder.toString();
  }

  public void flattenJsonMap(String fileName, Map<String, List<Map<String, Object>>> flattenedMap)
      throws IOException {

    String json = fileUtil.readFile(fileName);
    Map<String, Object> jsonMap = parser.jsonStringToMap(json);
    flattenJsonMap(jsonMap, "", "", -1, flattenedMap);
  }

  private void flattenJsonMap(
      Map<String, Object> jsonMap,
      String fileName,
      String refId,
      int index,
      Map<String, List<Map<String, Object>>> flattenedMap) {

    if (jsonMap.isEmpty()) {
      return;
    }

    Map<String, Object> flatValues = new HashMap<>();
    jsonMap.forEach((key, value) -> {

      if (value instanceof Map) {

        String id = refId.isEmpty() ? (String) ((Map) value).get(ID_FIELD) : refId;
        String file = fileName.isEmpty() ? key : fileName + FILE_SEP + key;
        flattenJsonMap((Map<String, Object>) value, file, id, -1, flattenedMap);

      } else if (value instanceof List) {

        if (!((List) value).isEmpty()
            && ((List) value).get(0) instanceof Map) {

          String file = fileName.isEmpty() ? key : fileName + FILE_SEP + key;
          int ind = 0;
          for (Object valItem : (List) value) {
            String id = refId.isEmpty() ? (String) ((Map) valItem).get(ID_FIELD) : refId;
            flattenJsonMap((Map<String, Object>) valItem, file, id, ind++, flattenedMap);
          }

        } else {
          flatValues.put(key, value);
        }

      } else {
        flatValues.put(key, value);
      }
    });

    if (fileName.isEmpty()) {
      return;
    }

    flatValues.put(ID_FIELD, refId);
    if (index > -1) {
      flatValues.put(INDEX_FIELD, String.valueOf(index));
    }

    flattenedMap
        .computeIfAbsent(fileName, k -> new ArrayList<>())
        .add(flatValues);
  }

}
