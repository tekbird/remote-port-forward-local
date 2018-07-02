package client;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;

import org.pmw.tinylog.Logger;

public class WebSocketMessageHandler implements Whole<ByteBuffer> {

	private AsynchronousSocketChannel serverSock;

	public WebSocketMessageHandler(AsynchronousSocketChannel serverSock) {
		this.serverSock = serverSock;
	}

	@Override
	public void onMessage(ByteBuffer message) {
		int limit = message.limit();
		Logger.info("received {} bytes from websocket", limit);
		try {
			this.serverSock.write(message).get();
			Logger.info("written {} bytes to service socket", limit);
		} catch (InterruptedException | ExecutionException e) {
			Logger.error("failed to write data received from websocket to local tcp socket with error {}",
					e.getMessage());
		}
	}

}
