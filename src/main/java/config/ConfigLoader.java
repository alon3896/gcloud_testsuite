/**
 * The ConfigLoader class is responsible for loading configuration data
 * from a YAML file and mapping it to a GcloudConfig object.
 */
package config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigLoader {

    public static GcloudConfig loadConfig(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(new File(filePath), GcloudConfig.class);
    }
}
