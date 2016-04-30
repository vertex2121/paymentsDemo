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
public class SoftAntiFraudChecking extends AbstractAntiFraudChecking {

    @Override
    public void check(List<Payment> payments, Payment payment) {
        dayLimit(payment);
        nightLimit(payment);
        oneDayLimit(payments, payment);
        oneHourToServiceLimit(payments, payment);
        oneDayToServeceCountLimit(payments, payment);
        oneServiceCountLimit(payments, payment);
        oneClientCountLimit(payments, payment);
    }

    private void dayLimit(Payment payment) {
        simpleCond(payment, p -> p.getAmount().compareTo(new BigDecimal(5000)) == 1
                && Utils.convertDateToLocalDateTime(p.getDate()).getHour() >= 9
                && Utils.convertDateToLocalDateTime(p.getDate()).getHour() <= 23,
                new AntiFraudException("Day limit violation."));

    }

    private void nightLimit(Payment payment) {
        simpleCond(payment, p -> p.getAmount().compareTo(new BigDecimal(5000)) == 1
                && (Utils.convertDateToLocalDateTime(p.getDate()).isAfter(LocalDateTime.now().withHour(23))
                        || Utils.convertDateToLocalDateTime(p.getDate()).isBefore(LocalDateTime.now().withHour(9))),
                new AntiFraudException("Night limit violation."));
    }

    private void oneDayLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(LocalDateTime.now().minusDays(1))
                && p.getService().equals(payment.getService())
                && p.getAcount().equals(payment.getAcount()), 
                p -> p.compareTo(new BigDecimal(2000)) == 1,
                new AntiFraudException("One day limit violation."));
    }

    private void oneHourToServiceLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(LocalDateTime.now().minusHours(1))
                && p.getService().equals(payment.getService()),
                amt -> amt.compareTo(new BigDecimal(3000)) == 1,
                new AntiFraudException("One hour to same service limit violation."));
    }

    private void oneDayToServeceCountLimit(List<Payment> payments, Payment payment) {
        condByServiceAndCnt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(LocalDateTime.now().minusDays(1))
                && p.getService().equals(payment.getService()),
                cnt -> cnt > 20,
                new AntiFraudException("One day to same service count limit violation."));
    }

    private void oneServiceCountLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmtAndCnt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(LocalDateTime.now().withHour(10))
                && Utils.convertDateToLocalDateTime(p.getDate()).isBefore(LocalDateTime.now().withHour(17))
                && p.getService().equals(payment.getService()),
                amt -> amt.compareTo(new BigDecimal(4000)) == 1,
                cnt -> cnt > 30,
                new AntiFraudException("One service count limit violation."));
    }

    private void oneClientCountLimit(List<Payment> payments, Payment payment) {
        condByServiceAndAmtAndCnt(payments, payment, 
                p -> Utils.convertDateToLocalDateTime(p.getDate()).isAfter(LocalDateTime.now().minusHours(2)),
                amt -> amt.compareTo(new BigDecimal(3000)) == 1,
                cnt -> cnt > 10,
                new AntiFraudException("Single client count limit violation."));
    }

}
