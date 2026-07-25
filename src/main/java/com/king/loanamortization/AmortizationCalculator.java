package com.king.loanamortization;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AmortizationCalculator {
    private static final int KOBO_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;
    private static final MathContext INTERMEDIATE_MC = new MathContext(25, RoundingMode.HALF_EVEN);

    public List<AmortizationEntry> generateSchedule(Loan loan) {
        BigDecimal monthlyRate = loan.annualRatePercent()
            .divide(BigDecimal.valueOf(100), INTERMEDIATE_MC)
            .divide(BigDecimal.valueOf(12), INTERMEDIATE_MC);

        // calculate monthly payment
        if (monthlyRate.signum() == 0) {
            loan.principal().divide(BigDecimal.valueOf(loan.termMonths()));
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRToN = onePlusR.pow(loan.termMonths(), INTERMEDIATE_MC);

        BigDecimal numerator = loan.principal().multiply(monthlyRate, INTERMEDIATE_MC)
                .multiply(onePlusRToN, INTERMEDIATE_MC);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE, INTERMEDIATE_MC);

        BigDecimal payment = numerator.divide(denominator, INTERMEDIATE_MC).setScale(KOBO_SCALE, ROUNDING);

        List<AmortizationEntry> schedule = new ArrayList<>(loan.termMonths());
        BigDecimal balance = loan.principal().setScale(KOBO_SCALE, ROUNDING);

        for (int month = 1; month <= loan.termMonths(); month++) {
            BigDecimal interest = balance.multiply(monthlyRate, INTERMEDIATE_MC)
                    .setScale(KOBO_SCALE, ROUNDING);

            BigDecimal principalPortion;
            BigDecimal actualPayment;

            boolean isLastPayment = (month == loan.termMonths());

            if (isLastPayment) {
                // pay off whatever balance remains exactly, no more, no less.
                principalPortion = balance;
                actualPayment = principalPortion.add(interest);
            } else {
                actualPayment = payment;
                principalPortion = actualPayment.subtract(interest);

                if (principalPortion.compareTo(balance) > 0) {
                    principalPortion = balance;
                    actualPayment = principalPortion.add(interest);
                }
            }

            balance = balance.subtract(principalPortion).setScale(KOBO_SCALE, ROUNDING);

            schedule.add(new AmortizationEntry(month, actualPayment, principalPortion, interest, balance));
        }

        return schedule;
    }
}
