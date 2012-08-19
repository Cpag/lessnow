package net.creapage.lessnow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OverviewItem extends JPanel {
	private static final long serialVersionUID = 1L;

	// --
	// -- Constants
	// --

	private static Font fontLess;
	private static Font fontCss;
	private static Color dark;
	private static Color light;

	static {
		dark = new Color(180, 180, 180);
		light = new Color(220, 220, 220);
		try {
			fontLess = Font.createFont(Font.TRUETYPE_FONT, OverviewItem.class.getResourceAsStream("/res/Ubuntu-R.ttf")).deriveFont(
					Font.PLAIN, 20f);
			fontCss = fontLess.deriveFont(Font.PLAIN, 14f);
		} catch (Exception e) {
			fontLess = new Font(Font.SERIF, Font.PLAIN, 20);
			fontCss = new Font(Font.SERIF, Font.PLAIN, 16);
		}
	}

	private static final Icon OK = new ImageIcon(OverviewItem.class.getResource("/res/true.png"));
	private static final Icon ERROR = new ImageIcon(OverviewItem.class.getResource("/res/false.png"));
	private static final Icon WAIT = new ImageIcon(OverviewItem.class.getResource("/res/wait.png"));

	// --
	// -- Fields
	// --

	private LessFileStatus lfs;
	private JLabel infoMessageLabel, statusLabel;
	private String lastInfoMessage;
	private Icon lastStatusIcon;

	// --
	// -- Initialization
	// --

	public OverviewItem(LessFileStatus lfs) {
		this.lfs = lfs;
		init();
		refreshStatus();
	}

	// --
	// -- Public - Swing
	// --
	
	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		size.width = OverviewPane.getPreferredOverviewItemWidth();
		return size;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D enhanced = (Graphics2D) g;
		enhanced.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		enhanced.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		enhanced.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		super.paint(enhanced);
	}

	// --
	// -- Public
	// --

	public LessFileStatus getLessFileStatus() {
		return lfs;
	}

	public boolean refreshStatus() {
		// - Get the status
		Icon icon;
		String info = null;
		switch (lfs.getStatus()) {
		case LessFileStatus.UP_TO_DATE:
			icon = OK;
			break;
		case LessFileStatus.ERROR:
			icon = ERROR;
			break;
		case LessFileStatus.UNKNOWN:
		case LessFileStatus.PROCESSING:
			icon = WAIT;
			break;
		default:
			throw new BugError("Invalid status: " + lfs.getStatus());
		}
		info = lfs.getInfoMessage();

		// - Check if change
		if (lastStatusIcon == icon && lastInfoMessage != null && lastInfoMessage.equals(info))
			return false;
		this.lastInfoMessage = info;
		this.lastStatusIcon = icon;

		// - Update components
		infoMessageLabel.setText(info);
		statusLabel.setIcon(icon);
		return true;
	}

	// --
	// -- Private
	// --

	private void init() {
		// - Initial status
		Icon icon = WAIT;
		String info = "...";

		// - Create components
		JLabel lessLabel = new JLabel(lfs.getLessName());
		this.infoMessageLabel = new JLabel(info);

		lessLabel.setFont(fontLess);
		infoMessageLabel.setFont(fontCss);

		JPanel lessandcss = new JPanel(new BorderLayout());
		lessandcss.add(lessLabel, BorderLayout.NORTH);
		lessandcss.add(infoMessageLabel, BorderLayout.CENTER);
		lessandcss.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		this.statusLabel = new JLabel(icon);
//		((ImageIcon) icon).setImageObserver(stateLabel);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		this.setLayout(new BorderLayout(10, 10));
		this.add(statusLabel, BorderLayout.WEST);
		this.add(lessandcss, BorderLayout.CENTER);

		lessLabel.setOpaque(false);
		infoMessageLabel.setOpaque(false);
		statusLabel.setOpaque(false);

		lessandcss.setOpaque(false);
		this.setBackground(light);
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, dark));
	}
}
