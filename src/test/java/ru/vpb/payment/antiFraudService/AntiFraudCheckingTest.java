/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.antiFraudService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static ru.vpb.payment.Utils.convertLocalDateTimeToDate;
import ru.vpb.payment.model.Payment;
import ru.vpb.payment.paymentService.PaymentService;

/**
 *
 * @author vertex21
 */
public class AntiFraudCheckingTest {

    PaymentService paymentService;

    public AntiFraudCheckingTest() {
    }

    @Before
    public void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    public void testCheckingCorrectClass() {
        Payment payment1 = new Payment(1L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(9)), new BigDecimal(6000), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(2L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(9)), new BigDecimal(6000), "Payment to MTC", "89859621477");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.pay();
        Payment.Status status1 = paymentService.getPaymentStatus(payment1);
        Payment.Status status2 = paymentService.getPaymentStatus(payment2);
        assertEquals(Payment.Status.checked, status1);
        assertEquals(Payment.Status.suspicious, status2);
    }
}
