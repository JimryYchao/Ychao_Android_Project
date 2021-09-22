package com.Ychao.PerformanceAnalysis.Utils;

import java.math.BigDecimal;

public class DoubleUtil {

    /**
     * double 乘法
     */
    public static double mul(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));

        try {
            return bd1.multiply(bd2).doubleValue();
        } catch (Exception e) {
            // 根据bugly观测，在进入GTOpMulPerfActivity页时有极小概率crash，故加上异常保护
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * double 除法
     */
    public static double div(double d1, double d2, int scale) {
        // 当然在此之前，你要判断分母是否为0，
        // 为0你可以根据实际需求做相应的处理

        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
//		return bd1.divide(bd2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        // 直接向下取整，保持和UI展示一致
        try {
            return bd1.divide(bd2, scale, BigDecimal.ROUND_DOWN).doubleValue();
        } catch (Exception e) {
            // 根据bugly观测，在进入GTOpMulPerfActivity页时有极小概率crash，故加上异常保护
            e.printStackTrace();
            return 0;
        }

    }

}
