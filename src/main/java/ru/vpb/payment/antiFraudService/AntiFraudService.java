/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.antiFraudService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.vpb.payment.exception.AntiFraudException;
import ru.vpb.payment.model.Payment;

/**
 *
 * @author vertex21
 */
public class AntiFraudService {

    private static final Logger LOG = Logger.getLogger(AntiFraudService.class.getName());
    private final Map<Long, List<Payment>> clientPayments = new HashMap<>();

    public void check(Payment payment) {
        initClient(payment.getClientId());
        Checking checking = AntiFraudCheckingFactory.getChecking(payment);
        try {
            checking.check(clientPayments.get(payment.getClientId()), payment);
            LOG.log(Level.INFO, "Payment id {0} successfully check  with {1}.", new Object[]{payment.getId(), checking.getClass().getSimpleName()});
        } catch (AntiFraudException afex) {
            LOG.log(Level.INFO, "Payment id {0} check failed with {1}.", new Object[]{payment.getId(), checking.getClass().getSimpleName()});
            throw afex;
        }
        payment.setStatus(Payment.Status.checked);
        clientPayments.get(payment.getClientId()).add(payment);
    }

    private void initClient(Long client) {
        if (!clientPayments.containsKey(client)) {
            clientPayments.put(client, new ArrayList<>());
        }
    }

}

class AntiFraudCheckingFactory {

    private static final Checking SOFT_ANTI_FRAUD = new SoftAntiFraudChecking();
    private static final Checking HARD_ANTI_FRAUD = new HardAntiFraudChecking();

    public static Checking getChecking(Payment payment) {
        switch (payment.getService()) {
            case "Payment to Megafon":
                return HARD_ANTI_FRAUD;
            default:
                return SOFT_ANTI_FRAUD;
        }
    }
}
