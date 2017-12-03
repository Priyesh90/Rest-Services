package com.db.awmd.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.exception.TransferBetweenSameAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;

public class TransferFundImplTest {

	private TransferFund transferFund;

	private AccountsRepository accountsRepository;

	@Before
	public void setUp() {
		accountsRepository = Mockito.mock(AccountsRepository.class);
		transferFund = new TransferFundImpl(accountsRepository);
	}

	@Test
	public void testTransferWithinSameAccount() {
		final Account accountFrom = new Account("Id-1", new BigDecimal("50.00"));
		final Account accountTo = new Account("Id-1");
		final Transfer transfer = new Transfer("Id-1", "Id-1", new BigDecimal(
				"1.00"));

		try {
			transferFund.transfer(accountFrom, accountTo, transfer);
			fail("Same account transfer");
		} catch (TransferBetweenSameAccountException sae) {
			assertThat(sae.getMessage()).isEqualTo(
					"Transfer to self account not allowed.");
		}
	}

	@Test
	public void testTransfer() throws Exception {
		final Account accountFrom = new Account("Id-1", new BigDecimal("10.00"));
		final Account accountTo = new Account("Id-2");
		final Transfer transfer = new Transfer("Id-1", "Id-2", new BigDecimal(
				"1.00"));

		transferFund.transfer(accountFrom, accountTo, transfer);
	}
}