import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Visual extends JPanel {

	private static final long serialVersionUID = -986203131141035214L;
	private static final int size = 1050;

	int[] stars, to;

	private Visual(int[] stars, int[] to) {
		this.stars = stars;
		this.to = to;
	}

	public static void visual(int[] stars, int[] to) {
		JFrame f = new JFrame();
		f.getContentPane().add(new Visual(stars, to));
		f.setSize(size, size);
		f.setVisible(true);
	}

	public void paint(Graphics g) {
		try {
			BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) bi.getGraphics();
			g2.setColor(Color.WHITE);
			for (int i = 0; i < stars.length; i += 2) {
				g2.fillOval(stars[i] - 1, stars[i + 1] - 1, 3, 3);
			}
			for (int i = 0; i < to.length; ++i) {
				if (to[i] == -1) continue;
				int x1 = stars[i * 2];
				int y1 = stars[i * 2 + 1];
				int x2 = stars[to[i] * 2];
				int y2 = stars[to[i] * 2 + 1];
				g2.drawLine(x1, y1, x2, y2);
			}
			g.drawImage(bi, 0, 0, size, size, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
