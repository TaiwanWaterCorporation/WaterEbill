package twcebillsysbatch.type;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jodd.typeconverter.IntegerConverter;
import jodd.util.StringUtil;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class GenNew111NoticePDFBatchType extends BatchType {
private static final Logger LOG = LoggerFactory.getLogger(BatchTypeEnum.GEN_NOTICE_PDF.getTypeCode());
	
	private static final String BATCH_EXCEPTION_MAIL_TEMPLATE_NAME = "sendnoticemail";
	/** pdf內容為帳單明細(預繳與持單自繳時使用) */
	private static final String BILL_TYPE_BILL_DETAIL = WaterConstant.BillType.BILL.code();
	/** pdf內容類型為通知(扣繳時使用) */
	private static final String BILL_TYPE_NOTICE = WaterConstant.BillType.NOTOICE.code();
	/** pdf產生記錄代表"帳單" */
	private static final String TYPE_BILL = PdfType.BILL.code();

	EbillService ebillService = SpringTool.getBean(EbillService.class);	
	
	@Override
	public String getTypeCode() {
		return BatchTypeEnum.GEN_NEW105_NOTICE_PDF.getTypeCode();
	}

	@Override
	public void work(HashMap<String, String> ops) {
		this.ebillService.init(DBDataTool.getInstance(), this.dbUtils);
		generateNoticePDF(ops);
	}

	private void generateNoticePDF(HashMap<String, String> ops) {
		//Johnson DBUtils直接從parent BatchType的建構子就給了，所有排程實作BatchType類別不用再自己new
		initialPreparation(ops);
		
		LOG.info("●●●產生帳單pdf啟動..");
		// 定義除錯用記錄指標
		int cnt = 0;
		int has_water_no = 0;
		int pdf_not_exists = 0;
		int cnt_water = 0;
		StringBuffer send_result = new StringBuffer();

		boolean skipMgrNotice = BooleanUtils.toBoolean(ops.get("skipMgrNotice"));//是否省略寄送通知信給管理員
		boolean skipDbLogging = BooleanUtils.toBoolean(ops.get("skipDbLogging")); // For UnitTest 是否省略寫進資料庫
		String reGenBarCode = StringUtil.toNotNullString(ops.get("reGenBarCode"));
		String reGeneBill = StringUtil.toNotNullString(ops.get("reGeneBill"));
		String ops_member_guid = StringUtil.toNotNullString(ops.get("member_guid")); // 指定要產的用戶
		
		String whereSql = "";
		String station = StringUtil.toNotNullString(ops.get("station"));
		if (station.length() > 0) {
			whereSql = whereSql + " and a.station like '" + station + "%'";
			whereSql = whereSql + " and a.waterno not in (select old_waterno from water_e.renamed_waterno(nolock) where old_waterno like '" + station + "%') ";
			
			LOG.info("本次針對 {} 區處理", station);
		}
		
		// 測試用，正常不會傳這些參數
		final String test_waterno = StringUtil.toNotNullString(ops.get("test_waterno"));
		final String test_password = StringUtil.toNotNullString(ops.get("test_password"));
		boolean test_notComWaternos = BooleanUtils.toBoolean(ops.get("notComWaternos")); //for notComWaternos 產製某區間水號
		boolean test_notComMemGuid = BooleanUtils.toBoolean(ops.get("notComMemGuid")); //for notComWaternos 產製某區間水號
		
		if (test_waterno.length() > 0) {
			whereSql = whereSql + " and a.waterno = '" + test_waterno + "'";
			LOG.warn("<<<<測試>>>>本次針對 {} 水號處理", test_waterno);
		}
		
        if(StringUtils.isNotEmpty(ops_member_guid)) {
        	whereSql = whereSql + " AND member_guid = '" + ops_member_guid + "'";
       }

		DateTime now = new DateTime();
		int rocYearNow = now.getYear() - 1911;
		
		String rocYearArgument = StringUtil.isNotEmpty(ops.get("yyy")) ? 
									StringUtil.toNotNullString(ops.get("yyy")) : 
										String.valueOf(rocYearNow);
		String rocMonthArgument = StringUtil.isNotEmpty(ops.get("mm")) ?
									StringUtil.toNotNullString(ops.get("mm")) :
										DateTimeFormat.forPattern("MM").print(now);
		int opsYearValue = Integer.valueOf(rocYearArgument) + 1911;
		int opsMonthValue = Integer.valueOf(rocMonthArgument);
		String adYM = new DateTime().withYear(opsYearValue).withMonthOfYear(opsMonthValue).toString("YYYYMM");
		String billRocYyymm = rocYearArgument + rocMonthArgument;
		
		if (IntegerConverter.valueOf(dbUtils.getsqlFirstValue(
				"SELECT COUNT(1) AS cnt FROM all_tables WHERE table_name= 'T" + billRocYyymm + "'",
				null,
				PropertiesTWCEBill.getWaterFeeConn())) == 0) {
			LOG.info("表格 T{} 不存在", billRocYyymm);
			
			if(!skipMgrNotice) {
				notifyBillTyyymmNotExist(rocYearArgument, rocMonthArgument);
			}

		} else {
			LOG.info("表格 T{} 存在", billRocYyymm); // 2011-04-01補充log

			StringBuffer errorMessages = new StringBuffer("\n<br/>");
			int errorCount = 0; // 帳單pdf(扣繳電子通知)產生失敗counter
			String selectEbillsWaternumSQL ="";
			if (!test_notComWaternos){
				selectEbillsWaternumSQL = "select waterno,member_guid" +
					" from water_e.EBILLS_WATERNUM a(NOLOCK)" +
					" where 1=1 "
						+ whereSql + 
					" order by member_guid desc ";
			}else if (test_notComMemGuid){
				selectEbillsWaternumSQL = "select a.waterno,a.member_guid" +
						" from water_e.EBILLS_WATERNUM a(NOLOCK)" +
						" join water_e.notComMemGuids b(NOLOCK) on a.member_guid=b.memguid" +
						" where 1=1 "
							+ whereSql + 
						" order by a.waterno desc ";
			}else{
				selectEbillsWaternumSQL = "select a.waterno,a.member_guid" +
					" from water_e.EBILLS_WATERNUM a(NOLOCK)" +
					" join water_e.notComWaternos b(NOLOCK) on a.waterno=b.waterno" +
					" where 1=1 "
						+ whereSql + 
					" order by a.waterno desc ";
			}
			
			Collection<Map<String, Object>> waternoAndMemberguid = dbUtils.getsqlValues(
					selectEbillsWaternumSQL, null, PropertiesTWCEBill.getWaterEConn());
			
			int dataSize = waternoAndMemberguid.size();
			LOG.debug("共撈出 {} 個水號訂閱資料, 會員水號查詢條件: {}", dataSize, selectEbillsWaternumSQL); // 2011-04-01補充log

			//Johnson 先建立好pdf存放資料夾
			File noticePdfDir = prepareDir(PropertiesTWCEBill.getNoticePdfPath(), adYM);
			
			//建立存放html 及 barcode圖檔目錄
			CommonTool.checkDirExistAndCanWrite(new File(noticePdfDir + File.separator +"html"));
			CommonTool.checkDirExistAndCanWrite(new File(noticePdfDir + File.separator +"html"+ File.separator +"barcode"));
			
			int processedDataCnt = 0;
			long forStarts = new Date().getTime();
			for (Map<String, Object> ewnRow : waternoAndMemberguid) {
				String ewnWaterno = StringUtil.toNotNullString(ewnRow.get("waterno"));
				String ewnMember_guid = StringUtil.toNotNullString(ewnRow .get("member_guid"));
				LOG.debug("開始處理會員: {}, 原水號:{}", ewnMember_guid, ewnWaterno); // 2011-04-01

				selectEbillsWaternumSQL = 
                		"SELECT rtrim(ltrim(id)) as id, rtrim(ltrim(name)) as name, rtrim(ltrim(sex)) as sex," +
                				" rtrim(ltrim(email)) as email, rtrim(ltrim(mID)) as mID, rowguid, issecmail, mailcheck," +
                				" oldMemberDate, system_create,mtype" +
                		" FROM dbo.member" +
                		" WHERE rowguid = '" + ewnMember_guid + "' ";

                Map<String, Object> member = dbUtils.getsqlOneRowValues(selectEbillsWaternumSQL, null, PropertiesTWCEBill.getWaterConn(), 1);
                
                String mailcheck = StringUtil.toNotNullString(member.get("mailcheck"));
                String oldMemberDate = StringUtil.toNotNullString(member.get("oldMemberDate"));
                if (!"Y".equals(mailcheck) && StringUtil.isEmpty(oldMemberDate)) { 
                	// ******** Condition 2. The member has to complete confirmation. *********
                	// 2011-04-01 會員email確認狀態改用程式判斷以方便加log
                    LOG.info("會員未確認且不是舊會員，不寄送電子帳單: " + ewnMember_guid); // 2011-04-01 補充log
                    continue;
                }

                

				String pdfPath2 = noticePdfDir + File.separator + ewnMember_guid + "." + "("
				+ billRocYyymm + ")" + ewnWaterno + ".pdf";
                
				String pdfPath = noticePdfDir + File.separator + ewnWaterno + "("
						+ billRocYyymm + ")" + "." +  ewnMember_guid + ".pdf";
				
				boolean pdfAlreadyExist = false;
				
				
				if(Integer.parseInt(adYM) >= 202112 ){
					if((new File(pdfPath)).exists()){
						pdfAlreadyExist = true;
	            	}
					if (pdfAlreadyExist){
						if ((new File(pdfPath)).length() < 1) {
		                    //檔案大小為0則刪除
							(new File(pdfPath)).delete();
							pdfAlreadyExist = false;
		                }
					}	
				}else{
					if((new File(pdfPath2)).exists()){
	            		pdfAlreadyExist = true;
	            	}
					
					if (pdfAlreadyExist){
						if ((new File(pdfPath)).length() < 1) {
		                    //檔案大小為0則刪除
							(new File(pdfPath)).delete();
							pdfAlreadyExist = false;
		                }
					}
					
				}
				
				
				
				
				LOG.info("PDF是否存在: {}, TYYYMM水號: {}, filepath: {}", pdfAlreadyExist, ewnWaterno, pdfPath); // 2011-04-01
																			// 補充log
				// 記錄除錯用記錄指標
				if (ewnWaterno.length() > 0) {
					++has_water_no;
				}
				if (!pdfAlreadyExist) {
					++pdf_not_exists;
				}
				
				
				String monthly = "";
				if (Integer.parseInt(rocMonthArgument) % 2 == 0) {
					monthly = "bimonthly";
				} else {
					monthly = "monthly";
				}

				if (!skipDbLogging) {
					// 在water_e.EBILLS_WATERNUM記錄當月無資料
					if (ewnWaterno.length() < 1) {
						HashMap<String, Object> pars = new HashMap<String, Object>();
						pars.clear();
						pars.put("waterno", ewnWaterno);
						pars.put("member_guid", ewnMember_guid);
						dbUtils.runUpdateSql(
								"update water_e.EBILLS_WATERNUM set "
										+ monthly
										+ " = '"
										+ rocMonthArgument
										+ "月無資料' where waterno=:waterno and member_guid=:member_guid",
								pars, PropertiesTWCEBill.getWaterEConn());
					}
				} else {
					LOG.warn("For JUnit Test 省略寫進資料庫: 在water_e.EBILLS_WATERNUM記錄當月無資料.");
				}
				// #endregion - 寫進water_e.EBILLS_WATERNUM

				
				// #region - Prepare Tyyymm data.
				//Johnson 只有用到Tyyymm四個欄位,但BatchWork.getTYYMMTableValue撈出幾乎所有欄位...
				Map<String, Object> tyyymmData = dataTool.queryTyyymmByWaterNO(ewnWaterno, billRocYyymm, null, 
						"waterno", "ac1", "prefee", "name","ALLOW2STA");
				String ac1 = StringUtil.toNotNullString(tyyymmData.get("AC1")).trim();//行庫代碼
				String prefee = StringUtil.toNotNullString(tyyymmData.get("PREFEE")).trim();//本期預繳餘額
				String watername = StringUtil.toNotNullString(tyyymmData.get("NAME")).trim(); // 用水名稱
				String allow2sta = StringUtil.toNotNullString(tyyymmData.get("ALLOW2STA")).trim(); // 電子帳單回饋金
				// #endregion - Prepare Tyyymm data.
				
				
				// 有當期水號資料才產生
				//Johnson 從上面走下來沒有水號早就出錯了，所以水號條件去除
				//if (!pdfAlreadyExist) { //應該waterfee沒值就不要產才對.
				if (!pdfAlreadyExist && !watername.equals("")) {
					LOG.info("準備產生PDF: {}", ewnWaterno); // 2011-04-01 補充log
					++cnt_water;
					
					// 帳單種類
					//Johnson 預設為僅通知(扣繳)
					String billtype = BILL_TYPE_NOTICE;
					
					// 附件類型
					if (prefee.length() > 0 && allow2sta.equals("電子帳單回饋金")) {
						if (IntegerConverter.valueOf(prefee.trim()) > 0) {	//Johnson 預繳餘額還有多的
							// 預繳足額(電子帳單)
							billtype = BILL_TYPE_BILL_DETAIL;
						} else {
							if (ac1.equals("") || ac1.equals("99")
									|| ac1.equals("9")) {	//Johnson 沒有行庫代號(ac1)的
								if (DBDataTool.getInstance().hasDobuleMemberforEbill(ewnWaterno)){
									String lastmemgrid=DBDataTool.getInstance().getLastMemberforEbillGridID(ewnWaterno);
									if (ewnMember_guid.endsWith(lastmemgrid)){
										billtype = BILL_TYPE_BILL_DETAIL;
									}else{
										billtype = BILL_TYPE_NOTICE;   
									}
								}else{
									billtype = BILL_TYPE_BILL_DETAIL;
								}
							} else {
								billtype = BILL_TYPE_NOTICE;
							}
						}
					}
					Map<String, Object> memberRow = dataTool.getMember(ewnMember_guid);
					
					if (billtype.equals(BILL_TYPE_NOTICE)) { // 產電子通知pdf
						LOG.info("準備產生電子通知pdf, 水號({})", ewnWaterno);
						try {
							PdfDataFactory pdfDataFactory = PdfDataFactory.getInstance( PdfDataTypeEnum.BANK_DEBIT.getTypeString() ).init(dbUtils);

							PdfData pdfData = pdfDataFactory.prepareData(ewnWaterno, adYM,"");
							if (reGeneBill.equals("Y")){
								pdfData.setReGenFlag("  ");
							}else{
								pdfData.setReGenFlag(" ");
							}
							File unsignedPdfFile = new File(noticePdfDir + File.separator + ewnMember_guid + "." + "("
									+ billRocYyymm + ")" + ewnWaterno + ".pdf");
							
							File unsignedPdfFile2 = new File(noticePdfDir + File.separator + ewnWaterno + "("
									+ billRocYyymm + ")" + "." +  ewnMember_guid + ".pdf");
							
							//20211208 新增11月之前就檔名 12月之後新檔名
							if(Integer.parseInt(adYM) >= 202112 ){
								unsignedPdfFile=unsignedPdfFile2;
							}
							
							
							PdfReport pdfReport = new PdfReportOnBankDebitsNoticeNew111();
							
							pdfReport.output(pdfData, new FileOutputStream(unsignedPdfFile));
							
							
							LOG.info("電子通知pdf({})產生成功", ewnWaterno);
							
							if (reGeneBill.equals("Y")){
								ReSendeBillLog resendebilllog = new ReSendeBillLog();
								resendebilllog.setMemguid(ewnMember_guid);
								resendebilllog.setYymm(billRocYyymm);
								resendebilllog.setWaterno(ewnWaterno);
								resendebilllog.setPdftype("1");
								resendebilllog.setPdftypecode(PdfDataTypeEnum.BANK_DEBIT.getTypeString());
								dataTool.insertresendebilllogToDatabase(resendebilllog);									
							}

							if (!skipDbLogging) {
								PdfSta sta = new PdfSta();
								sta.setMember_guid(ewnMember_guid);
								sta.setMemo("台灣自來水公司" + rocYearArgument + "年"
										+ rocMonthArgument + "月帳單通知");
								sta.setWatername(EbillService.AESDecrypt(watername));
								sta.setWaterno(ewnWaterno);
								sta.setFilepath(new File(noticePdfDir.getAbsolutePath() + File.separator
										+ ewnMember_guid + ".(" + billRocYyymm + ")" + ewnWaterno + ".pdf").getAbsolutePath());
								if(Integer.parseInt(adYM) >= 202112 ){
									sta.setFilepath(new File(noticePdfDir.getAbsoluteFile() + File.separator
											+ ewnWaterno + "("
											+ billRocYyymm + ")" + "." +  ewnMember_guid + ".pdf").getAbsolutePath());
								}
								
			
								sta.setWatertype(TYPE_BILL);
								sta.setIspaid("Y");
								sta.setYymm(adYM);
								sta.setPdftype(PdfDataTypeEnum.BANK_DEBIT.getTypeString());
								
								dataTool.insertPdfGenRecordToDatabase(sta, pdfData);

								Map<String, Object> pars = new HashMap<>();
								pars.put("waterno", ewnWaterno);
								pars.put("member_guid", ewnMember_guid);
								String sqlCommand = 
										"UPDATE water_e.EBILLS_WATERNUM" +
										" SET " + monthly + " = '" + rocMonthArgument + "月成功'" +
										" WHERE waterno=:waterno" +
												" AND member_guid=:member_guid";
								dbUtils.runUpdateSql(sqlCommand, pars, PropertiesTWCEBill.getWaterEConn());
							} else {
								LOG.warn("For JUnit Test 省略寫進資料庫: 在water_e.EBILLS_WATERNUM及pdf_sta記錄當月成功.");
							}
							cnt++;
						} catch (Exception ex) {
							++errorCount;
							
							String errorMessage = "帳單pdf(扣繳電子通知)產生失敗: waterno: "
									+ ewnWaterno + ", member_guid: " + ewnMember_guid
									+ ", filepath: " + noticePdfDir.getAbsolutePath();
							LOG.warn(errorMessage);
							errorMessages.append(errorMessage).append("\n");
							LOG.warn("TYYYMM 無該水號資料造成無法output，或無會員資料，或其它不明原因!", ex);
							
							if (!skipDbLogging) {
								// 在water_e.EBILLS_WATERNUM記錄當月失敗							
								HashMap<String, Object> pars = new HashMap<String, Object>();
								pars.put("waterno", ewnWaterno);
								pars.put("member_guid", ewnMember_guid);
								String sqlCommand = 
										"UPDATE water_e.EBILLS_WATERNUM" +
										" SET " + monthly + " = '" + rocMonthArgument + "月失敗'" +
										" WHERE waterno=:waterno" +
											" AND member_guid=:member_guid";
								dbUtils.runUpdateSql(sqlCommand, pars, PropertiesTWCEBill.getWaterEConn());
								LOG.info("在 water_e.EBILLS_WATERNUM 記錄 " + rocMonthArgument + "月, 該月失敗 just now.");
							} else {
								LOG.warn("For JUnit Test 省略寫進資料庫: 在water_e.EBILLS_WATERNUM記錄當月失敗.");
							}
							
						}
					} else if (billtype.equals(BILL_TYPE_BILL_DETAIL)) { // 產電子帳單pdf
						LOG.info("準備產生電子帳單pdf, 水號({})", ewnWaterno);

						Map<String, Object> paperBillInfo = getPaperBillInfo(ewnWaterno, adYM);
						
						if ("false".equals(paperBillInfo.get("bill"))) {
							LOG.info("本期不開紙本帳單(" + ewnWaterno + ")"); // 應該是不開"電子"帳單吧...?
							
							if (!skipDbLogging) {

								PdfSta sta = new PdfSta();

								int failCode = ((NotBillInfo)paperBillInfo.get("info")).getCode();
								String failReason = ((NotBillInfo)paperBillInfo.get("info")).getReason();
								sta.setFailReason(failReason);
								sta.setFailCode(failCode);
								sta.setYymm(adYM);
								sta.setMember_guid(ewnMember_guid);
								sta.setMemo("台灣自來水公司" + rocYearArgument + "年"
										+ rocMonthArgument + "月帳單通知");
								try {
									sta.setWatername(EbillService.AESDecrypt(watername));
								} catch (InvalidKeyException
										| NoSuchAlgorithmException
										| NoSuchPaddingException
										| IllegalBlockSizeException
										| BadPaddingException e) {
									e.printStackTrace();
								}
								sta.setWaterno(ewnWaterno);
								

								sta.setWatertype(TYPE_BILL);
								
								sta.setPdftype(PdfDataTypeEnum.SELF_OR_PREPAY.getTypeString());

								dataTool.insertPdfGenRecordToDatabase(sta, null);

								Map<String, Object> pars = new HashMap<>();
								pars.put("waterno", ewnWaterno);
								pars.put("member_guid", ewnMember_guid);

								String sqlCommand = "UPDATE water_e.EBILLS_WATERNUM" +
										" SET " + monthly + " = '" + rocMonthArgument + "月不開單'" +
										" WHERE waterno=:waterno" +
											" AND member_guid=:member_guid";
								dbUtils.runUpdateSql(sqlCommand, pars, PropertiesTWCEBill.getWaterEConn());
							} else {
								LOG.warn("For JUnit Test 省略寫進資料庫: 在water_e.pdf_sta紀錄及EBILLS_WATERNUM記錄當月'不開單'，而不是未繳.");
							}
						} else {
							try {
								PdfDataFactory pdfDataFactory = PdfDataFactory.getInstance(PdfDataTypeEnum.SELF_OR_PREPAY.getTypeString()).init(dbUtils);
								
								PdfData data = pdfDataFactory.prepareData(ewnWaterno, adYM,"");
																
								if (reGeneBill.equals("Y")){
									data.setReGenFlag(" ");
								}else{
									data.setReGenFlag(" ");
								}
								Integer yyyyYear = Integer.parseInt(data.getRocYear())+1911;
								String yyyyYearstr=yyyyYear.toString()+data.getMonthString();
								if (!reGenBarCode.equals("")){
									data.setChagWorkArea1(reGenBarCode.substring(0, 2));
									data.setChagWorkArea2(reGenBarCode.substring(2));
									if( Integer.parseInt(yyyyYearstr) < 201803){
										data.setCarrierID("");
									}
								}
								
								File unsignedPdfFile = new File(noticePdfDir + File.separator + ewnMember_guid + "." + "("
										+ billRocYyymm + ")" + ewnWaterno + ".pdf");
								File unsignedPdfFile2 = new File(noticePdfDir + File.separator + ewnWaterno + "("
										+ billRocYyymm + ")" + "." +  ewnMember_guid + ".pdf");
								
								
								PdfReport pdfReport = new PdfReportOnSelfOrPrePayNew111();
								

								pdfReport.output(data, new FileOutputStream(unsignedPdfFile));
								
								
								LOG.info("電子帳單(" + ewnWaterno + ")產生成功, filepath: " + 
										unsignedPdfFile.getAbsolutePath());
								
								if (!skipDbLogging && reGenBarCode.equals("")) {
									// 在water_e.pdf_sta記錄 START
									PdfSta sta = new PdfSta();
									sta.setMember_guid(ewnMember_guid);
									// pars.put("memo", thisyear + thismonth +
									// "水費（含代徵費用）電子通知及收據");
									sta.setMemo("台灣自來水公司" + rocYearArgument + "年"
											+ rocMonthArgument + "月帳單通知");
									sta.setWatername(EbillService.AESDecrypt(watername));
									//sta.setWatername(watername);
									sta.setWaterno(ewnWaterno);
									sta.setFilepath(new File(noticePdfDir.getAbsolutePath() + File.separator
											+ ewnMember_guid + ".(" + billRocYyymm + ")" + ewnWaterno + ".pdf").getAbsolutePath());							
									//20211011 修改檔名順序
									if(Integer.parseInt(adYM) >= 202112 ){
										sta.setFilepath(new File(noticePdfDir.getAbsoluteFile() + File.separator
												+ ewnWaterno + "("
												+ billRocYyymm + ")" + "." +  ewnMember_guid + ".pdf").getAbsolutePath());
									}
									
									
									sta.setWatertype(TYPE_BILL);
									sta.setIspaid("Y");
									sta.setYymm(adYM);
									sta.setPdftype(PdfDataTypeEnum.SELF_OR_PREPAY.getTypeString());

									dataTool.insertPdfGenRecordToDatabase(sta, data);
									// 在water_e.pdf_sta記錄 END
									
									// 在water_e.EBILLS_WATERNUM記錄當月成功
									Map<String, Object> pars = new HashMap<>();
									pars.put("waterno", ewnWaterno);
									pars.put("member_guid", ewnMember_guid);
									String sqlCommand = "UPDATE water_e.EBILLS_WATERNUM" +
											" SET " + monthly + " = '" + rocMonthArgument + "月成功'" +
											" WHERE waterno=:waterno" +
												" AND member_guid=:member_guid";
									dbUtils.runUpdateSql(sqlCommand, pars, PropertiesTWCEBill.getWaterEConn());
								} else {
									LOG.warn("For JUnit Test 省略寫進資料庫: 在water_e.pdf_sta記錄及在water_e.EBILLS_WATERNUM記錄當月成功");
								}
								
								if (!reGenBarCode.equals("")){
									//寫入一筆待發送排程
									ReSendeBillLog resendebilllog = new ReSendeBillLog();
									resendebilllog.setMemguid(ewnMember_guid);
									resendebilllog.setYymm(billRocYyymm);
									resendebilllog.setWaterno(ewnWaterno);
									resendebilllog.setPdftype("1");
									resendebilllog.setPdftypecode(PdfDataTypeEnum.SELF_OR_PREPAY.getTypeString());
									dataTool.insertresendebilllogToDatabase(resendebilllog);									
								}
								
								cnt++;
							} catch (Exception ex) {
								++errorCount;
								String errorMessage = "帳單pdf2(電子通知)產生失敗: waterno: "
										+ ewnWaterno
										+ ", member_guid: "
										+ ewnMember_guid
										+ ", filepath: "
										+ noticePdfDir.getAbsolutePath();
								LOG.warn(errorMessage);
								errorMessages.append(errorMessage).append("\n");
//								LOG.error("帳單pdf2(電子帳單)產生失敗!", ex);
								// DONE TO DO by Ca 20131227: 跟上面訊息重複，改為可能原因.
								LOG.warn("TYYYMM 無該水號資料造成無法output，或無會員資料，或其它不明原因!", ex);

								if (!skipDbLogging) {
									// 在water_e.EBILLS_WATERNUM記錄當月失敗
									HashMap<String, Object> pars = new HashMap<String, Object>();
									pars.clear();
									pars.put("waterno", ewnWaterno);
									pars.put("member_guid", ewnMember_guid);
									dbUtils.runUpdateSql(
											"update water_e.EBILLS_WATERNUM set "
													+ monthly
													+ " = '"
													+ rocMonthArgument
													+ "月失敗' where waterno=:waterno and member_guid=:member_guid",
											pars, PropertiesTWCEBill.getWaterEConn());
								} else {
									LOG.warn("For JUnit Test 省略寫進資料庫: 在water_e.EBILLS_WATERNUM記錄當月失敗");
								}

							}
						}

					}
				} else {
					LOG.info("無法產生PDF，原因: PDF是否存在: " + pdfAlreadyExist + ", TYYYMM水號: "
							+ ewnWaterno + ", 會員: " + ewnMember_guid + ", 原水號:"
							+ StringUtil.toNotNullString(ewnRow.get("waterno"))); // 2011-04-01
				}

				int remainder = ++processedDataCnt % 500;
				if (processedDataCnt == 1 || remainder == 0 || processedDataCnt == dataSize) {
					long soFar = new Date().getTime() - forStarts;
					long avg = soFar / processedDataCnt;
					LOG.info("●●●Subsummary: 已處理資料: →{}← out of →{}←. 平均處理每筆需要 →{}← ms. \n" +
							"ops=→{}←", 
							processedDataCnt, dataSize, avg, ops);
				}		
				
			} // end of for loop.
			
			LOG.info("水號正常者共 {} 筆", has_water_no);
			LOG.info("本次處理PDF共 {} 筆", pdf_not_exists);
			LOG.info("水號正常且本次需處理PDF資料共 {} 筆", cnt_water);
			LOG.info("產生帳單pdf結束共 {} 筆\n", cnt);
			
			if(!skipMgrNotice) {
				
				//add by jenny 
				updatePDFsta(adYM);
				//add by jenny 
				
				send_result.append("產生帳單pdf結束共產生 ").append(cnt).append(" 筆, 失敗 ")
					.append(errorCount).append("筆 ").append(errorMessages.toString());
				String noticeHtmlTemplate = dataTool.getCW06MailTemplateContent("sendnoticemail");
				
				String subject = "台灣自來水公司" + rocYearArgument + "年" + rocMonthArgument + "月帳單通知" + "pdf " + station;
				OMICardEmailReturn sendManagerRtn = omiCardTool.sendManager2(subject, 
						MessageFormat.format(noticeHtmlTemplate, subject, send_result.toString()));
				// Add log by Ca 20140106.
				if(sendManagerRtn.getERRORCODE() == 0 && StringUtils.isEmpty(sendManagerRtn.getERRORMSG())) {
					LOG.debug("●●●OMICard任務ID: →{}←. ops=→{}←", sendManagerRtn.getLAUNCHID(), ops);
				} else {
		        	LOG.error("●●●寄送管理者通知信有錯!! errorcode: →{}←, errormsg: →{}←. ops=→{}←", 
		        			sendManagerRtn.getERRORCODE(), sendManagerRtn.getERRORMSG(), ops);
				}
			}
			
		}

	}


	private void updatePDFsta(String adYM) {
		HashMap<String, Object> pars = new HashMap<String, Object>();
		pars.clear();
		pars.put("adYM", adYM);
		
		dbUtils.runUpdateSql(
				"UPDATE water_e.water_e.pdf_sta SET pdfdata = null"				
						+ " where yymm=:adYM AND pdfdata = 'null' ",
				pars, PropertiesTWCEBill.getWaterEConn());
		
		
	}
	private Map<String, Object> getPaperBillInfo(String waterno, String yyyymm) {
		Map<String, Object> rtn = new HashMap<>();
		Map<String, Object> row = dataTool.paperBillYesNo(waterno, yyyymm);
		if(row.isEmpty()) {
        	//沒撈到資料do nothing
        	LOG.info("paperBillYesNo() 判斷是否不開單-->沒撈到資料,不開單!");
        	
        	rtn.put("bill", "false");
        	rtn.put("info", NotBillInfo.NO_DATA);
        } else if("1".equals(row.get("CHAG_OWED_DUE"))) {
        	//再加判斷waterfee 判斷上月PAID 這個欄位是否已變Y,如果用戶已繳費,
        	//資料更新會有2-3天時間差 add by jenny 20130820 start
        	boolean isPaidLast = false;
//			isPaidLast = getIsPaidLast(waterno, yyyymm);
        	// 改共用EbillService. 此method會撈BMBA and Waterfee Tyyymm.
			isPaidLast = this.ebillService.getIsPaidLast(waterno, yyyymm);
			if (isPaidLast){
				LOG.info("paperBillYesNo() 判斷是否不開單-->要開單!");
				rtn.put("bill", "true");
			}
			else{
				LOG.info("paperBillYesNo() 判斷是否不開單-->前期逾期!");
	        	
	        	rtn.put("bill", "false");
	        	rtn.put("info", NotBillInfo.CHAG_DUE);
			}
        	
        } else if("Y".equals(row.get("CHAG_BILL_NOT"))) {
        	LOG.info("paperBillYesNo() 判斷是否不開單-->不印紙本!");
        	
        	rtn.put("bill", "false");
        	rtn.put("info", NotBillInfo.BILL_NOT);
        } else {
        	//有撈到資料,且不符合不開單規則
        	LOG.info("paperBillYesNo() 判斷是否不開單-->要開單!");
        	rtn.put("bill", "true");
        }
		return rtn;
	}
	
	enum NotBillInfo {
		CHAG_DUE(1, "前期逾期"),
		BILL_NOT(2, "不印紙本"),
		NO_DATA(3, "無資料"),
		ERROR(4, "產生失敗")
		;
		
		private int code;
		private String reason;
		
		NotBillInfo(int code, String reason) {
			this.code = code;
			this.reason = reason;
		}
		
		public int getCode() {
			return this.code;
		}
		
		public String getReason() {
			return this.reason;
		}
		
		public static String findReason(int code){
			String notFound = "";
			NotBillInfo[] notBillInfos = NotBillInfo.values();
			for(int i=0;i<notBillInfos.length;i++){
				if(notBillInfos[i].getCode() == code){
					return notBillInfos[i].getReason();
				}
			}
			return notFound;
		}
	}

	private File prepareDir(String dirpath, String dirpath2) {
		final String fullpath = dirpath + File.separator + dirpath2;
		File rtn = new File(fullpath);
		CommonTool.checkDirExistAndCanWrite(rtn);
		return rtn;
	}

	/**
	 * 此排程的初始檢查，若有不滿足或無法繼續的，直接丟出exception給BatchWork.work()接.
	 * @param ops
	 */
	private void initialPreparation(HashMap<String, String> ops) {
		String unsignedDocsUrl = PropertiesTWCEBill.getNoticePdfPath();
		CommonTool.checkDirExistAndCanWrite(new File(unsignedDocsUrl));
	}

	/**
	 * 對manager作帳單表格(Tyyymm)異常通知的email派送
	 */
	public void notifyBillTyyymmNotExist(String year, String month) {
		String emailContentTemplate = dataTool.getCW06MailTemplateContent(BATCH_EXCEPTION_MAIL_TEMPLATE_NAME);
		StringBuffer mailSubject = new StringBuffer();
		mailSubject.append(year).append("年").append(month).append("月").append(new DateTime().getDayOfMonth()).append("日 ");
		mailSubject.append("台灣自來水公司").append(year).append( "年").append( month).append( "月帳單異常通知pdf");
		StringBuffer mailContent = new StringBuffer();
		mailContent.append(MessageFormat.format(emailContentTemplate, 
				"台灣自來水公司" + year + "年" + month + "月帳單異常通知" + "pdf",
				"開單資料 T" + year + month + " 不存在"));
		
		//Johnson 改成call OMICardTool
		omiCardTool.sendManager2(mailSubject.toString(), mailContent.toString());
	}
	
	
}