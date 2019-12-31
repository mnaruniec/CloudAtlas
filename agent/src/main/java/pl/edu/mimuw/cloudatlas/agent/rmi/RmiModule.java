package pl.edu.mimuw.cloudatlas.agent.rmi;

import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiModule extends Module {
	AgentAPI agentAPI;

	public RmiModule(Bus bus) throws RemoteException {
		super(bus);
		agentAPI = new AgentAPI(bus);
		IAgentAPI stub =
				(IAgentAPI) UnicastRemoteObject.exportObject(agentAPI, 0);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind("AgentAPI", stub);
		System.out.println("AgentAPI bound in RMI registry.");
	}

	@Override
	public String getDefaultName() {
		return "rmi";
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof RmiMessage) {
			agentAPI.registerResponse((RmiMessage) message);
		} else {
			System.out.println("Rmi module received non-rmi message. Ignoring");
		}
	}
}
