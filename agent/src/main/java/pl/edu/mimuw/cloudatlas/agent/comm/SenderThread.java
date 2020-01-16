package pl.edu.mimuw.cloudatlas.agent.comm;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.OutNetworkMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class SenderThread implements Runnable {
	public static final long SOCKET_CREATION_INTERVAL_MS = 1000;

	private int nextTransmissionId;

	private InetAddress localAddress;
	private Kryo kryo;
	private BlockingQueue<OutNetworkMessage> queue;
	private DatagramSocket socket;

	public SenderThread(AgentConfig config, Kryo kryo, BlockingQueue<OutNetworkMessage> queue) throws SocketException {
		this.localAddress = config.getIP();
		this.kryo = kryo;
		this.queue = queue;
		this.socket = createSocket();
		// randomized in case server gets quickly restarted
		this.nextTransmissionId = new Random().nextInt(Integer.MAX_VALUE) + Integer.MIN_VALUE;
	}

	@Override
	public void run() {
		System.out.println("Communication sender thread started.");
		// TODO - add generic trycatch to every infinite loop
		while (true) {
			try {
				OutNetworkMessage message = queue.take();
				handleMessage(message);
			} catch (InterruptedException e) {
				System.out.println("InterruptedException caught in communication sender thread. Shutting down.");
				System.exit(1);
			} catch (Exception e) {
				System.out.println("Unexpected exception caught in communication sender thread. Ignoring.");
				e.printStackTrace();
			}
		}
	}

	private void handleMessage(OutNetworkMessage message) {
		byte[] buffer = serialize(message);
		if (buffer == null) {
			return;
		}

		int transmissionId = getNextTransmissionId();
		int numDatagrams = (int) Math.ceil(
				(buffer.length + CommModule.FIRST_HEADER_ADDITION)
				/ (double)(CommModule.MAX_DATAGRAM_SIZE - CommModule.MIN_HEADER_SIZE)
		);

		int offset = 0;
		int left = buffer.length;
		byte[] datagramBuffer = new byte[CommModule.MAX_DATAGRAM_SIZE];

		for (int i = 0; i < numDatagrams; ++i) {
			ByteBuffer wrapper = ByteBuffer.wrap(datagramBuffer);
			wrapper.putInt(transmissionId).putInt(i);
			int headerSize;

			if (i == 0) {
				wrapper.putInt(numDatagrams);
				wrapper.putLong(0L);  // placeholder for timestamp
				headerSize = CommModule.FIRST_HEADER_SIZE;
			} else {
				headerSize = CommModule.MIN_HEADER_SIZE;
			}

			int totalLen = Math.min(CommModule.MAX_DATAGRAM_SIZE, headerSize + left);
			int bodyLen = totalLen - headerSize;
			wrapper.put(buffer, offset, bodyLen);
			DatagramPacket datagram = new DatagramPacket(
					datagramBuffer,
					totalLen,
					message.destAddress,
					CommModule.RECEIVER_PORT
			);

			sendDatagram(datagram, i == 0);

			left -= bodyLen;
			offset += bodyLen;
		}

		if (left != 0 || offset != buffer.length) {
			System.out.println("Sender thread sent all datagrams, but the counters don't match. Ignoring.");
		}
	}

	private void sendDatagram(DatagramPacket datagram, boolean isFirst) {
		while (true) {
			try {
				if (isFirst) {
					refreshTimestamp(datagram);
				}
				socket.send(datagram);
				return;
			} catch (Exception e) {
				System.out.println("Exception in socket.send. Trying to open new socket.");
				e.printStackTrace();
				restoreSocket();
			}
		}
	}

	private void refreshTimestamp(DatagramPacket datagram) {
		ByteBuffer buffer = ByteBuffer.wrap(datagram.getData());
		buffer.putLong(CommModule.TIMESTAMP_OFFSET, new Date().getTime());
	}

	private int getNextTransmissionId() {
		return ++nextTransmissionId;
	}

	private byte[] serialize(OutNetworkMessage message) {
		try {
			Output output = new Output(CommModule.MAX_DATAGRAM_SIZE, -1);
			kryo.writeClassAndObject(output, message.payload);
			return output.toBytes();
		} catch (Exception e) {
			System.out.println("Exception thrown when trying to serialize the payload. Ignoring.");
			e.printStackTrace();
			return null;
		}
	}

	private void restoreSocket() {
		do {
			if (socket != null) {
				socket.close();
				socket = null;
			}

			try {
				socket = createSocket();
				break;
			} catch (Exception e) {
				System.out.println("Exception when creating new sender socket. Trying again.");
				e.printStackTrace();
				try {
					Thread.sleep(SOCKET_CREATION_INTERVAL_MS);
				} catch (InterruptedException ex) {
					System.out.println("Communication sender thread interrupted. Shutting down.");
					System.exit(1);
				}
			}
		} while (socket == null || socket.isClosed() || socket.getLocalPort() == CommModule.RECEIVER_PORT);
	}

	private DatagramSocket createSocket() throws SocketException {
		return new DatagramSocket(0, localAddress);
	}
}
