package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueList;

import java.util.ArrayList;

public class ValueListSerializer extends Serializer<ValueList> {
	@Override
	public void write(Kryo kryo, Output output, ValueList object) {
		kryo.writeClassAndObject(output, ((TypeCollection) object.getType()).getElementType());
		kryo.writeObject(output, new ArrayList<>(object.getValue()));
	}

	@Override
	public ValueList read(Kryo kryo, Input input, Class<? extends ValueList> type) {
		Type t = (Type) kryo.readClassAndObject(input);
		ArrayList<Value> list = kryo.readObject(input, ArrayList.class);
		return new ValueList(list, t);
	}
}
