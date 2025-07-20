package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties prop;
    static {
        prop = new Properties();
        try (InputStream input = new FileInputStream("src/test/resources/config.properties")) {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String get(String key) {
        return prop.getProperty(key);
    }
}
