package requests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.response.Response;
import utils.ReadJsonFile;

public class ReqResAPI {


    public static Response loginRequest(String payload, String token) {
        return  given().log().all()
	        		.headers(RequestHeader.getGenericHeaders(token))
	        		.body(payload)
        		.when().log().all()
        			.post("/api/login");
    }
    
    public static Response loginRequestWithoutPayload(String token) {
        return  given().log().all()
	        		.headers(RequestHeader.getGenericHeaders(token))
        		.when().log().all()
        			.post("/api/login");
    }
    
    public static Response createUser(String payload, String token) {
        return  given().log().all()
	        		.headers(RequestHeader.getGenericHeaders(token))
	        		.body(payload)
        		.when().log().all()
        			.post("/api/users");
    }
    
    public static Response getSingleUser(String id, String token, boolean checkCached, String etag) {
    	
    	if(!checkCached)
    	{
    		return  given().log().all()
	        		.headers(RequestHeader.getGenericHeaders(token))
        		.when().log().all()
        			.get("/api/users/"+id);
    	}
    	else
    	{
    		return  given().log().all()
	        		.headers(RequestHeader.getGenericHeaders(token))
	        		.header("If-None-Match", etag)
        		.when().log().all()
        			.get("/api/users/"+id);
    	}
    }
    
    public static Response deleteSingleUser(String id, String token) {
        return  given().log().all()
	        		.headers(RequestHeader.getGenericHeaders(token))
        		.when().log().all()
        			.delete("/api/users/"+id);
    }
    
    public static Response delayedResponse(String token) {
        return  given().log().all()
        			.queryParam("delay", 3)
	        		.headers(RequestHeader.getGenericHeaders(token))
        		.when().log().all()
        			.get("/api/users");
    }
    
    public static Response singleUserSuccess() {
    	String singleUserSuccessMockResponsePayload = ReadJsonFile.getJsonString("requestdata/SingleUserSuccessMockRequestPayload.json");
        return  given().log().all()
        			.body(singleUserSuccessMockResponsePayload)
        			.header("Content-Type", "application/json")
        		.when().log().all()
        			.get("/api/singleusers");
    }
}
