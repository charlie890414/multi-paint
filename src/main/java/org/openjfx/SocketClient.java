package org.openjfx;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SocketClient extends java.lang.Thread {
	Socket client;
	ObjectInputStream in;
	ObjectOutputStream out;
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
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
			while (client.isConnected()) {
				Message data = (Message) in.readObject();
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
