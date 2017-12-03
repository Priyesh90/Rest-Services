package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferFund;
import com.db.awmd.challenge.service.TransferFundImpl;

import static org.mockito.Mockito.verifyZeroInteractions;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private TransferFund transferFund;

	@Autowired
	private NotificationService notificationService;

	@Before
	public void setUp() {
		transferFund = Mockito.mock(TransferFundImpl.class);
		notificationService = Mockito.mock(NotificationService.class);
	}

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		assertThat(this.accountsService.getAccount("Id-123"))
				.isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo(
					"Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void makeTransferTest() {
		final String accountFromId = UUID.randomUUID().toString();
		final String accountToId = UUID.randomUUID().toString();
		final Account accountFrom = new Account(accountFromId, new BigDecimal(
				"200.50"));
		final Account accountTo = new Account(accountToId, new BigDecimal(
				"100.00"));
		when(
				transferFund.transfer(any(Account.class), any(Account.class),
						any(Transfer.class))).thenReturn(true);
		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);

		Transfer transfer = new Transfer(accountFromId, accountToId,
				new BigDecimal("100.50"));

		this.accountsService.makeTransfer(transfer);

		assertThat(this.accountsService.getAccount(accountFromId).getBalance())
				.isEqualTo(new BigDecimal("100.00"));
		assertThat(this.accountsService.getAccount(accountToId).getBalance())
				.isEqualTo(new BigDecimal("200.50"));
	}

	@Test
	public void makeTransferWithInvalidAccount() {
		final String accountFromId = UUID.randomUUID().toString();
		final String accountToId = UUID.randomUUID().toString();
		this.accountsService.createAccount(new Account(accountFromId));
		Transfer transfer = new Transfer(accountFromId, accountToId,
				new BigDecimal(100));
		try {
			this.accountsService.makeTransfer(transfer);
			fail("Should have failed because account does not exist");
		} catch (AccountNotFoundException anfe) {
			assertThat(anfe.getMessage()).isEqualTo(
					"Account " + accountToId + " not found.");
		}
		verifyZeroInteractions(transferFund);
		verifyZeroInteractions(notificationService);
	}

	@Test
	public void makeTransferWithNotEnoughFundsException() {
		final String accountFromId = UUID.randomUUID().toString();
		final String accountToId = UUID.randomUUID().toString();
		this.accountsService.createAccount(new Account(accountFromId));
		this.accountsService.createAccount(new Account(accountToId));
		Transfer transfer = new Transfer(accountFromId, accountToId,
				new BigDecimal(500));
		try {
			this.accountsService.makeTransfer(transfer);
			fail("Should have failed because account does not have enough funds for the transfer");
		} catch (NotEnoughFundsException nbe) {
			assertThat(nbe.getMessage()).isEqualTo(
					"Not enough funds on account " + accountFromId
							+ " balance=0");
		}
		verifyZeroInteractions(transferFund);
		verifyZeroInteractions(notificationService);
	}
}
