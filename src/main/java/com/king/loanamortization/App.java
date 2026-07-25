package com.king.loanamortization;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class App 
{
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);

        BigDecimal principal = promptBigDecimal(scanner, "Loan principal (e.g. 250000.00): ");
        BigDecimal annualRatePercent = promptBigDecimal(scanner, "Annual interest rate as a percent (e.g. 6.5): ");
        int termMonths = promptInt(scanner, "Term in months (e.g. 360 for 30 years): ");

        Loan loan = new Loan(principal, annualRatePercent, termMonths);
        AmortizationCalculator calculator = new AmortizationCalculator();
        List<AmortizationEntry> schedule = calculator.generateSchedule(loan);

        printSchedule(loan, schedule);
    }

    private static void printSchedule(Loan loan, List<AmortizationEntry> schedule) {
        System.out.println();
        System.out.printf("Loan: %s at %s%% for %d months%n",
                loan.principal(), loan.annualRatePercent(), loan.termMonths());
        System.out.println("=".repeat(70));
        System.out.printf("%-6s %-12s %-14s %-14s %-14s%n",
                "Month", "Payment", "Principal", "Interest", "Balance");
        System.out.println("-".repeat(70));

        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPrincipal = BigDecimal.ZERO;

        for (AmortizationEntry entry : schedule) {
            System.out.println(entry);
            totalInterest = totalInterest.add(entry.interestPaid());
            totalPrincipal = totalPrincipal.add(entry.principalPaid());
        }

        System.out.println("-".repeat(70));
        System.out.printf("Total principal paid: %s%n", totalPrincipal);
        System.out.printf("Total interest paid:  %s%n", totalInterest);
        System.out.printf("Final balance:        %s%n",
                schedule.get(schedule.size() - 1).remainingBalance());
    }

    private static BigDecimal promptBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                // constructed from a String -> exact, no binary floating-point contamination.
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, try again.");
            }
        }
    }

    private static int promptInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer, try again.");
            }
        }
    }
}
