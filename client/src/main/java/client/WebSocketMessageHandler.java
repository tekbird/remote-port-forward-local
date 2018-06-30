package client;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.websocket.MessageHandler.Partial;

import org.pmw.tinylog.Logger;

public class WebSocketMessageHandler implements Partial<ByteBuffer> {

	private AsynchronousSocketChannel serverSock;

	public WebSocketMessageHandler(AsynchronousSocketChannel serverSock) {
		this.serverSock = serverSock;
	}

	@Override
	public void onMessage(ByteBuffer partialMessage, boolean last) {
		Logger.info("received {} bytes from remote server", partialMessage.limit());
		try {
			Future<Integer> writeTask = this.serverSock.write(partialMessage);
			Integer writtenLength = writeTask.get();
			Logger.info("sent {} bytes to socket channel", writtenLength);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

}