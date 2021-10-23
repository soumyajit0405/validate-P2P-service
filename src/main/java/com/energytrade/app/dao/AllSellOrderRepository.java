package com.energytrade.app.dao;

import org.springframework.data.jpa.repository.Modifying;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.energytrade.app.model.AllBlockchainOrder;
import com.energytrade.app.model.AllContract;
import com.energytrade.app.model.AllSellOrder;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AllSellOrderRepository extends JpaRepository<AllSellOrder, Long>
{
    
	
	  @Query("Select a from AllSellOrder a , AllBlockchainOrder b where a.sellOrderId=b.generalOrderId and b.orderId=?1") 
	  AllSellOrder getAllSellOrdersByBC(String bcOrderId );
	  
	  @Query(nativeQuery = true,value ="select a from all_sell_orders a where transfer_start_ts <= :startTs and transfer_end_ts >=:startTs \n"
	  						+"			and transfer_end_ts <= :endTs \n"
	  						+"	 		and seller_id =:sellerId and order_status_id in (3,4,5,6)  and sell_order_id not in ( :sellOrderId) \n"
	  						+"	 		union \n"
	  						+"          select a from all_sell_orders a where transfer_start_ts <= :endTs and transfer_end_ts >=:endTs  \n"
	  						+"            and transfer_start_ts >= :startTs \n"
	  						+"	 		and seller_id =:sellerId and order_status_id in (3,4,5,6) and sell_order_id not in ( :sellOrderId) \n"
	  						+"	 		union  \\r\\n\"\r\n"
	  						+"	 		select a from all_sell_orders a where transfer_start_ts >= :startTs and transfer_end_ts <=:endTs \n"
	  						+ "	 		and seller_id =:sellerId and order_status_id in (3,4,5,6) and sell_order_id not in ( :sellOrderId) \n"
	  						+ "            union  \\r\\n\"\r\n"
	  						+"	 		select a from all_sell_orders a where transfer_start_ts <= :startTs and transfer_end_ts >=:endTs  \n"
	  						+"	 		and seller_id =:sellerId and order_status_id in (3,4,5,6) and sell_order_id not in ( :sellOrderId)")
	  List<AllSellOrder> getOverLappedSellOrders(@Param("startTs") String startTs,@Param("endTs") String endTs,@Param("sellOrderId") int sellOrderId,
			  @Param("sellerId") int sellerId);
	  
	  
	  
	  @Query(nativeQuery = true,value ="select a from all_sell_orders a where transfer_start_ts <= :startTs and transfer_end_ts >=:startTs \n"
				+"			and transfer_end_ts <= :endTs \n"
				+"	 		and seller_id =:sellerId and order_status_id in (3,4,5,6)  and sell_order_id not in ( :sellOrderId) \n"
				+"	 		union \n"
				+"          select a from all_sell_orders a where transfer_start_ts <= :endTs and transfer_end_ts >=:endTs  \n"
				+"            and transfer_start_ts >= :startTs \n"
				+"	 		and seller_id =:sellerId and order_status_id in (3,4,5,6) and sell_order_id not in ( :sellOrderId) \n"
				+"	 		union  \\r\\n\"\r\n"
				+"	 		select a from all_sell_orders a where transfer_start_ts >= :startTs and transfer_end_ts <=:endTs \n"
				+ "	 		and seller_id =:sellerId and order_status_id in (3,4,5,6) and sell_order_id not in ( :sellOrderId) \n"
				+ "            union  \\r\\n\"\r\n"
				+"	 		select a from all_sell_orders a where transfer_start_ts <= :startTs and transfer_end_ts >=:endTs  \n"
				+"	 		and seller_id =:sellerId and order_status_id in (3,4,5,6) and sell_order_id not in ( :sellOrderId)")
List<AllSellOrder> getOverLappedContracts(@Param("startTs") String startTs,@Param("endTs") String endTs,@Param("sellOrderId") int sellOrderId,
@Param("sellerId") int sellerId);
	      
	  
@Query(nativeQuery = true, value ="select ac from  all_contracts ac,all_sell_orders aso "
		+ "where ac.sell_order_id = ?1 "
		+ "  and aso.sell_order_id = ac.sell_order_id")
AllContract findDeviceForBuyer(int sellOrderId);


@Modifying
@Query(nativeQuery = true,value="update all_sell_orders set order_status_id=?1 where sell_order_id =?2")
void updateSellOrder(int status, int orderId);


@Modifying
@Query(nativeQuery = true,value="update all_contracts set contract_status_id=?1 where sell_order_id =?2")
void updateContract(int status, int orderId);
}