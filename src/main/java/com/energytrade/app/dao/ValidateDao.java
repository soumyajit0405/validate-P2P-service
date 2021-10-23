package com.energytrade.app.dao;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.energytrade.app.model.AllSellOrder;





@Transactional
@Repository
public class ValidateDao extends AbstractBaseDao
{
	@Autowired
    AllSellOrderRepository allSellOrderRepo;
	
	@Autowired
	AgentWorker agentWorker;
 
	public ValidateDao() {
		
	}
	
	public HashMap<String,Object> validateTrade(String blockChainOrderId) {
	
		HashMap<String,Object> response = new HashMap<>();
		try {
			SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// SimpleDateFormat dt1 = new SimpleDateFormat("2018-03-06");
			
			AllSellOrder sellOrder = allSellOrderRepo.getAllSellOrdersByBC(blockChainOrderId);
			final long HOUR = 3600 * 1000; // in milli-seconds
			final long HALFHOUR = 1800 * 1000;
			//Date startTs = new Date(sellOrder.getTransferStartTs().getTime() - 5 * HOUR - HALFHOUR); // Local Change
			//Date endTs = new Date(sellOrder.getTransferEndTs().getTime() - 5 * HOUR - HALFHOUR); // Local Change
			
			Date startTs = new Date(sellOrder.getTransferStartTs().getTime() ); // Server Deployment Change
			Date endTs = new Date(sellOrder.getTransferEndTs().getTime() );// Server Deployment Change
			
			System.out.println(sellOrder.getTransferStartTs());
			response =agentWorker.validate(sellOrder.getSellOrderId(),sellOrder.getUserDevice().getMeterId(),
					dt1.format(startTs),dt1.format(endTs),
					sellOrder.getEnergy().floatValue(),sellOrder.getPowerToSell().floatValue(),sellOrder.getAllUser().getUserId()); 
			response.put("sellStatus","Validated");
			response.put("buyStatus","Validated");
		}
		catch(Exception e) {
			e.printStackTrace();
			
		}
		return response;
	}
	
	
//	private void init(AllSellOrder sellOrder) {
//		agentWorker.orderId = sellOrder.getSellOrderId();
//		agentWorker.meterId = sellOrder.getUserDevice().getMeterId();
//		agentWorker.startTs = sellOrder.getTransferStartTs().toString();
//		agentWorker.endTs = sellOrder.getTransferEndTs().toString();
//		agentWorker.energy = sellOrder.getEnergy().floatValue();
//		agentWorker.power = sellOrder.getPowerToSell().floatValue();
//		agentWorker.sellerId = sellOrder.getAllUser().getUserId();
//	}
          
}