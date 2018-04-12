package org.jtarun.flattenjson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JsonParserProxy {
  private JacksonJsonParser parser = new JacksonJsonParser();
  private ObjectMapper mapper = new ObjectMapper();

  public Map<String, Object> jsonStringToMap(String json) {
    return parser.parseMap(json);
  }

  public String toJsonString(Object object) {
    try {
      return mapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Exception caused during conversion to json string.");
    }
  }

  public Object[] toObjectList(String json) throws IOException {
    return mapper.readValue(json, Object[].class);
  }

}
