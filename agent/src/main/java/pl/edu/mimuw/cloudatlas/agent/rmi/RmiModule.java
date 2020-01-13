package pl.edu.mimuw.cloudatlas.agent.rmi;

import pl.edu.mimuw.cloudatlas.agent.rmi.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiResponse;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiModule extends Module {
	private AgentAPI agentAPI;

	public RmiModule(Bus bus) throws RemoteException {
		super(bus);
		agentAPI = new AgentAPI(bus);

	}

	@Override
	public void init() throws RemoteException {
		IAgentAPI stub =
				(IAgentAPI) UnicastRemoteObject.exportObject(agentAPI, 0);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind("AgentAPI", stub);
		System.out.println("AgentAPI bound in RMI registry.");
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_RMI_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof RmiResponse) {
			agentAPI.registerResponse((RmiResponse) message);
		} else {
			System.out.println("Rmi module received not an RmiResponse message. Ignoring");
		}
	}
}
