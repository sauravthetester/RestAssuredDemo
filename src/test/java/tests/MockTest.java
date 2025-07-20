package tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static utils.StatusCodesUtil.OK;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.client.WireMock;

import base.ReqResBase;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import requests.ReqResAPI;
import utils.ConfigReader;
import utils.ReadJsonFile;

public class MockTest extends ReqResBase{
	
	@BeforeClass(alwaysRun = true)
	public void mockSetUp()
	{
		RestAssured.baseURI = ConfigReader.get("mockURI");
	}
	
	@Test(priority = 1, groups = {"mock"})
	public void validateSingleUserSuccess()
	{
		Response response = ReqResAPI.singleUserSuccess();
		long responseTime = response.getTime();
		
		response.then().statusCode(OK)
			.body("id", equalTo("123"))
			.body("name", equalTo("John Doe"));
        Assert.assertTrue(responseTime < 2500, "API took too long to respond: " + responseTime + " ms");						
	}

}
