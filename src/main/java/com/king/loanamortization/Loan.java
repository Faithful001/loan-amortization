package com.king.loanamortization;

import java.math.BigDecimal;

public record Loan(BigDecimal principal, BigDecimal annualRatePercent, int termMonths) {

    public Loan {
        if (principal == null || annualRatePercent == null) {
            throw new IllegalArgumentException("principal and annualRatePercent must not be null");
        }
        if (principal.signum() <= 0) {
            throw new IllegalArgumentException("principal must be positive");
        }
        if (annualRatePercent.signum() < 0) {
            throw new IllegalArgumentException("annualRatePercent must not be negative");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("termMonths must be positive");
        }
    }
}
