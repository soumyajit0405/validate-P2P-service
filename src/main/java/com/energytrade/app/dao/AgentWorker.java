package com.energytrade.app.dao;

import java.io.IOException;
import java.security.Timestamp;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.energytrade.app.util.HttpConnectorHelper;

@Component
class AgentWorker {
//	public int orderId;
//	public int meterId;
//	public String startTs;
//	public String endTs;
//	public float energy;
//	public float power;
//	public int sellerId;
	public float quarterValue;
	public long numberOfQuarters;
	public static String url = "http://139.59.30.90:3020/data_warehouse/meter/fetchDatasnapshot";
	final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs
	final long HOUR = 3600 * 1000; // in milli-seconds
	final long HALFHOUR = 1800 * 1000;

	@Autowired
	DBHelper dbhelper;
	
	@Autowired
	ScheduleDAO sdao;
	
	@Autowired
	HttpConnectorHelper httpconnectorhelper;
	
//	public AgentWorker(int orderId, int meterId, String startTs, String endTs, float energy, float power,
//			int sellerId) {
//		orderId = orderId;
//		meterId = meterId;
//		startTs = startTs;
//		endTs = endTs;
//		energy = energy;
//		power = power;
//		sellerId = sellerId;
//	}

	
	public HashMap<String,Object> validate(int orderId, int meterId, String startTs, String endTs, float energy, float power,
			int sellerId) {
		HashMap<String,Object> validationResponse = new HashMap<>();
		 float quarterValue=0;
		 long numberOfQuarters=0;
		 String url = "http://139.59.30.90:3020/data_warehouse/meter/fetchDatasnapshot";
		final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs
		final long HOUR = 3600 * 1000; // in milli-seconds
		final long HALFHOUR = 1800 * 1000;
		
		try {
		//	power = sdao.getPowerForOrder(orderId);
			System.out.println("Power From Order "+ power);
			ArrayList<Object> list = getMinutes(startTs,endTs,power);
			quarterValue = (float)list.get(0);
			numberOfQuarters = (long)list.get(1);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<HashMap<String, Object>> overLappedTrades = null;
		ArrayList<HashMap<String, Object>> overLappedContracts = null;
		try {
			
			overLappedTrades = sdao.getOlpSell(orderId, startTs, endTs, sellerId);
			overLappedContracts = sdao.getOlpContracts(orderId, startTs, endTs, sellerId);
			// overLappedContracts = new ArrayList<>();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		float sellerStartReading = 0, sellerEndReading = 0, buyerStartReading = 0, buyerEndReading = 0;
		JSONObject inputDetails1 = new JSONObject();
		JSONObject response = null;
		int continueStatus = 1;
		
	//	System.out.println(Thread.currentThread().getName() + " ----> Inside Agent Worker");
		// processmessage();//call processmessage method that sleeps the thread for 2
		// seconds
		try {
			HashMap<String, Object> order = sdao.getBuyerDeviceId(orderId);

			float unit = (float) order.get("units");
			float price = (float) order.get("price");
			if (overLappedTrades.size() == 0 && overLappedContracts.size() == 0) {

				ArrayList<String> dateRanges = getStartAndEndDates(startTs,endTs);
				JSONObject requestObject = new JSONObject();
				requestObject.put("meterId", meterId);
				requestObject.put("timeslot", dateRanges.get(1));
				requestObject.put("queryDate", dateRanges.get(0));
				response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
				if (response == null || response.has("msg") || response.has("errormessage")) {
					sdao.updateSellOrder(orderId, 8);
					continueStatus = 0;
				} else {
					JSONObject data = (JSONObject) response.get("dataSanpshot");
					if (!data.get("Active_E_Total_kWh").equals(null)) {
						sellerStartReading = (int) data.get("Active_E_Total_kWh");
					} else {
						sellerStartReading = 0;
					}

				}
				System.out.println("continueStatus1" + continueStatus);
				requestObject = new JSONObject();
				requestObject.put("meterId", meterId);
				requestObject.put("timeslot", dateRanges.get(2));
				requestObject.put("queryDate", dateRanges.get(0));
			//	if (continueStatus != 0) {
					response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
					if (response == null || response.has("msg") || response.has("errormessage")) {
						sdao.updateSellOrder(orderId, 8);
						continueStatus = 0;
					} else {
						JSONObject data = (JSONObject) response.get("dataSanpshot");
						if (!data.get("Active_E_Total_kWh").equals(null)) {
							sellerEndReading = (int) data.get("Active_E_Total_kWh");
						} else {
							sellerEndReading = 0;
						}

					}
					System.out.println("continueStatus2" + continueStatus);
					requestObject = new JSONObject();
					requestObject.put("meterId", (int) order.get("meterId"));
					requestObject.put("timeslot", dateRanges.get(1));
					requestObject.put("queryDate", dateRanges.get(0));
					//if (continueStatus != 0) {
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerStartReading = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerStartReading = 0;
							}
							// buyerStartReading = (int) data.get("cumulativeEnergy-kWh(I)");

						}

					//}
					System.out.println("continueStatus3" + continueStatus);
					requestObject = new JSONObject();
					requestObject.put("meterId", (int) order.get("meterId"));
					requestObject.put("timeslot", dateRanges.get(2));
					requestObject.put("queryDate", dateRanges.get(0));
					//if (continueStatus != 0) {
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerEndReading = (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerEndReading = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerEndReading = 0;
							}
						}

					//}
					System.out.println("continueStatus4" + continueStatus);
					//if (continueStatus != 0) {
						float p_consumed = buyerEndReading - buyerStartReading;
						float p_produced = sellerEndReading - sellerStartReading;
						float s_fine = 0, b_fine = 0;
						if (p_produced < energy) {
							s_fine = (energy - p_produced) * (price + 2.5f);
						} else if (p_produced > energy) {
							s_fine = (p_produced - energy) * (2.5f);
						}
//			}else if (p_consumed == energy ) {
//				s_fine = (p_produced - energy) *(2.5f);
//			}
						if (p_consumed > energy) {
							b_fine = (p_consumed - energy) * (2.5f);
						}
						System.out.println("  p_produced " + p_produced);
						System.out.println("  p_consumed " + p_consumed);
						System.out.println("  b_fine " + b_fine);
						System.out.println("  s_fine " + s_fine);
						dbhelper.updateOrderAmount(p_produced, s_fine, p_consumed, b_fine,orderId);
						System.out.println("  Status completed   ");
						sdao.updateOrderStatus(orderId);
						System.out.println("continueStatus5" + continueStatus);
					//}
				//}
				System.out.println("  -----------Completed the validation----------   ");
				validationResponse.put("buyerStartReading", buyerStartReading);
				validationResponse.put("buyerEndReading",buyerEndReading );
				validationResponse.put("sellerStartReading", sellerStartReading);
				validationResponse.put("sellerEndReading", sellerEndReading);
				validationResponse.put("buyerFine", b_fine);
				validationResponse.put("sellerFine", s_fine);
			} else if (overLappedTrades.size() == 0 && overLappedContracts.size() > 0) {

				ArrayList<Object> listOfDates = null;
				float buyerReading = 0;
				for (int i = 0; i < numberOfQuarters; i++) {
					JSONObject requestObject = new JSONObject();
					if (i == 0) {
						listOfDates = getSellerDates(startTs, i, startTs,endTs);
					} else {
						listOfDates = getSellerDates((String) listOfDates.get(4), i,startTs,endTs);
					}

					float powers = compareSellerTime(overLappedContracts, (Date) listOfDates.get(2),
							(Date) listOfDates.get(3));
					int buyerReading1 = 0;
					int buyerReading2 = 0;
					if (powers == 0) {

						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading1 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading1 = 0;
							}
							// buyerReading1 = buyerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");

						}

						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(5));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading2 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading2 = 0;
							}

							// buyerReading2 = buyerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");

						}
						buyerReading = buyerReading2 - buyerReading1;

					} else {

						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerReading1 = buyerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading1 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading1 = 0;
							}

						}
						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerReading2 = buyerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading2 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading2 = 0;
							}

						}

						buyerReading = buyerReading
								+ (buyerReading2 - buyerReading1) * quarterValue / (quarterValue + powers);
					}

				}

				ArrayList<String> dateRanges = getStartAndEndDates(startTs,endTs);
				JSONObject requestObject = new JSONObject();
				requestObject.put("meterId", meterId);
				requestObject.put("timeslot", dateRanges.get(1));
				requestObject.put("queryDate", dateRanges.get(0));
				response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
				if (response == null || response.has("msg") || response.has("errormessage")) {
					sdao.updateSellOrder(orderId, 8);
					continueStatus = 0;
				} else {
					JSONObject data = (JSONObject) response.get("dataSanpshot");
					//sellerStartReading = (int) data.get("cumulativeEnergy-kWh(I)");
					if (!data.get("Active_E_Total_kWh").equals(null)) {
						sellerStartReading = (int) data.get("Active_E_Total_kWh");
					} else {
						sellerStartReading = 0;
					}

				}
				requestObject = new JSONObject();
				requestObject.put("meterId", meterId);
				requestObject.put("timeslot", dateRanges.get(5));
				requestObject.put("queryDate", dateRanges.get(0));
				//if (continueStatus != 0) {
					response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
					if (response == null || response.has("msg") || response.has("errormessage")) {
						sdao.updateSellOrder(orderId, 8);
						continueStatus = 0;
					} else {
						JSONObject data = (JSONObject) response.get("dataSanpshot");
						//sellerEndReading = (int) data.get("cumulativeEnergy-kWh(I)");
						if (!data.get("Active_E_Total_kWh").equals(null)) {
							sellerEndReading = (int) data.get("Active_E_Total_kWh");
						} else {
							sellerEndReading = 0;
						}

					}
				//}

				float p_consumed = buyerReading;
				float p_produced = sellerEndReading - sellerStartReading;
				float s_fine = 0, b_fine = 0;
				if (p_produced < energy) {
					s_fine = (energy - p_produced) * (price + 2.5f);
				} else if (p_produced > energy) {
					s_fine = (p_produced - energy) * (2.5f);
				}
//					}else if (p_consumed == energy ) {
//						s_fine = (p_produced - energy) *(2.5f);
//					}
				if (p_consumed > energy) {
					b_fine = (p_consumed - energy) * (2.5f);
				}
				System.out.println("  p_produced " + p_produced);
				System.out.println("  p_consumed " + p_consumed);
				System.out.println("  b_fine " + b_fine);
				System.out.println("  s_fine " + s_fine);
				dbhelper.updateOrderAmount(p_produced, s_fine, p_consumed, b_fine, orderId);
				System.out.println("  Status completed   ");
				sdao.updateOrderStatus(orderId);
				System.out.println("continueStatus5" + continueStatus);
				validationResponse.put("buyerStartReading", buyerStartReading);
				validationResponse.put("buyerEndReading",buyerEndReading );
				validationResponse.put("sellerStartReading", sellerStartReading);
				validationResponse.put("sellerEndReading", sellerEndReading);
				validationResponse.put("buyerFine", b_fine);
				validationResponse.put("sellerFine", s_fine);
			} else if (overLappedContracts.size() == 0 && overLappedTrades.size() > 0) {
				ArrayList<Object> listOfDates = null;
				float sellerReading = 0;

				for (int i = 0; i < numberOfQuarters; i++) {
					float sellerReading1 = 0, sellerReading2 = 0;
					JSONObject requestObject = new JSONObject();
					if (i == 0) {
						listOfDates = getSellerDates(startTs, i,startTs,endTs);
					} else {
						listOfDates = getSellerDates((String) listOfDates.get(4), i,startTs,endTs);
					}

					float powers = compareSellerTime(overLappedTrades, (Date) listOfDates.get(2),
							(Date) listOfDates.get(3));
					if (powers == 0) {

						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId, 8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading1 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading1 = 0;
							}
							// sellerReading1 = sellerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");

						}

						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(5));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId, 8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading2 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading2 = 0;
							}
							// sellerReading2 = sellerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");

						}
						sellerReading = sellerReading + (sellerReading2 - sellerReading1);
					} else {

						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId, 8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// sellerReading1 = sellerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading1 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading1 = 0;
							}

						}
						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(5));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId, 8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading2 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading2 = 0;
							}
							// sellerReading2 = sellerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");

						}
						sellerReading = sellerReading
								+ (sellerReading2 - sellerReading1) * quarterValue / (quarterValue + powers);
					}
				}
				ArrayList<String> dateRanges = getStartAndEndDates(startTs,endTs);
				JSONObject requestObject = new JSONObject();
				requestObject.put("meterId", (int) order.get("meterId"));
				requestObject.put("timeslot", dateRanges.get(1));
				requestObject.put("queryDate", dateRanges.get(0));
				//if (continueStatus != 0) {
					response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
					if (response == null || response.has("msg") || response.has("errormessage")) {
						sdao.updateContract(orderId, 9);
						continueStatus = 0;
					} else {
						JSONObject data = (JSONObject) response.get("dataSanpshot");
						// buyerStartReading = (int) data.get("cumulativeEnergy-kWh(I)");
						System.out.println(data.get("Active_I_Total_kWh"));
						System.out.println(data.get("Active_I_Total_kWh") instanceof String);
						if (((Object) data.get("Active_I_Total_kWh")).equals(null)) {
							buyerStartReading = 0;
						} else {
							buyerStartReading = (int) data.get("Active_I_Total_kWh");
						}

					}

				//}
				System.out.println("continueStatus3" + continueStatus);
				requestObject = new JSONObject();
				requestObject.put("meterId", (int) order.get("meterId"));
				requestObject.put("timeslot", dateRanges.get(2));
				requestObject.put("queryDate", dateRanges.get(0));
				//if (continueStatus != 0) {
					response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
					if (response == null || response.has("msg") || response.has("errormessage")) {
						sdao.updateContract(orderId, 9);
						continueStatus = 0;
					} else {
						JSONObject data = (JSONObject) response.get("dataSanpshot");
						// buyerEndReading = (int) data.get("cumulativeEnergy-kWh(I)");
						if (!data.get("Active_I_Total_kWh").equals(null)) {
							buyerEndReading = (int) data.get("Active_I_Total_kWh");
						} else {
							buyerEndReading = 0;
						}

					}

					float p_consumed = buyerEndReading - buyerStartReading;
					float p_produced = sellerReading;
					float s_fine = 0, b_fine = 0;
					if (p_produced < energy) {
						s_fine = (energy - p_produced) * (price + 2.5f);
					} else if (p_produced > energy) {
						s_fine = (p_produced - energy) * (2.5f);
					}
//					}else if (p_consumed == energy ) {
//						s_fine = (p_produced - energy) *(2.5f);
//					}
					if (p_consumed > energy) {
						b_fine = (p_consumed - energy) * (2.5f);
					}
					System.out.println("  p_produced " + p_produced);
					System.out.println("  p_consumed " + p_consumed);
					System.out.println("  b_fine " + b_fine);
					System.out.println("  s_fine " + s_fine);
					dbhelper.updateOrderAmount(p_produced, s_fine, p_consumed, b_fine, orderId);
					System.out.println("  Status completed   ");
					sdao.updateOrderStatus(orderId);
					System.out.println("continueStatus5" + continueStatus);
					validationResponse.put("buyerStartReading", buyerStartReading);
					validationResponse.put("buyerEndReading",buyerEndReading );
					validationResponse.put("sellerStartReading", sellerStartReading);
					validationResponse.put("sellerEndReading", sellerEndReading);
					validationResponse.put("buyerFine", b_fine);
					validationResponse.put("sellerFine", s_fine);
				//}
			} else {
				ArrayList<Object> listOfDates = null;
				float buyerReading = 0;
				for (int i = 0; i < numberOfQuarters; i++) {

					float buyerReading1 = 0, buyerReading2 = 0;
					JSONObject requestObject = new JSONObject();
					if (i == 0) {
						listOfDates = getSellerDates(startTs, i,startTs,endTs);
					} else {
						listOfDates = getSellerDates((String) listOfDates.get(4), i,startTs,endTs);
					}

					float powers = compareSellerTime(overLappedContracts, (Date) listOfDates.get(2),
							(Date) listOfDates.get(3));
					if (powers == 0) {

						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerReading1 = buyerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading1 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading1 = 0;
							}

						}

						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(5));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerReading2 = buyerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading2 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading2 = 0;
							}

						}

						buyerReading = buyerReading + (buyerReading2 - buyerReading1);
					} else {

						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerReading1 = buyerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading1 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading1 = 0;
							}

						}
						requestObject.put("meterId", (int) order.get("meterId"));
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateContract(orderId, 9);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							// buyerReading2 = buyerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");
							if (!data.get("Active_I_Total_kWh").equals(null)) {
								buyerReading2 = (int) data.get("Active_I_Total_kWh");
							} else {
								buyerReading2 = 0;
							}

						}

						buyerReading = buyerReading
								+ (buyerReading2 - buyerReading1) * quarterValue / (quarterValue + powers);

					}
				}
				float sellerReading = 0;
				for (int i = 0; i < numberOfQuarters; i++) {
					float sellerReading1 = 0, sellerReading2 = 0;
					JSONObject requestObject = new JSONObject();
					if (i == 0) {
						listOfDates = getSellerDates(startTs, i,startTs,endTs);
					} else {
						listOfDates = getSellerDates((String) listOfDates.get(4), i,startTs,endTs);
					}

					float powers = compareSellerTime(overLappedTrades, (Date) listOfDates.get(2),
							(Date) listOfDates.get(3));
					if (powers == 0) {

						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId,  8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading1 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading1 = 0;
							}
							// sellerReading1 = sellerReading1 + (int) data.get("cumulativeEnergy-kWh(I)");

						}
						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(5));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId,  8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading2 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading2 = 0;
							}
							// sellerReading2 = sellerReading2 + (int) data.get("cumulativeEnergy-kWh(I)");

						}
						sellerReading = sellerReading + (sellerReading2 - sellerReading1);
					} else {

						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(1));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId,  8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading1 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading1 = 0;
							}
//							sellerReading1 = sellerReading1
//									+ (int) data.get("cumulativeEnergy-kWh(I)") ;

						}

						requestObject.put("meterId", meterId);
						requestObject.put("timeslot", listOfDates.get(5));
						requestObject.put("queryDate", listOfDates.get(0));
						response = httpconnectorhelper.sendRequestToAgent(url, requestObject, 1);
						if (response == null || response.has("msg") || response.has("errormessage")) {
							sdao.updateSellOrder(orderId,  8);
							continueStatus = 0;
						} else {
							JSONObject data = (JSONObject) response.get("dataSanpshot");
//							sellerReading2 = sellerReading2
//									+ (int) data.get("cumulativeEnergy-kWh(I)") ;
							if (!data.get("Active_E_Total_kWh").equals(null)) {
								sellerReading2 = (int) data.get("Active_E_Total_kWh");
							} else {
								sellerReading2 = 0;
							}

						}

						sellerReading = sellerReading
								+ (sellerReading2 - sellerReading1) * quarterValue / (quarterValue + powers);

					}
				}

				float p_consumed = buyerReading;
				float p_produced = sellerReading;
				float s_fine = 0, b_fine = 0;
				if (p_produced < energy) {
					s_fine = (energy - p_produced) * (price + 2.5f);
				} else if (p_produced > energy) {
					s_fine = (p_produced - energy) * (2.5f);
				}
//			}else if (p_consumed == energy ) {
//				s_fine = (p_produced - energy) *(2.5f);
//			}
				if (p_consumed > energy) {
					b_fine = (p_consumed - energy) * (2.5f);
				}
				System.out.println("  p_produced " + p_produced);
				System.out.println("  p_consumed " + p_consumed);
				System.out.println("  b_fine " + b_fine);
				System.out.println("  s_fine " + s_fine);
				dbhelper.updateOrderAmount(p_produced, s_fine, p_consumed, b_fine, orderId);
				System.out.println("  Status completed   ");
				sdao.updateOrderStatus(orderId);
				System.out.println("continueStatus5" + continueStatus);
				validationResponse.put("buyerStartReading", buyerStartReading);
				validationResponse.put("buyerEndReading",buyerEndReading );
				validationResponse.put("sellerStartReading", sellerStartReading);
				validationResponse.put("sellerEndReading", sellerEndReading);
				validationResponse.put("buyerFine", b_fine);
				validationResponse.put("sellerFine", s_fine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + " (End)");// prints thread name
		return validationResponse;
	}

	private ArrayList<String> getStartAndEndDates(String startTs, String endTs) throws ParseException {
		ArrayList<String> dateRanges = new ArrayList<>();
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// SimpleDateFormat dt1 = new SimpleDateFormat("2018-03-06");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		Date startDate = dateTimeFormat.parse(startTs);
		String sR1 = timeFormat.format(startDate);
		Date startDateR1 = new Date(startDate.getTime() - 15 * ONE_MINUTE_IN_MILLIS);
		String sR2 = timeFormat.format(startDateR1);
		String startDateRange = sR2 + "-" + sR1;

		Date endDate = dateTimeFormat.parse(endTs);
		String eR1 = timeFormat.format(endDate);
		Date endDateR1 = new Date(endDate.getTime() - 15 * ONE_MINUTE_IN_MILLIS);
		String eR2 = timeFormat.format(endDateR1);
		String endDateRange = eR2 + "-" + eR1;
		dateRanges.add(dateFormat.format(startDate));
		dateRanges.add(startDateRange);
		dateRanges.add(endDateRange);
		return dateRanges;

	}

	private ArrayList<Object> getSellerDates(String dateTime, int index, String startTs, String endTs) throws ParseException {
		ArrayList<Object> dateRanges = new ArrayList<>();
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// SimpleDateFormat dt1 = new SimpleDateFormat("2018-03-06");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		Date startDate = dateTimeFormat.parse(dateTime);
		String sR1 = timeFormat.format(startDate);
		Date startDateR1 = new Date(startDate.getTime() - 15 * ONE_MINUTE_IN_MILLIS);
		String sR2 = timeFormat.format(startDateR1);
		String startDateRange = sR2 + "-" + sR1;

		Date endDate = dateTimeFormat.parse(endTs);
		String eR1 = timeFormat.format(endDate);
		Date endDateR1 = new Date(endDate.getTime() - 15 * ONE_MINUTE_IN_MILLIS);
		String eR2 = timeFormat.format(endDateR1);
		String endDateRange = eR2 + "-" + eR1;
		dateRanges.add(dateFormat.format(startDate));
		dateRanges.add(startDateRange);
		// dateRanges.add(endDateRange);

		// Add 15 mins
		Date dateAfterfm = new Date(startDate.getTime() + 15 * ONE_MINUTE_IN_MILLIS);
		String dateAfterfms = dateTimeFormat.format(dateAfterfm);
		dateRanges.add(startDate);
		dateRanges.add(dateAfterfm);
		dateRanges.add(dateAfterfms);

		String sR3 = timeFormat.format(dateAfterfm);

		String startDateFm = sR1 + "-" + sR3;
		dateRanges.add(startDateFm);
		return dateRanges;

	}

	private float compareSellerTime(ArrayList<HashMap<String, Object>> listOfSellerOrders, Date startTs, Date endTs)
			throws ParseException {
		Date startDate, endDate;
		float sum = 0;
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		for (HashMap<String, Object> map : listOfSellerOrders) {
			startDate = (Date) map.get("startDate");
			endDate = (Date) map.get("endDate");
			if (startDate.compareTo(startTs) <= 0 && endDate.compareTo(endTs) >= 0) {
				long duration = endDate.getTime() - startDate.getTime();
				long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
				long numberOfQuarters = diffInMinutes / 15;
				sum = sum + (float) map.get("powerToSell") / 4;
			}

		}
		return sum;

	}

	private void processmessage() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Object> getMinutes(String startTs, String endTs,float power) throws ParseException {
		ArrayList<Object> list = new ArrayList<>();
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date endDate = dateTimeFormat.parse(endTs);
		Date startDate = dateTimeFormat.parse(startTs);
		long duration = endDate.getTime() - startDate.getTime();
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		long numberOfQuarters = diffInMinutes / 15;
		
		float quarterValue = power / 4;
		list.add(quarterValue);
		list.add(numberOfQuarters);
		System.out.println(quarterValue);
		return list;
	}
}