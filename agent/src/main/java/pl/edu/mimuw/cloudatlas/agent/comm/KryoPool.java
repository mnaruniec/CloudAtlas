package pl.edu.mimuw.cloudatlas.agent.comm;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;
import pl.edu.mimuw.cloudatlas.serialization.KryoInitializer;

public class KryoPool extends Pool<Kryo> {
	public KryoPool() {
		super(true, false, 2);
	}

	@Override
	protected Kryo create() {
		Kryo kryo = new Kryo();
		KryoInitializer.initializeKryo(kryo);
		registerLocalClasses(kryo);
		return kryo;
	}

	private static void registerLocalClasses(Kryo kryo) {
		kryo.register(FreshnessInfoRequestPayload.class);
		kryo.register(FreshnessInfoResponsePayload.class);
		kryo.register(DataRequestPayload.class);
		kryo.register(DataResponsePayload.class);

		kryo.register(FreshnessInfo.class);
		kryo.register(GossipData.class);
	}
}
