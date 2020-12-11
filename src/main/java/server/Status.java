package server;


/**
 * Creates a server.Status enum type for tracking errors. Each server.Status enum type
 * will use the ordinal as the error code, and store a message describing
 * the error.
 * Example of Prof. Engle
 * @see StatusTester
 */
public enum Status {

	/*
	 * Creates several server.Status enum types. The server.Status name and message is
	 * given in the NAME(message) format below. The server.Status ordinal is
	 * determined by its position in the list. (For example, OK is the
	 * first element, and will have ordinal 0.)
	 */

	OK("No errors occured."),
	ERROR("Unknown error occurred."),
	MISSING_CONFIG("Unable to find configuration file."),
	MISSING_VALUES("Missing values in configuration file."),
	CONNECTION_FAILED("Failed to establish a database connection."),
	CREATE_FAILED("Failed to create necessary tables."),
	INVALID_LOGIN("Invalid username and/or password."),

	// Registration errors:
	DUPLICATE_USER("User with that username already exists."),
	INVALID_USERNAME("Username cannot be blank."),
	INVALID_PASSWORD("Password must be between 5 to 10 characters, with at least one number, one letter, and one special character."),

	SQL_EXCEPTION("Unable to execute SQL statement.");

	private final String message;

	private Status(String message) {
		this.message = message;
	}

	public String message() {
		return message;
	}

	@Override
	public String toString() {
		return this.message;
	}
}