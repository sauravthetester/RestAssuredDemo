package requests;

import java.util.ArrayList;
import java.util.List;

import io.restassured.http.Header;
import io.restassured.http.Headers;

public class RequestHeader {
	
	public static Headers getGenericHeaders(String token) {
        List<Header> headerList = new ArrayList<>();
        headerList.add(new Header("Content-Type", "application/json"));
        headerList.add(new Header("x-api-key", token));
        return new Headers(headerList);
    }

}
