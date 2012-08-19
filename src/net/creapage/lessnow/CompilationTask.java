package net.creapage.lessnow;

import java.io.IOException;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;

public class CompilationTask {

	private LessFileStatus lfs;
	private String charset;
	private boolean minify;

	public CompilationTask(LessFileStatus lfs, String charset, boolean minify) {
		this.lfs = lfs;
		this.charset = charset;
		this.minify = minify;
	}

	public CompilationTask(LessFileStatus lfs2) {
		// TODO Auto-generated constructor stub
	}

	public LessFileStatus getLfs() {
		return lfs;
	}

	public void run(LessCompiler compiler) {
		lfs.setStatus(LessFileStatus.PROCESSING);
		try {
			compiler.setEncoding(charset);
			compiler.setCompress(minify);
			compiler.init();
			compiler.compile(lfs.getFrom(), lfs.getTo(), true);
			lfs.setStatus(LessFileStatus.UP_TO_DATE);
		} catch (IOException e) {
			lfs.setErrorStatus(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (LessException e) {
			lfs.setErrorStatus(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
