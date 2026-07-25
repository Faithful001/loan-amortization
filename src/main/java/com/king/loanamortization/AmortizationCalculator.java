package com.king.loanamortization;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AmortizationCalculator {

    private static final int KOBO_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN; // use the banker's rounding (HALF_EVEN)
    private static final MathContext INTERMEDIATE_MC = new MathContext(25, RoundingMode.HALF_EVEN);

    /**
     * Generates the full month-by-month amortization schedule for the given loan.
     */
    public List<AmortizationEntry> generateSchedule(Loan loan) {
        // calculate monthly rate
        BigDecimal monthlyRate = monthlyRate(loan.annualRatePercent());
        // calculate monthly payment
        BigDecimal payment = calculateMonthlyPayment(loan.principal(), monthlyRate, loan.termMonths());

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

    /**
     * converts a normal annual rate into as a percentage value (e.g. 6.5 -> 6.5%)
     */
    private BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent
                .divide(BigDecimal.valueOf(100), INTERMEDIATE_MC)
                .divide(BigDecimal.valueOf(12), INTERMEDIATE_MC);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int totalMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(totalMonths), KOBO_SCALE, ROUNDING);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRateToThePowerOfMonths = onePlusRate.pow(totalMonths, INTERMEDIATE_MC);

        BigDecimal numerator = principal.multiply(monthlyRate, INTERMEDIATE_MC)
                .multiply(onePlusRateToThePowerOfMonths, INTERMEDIATE_MC);
        BigDecimal denominator = onePlusRateToThePowerOfMonths.subtract(BigDecimal.ONE, INTERMEDIATE_MC);

        return numerator.divide(denominator, INTERMEDIATE_MC).setScale(KOBO_SCALE, ROUNDING);
    }
}
