package com.bharath.incometer.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
	CASH("Cash"),
	CREDIT_CARD("Credit Card"),
	DEBIT_CARD("Debit Card"),
	UPI("UPI"),
	WALLET("Wallet"),
	BANK_ACCOUNT("Bank Account"),
	NET_BANKING("Net Banking"),
	CHEQUE("Cheque"),
	OTHER("Other");

	private final String displayName;

	PaymentType(String displayName) {
		this.displayName = displayName;
	}

}
