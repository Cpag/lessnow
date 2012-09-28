package net.creapage.lessnow;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LessFileStatus {

	// --
	// -- Constants
	// --

	public static final int UNKNOWN = -1;
	public static final int UP_TO_DATE = 0;
	public static final int ERROR = 1;
	public static final int PROCESSING = 2;

	// --
	// -- Fields
	// --

	private File from, to;
	private int status = UNKNOWN;

	private String errorMessage;

	private DirProject dirProject;
	private int showUpdatedFilesDays;
	private OverviewItem overviewItem;
	private boolean showed = false;
	private String lessName;

	// --
	// -- Initialization
	// --

	public LessFileStatus(DirProject parent, File from, File to, String relativePath, int showUpdatedFilesDays) {
		this.dirProject = parent;
		this.from = from;
		this.to = to;
		this.showUpdatedFilesDays = showUpdatedFilesDays;
		this.lessName = dirProject.getName() + '/' + relativePath;
		if (isUpToDate())
			setStatus(UP_TO_DATE);
	}

	// --
	// -- Public - write
	// --

	public void setStatus(int s) {
		this.status = s;
		if (s != ERROR)
			errorMessage = null;
		if (overviewItem != null) {
			overviewItem.refreshStatus();
			overviewItem.repaint();
		}
	}

	public void setErrorStatus(String error) {
		this.errorMessage = error;
		this.setStatus(ERROR);
	}

	public void setShowed(boolean showed) {
		this.showed = showed;
	}

	// --
	// -- Public - read
	// --

	public DirProject getDirProject() {
		return dirProject;
	}

	public File getFrom() {
		return from;
	}

	public File getTo() {
		return to;
	}

	public int getStatus() {
		return status;
	}

	public boolean isDeleted() {
		return !from.exists();
	}

	public long getLastModified() {
		if (from.exists())
			return from.lastModified();
		return 0;
	}

	public long getLastModifiedTo() {
		if (to.exists())
			return to.lastModified();
		return 0;
	}

	public String getLessName() {
		return lessName;
	}

	public boolean isUpToDate() {
		if (!to.isFile())
			return false;
		return to.lastModified() > from.lastModified();
	}

	public String getInfoMessage() {
		if (status == ERROR)
			return errorMessage;
		return createUpdatedMessage();
	}

	public int compareTo(LessFileStatus other) {
		long diff = getLastModified() - other.getLastModified();
		return diff > 0 ? -1 : (diff < 0 ? 1 : other.getLessName().compareTo(getLessName()));
	}

	public boolean shouldBeShowed() {
//		System.out.println(getLessName() + " " + (showUpdatedFilesDays == -1 || showUpdatedFilesDays > getDayCountSinceUpdated()));
		return showUpdatedFilesDays == -1 || showUpdatedFilesDays >= getDayCountSinceUpdated();
	}

	public OverviewItem getOverviewItem() {
		if (overviewItem == null)
			overviewItem = new OverviewItem(this);
		return overviewItem;
	}

	public boolean isShowed() {
		return showed;
	}

	// --
	// -- Private
	// --

	private String createUpdatedMessage() {
		long lastModified = getLastModified();
		long diff = System.currentTimeMillis() - lastModified;
		if (diff < 10000)
			return "Updated just now!";
		if (diff < (1000 * 60 * 5))
			return "Updated not more than 5 minutes ago.";
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long currentDateWithoutTime = cal.getTimeInMillis();
		if (lastModified >= currentDateWithoutTime) {
			DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			return "Updated today: " + timeFormat.format(new Date(lastModified));
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "Updated on " + dateFormat.format(new Date(lastModified));
	}

	private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

	private int getDayCountSinceUpdated() {
		long curTime = System.currentTimeMillis() / DAY_IN_MS;
		long lastModified = getLastModified() / DAY_IN_MS;
		long count = curTime - lastModified;
//System.out.println(getLessName() + " " + count + " : " + curTime + " - " + lastModified + " [" + showUpdatedFilesDays + "]");
		return (int) count;
	}
}
