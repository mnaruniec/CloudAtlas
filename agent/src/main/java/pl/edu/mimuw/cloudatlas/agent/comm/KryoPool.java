package pl.edu.mimuw.cloudatlas.agent.comm;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.serializers.Inet4AddressSerializer;
import pl.edu.mimuw.cloudatlas.agent.comm.serializers.Inet6AddressSerializer;
import pl.edu.mimuw.cloudatlas.agent.comm.serializers.PathNameSerializer;
import pl.edu.mimuw.cloudatlas.agent.comm.serializers.ValueListSerializer;
import pl.edu.mimuw.cloudatlas.agent.comm.serializers.ValueSetSerializer;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.HashSet;

public class KryoPool extends Pool<Kryo> {
	public KryoPool() {
		super(true, false, 2);
	}

	@Override
	protected Kryo create() {
		Kryo kryo = new Kryo();

		kryo.register(Inet4Address.class, new Inet4AddressSerializer());
		kryo.register(Inet6Address.class, new Inet6AddressSerializer());
		kryo.register(PathName.class, new PathNameSerializer());
		kryo.register(ValueList.class, new ValueListSerializer());
		kryo.register(ValueSet.class, new ValueSetSerializer());

		kryo.register(ArrayList.class);
		kryo.register(HashSet.class);

		kryo.register(FreshnessInfoRequestPayload.class);
		kryo.register(FreshnessInfoResponsePayload.class);
		kryo.register(DataRequestPayload.class);
		kryo.register(DataResponsePayload.class);

		kryo.register(FreshnessInfo.class);

		kryo.register(Attribute.class);
		kryo.register(AttributesMap.class);
		kryo.register(TypeCollection.class);
		kryo.register(TypePrimitive.class);
		kryo.register(ValueBoolean.class);
		kryo.register(ValueContact.class);
		kryo.register(ValueDouble.class);
		kryo.register(ValueDuration.class);
		kryo.register(ValueInt.class);
		kryo.register(ValueNull.class);
		kryo.register(ValueString.class);
		kryo.register(ValueTime.class);
		// TODO - ValueQuery, ZMI

		return kryo;
	}
}
