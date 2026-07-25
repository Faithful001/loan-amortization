package com.king.loanamortization;

import java.math.BigDecimal;

public record AmortizationEntry(
        int month,
        BigDecimal payment,
        BigDecimal principalPaid,
        BigDecimal interestPaid,
        BigDecimal remainingBalance
) {
    @Override
    public String toString() {
        return String.format("%-6d %-12s %-14s %-14s %-14s",
                month, payment, principalPaid, interestPaid, remainingBalance);
    }
}
