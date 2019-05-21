package paint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SocketServer extends java.lang.Thread {
	Socket Server;
	Queue<Message> messages = new LinkedList<Message>();
	ObjectInputStream in;
	ObjectOutputStream out;
	GZIPInputStream gzipin;
	GZIPOutputStream gzipout;

	public SocketServer(Socket Server) {
		this.Server = Server;
	}

	public void write(Message message) throws IOException {
		System.out.println(out);
		try {
			out.writeObject(message);
			out.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("連接成功 : InetAddress = " + Server.getInetAddress());
		try {
			gzipout = new GZIPOutputStream(Server.getOutputStream());
			out = new ObjectOutputStream(gzipout);
			gzipin = new GZIPInputStream(Server.getInputStream());
			in = new ObjectInputStream(gzipin);
			while (Server.isConnected()) {
				Message data = (Message) in.readObject();
				System.out.println("客戶端訊息:\n" + data.history.toString());
				messages.add(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (Server != null) {
				in.close();
				out.close();
				Server.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
