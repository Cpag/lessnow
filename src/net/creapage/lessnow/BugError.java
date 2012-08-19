package net.creapage.lessnow;

public class BugError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BugError(String message) {
		super(message);
	}

	public BugError(String message, Throwable cause) {
		super(message, cause);
	}

	public BugError(Throwable cause) {
		super(cause);
	}
}
