package pl.edu.mimuw.cloudatlas.agent.comm;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.OutNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.receiver.ReceiverThread;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;

import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class CommModule extends Module {
	public static final int RECEIVER_PORT = 31337;
	public static final int MAX_DATAGRAM_SIZE = 512;
	public static final int MIN_HEADER_SIZE = 8;
	public static final int FIRST_HEADER_ADDITION = 4;
	public static final int FIRST_HEADER_SIZE = MIN_HEADER_SIZE + FIRST_HEADER_ADDITION;

	private Pool<Kryo> kryoPool;
	private ExecutorService executorService = Executors.newFixedThreadPool(2);
	private ReceiverThread receiver;
	private SenderThread sender;
	private BlockingQueue<OutNetworkMessage> senderQueue = new LinkedBlockingQueue<>();

	public CommModule(Bus bus, AgentConfig config) throws SocketException {
		super(bus);
		initializeKryo();
		this.receiver = new ReceiverThread(bus, config, kryoPool.obtain());
		this.sender = new SenderThread(config, kryoPool.obtain(), senderQueue);
	}

	@Override
	public void init() {
		executorService.submit(receiver);
		executorService.submit(sender);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_COMM_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof OutNetworkMessage) {
			handleOutNetworkMessage((OutNetworkMessage) message);
		} else {
			System.out.println("Comm module received message of unhandled type. Ignoring.");
		}
	}

	private void handleOutNetworkMessage(OutNetworkMessage message) {
		try {
			senderQueue.put(message);
		} catch (InterruptedException e) {
			System.out.println("Comm module interrupted. Shutting down.");
			System.exit(1);
		}
	}

	private void initializeKryo() {
		kryoPool = new KryoPool();
	}
}
