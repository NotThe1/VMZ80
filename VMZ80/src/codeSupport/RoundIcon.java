package codeSupport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class RoundIcon implements Icon{
	Color color;

	public RoundIcon(Color c) {
		color = c;
	}// Constructor

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(color);
		g.fillOval(x, y, getIconWidth(), getIconHeight());
	}// paintIcon

	@Override
	public int getIconHeight() {
		return 10;
	}// getIconHeigt

	@Override
	public int getIconWidth() {
		return 10;
	}// getIconWidth
}//class RoundIcon1
