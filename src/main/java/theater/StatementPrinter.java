package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private static final int TRAGEDY_BASE_AMOUNT = 40000;
    private static final int THIS_BASE_AMOUNT = 1000;
    private static final int THIS_BASE_AMOUNT_MODIFIER = 30;
    private static final int THIS_BASE_AMOUNT_MODIFIER_TWO = 100;
    private final Map<String, Play> plays;
    private Invoice invoice;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {

        final StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());
        for (Performance p : invoice.getPerformances()) {
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(p).getName(),
                    usd(getAmount(p)),
                    p.getAudience()));
        }

        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getVolumeCredits()));
        return result.toString();
    }

    private int getTotalAmount() {
        int totalAmount = 0;
        for (Performance p : invoice.getPerformances()) {
            totalAmount += getAmount(p);
        }
        return totalAmount;
    }

    private int getVolumeCredits() {
        int volumeCredits = 0;
        for (Performance p : invoice.getPerformances()) {
            volumeCredits += getVolumeCredits(p, volumeCredits);
        }
        return volumeCredits;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / THIS_BASE_AMOUNT_MODIFIER_TWO);
    }

    private int getVolumeCredits(Performance performance, int volumeCredits) {
        int result = 0;
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private Play getPlay(Performance p) {
        return plays.get(p.getPlayID());
    }

    private int getAmount(Performance performance) {
        int result;
        final Play play = getPlay(performance);
        switch (play.getType()) {
            case "tragedy":
                result = TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += THIS_BASE_AMOUNT * (performance.getAudience() - THIS_BASE_AMOUNT_MODIFIER);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }
}
