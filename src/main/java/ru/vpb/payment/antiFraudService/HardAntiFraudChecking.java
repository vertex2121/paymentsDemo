/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.antiFraudService;

import ru.vpb.payment.exception.AntiFraudException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import ru.vpb.payment.Utils;
import ru.vpb.payment.model.Payment;

/**
 *
 * @author vertex21
 */
public class HardAntiFraudChecking extends AbstractAntiFraudChecking {

    @Override
    public void check(List<Payment> payments, Payment payment) {
        dayLimit(payment);
        nightLimit(payment);
        oneDayLimit(payments, payment);
        oneHourToServiceLimit(payments, payment);
        oneDayToServiceCountLimit(payments, payment);
        oneServiceCountLimit(payments, payment);
        oneClientCountLimit(payments, payment);
    }

    private void dayLimit(Payment payment) {
        simpleCond(payment, p -> p.getAmount().compareTo(new BigDecimal(8000)) == 1
                && Utils.convertDateToLocalDateTime(p.getDate()).getHour() >= 7
                && Utils.convertDateToLocalDateTime(p.getDate()).getHour() <= 19,
                new AntiFraudException("Day limit violation."));

    }

    private void nightLimit(Payment payment) {
        simpleCond(payment, p -> p.getAmount().compareTo(new BigDecimal(7000)) == 1
                && (Utils.convertDateToLocalDateTime(p.getDate()).getHour() >= 19
                || Utils.convertDateToLocalDateTime(p.getDate()).getHour() <= 7),
                new AntiFraudException("Night limit violation."));
    }

    private void oneDayLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmt(payments, payment,
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(Utils.convertDateToLocalDateTime(payment.getDate()).minusDays(1))
                && p.getService().equals(payment.getService())
                && p.getAcount().equals(payment.getAcount()),
                p -> p.compareTo(new BigDecimal(10000)) == 1,
                new AntiFraudException("One day limit violation."));
    }

    private void oneHourToServiceLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmt(payments, payment, p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(Utils.convertDateToLocalDateTime(payment.getDate()).minusHours(1))
                && p.getService().equals(payment.getService()),
                amt -> amt.compareTo(new BigDecimal(7000)) == 1,
                new AntiFraudException("One hour to same service limit violation."));
    }

    private void oneDayToServiceCountLimit(List<Payment> payments, Payment payment) {
        condByServiceAndCnt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(Utils.convertDateToLocalDateTime(payment.getDate()).minusDays(1))
                && p.getService().equals(payment.getService()),
                cnt -> cnt > 3,
                new AntiFraudException("One day to same service count limit violation."));
    }

    private void oneServiceCountLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmtAndCnt(payments, payment,
                p -> Utils.convertDateToLocalDateTime(p.getDate()).getHour() >= 10
                && Utils.convertDateToLocalDateTime(p.getDate()).getHour() <= 17
                && Utils.convertDateToLocalDateTime(p.getDate()).isAfter(Utils.convertDateToLocalDateTime(payment.getDate()).withHour(10))
                && Utils.convertDateToLocalDateTime(p.getDate()).isBefore(Utils.convertDateToLocalDateTime(payment.getDate()).withHour(17))
                && p.getService().equals(payment.getService()),
                amt -> amt.compareTo(new BigDecimal(5000)) == 1,
                cnt -> cnt > 2,
                new AntiFraudException("One service count limit violation."));
    }

    private void oneClientCountLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmtAndCnt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(Utils.convertDateToLocalDateTime(payment.getDate()).minusHours(2)),
                amt -> amt.compareTo(new BigDecimal(4000)) == 1,
                cnt -> cnt > 2,
                new AntiFraudException("Single client count limit violation."));
    }

}
