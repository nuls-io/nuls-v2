package io.nuls.tools.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author tag
 * */
public class DoubleUtils {
    public static final int DEFAULT_SCALE = 8;

    /**
     * 创建一个BigDecimal类型对象
     * @param value  初始值
     * @return 生成的BigDecimal对象
     * */
    public static BigDecimal createBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }


    /**
     * 双精度浮点数根据指定保留小数位数，指定小数保留模式处理double数据
     * @param value         需处理的double数据
     * @param scale         须保留的小数位数
     * @param roundingMode  小数的保留模式
     * @return              处理后的double数据
     * */
    public static double round(double value, int scale, int roundingMode) {
        BigDecimal bd = createBigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }


    /**
     * 双精度浮点数根据指定保留小数位数，默认小数保留模式处理double数据
     * @param value         需处理的double数据
     * @param scale         须保留的小数位数
     * @return              处理后的double数据
     * */
    public static double round(double value, int scale) {
        return round(value, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 双精度浮点数根据默认保留小数位数（八位），默认小数保留模式处理double数据
     * @param value         需处理的double数据
     * @return              处理后的double数据
     * */
    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * double数据根据指定保留小数位数，默认小数保留模式处理后再根据double数据显示规则转为字符串
     * @param value          需处理的double数据
     * @param scale          须保留的小数位数
     * @param hasThousands   转换规则（每三位是否需要用','分隔）
     * @return               转换后的字符串
     * */
    public static String getRoundStr(Double value, int scale, boolean hasThousands) {
        if (null == value) {
            return "";
        }
        String suffix = "";
        for (int i = 0; i < scale; i++) {
            if (i == 0) {
                suffix += ".";
            }
            suffix += "0";
        }

        if (hasThousands) {
            return new DecimalFormat("###,##0" + suffix).format(round(value, scale));
        } else {
            return new DecimalFormat("##0" + suffix).format(round(value, scale));
        }
    }

    /**
     * double数据根据指定保留小数位数，默认小数保留模式处理后再根据double数据默认显示规则转为字符串
     * @param value          需处理的double数据
     * @param scale          须保留的小数位数
     * @return               转换后的字符串
     * */
    public static String getRoundStr(Double value, int scale) {
        return getRoundStr(value, scale, false);
    }

    /**
     * double数据根据默认保留小数位数，默认小数保留模式处理后再根据double数据默认显示规则转为字符串
     * @param value          需处理的double数据
     * @return               转换后的字符串
     * */
    public static String getRoundStr(Double value) {
        return getRoundStr(value, DEFAULT_SCALE, false);
    }


    /**
     * 将数字字符串转为Double数据
     * @param value  数字字符串
     * @return 转换得到的Double数据
     * */
    public static Double parseDouble(String value) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return Double.parseDouble(value.replaceAll(",", "").trim());
    }


    /**
     * 将数字字符串转为Double数据，保留指定小数位
     * @param value  数字字符串
     * @param scale  小数位
     * @return 转换得到的Double数据
     * */
    public static Double parseDouble(String value, int scale) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        return round(Double.parseDouble(value.replaceAll(",", "").trim()), scale);
    }


    /**
     * 两个double数据相加
     * @param d1  被加数
     * @param d2  加数
     * @return    和
     * */
    public static double sum(double d1, double d2) {
        return round(createBigDecimal(d1).add(createBigDecimal(d2)).doubleValue());
    }

    public static double sum(double d1, BigDecimal d2) {
        return round(createBigDecimal(d1).add(d2).doubleValue());
    }

    /**
     * 两个double数据相减
     * @param d1  被减数
     * @param d2  减数
     * @return    差
     * */
    public static double sub(double d1, double d2) {
        return round(sub(createBigDecimal(d1), createBigDecimal(d2)).doubleValue());
    }

    public static double sub(double d1, BigDecimal d2) {
        return round(createBigDecimal(d1).subtract(d2).doubleValue());
    }

    /**
     * 两个double数据相乘
     * @param d1  被乘数
     * @param d2  乘数
     * @return    积
     * */
    public static double mul(double d1, double d2) {
        return mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue();
    }

    public static double mul(double d1, BigDecimal d2) {
        return createBigDecimal(d1).multiply(d2).doubleValue();
    }


    /**
     * 两个double数据相乘，保留指定位数
     * @param d1     被乘数
     * @param d2     乘数
     * @param scale  须保留的位数
     * @return       积
     * */
    public static double mul(double d1, double d2, int scale) {
        return round(mul(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }


    /**
     * 两个double数据相除，保留指定位数
     * @param d1     被除数
     * @param d2     除数
     * @param scale  须保留的位数
     * @return       商
     * */
    public static double div(double d1, double d2, int scale) {
        return round(div(createBigDecimal(d1), createBigDecimal(d2)).doubleValue(), scale);
    }

    public static double div(double d1, BigDecimal d2, int scale) {
        return round(createBigDecimal(d1).divide(d2).doubleValue(), scale);
    }


    /**
     * 两个double数据相除
     * @param d1     被除数
     * @param d2     除数
     * @return       商
     * */
    public static double div(double d1, double d2) {
        return div(d1, d2, DEFAULT_SCALE);
    }

    public static double div(double d1, BigDecimal d2) {
        return div(d1,d2,DEFAULT_SCALE);
    }

    /**
     * 两个BigDecimal数据相加
     * @param bd1  被加数
     * @param bd2  加数
     * @return     和
     * */
    public static BigDecimal sum(BigDecimal bd1, BigDecimal bd2) {
        return bd1.add(bd2);
    }


    /**
     * 两个BigDecimal数据相减
     * @param bd1  被减数
     * @param bd2  减数
     * @return     差
     * */
    public static BigDecimal sub(BigDecimal bd1, BigDecimal bd2) {
        return bd1.subtract(bd2);
    }


    /**
     * 两个BigDecimal数据相乘
     * @param bd1  被乘数
     * @param bd2  乘数
     * @return     积
     * */
    public static BigDecimal mul(BigDecimal bd1, BigDecimal bd2) {
        return bd1.multiply(bd2);
    }


    /**
     * 两个BigDecimal数据相除
     * @param bd1  被除数
     * @param bd2  除数
     * @return     商
     * */
    public static BigDecimal div(BigDecimal bd1, BigDecimal bd2) {
        if (bd2.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("除数不能为0！");
        }
        return bd1.divide(bd2, 12, RoundingMode.HALF_UP);
    }

    /**
     * BigDecimal数据与double数据相加
     * @param bd1  被加数
     * @param d2   加数
     * @return     和
     * */
    public static BigDecimal sum(BigDecimal bd1, double d2) {
        return sum(bd1, createBigDecimal(d2));
    }


    /**
     * BigDecimal数据与double数据相减
     * @param bd1  被减数
     * @param d2   减数
     * @return     差
     * */
    public static BigDecimal sub(BigDecimal bd1, double d2) {
        return sub(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimal数据与double数据相乘
     * @param bd1  被乘数
     * @param d2   乘数
     * @return     积
     * */
    public static BigDecimal mul(BigDecimal bd1, double d2) {
        return mul(bd1, createBigDecimal(d2));
    }

    /**
     * BigDecimal数据与double数据相除
     * @param bd1  被除数
     * @param d2   除数
     * @return     商
     * */
    public static BigDecimal div(BigDecimal bd1, double d2) {
        return div(bd1, createBigDecimal(d2));
    }


    /**
     * 求绝对值
     * @param d1 double数据
     * @return   绝对值
     * */
    public static double abs(double d1) {
        return Math.abs(d1);
    }

    /**
     * double数据转long型数据
     * @param val  double数据
     * @return     转换得到的long数据
     * */
    public static long longValue(double val) {
        return createBigDecimal(val).longValue();
    }

    /**
     * 两个double数据比较大小
     * @param d1  被比较数
     * @param d2  比较数
     * @return    比较结果
     * */
    public static int compare(double d1, double d2) {
        return createBigDecimal(d1).compareTo(createBigDecimal(d2));
    }
}
