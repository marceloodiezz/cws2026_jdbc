package uo.ri.cws.application.service.voucher.commands;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import uo.ri.conf.Factories;
import uo.ri.cws.application.persistence.client.ClientGateway;
import uo.ri.cws.application.persistence.client.ClientGateway.ClientRecord;
import uo.ri.cws.application.persistence.invoice.InvoiceGateway;
import uo.ri.cws.application.persistence.invoice.InvoiceGateway.EligibleInvoiceRecord;
import uo.ri.cws.application.persistence.paymentmean.PaymentMeanGateway;
import uo.ri.cws.application.persistence.paymentmean.PaymentMeanGateway.PaymentMeanRecord;
import uo.ri.cws.application.persistence.recommendation.RecommendationGateway;
import uo.ri.cws.application.persistence.recommendation.RecommendationGateway.RecommendationRecord;
import uo.ri.cws.application.persistence.util.command.Command;
import uo.ri.cws.application.persistence.voucher.VoucherGateway;
import uo.ri.cws.application.persistence.voucher.VoucherGateway.VoucherRecord;
import uo.ri.cws.application.persistence.workorder.WorkOrderGateway;
import uo.ri.cws.application.persistence.workorder.WorkOrderGateway.EligibleWorkOrderRecord;
import uo.ri.util.exception.BusinessException;

public class GenerateVouchers implements Command<int[]> {
    
    private static final int RECOMMENDATIONS_PER_VOUCHER = 3;
    private static final BigDecimal VOUCHER_AMOUNT_25 = new BigDecimal("25.00");
    private static final String VOUCHER_DESCRIPTION_25 = "Por recomendación";
    
    private static final BigDecimal VOUCHER_AMOUNT_30 = new BigDecimal("30.00");
    private static final String VOUCHER_DESCRIPTION_30 = "Por factura de más de 500";
    
    private static final int WORKORDERS_PER_VOUCHER = 3;
    private static final BigDecimal VOUCHER_AMOUNT_20 = new BigDecimal("20.00");
    private static final String VOUCHER_DESCRIPTION_20 = "Por tres órdenes de trabajo";
    
    private ClientGateway cg = Factories.persistence.forClient();
    
    private RecommendationGateway rg = Factories.persistence.forRecommendation();
    
    private WorkOrderGateway wg = Factories.persistence.forWorkOrder();
    
    private PaymentMeanGateway pmg = Factories.persistence.forPaymentMean();
    
    private VoucherGateway vg = Factories.persistence.forVoucher();
    
    private InvoiceGateway ig = Factories.persistence.forInvoice();
    
    @Override
    public int[] execute() throws BusinessException {
        int[] counters = { 0, 0, 0 };
        
        generateRecommendationVouchers(counters);
        
        generateInvoiceVouchers(counters);
        
        generateWorkOrderVouchers(counters);
        
        return counters;
    }
    
    private void generateRecommendationVouchers(int[] counters) throws BusinessException {
    	List<ClientRecord> clients = cg.findAll();
        
        for (ClientRecord client : clients) {
            
            if (!wg.hasPaidWorkOrdersByClientId(client.id)) {
                continue;
            }
            
            List<RecommendationRecord> recommendations = rg.findAvailableByRecommender(client.id);
            List<RecommendationRecord> validRecommendations = new ArrayList<>();
            
            for (RecommendationRecord recommendation : recommendations) {
                if (wg.hasPaidWorkOrdersByClientId(recommendation.recommendedId)) {
                    validRecommendations.add(recommendation);
                }
            }
            
            int numberOfVouchers = validRecommendations.size() / RECOMMENDATIONS_PER_VOUCHER;
            
            for (int i = 0; i < numberOfVouchers; i++) {
                generateVoucher(client.id, VOUCHER_AMOUNT_25, VOUCHER_DESCRIPTION_25);
                
                int start = i * RECOMMENDATIONS_PER_VOUCHER;
                
                for (int j = start; j < start + RECOMMENDATIONS_PER_VOUCHER; j++) {
                    RecommendationRecord recommendation = validRecommendations.get(j);
                    
                    recommendation.usedForVoucher = true;
                    recommendation.updatedAt = LocalDateTime.now();
                    
                    rg.update(recommendation);
                }
                
                counters[1]++;
            }
        }
    }
    
    private void generateInvoiceVouchers(int[] counters) throws BusinessException {
    	List<EligibleInvoiceRecord> invoices = ig.findEligibleForVoucher();
    	
    	for (EligibleInvoiceRecord invoice : invoices) {
    		generateVoucher(invoice.clientId, VOUCHER_AMOUNT_30, VOUCHER_DESCRIPTION_30);
    		ig.markAsUsedForVoucher(invoice.invoiceId);
    		counters[2]++;
    	}
    }
    
    private void generateWorkOrderVouchers(int[] counters) throws BusinessException {
    	List<EligibleWorkOrderRecord> workOrders = wg.findEligibleForVoucher();

        Map<String, List<EligibleWorkOrderRecord>> workOrdersByClient = new HashMap<>();
        
        for (EligibleWorkOrderRecord workOrder : workOrders) {
            workOrdersByClient.computeIfAbsent(workOrder.clientId, id -> new ArrayList<>()).add(workOrder);
        }
        
        for (List<EligibleWorkOrderRecord> clientWorkOrders : workOrdersByClient.values()) {
            int numberOfVouchers = clientWorkOrders.size() / WORKORDERS_PER_VOUCHER;

            for (int i = 0; i < numberOfVouchers; i++) {
                String clientId = clientWorkOrders.get(0).clientId;
                generateVoucher(clientId, VOUCHER_AMOUNT_20, VOUCHER_DESCRIPTION_20);
                
                int start = i * WORKORDERS_PER_VOUCHER;
                for (int j = start; j < start + WORKORDERS_PER_VOUCHER; j++) {
                    EligibleWorkOrderRecord workOrder = clientWorkOrders.get(j);
                    wg.markAsUsedForVoucher(workOrder.workorderId);
                }

                counters[0]++;
            }
        }
    }
    
    private void generateVoucher(String clientId, BigDecimal amount, String description) {
        PaymentMeanRecord pm = new PaymentMeanRecord();

        pm.dtype = "Voucher";
        pm.clientId = clientId;
        pm.accumulated = BigDecimal.ZERO;
        
        VoucherRecord voucher = new VoucherRecord();
        
        voucher.id = pm.id;
        voucher.code = UUID.randomUUID().toString();
        voucher.description = description;
        voucher.available = amount;
        
        pmg.add(pm);
        vg.add(voucher);
    }
    
}
