package io.nuls.core.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author tag
 */
public class DoubleUtils {
    public static final int DEFAULT_SCALE = 8;

    /**
     * Create aBigDecimalType Object
     *
     * @param value Initial value
     * @return GeneratedBigDecimalobject
     */
    public static BigDecimal createBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }


    /**
     * Double precision floating-point numbers are processed according to the specified number of decimal places and the specified decimal retention modedoubledata
     *
     * @param value        To be processeddoubledata
     * @param scale        Decimal Places to be Retained
     * @param roundingMode Decimal retention mode
     * @return Processeddoubledata
     */
    public static double round(double value, int scale, RoundingMode roundingMode) {
        BigDecimal bd = createBigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }


    /**
     * Double precision floating-point numbers are processed according to the specified number of decimal places and default decimal retention modedoubledata
     *
     * @param value To be processeddoubledata
     * @param scale Decimal Places to be Retained
     * @return Processeddoubledata
     */
    public static double round(double value, int scale) {
        return round(value, scale, RoundingMode.HALF_UP);
    }

    /**
     * Double precision floating-point numbers retain decimal places by default（Eight digit）, default decimal retention mode processingdoubledata
     *
     * @param value To be processeddoubledata
     * @return Processeddoubledata
     */
    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * doubleThe data is processed according to the specified decimal places, with default decimal retention mode, and then processed according to thedoubleConvert data display rules to strings
     *
     * @param value        To be processeddoubledata
     * @param scale        Decimal Places to be Retained
     * @param hasThousands Conversion rules（Do you need to use every three digits','separate）
     * @return Converted string
     */
    public static String getRoundStr(Double value, int scale, boolean hasThousands) {
        if (null == value) {
            return "";
        }
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < scale; i++) {
            if (i == 0) {
                suffix.append(".");
            }
            suffix.append("0");
        }

        if (hasThousands) {
            return new DecimalFormat("###,##0" + suffix).format(round(value, scale));
        } else {
            return new DecimalFormat("##0" + suffix).format(round(value, scale));
        }
    }

    /**
     * doubleThe data is processed according to the specified decimal places, with default decimal retention mode, and then processed according to thedoubleConvert default data display rules to strings
     *
     * @param value To be processeddoubledata
     * @param scale Decimal Places to be Retained
     * @return Converted string
     */
    public static String getRoundStr(Double value, int scale) {
        return getRoundStr(value, scale, false);
    }

    /**
     * doubleThe data is processed according to the default decimal places, and then processed according to the default decimal retention modedoubleConvert default data display rules to strings
     *
     * @param value To be processeddoubledata
     * @return Converted string
     */
    public static String getRoundStr(Double value) {
        return getRoundStr(value, DEFAULT_SCALE, false);
    }


    /**
     * Convert numeric strings toDoubledata
     *
     * @param value Number string
     * @return ConvertedDoubledata
     */
    public static Double parseDouble(String value) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return Double.parseDouble(value.replaceAll(",", "").trim());
    }


    /**
     * Convert numeric strings toDoubleData, retain specified decimal places
     *
     * @param value Number string
     * @param scale Decimal places
     * @return ConvertedDoubledata
     */
    public static Double parseDouble(String value, int scale) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return round(Double.parseDouble(value.replaceAll(",", "").trim()), scale);
    }


    /**
     * TwodoubleData addition
     *
     * @param d1 Addend
     * @param d2 Addend
     * @return and
     */
    public static double sum(double d1, double d2) {
        return round(createBigDecimal(d1).add(createBigDecimal(d2)).doubleValue());
    }

    public static double sum(double d1, BigDecimal d2) {
        return round(createBigDecimal(d1).add(d2).doubleValue());
    }

    /**
     * TwodoubleData subtraction
     *
     * @param d1 Subtracted number
     * @param d2 Subtraction
     * @return difference
     */
    public static double sub(double d1, double d2) {
        return round(sub(createBigDecimal(d1), createBigDecimal(d2)).doubleValue());
    }

    public static double sub(double d1, BigDecimal d2) {
        return round(createBigDecimal(d1).subtract(d2).doubleValue());
    }

    /**
     * TwodoubleData multiplication
     *
     * @param d1 Multiplicand
     * @param d2 multiplier
     * @return product
     */
    public static double mul(double d1, double d2) {
        return mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue();
    }

    public static double mul(double d1, BigDecimal d2) {
        return createBigDecimal(d1).multiply(d2).doubleValue();
    }


    /**
     * TwodoubleMultiplying data while retaining specified digits
     *
     * @param d1    Multiplicand
     * @param d2    multiplier
     * @param scale Number of digits to be retained
     * @return product
     */
    public static double mul(double d1, double d2, int scale) {
        return round(mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }


    /**
     * TwodoubleData division, retaining specified number of digits
     *
     * @param d1    Dividend
     * @param d2    Divisor
     * @param scale Number of digits to be retained
     * @return merchant
     */
    public static double div(double d1, double d2, int scale) {
        return round(div(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }

    public static double div(double d1, BigDecimal d2, int scale) {
        return round(createBigDecimal(d1).divide(d2).doubleValue(), scale);
    }

    public static double div(BigInteger b1, BigInteger b2, int scale) {
        BigDecimal d1 = new BigDecimal(b1);
        BigDecimal d2 = new BigDecimal(b2);
        return round(div(d1, d2).doubleValue(), scale);
    }

    /**
     * TwodoubleData division
     *
     * @param d1 Dividend
     * @param d2 Divisor
     * @return merchant
     */
    public static double div(double d1, double d2) {
        return div(d1, d2, DEFAULT_SCALE);
    }

    public static double div(double d1, BigDecimal d2) {
        return div(d1, d2, DEFAULT_SCALE);
    }

    /**
     * TwoBigDecimalData addition
     *
     * @param bd1 Addend
     * @param bd2 Addend
     * @return and
     */
    public static BigDecimal sum(BigDecimal bd1, BigDecimal bd2) {
        return bd1.add(bd2);
    }


    /**
     * TwoBigDecimalData subtraction
     *
     * @param bd1 Subtracted number
     * @param bd2 Subtraction
     * @return difference
     */
    public static BigDecimal sub(BigDecimal bd1, BigDecimal bd2) {
        return bd1.subtract(bd2);
    }


    /**
     * TwoBigDecimalData multiplication
     *
     * @param bd1 Multiplicand
     * @param bd2 multiplier
     * @return product
     */
    public static BigDecimal mul(BigDecimal bd1, BigDecimal bd2) {
        return bd1.multiply(bd2);
    }


    /**
     * TwoBigDecimalData division
     *
     * @param bd1 Dividend
     * @param bd2 Divisor
     * @return merchant
     */
    public static BigDecimal div(BigDecimal bd1, BigDecimal bd2) {
        if (bd2.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("The divisor cannot be0！");
        }
        return bd1.divide(bd2, 12, RoundingMode.HALF_UP);
    }

    /**
     * BigDecimalData anddoubleData addition
     *
     * @param bd1 Addend
     * @param d2  Addend
     * @return and
     */
    public static BigDecimal sum(BigDecimal bd1, double d2) {
        return sum(bd1, createBigDecimal(d2));
    }


    /**
     * BigDecimalData anddoubleData subtraction
     *
     * @param bd1 Subtracted number
     * @param d2  Subtraction
     * @return difference
     */
    public static BigDecimal sub(BigDecimal bd1, double d2) {
        return sub(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimalData anddoubleData multiplication
     *
     * @param bd1 Multiplicand
     * @param d2  multiplier
     * @return product
     */
    public static BigDecimal mul(BigDecimal bd1, double d2) {
        return mul(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimalData anddoubleData division
     *
     * @param bd1 Dividend
     * @param d2  Divisor
     * @return merchant
     */
    public static BigDecimal div(BigDecimal bd1, double d2) {
        return div(bd1, createBigDecimal(d2));
    }


    /**
     * Find absolute value
     *
     * @param d1 doubledata
     * @return absolute value
     */
    public static double abs(double d1) {
        return Math.abs(d1);
    }

    /**
     * doubleData conversionlongType data
     *
     * @param val doubledata
     * @return Convertedlongdata
     */
    public static long longValue(double val) {
        return createBigDecimal(val).longValue();
    }

    /**
     * TwodoubleData comparison size
     *
     * @param d1 Number of Comparables
     * @param d2 Comparison number
     * @return Comparison results
     */
    public static int compare(double d1, double d2) {
        return createBigDecimal(d1).compareTo(createBigDecimal(d2));
    }
}
