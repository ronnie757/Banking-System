package bw.bank.model;

import java.math.BigDecimal;

public interface InterestBearing {
    BigDecimal calculateInterest();
    double getInterestRate();
}
