package com.db.awmd.challenge.exception;

public class NotEnoughFundsException extends RuntimeException {

	private static final long serialVersionUID = -1716623641183284787L;

	public NotEnoughFundsException(String message) {
		super(message);
	}
}
