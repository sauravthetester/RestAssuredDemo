package mockutils;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockSetup {
	
	public static WireMockServer wireMockServer;

    public static void startServer() {
        wireMockServer = new WireMockServer(8084);
        wireMockServer.start();
    }

    public static void stopServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

}
