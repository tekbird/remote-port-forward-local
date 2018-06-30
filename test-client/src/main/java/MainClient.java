import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;

import org.pmw.tinylog.Logger;

public class MainClient {

	public static void main(String[] args) throws IOException {
		AsynchronousSocketChannel sockChannel = AsynchronousSocketChannel.open();
		sockChannel.connect(new InetSocketAddress("localhost", 24), sockChannel,
				new CompletionHandler<Void, AsynchronousSocketChannel>() {

					@Override
					public void completed(Void result, AsynchronousSocketChannel attachment) {
						Logger.info("successfully connected");
						// SendReady();
						StartRead(attachment);
					}

					@Override
					public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
						Logger.error("failed to connect");
					}
				});

		while (true) {
			Logger.info("waiting to read data");
			int byteRead = System.in.read();
			sockChannel.write(ByteBuffer.wrap(new byte[] { (byte) byteRead }));
		}
	}

	protected static void StartRead(AsynchronousSocketChannel socket) {
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		if (socket.isOpen()) {
			socket.read(buffer, socket, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

				@Override
				public void completed(Integer size, AsynchronousSocketChannel attachment) {
					if (size != -1) {
						Logger.info("read {} bytes", size);
					}
					StartRead(attachment);
				}

				@Override
				public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
					Logger.error("failed to read");
				}

			});
		} else {
			Logger.error("read stopped, connection is closed");
		}
	}

}
