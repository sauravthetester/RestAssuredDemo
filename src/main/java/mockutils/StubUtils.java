package mockutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import utils.ReadJsonFile;

public class StubUtils {
	
	private static String singleUserSuccessMockUrlPath = "/api/singleusers";
	private static String singleUserNotFoundMockUrlPath = "/api/user/999";
	
	private static String singleUserSuccessMockResponsePayload = ReadJsonFile.getJsonString("responsedata/SingleUserSuccessMockResponsePayload.json");
	private static String singleUserNotFoundMockResponsePayload = ReadJsonFile.getJsonString("responsedata/SingleUserNotFoundMockResponsePayload.json");

	public static void registerAllStubs()
	{
		stubGetSingleUserSuccess();
		stubGetSingleUserNotFound();
	}
	
	public static void stubGetSingleUserSuccess() {
		stubFor(get(urlEqualTo(singleUserSuccessMockUrlPath))
        		.withRequestBody(matchingJsonPath("$.id", matching("\\d{3}")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(singleUserSuccessMockResponsePayload)));
	}
	
	public static void stubGetSingleUserNotFound() {
		stubFor(get(urlEqualTo(singleUserNotFoundMockUrlPath))
	            .willReturn(aResponse()
	                .withStatus(404)
	                .withHeader("Content-Type", "application/json")
	                .withBody(singleUserNotFoundMockResponsePayload)));
	}
}
