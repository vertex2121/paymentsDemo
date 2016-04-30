/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.paymentService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import ru.vpb.payment.model.Payment;
import ru.vpb.payment.exception.PaymentException;
import java.util.Queue;
import ru.vpb.payment.exception.AntiFraudException;
import ru.vpb.payment.antiFraudService.AntiFraudService;
import ru.vpb.payment.model.Payment.Status;

/**
 *
 * @author vertex21
 */
public class PaymentService {

    private final Queue<Payment> payments = new LinkedList<>();
    private final Queue<Payment> delayedPayments = new LinkedList<>();
    private final List<Payment> executedPayments = new ArrayList<>();
    private static final PaymentHandler PAYMENT_HANDLER = new PaymentHandler();
    private final AntiFraudService afs = new AntiFraudService();

    public void pay() {
        while (!payments.isEmpty()) {
            Payment payment = payments.poll();
            try {
                afs.check(payment);
                PAYMENT_HANDLER.execute(payment);
                executedPayments.add(payment);
            } catch (PaymentException pex) {
                throw new PaymentException();
            } catch (AntiFraudException afex) {
                payment.setStatus(Payment.Status.suspicious);
                delayedPayments.offer(payment);
            }
        }
    }

    public boolean addPayToQueue(Payment payment) {
        return payments.offer(payment);
    }
    
    public Status getPaymentStatus(Payment payment) {
        if (payments.contains(payment)) {
            return payment.getStatus();
        } else if (delayedPayments.contains(payment)) {
            return payment.getStatus();
        } else if (executedPayments.contains(payment)) {
            return payment.getStatus();
        } else {
            throw new NoSuchElementException();
        }
    }

    public void checkDelayed() {
        for (Payment payment : delayedPayments) {
            payment.setStatus(Payment.Status.checked);
            try {
                PAYMENT_HANDLER.execute(payment);
            } catch (PaymentException pex) {
                throw new PaymentException();
            }
        }
    }

}
