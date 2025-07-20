package tests;

import static io.restassured.module.jsv.JsonSchemaValidator.*;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import base.ReqResBase;
import io.restassured.response.Response;
import requests.ReqResAPI;
import utils.ReadJsonFile;
import static utils.StatusCodesUtil.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.Matchers.*;

public class ReqResTest extends ReqResBase{
	
	@Test(priority = 1, groups = {"smoke","regression"})
	public void validateSuccessfulLogin()
	{
		String payload = ReadJsonFile.getJsonString("requestdata/successfulLoginPayload.json");
		Response response = ReqResAPI.loginRequest(payload,"reqres-free-v1");
		String token = response.jsonPath().getString("token");
		long responseTime = response.getTime();
		
		response.then().statusCode(OK);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("schema/successfulLoginSchema.json"));
		Assert.assertTrue(response.jsonPath().getMap("$").containsKey("token"), "Key not found!");
		Assert.assertNotNull(token, "Token is null!");
        Assert.assertTrue(responseTime < 2500, "API took too long to respond: " + responseTime + " ms");						
	}
	
	@Test(priority = 2, groups = {"smoke"})
	public void validateSuccessfulLogin_IdempotencyCheck() {
	    String payload = ReadJsonFile.getJsonString("requestdata/successfulLoginPayload.json");

	    // First call
	    Response firstResponse = ReqResAPI.loginRequest(payload, "reqres-free-v1");
	    String firstToken = firstResponse.jsonPath().getString("token");
	    long firstTime = firstResponse.getTime();

	    // Validations on first response
	    firstResponse.then().statusCode(anyOf(equalTo(OK), equalTo(NO_CONTENT)));
	    firstResponse.then().assertThat().body(matchesJsonSchemaInClasspath("schema/successfulLoginSchema.json"));
	    Assert.assertNotNull(firstToken, "Token is null in first call!");
	    Assert.assertTrue(firstResponse.jsonPath().getMap("$").containsKey("token"), "Token key missing!");
	    Assert.assertTrue(firstTime < 2500, "First call too slow: " + firstTime + " ms");

	    // Second call with same payload (testing idempotency)
	    Response secondResponse = ReqResAPI.loginRequest(payload, "reqres-free-v1");
	    String secondToken = secondResponse.jsonPath().getString("token");
	    long secondTime = secondResponse.getTime();

	    // Validations on second response
	    secondResponse.then().statusCode(anyOf(equalTo(OK), equalTo(NO_CONTENT)));
	    secondResponse.then().assertThat().body(matchesJsonSchemaInClasspath("schema/successfulLoginSchema.json"));
	    Assert.assertNotNull(secondToken, "Token is null in second call!");
	    Assert.assertTrue(secondResponse.jsonPath().getMap("$").containsKey("token"), "Token key missing in second call!");
	    Assert.assertTrue(secondTime < 2500, "Second call too slow: " + secondTime + " ms");

	    // Idempotency check
	    Assert.assertEquals(firstToken, secondToken, "Tokens differ — login API is not idempotent!");
	}
	
	@Test(priority = 3, groups = {"negative","regression"})
	public void validateUnauthorizedLogin()
	{
		String payload = ReadJsonFile.getJsonString("requestdata/successfulLoginPayload.json");
		String invalidToken = "reqres-free-v2";
		Response response = ReqResAPI.loginRequest(payload,invalidToken);
		long responseTime = response.getTime();
		response.then().statusCode(UNAUTHORIZED);
		Assert.assertTrue(response.jsonPath().getMap("$").containsKey("error"), "Key not found!");
        Assert.assertTrue(responseTime < 2500, "API took too long to respond: " + responseTime + " ms");
	}
	
	@Test(priority = 4, groups = {"negative","regression"})
	public void validateBadRequestLogin()
	{
		Response response = ReqResAPI.loginRequestWithoutPayload("reqres-free-v1");
		long responseTime = response.getTime();	
		response.then().statusCode(BAD_REQUEST);
		Assert.assertTrue(response.jsonPath().getMap("$").containsKey("error"), "Key not found!");
        Assert.assertTrue(responseTime < 2500, "API took too long to respond: " + responseTime + " ms");
								
	}
	
	@Parameters("rateLimitingThreads")
	@Test(priority = 5, groups = {"negative"}, enabled = false)
	public void validateRateLimitingWithParallelRequestsLogin(int rateLimitingThreads) throws InterruptedException {
	    String payload = ReadJsonFile.getJsonString("requestdata/successfulLoginPayload.json");
	    
	    ExecutorService executor = Executors.newFixedThreadPool(rateLimitingThreads);
	    List<Future<Integer>> results = new ArrayList<>();

	    for (int i = 0; i < rateLimitingThreads; i++) {
	        results.add(executor.submit(() -> {
	            Response res = ReqResAPI.loginRequest(payload,"reqres-free-v1");
	            return res.getStatusCode();
	        }));
	    }
	                                              
	    executor.shutdown();                                                                                                                                             
	    executor.awaitTermination(10, TimeUnit.SECONDS);

	    long tooMany = results.stream()
	        .map(future -> {
	            try { return future.get(); } catch (Exception e) { return 0; }
	        })                                                                                                                                  
	        .filter(code -> code == 429)
	        .count();

	    Assert.assertTrue(tooMany == 0, tooMany+" number of responses received with status 429");
	}
	
	@Test(priority = 6, groups = {"negative"})
	public void validateHtmlInjectionAttackCreateUser() {
		String payload = ReadJsonFile.getJsonString("requestdata/htmlInjectionAttackPayload.json");
		Response response = ReqResAPI.createUser(payload,"reqres-free-v1");
		long responseTime = response.getTime();
		
		response.then().statusCode(CREATED);
		
        Assert.assertTrue(responseTime < 2500, "API took too long to respond: " + responseTime + " ms");
        Assert.assertFalse(response.body().toString().contains("<script>alert('xss')</script>"),
                "Potential XSS vulnerability: script tag echoed in response!");
        
        Assert.assertTrue(
        		response.jsonPath().getString("name").contains("&lt;script&gt;alert('xss')&lt;/script&gt;") ||
                !response.jsonPath().getString("name").toString().contains("<script>"),
                "Response should escape or sanitize script content");
        
        Assert.assertTrue(
        		response.jsonPath().getString("job").contains("&lt;script&gt;alert('xss')&lt;/script&gt;") ||
                !response.jsonPath().getString("job").toString().contains("<script>"),
                "Response should escape or sanitize script content");
	}
	
	@Test(priority = 7, groups = {"negative"})
	public void validateSQLInjectionAttackCreateUser() {
		String payload = ReadJsonFile.getJsonString("requestdata/sqlInjectionAttackPayload.json");
		Response response = ReqResAPI.createUser(payload,"reqres-free-v1");
		long responseTime = response.getTime();
		
		response.then().statusCode(not(OK));
		
		String body = response.asString().toLowerCase();
	    Assert.assertFalse(body.contains("sql"),        "SQL error leaked!");
	    Assert.assertFalse(body.contains("exception"),  "Stack trace leaked!");
	}
	
	@Test(priority = 8, groups = {"smoke"})
	public void validateDelayedResponseDataSize() {
		Response response = ReqResAPI.delayedResponse("reqres-free-v1");
		long responseTime = response.getTime();
		
		response.then().statusCode(OK);
		response.then()
			.body("total", equalTo(12))
			.body("data.size()", equalTo(6));
	}
	
	@Test(priority = 9, groups = {"regression"})
	public void validateDelayedResponseAdvancedAssertions() {
		Response response = ReqResAPI.delayedResponse("reqres-free-v1");
		long responseTime = response.getTime();
		
		response.then().statusCode(OK);
		Assert.assertTrue(responseTime < 10000, "API took too long to respond: " + responseTime + " ms");
		response.then()
			.body("data.find { it.id == 2 }.first_name", equalTo("Janet"))
			.body("data.findAll { it.email.contains('@reqres.in') }.size()", equalTo(6));
	}
	
	@Test(priority = 10, groups = {"regression"})
	public void validateCachingGetSingleuser() {
		Response response = ReqResAPI.getSingleUser("2","reqres-free-v1",false,"");
		long responseTime = response.getTime();
		
		response.then().statusCode(OK);
		String etag = response.getHeader("ETag");
		
		Response responseCached = ReqResAPI.getSingleUser("2","reqres-free-v1",true,etag);
		responseCached.then().statusCode(NOT_MODIFIED);
	}
	
	/*
	 * This test is for demonstration purpose and not for execution
	 */
	@Test(priority = 11, groups = {"dummy"})
	public void validateDataIntegrityCreateUser() {
		String payload = ReadJsonFile.getJsonString("requestdata/createUserSuccessfullyPayload.json");
		Response response = ReqResAPI.createUser(payload,"reqres-free-v1");
		long responseTime = response.getTime();
		
		response.then().statusCode(equalTo(CREATED));
		response.then().body("name", equalTo("saurav"));
		response.then().body("job", equalTo("QA"));
		int userId = response.jsonPath().getInt("id");
		
		response = ReqResAPI.getSingleUser(Integer.toString(userId),"reqres-free-v1",false,"");
		response.then().statusCode(equalTo(OK));
		
		response = ReqResAPI.deleteSingleUser(Integer.toString(userId),"reqres-free-v1");
		response.then().statusCode(equalTo(NO_CONTENT));
		
		response = ReqResAPI.getSingleUser(Integer.toString(userId),"reqres-free-v1",false,"");
		response.then().statusCode(equalTo(NOT_FOUND));
	}

}
