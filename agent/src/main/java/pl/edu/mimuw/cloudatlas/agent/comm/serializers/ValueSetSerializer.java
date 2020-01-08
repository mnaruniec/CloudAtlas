package pl.edu.mimuw.cloudatlas.agent.comm.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueSet;

import java.util.HashSet;

public class ValueSetSerializer extends Serializer<ValueSet> {
	@Override
	public void write(Kryo kryo, Output output, ValueSet object) {
		kryo.writeClassAndObject(output, ((TypeCollection) object.getType()).getElementType());
		kryo.writeObject(output, new HashSet<>(object.getValue()));
	}

	@Override
	public ValueSet read(Kryo kryo, Input input, Class<? extends ValueSet> type) {
		Type t = (Type) kryo.readClassAndObject(input);
		HashSet<Value> set = kryo.readObject(input, HashSet.class);
		return new ValueSet(set, t);
	}
}
