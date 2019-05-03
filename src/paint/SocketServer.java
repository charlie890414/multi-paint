package paint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class SocketServer extends java.lang.Thread {
	Socket Server;
	Canvas canvas;
	GraphicsContext gc;
	Parameter parameter;
	Queue<Message> tmp = new LinkedList<Message>();
	ObjectInputStream in;
	ObjectOutputStream out;

	public SocketServer(Socket Server) {
		this.Server = Server;
	}

	public void set_canvas(Canvas canvas) {
		this.canvas = canvas;
		this.gc = canvas.getGraphicsContext2D();
	}

	public void set_parameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public Message get_update() {
		return tmp.poll();
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

	public void write(Message message) throws IOException {
		System.out.println(out);
		try {
			out.writeObject(message);
			out.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public void run() {
		System.out.println("連接成功 : InetAddress = " + Server.getInetAddress());
		try {
			out = new ObjectOutputStream(Server.getOutputStream());
			in = new ObjectInputStream(Server.getInputStream());
			while (Server.isConnected()) {
				Message data = (Message) in.readObject();
				System.out.println("客戶端訊息:\n" + data.history.toString());
				tmp.add(data);
				System.out.println(data);
				if(data.type.equals("History"))
					update(data.history);
				else if(data.type.equals("Undo"))
					undo();
				else if(data.type.equals("Redo"))
					redo();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {
		ServerSocket ss = new ServerSocket(30000);
		while (true) {
			Socket client = ss.accept();
			SocketServer serverThread = new SocketServer(client);
			new Thread(serverThread).start();
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
