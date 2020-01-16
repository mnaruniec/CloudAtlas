package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.model.PathName;

public final class FreshnessInfoRequestPayload extends FreshnessInfoPayload implements NetworkRequestPayload {
	private String pathName;

	private FreshnessInfoRequestPayload() {}

	public FreshnessInfoRequestPayload(PathName pathName, FreshnessInfo freshnessInfo) {
		super(freshnessInfo);
		this.pathName = pathName.toString();
	}

	public String getPathName() {
		return pathName;
	}
}
