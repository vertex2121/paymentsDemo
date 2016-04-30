/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.vpb.payment.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vertex21
 */
public class Payment {
    private static final Logger LOG = Logger.getLogger(Payment.class.getName());
    
    private final Long id;
    private final Long clientId;
    private final Date date;
    private final BigDecimal amount;
    private final String service;
    private final String acount;
    private Status status = Status.initial;

    public Payment(Long id, Long clientId, BigDecimal amount, String service, String acount) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.service = service;
        this.acount = acount;
        this.date = new Date();
    }

    public Payment(Long id, Long clientId, Date date, BigDecimal amount, String service, String acount) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.service = service;
        this.acount = acount;
        this.date = date;
    }

    
    
    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getService() {
        return service;
    }

    public String getAcount() {
        return acount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        LOG.log(Level.INFO, "Payment id {0} was changed to {1}", new Object[]{id, status});
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Payment other = (Payment) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Payment{" + "id=" + id + '}';
    }

    public enum Status {
        
        initial("создан"),
        suspicious("требует подтверждения"),
        checked("готов к проведению"),
        executed("проведен");
        
        private final String label;
        
        Status (String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return this.label;
        }
    }
    
    
}
