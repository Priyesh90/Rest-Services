package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountUpdate;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account)
			throws DuplicateAccountIdException {
		Account previousAccount = ((ConcurrentHashMap<String, Account>) accounts)
				.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id "
					+ account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public boolean updateAccountsBatch(List<AccountUpdate> accountUpdates) {
		for (AccountUpdate accountUpdate : accountUpdates) {
			if (accounts.containsKey(accountUpdate.getAccountId())) {
				Account account = accounts.get(accountUpdate.getAccountId());
				account.setBalance(accountUpdate.getAmount());
				accounts.put(accountUpdate.getAccountId(), account);
			}
		}
		return true;
	}
}
