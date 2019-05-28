package paint;

import java.sql.Timestamp;
import java.util.Vector;

import javafx.scene.paint.Color;
import javafx.util.Pair;

public class History implements java.io.Serializable {
	private static final long serialVersionUID = 2L;
	String DRAW_MOD;
	int DRAW_LINEWIDTH;
	double DRAW_STARTX;
	double DRAW_STARTY;
	double DRAW_ENDX;
	double DRAW_ENDY;
	Color DRAW_COLOR;
	double red;
	double green;
	double blue;
	double opacity;
	Vector<Pair<Integer, Integer>> fillpoint = new Vector<Pair<Integer, Integer>>();
	Vector<Pair<Double, Double>> point = new Vector<Pair<Double, Double>>();
	Timestamp create_time;

	public void mergecolor() {
		if (DRAW_COLOR == null) {
			DRAW_COLOR = new Color(red, green, blue, opacity);
		}
	}

	public void separatecolor() {
		if (DRAW_COLOR != null) {
			red = DRAW_COLOR.getRed();
			green = DRAW_COLOR.getGreen();
			blue = DRAW_COLOR.getBlue();
			opacity = DRAW_COLOR.getOpacity();
			DRAW_COLOR = null;
		}
	}

	public String toString() {
		return "DRAW_MOD: " + DRAW_MOD + "\n" + "DRAW_STARTX: " + DRAW_STARTX + "\n" + "DRAW_STARTY: " + DRAW_STARTY
				+ "\n" + "DRAW_ENDX: " + DRAW_ENDX + "\n" + "DRAW_ENDY: " + DRAW_ENDY + "\n" + "DRAW_LINEWIDTH: "
				+ DRAW_LINEWIDTH + "\n" + "point: " + point + "\n" + "create_time: " + create_time;
	}
}
