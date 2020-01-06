package pl.edu.mimuw.cloudatlas.agent.comm.receiver;

import java.net.InetAddress;

public class TransmissionId {
	private InetAddress address;
	private int transmissionToken;

	public TransmissionId(InetAddress address, int transmissionToken) {
		this.address = address;
		this.transmissionToken = transmissionToken;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TransmissionId)) {
			return false;
		}
		return address == ((TransmissionId) o).address
				&& transmissionToken == ((TransmissionId) o).transmissionToken;
	}

	@Override
	public int hashCode() {
		return address.hashCode() * 1000003 + transmissionToken;
	}
}
