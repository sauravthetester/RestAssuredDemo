package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReadJsonFile {
	public static String getJsonString(String filePath) {
        try (InputStream iStream = ReadJsonFile.class.getClassLoader().getResourceAsStream(filePath)) {
            if (iStream == null) 
            	throw new RuntimeException("File not found: "+filePath);
            return new String(iStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
