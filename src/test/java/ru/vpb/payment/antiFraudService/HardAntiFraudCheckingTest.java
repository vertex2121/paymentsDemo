/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.antiFraudService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static ru.vpb.payment.Utils.convertLocalDateTimeToDate;
import ru.vpb.payment.model.Payment;
import ru.vpb.payment.model.Payment.Status;
import ru.vpb.payment.paymentService.PaymentService;

/**
 *
 * @author vertex21
 */
public class HardAntiFraudCheckingTest {

    PaymentService paymentService;

    public HardAntiFraudCheckingTest() {
    }

    @Before
    public void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    public void testDayLimitViolation() {
        Payment payment = new Payment(1L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(9)), new BigDecimal(8001), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment);
        paymentService.pay();
        Status status = paymentService.getPaymentStatus(payment);
        assertEquals(Status.suspicious, status);
    }

    @Test
    public void testDayLimitPass() {
        Payment payment = new Payment(1L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(9)), new BigDecimal(7000), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment);
        paymentService.pay();
        Status status = paymentService.getPaymentStatus(payment);
        assertEquals(Status.checked, status);
    }

    @Test
    public void testNigthLimitViolation() {
        Payment payment = new Payment(2L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(5)), new BigDecimal(7001), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment);
        paymentService.pay();
        Status status = paymentService.getPaymentStatus(payment);
        assertEquals(Status.suspicious, status);
    }

    @Test
    public void testNigthLimitPass() {
        Payment payment = new Payment(2L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(5)), new BigDecimal(6500), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment);
        paymentService.pay();
        Status status = paymentService.getPaymentStatus(payment);
        assertEquals(Status.checked, status);
    }

    @Test
    public void testOneDayLimitViolation() {
        Payment payment1 = new Payment(3L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(0)), new BigDecimal(6500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(4L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(9)), new BigDecimal(7000), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        assertEquals(Status.checked, status1);
        assertEquals(Status.suspicious, status2);
    }

    @Test
    public void testOneDayLimitPass() {
        Payment payment1 = new Payment(5L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(0)), new BigDecimal(3500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(6L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(9)), new BigDecimal(5000), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
    }

    @Test
    public void testOneHourToServiceLimitViolation() {
        Payment payment1 = new Payment(7L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusMinutes(20)), new BigDecimal(3500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(8L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusMinutes(10)), new BigDecimal(4000), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        assertEquals(Status.checked, status1);
        assertEquals(Status.suspicious, status2);
    }

    @Test
    public void testOneHourToServiceLimitPass() {
        Payment payment1 = new Payment(9L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusMinutes(20)), new BigDecimal(3500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(10L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusMinutes(10)), new BigDecimal(3000), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
    }
    
    @Test
    public void testOneDayToServiceCountLimitViolation() {
        Payment payment1 = new Payment(11L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(11)), new BigDecimal(350), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(12L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(15)), new BigDecimal(400), "Payment to Megafon", "89859621478");
        Payment payment3 = new Payment(13L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(8)), new BigDecimal(400), "Payment to Megafon", "89859621478");
        Payment payment4 = new Payment(14L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(5)), new BigDecimal(400), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment3);
        paymentService.addPayToQueue(payment4);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        Status status3 = paymentService.getPaymentStatus(payment3);
        Status status4 = paymentService.getPaymentStatus(payment4);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
        assertEquals(Status.checked, status3);
        assertEquals(Status.suspicious, status4);
    }
    
    @Test
    public void testOneDayToServiceCountLimitPass() {
        Payment payment1 = new Payment(18L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(19).minusDays(1)), new BigDecimal(350), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(15L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(10)), new BigDecimal(400), "Payment to Megafon", "89859621478");
        Payment payment3 = new Payment(16L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(15)), new BigDecimal(400), "Payment to Megafon", "89859621478");
        Payment payment4 = new Payment(17L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(20)), new BigDecimal(400), "Payment to Megafon", "89859621478");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment3);
        paymentService.addPayToQueue(payment4);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        Status status3 = paymentService.getPaymentStatus(payment3);
        Status status4 = paymentService.getPaymentStatus(payment4);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
        assertEquals(Status.checked, status3);
        assertEquals(Status.checked, status4);
    }
    
    @Test
    public void testOneServiceCountLimitViolation() {
        Payment payment1 = new Payment(19L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(11)), new BigDecimal(1500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(20L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(15)), new BigDecimal(2500), "Payment to Megafon", "89859621479");
        Payment payment3 = new Payment(21L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(16)), new BigDecimal(1200), "Payment to Megafon", "89859621477");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment3);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        Status status3 = paymentService.getPaymentStatus(payment3);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
        assertEquals(Status.suspicious, status3);
    }
    
    @Test
    public void testOneServiceCountLimitPass() {
        Payment payment1 = new Payment(22L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusHours(10)), new BigDecimal(350), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(23L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusHours(15)), new BigDecimal(400), "Payment to Megafon", "89859621479");
        Payment payment4 = new Payment(24L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().minusHours(18)), new BigDecimal(400), "Payment to Megafon", "89859621476");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment4);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        Status status4 = paymentService.getPaymentStatus(payment4);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
        assertEquals(Status.checked, status4);
    }
    
    @Test
    public void testOneClientCountLimitViolation() {
        Payment payment1 = new Payment(19L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(11)), new BigDecimal(1500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(20L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(12)), new BigDecimal(1500), "Payment to Megafon", "89859621479");
        Payment payment3 = new Payment(21L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(12)), new BigDecimal(1200), "Payment to Megafon", "89859621477");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment3);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        Status status3 = paymentService.getPaymentStatus(payment3);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
        assertEquals(Status.suspicious, status3);
    }
    
    @Test
    public void testOneClientCountLimitPass() {
        Payment payment1 = new Payment(19L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(1)), new BigDecimal(1500), "Payment to Megafon", "89859621478");
        Payment payment2 = new Payment(20L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(2)), new BigDecimal(1500), "Payment to Megafon", "89859621479");
        Payment payment3 = new Payment(21L, 100L, convertLocalDateTimeToDate(LocalDateTime.now().withHour(2)), new BigDecimal(900), "Payment to Megafon", "89859621477");
        paymentService.addPayToQueue(payment1);
        paymentService.addPayToQueue(payment2);
        paymentService.addPayToQueue(payment3);
        paymentService.pay();
        Status status1 = paymentService.getPaymentStatus(payment1);
        Status status2 = paymentService.getPaymentStatus(payment2);
        Status status3 = paymentService.getPaymentStatus(payment3);
        assertEquals(Status.checked, status1);
        assertEquals(Status.checked, status2);
        assertEquals(Status.checked, status3);
    }
    
}
