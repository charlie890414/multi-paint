package paint;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SocketClient extends java.lang.Thread {
	private Socket client;
	ObjectInputStream in;
	static ObjectOutputStream out;
	Canvas canvas;
	GraphicsContext gc;
	Parameter parameter;
	String address = "127.0.0.1";
	private int port = 30000;

	public void set_canvas(Canvas canvas) {
		this.canvas = canvas;
		this.gc = canvas.getGraphicsContext2D();
	}

	public void set_parameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public void set_address(String address) {
		this.address = address;
	}

	private void update(History tmphistory) {
		tmphistory.mergecolor();
		if (parameter.history.size() > parameter.history_pointer) {
			parameter.history.subList(parameter.history_pointer, parameter.history.size()).clear();
		}
		parameter.history.add(tmphistory);
		parameter.history_pointer++;
		gc.beginPath();
		if (tmphistory.DRAW_MOD.equals("PENCIL")) {
			gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
			gc.setStroke(tmphistory.DRAW_COLOR);
			for (int j = 0; j < tmphistory.point.size(); j++) {
				gc.lineTo(tmphistory.point.get(j).getKey(), tmphistory.point.get(j).getValue());
				gc.stroke();
			}
		}
		if (tmphistory.DRAW_MOD.equals("ERASER")) {
			gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
			gc.setStroke(Color.WHITE);
			for (int j = 0; j < tmphistory.point.size(); j++) {
				gc.lineTo(tmphistory.point.get(j).getKey(), tmphistory.point.get(j).getValue());
				gc.stroke();
			}
		}
		if (tmphistory.DRAW_MOD.equals("SQUARE")) {
			gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
			gc.setStroke(tmphistory.DRAW_COLOR);
			gc.setFill(tmphistory.DRAW_COLOR);
			gc.fillRect(Math.min(tmphistory.DRAW_STARTX, tmphistory.DRAW_ENDX),
					Math.min(tmphistory.DRAW_STARTY, tmphistory.DRAW_ENDY),
					Math.abs(tmphistory.DRAW_ENDX - tmphistory.DRAW_STARTX),
					Math.abs(tmphistory.DRAW_ENDY - tmphistory.DRAW_STARTY));

		}
		if (tmphistory.DRAW_MOD.equals("CIRCLE")) {
			gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
			gc.setStroke(tmphistory.DRAW_COLOR);
			gc.setFill(tmphistory.DRAW_COLOR);
			gc.fillOval(Math.min(tmphistory.DRAW_STARTX, tmphistory.DRAW_ENDX),
					Math.min(tmphistory.DRAW_STARTY, tmphistory.DRAW_ENDY),
					Math.abs(tmphistory.DRAW_ENDX - tmphistory.DRAW_STARTX),
					Math.abs(tmphistory.DRAW_ENDY - tmphistory.DRAW_STARTY));
		}
		if (tmphistory.DRAW_MOD.equals("FILL")) {
			PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
			PixelWriter gpw = gc.getPixelWriter();
			Color currnt_color = gpr.getColor(tmphistory.point.get(0).getKey(), tmphistory.point.get(0).getValue());
			boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
			Queue<Pair<Integer, Integer>> q = new LinkedList<>();
			q.add(new Pair<>(tmphistory.point.get(0).getKey(), tmphistory.point.get(0).getValue()));
			while (!q.isEmpty()) {
				Pair<Integer, Integer> pair = q.poll();
				if (visited[pair.getKey()][pair.getValue()] == true)
					continue;
				visited[pair.getKey()][pair.getValue()] = true;
				gpw.setColor(pair.getKey(), pair.getValue(), tmphistory.DRAW_COLOR);
				if (gpr.getColor(pair.getKey(), pair.getValue()).equals(currnt_color)) {
					if (pair.getKey() + 1 < canvas.getWidth()) {
						q.add(new Pair<>(pair.getKey() + 1, pair.getValue()));
						if (pair.getValue() + 1 < canvas.getHeight()) {
							q.add(new Pair<>(pair.getKey() + 1, pair.getValue() + 1));
						}
						if (pair.getValue() - 1 >= 0) {
							q.add(new Pair<>(pair.getKey() + 1, pair.getValue() - 1));
						}
					}
					if (pair.getKey() - 1 >= 0) {
						q.add(new Pair<>(pair.getKey() - 1, pair.getValue()));
						if (pair.getValue() + 1 < canvas.getHeight()) {
							q.add(new Pair<>(pair.getKey() - 1, pair.getValue() + 1));
						}
						if (pair.getValue() - 1 >= 0) {
							q.add(new Pair<>(pair.getKey() - 1, pair.getValue() - 1));
						}
					}
					if (pair.getValue() - 1 >= 0) {
						q.add(new Pair<>(pair.getKey(), pair.getValue() - 1));
					}
					if (pair.getValue() + 1 < canvas.getHeight()) {
						q.add(new Pair<>(pair.getKey(), pair.getValue() + 1));
					}
				}
			}
		}
		gc.closePath();
	}

	public void undo() {
		System.out.println(parameter.history);
		System.out.println(parameter.history_pointer);
		if (parameter.history_pointer - 1 >= 0) {
			parameter.history_pointer--;
			gc.setFill(Color.WHITE);
			gc.fillRect(0, 0, 500, 360);
			for (int i = 0; i < parameter.history_pointer; i++) {
				History tmphistory = parameter.history.get(i);
				gc.beginPath();
				if (tmphistory.DRAW_MOD.equals("PENCIL")) {
					gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
					gc.setStroke(tmphistory.DRAW_COLOR);
					for (int j = 0; j < tmphistory.point.size(); j++) {
						gc.lineTo(tmphistory.point.get(j).getKey(), tmphistory.point.get(j).getValue());
						gc.stroke();
					}
				}
				if (tmphistory.DRAW_MOD.equals("ERASER")) {
					gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
					gc.setStroke(Color.WHITE);
					for (int j = 0; j < tmphistory.point.size(); j++) {
						gc.lineTo(tmphistory.point.get(j).getKey(), tmphistory.point.get(j).getValue());
						gc.stroke();
					}
				}
				if (tmphistory.DRAW_MOD.equals("SQUARE")) {
					gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
					gc.setStroke(tmphistory.DRAW_COLOR);
					gc.setFill(tmphistory.DRAW_COLOR);
					gc.fillRect(Math.min(tmphistory.DRAW_STARTX, tmphistory.DRAW_ENDX),
							Math.min(tmphistory.DRAW_STARTY, tmphistory.DRAW_ENDY),
							Math.abs(tmphistory.DRAW_ENDX - tmphistory.DRAW_STARTX),
							Math.abs(tmphistory.DRAW_ENDY - tmphistory.DRAW_STARTY));

				}
				if (tmphistory.DRAW_MOD.equals("CIRCLE")) {
					gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
					gc.setStroke(tmphistory.DRAW_COLOR);
					gc.setFill(tmphistory.DRAW_COLOR);
					gc.fillOval(Math.min(tmphistory.DRAW_STARTX, tmphistory.DRAW_ENDX),
							Math.min(tmphistory.DRAW_STARTY, tmphistory.DRAW_ENDY),
							Math.abs(tmphistory.DRAW_ENDX - tmphistory.DRAW_STARTX),
							Math.abs(tmphistory.DRAW_ENDY - tmphistory.DRAW_STARTY));
				}
				if (tmphistory.DRAW_MOD.equals("FILL")) {
					PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
					PixelWriter gpw = gc.getPixelWriter();
					Color currnt_color = gpr.getColor(tmphistory.point.get(0).getKey(),
							tmphistory.point.get(0).getValue());
					boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
					Queue<Pair<Integer, Integer>> q = new LinkedList<>();
					q.add(new Pair<>(tmphistory.point.get(0).getKey(), tmphistory.point.get(0).getValue()));
					while (!q.isEmpty()) {
						Pair<Integer, Integer> pair = q.poll();
						if (visited[pair.getKey()][pair.getValue()] == true)
							continue;
						visited[pair.getKey()][pair.getValue()] = true;
						gpw.setColor(pair.getKey(), pair.getValue(), tmphistory.DRAW_COLOR);
						if (gpr.getColor(pair.getKey(), pair.getValue()).equals(currnt_color)) {
							if (pair.getKey() + 1 < canvas.getWidth()) {
								q.add(new Pair<>(pair.getKey() + 1, pair.getValue()));
								if (pair.getValue() + 1 < canvas.getHeight()) {
									q.add(new Pair<>(pair.getKey() + 1, pair.getValue() + 1));
								}
								if (pair.getValue() - 1 >= 0) {
									q.add(new Pair<>(pair.getKey() + 1, pair.getValue() - 1));
								}
							}
							if (pair.getKey() - 1 >= 0) {
								q.add(new Pair<>(pair.getKey() - 1, pair.getValue()));
								if (pair.getValue() + 1 < canvas.getHeight()) {
									q.add(new Pair<>(pair.getKey() - 1, pair.getValue() + 1));
								}
								if (pair.getValue() - 1 >= 0) {
									q.add(new Pair<>(pair.getKey() - 1, pair.getValue() - 1));
								}
							}
							if (pair.getValue() - 1 >= 0) {
								q.add(new Pair<>(pair.getKey(), pair.getValue() - 1));
							}
							if (pair.getValue() + 1 < canvas.getHeight()) {
								q.add(new Pair<>(pair.getKey(), pair.getValue() + 1));
							}
						}
					}
				}
				gc.closePath();
			}
		}
	}

	public void redo() {
		System.out.println(parameter.history);
		System.out.println(parameter.history_pointer);
		if (parameter.history_pointer < parameter.history.size()) {
			History tmphistory = parameter.history.get(parameter.history_pointer);
			parameter.history_pointer++;
			gc.beginPath();
			if (tmphistory.DRAW_MOD.equals("PENCIL")) {
				gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
				gc.setStroke(tmphistory.DRAW_COLOR);
				for (int j = 0; j < tmphistory.point.size(); j++) {
					gc.lineTo(tmphistory.point.get(j).getKey(), tmphistory.point.get(j).getValue());
					gc.stroke();
				}
			}
			if (tmphistory.DRAW_MOD.equals("ERASER")) {
				gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
				gc.setStroke(Color.WHITE);
				for (int j = 0; j < tmphistory.point.size(); j++) {
					gc.lineTo(tmphistory.point.get(j).getKey(), tmphistory.point.get(j).getValue());
					gc.stroke();
				}
			}
			if (tmphistory.DRAW_MOD.equals("SQUARE")) {
				gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
				gc.setStroke(tmphistory.DRAW_COLOR);
				gc.setFill(tmphistory.DRAW_COLOR);
				gc.fillRect(Math.min(tmphistory.DRAW_STARTX, tmphistory.DRAW_ENDX),
						Math.min(tmphistory.DRAW_STARTY, tmphistory.DRAW_ENDY),
						Math.abs(tmphistory.DRAW_ENDX - tmphistory.DRAW_STARTX),
						Math.abs(tmphistory.DRAW_ENDY - tmphistory.DRAW_STARTY));

			}
			if (tmphistory.DRAW_MOD.equals("CIRCLE")) {
				gc.setLineWidth(tmphistory.DRAW_LINEWIDTH);
				gc.setStroke(tmphistory.DRAW_COLOR);
				gc.setFill(tmphistory.DRAW_COLOR);
				gc.fillOval(Math.min(tmphistory.DRAW_STARTX, tmphistory.DRAW_ENDX),
						Math.min(tmphistory.DRAW_STARTY, tmphistory.DRAW_ENDY),
						Math.abs(tmphistory.DRAW_ENDX - tmphistory.DRAW_STARTX),
						Math.abs(tmphistory.DRAW_ENDY - tmphistory.DRAW_STARTY));
			}
			if (tmphistory.DRAW_MOD.equals("FILL")) {
				PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
				PixelWriter gpw = gc.getPixelWriter();
				Color currnt_color = gpr.getColor(tmphistory.point.get(0).getKey(), tmphistory.point.get(0).getValue());
				boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
				Queue<Pair<Integer, Integer>> q = new LinkedList<>();
				q.add(new Pair<>(tmphistory.point.get(0).getKey(), tmphistory.point.get(0).getValue()));
				while (!q.isEmpty()) {
					Pair<Integer, Integer> pair = q.poll();
					if (visited[pair.getKey()][pair.getValue()] == true)
						continue;
					visited[pair.getKey()][pair.getValue()] = true;
					gpw.setColor(pair.getKey(), pair.getValue(), tmphistory.DRAW_COLOR);
					if (gpr.getColor(pair.getKey(), pair.getValue()).equals(currnt_color)) {
						if (pair.getKey() + 1 < canvas.getWidth()) {
							q.add(new Pair<>(pair.getKey() + 1, pair.getValue()));
							if (pair.getValue() + 1 < canvas.getHeight()) {
								q.add(new Pair<>(pair.getKey() + 1, pair.getValue() + 1));
							}
							if (pair.getValue() - 1 >= 0) {
								q.add(new Pair<>(pair.getKey() + 1, pair.getValue() - 1));
							}
						}
						if (pair.getKey() - 1 >= 0) {
							q.add(new Pair<>(pair.getKey() - 1, pair.getValue()));
							if (pair.getValue() + 1 < canvas.getHeight()) {
								q.add(new Pair<>(pair.getKey() - 1, pair.getValue() + 1));
							}
							if (pair.getValue() - 1 >= 0) {
								q.add(new Pair<>(pair.getKey() - 1, pair.getValue() - 1));
							}
						}
						if (pair.getValue() - 1 >= 0) {
							q.add(new Pair<>(pair.getKey(), pair.getValue() - 1));
						}
						if (pair.getValue() + 1 < canvas.getHeight()) {
							q.add(new Pair<>(pair.getKey(), pair.getValue() + 1));
						}
					}
				}
			}
			gc.closePath();
		}
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
			in = new java.io.ObjectInputStream(client.getInputStream());
			while (client.isConnected()) {
				Message data;
				try {
					data = (Message) in.readObject();
					System.out.println("伺服端訊息:\n" + data.history.toString());
					if(data.type.equals("History"))
						update(data.history);
					else if(data.type.equals("Undo"))
						undo();
					else if(data.type.equals("Redo"))
						redo();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {
		(new SocketClient()).start();
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
