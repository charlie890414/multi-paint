package paint;

import java.util.Vector;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

public class Parameter {
	public boolean START_DRAW;
	public String DRAW_MOD;
	public Color DRAW_COLOR;
	public int DRAW_LINEWIDTH;
	public double DRAW_STARTX;
	public double DRAW_STARTY;
	public Circle penanimation;
	public Rectangle rectangleanimation;
	public Ellipse ellipseanimation;
	public Vector<History> history = new Vector<History>();
	public History historytmp;
	public Integer history_pointer;
	public Vector<SocketServer> Server = new Vector<SocketServer>();
	public SocketClient Client;
}
