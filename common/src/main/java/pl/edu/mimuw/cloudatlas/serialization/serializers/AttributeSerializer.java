package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.model.Attribute;

public class AttributeSerializer extends Serializer<Attribute> {
	@Override
	public void write(Kryo kryo, Output output, Attribute object) {
		output.writeString(object.getName());
	}

	@Override
	public Attribute read(Kryo kryo, Input input, Class<? extends Attribute> type) {
		return new Attribute(input.readString());
	}
}
