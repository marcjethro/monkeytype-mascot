import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyAdapter;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.NativeInputEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
	static ImageIcon monkeyUpImage = new ImageIcon("m3.png");
	static ImageIcon monkeyLeftImage = new ImageIcon("m1.png");
	static ImageIcon monkeyRightImage = new ImageIcon("m2.png");
	static ImageIcon monkeyBothImage = new ImageIcon("m4.png");
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.getContentPane().setBackground(new Color(0, 0, 0, 0));
		frame.setAlwaysOnTop(true);
		JLabel imageLabel = new JLabel(monkeyUpImage);
		imageLabel.setBackground(new Color(0, 0, 0, 0));
		imageLabel.setOpaque(false);

		FrameDragListener frameDragListener = new FrameDragListener(frame);
		frame.addMouseListener(frameDragListener);
		frame.addMouseMotionListener(frameDragListener);

		frame.add(imageLabel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new MonKeyListener(imageLabel));
		} catch (NativeHookException exc) {
			exc.printStackTrace();
		}

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
		boolean leftDown;
		boolean rightDown;

		MonKeyListener(JLabel imageLabel) {
			this.imageLabel = imageLabel;
			this.leftDown = false;
			this.rightDown = false;
		}

		void updateLbl() {
			if (leftDown && rightDown) {
				this.imageLabel.setIcon(monkeyBothImage);
			} else if (rightDown) {
				this.imageLabel.setIcon(monkeyRightImage);
			} else if (leftDown) {
				this.imageLabel.setIcon(monkeyLeftImage);
			} else {
				this.imageLabel.setIcon(monkeyUpImage);
			}
			this.imageLabel.repaint();
		}

		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			int eventKeyCode = e.getKeyCode();
			for (int leftKeyCode : Keys.left) {
				if (eventKeyCode == leftKeyCode) {
					leftDown = true;
				}
			}
			for (int rightKeyCode : Keys.right) {
				if (eventKeyCode == rightKeyCode) {
					rightDown = true;
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
