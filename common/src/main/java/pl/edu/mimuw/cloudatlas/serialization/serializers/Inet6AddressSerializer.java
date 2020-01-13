package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.net.Inet6Address;

public class Inet6AddressSerializer extends Serializer<Inet6Address> {
	@Override
	public void write(Kryo kryo, Output output, Inet6Address object) {
		output.write(object.getAddress());
	}

	@Override
	public Inet6Address read(Kryo kryo, Input input, Class<? extends Inet6Address> type) {
		try {
			return (Inet6Address) Inet6Address.getByAddress(input.readBytes(16));
		} catch (Exception e) {
			return null;
		}
	}
}
