package org.openjfx;

public class Message implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	String type;
	History history;
	Parameter parameter;

	public Message(History history) {
		type = "History";
		this.history = history;
	}

	public Message(Parameter parameter) {
		type = "Parameter";
		this.parameter = parameter;
	}

	public Message(String type) {
		this.type = type;
	}

	public String toString() {
		return type;
	}
}