package paint;

public class Message implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String type;
	History history = new History();
	

	public Message(History history) {
		// TODO Auto-generated constructor stub
		type = "History";
		this.history = history;
	}
	
	public Message(String type) {
		// TODO Auto-generated constructor stub
		this.type = type;
	}
	
	public String toString() {
		return type+"\n";
	}
}