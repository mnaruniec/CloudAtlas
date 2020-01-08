package pl.edu.mimuw.cloudatlas.agent.comm;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.OutNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.common.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class SenderThread implements Runnable {
	public static final long SOCKET_CREATION_INTERVAL_MS = 1000;

	public Random random = new Random();

	private BlockingQueue<OutNetworkMessage> queue;

	private DatagramSocket socket;

	public SenderThread(BlockingQueue<OutNetworkMessage> queue) throws SocketException {
		this.queue = queue;
		this.socket = new DatagramSocket();
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
		int transmissionId = getNextTransmissionId();
		int numDatagrams = (int) Math.ceil(
				(buffer.length + CommModule.FIRST_HEADER_ADDITION)
				/ (double)CommModule.MAX_DATAGRAM_SIZE
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

			sendDatagram(datagram);

			left -= bodyLen;
			offset += bodyLen;
		}
	}

	private void sendDatagram(DatagramPacket datagram) {
		while (true) {
			try {
				socket.send(datagram);
				return;
			} catch (Exception e) {
				System.out.println("Exception in socket.send. Trying to open new socket.");
				e.printStackTrace();
				restoreSocket();
			}
		}
	}

	private int getNextTransmissionId() {
		// TODO - consider deterministic
		return random.nextInt();
	}

	private byte[] serialize(Message message) {
		// TODO
		return new byte[1];
	}


	private void restoreSocket() {
		do {
			if (socket != null) {
				socket.close();
				socket = null;
			}

			try {
				socket = new DatagramSocket();
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
		// TODO - consider using timer
	}
}
