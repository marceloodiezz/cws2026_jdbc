package uo.ri.cws.application.persistence.workorder.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uo.ri.cws.application.persistence.util.jdbc.Jdbc;
import uo.ri.cws.application.persistence.util.jdbc.Queries;
import uo.ri.cws.application.persistence.workorder.WorkOrderGateway;
import uo.ri.util.exception.NotYetImplementedException;
import uo.ri.util.exception.PersistenceException;

public class WorkOrderGatewayImpl implements WorkOrderGateway {
    
    // Para DeleteMechanic -------------------------
    
    @Override
    public boolean hasWorkOrders(String mechanicId) throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TWORKORDERS_HASWORKORDERS"))) {
            pst.setString(1, mechanicId);
            
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    
    // Para FindNotInvoicedWorkOrdersByClient ------
    
    @Override
    public List<InvoicingWorkOrderRecord> findNotInvoicedByClientNif(String nif) throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(
                Queries.getSQLSentence("TWORKORDERS_FINDNOTINVOICEDBYCLIENTNIF"))) {
            pst.setString(1, nif);
            
            try (ResultSet rs = pst.executeQuery()) {
                return new InvoicingWorkOrderRecordAssembler().toRecordList(rs);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    
    // Para GenerateVouchers -----------------------
    
    @Override
    public boolean hasPaidWorkOrdersByClientId(String clientId) throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(
                Queries.getSQLSentence("TWORKORDERS_HASPAIDWORKORDERSBYCLIENTID"))) {
            pst.setString(1, clientId);
            
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    
    // ------------------
    
    @Override
    public void add(WorkOrderRecord t) throws PersistenceException {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void remove(String id) throws PersistenceException {
        throw new NotYetImplementedException();
    }
    
    @Override
    public void update(WorkOrderRecord t) throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TWORKORDERS_UPDATE"))) {
            pst.setString(1, t.invoice_id);
            pst.setString(2, t.state);
            pst.setTimestamp(3, Timestamp.valueOf(t.updatedAt));
            pst.setString(4, t.id);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    @Override
    public Optional<WorkOrderRecord> findById(String id) throws PersistenceException {
        Connection c = Jdbc.getCurrentConnection();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TWORKORDERS_FINDBYID"))) {
            pst.setString(1, id);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    WorkOrderRecord record = new WorkOrderRecordAssembler().toRecord(rs);
                    return Optional.of(record);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
    
    @Override
    public List<WorkOrderRecord> findAll() throws PersistenceException {
        throw new NotYetImplementedException();
    }


	@Override
	public List<EligibleWorkOrderRecord> findEligibleForVoucher() throws PersistenceException {
		Connection c = Jdbc.getCurrentConnection();
    	
        List<EligibleWorkOrderRecord> eligibleWorkorders = new ArrayList<>();
        
        try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TWORKORDERS_FINDAVAILABLEFORVOUCHER"))) {
            try (ResultSet rs = pst.executeQuery()) {
                while(rs.next()) {
                	EligibleWorkOrderRecord r = new EligibleWorkOrderRecord();
                	r.clientId = rs.getString("client_id");
                	r.workorderId = rs.getString("workorder_id");
                	eligibleWorkorders.add(r);
                }
            }
            
            return eligibleWorkorders;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
	}


	@Override
	public void markAsUsedForVoucher(String id) throws PersistenceException {
		Connection c = Jdbc.getCurrentConnection();
    	
    	try (PreparedStatement pst = c.prepareStatement(Queries.getSQLSentence("TWORKORDERS_MARKASUSEDFORVOUCHER"))) {
    		pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(2, id);
            
            int rows = pst.executeUpdate();
            
            if (rows != 1) {
                throw new PersistenceException("Could not mark workorder as used for voucher: " + id);
            }
            
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
	}
    
}
