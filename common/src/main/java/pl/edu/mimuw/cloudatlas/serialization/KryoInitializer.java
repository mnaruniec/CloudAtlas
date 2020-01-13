package pl.edu.mimuw.cloudatlas.serialization;

import com.esotericsoftware.kryo.Kryo;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Type;
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
import pl.edu.mimuw.cloudatlas.serialization.serializers.Inet4AddressSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.Inet6AddressSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.PathNameSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.ProgramSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.SignedInstallationSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.SignedUninstallationSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.ValueListSerializer;
import pl.edu.mimuw.cloudatlas.serialization.serializers.ValueSetSerializer;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.InstallationPayload;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.UninstallationPayload;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class KryoInitializer {
	private KryoInitializer() {}

	public static void initializeKryo(Kryo kryo) {

		kryo.register(Inet4Address.class, new Inet4AddressSerializer());
		kryo.register(Inet6Address.class, new Inet6AddressSerializer());
		kryo.register(PathName.class, new PathNameSerializer());
		kryo.register(ValueList.class, new ValueListSerializer());
		kryo.register(ValueSet.class, new ValueSetSerializer());
		kryo.register(Program.class, new ProgramSerializer());
		kryo.register(SignedInstallation.class, new SignedInstallationSerializer());
		kryo.register(SignedUninstallation.class, new SignedUninstallationSerializer());

		kryo.register(ArrayList.class);
		kryo.register(HashSet.class);
		kryo.register(HashMap.class);

		kryo.register(Attribute.class);
		kryo.register(AttributesMap.class);
		kryo.register(Type.PrimaryType.class);
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

		kryo.register(InstallationPayload.class);
		kryo.register(UninstallationPayload.class);
	}
}
