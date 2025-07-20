package base;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import io.restassured.RestAssured;
import mockutils.StubUtils;
import mockutils.WireMockSetup;
import utils.ConfigReader;

public class ReqResBase {
	
	@Parameters({"envr","mock"})
	@BeforeSuite(alwaysRun = true)
    public void setup(String envr, boolean mock) {
		if(mock)
		{
			WireMockSetup.startServer();
	        configureFor("localhost", 8084);
	        StubUtils.registerAllStubs();
	        
	        RestAssured.baseURI = ConfigReader.get("mockURI");
		}
		else
		{
	        switch (envr) {
	            case "prod": 
	            	RestAssured.baseURI = ConfigReader.get("baseURI_PROD");
	            	break;
	            case "stage": 
	            	RestAssured.baseURI = ConfigReader.get("baseURI_STG");
	            	break;
	            case "dev": 
	            	RestAssured.baseURI = ConfigReader.get("baseURI_DEV");
	            	break;
	            default: 
	            	RestAssured.baseURI = ConfigReader.get("baseURI_DEV");
	            	break;
	        }
		}
    }
	
	@AfterSuite(alwaysRun = true)
    public void afterSuite() {
        WireMockSetup.stopServer();
    }
}
