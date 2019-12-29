package pl.edu.mimuw.cloudatlas.agent.common;

public abstract class Message {
	public final String dest;
	public final String src;

	public Message(String dest, String src) {
		this.dest = dest;
		this.src = src;
	}
}
