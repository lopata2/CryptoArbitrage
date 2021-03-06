package BenTrapani.CryptoArbitrage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/***
 * 
 * @author benjamintrapani
 *
 *         Fraction class because standard library doesn't have one it seems...
 *
 */
public class Fraction implements Comparable<Fraction> {
	public final BigInteger numerator;
	public final BigInteger denominator;

	public Fraction(int value) {
		numerator = BigInteger.valueOf(value);
		denominator = BigInteger.ONE;
	}

	public Fraction(BigInteger val) {
		numerator = val;
		denominator = BigInteger.ONE;
	}

	public Fraction(BigInteger num, BigInteger denom) {
		BigIntPair reducedPair = computeReduced(num, denom);
		numerator = reducedPair.n1;
		denominator = reducedPair.n2;
	}

	public Fraction(long num, long denom) {
		this(BigInteger.valueOf(num), BigInteger.valueOf(denom));
	}

	public Fraction(BigDecimal val) {
		BigInteger unscaledValue = val.unscaledValue();
		BigInteger tempDenom;
		if (val.scale() < 0) {
			unscaledValue = unscaledValue.multiply(BigInteger.valueOf(val.scale() * -1));
			tempDenom = BigInteger.ONE;
		} else {
			tempDenom = BigInteger.TEN.pow(val.scale());
		}
		BigIntPair reducedPair = computeReduced(unscaledValue, tempDenom);
		numerator = reducedPair.n1;
		denominator = reducedPair.n2;
	}

	public BigDecimal convertToBigDecimal(int scale, int roundingMode) {
		return new BigDecimal(numerator).divide(new BigDecimal(denominator), scale, roundingMode);
	}

	public Fraction multiply(Fraction other) {
		return new Fraction(other.numerator.multiply(numerator), other.denominator.multiply(denominator));
	}

	public Fraction divide(Fraction other) {
		return new Fraction(other.denominator.multiply(numerator), other.numerator.multiply(denominator));
	}

	private static class FractionPairWithCommonDenominator {
		public final BigInteger numerator1;
		public final BigInteger numerator2;
		public final BigInteger commonDenominator;

		public FractionPairWithCommonDenominator(final Fraction f1, final Fraction f2) {
			commonDenominator = f1.denominator.multiply(f2.denominator);
			BigInteger commonDenomFac1 = f2.denominator;
			BigInteger commonDenomFac2 = f1.denominator;
			numerator1 = f1.numerator.multiply(commonDenomFac1);
			numerator2 = f2.numerator.multiply(commonDenomFac2);
		}
	}

	public Fraction add(Fraction other) {
		FractionPairWithCommonDenominator fracPair = new FractionPairWithCommonDenominator(this, other);
		return new Fraction(fracPair.numerator1.add(fracPair.numerator2), fracPair.commonDenominator);
	}

	public Fraction subtract(Fraction other) {
		FractionPairWithCommonDenominator fracPair = new FractionPairWithCommonDenominator(this, other);
		return new Fraction(fracPair.numerator1.subtract(fracPair.numerator2), fracPair.commonDenominator);
	}

	// This converts to double, and will reduce precision.
	// The precision will be reduced so that the ratio expressed is smaller than
	// it really is.
	public Fraction logLossy() {
		BigDecimal divResult = new BigDecimal(numerator).divide(new BigDecimal(denominator), 20, RoundingMode.DOWN);
		double doubleDivRes = divResult.doubleValue();
		return new Fraction(new BigDecimal(Math.log(doubleDivRes)));
	}

	public Fraction max(Fraction other) {
		if (compareTo(other) > 0) {
			return this;
		}
		return other;
	}

	public Fraction min(Fraction other) {
		if (compareTo(other) < 0) {
			return this;
		}
		return other;
	}

	private static class BigIntPair {
		public final BigInteger n1;
		public final BigInteger n2;

		public BigIntPair(BigInteger n1, BigInteger n2) {
			this.n1 = n1;
			this.n2 = n2;
		}
	}

	private static BigIntPair computeReduced(BigInteger tempNum, BigInteger tempDenom) {
		BigInteger gcd = tempNum.abs().gcd(tempDenom.abs());
		BigInteger reducedNum = tempNum.divide(gcd);
		BigInteger reducedDenom = tempDenom.divide(gcd);
		return new BigIntPair(reducedNum, reducedDenom);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Fraction other = (Fraction) obj;
		return numerator.equals(other.numerator) && denominator.equals(other.denominator);
	}

	@Override
	public int hashCode() {
		return numerator.hashCode() + denominator.hashCode() * 51;
	}

	@Override
	public int compareTo(Fraction o) {
		FractionPairWithCommonDenominator pair = new FractionPairWithCommonDenominator(this, o);
		return pair.numerator1.compareTo(pair.numerator2);
	}

	@Override
	public String toString() {
		return numerator.toString() + " / " + denominator.toString();
	}
}
