package paint;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SocketClient extends java.lang.Thread {
	Socket client;
	ObjectInputStream in;
	ObjectOutputStream out;
	GZIPInputStream gzipin;
	GZIPOutputStream gzipout;
	Queue<Message> messages = new LinkedList<Message>();
	String address = "127.0.0.1";
	int port = 30000;

	public void set_address(String address) {
		this.address = address;
	}

	public void write(Message message) throws IOException {
		System.out.println(out);
		out.writeObject(message);
		out.reset();
	}

	public SocketClient() {
		try {
			client = new Socket();
		} catch (Exception e) {
			System.out.println("Socket出錯 !");
		}
	}

	public void run() {
		try {
			InetSocketAddress isa = new InetSocketAddress(this.address, this.port);
			client.connect(isa, 10000);
			System.out.println("連接成功!");
			gzipout = new GZIPOutputStream(client.getOutputStream());
			out = new ObjectOutputStream(gzipout);
			gzipin = new GZIPInputStream(client.getInputStream());
			in = new ObjectInputStream(gzipin);
			while (client.isConnected()) {
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
			if (client != null) {
				in.close();
				out.close();
				client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
