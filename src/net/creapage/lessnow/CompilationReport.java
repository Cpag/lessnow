package net.creapage.lessnow;

public interface CompilationReport {
	public void toggleProcess(boolean val);
	public void add(LessFileStatus lfs);
	public void remove(LessFileStatus lfs);
	public void refreshAll();
}
