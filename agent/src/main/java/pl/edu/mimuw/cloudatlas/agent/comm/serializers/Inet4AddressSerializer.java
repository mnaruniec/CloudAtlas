package pl.edu.mimuw.cloudatlas.agent.comm.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.net.Inet4Address;

public class Inet4AddressSerializer extends Serializer<Inet4Address> {
	@Override
	public void write(Kryo kryo, Output output, Inet4Address object) {
		output.write(object.getAddress());
	}

	@Override
	public Inet4Address read(Kryo kryo, Input input, Class<? extends Inet4Address> type) {
		try {
			return (Inet4Address) Inet4Address.getByAddress(input.readBytes(4));
		} catch (Exception e) {
			return null;
		}
	}
}
