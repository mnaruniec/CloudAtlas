package pl.edu.mimuw.cloudatlas.agent.comm.receiver;

import com.esotericsoftware.kryo.Kryo;
import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.agent.comm.CommModule;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReceiverThread implements Runnable {
	public static final long SOCKET_CREATION_INTERVAL_MS = 1000;

	private InetAddress localAddress;
	private long transmissionTimeoutMs;

	private Bus bus;
	private Kryo kryo;
	private DatagramSocket socket;

	private Map<TransmissionId, Transmission> transmissionMap = new ConcurrentHashMap<>();

	public ReceiverThread(Bus bus, AgentConfig config, Kryo kryo) throws SocketException {
		this.localAddress = config.getIP();
		this.transmissionTimeoutMs = config.getUdpReceiveTimeoutMs();
		this.bus = bus;
		this.kryo = kryo;
		this.socket = createSocket();
	}

	@Override
	public void run() {
		System.out.println("Communication receiver thread started.");
		while (true) {
			try {
				DatagramPacket datagram = receiveDatagram();
				processDatagram(datagram);
			} catch (Exception e) {
				System.out.println("Unexpected exception caught in communication receiver thread. Ignoring.");
				e.printStackTrace();
			}
		}
	}

	public void timeoutTransmission(TransmissionId transmissionId) {
		// TODO - consider lazy for debug message
		transmissionMap.remove(transmissionId);
	}

	private void processDatagram(DatagramPacket datagram) {
		if (datagram.getLength() > CommModule.MAX_DATAGRAM_SIZE) {
			System.out.println("Receiver thread got a too long datagram. Ignoring.");
			return;
		}

		if (datagram.getLength() < CommModule.MIN_HEADER_SIZE) {
			System.out.println("Receiver thread got a too short datagram. Ignoring.");
			return;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(datagram.getData(), datagram.getOffset(), datagram.getLength());
		int transmissionToken = byteBuffer.getInt();
		TransmissionId transmissionId = new TransmissionId(datagram.getAddress(), transmissionToken);

		Transmission transmission = transmissionMap.computeIfAbsent(
				transmissionId,
				key -> new Transmission(bus, kryo, transmissionId, transmissionTimeoutMs)
		);

		insertDatagram(transmission, datagram);
	}

	private void insertDatagram(Transmission transmission, DatagramPacket datagram) {
		transmission.insertDatagram(datagram);
		if (transmission.isFinished()) {
			removeTransmission(transmission);
		}
	}

	private void removeTransmission(Transmission transmission) {
		transmissionMap.remove(transmission.transmissionId);
	}

	private DatagramPacket receiveDatagram() {
		byte[] buffer = new byte[CommModule.MAX_DATAGRAM_SIZE + 1];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (true) {
			try {
				socket.receive(packet);
				return packet;
			} catch (Exception e) {
				System.out.println("Exception in socket.receive. Trying to open new socket.");
				e.printStackTrace();
				restoreSocket();
			}
		}
	}

	private void restoreSocket() {
		if (socket != null) {
			socket.close();
		}

		while (true) {
			try {
				socket = createSocket();
				break;
			} catch (Exception e) {
				System.out.println("Exception when creating new receive socket. Trying again.");
				e.printStackTrace();
				try {
					Thread.sleep(SOCKET_CREATION_INTERVAL_MS);
				} catch (InterruptedException ex) {
					System.out.println("Communication receiver thread interrupted. Shutting down.");
					System.exit(1);
				}
			}
		}
	}

	private DatagramSocket createSocket() throws SocketException {
		return new DatagramSocket(CommModule.RECEIVER_PORT, localAddress);
	}
}
