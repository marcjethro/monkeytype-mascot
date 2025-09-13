import java.awt.image.BufferedImage;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyAdapter;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.NativeInputEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
	private static Timer shakeTimer;
	private static int shakeCount;
	private static final int SHAKE_INTERVAL_MS = 10;
	
	static ImageIcon monkeyUpImage;
	static ImageIcon monkeyLeftImage;
	static ImageIcon monkeyRightImage;
	static ImageIcon monkeyBothImage;

	static ImageIcon superUpImage;
	static ImageIcon superLeftImage;
	static ImageIcon superRightImage;
	static ImageIcon superBothImage;

	static int iconHeight;
	static int iconWidth;

	static Queue<Long> timeQueue = new LinkedList<>();

	static boolean goingRight;
	static int offsetXIndex;

	public static void main(String[] args) {
		monkeyUpImage = new ImageIcon(Main.class.getResource("/images/m3.png"));
		monkeyLeftImage = new ImageIcon(Main.class.getResource("/images/m1.png"));
		monkeyRightImage = new ImageIcon(Main.class.getResource("/images/m2.png"));
		monkeyBothImage = new ImageIcon(Main.class.getResource("/images/m4.png"));

		superUpImage = new ImageIcon(Main.class.getResource("/images/m3_fast.png"));
		superLeftImage = new ImageIcon(Main.class.getResource("/images/m1_fast.png"));
		superRightImage = new ImageIcon(Main.class.getResource("/images/m2_fast.png"));
		superBothImage = new ImageIcon(Main.class.getResource("/images/m4_fast.png"));

		iconHeight = monkeyUpImage.getIconHeight();
		iconWidth = monkeyUpImage.getIconWidth();

		JFrame frame = new JFrame();
		frame.setLayout(null);
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.getContentPane().setBackground(new Color(0, 0, 0, 0));
		frame.setAlwaysOnTop(true);
		frame.setSize(iconWidth + 50, iconHeight + 50);

		JLabel imageLabel = new JLabel();
		imageLabel.setIcon(monkeyUpImage);
		imageLabel.setBackground(new Color(0, 0, 0, 0));
		imageLabel.setOpaque(false);
		imageLabel.setBounds(25, 25, iconWidth, iconHeight);

		TJLabel superLabel = new TJLabel();
		superLabel.setIcon(superUpImage);
		superLabel.setAlpha(0f);
		superLabel.setBackground(new Color(0, 0, 0, 0));
		superLabel.setOpaque(false);
		superLabel.setLocation(25, 25);

		FrameDragListener frameDragListener = new FrameDragListener(frame);
		frame.addMouseListener(frameDragListener);
		frame.addMouseMotionListener(frameDragListener);

		frame.add(imageLabel);
		frame.add(superLabel);
		frame.setComponentZOrder(imageLabel, 1);
		frame.setComponentZOrder(superLabel, 0);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new MonKeyListener(imageLabel, superLabel));
		} catch (NativeHookException exc) {
			exc.printStackTrace();
		}

		Timer timeTimer = new Timer(50, (e) -> {
			long currentTime = System.currentTimeMillis();

			if (timeQueue.size() != 0) {
				while (timeQueue.peek() < currentTime - 1000) {
					timeQueue.poll();
					if (timeQueue.size() == 0) return;
				}
			}

			int numberOfChars = timeQueue.size();

			if (numberOfChars >= 8) {
				shakeCount = 40;
			}

			if (numberOfChars >= 12) {
				superLabel.setAlpha(1f);
			} else {
				superLabel.setAlpha(0f);
			}
		});

		Point originalCoords = imageLabel.getLocation();
		int[] offsetXArray = {-3, -2, -1, 0, 1, 2, 3};
		offsetXIndex = 3;
		goingRight = true;

		shakeTimer = new Timer(SHAKE_INTERVAL_MS, (e) -> {
			if (shakeCount != 0) {
				int offsetX = offsetXArray[offsetXIndex];
				imageLabel.setLocation(originalCoords.x + offsetX, originalCoords.y);
				superLabel.setLocation(originalCoords.x + offsetX, originalCoords.y);
				if (goingRight) {
					offsetXIndex++;
					if (offsetXIndex == 6) goingRight = false;
				} else {
					offsetXIndex--;
					if (offsetXIndex == 0) goingRight = true;
				}
				shakeCount--;
			} else {
				imageLabel.setLocation(originalCoords);
				superLabel.setLocation(originalCoords);
			}
		});

		shakeTimer.start();
		timeTimer.start();
	}

	public static class FrameDragListener extends MouseAdapter {
		private final JFrame frame;
		private Point compCoords = null;

		public FrameDragListener(JFrame frame) {
			this.frame = frame;
		}

		public void mouseReleased(MouseEvent e) {
			compCoords = null;
		}

		public void mousePressed(MouseEvent e) {
			compCoords = e.getPoint();
		}

		public void mouseDragged(MouseEvent e) {
			Point curr = e.getLocationOnScreen();
			frame.setLocation(curr.x - compCoords.x, curr.y - compCoords.y);
		}
	}

	static class MonKeyListener extends NativeKeyAdapter{
		JLabel imageLabel;
		TJLabel superLabel;
		boolean leftDown;
		boolean rightDown;

		MonKeyListener(JLabel imageLabel, TJLabel superLabel) {
			this.imageLabel = imageLabel;
			this.superLabel = superLabel;
			this.leftDown = false;
			this.rightDown = false;
		}

		void updateLbl() {
			if (leftDown && rightDown) {
				this.imageLabel.setIcon(monkeyBothImage);
				this.superLabel.setIcon(superBothImage);
			} else if (rightDown) {
				this.imageLabel.setIcon(monkeyRightImage);
				this.superLabel.setIcon(superRightImage);
			} else if (leftDown) {
				this.imageLabel.setIcon(monkeyLeftImage);
				this.superLabel.setIcon(superLeftImage);
			} else {
				this.imageLabel.setIcon(monkeyUpImage);
				this.superLabel.setIcon(superUpImage);
			}

			this.imageLabel.repaint();
			this.superLabel.repaint();
		}

		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			int eventKeyCode = e.getKeyCode();

			for (int leftKeyCode : Keys.left) {
				if (eventKeyCode == leftKeyCode) {
					leftDown = true;
					timeQueue.add(System.currentTimeMillis());
				}
			}
			for (int rightKeyCode : Keys.right) {
				if (eventKeyCode == rightKeyCode) {
					rightDown = true;
					timeQueue.add(System.currentTimeMillis());
				}
			}
			this.updateLbl();
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent e) {
			int eventKeyCode = e.getKeyCode();
			for (int leftKeyCode : Keys.left) {
				if (eventKeyCode == leftKeyCode) {
					leftDown = false;
				}
			}
			for (int rightKeyCode : Keys.right) {
				if (eventKeyCode == rightKeyCode) {
					rightDown = false;
				}
			}
			this.updateLbl();
		}
	}

}

class TJLabel extends JLabel {
	private float alpha = 0f; // Default to fully opaque
	private int x;
	private int y;
	private int width;
	private int height;

	public void setAlpha(float alpha) {
		if (alpha < 0f || alpha > 1f) {
			throw new IllegalArgumentException("Alpha value must be between 0.0f and 1.0f");
		}
		if (this.alpha > alpha) {
			this.alpha = Math.max(this.alpha - 0.25f, alpha);
		} else {
			this.alpha = Math.min(this.alpha + 0.2f, alpha);
		}

		repaint();
	}

	@Override
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Point getLocation() {
		return new Point(this.x, this.y);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create(); // Create a copy of the Graphics object
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		// Draw the image icon
		if (getIcon() != null) {
			ImageIcon icon = (ImageIcon) getIcon();
			Image image = icon.getImage();
			Point coords = getLocation();
			g2d.drawImage(image, coords.x, coords.y, icon.getIconWidth(), icon.getIconHeight(), this);
		}

		g2d.dispose();
	}
}
