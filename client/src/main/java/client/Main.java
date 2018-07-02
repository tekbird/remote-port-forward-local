package client;

import java.io.IOException;
import java.net.URI;

import javax.websocket.DeploymentException;

import org.pmw.tinylog.Logger;

public class Main {

	public static void main(String[] args) throws IOException, DeploymentException {
		Logger.getConfiguration()
				.formatPattern("[{date:yyyy-MM-dd HH:mm:ss.S} {level}] [{thread}] {class}.{method}: {message}")
				.activate();
		Logger.info("client starting");
		if (args.length == 3) {
			Integer localPort = 5433;// Integer.parseInt(args[0]);
			Integer remotePort = 5432;// Integer.parseInt(args[1]);
			String websocketUrl = "ws://localhost:8080/server/ep";// args[2];
			String serverUri = websocketUrl + "/" + remotePort;
			registerShutdown();
			PortAccessService portAccessService = new PortAccessService();
			portAccessService.start(localPort, URI.create(serverUri));
			System.in.read();
		} else {
			Logger.error("invalid parameters. usage: client <local-port> <remote-port> <websocket-url>");
		}
	}

	private static void registerShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Logger.info("application shutting down");
			}
		});
	}
}
