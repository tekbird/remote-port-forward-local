package net.portforward.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.pmw.tinylog.Logger;

@ServerEndpoint(value = "/ep/{port}")
public class EndPoint {

	private Runnable onCloseCallback;

	@OnOpen
	public void onOpen(Session session, @PathParam("port") int port) throws IOException {
		Logger.info("onOpen: {}", session.getId());
		IncomingMessageHandler incomingHandler = new IncomingMessageHandler();
		session.addMessageHandler(ByteBuffer.class, incomingHandler);
		AsynchronousSocketChannel sockChannel = AsynchronousSocketChannel.open();
		InetSocketAddress address = new InetSocketAddress("localhost", port);
		sockChannel.connect(address, sockChannel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
			@Override
			public void completed(Void result, AsynchronousSocketChannel socketChannel) {
				Logger.info("connected to service socket");
				setOnClose(() -> {
					try {
						socketChannel.close();
						Logger.info("closed socket to server");
					} catch (IOException e) {
						Logger.error("failed to close socket with error {}", e.getMessage());
					}
				});
				incomingHandler.setLocalSocket(socketChannel);
				startRead(session, socketChannel);
			}

			private void startRead(Session session, AsynchronousSocketChannel sockChannel) {

				Logger.info("starting to read from service socket");
				final ByteBuffer buffer = ByteBuffer.allocate(4096);

				sockChannel.read(buffer, null, new CompletionHandler<Integer, Void>() {

					@Override
					public void completed(Integer length, Void v) {
						if (length == -1) {
							Logger.warn("socket connection is being closed");
							try {
								session.close();
							} catch (IOException e) {
								Logger.error("failed to close websocket connection with error {}", e.getMessage());
							}
							return;
						}
						if (length > 0) {
							buffer.flip();
							try {
								Logger.info("sending {} bytes to websocket", buffer.limit());
								session.getBasicRemote().sendBinary(buffer);
							} catch (IOException e) {
								Logger.error("failed to send to websocket with error {}", e.getMessage());
							}
						}
						startRead(session, sockChannel);
					}

					@Override
					public void failed(Throwable exc, Void v) {
						Logger.error("failed to read message from socket with error {}", exc.getMessage());
						try {
							session.close();
						} catch (IOException e) {
							Logger.error("failed to close websocket session with error {}", e.getMessage());
						}
					}

				});
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel channel) {
				Logger.info("failed to connect to socket with error {}", exc.getMessage());
			}

		});

	}

	protected void setOnClose(Runnable callback) {
		this.onCloseCallback = callback;
	}

	@OnClose
	public void onClose(Session session) throws IOException {
		Logger.warn("websocket connection is closed");
		if (this.onCloseCallback != null) {
			this.onCloseCallback.run();
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
	}
}
