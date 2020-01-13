package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.model.PathName;

public class PathNameSerializer extends Serializer<PathName> {
	@Override
	public void write(Kryo kryo, Output output, PathName object) {
		output.writeString(object.getName());
	}

	@Override
	public PathName read(Kryo kryo, Input input, Class<? extends PathName> type) {
		return new PathName(input.readString());
	}
}
