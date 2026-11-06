package uo.ri.cws.application.persistence.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import uo.ri.cws.application.persistence.Gateway;
import uo.ri.util.exception.PersistenceException;

public interface InvoiceGateway extends Gateway<InvoiceGateway.InvoiceRecord> {
    
    long getNextInvoiceNumber() throws PersistenceException;
    
    public class InvoiceRecord extends Gateway.BaseRecord {
        
        public long number;
        public LocalDate date;
        public String state;
        
        public BigDecimal subtotal;
        public BigDecimal total;
        
        public double vatRate;
        public BigDecimal vatAmount;
        
    }
    
    List<EligibleInvoiceRecord> findEligibleForVoucher() throws PersistenceException;
    
    void markAsUsedForVoucher(String id) throws PersistenceException;
    
    public class EligibleInvoiceRecord {
        public String invoiceId;
        public String clientId;
    }
    
}
