package uo.ri.cws.application.persistence.invoice.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uo.ri.cws.application.persistence.invoice.InvoiceGateway;
import uo.ri.cws.application.persistence.util.jdbc.Jdbc;
import uo.ri.cws.application.persistence.util.jdbc.Queries;
import uo.ri.util.exception.NotYetImplementedException;
import uo.ri.util.exception.PersistenceException;

public class InvoiceGatewayImpl implements InvoiceGateway {
    
    @Override
    public long getNextInvoiceNumber() throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TINVOICES_GETNEXTINVOICENUMBER"))) {
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) + 1;
                }
            }
            
            return 1L;
            
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    @Override
    public void add(InvoiceRecord invoice) throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TINVOICES_ADD"))) {
            
            pst.setString(1, invoice.id);
            pst.setLong(2, invoice.version);
            pst.setLong(3, invoice.number);
            pst.setDate(4, Date.valueOf(invoice.date));
            pst.setString(5, invoice.state);
            pst.setBigDecimal(6, invoice.subtotal);
            pst.setBigDecimal(7, invoice.total);
            pst.setDouble(8, invoice.vatRate);
            pst.setBigDecimal(9, invoice.vatAmount);
            pst.setTimestamp(10, Timestamp.valueOf(invoice.createdAt));
            pst.setTimestamp(11, Timestamp.valueOf(invoice.updatedAt));
            pst.setString(12, invoice.entityState);
            pst.setBoolean(13, false);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    @Override
    public void remove(String id) throws PersistenceException {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void update(InvoiceRecord t) throws PersistenceException {
        throw new NotYetImplementedException();
    }
    
    @Override
    public Optional<InvoiceRecord> findById(String id) throws PersistenceException {
        throw new NotYetImplementedException();
    }
    
    @Override
    public List<InvoiceRecord> findAll() throws PersistenceException {
        throw new NotYetImplementedException();
    }
    
    @Override
    public List<EligibleInvoiceRecord> findEligibleForVoucher() throws PersistenceException {
    	Connection c = Jdbc.getCurrentConnection();
    	
        List<EligibleInvoiceRecord> eligibleInvoices = new ArrayList<>();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TINVOICES_FINDELIGIBLEFORVOUCHER"))) {
            try (ResultSet rs = pst.executeQuery()) {
                while(rs.next()) {
                	EligibleInvoiceRecord r = new EligibleInvoiceRecord();
                	r.clientId = rs.getString("client_id");
                	r.invoiceId = rs.getString("invoice_id");
                	eligibleInvoices.add(r);
                }
            }
            
            return eligibleInvoices;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    @Override
    public void markAsUsedForVoucher(String id) throws PersistenceException {
    	Connection c = Jdbc.getCurrentConnection();
    	
    	try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TINVOICES_MARKASUSEDFORVOUCHER"))) {
    		pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(2, id);
            
            int rows = pst.executeUpdate();
            
            if (rows != 1) {
                throw new PersistenceException("Could not mark invoice as used for voucher: " + id);
            }
            
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
}
