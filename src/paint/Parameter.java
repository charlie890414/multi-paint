package paint;

import java.util.Vector;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

public class Parameter {
	public boolean START_DRAW = false;
	public String DRAW_MOD = "PENCIL";
	public Color DRAW_COLOR = Color.BLACK;
	public int DRAW_LINEWIDTH = 1;
	public double DRAW_STARTX = 0;
	public double DRAW_STARTY = 0;
	public Circle penanimation;
	public Rectangle rectangleanimation;
	public Ellipse ellipseanimation;
	public Vector<History> history = new Vector<History>();
	public History historytmp = null;
	public Integer history_pointer = 0;
	public Vector<SocketServer> Server = new Vector<SocketServer>();
	public SocketClient Client = null;
}
