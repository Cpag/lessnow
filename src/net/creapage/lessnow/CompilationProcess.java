package net.creapage.lessnow;

import java.util.LinkedList;
import java.util.List;

import org.lesscss.LessCompiler;

public class CompilationProcess {

	private final String LOCK_QUEUE = "LOCK_QUEUE";
	private List<CompilationTask> queue = new LinkedList<CompilationTask>();
	private boolean active = false;
	private CompilationReport report;
	private LessCompiler compiler;

	public CompilationProcess(CompilationReport report) {
		this.report = report;
		this.compiler = new LessCompiler();
	}

	public void addToQueue(CompilationTask task) {
		synchronized (LOCK_QUEUE) {
			if (!queue.contains(task)) {
				queue.add(task);
				invokeQueue();
			}
		}
	}

	private void invokeQueue() {
		if (active)
			return;
		synchronized (LOCK_QUEUE) {
			if (queue.isEmpty())
				return;
		}
		active = true;
		new Thread() {
			@Override
			public void run() {
				report.toggleProcess(true);
				boolean queueEmpty;
				synchronized (LOCK_QUEUE) {
					queueEmpty = queue.isEmpty();
				}
				while (!queueEmpty) {
					CompilationTask task;
					synchronized (LOCK_QUEUE) {
						task = queue.get(0);
						queue.remove(0);
					}
					task.run(compiler);
					synchronized (LOCK_QUEUE) {
						queueEmpty = queue.isEmpty();
					}
				}
				report.toggleProcess(false);
				active = false;
				//invokeQueue();
			}
		}.start();
	}
}
