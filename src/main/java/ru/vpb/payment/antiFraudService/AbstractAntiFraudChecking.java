/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.antiFraudService;

import ru.vpb.payment.exception.AntiFraudException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import ru.vpb.payment.model.Payment;

/**
 *
 * @author vertex21
 */
public abstract class AbstractAntiFraudChecking implements Checking {

    void simpleCond(Payment payment, Predicate<Payment> condition, AntiFraudException ex) {
        if (condition.test(payment)) {
            throw ex;
        }
    }

    void condByServiceAndAmt(List<Payment> payments, Payment payment, Predicate<Payment> filters, Predicate<BigDecimal> amount, AntiFraudException ex) {
        ArrayList<Payment> newPayments = new ArrayList<>(payments);
        newPayments.add(payment);
        BigDecimal oneDayAmount = newPayments.stream()
                .filter(filters)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.test(oneDayAmount)) {
            throw ex;
        }
    }

    void condByServiceAndCnt(List<Payment> payments, Payment payment, Predicate<Payment> filters, Predicate<Long> count, AntiFraudException ex) {
        ArrayList<Payment> newPayments = new ArrayList<>(payments);
        newPayments.add(payment);
        Long cnt = newPayments.stream()
                .filter(filters)
                .count();
        if (count.test(cnt)) {
            throw ex;
        }
    }

    void condByServiceAndAmtAndCnt(List<Payment> payments, Payment payment, Predicate<Payment> filters, Predicate<BigDecimal> amount, Predicate<Integer> count, AntiFraudException ex) {
        ArrayList<Payment> newPayments = new ArrayList<>(payments);
        newPayments.add(payment);
        final AtomicInteger oneServiceCnt = new AtomicInteger();
        BigDecimal oneServiceAmtTtl = newPayments.stream()
                .filter(filters)
                .peek(p -> {oneServiceCnt.getAndIncrement();})
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.test(oneServiceAmtTtl) && count.test(oneServiceCnt.get())) {
            throw ex;
        }
    }

}
