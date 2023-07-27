package twcebillsysbatch.type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jodd.datetime.JDateTime;
import jodd.typeconverter.IntegerConverter;
import jodd.typeconverter.StringConverter;
import jodd.util.StringUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenNew111ProofReceiptPDFBatchType extends BatchType {
	static final Logger LOG = LoggerFactory.getLogger(GenProofReceiptPDFBatchType.class);

	@Override
	public String getTypeCode() {
		return BatchTypeEnum.GEN_NEW105_PROOF_PDF.getTypeCode();
	}

	@Override
	public void work(HashMap<String, String> ops) {
		LOG.info("產生扣帳收據pdf啟動...");
		int handledDataCnt = 0;
		StringBuffer send_result = new StringBuffer();

		String reGeneBill = StringUtil.toNotNullString(ops.get("reGeneBill"));
		String opsStation = StringUtil.toNotNullString(ops.get("station"));
		String opsTestWaterNo = ops.get("testWaterNo");
        String ops_member_guid = StringUtil.toNotNullString(ops.get("member_guid")); // 指定要產的用戶
		//測試用密碼參數
		String opsTest_password = StringUtil.toNotNullString(ops.get("test_password"));
		boolean test_notComWaternos = BooleanUtils.toBoolean(ops.get("notComWaternos")); //for notComWaternos 產製某區間水號
		boolean opsSkipMgrNotice = BooleanUtils.toBoolean(ops.get("skipMgrNotice"));//是否省略寄送通知信給管理員
		// 預防測試時，寫入測試資料夾的路徑。Unit Test ONLY.
		boolean unitTestSkipPdfSta = BooleanUtils.toBoolean(ops.get("unitTestSkipPdfSta"));
		boolean unitTestIsNotSentSuccess = BooleanUtils.toBoolean(ops.get("unitTestIsNotSentSuccess"));
		boolean unitTestIsPdfNotExisting = BooleanUtils.toBoolean(ops.get("unitTestIsPdfNotExisting"));

		JDateTime jdt = new JDateTime();
		String opsRocYear = StringUtil.isNotEmpty(ops.get("yyy")) ? 
				StringUtil.toNotNullString(ops.get("yyy")) : 
				StringUtil.toNotNullString(jdt.getYear() - 1911);
		String opsRocMonth = StringUtil.isNotEmpty(ops.get("mm")) ? 
				StringUtil.toNotNullString(ops.get("mm")) : 
				jdt.toString("MM");
		String opsAdYear = StringConverter.valueOf(IntegerConverter
				.valueOf(opsRocYear) + 1911);
		String billRocYyymm = opsRocYear + opsRocMonth;
		// 本期年月
		String adYearMonth = DateTool.rocYMtoADYM(opsRocYear + opsRocMonth);
		
		String whereSql = "";
		if (opsStation.length() > 0) {
			whereSql = whereSql + " AND waterno like '" + opsStation + "%'";
			//whereSql = whereSql + " and waterno not in (select old_waterno from water_e.renamed_waterno(nolock) where old_waterno like '" + opsStation + "%') ";
		}
		if (StringUtils.isNotEmpty(opsTestWaterNo)) {
			whereSql += " AND waterno = '" + opsTestWaterNo + "'";
		}
		if (test_notComWaternos) {
			whereSql += " AND waterno in (select * from water_e.notComWaternos)";
		}
		
        if(StringUtils.isNotEmpty(ops_member_guid)) {
        	whereSql = whereSql + " AND member_guid = '" + ops_member_guid + "'";
        }
		

		String sqlQuery =  "SELECT * FROM water_e.EBILLS_WATERNUM WITH (NOLOCK)" +
				" WHERE 1=1 " + whereSql + 
				" ORDER BY member_guid DESC ";
		LOG.info("取得EBILLS_WATERNUM資料中... query: → {} ←", sqlQuery);
		Collection<Map<String, Object>> ebillsWaternumResults = null;
    	long start = new Date().getTime();
		ebillsWaternumResults = dbUtils.getsqlValues(sqlQuery, null, PropertiesTWCEBill.getWaterEConn());
		long end = new Date().getTime();


		int errorCount = 0;
		StringBuffer errorMessages = new StringBuffer("\n<br/>");

		if (ebillsWaternumResults != null && !ebillsWaternumResults.isEmpty()) {
			String paid = "";
			String wrba_invo_no = "";

			CommonTool.checkDirExistAndCanWrite(new File(PropertiesTWCEBill.getProofDirPath() + File.separator +adYearMonth));
			CommonTool.checkDirExistAndCanWrite(new File(PropertiesTWCEBill.getProofDirPath() + File.separator +adYearMonth + File.separator +"html"));
			CommonTool.checkDirExistAndCanWrite(new File(PropertiesTWCEBill.getProofDirPath() + File.separator +adYearMonth + File.separator +"html"+ File.separator +"barcode"));
			CommonTool.checkDirExistAndCanWrite(new File(PropertiesTWCEBill.getReceiptDirPath() + File.separator +adYearMonth));
			CommonTool.checkDirExistAndCanWrite(new File(PropertiesTWCEBill.getReceiptDirPath() + File.separator +adYearMonth + File.separator +"html"));
			CommonTool.checkDirExistAndCanWrite(new File(PropertiesTWCEBill.getReceiptDirPath() + File.separator +adYearMonth + File.separator +"html"+ File.separator +"barcode"));
			
	    	long startFor = new Date().getTime();
			for (Map<String, Object> ewnRow : ebillsWaternumResults) {

				String ewnWaterno = StringUtil.toNotNullString(ewnRow.get("waterno"));
				String ewnMember_guid = StringUtil.toNotNullString(ewnRow.get("member_guid"));
				String ewnEntitybill = StringUtil.toNotNullString(ewnRow.get("entitybill"));


				Map<String, Object> TRAN_WB_W860 = dataTool.getW860TableValue(adYearMonth, ewnWaterno);
				paid = StringUtil.toNotNullString(TRAN_WB_W860.get("W860_RESULT")).trim();
				
				Map<String, Object> tyyymmData = dataTool.queryTyyymmByWaterNO(ewnWaterno, opsRocYear+opsRocMonth, null, 
						"waterno", "ac1", "prefee", "name","ALLOW2STA","PAID");
				String allow2sta = StringUtil.toNotNullString(tyyymmData.get("ALLOW2STA")).trim(); 
				String paid_waterfee = StringUtil.toNotNullString(tyyymmData.get("PAID")).trim(); 
				if (!paid_waterfee.equals("Y")){paid="";}
				
				if ("00".equals(paid) || unitTestIsNotSentSuccess) {	
					
					Map<String, Object> WRBA = dataTool.getWRBATableValueByWaterNo(ewnWaterno);
					wrba_invo_no = StringUtil.toNotNullString(WRBA.get("WRBA_INVO_NO"));

					String proofPdfPath;
					String proofPdfPath2;
					
					

					String lastmemgrid="";
					if (allow2sta.equals("電子帳單回饋金")){ 
						if (DBDataTool.getInstance().hasDobuleMemberforEbill(ewnWaterno)){

							lastmemgrid=DBDataTool.getInstance().getLastMemberforEbillGridID(ewnWaterno);						
						}else{
							lastmemgrid=ewnMember_guid;
						}
					}


					Map<String, Object> member = dataTool.getMember(ewnMember_guid);
					

					if (member.isEmpty()) {
						++errorCount;
						LOG.error("扣帳收據pdf產生失敗: waterno: " + ewnWaterno + ", member_guid: "+ ewnMember_guid + "原因: 未取得會員帳號資料!");
						continue;
					}

					if (StringUtils.isNotEmpty(wrba_invo_no)) {
						proofPdfPath = PropertiesTWCEBill.getProofDirPath()
								+ File.separator + adYearMonth + File.separator
								+ ewnMember_guid + ".(" + opsRocYear + opsRocMonth
								+ ")" + ewnWaterno + ".pdf";
						

						proofPdfPath2 = PropertiesTWCEBill.getProofDirPath()
								+ File.separator + adYearMonth + File.separator
								+ ewnWaterno + "(" + opsRocYear + opsRocMonth
								+ ")" + "." + ewnMember_guid + "(R).pdf";
						
						if(Integer.parseInt(adYearMonth) >= 202201 ){
							proofPdfPath = proofPdfPath2;
						}

						File proofPdfFile = makeSureParentExistsAndReturnFile(proofPdfPath);
						boolean proofPdfExists = proofPdfFile.exists();

						if (!proofPdfExists || unitTestIsPdfNotExisting) {
							try {

								// Johnson 產生pdf資料邏輯抽出，並產生PdfData
								PdfDataFactory proofPdfDataFactory = PdfDataFactory.getInstance(PdfDataTypeEnum.ORG_PROOF.getTypeString()).init(dbUtils);

								PdfData pdfData = proofPdfDataFactory.prepareData(ewnWaterno, opsAdYear + opsRocMonth,"");
								if (reGeneBill.equals("Y")){
									pdfData.setReGenFlag("  ");
								}else{
									pdfData.setReGenFlag(" ");
								}
								
								
								PdfReport pdfReport = null;
								
								String  hasCarrierid="N";
								
								pdfReport=new PdfReportOnOrgProofNew111();
									
								
								pdfReport.output(pdfData, new FileOutputStream(proofPdfFile));
								
								handledDataCnt++;
								LOG.info("公司用戶(" + ewnWaterno + ")收據PDF產生成功");
    							if (reGeneBill.equals("Y")){
								ReSendeBillLog resendebilllog = new ReSendeBillLog();
									resendebilllog.setMemguid(ewnMember_guid);
									resendebilllog.setYymm(billRocYyymm);
									resendebilllog.setWaterno(ewnWaterno);
									resendebilllog.setPdftype("2");
									resendebilllog.setPdftypecode(PdfDataTypeEnum.ORG_PROOF.getTypeString());
									dataTool.insertresendebilllogToDatabase(resendebilllog);									
								}
								if (!unitTestSkipPdfSta) {
									PdfSta pdfSta = new PdfSta();
									pdfSta.setMember_guid(ewnMember_guid);
									pdfSta.setMemo("台灣自來水公司" + opsRocYear + opsRocMonth
											+ "水費代繳扣帳成功通知");
									pdfSta.setWatername(EbillService.AESDecrypt(StringUtils
											.trimToEmpty((String) WRBA
													.get("WRBA_BNEW_USER"))));
									
									pdfSta.setWaterno(ewnWaterno);
									pdfSta.setFilepath(proofPdfFile
											.getAbsolutePath());
									pdfSta.setWatertype(PdfDataTool.PdfType.RECEIPT.code());
									pdfSta.setIspaid("Y");
									pdfSta.setYymm(adYearMonth);
									pdfSta.setPdftype(PdfDataTypeEnum.ORG_PROOF.getTypeString());

									dataTool.insertPdfGenRecordToDatabase(pdfSta, pdfData);
								}

							} catch (Exception ex) {
								++errorCount;
								String errorMessage = "扣帳收據pdf(公司用戶)產生失敗: waterno: "
										+ ewnWaterno
										+ ", member_guid: "
										+ ewnMember_guid
										+ ", filepath: "
										+ proofPdfPath;
								LOG.error(errorMessage);
								errorMessages.append(errorMessage).append("<br>");
								LOG.error("扣帳收據pdf(公司用戶)產生失敗!", ex);
							}
						} else {
							LOG.info("扣帳收據pdf已經存在，故不再產==>waterno: → {} ←. member guid: → {} ←. path: → {} ←", 
									ewnWaterno, ewnMember_guid, proofPdfPath);
						}
					} else {
						proofPdfPath = PropertiesTWCEBill.getReceiptDirPath()	//與上面不一樣，這邊是找個人用戶收據資料夾
								+ File.separator + adYearMonth + File.separator
								+ ewnMember_guid + "." + "(" + opsRocYear + opsRocMonth
								+ ")" + ewnWaterno + ".pdf";
						proofPdfPath2 = PropertiesTWCEBill.getReceiptDirPath()
								+ File.separator + adYearMonth + File.separator
								+ ewnWaterno + "(" + opsRocYear + opsRocMonth
								+ ")" + "." + ewnMember_guid + "(R).pdf";
						
						if(Integer.parseInt(adYearMonth) >= 202201 ){
							proofPdfPath = proofPdfPath2;
						}

						
						File proofPdfFile = makeSureParentExistsAndReturnFile(proofPdfPath);
						boolean receiptPdfAlreadyExists = proofPdfFile.exists();
						if (!receiptPdfAlreadyExists || unitTestIsPdfNotExisting) {
							try {
								PdfDataFactory receiptPdfDataFactory = PdfDataFactory.getInstance(
										PdfDataTypeEnum.PERSONAL_RECEIPT.getTypeString())
										.init(dbUtils);
								PdfData pdfData = receiptPdfDataFactory.prepareData(ewnWaterno, adYearMonth,"");
								if (reGeneBill.equals("Y")){
									pdfData.setReGenFlag("  ");
								}else{
									pdfData.setReGenFlag(" ");
								}
								
								
								pdfData.setIsW890Paid("N");
								
								pdfData.setIsW880Paid("N");
								
								
								PdfReport pdfReport = null;
								
								String  hasCarrierid="N";
								
								pdfReport=new PdfReportOnOrgProofNew111();

								
								
								pdfReport.output(pdfData, new FileOutputStream(proofPdfFile));
								
								
							
								
								handledDataCnt++;
								LOG.info("個人用戶收據pdf(" + ewnWaterno + ")產生成功");
    							if (reGeneBill.equals("Y")){
								ReSendeBillLog resendebilllog = new ReSendeBillLog();
									resendebilllog.setMemguid(ewnMember_guid);
									resendebilllog.setYymm(billRocYyymm);
									resendebilllog.setWaterno(ewnWaterno);
									resendebilllog.setPdftype("2");
									resendebilllog.setPdftypecode(PdfDataTypeEnum.ORG_PROOF.getTypeString());
									dataTool.insertresendebilllogToDatabase(resendebilllog);									
    							}
								
								if (!unitTestSkipPdfSta) {

									PdfSta pdfSta = new PdfSta();
									pdfSta.setMember_guid(ewnMember_guid);
									pdfSta.setMemo("台灣自來水公司" + opsRocYear + opsRocMonth
											+ "水費代繳扣帳成功通知");
									pdfSta.setWatername(EbillService.AESDecrypt(StringUtils
											.trimToEmpty((String) WRBA
													.get("WRBA_BNEW_USER"))));
									
									pdfSta.setWaterno(ewnWaterno);
	
									pdfSta.setFilepath(proofPdfPath);
									pdfSta.setWatertype(PdfDataTool.PdfType.RECEIPT.code());
									pdfSta.setYymm(adYearMonth);
									pdfSta.setPdftype(PdfDataTypeEnum.PERSONAL_RECEIPT.getTypeString());
									dataTool.insertPdfGenRecordToDatabase(pdfSta, pdfData);
								}
							} catch (Exception ex) {
								++errorCount;
								String errorMessage = "扣帳收據pdf(個人用戶)產生失敗: waterno: "
										+ ewnWaterno
										+ ", member_guid: "
										+ ewnMember_guid
										+ ", filepath: "
										+ proofPdfPath;
								LOG.error(errorMessage);
								errorMessages.append(errorMessage).append(
										"\n<br/>");
								LOG.error("扣帳收據pdf(個人用戶)產生失敗!", ex);
							}
						} else {
							LOG.info("扣帳收據pdf已經存在，故不再產==>waterno: → {} ←. member guid: → {} ←. path: → {} ←", 
									ewnWaterno, ewnMember_guid, proofPdfPath);
						}
					}
				} else { // 由LOG輸出原因 20140317 modified by Ca.
					if (!"00".equals(paid)) {
						LOG.info("不產PDF，因為未繳! waterno: " + ewnWaterno + 
								", 期別: " + adYearMonth + ", 會員: " + ewnMember_guid + ", paid?: " + paid + 
								", 免寄紙本?: " + ewnEntitybill); 
					} else if ( !(unitTestIsNotSentSuccess) ) {
						LOG.info("不產PDF，因為已經成功寄送過扣帳收據PDF! waterno: " + ewnWaterno + 
								", 期別: " + adYearMonth + ", 會員: " + ewnMember_guid + ", paid?: " + paid + 
								", 免寄紙本?: " + ewnEntitybill); 
					}
				}
			} // End of for.
			long endFor = new Date().getTime();
	        // HyWeb debug參考用 commented by Ca 20130926.
			LOG.info("平均每秒處理→{}←筆資料。ops={}", (handledDataCnt / (endFor - startFor)) * 1000, ops);
		} else {
			LOG.warn("產生扣帳收據pdf時,撈EBILLS_WATERNUM無資料!");
		}

        // Added ops by Ca 20130926.
		LOG.info("產生扣帳收據pdf結束，共處理 {} 筆。ops={}", handledDataCnt, ops);
		
		//if("true".equals(ops.get("skipMgrNotice"))) {
		if(!opsSkipMgrNotice) {
	        // Added by Ca 20130926.
			LOG.info("準備寄送管理員通知。ops={}", ops);
			send_result.append("產生扣帳收據pdf結束共產生 ").append(handledDataCnt).append(" 筆, 失敗 ")
					.append(errorCount).append("筆 ").append(errorMessages);
		
			// Johnson 改為call OMICard
			// new MailHunterUtil().SmtpMail(mgrEmailSubject, mgrEmailContent);
			String mgrEmailSubject = "台灣自來水公司" + opsRocYear + opsRocMonth
					+ "水費代繳扣帳成功通知pdf";
			String mgrEmailContent = MessageFormat.format(
					dataTool.getCW06MailTemplateContent("sendnoticemail"),
					mgrEmailSubject, send_result);
			OMICardEmailReturn sendManagerRtn = omiCardTool.sendManager2(
					mgrEmailSubject, mgrEmailContent);
			if (!sendManagerRtn.success()) {
				LOG.error("寄送管理者通知信有錯!! code: " + sendManagerRtn.getERRORCODE()
						+ ", msg: " + sendManagerRtn.getERRORMSG());
			}
		}

	}

	private void replaceFile(File src, File dest) throws IOException{
		FileUtils.copyFile(src, dest);
        if(!src.delete()){
        	FileUtils.deleteQuietly(src);
        }
	}

}
