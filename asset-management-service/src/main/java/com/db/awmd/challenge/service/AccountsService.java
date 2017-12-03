package com.db.awmd.challenge.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.exception.TransferBetweenSameAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;

@Service
@Slf4j
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	protected final Object lock = new Object();

	@Getter
	private final NotificationService notificationService;

	@Autowired
	private TransferFund transferFund;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository,
			NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	/**
	 * We also need to serialize access to the getAccount() method in order to
	 * avoid a situation in which other threads reading the total balance while
	 * the current thread is updating an account’s balance which affects the
	 * total balance. In other words, no thread can access the getAccount()
	 * method when the current threading is executing the transfer() method
	 * because both methods are locked by the same lock object bankLock.
	 * 
	 * @param accountId
	 * @return
	 */
	public Account getAccount(String accountId) {
		synchronized (lock) {
			return this.accountsRepository.getAccount(accountId);
		}
	}

	/**
	 * Makes a transfer between two accounts for the balance specified by the
	 * {@link Transfer} object
	 * 
	 * @param transfer
	 * @return
	 * @throws AccountNotFoundException
	 *             When an account does not exist
	 * @throws NotEnoughFundsException
	 *             When there are not enough funds to complete the transfer
	 * @throws TransferBetweenSameAccountException
	 *             Transfer to self account is not permitted
	 */
	public void makeTransfer(Transfer transferEntity)
			throws AccountNotFoundException, NotEnoughFundsException {
		log.info(" in account service transfer ");
		synchronized (lock) {
			final Account accountFrom = getAccount(transferEntity
					.getAccountFromId());
			final Account accountTo = getAccount(transferEntity
					.getAccountToId());

			// check require for validation of from and to account like if
			// accountFrom or accountTo equals to null then throw exception
			// InvalidAccountException
			if (accountFrom == null) {
				throw new AccountNotFoundException("Account "
						+ transferEntity.getAccountFromId() + " not found.");
			}

			if (accountTo == null) {
				throw new AccountNotFoundException("Account "
						+ transferEntity.getAccountToId() + " not found.");
			}
			// initiate the transfer
			boolean successful = transferFund.transfer(accountFrom, accountTo,
					transferEntity);

			if (successful) {// notify
				notifyCustomer(accountFrom, accountTo, transferEntity);
			}
		}
	}

	/**
	 * Sending notification to customer about transaction and account status
	 * 
	 * @param currAccountFrom
	 * @param currAccountTo
	 * @param transfer
	 */
	private void notifyCustomer(final Account currAccountFrom,
			final Account currAccountTo, final Transfer transfer) {
		notificationService.notifyAboutTransfer(currAccountFrom, "An amount "
				+ transfer.getAmount()
				+ " has been debited to your account with id "
				+ currAccountFrom.getAccountId() + " .");
		notificationService.notifyAboutTransfer(currAccountTo,
				"An amount " + transfer.getAmount()
						+ " has been credited to your account with id "
						+ currAccountTo.getAccountId() + " .");
	}
}