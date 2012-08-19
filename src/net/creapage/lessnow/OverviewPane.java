package net.creapage.lessnow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class OverviewPane extends JPanel implements CompilationReport {
	private static final long serialVersionUID = -4127456742787322239L;

	// --
	// -- Fields
	// --

	private final String LOCK_MODEL = "LOCK_MODEL";
	private Gui gui;
	private Font font;
	private Icon loadingIco = new ImageIcon(OverviewPane.class.getResource("/res/loading.gif"));
	private Icon emptyIco = new ImageIcon(OverviewPane.class.getResource("/res/nothing.png"));

	private JPanel listPane;
	private JLabel process = new JLabel(emptyIco, JLabel.CENTER);

	private List<LessFileStatus> sortedLFS = new LinkedList<LessFileStatus>();

	private static Comparator<LessFileStatus> lfsComparator = new Comparator<LessFileStatus>() {
		@Override
		public int compare(LessFileStatus a, LessFileStatus b) {
			return a.compareTo(b);
		}
	};

	// --
	// -- Initialization
	// --

	public OverviewPane(Gui gui) {
		this.gui = gui;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, OverviewItem.class.getResourceAsStream("/font/arvo.ttf")).deriveFont(Font.PLAIN,
					24f);
		} catch (Exception e) {
			font = new Font(Font.SERIF, Font.PLAIN, 24);
		}

		listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
		hackListComp = this;
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.add(listPane);
		JScrollPane scrollPane = new JScrollPane(wrapperPanel);
		this.setLayout(new BorderLayout());
		this.add(process, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		process.setFont(font);
		process.setOpaque(true);
		process.setBackground(new Color(225, 225, 225));
		process.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));

		listPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					showPopup(listPane.getComponentAt(e.getPoint()), e.getPoint());
			}
		});

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					synchronized (LOCK_MODEL) {
						reorderModelList();
					}
					refreshAll();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 5000, 5000);
	}

	// --
	// -- Public
	// --

	public boolean isEmpty() {
		synchronized (LOCK_MODEL) {
			return listPane.getComponentCount() == 0;
		}
	}

	// --
	// -- Public - CompilationReport
	// --

	@Override
	public void toggleProcess(boolean on) {
		if (on)
			process.setIcon(loadingIco);
		else
			process.setIcon(emptyIco);
	}

	@Override
	public void add(LessFileStatus lfs) {
//System.out.println("ADD " + lfs.getLessName());
		synchronized (LOCK_MODEL) {
			// - Compute sortedIndex
			int sortedIndex, sortedLen = sortedLFS.size();
			for (sortedIndex = 0; sortedIndex < sortedLen; sortedIndex++) {
				LessFileStatus item = sortedLFS.get(sortedIndex);
				if (lfs.compareTo(item) < 0)
					break;
			}
			// - Add element in sorted list
			if (sortedIndex == sortedLen)
				sortedLFS.add(lfs);
			else
				sortedLFS.add(sortedIndex, lfs);
			// - Compute compIndex
			int compIndex, compLen = listPane.getComponentCount();
			if (lfs.shouldBeShowed()) {
				for (compIndex = 0; compIndex < compLen; compIndex++) {
					LessFileStatus item = ((OverviewItem) listPane.getComponent(compIndex)).getLessFileStatus();
					if (lfs.compareTo(item) < 0)
						break;
				}
				// - Add element in JPanel
				if (compIndex == compLen)
					listPane.add(lfs.getOverviewItem());
				else
					listPane.add(lfs.getOverviewItem(), compIndex);
				lfs.setShowed(true);
				updateGui();
			}
			// - Reorder
			reorderModelList();
		}
	}

	@Override
	public void remove(LessFileStatus lfs) {
//System.out.println("REMOVE " + lfs.getLessName());
		synchronized (LOCK_MODEL) {
			sortedLFS.remove(lfs);
			listPane.remove(lfs.getOverviewItem());
			lfs.setShowed(false);
		}
		updateGui();
	}

	@Override
	public void refreshAll() {
		boolean reorder = false;
		synchronized (LOCK_MODEL) {
			for (LessFileStatus lfs : sortedLFS) {
				lfs.getOverviewItem().refreshStatus();
				if (lfs.shouldBeShowed() != lfs.isShowed())
					reorder = true;
			}
		}
		if (reorder)
			reorderModelList();
	}

	// --
	// -- Private
	// --

	private void reorderModelList() {
		Collections.sort(sortedLFS, lfsComparator);
		boolean changed = false;
		int compIndex = -1;
//System.out.println("reorder");
		for (LessFileStatus sorted : sortedLFS) {
			if (!sorted.shouldBeShowed())
				continue;
			compIndex++;
			OverviewItem sortedComp = sorted.getOverviewItem();
			boolean hasComp = compIndex < listPane.getComponentCount();
			if (!hasComp) {
				listPane.add(sortedComp);
				sorted.setShowed(true);
				changed = true;
				continue;
			}
			if (listPane.getComponent(compIndex) == sorted.getOverviewItem())
				continue;
//System.out.println("	==> " + i + " " + sorted.getLessName());
			listPane.remove(sortedComp);
			listPane.add(sortedComp, compIndex);
			sorted.setShowed(true);
			changed = true;
		}
		compIndex++;
		while (compIndex != -1 && compIndex < listPane.getComponentCount()) {
			OverviewItem comp = (OverviewItem) listPane.getComponent(compIndex);
			listPane.remove(compIndex);
			comp.getLessFileStatus().setShowed(true);
			changed = true;
		}
		if (changed)
			updateGui();
	}

	private void updateGui() {
		listPane.revalidate();
		gui.setWithOverview(!isEmpty());
	}

	private void showPopup(Component comp, Point point) {
		final OverviewItem overviewItem = (OverviewItem) comp;
		final LessFileStatus lfs = overviewItem.getLessFileStatus();
		final DirProject dirProject = lfs.getDirProject();
		OptionPopup popup = new OptionPopup(lfs, new Runnable() {
			@Override
			public void run() {
				dirProject.ignoreFile(lfs);
				sortedLFS.remove(lfs);
				listPane.remove(overviewItem);
			}
		}, new Runnable() {
			@Override
			public void run() {
				dirProject.compile(lfs);
			}
		});
		popup.show(listPane, point.x, point.y);
	}

	// --
	// -- HACK
	// --
	
	private static JPanel hackListComp;

	static int getPreferredOverviewItemWidth() {
		if (hackListComp == null)
			return -1;
		return hackListComp.getWidth() - 24;
	}
}