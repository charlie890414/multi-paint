package paint;

public class Message implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String type;
	History history = new History();
	

	public Message(History history) {
		type = "History";
		this.history = history;
	}
	
	public Message(String type) {
		this.type = type;
	}
	
	public String toString() {
		return type;
	}
}