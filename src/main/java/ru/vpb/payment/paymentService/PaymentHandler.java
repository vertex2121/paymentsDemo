/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.paymentService;

import ru.vpb.payment.model.Payment;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vertex21
 */
public class PaymentHandler {
    private static final Logger LOG = Logger.getLogger(PaymentHandler.class.getName());
    
    public void execute(Payment payment) {
        LOG.log(Level.INFO, "Payment id {0} executed.", payment.getId());
    }
}
