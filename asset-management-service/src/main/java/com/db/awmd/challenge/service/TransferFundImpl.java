package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Arrays;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountUpdate;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.exception.TransferBetweenSameAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;

@Component
public class TransferFundImpl implements TransferFund {

	protected final Object lock = new Object();

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public TransferFundImpl(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	/**
	 * Method which transfer fund to another account.
	 * 
	 * Here for comparison , StringUtils equals method is use instead of equals
	 * because StringUtils equals method is null safe and optimize
	 * 
	 * old : To handler NullPointer we write below code snippet 
	 *  
	 * if (testString != null && testString.equals("TestString")) {
         //Do the operation
      } 
	 * New Using StringUtils :
	 * 
	 * if (StringUtils.equals(testString, "TestString")) {
         //Do the operations
      }
	 * 
	 * @param currAccountFrom
	 *            The existing source account
	 * @param currAccountTo
	 *            Destination account
	 * @param transfer
	 *            The transfer object as requested
	 * @throws AccountNotFoundException
	 * @throws NotEnoughFundsException
	 * @throws TransferBetweenSameAccountException
	 */
	public boolean transfer(final Account currAccountFrom,
			final Account currAccountTo, final Transfer transfer)
			throws NotEnoughFundsException, TransferBetweenSameAccountException {
		boolean status = false;
		synchronized (lock) {
			// StringUtils class operations on the String that are null safe.
			if (StringUtils.equals(currAccountFrom.getAccountId(),
					transfer.getAccountToId())) {
				throw new TransferBetweenSameAccountException(
						"Transfer to self account not allowed.");
			} else if (!enoughFunds(currAccountFrom, transfer.getAmount())) {
				throw new NotEnoughFundsException(
						"Not enough funds in account "
								+ currAccountFrom.getAccountId() + " balance="
								+ currAccountFrom.getBalance());
			} else {
				final BigDecimal amount = transfer.getAmount();
				// atomic operation in production
				status = accountsRepository.updateAccountsBatch(Arrays.asList(
						new AccountUpdate(currAccountFrom.getAccountId(),
								currAccountFrom.getBalance().subtract(amount)),
						new AccountUpdate(currAccountTo.getAccountId(),
								currAccountTo.getBalance().add(amount))));
			}
		}
		return status;
	}

	private boolean enoughFunds(final Account account, final BigDecimal amount) {
		return account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
	}
}