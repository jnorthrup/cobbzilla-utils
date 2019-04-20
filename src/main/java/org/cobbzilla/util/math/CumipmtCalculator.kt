package org.cobbzilla.util.math

import java.lang.Math.pow

object CumipmtCalculator {

    fun cumipmt(ratePerPeriod: Double, paymentsCount: Int, financed: Double, startPeriod: Int,
                endPeriod: Int, isDueOnPeriodEnd: Boolean): Double {
        var startPeriod = startPeriod
        if (startPeriod < 1 || endPeriod < startPeriod || ratePerPeriod <= 0 || endPeriod > paymentsCount ||
                paymentsCount <= 0 || financed <= 0)
            throw IllegalArgumentException()

        var payment = 0.0

        if (startPeriod == 1) {
            if (isDueOnPeriodEnd) payment = -financed
            startPeriod++
        }

        var rmz = -financed * ratePerPeriod / (1 - 1 / pow(1 + ratePerPeriod, paymentsCount.toDouble()))
        if (!isDueOnPeriodEnd) rmz /= 1 + ratePerPeriod

        for (i in startPeriod..endPeriod) {
            payment += getPeriodPaymentAddOn(ratePerPeriod, i.toDouble(), rmz, financed, isDueOnPeriodEnd)
        }

        return payment * ratePerPeriod
    }

    private fun getPeriodPaymentAddOn(ratePerPeriod: Double, periodIndex: Double, rmz: Double, financed: Double,
                                      isDueOnPeriodEnd: Boolean): Double {
        val term = pow(1 + ratePerPeriod, periodIndex - if (isDueOnPeriodEnd) 1 else 2)

        var addOn = rmz * (term - 1) / ratePerPeriod
        if (!isDueOnPeriodEnd) addOn *= 1 + ratePerPeriod

        var res = -(financed * term + addOn)
        if (!isDueOnPeriodEnd) res -= rmz

        return res
    }
}
