package uo.ri.cws.application.persistence.workorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import uo.ri.cws.application.persistence.Gateway;
import uo.ri.util.exception.PersistenceException;

public interface WorkOrderGateway extends Gateway<WorkOrderGateway.WorkOrderRecord> {
    
    // Para DeleteMechanic -------------------------
    
    boolean hasWorkOrders(String mechanicId) throws PersistenceException;
    
    
    // Para FindNotInvoicedWorkOrdersByClient ------
    
    List<InvoicingWorkOrderRecord> findNotInvoicedByClientNif(String nif) throws PersistenceException;
    
    
    // Para GenerateVouchers --------------------------
    
    boolean hasPaidWorkOrdersByClientId(String clientId) throws PersistenceException;
    
    
    public class WorkOrderRecord extends Gateway.BaseRecord {
        
        public LocalDateTime date;
        public String description;
        public BigDecimal total_amount;
        public String state;
        
        public String invoice_id;
        public String mechanic_id;
        public String vehicle_id;
        
    }
    
    public class InvoicingWorkOrderRecord {
        
        public String id;
        public String description;
        public LocalDateTime date;
        public String state;
        public BigDecimal amount;
        
    }
    
    List<EligibleWorkOrderRecord> findEligibleForVoucher() throws PersistenceException;
    
    void markAsUsedForVoucher(String id) throws PersistenceException;
    
    public class EligibleWorkOrderRecord {
        public String workorderId;
        public String clientId;
    }
    
}
