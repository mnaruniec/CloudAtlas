package pl.edu.mimuw.cloudatlas.agent.comm.receiver;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import pl.edu.mimuw.cloudatlas.agent.comm.CommModule;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.InNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.local.TriggerTransmissionTimeoutMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.Payload;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.timer.SetTimeoutMessage;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Transmission {
	public final TransmissionId transmissionId;

	private Bus bus;
	private Kryo kryo;
	private int numDatagrams = 0;
	private boolean finished = false;

	private Map<Integer, DatagramPacket> datagramMap = new HashMap<>();

	public Transmission(Bus bus, Kryo kryo, TransmissionId transmissionId, long timeout) {
		this.bus = bus;
		this.kryo = kryo;
		this.transmissionId = transmissionId;
		setTransmissionTimeout(timeout);
	}

	public void insertDatagram(DatagramPacket datagram) {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(
					datagram.getData(),
					datagram.getOffset() + 4,
					datagram.getLength() - 4
			);

			int sequenceNum = byteBuffer.getInt();
			if (sequenceNum == 0) {
				if (datagram.getLength() < CommModule.FIRST_HEADER_SIZE) {
					finishWithError("Transmission received first packet with too short header.");
					return;
				}
				numDatagrams = byteBuffer.getInt();
				if (numDatagrams <= 0) {
					finishWithError("Transmission received first packet with non-positive datagrams number value.");
					return;
				}
			} else if (sequenceNum < 0) {
				finishWithError("Transmission received packet with negative sequence number.");
				return;
			} else if (numDatagrams != 0 && sequenceNum >= numDatagrams) {
				finishWithError(
						"Transmission received packet with sequence number "
								+ sequenceNum + ", expected only "
								+ numDatagrams + " datagrams."
				);
				return;
			}

			if (datagramMap.containsKey(sequenceNum)) {
				finishWithError("Transmission received packet with duplicate sequence number.");
				return;
			}

			datagramMap.put(sequenceNum, datagram);

			finishIfNeeded();
		} catch (Exception e) {
			finishWithError("Unexpected exception caught when inserting datagram to transmission.");
			e.printStackTrace();
		}
	}

	public boolean isFinished() {
		return finished;
	}

	private void finishIfNeeded() {
		if (numDatagrams == 0 || datagramMap.size() < numDatagrams) {
			return;
		}

		try {
			DatagramPacket firstDatagram = datagramMap.get(0);
			int bufferSize = firstDatagram.getLength() - CommModule.FIRST_HEADER_SIZE;

			for (int i = 1; i < numDatagrams; ++i) {
				DatagramPacket datagram = datagramMap.get(i);
				if (datagram == null) {
					finishWithError("Transmission received packet with too large sequence number.");
					return;
				}
				bufferSize += datagram.getLength() - CommModule.MIN_HEADER_SIZE;
			}

			ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
			buffer.put(
					firstDatagram.getData(),
					firstDatagram.getOffset() + CommModule.FIRST_HEADER_SIZE,
					firstDatagram.getLength() - CommModule.FIRST_HEADER_SIZE
			);

			for (int i = 1; i < numDatagrams; ++i) {
				DatagramPacket datagram = datagramMap.get(i);
				buffer.put(
						datagram.getData(),
						datagram.getOffset() + CommModule.MIN_HEADER_SIZE,
						datagram.getLength() - CommModule.MIN_HEADER_SIZE
				);
			}

			Payload payload = deserialize(buffer.array());
			if (payload == null) {
				return;
			}

			bus.sendMessage(new InNetworkMessage(
					Constants.DEFAULT_GOSSIP_MODULE_NAME,
					Constants.DEFAULT_COMM_MODULE_NAME,
					transmissionId.address,
					payload
			));
		} finally {
			finished = true;
		}
	}

	private Payload deserialize(byte[] buffer) {
		try {
			Input input = new Input(buffer);
			return (Payload) kryo.readClassAndObject(input);
		} catch (Exception e) {
			finishWithError("Exception thrown when trying to deserialize the Payload.");
			e.printStackTrace();
			return null;
		}
	}

	private void finishWithError(String message) {
		System.out.println(message + " Finishing.");
		finished = true;
	}

	private void setTransmissionTimeout(long timeout) {
		Runnable callback = () -> {
			bus.sendMessage(new TriggerTransmissionTimeoutMessage(
					Constants.DEFAULT_COMM_MODULE_NAME,
					Constants.DEFAULT_TIMER_MODULE_NAME,
					transmissionId
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				Constants.DEFAULT_TIMER_MODULE_NAME,
				Constants.DEFAULT_COMM_MODULE_NAME,
				callback,
				timeout
		));
	}
}
