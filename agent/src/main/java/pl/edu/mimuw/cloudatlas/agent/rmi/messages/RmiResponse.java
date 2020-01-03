package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiResponse extends RmiMessage {
	public final RuntimeException exception;

	public RmiResponse(String dest, String src, long requestId, RuntimeException exception) {
		super(dest, src, requestId);
		this.exception = exception;
	}

	public RmiResponse(String dest, String src, long requestId) {
		this(dest, src, requestId, null);
	}

	// creates response to a request
	public RmiResponse(RmiMessage request) {
		this(request.src, request.dest, request.requestId, null);
	}

	// creates response to a request
	public RmiResponse(RmiMessage request, RuntimeException exception) {
		this(request.src, request.dest, request.requestId, exception);
	}
}
