/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.antiFraudService;

import java.util.List;
import ru.vpb.payment.model.Payment;

/**
 *
 * @author vertex21
 */
public interface Checking {
    void check(List<Payment> payments, Payment payment);
}
