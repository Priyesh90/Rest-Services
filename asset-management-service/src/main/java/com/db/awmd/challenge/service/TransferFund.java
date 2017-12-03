package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;

public interface TransferFund {

	boolean transfer(final Account accountFrom, final Account accountTo,
			final Transfer transfer) throws AccountNotFoundException,
			NotEnoughFundsException;
}
