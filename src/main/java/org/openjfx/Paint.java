package org.openjfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;

import java.sql.Timestamp;

public class Paint extends Application {

	private Parameter parameter = new Parameter();
	private Canvas canvas;
	private GraphicsContext gc;

	private void draw(History current) {
		current.mergecolor();
		if (parameter.history.size() > parameter.history_pointer) {
			parameter.history.subList(parameter.history_pointer, parameter.history.size()).clear();
		}
		parameter.history.add(current);
		parameter.history_pointer++;
		gc.beginPath();
		if (current.DRAW_MOD.equals("PENCIL")) {
			gc.setLineWidth(current.DRAW_LINEWIDTH);
			gc.setStroke(current.DRAW_COLOR);
			for (int j = 0; j < current.point.size(); j++) {
				gc.lineTo(current.point.get(j).getKey(), current.point.get(j).getValue());
				gc.stroke();
			}
		}
		if (current.DRAW_MOD.equals("ERASER")) {
			gc.setLineWidth(current.DRAW_LINEWIDTH);
			gc.setStroke(Color.WHITE);
			for (int j = 0; j < current.point.size(); j++) {
				gc.lineTo(current.point.get(j).getKey(), current.point.get(j).getValue());
				gc.stroke();
			}
		}
		if (current.DRAW_MOD.equals("SQUARE")) {
			gc.setLineWidth(current.DRAW_LINEWIDTH);
			gc.setStroke(current.DRAW_COLOR);
			gc.setFill(current.DRAW_COLOR);
			gc.fillRect(Math.min(current.DRAW_STARTX, current.DRAW_ENDX),
					Math.min(current.DRAW_STARTY, current.DRAW_ENDY), Math.abs(current.DRAW_ENDX - current.DRAW_STARTX),
					Math.abs(current.DRAW_ENDY - current.DRAW_STARTY));

		}
		if (current.DRAW_MOD.equals("CIRCLE")) {
			gc.setLineWidth(current.DRAW_LINEWIDTH);
			gc.setStroke(current.DRAW_COLOR);
			gc.setFill(current.DRAW_COLOR);
			gc.fillOval(Math.min(current.DRAW_STARTX, current.DRAW_ENDX),
					Math.min(current.DRAW_STARTY, current.DRAW_ENDY), Math.abs(current.DRAW_ENDX - current.DRAW_STARTX),
					Math.abs(current.DRAW_ENDY - current.DRAW_STARTY));
		}
		if (current.DRAW_MOD.equals("FILL")) {
			PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
			PixelWriter gpw = gc.getPixelWriter();
			Color currnt_color = gpr.getColor(current.fillpoint.get(0).getKey(), current.fillpoint.get(0).getValue());
			boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
			Queue<Pair<Integer, Integer>> q = new LinkedList<>();
			q.add(new Pair<>(current.fillpoint.get(0).getKey(), current.fillpoint.get(0).getValue()));
			while (!q.isEmpty()) {
				Pair<Integer, Integer> pair = q.poll();
				if (visited[pair.getKey()][pair.getValue()] == true)
					continue;
				visited[pair.getKey()][pair.getValue()] = true;
				gpw.setColor(pair.getKey(), pair.getValue(), current.DRAW_COLOR);
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
		current.separatecolor();
	}

	private void undo() {
		System.out.println(parameter.history);
		System.out.println(parameter.history_pointer);
		if (parameter.history_pointer > 0) {
			parameter.history_pointer--;
			gc.setFill(Color.WHITE);
			gc.fillRect(0, 0, 500, 360);
			for (int i = 0; i < parameter.history_pointer; i++) {
				History current = parameter.history.get(i);
				current.mergecolor();
				gc.beginPath();
				if (current.DRAW_MOD.equals("PENCIL")) {
					gc.setLineWidth(current.DRAW_LINEWIDTH);
					gc.setStroke(current.DRAW_COLOR);
					for (int j = 0; j < current.point.size(); j++) {
						gc.lineTo(current.point.get(j).getKey(), current.point.get(j).getValue());
						gc.stroke();
					}
				}
				if (current.DRAW_MOD.equals("ERASER")) {
					gc.setLineWidth(current.DRAW_LINEWIDTH);
					gc.setStroke(Color.WHITE);
					for (int j = 0; j < current.point.size(); j++) {
						gc.lineTo(current.point.get(j).getKey(), current.point.get(j).getValue());
						gc.stroke();
					}
				}
				if (current.DRAW_MOD.equals("SQUARE")) {
					gc.setLineWidth(current.DRAW_LINEWIDTH);
					gc.setStroke(current.DRAW_COLOR);
					gc.setFill(current.DRAW_COLOR);
					gc.fillRect(Math.min(current.DRAW_STARTX, current.DRAW_ENDX),
							Math.min(current.DRAW_STARTY, current.DRAW_ENDY),
							Math.abs(current.DRAW_ENDX - current.DRAW_STARTX),
							Math.abs(current.DRAW_ENDY - current.DRAW_STARTY));

				}
				if (current.DRAW_MOD.equals("CIRCLE")) {
					gc.setLineWidth(current.DRAW_LINEWIDTH);
					gc.setStroke(current.DRAW_COLOR);
					gc.setFill(current.DRAW_COLOR);
					gc.fillOval(Math.min(current.DRAW_STARTX, current.DRAW_ENDX),
							Math.min(current.DRAW_STARTY, current.DRAW_ENDY),
							Math.abs(current.DRAW_ENDX - current.DRAW_STARTX),
							Math.abs(current.DRAW_ENDY - current.DRAW_STARTY));
				}
				if (current.DRAW_MOD.equals("FILL")) {
					PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
					PixelWriter gpw = gc.getPixelWriter();
					Color currnt_color = gpr.getColor(current.fillpoint.get(0).getKey(), current.fillpoint.get(0).getValue());
					boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
					Queue<Pair<Integer, Integer>> q = new LinkedList<>();
					q.add(new Pair<>(current.fillpoint.get(0).getKey(), current.fillpoint.get(0).getValue()));
					while (!q.isEmpty()) {
						Pair<Integer, Integer> pair = q.poll();
						if (visited[pair.getKey()][pair.getValue()] == true)
							continue;
						visited[pair.getKey()][pair.getValue()] = true;
						gpw.setColor(pair.getKey(), pair.getValue(), current.DRAW_COLOR);
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
				current.separatecolor();
			}
		}
	}

	private void redo() {
		System.out.println(parameter.history);
		System.out.println(parameter.history_pointer);
		if (parameter.history_pointer < parameter.history.size()) {
			History current = parameter.history.get(parameter.history_pointer);
			current.mergecolor();
			parameter.history_pointer++;
			gc.beginPath();
			if (current.DRAW_MOD.equals("PENCIL")) {
				gc.setLineWidth(current.DRAW_LINEWIDTH);
				gc.setStroke(current.DRAW_COLOR);
				for (int j = 0; j < current.point.size(); j++) {
					gc.lineTo(current.point.get(j).getKey(), current.point.get(j).getValue());
					gc.stroke();
				}
			}
			if (current.DRAW_MOD.equals("ERASER")) {
				gc.setLineWidth(current.DRAW_LINEWIDTH);
				gc.setStroke(Color.WHITE);
				for (int j = 0; j < current.point.size(); j++) {
					gc.lineTo(current.point.get(j).getKey(), current.point.get(j).getValue());
					gc.stroke();
				}
			}
			if (current.DRAW_MOD.equals("SQUARE")) {
				gc.setLineWidth(current.DRAW_LINEWIDTH);
				gc.setStroke(current.DRAW_COLOR);
				gc.setFill(current.DRAW_COLOR);
				gc.fillRect(Math.min(current.DRAW_STARTX, current.DRAW_ENDX),
						Math.min(current.DRAW_STARTY, current.DRAW_ENDY),
						Math.abs(current.DRAW_ENDX - current.DRAW_STARTX),
						Math.abs(current.DRAW_ENDY - current.DRAW_STARTY));

			}
			if (current.DRAW_MOD.equals("CIRCLE")) {
				gc.setLineWidth(current.DRAW_LINEWIDTH);
				gc.setStroke(current.DRAW_COLOR);
				gc.setFill(current.DRAW_COLOR);
				gc.fillOval(Math.min(current.DRAW_STARTX, current.DRAW_ENDX),
						Math.min(current.DRAW_STARTY, current.DRAW_ENDY),
						Math.abs(current.DRAW_ENDX - current.DRAW_STARTX),
						Math.abs(current.DRAW_ENDY - current.DRAW_STARTY));
			}
			if (current.DRAW_MOD.equals("FILL")) {
				PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
				PixelWriter gpw = gc.getPixelWriter();
				Color currnt_color = gpr.getColor(current.fillpoint.get(0).getKey(), current.fillpoint.get(0).getValue());
				boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
				Queue<Pair<Integer, Integer>> q = new LinkedList<>();
				q.add(new Pair<>(current.fillpoint.get(0).getKey(), current.fillpoint.get(0).getValue()));
				while (!q.isEmpty()) {
					Pair<Integer, Integer> pair = q.poll();
					if (visited[pair.getKey()][pair.getValue()] == true)
						continue;
					visited[pair.getKey()][pair.getValue()] = true;
					gpw.setColor(pair.getKey(), pair.getValue(), current.DRAW_COLOR);
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
			current.separatecolor();
		}
	}

	@Override
	public void start(Stage primaryStage) {
		Label label1 = new Label(" Mouse In Canvas: X: " + 0 + ", Y: " + 0);
		label1.setPrefWidth(580);
		label1.setStyle("-fx-border-color: gray;");
		Label label2 = new Label("  " + parameter.DRAW_MOD + " ");
		label2.setPrefWidth(60);
		label2.setStyle("-fx-border-color: gray;");

		BorderPane bottom_border = new BorderPane();
		bottom_border.setLeft(label1);
		bottom_border.setRight(label2);
		bottom_border.setPrefHeight(20);
		bottom_border.setPrefWidth(640);

		canvas = new Canvas(500, 360);

		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, 500, 360);

		Button btn1 = new Button();
		btn1.setText("SQUARE");
		btn1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parameter.DRAW_MOD = "SQUARE";
				label2.setText("  " + parameter.DRAW_MOD + " ");
			}
		});
		btn1.setPrefSize(65, 10);

		Button btn2 = new Button();
		btn2.setText("CIRCLE");
		btn2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parameter.DRAW_MOD = "CIRCLE";
				label2.setText("  " + parameter.DRAW_MOD + " ");
			}
		});
		btn2.setPrefSize(65, 10);

		Button btn3 = new Button();
		btn3.setText("PENCIL");
		btn3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parameter.DRAW_MOD = "PENCIL";
				label2.setText("  " + parameter.DRAW_MOD + " ");
			}
		});
		btn3.setPrefSize(65, 10);

		Button btn4 = new Button();
		btn4.setText("ERASER");
		btn4.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parameter.DRAW_MOD = "ERASER";
				label2.setText("  " + parameter.DRAW_MOD + " ");
			}
		});
		btn4.setPrefSize(65, 10);

		Button btn5 = new Button();
		btn5.setText("FILL");
		btn5.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parameter.DRAW_MOD = "FILL";
				label2.setText("  " + parameter.DRAW_MOD + " ");
			}
		});
		btn5.setPrefSize(65, 10);

		Button save = new Button("Save");
		save.setText("Save");
		save.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent t) {
				WritableImage image = canvas.snapshot(null, null);
				FileChooser fileChooser = new FileChooser();
				fileChooser
						.setInitialDirectory(new File("C:\\Users\\" + System.getProperty("user.name") + "\\Pictures"));
				fileChooser.setInitialFileName("picture.png");
				FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("PNG", "*.png");
				fileChooser.getExtensionFilters().add(filter);
				fileChooser.setTitle("Save Image");
				File file = fileChooser.showSaveDialog(null);

				if (file != null) {
					try {
						ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("錯誤");
						alert.setHeaderText("");
						alert.setContentText("在儲存期間發生錯誤");
						alert.showAndWait();
					}
				}
			}

		});
		save.setPrefSize(130, 10);

		Button btn6 = new Button();
		btn6.setText("UNDO");
		btn6.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				undo();
				if (parameter.Client != null) {
					try {
						parameter.Client.write(new Message("Undo"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if (parameter.Server != null) {
					try {
						for (int j = 0; j < parameter.Server.size(); j++) {
							parameter.Server.get(j).write(new Message("Undo"));
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btn6.setPrefSize(65, 10);

		Button btn7 = new Button();
		btn7.setText("REDO");
		btn7.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				redo();
				if (parameter.Client != null) {
					try {
						parameter.Client.write(new Message("Redo"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if (parameter.Server != null) {
					try {
						for (int i = 0; i < parameter.Server.size(); i++) {
							parameter.Server.get(i).write(new Message("Redo"));
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btn7.setPrefSize(65, 10);

		ColorPicker colorPicker = new ColorPicker(parameter.DRAW_COLOR);
		colorPicker.setOnAction((ActionEvent t) -> {
			parameter.DRAW_COLOR = colorPicker.getValue();
		});
		colorPicker.setPrefSize(130, 25);

		int[] width = new int[10];
		ObservableList<Integer> number = FXCollections.observableArrayList();
		for (int i = 0; i < 10; i++) {
			width[i] = i + 1;
			number.add(i + 1);
		}
		ChoiceBox<Integer> cb = new ChoiceBox<Integer>();
		cb.setItems(number);
		cb.getSelectionModel().select(0);
		cb.setPrefSize(130, 25);
		cb.getSelectionModel().selectedIndexProperty().addListener((ChangeListener<Number>) (ov, value,
				new_value) -> parameter.DRAW_LINEWIDTH = width[new_value.intValue()]);

		FlowPane flowL = new FlowPane();
		flowL.setPadding(new Insets(10, 0, 5, 3));
		flowL.setHgap(3);
		flowL.setVgap(10);
		flowL.setStyle("-fx-background-color: DAE6F3;");
		flowL.getChildren().add(btn3);
		flowL.getChildren().add(btn4);
		flowL.getChildren().add(btn1);
		flowL.getChildren().add(btn2);
		flowL.getChildren().add(btn5);
		flowL.getChildren().add(colorPicker);
		flowL.getChildren().add(cb);
		flowL.getChildren().add(btn6);
		flowL.getChildren().add(btn7);
		flowL.getChildren().add(save);
		flowL.setPrefWidth(140);

		FlowPane flowR = new FlowPane();
		flowR.getChildren().add(canvas);
		flowR.setPrefWidth(500);

		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("檔案");
		menuBar.getMenus().add(menu);
		MenuItem menuItem1 = new MenuItem("開新檔案");
		menuItem1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
				gc.setFill(Color.WHITE);
				gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			}
		});
		menu.getItems().add(menuItem1);
		MenuItem menuItem4 = new MenuItem("開啟舊檔");
		menuItem4.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open File");
				fileChooser
						.setInitialDirectory(new File("C:\\Users\\" + System.getProperty("user.name") + "\\Pictures"));
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					try {
						InputStream io = new FileInputStream(file);
						Image img = new Image(io);
						gc.drawImage(img, 0, 0, 500, 360);
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("錯誤");
						alert.setHeaderText("");
						alert.setContentText("在讀取期間發生錯誤");
						alert.showAndWait();
					}
				}
			}
		});
		menu.getItems().add(menuItem4);
		MenuItem menuItem2 = new MenuItem("建立連線");
		menu.getItems().add(menuItem2);
		menuItem2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					InetAddress IP = InetAddress.getLocalHost();

					primaryStage.setTitle("多人小畫家 Server ip " + IP.getHostAddress());
					var Serverthread = new Thread(() -> {
						try {
							ServerSocket ss = new ServerSocket(30000);
							System.out.println("伺服器開始運行 !");
							while (!ss.isClosed()) {
								Socket Server = ss.accept();
								parameter.Server.add(new SocketServer(Server));
								parameter.Server.lastElement().start();
							}
							ss.close();
						} catch (Exception e) {
							e.printStackTrace();
						}

					});
					Serverthread.setDaemon(true);
					Serverthread.start();

					Thread updateServer = new Thread(new Runnable() {

						@Override
						public void run() {
							while (true) {
								for (int i = 0; i < parameter.Server.size(); i++) {
									if (parameter.Server.get(i).messages.peek() != null) {
										System.out.println(parameter.Server.get(i).messages.peek());
										if (parameter.Server.get(i).messages.peek().type.equals("History")) {
											final History todraw = parameter.Server.get(i).messages.peek().history;
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													draw(todraw);
												}
											});
										} else if (parameter.Server.get(i).messages.peek().type.equals("Undo")) {
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													undo();
												}
											});
										} else if (parameter.Server.get(i).messages.peek().type.equals("Redo")) {
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													redo();
												}
											});
										}
										for (int j = 0; j < parameter.Server.size(); j++) {
											if (i != j)
												try {
													parameter.Server.get(i).messages.peek().history.separatecolor();
													parameter.Server.get(j)
															.write(parameter.Server.get(i).messages.peek());
													parameter.Server.get(i).messages.peek().history.mergecolor();
												} catch (Exception e) {
													e.printStackTrace();
												}
										}
										parameter.Server.get(i).messages.poll();
									}
								}
							}
						}

					});
					updateServer.setDaemon(true);
					updateServer.start();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		});
		MenuItem menuItem3 = new MenuItem("加入連線");
		menu.getItems().add(menuItem3);
		menuItem3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String setaddress = "127.0.0.1";
				TextInputDialog textInputDialog = new TextInputDialog();
				textInputDialog.setTitle("設定連結地址");
				textInputDialog.setContentText("連結地址");
				textInputDialog.showAndWait();
				if (textInputDialog.getResult() == null || textInputDialog.getResult().equals("")) {
					setaddress = "127.0.0.1";
				} else {
					setaddress = textInputDialog.getResult();
				}
				final String address = setaddress;
				primaryStage.setTitle("多人小畫家 Client ip " + address);
				parameter.Client = new SocketClient();
				parameter.Client.set_address(address);
				parameter.Client.start();

				Thread updateClient = new Thread(new Runnable() {

					@Override
					public void run() {
						while (true) {
							System.out.println(parameter.Client.messages.size());
							if (parameter.Client.messages.peek() != null) {
								System.out.println(parameter.Client.messages.peek());
								if (parameter.Client.messages.peek().type.equals("History")) {
									final History todraw = parameter.Client.messages.peek().history;
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											draw(todraw);
										}
									});
								} else if (parameter.Client.messages.peek().type.equals("Undo")) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											undo();
										}
									});
								} else if (parameter.Client.messages.peek().type.equals("Redo")) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											redo();
										}
									});
								}
								parameter.Client.messages.poll();
							}
						}
					}

				});
				updateClient.setDaemon(true);
				updateClient.start();
			}
		});
		menuBar.setPrefHeight(20);

		BorderPane border = new BorderPane();
		border.setTop(menuBar);
		border.setLeft(flowL);
		border.setRight(flowR);
		border.setBottom(bottom_border);

		StackPane root = new StackPane();
		root.getChildren().add(border);
		Scene scene = new Scene(root, 640, 400);

		scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (e.getX() > 140 && e.getY() > 20 && e.getY() < 380)
					label1.setText(" Mouse In Canvas: X: " + (int) (Math.round(e.getX() - 140.0)) + ", Y: "
							+ (int) (Math.round(e.getY() - 20.0)));
				else
					label1.setText(" Mouse In Canvas: X: " + 0 + ", Y: " + 0);
			}
		});

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (e.getX() > 140 && e.getY() > 20 && e.getY() < 380) {
					parameter.historytmp = new History();
					parameter.START_DRAW = true;

					parameter.historytmp.DRAW_STARTX = (int) (Math.round(e.getX() - 140.0));
					parameter.historytmp.DRAW_STARTY = (int) (Math.round(e.getY() - 20.0));
					parameter.historytmp.DRAW_COLOR = parameter.DRAW_COLOR;
					parameter.historytmp.DRAW_MOD = parameter.DRAW_MOD;
					parameter.historytmp.DRAW_LINEWIDTH = parameter.DRAW_LINEWIDTH;
					parameter.DRAW_STARTX = (Math.round(e.getX() - 140.0));
					parameter.DRAW_STARTY = (int) (Math.round(e.getY() - 20.0));
					gc.beginPath();
					gc.setLineWidth(parameter.DRAW_LINEWIDTH);
					gc.moveTo((int) (Math.round(e.getX() - 140.0)), (int) (Math.round(e.getY() - 20.0)));
					if (parameter.DRAW_MOD == "PENCIL") {
						parameter.penanimation = new Circle(e.getX(), e.getY() + 2, parameter.DRAW_LINEWIDTH,
								Color.WHITE);
						parameter.penanimation.setStroke(Color.BLACK);
						border.getChildren().add(parameter.penanimation);
						primaryStage.show();
						gc.setStroke(parameter.DRAW_COLOR);
						gc.lineTo(e.getX() - 140, e.getY() - 20.0);
						gc.stroke();
						parameter.historytmp.point
								.add(new Pair<Double, Double>(Double.valueOf(e.getX() - 140),
								Double.valueOf(e.getY() - 20.0)));
					} else if (parameter.DRAW_MOD == "ERASER") {
						parameter.penanimation = new Circle(e.getX(), e.getY() + 2, parameter.DRAW_LINEWIDTH,
								Color.WHITE);
						parameter.penanimation.setStroke(Color.BLACK);
						border.getChildren().add(parameter.penanimation);
						primaryStage.show();
						gc.setStroke(Color.WHITE);
						gc.lineTo(e.getX() - 140, e.getY() - 20.0);
						gc.stroke();
						parameter.historytmp.point
								.add(new Pair<Double, Double>(Double.valueOf(e.getX() - 140),
								Double.valueOf(e.getY() - 20.0)));
					} else if (parameter.DRAW_MOD == "SQUARE") {
						parameter.rectangleanimation = new Rectangle(e.getX(), e.getY(), 0, 0);
						parameter.rectangleanimation.setFill(parameter.DRAW_COLOR);
						border.getChildren().add(parameter.rectangleanimation);
						primaryStage.show();
					} else if (parameter.DRAW_MOD == "CIRCLE") {
						parameter.ellipseanimation = new Ellipse(e.getX(), e.getY(), 0, 0);
						parameter.ellipseanimation.setFill(parameter.DRAW_COLOR);
						border.getChildren().add(parameter.ellipseanimation);
						primaryStage.show();
					}
				}

			}
		});

		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (e.getX() > 140 && e.getY() > 20 && e.getY() < 380) {
					if (parameter.DRAW_MOD == "PENCIL") {
						Timeline timeline = new Timeline();
						KeyValue kv1 = new KeyValue(parameter.penanimation.centerXProperty(),
								(int) (Math.round(e.getX() - 140.0)) + 140);
						KeyValue kv2 = new KeyValue(parameter.penanimation.centerYProperty(),
								(int) (Math.round(e.getY() - 20.0)) + 24);
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), kv1));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), kv2));
						timeline.play();
						gc.setStroke(parameter.DRAW_COLOR);
						gc.lineTo(e.getX() - 140, e.getY() - 20.0);
						gc.stroke();
						parameter.historytmp.point
								.add(new Pair<Double, Double>(Double.valueOf(e.getX() - 140),
								Double.valueOf(e.getY() - 20.0)));
					} else if (parameter.DRAW_MOD == "ERASER") {
						Timeline timeline = new Timeline();
						KeyValue kv1 = new KeyValue(parameter.penanimation.centerXProperty(),
								(int) (Math.round(e.getX() - 140.0)) + 140);
						KeyValue kv2 = new KeyValue(parameter.penanimation.centerYProperty(),
								(int) (Math.round(e.getY() - 20.0)) + 24);
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), kv1));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), kv2));
						timeline.play();
						gc.setStroke(Color.WHITE);
						gc.lineTo(e.getX() - 140, e.getY() - 20.0);
						gc.stroke();
						parameter.historytmp.point
								.add(new Pair<Double, Double>(Double.valueOf(e.getX() - 140),
								Double.valueOf(e.getY() - 20.0)));
					} else if (parameter.DRAW_MOD == "SQUARE") {
						Timeline timeline = new Timeline();
						KeyValue kv1 = new KeyValue(parameter.rectangleanimation.xProperty(),
								Math.min(parameter.DRAW_STARTX, (int) (Math.round(e.getX() - 140.0))) + 140);
						KeyValue kv2 = new KeyValue(parameter.rectangleanimation.yProperty(),
								Math.min(parameter.DRAW_STARTY, (int) (Math.round(e.getY() - 20.0))) + 26);
						KeyValue kv3 = new KeyValue(parameter.rectangleanimation.widthProperty(),
								Math.abs((int) (Math.round(e.getX() - 140.0)) - parameter.DRAW_STARTX));
						KeyValue kv4 = new KeyValue(parameter.rectangleanimation.heightProperty(),
								Math.abs((int) (Math.round(e.getY() - 20.0)) - parameter.DRAW_STARTY));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv1));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv2));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv3));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv4));
						timeline.play();
					} else if (parameter.DRAW_MOD == "CIRCLE") {
						Timeline timeline = new Timeline();
						KeyValue kv1 = new KeyValue(parameter.ellipseanimation.centerXProperty(),
								Math.min(parameter.DRAW_STARTX, (int) (Math.round(e.getX() - 140.0))) + 140
										+ Math.abs((int) (Math.round(e.getX() - 140.0)) - parameter.DRAW_STARTX) / 2);
						KeyValue kv2 = new KeyValue(parameter.ellipseanimation.centerYProperty(),
								Math.min(parameter.DRAW_STARTY, (int) (Math.round(e.getY() - 20.0))) + 26
										+ Math.abs((int) (Math.round(e.getY() - 20.0)) - parameter.DRAW_STARTY) / 2);
						KeyValue kv3 = new KeyValue(parameter.ellipseanimation.radiusXProperty(),
								Math.abs((int) (Math.round(e.getX() - 140.0)) - parameter.DRAW_STARTX) / 2);
						KeyValue kv4 = new KeyValue(parameter.ellipseanimation.radiusYProperty(),
								Math.abs((int) (Math.round(e.getY() - 20.0)) - parameter.DRAW_STARTY) / 2);
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv1));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv2));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv3));
						timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), kv4));
						timeline.play();
					}
				}
			}
		});

		scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				border.getChildren().remove(parameter.rectangleanimation);
				border.getChildren().remove(parameter.ellipseanimation);
				border.getChildren().remove(parameter.penanimation);
				if (parameter.START_DRAW) {
					if (e.getX() > 140 && e.getY() > 20 && e.getY() < 380) {
						if (parameter.DRAW_MOD == "SQUARE") {
							gc.setFill(parameter.DRAW_COLOR);
							gc.setStroke(parameter.DRAW_COLOR);
							gc.fillRect(Math.min(parameter.DRAW_STARTX, (int) (Math.round(e.getX() - 140.0))),
									Math.min(parameter.DRAW_STARTY, (int) (Math.round(e.getY() - 20.0))),
									Math.abs((int) (Math.round(e.getX() - 140.0)) - parameter.DRAW_STARTX),
									Math.abs((int) (Math.round(e.getY() - 20.0)) - parameter.DRAW_STARTY));
						} else if (parameter.DRAW_MOD == "CIRCLE") {
							gc.setFill(parameter.DRAW_COLOR);
							gc.setStroke(parameter.DRAW_COLOR);
							gc.fillOval(Math.min(parameter.DRAW_STARTX, (int) (Math.round(e.getX() - 140.0))),
									Math.min(parameter.DRAW_STARTY, (int) (Math.round(e.getY() - 20.0))),
									Math.abs((int) (Math.round(e.getX() - 140.0)) - parameter.DRAW_STARTX),
									Math.abs((int) (Math.round(e.getY() - 20.0)) - parameter.DRAW_STARTY));
						} else if (parameter.DRAW_MOD == "FILL") {
							PixelReader gpr = canvas.snapshot(null, null).getPixelReader();
							PixelWriter gpw = gc.getPixelWriter();
							Color currnt_color = gpr.getColor((int) (e.getX() - 140.0), (int) (e.getY() - 20.0));
							boolean visited[][] = new boolean[(int) canvas.getWidth()][(int) canvas.getHeight()];
							Queue<Pair<Integer, Integer>> q = new LinkedList<>();
							q.add(new Pair<>((int) (e.getX() - 140.0), (int) (e.getY() - 20.0)));
							while (!q.isEmpty()) {
								Pair<Integer, Integer> pair = q.poll();
								if (visited[pair.getKey()][pair.getValue()] == true)
									continue;
								visited[pair.getKey()][pair.getValue()] = true;
								gpw.setColor(pair.getKey(), pair.getValue(), parameter.DRAW_COLOR);	
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
							parameter.historytmp.fillpoint
									.add(new Pair<Integer, Integer>(Integer.valueOf((int) (Math.round(e.getX() - 140))),
											Integer.valueOf((int) (Math.round(e.getY() - 20.0)))));
						}
						parameter.DRAW_STARTX = 0;
						parameter.DRAW_STARTY = 0;
						parameter.START_DRAW = false;

						// history
						parameter.historytmp.DRAW_ENDX = (int) (Math.round(e.getX() - 140.0));
						parameter.historytmp.DRAW_ENDY = (int) (Math.round(e.getY() - 20.0));
						parameter.historytmp.create_time = new Timestamp(System.currentTimeMillis());
						if (parameter.history.size() > parameter.history_pointer) {
							parameter.history.subList(parameter.history_pointer, parameter.history.size()).clear();
						}
						parameter.history.add(parameter.historytmp);
						parameter.history_pointer++;
						if (parameter.Client != null) {
							try {
								parameter.historytmp.separatecolor();
								parameter.Client.write(new Message(parameter.historytmp));
								parameter.historytmp.mergecolor();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						if (parameter.Server != null) {
							try {
								parameter.historytmp.separatecolor();
								for (int i = 0; i < parameter.Server.size(); i++) {
									parameter.Server.get(i).write(new Message(parameter.historytmp));
								}
								parameter.historytmp.mergecolor();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
					gc.closePath();
				}
			}
		});

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				for (int i = 0; i < parameter.Server.size(); i++) {
					if (parameter.Server.get(i) != null)
						parameter.Server.get(i).close();
				}

				if (parameter.Client != null)
					parameter.Client.close();
				System.exit(0);
			}
		});

		primaryStage.setTitle("多人小畫家");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image("file:src//paint//icon.png"));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch();
	}

}
