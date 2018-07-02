package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.websocket.*;
import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

@ClientEndpoint
public class WebSocketEndpoint extends Endpoint {

	private AsynchronousSocketChannel serverSock;

	public WebSocketEndpoint(AsynchronousSocketChannel sockChannel) {
		this.serverSock = sockChannel;
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		Whole<ByteBuffer> onMessageHandler = new WebSocketMessageHandler(serverSock);
		session.addMessageHandler(ByteBuffer.class, onMessageHandler);
		Logger.info("connected to remote server");
		startRead(session);
	}

	private void startRead(Session session) {

		final ByteBuffer buffer = ByteBuffer.allocate(2048);

		this.serverSock.read(buffer, null, new CompletionHandler<Integer, Void>() {
			@Override
			public void completed(Integer length, Void v) {
				if (length == -1) {
					Logger.warn("socket connection is being closed");
					try {
						session.close();
						Logger.info("closed websocket connection");
					} catch (IOException e) {
						Logger.error("failed to close websocket connection with error {}", e.getMessage());
					}
				}
				if (length > 0) {
					buffer.flip();
					try {
						Logger.info("sending {} bytes to websocket", buffer.limit());
						session.getBasicRemote().sendBinary(buffer);
					} catch (IOException e) {
						Logger.error("failed to send data to remote server with error {}", e.getMessage());
					}
				}
				if (serverSock.isOpen()) {
					startRead(session);
				}
			}

			@Override
			public void failed(Throwable exc, Void v) {
				Logger.error("failed to read from socket with error {}", exc.getMessage());
			}

		});
	}

	@Override
	public void onClose(Session session, CloseReason reason) {
		Logger.warn("closing websocket connection");
		try {
			this.serverSock.close();
			Logger.info("closed socket connection");
		} catch (IOException e) {
			Logger.error("failed to close socket connection with error {}", e.getMessage());
		}
	}
}
