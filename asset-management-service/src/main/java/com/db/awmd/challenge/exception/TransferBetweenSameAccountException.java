package com.db.awmd.challenge.exception;

public class TransferBetweenSameAccountException extends RuntimeException {

	private static final long serialVersionUID = 8350618077563614067L;

	public TransferBetweenSameAccountException(String message) {
		super(message);
	}

}
