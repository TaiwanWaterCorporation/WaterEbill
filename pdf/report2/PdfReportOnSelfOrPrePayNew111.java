package twcebillsysbatch.pdf.report2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;

public class PdfReportOnSelfOrPrePayNew111 extends PdfReport {
	private static final Logger LOG = Logger.getLogger(PdfReportOnSelfOrPrePayNew103.class);
	private static final int PDF_TOTAL_WIDTH = 550;
	
	private static final int orgBestWidth = 360;//410
    private static final int orgBestHigth = 40;

	/**
	 * 依據data產出pdf實體到目標OutputStream
	 * @param data
	 * @param outputStream
	 */
	@Override
	public void output(PdfData data, OutputStream outputStream) {
		
		Integer yyyyYear = Integer.parseInt(data.getRocYear())+1911;
		String yyyyYearstr=yyyyYear.toString()+data.getMonthString();
		String ebillPath=PropertiesTWCEBill.getNoticePdfPath()+ File.separator + yyyyYearstr ;
		String pdfimgPath=PropertiesTWCEBill.getPdfNoticeBank103Path();
		String rocDateTensUnits = "";
        if (String.valueOf(data.getRocYear()).length() >= 3) {//100年後
            rocDateTensUnits = String.valueOf(data.getRocYear()).substring(1, 3);
        } else {//99年以前
            rocDateTensUnits = String.valueOf(data.getRocYear());
        }
        String imgfilename=rocDateTensUnits + data.getMonthString() +data.getWaterNO();
        
        Map<String, Object> mapdata = new HashMap<String, Object>();
        try {
        	//條碼一
            String filepath =ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_1.jpg";
			genBarcode("",rocDateTensUnits + data.getMonthString() + data.getWaterNO(),filepath);
			mapdata.put("barcode1",rocDateTensUnits + data.getMonthString() + data.getWaterNO());
			
			//條碼二
			String new_amt = StringUtils.leftPad(data.getTotal(), 8, "0");
            filepath = ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_2.jpg";
			genBarcode("",new_amt,filepath);
			mapdata.put("barcode2",new_amt);
			
			//條碼三
			String code39_3="";
			if (data.getReceivingDuedateRocMonth().equals("01")) {
            	Integer dueNextYear = Integer.parseInt(rocDateTensUnits) + 1;
            	String dueNextYearString = dueNextYear <= 9 ? 
            			"0" + dueNextYear.toString() : dueNextYear.toString();
                code39_3=dueNextYearString + data.getReceivingDuedateRocMonth() + "21101";
            } else {
                code39_3=rocDateTensUnits + data.getReceivingDuedateRocMonth() + "21101";
            }
			
			//20200424紓困方案之水號 修改代收日期(武漢肺炎)
			if(Integer.parseInt(yyyyYearstr) >= 202005 && Integer.parseInt(yyyyYearstr) <= 202012){
				if(EbillService.checkWaternoDelayList(data.getWaterNO()) && !(( data.getRocYear() + data.getMonthString() ).equals( (data.getDuedate().substring(0,5)) ) )){
					 code39_3=data.getDuedate().substring(1,3)+ data.getDuedate().substring(3,5) + "21101";
				}
			}
			
            filepath = ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_3.jpg";
			genBarcode("",code39_3,filepath);
			mapdata.put("barcode3",code39_3);
			
			//條碼四
			LOG.info("條碼四:水號=" + data.getWaterNO()+ data.getChagWorkArea1() + data.getChagWorkArea2());
            filepath = ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_4.jpg";
			String strBarCode4=data.getWaterNO() + StringUtils.defaultIfBlank(data.getChagWorkArea1(),"") + StringUtils.defaultIfBlank(data.getChagWorkArea2(),"") + "0";
			genBarcode_3("",strBarCode4,filepath);
			mapdata.put("barcode4",strBarCode4);
			
			//條碼五
			//三位數民國年
            String bar_year = StringUtils.leftPad(data.getRocYear(), 3, "0");

            //Johnson 總金額補成9位數左邊補0
            String bar_money = StringUtils.leftPad(data.getTotal().trim(), 9, "0");

            //Johnson 民國年月與總金額之間會放一碼查核碼
            String checkcode = "";
            checkcode = pdfutil.trans_checkcode(data.getReceivingDuedateRocYear() + data.getReceivingDuedateRocMonth() + "21101", 
                		data.getWaterNO() + data.getChagWorkArea1() + data.getChagWorkArea2() + "0",
                		bar_year + data.getMonthString() + bar_money);
            
            
            filepath = ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_5.jpg";
            String strBarCode5=bar_year + data.getMonthString() + checkcode + bar_money;
            genBarcode_3("",strBarCode5,filepath);
            mapdata.put("barcode5",strBarCode5);
			
			//載具編號
	        String tmpcarrierid="";
	        if(!StringUtils.isEmpty(data.getCarrierID())){
	        	tmpcarrierid=data.getCarrierID().toString();
	        }

	        if (!tmpcarrierid.equals("")){        	
	            

	        	if (tmpcarrierid.length()>=23){
	        		String tmpstr=tmpcarrierid.substring(0,5)+" "+tmpcarrierid.substring(5,15)+" "+tmpcarrierid.substring(15);
	        		tmpcarrierid=tmpstr;
	        	}else if (tmpcarrierid.length()>=21){//201811載具縮短為21碼
	        		String tmpstr=tmpcarrierid.substring(0,5)+" "+tmpcarrierid.substring(5,15)+" "+tmpcarrierid.substring(15);
	        		tmpcarrierid=tmpstr;
	        	} 
	            
	        	filepath =ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_CARRIERID.jpg";
				try {
					genBarcodetype2("年期別-載具流水號-檢核碼",data.getCarrierID().toString(),filepath);
					//genBarcode("年期別-載具流水號-檢核碼",data.getCarrierID().toString(),filepath);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		
	        
	        
	        
			//1.讀html template
			Configuration cfg = new Configuration();
			cfg.setDefaultEncoding("UTF-8");
			try {
				cfg.setDirectoryForTemplateLoading(new File(PropertiesTWCEBill.getPdfNoticeSelf103Path()));
				// Load the template
				Template template;		
				template=cfg.getTemplate("SelfOrPrePayNotice111.ftl");

				template.setEncoding("UTF-8");
			
			//2.將data與html template merge為html檔
								  
				
				BigDecimal Bbasicfee = new BigDecimal(data.getBasicfee()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Bfee = new BigDecimal(data.getFee().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Bfeedback = new BigDecimal(data.getFeedback().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Bpbfee = new BigDecimal(data.getPbfee().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Btax = new BigDecimal(data.getTax().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Bdelayfee = new BigDecimal(data.getDelayfee().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Ballowance = new BigDecimal(data.getAllowance().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal Ballow2 = new BigDecimal(data.getAllow2().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
				//108年4月後加入新欄位 應收操作維護費,加退操作維護費,工程改善費,加退工程改善費
	            BigDecimal Bopamt = null;
	            BigDecimal Bopamt2 = null;
	            if( Integer.parseInt(yyyyYearstr) >= 201904){
		             Bopamt = new BigDecimal(data.getOP_AMT().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
		             Bopamt2 = new BigDecimal(data.getOP_AMT2().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
	            }
	            //水費項目小計用應繳總金額回推
	            BigDecimal totamt = new BigDecimal(data.getTotal().trim());
	            
	            BigDecimal subamt2; 

	            if(Integer.parseInt(yyyyYearstr) >= 201904){
				     subamt2 = new BigDecimal((NumberUtils.toInt(data.getDirty().trim())
				    		+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
				    		+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
				    		+ NumberUtils.toInt(data.getMB_AMT()) + NumberUtils.toInt(data.getMB_AMT2().trim())
				    		+ NumberUtils.toInt(data.getLevy().trim())));
	            }else{
		             subamt2 = new BigDecimal((NumberUtils.toInt(data.getDirty().trim())
		            		+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
		            		+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
		            		+ NumberUtils.toInt(data.getLevy().trim())));	
	            }
	            
	            
	            
	            if(!pdfutil.trans_zero("◎本期預繳金額", data.getLastfee().trim()).equals("")){
	            	//本期預繳金額
	            	BigDecimal prepayamt = new BigDecimal(NumberUtils.toInt(data.getLastfee().trim()) - NumberUtils.toInt(data.getPrefee().trim()));
	            	totamt=totamt.add(prepayamt);
	            }
	            
	            //減去稅就是應稅銷售
	            String subamtnotax =thousandDecimalFormat.format(Math.round(totamt.subtract(subamt2).subtract(Btax).doubleValue())) + "元";
     	       
				
				
				
				mapdata.put("REMARK",data.getReGenFlag());
				if (!tmpcarrierid.equals("")&&("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta()))) {
					mapdata.put("SHOWCARRIERID",tmpcarrierid);
				}
				mapdata.put("FILEPATH",pdfimgPath);
				mapdata.put("imgname",ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator+imgfilename);
				mapdata.put("YYY", ""+data.getRocYear());//帳單年				
				mapdata.put("MM", ""+NumberUtils.toInt(data.getMonthString()));//帳單月
				mapdata.put("MM2", ""+data.getMonthString());//帳單月
				
				mapdata.put("PYYY", ""+data.getRocYear());//繳費期限年
				mapdata.put("PMM", ""+data.getMonthString());//繳費期限月
				
				
				
				mapdata.put("RYYY",data.getReceivingDuedateRocYear());//代收年
				mapdata.put("RMM",data.getReceivingDuedateRocMonth());//代收月
				mapdata.put("PYYYMM", ""+data.getRocYear()+"/"+data.getMonthString()+"/21");//繳費期限月
				
				
				mapdata.put("RPTDATE", ""+data.getPrintDate()); //印製日期
				mapdata.put("ZIPCODE", ""+data.getWrbaPostCode2());//ZIPCODE
				mapdata.put("ADDR", StringUtils.isEmpty(data.getWrbaComuAddr())?
						data.getWrbaEmpeAddr2():
							data.getWrbaComuAddr());
				//mapdata.put("USERNAME", ""+data.getUserName());
				mapdata.put("USERNAME", ""+EbillService.AESDecrypt(data.getUserName()));
				
				mapdata.put("STATIONNAME", ""+data.getWaterStationName());
				mapdata.put("STATIONADDR", ""+data.getWaterStationAddr());
				mapdata.put("ATATIONTEL", "　"+data.getWaterStationTel());
				
				mapdata.put("RECDUEDT",data.getReceivingDuedateRocString()); //代收期限
				
				
				
				mapdata.put("WRITEOFFNO",data.getWriteOffNumber());//銷帳編號
				mapdata.put("TOTALAMT",StringUtils.leftPad(data.getTotal().trim(), 8, "0"));//應繳總金額
				mapdata.put("CHECKINGCODE",data.getCheckingCode());//查核碼
				
				mapdata.put("WATERDTS",DateTool.changeRocDateDisplay(PdfTool.genMeteredDate(data.getLastMeteredDate()), "/"));
				mapdata.put("WATERDTE",DateTool.changeRocDateDisplay(data.getMeteredDate(), "/"));
				//mapdata.put("WATERADDR", ""+data.getWaterAddress());
				mapdata.put("WATERADDR", ""+EbillService.AESDecrypt(data.getWaterAddress()));
				
				//20180307新增補寄帳單顯示補1 補2
				if(Integer.parseInt(data.getChagWorkArea1() + data.getChagWorkArea2())==9983){
					mapdata.put("REGENTITLE", "補1");
				}else if(Integer.parseInt(data.getChagWorkArea1() + data.getChagWorkArea2())==9984){
					mapdata.put("REGENTITLE", "補2");
				}
				
				
				mapdata.put("BANK", data.getAc1() + data.getAc2() + "********");
				mapdata.put("INVONO", StringUtils.isEmpty(data.getWrbaInvoNO())?"":"用戶營利事業統一編號 " +data.getWrbaInvoNO());
			
//				mapdata.put("LASTAVGQTY", data.getLastAvgQty() + " 度"); //去年同期日平均度數
//				mapdata.put("AVGQTY", data.getAvgQty() + " 度");//本期日平均度數
//				mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
				mapdata.put("LASTAVGQTY", data.getLastAvgQty()); //去年同期日平均度數
				mapdata.put("AVGQTY", data.getAvgQty());//本期日平均度數
				mapdata.put("THISQTY", (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())) ); //本期用水度數
				mapdata.put("LASTQTY", (NumberUtils.toInt(data.getLAST_REAL_QTY().trim())));//上期實用度數
				mapdata.put("LASTDAVGQ", data.getLAST_DAVGQ()); //上期日平均用水度數
				mapdata.put("LYEARREALQTY", (NumberUtils.toInt(data.getLYEAR_REAL_QTY().trim())));//去年同期實用度數

				
				
				mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
				mapdata.put("TOTAL2",thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
				String registration_number = PdfTool.genRegistrationNumber(data.getWaterNO());
				mapdata.put("WATERINVONO", registration_number);

			
				
				//add by jenny 20150710 
				//ADD BY JENNY 20151027 新增旗山所(7A)也要顯示宣導語
	            String tmpwaterno=data.getWaterNO().toString();

	            
	            EbillService ebillService = SpringTool.getBean(EbillService.class);
	    		ebillService.init(DBDataTool.getInstance(), dbutil);
	            //String lastYyyyMm = ebillService.getLastBillYyyyMm(data.getWaterNO(), yyyyYearstr);
	    		Map<String, String> lastBillInfo = ebillService.getLastBillInfo(data.getWaterNO(),yyyyYearstr);
	    		String lastWaterno = lastBillInfo.get("WATERNO");
	    		String lastYyyyMm = lastBillInfo.get("YYMM");
	    		mapdata.put("LASTYYMM","");//新水號無前期
	        	mapdata.put("INVOICENO","");//新水號無前期
	            if(!lastYyyyMm.equals("")){
	            	String lastRocYM = DateTool.adYMtoRocYM(lastYyyyMm);
	            	Map<String, Object> rtntyymms=DBDataTool.getInstance().getInvoiceNoByWaternoYM(lastRocYM,lastWaterno);
	            	String invoiceM = "";
	            	mapdata.put("LASTYYMM","");
	            	mapdata.put("INVOICENO","");
	            	if(!StringUtils.trimToEmpty(ObjectUtils.toString(rtntyymms.get("INVOICENO"))).equals("")&&!StringUtils.trimToEmpty(ObjectUtils.toString(rtntyymms.get("PAIDDT"))).equals("")){
	            		mapdata.put("INVOICENO",ObjectUtils.toString(rtntyymms.get("INVOICENO")));
	            		
	            		

	            		String padditDay = ObjectUtils.toString(rtntyymms.get("PAIDDT"));
	            		switch(String.format("%02d",Integer.parseInt(padditDay.substring(3,5).trim()))){
	            		case "01":
	            			invoiceM = "01-02";
	            			break;
	            		case "02":
	            			invoiceM = "01-02";
	            			break;
	            		case "03":
	            			invoiceM = "03-04";
	            			break;
	            		case "04":
	            			invoiceM = "03-04";
	            			break;
	            		case "05":
	            			invoiceM = "05-06";
	            			break;
	            		case "06":
	            			invoiceM = "05-06";
	            			break;
	            		case "07":
	            			invoiceM = "07-08";
	            			break;
	            		case "08":
	            			invoiceM = "07-08";
	            			break;
	            		case "09":
	            			invoiceM = "09-10";
	            			break;
	            		case "10":
	            			invoiceM = "09-10";
	            			break;
	            		case "11":
	            			invoiceM = "11-12";
	            			break;
	            		case "12":
	            			invoiceM = "11-12";
	            			break;
	            		}
	            		mapdata.put("LASTYYMM",padditDay.substring(0,3) + "<span>年</span>" + invoiceM + "<span>月</span>");
	            	}
	            }
	            //UPDATE BY Vincent 20160317 END:001
				
				String receiptNumber = StringUtils.leftPad(data.getChagBillNO1().trim(), 2, "0") + StringUtils.leftPad(data.getChagBillNO2(), 8, "0");
	            if (StringUtils.isEmpty(data.getWrbaInvoNO())) {
	            	mapdata.put("RECEIPTNUMBER",receiptNumber); //收據號碼
	            	mapdata.put("AMT1",""); 
	            	mapdata.put("AMT1DESC",""); 
	            	mapdata.put("AMT2",""); 
	            	mapdata.put("AMT2DESC",""); 
	            	mapdata.put("AMT3",""); 
	            	mapdata.put("AMT3DESC",""); 
	            	mapdata.put("thiscancel","本聯作廢");
	            } else {
	            	mapdata.put("RECEIPTNUMBER",receiptNumber); //收據號碼
	            	mapdata.put("AMT1DESC","應稅銷售額 "); 
	            	mapdata.put("AMT1",subamtnotax);
	            	//mapdata.put("AMT1",thousandDecimalFormat.format(Math.round(Bbasicfee.add(Bfee).add(Ballowance).add(Bpbfee).doubleValue())) + "元"); 
	            	mapdata.put("AMT2DESC","營業稅"); 
	            	mapdata.put("AMT2",thousandDecimalFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元"); 
	            	mapdata.put("AMT3DESC","代徵費用"); 

	            	if( Integer.parseInt(yyyyYearstr) >= 201904){
	            		mapdata.put("AMT3",thousandDecimalFormat.format((NumberUtils.toInt(data.getDirty().trim())
		            		+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
		            		+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
		            		+ NumberUtils.toInt(data.getMB_AMT()) + NumberUtils.toInt(data.getMB_AMT2().trim())
		            		+ NumberUtils.toInt(data.getLevy().trim()))) + "元"); 
	            	}else{
		            	mapdata.put("AMT3",thousandDecimalFormat.format((NumberUtils.toInt(data.getDirty().trim())
			            		+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
			            		+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
			            		+ NumberUtils.toInt(data.getLevy().trim()))) + "元"); 	
	            	}
	            	mapdata.put("thiscancel","");
	            }
				
				List<String> discountlists = new ArrayList<String>();
				
				if (!StringUtils.defaultIfBlank(data.getTempNote(), "").equals("")) {
					discountlists.add(data.getTempNote());
	            }
				//99年7月加入宣導訊息
	            if ("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta())) {
	            	String tmpamt="";
	            	int afterConvert; 
	            	if ("電子帳單回饋金".equals(data.getAllowanceSta())){tmpamt=data.getAllowance();}
	            	else{tmpamt=data.getAllow2();}
	            	
	            	afterConvert = Integer.parseInt(tmpamt); //判斷回饋金 有可能有AllowanceSta=電子帳單回饋金 但getAllow2=0會產生錯誤
	            	if(afterConvert==0){
	            		discountlists.add("");
	            	}else{

	            		discountlists.add("感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金"+Math.abs(afterConvert)+"元，直接於本期水費折抵。");
						
	            	}
	            }	            
	            
	            
				mapdata.put("discountlists", discountlists);
				
				List<String> memolists = new ArrayList<String>();
				
				if (StringUtils.isNotEmpty(data.getWrbaInvoNO())) {
					memolists.add("備註：本通知內容如有爭議時，以本公司用水當地服務（營運）所所存為準。");					
	            } else {
	            	memolists.add("備註：1.營業稅分別併入各項費用欄內，依法不另列示。");
	            	memolists.add("　　　2.本通知內容如有爭議時，以本公司用水當地服務（營運）所所存為準。");

	                //99年7月加入宣導訊息
	                if ("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta())) {
	                    // 折扣金有兩類(扣繳戶扣5元，其餘扣3元
	                	String showMsgtmp="";
	                	int afterConvert2=0; 
	                	if ("電子帳單回饋金".equals(data.getAllowanceSta())){
	                		showMsgtmp=data.getAllowance().trim().substring(1, 2);
	                	}else{
	                		//showMsgtmp=data.getAllow2().trim().substring(1, 2);
	                    	if(Integer.parseInt(data.getAllow2().trim())==0){
	                    		afterConvert2=1;
	                    	}else{
	                    		showMsgtmp=data.getAllow2().trim().substring(1, 2);
	                    	}
	                		
	                	}
	                	
	                    LOG.info("水號=" + data.getWaterNO() + ",回饋金: " + data.getAllowance().trim() + "->" 
	                    		+  showMsgtmp);
	                    if(afterConvert2==1){
	                    	memolists.add("");
	                    }else{

	                    	memolists.add("　　　3.感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金3元，直接於本期水費折抵。");
	                    }

	                    
	                }
	            }
				mapdata.put("memolists", memolists);
				
				
				List<String> tbcol1 = new ArrayList<String>();
				
				tbcol1.add("用水種別 ");
				tbcol1.add("工作區 ");
				tbcol1.add("水表口徑 ");
				//tbcol1.add("本公司營利事業統一編號 ");
				//tbcol1.add("收據號碼 ");        
				tbcol1.add("本期繳費起始日");
				tbcol1.add("下期繳費起始日");
				tbcol1.add("本期抄表日期 ");
				tbcol1.add("下期抄表日期 ");				
				tbcol1.add("本期指針數 ");
				tbcol1.add("上期指針數 ");
				tbcol1.add("註記");
				tbcol1.add("期別");
				
				
				
				if(StringUtils.isNotEmpty(data.getMasterNO()) && ( data.getBRANCH().trim().equals("1") || data.getBRANCH().trim().equals("2"))){//總表編號
					tbcol1.add("用水度數 ");
					tbcol1.add("分攤/副表度數 ");
				}		
					
				
//				if(!pdfutil.trans_zero("公共用水分攤戶數 ", data.getChagBillScount()).equals("")){
//					tbcol1.add("公共用水分攤戶數 ");
//				}
				
				if(!pdfutil.trans_zero("公共用水分攤戶數 ", data.getChagBillScount()).equals("")){
					tbcol1.add("公共用水分攤度數/戶數 ");
				}

				
				tbcol1.add("本期實用度數");
//				tbcol1.add("上期實用度數");
				if(!pdfutil.trans_zero("本期總表指針數 ", "" + NumberUtils.toInt(data.getMasterReading())).equals("")){
					tbcol1.add("本期總表指針數 ");
				}
				if(!pdfutil.trans_zero("上期總表指針數 ", "" + NumberUtils.toInt(data.getMasterLastread())).equals("")){
					tbcol1.add("上期總表指針數 ");
				}
				
				
			    //20210407 副表本期指針,副表上期指針
		        if( Integer.parseInt(yyyyYearstr) >= 202105){
					if(!pdfutil.trans_zero("副表本期指針", "" + NumberUtils.toInt(data.getSREAD1())).equals("")){
						tbcol1.add("副表本期指針");
					}
					if(!pdfutil.trans_zero("副表上期指針", "" + NumberUtils.toInt(data.getSREAD2())).equals("")){
						tbcol1.add("副表上期指針");
					}
		        }
				
				
//				if(!pdfutil.trans_zero("分攤總度數 ", "" + NumberUtils.toInt(data.getMasterSqty())).equals("")){
//					tbcol1.add("分攤總度數 ");
//				}	            
	            
				if(!pdfutil.trans_zero("契約度數 ", data.getContract().trim()).equals("")){
					tbcol1.add("契約度數 ");
				}
				
				 //20190716 新增分表總實用度數  只有總表才有
		       	if( data.getBRANCH().trim().equals("1")){
		       		if(!pdfutil.trans_zero("分表總實用度數  ", "" + NumberUtils.toInt(data.getSTQTY())).equals("")){
			       		tbcol1.add("分表總實用度數 ");
			    	}
				}
				
		       	
				if((25-tbcol1.size())>0){
					for (int i=25-tbcol1.size();i>0;--i){
						tbcol1.add(" ");
					}
				}
				mapdata.put("tbcol1", tbcol1);

				
				List<String> tbcol2 = new ArrayList<String>();
				
				String waterType = data.getWaterType().toString().trim();
	            String waterTypeDesc = PropertiesTool.getWaterTypeDesc(waterType);
	            tbcol2.add(waterTypeDesc);
	            
				if(Integer.parseInt(yyyyYearstr) >= 201903){//20190220更新工作區為水表表號
					tbcol2.add(data.getMno());
				}else{
					tbcol2.add(data.getChagWorkArea1() + data.getChagWorkArea2());
				}
	            //tbcol2.add(data.getChagWorkArea1() + data.getChagWorkArea2());
	            tbcol2.add("" + NumberUtils.toInt(data.getDiameter()));
	            //tbcol2.add("" + PdfTool.genRegistrationNumber(data.getWaterNO()));
	            //tbcol2.add(receiptNumber);
	            
	            //20121222 增加本期繳費起始日,此行以下的同區塊pdf資訊的y軸都往下
	            tbcol2.add(data.getChagCurnDate());
	            
	            tbcol2.add(DateTool.changeADDateToRocDateDisplay(data.getNextBillDate(), "/"));
	            tbcol2.add(DateTool.changeRocDateDisplay(data.getMeteredDate(), "/"));
	            tbcol2.add(DateTool.changeADDateToRocDateDisplay(data.getNextMeteredDate(), "/"));
	            
	            
	          //ADD BY JENNY 2013.2.23 文邦說本期指針數及上期指針數如為0時，改為印出空白即可 start
	            // TODO add 3, 4, 7, or 9的話，就印空白 by Ca 20140630.
	            if (NumberUtils.toInt(data.getReading()) == 0 || NumberUtils.toInt(data.getMstus()) == 3 || 
	            		NumberUtils.toInt(data.getMstus()) == 4 || NumberUtils.toInt(data.getMstus()) == 7 ||
	            		NumberUtils.toInt(data.getMstus()) == 9) {
	            	tbcol2.add("");
	        	} else {
	        		tbcol2.add("" + NumberUtils.toInt(data.getReading()));
	    		}
	            
	            if (NumberUtils.toInt(data.getLastread())==0){tbcol2.add("");}
	            else {tbcol2.add("" + NumberUtils.toInt(data.getLastread()));}
	          //ADD BY JENNY 2013.2.23 文邦說本期指針數及上期指針數如為0時，改為印出空白即可 end
	            
	            //Johnson 102年1月後加入欄位
	            tbcol2.add(PdfTool.genNoteMark(data.getMstus()));
	            
	            double c_period = 0;
	            if (StringUtils.isNotEmpty(data.getChagWatrPeriod())) {
	                c_period = Double.valueOf(data.getChagWatrPeriod()) * 0.5;
	            }
	            tbcol2.add("" + c_period + "");
				
	            if(StringUtils.isNotEmpty(data.getMasterNO()) && ( data.getBRANCH().trim().equals("1") || data.getBRANCH().trim().equals("2"))){//總表編號
	            	 tbcol2.add(data.getToqty().trim());
		      		 tbcol2.add(data.getSqty().trim());
				}	
	            

//	            if(!pdfutil.trans_zero(data.getChagBillScount(), data.getChagBillScount()).equals("")){
//	            	tbcol2.add(data.getChagBillScount().trim());
//	            }

	            
	            if(!pdfutil.trans_zero(data.getChagBillScount(), data.getChagBillScount()).equals("")){
	            	tbcol2.add(data.getM_SHSQTY()+"/"+data.getChagBillScount());
	            }
	            
//	            tbcol2.add("" + (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())));
	            

	            tbcol2.add("" + (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())));
	            
//	            tbcol2.add("" + (NumberUtils.toInt(data.getLastChagRealScale().trim())));
	            
	            if(!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterReading()), "" + 
	            		NumberUtils.toInt(data.getMasterReading())).equals("")){
	            	tbcol2.add("" + NumberUtils.toInt(data.getMasterReading()));
	            }
	            
	            if(!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterLastread()), "" + 
	            		NumberUtils.toInt(data.getMasterLastread())).equals("")){
	            	tbcol2.add("" + NumberUtils.toInt(data.getMasterLastread()));
	            }
	            
	            
			    //20210407 副表本期指針,副表上期指針
			    if( Integer.parseInt(yyyyYearstr) >= 202105){
			        if (!pdfutil.trans_zero("" + NumberUtils.toInt(data.getSREAD1()), "" + NumberUtils.toInt(data.getSREAD1())).equals("")){
			        	tbcol2.add("" + NumberUtils.toInt(data.getSREAD1()));
			        }
			        if (!pdfutil.trans_zero("" + NumberUtils.toInt(data.getSREAD2()), "" + NumberUtils.toInt(data.getSREAD2())).equals("")){
			        	tbcol2.add("" + NumberUtils.toInt(data.getSREAD2()));
			        }
			    }
	            
	            
	            if (!pdfutil.trans_zero(data.getContract().trim(), data.getContract().trim()).equals("")){
	            	tbcol2.add(data.getContract().trim());
	            }
						       	
		       	
	            if((25-tbcol2.size())>0){
					for (int i=25-tbcol2.size();i>0;--i){
						tbcol2.add(" ");
					}
				}
	            mapdata.put("tbcol2", tbcol2);
	            
	            List<String> tbcol3 = new ArrayList<String>();

	            tbcol3.add("水費項目小計");
	            if (!pdfutil.trans_zero("基本費", data.getBasicfee().trim()).equals("")){
	            	tbcol3.add("基本費");
	            }
	            if (!pdfutil.trans_zero("用水費", data.getFee().trim()).equals("")){
	            	tbcol3.add("用水費");
	            }

	            

	            if (!pdfutil.trans_zero("退還/追繳水費", data.getPbfee().trim()).equals("")){
	            	
	            	if(Integer.parseInt(data.getPbfee().trim()) > 0){
	            		tbcol3.add("加收水費");
	            	}else{
	            		tbcol3.add("退還水費");
	            	}
	            }
	            
	            if (!pdfutil.trans_zero("水源保護區優惠", data.getFeedback().trim()).equals("")){
	            	tbcol3.add("水源保護區優惠");
	            }
	            if (!pdfutil.trans_zero("營業稅", data.getTax().trim()).equals("")){
	            	tbcol3.add("營業稅");
	            }
	            if (!pdfutil.trans_zero("補繳遲延繳付費", data.getDelayfee().trim()).equals("")){
	            	tbcol3.add("補繳遲延繳付費");
	            }
	            

	            if (!pdfutil.trans_zero(data.getAllowanceSta(), data.getAllowance().trim()).equals("")){
	            	tbcol3.add(data.getAllowanceSta().trim());
	            }
//	            if (!pdfutil.trans_zero(data.getAllow2Sta(), data.getAllow2().trim()).equals("")){
//	            	tbcol3.add(data.getAllow2Sta().trim());
//	            }
	            if (!pdfutil.trans_zero(data.getAllow2Sta(), data.getAllow2().trim()).equals("")){
	            	//tbcol3.add(data.getAllow2Sta().trim());
	            	if(Math.abs(NumberUtils.toInt(data.getAllow2().trim()))<=3){
	            		tbcol3.add(data.getAllow2Sta().trim());
	            	}else if(Math.abs(NumberUtils.toInt(data.getAllow2().trim()))>3){
//	            		tbcol3.add("電子帳單/行動支付回饋金");
	            		//20220913 112電子帳單回饋金變5元
	            		tbcol3.add(data.getAllow2Sta().trim());
	            	}
	            }
	            //create_table("補助費",FontChinese_10,Writer,203,429);
	            // Middle.
	            
	            int memocount=0;
	          //add by jenny 5月份臨時增加用水費折扣金額備註 2015-05-28 start
	            if (!pdfutil.trans_zero("水費折扣", data.getDiscountamt().trim()).equals("")){
	            	if (tbcol3.size()<10){
	            		for (int i=10-tbcol3.size();i>0;--i){
							tbcol3.add(" ");
						}
	            	}
	            	memocount++;
	            	tbcol3.add("備註"+Integer.toString(memocount)+":用水費金額已");
		            tbcol3.add("折扣金額」"+data.getDiscountamt().trim());
		            
	            }	
	            
	            //add by jenny 5月份臨時增加用水費折扣金額備註 2015-05-28 end
	            
				
				
        		if( !pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getPbfee().trim())) + "元", data.getPbfee().trim()).equals("") && EbillService.checkPromoteWaterno(data.getWaterNO(),"202011")&&  Integer.parseInt(yyyyYearstr) >= 202008 && Integer.parseInt(yyyyYearstr) <= 202012 ){

					if (tbcol3.size()<10){
	            		for (int i=10-tbcol3.size();i>0;--i){
							tbcol3.add(" ");
						}
	            	}
					memocount++;
		
					tbcol3.add("註"+Integer.toString(memocount)+":加退水費內含");//20200515 修改備註為註
					tbcol3.add("回饋金3元");//20200515 修改備註為註  
        		}
				
				
	            
				/* <START> 停水扣減. 根據pdfData去決定是否要印停水扣減，故不限區. */
				String cutoffDedcFeeWtTax = data.getWaterCutoffDedcFeeWtTax();
				if (!cutoffDedcFeeWtTax.equals("0") // Default property value
						&& !cutoffDedcFeeWtTax.equals("") // Got data but no value.
						) {
					if (tbcol3.size()<10){
	            		for (int i=10-tbcol3.size();i>0;--i){
							tbcol3.add(" ");
						}
	            	}
					memocount++;
					//UPDATE BY Vincent 20160322 4月份6區帳單扣減項目 START:001 ADD LINE:550~553、556
					//UPDATE BY Vincent 20160815 END:001
				}
	            
//	            if((18-tbcol3.size())>0){
//					for (int i=18-tbcol3.size();i>0;--i){
//						tbcol3.add(" ");
//					}
//				}
	            if((16-tbcol3.size())>0){
					for (int i=16-tbcol3.size();i>0;--i){
						tbcol3.add(" ");
					}
				}
	            

	            // Right for 說明字樣.
//	            tbcol3.add(" Subtotal Levy");	//ADD BY Vincent 新增英文欄位 20160620
//	            tbcol3.add("◎代徵費用小計金額"); // 第一欄固定會顯示
	            tbcol3.add("代徵費用小計"); // 第一欄固定會顯示
	            // by Ca 20150312 : 污水處理費改為污水下水道使用費
//	            if (!pdfutil.trans_zero("污水下水道使用費", data.getDirty().trim()).equals("")){
//	            	tbcol3.add("污水下水道使用費");
//	            }
	            
	            

			    if( Integer.parseInt(yyyyYearstr) >= 201907){
					if (!pdfutil.trans_zero("加退污水下水道使用費", data.getFOUL_AMT2()).equals("")){
		            	if(Integer.parseInt(data.getFOUL_AMT2().trim()) > 0){
		            		tbcol3.add("加收污水下水道使用費");
		            	}else{
		            		tbcol3.add("退還污水下水道使用費");
		            	}
			        }
			    }
	            
	            
	            if (!pdfutil.trans_zero("清除處理費", data.getServ().trim()).equals("")){
	            	tbcol3.add("清除處理費");
	            }
//	            if (!pdfutil.trans_zero("水源保育費", data.getChagBaoyu()).equals("")){
//	            	tbcol3.add("水源保育費");
//	            }
	            if (!pdfutil.trans_zero("水源保育與回饋費", data.getChagBaoyu()).equals("")){
	            	tbcol3.add("水源保育與回饋費");
	            }
//	            if (!pdfutil.trans_zero("加退水源保育費", data.getChagBaoyu2()).equals("")){
//	            	tbcol3.add("加退水源保育費");
//	            }
//	            if (!pdfutil.trans_zero("加退水源保育與回饋費", data.getChagBaoyu2()).equals("")){
//	            	tbcol3.add("加退水源保育與回饋費");
//	            }
	            
	            //20201014 有加退項，加項顯示「加收」、減項顯示「退還」
	            if (!pdfutil.trans_zero("加退水源保育與回饋費", data.getChagBaoyu2()).equals("")){
	            	
	            	if(Integer.parseInt(data.getChagBaoyu2().trim()) > 0){
	            		tbcol3.add("加收水源保育與回饋費");
	            	}else{
	            		tbcol3.add("退還水源保育與回饋費");
	            	}
	            	
	            }
	            

	            if (!pdfutil.trans_zero("加退清除處理費", data.getPbserv().trim()).equals("")){
	            	
	            	if(Integer.parseInt(data.getPbserv().trim()) > 0){
	            		tbcol3.add("加收清除處理費");
	            	}else{
	            		tbcol3.add("退還清除處理費");
	            	}
	            }
	            
	            

	            if( Integer.parseInt(yyyyYearstr) >= 201904){
					if (!pdfutil.trans_zero("工程改善費", data.getMB_AMT()).equals("")){
			            tbcol3.add("工程改善費");
			        }
//		            if (!pdfutil.trans_zero("加退工程改善費", data.getMB_AMT2()).equals("")){
//		          		tbcol3.add("加退工程改善費");
//		            }
					

					if (!pdfutil.trans_zero("加退工程改善費", data.getMB_AMT2()).equals("")){
						
		            	if(Integer.parseInt(data.getMB_AMT2().trim()) > 0){
		            		tbcol3.add("加收工程改善費");
		            	}else{
		            		tbcol3.add("退還工程改善費");
		            	}
					}
					
	            }
	            // 固定補助費說明 desc from PdfDataOnOrgProof.java
	            if (!pdfutil.trans_zero(
	            		data.getAllowanceSta(), data.getLevy().trim()).equals("")){
	            	tbcol3.add(data.getAllowanceSta());
	            }
	            
	            if((25-tbcol3.size())>0){
					for (int i=25-tbcol3.size();i>0;--i){
						tbcol3.add(" ");
					}
				}
	            
	            mapdata.put("tbcol3", tbcol3);
	            
	            
	            // Right.
	             
	            
	            
	            
	            String subamt1=thousandDecimalFormat.format(Math.round(totamt.subtract(subamt2).doubleValue())) + "元";     
	          
	            
	            List<String> tbcol4 = new ArrayList<String>();
	            
	            //tbcol4.add(thousandDecimalFormat.format(Math.round(Bbasicfee.add(Bfee).add(Bpbfee).add(Bfeedback).add(Btax).add(Bdelayfee).add(Ballowance).add(Ballow2).doubleValue())) + "元");
//	            tbcol4.add("");		//ADD BY Vincent 20160620
	            tbcol4.add(subamt1);
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bbasicfee) + "元", data.getBasicfee().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Bbasicfee) + "元");
	            }
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bfee) + "元", data.getFee().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Bfee) + "元");
	            }
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getPbfee().trim())) + "元", data.getPbfee().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getPbfee().trim())) + "元");
	            }
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bfeedback) + "元", data.getFeedback().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Bfeedback) + "元");
	            }
	            if (!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元", data.getTax().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元");
	            }
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bdelayfee) + "元", data.getDelayfee().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Bdelayfee) + "元");
	            }
	            

	            if( Integer.parseInt(yyyyYearstr) >= 201904){
				    if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bopamt) + "元", data.getOP_AMT().trim()).equals("")){
				    	tbcol4.add(waterSubamtDetailFormat.format(Bopamt) + "元");
				    }
				    if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bopamt2) + "元", data.getOP_AMT2().trim()).equals("")){
				    	tbcol4.add(waterSubamtDetailFormat.format(Bopamt2) + "元");
				    }
	            }
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Ballowance) + "元", data.getAllowance().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Ballowance) + "元");
	            }
	            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Ballow2) + "元", data.getAllow2().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Ballow2) + "元");
	            }
	            
	          //add by jenny 5月份臨時增加用水費折扣金額備註 2015-05-28 start
	            if (!pdfutil.trans_zero("水費折扣", data.getDiscountamt().trim()).equals("")){
	            	if (tbcol4.size()<10){
	            		for (int i=10-tbcol4.size();i>0;--i){
	            			tbcol4.add(" ");
						}
	            	}
	            	
	            	tbcol4.add("扣除「節水優惠");
	            	tbcol4.add("元");
		            
	            }	            
	            //add by jenny 5月份臨時增加用水費折扣金額備註 2015-05-28 end
	            
	            

	        	if (!cutoffovidDedcFeeWtTax.equals("0") // Default property value
						&& !cutoffovidDedcFeeWtTax.equals("") // Got data but no value.
						) {
					if (tbcol4.size()<10){
	            		for (int i=10-tbcol4.size();i>0;--i){
	            			tbcol4.add(" ");
						}
	            	}
					//if((yyyyYearstr.equals("202005") || yyyyYearstr.equals("202006") || yyyyYearstr.equals("202007") || yyyyYearstr.equals("202008") || 
				}     
	        	//add by jenny 5月份增加武漢肺炎折扣金額備註2020-04-28 end
	            
	            
	        	
	        	
				//add by jenny 5月份增加武漢肺炎折扣金額備註2020-04-28 end
				
				
        		if(  !pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getPbfee().trim())) + "元", data.getPbfee().trim()).equals("")&& EbillService.checkPromoteWaterno(data.getWaterNO(),"202011")&&  Integer.parseInt(yyyyYearstr) >= 202008 && Integer.parseInt(yyyyYearstr) <= 202012 ){

					if (tbcol4.size()<10){
	            		for (int i=10-tbcol4.size();i>0;--i){
	            			tbcol4.add(" ");
						}
	            	}

		         }
	            
	        	
	        	
	        	
    			
				/* <START> 停水扣減. 根據pdfData去決定是否要印停水扣減，故不限區. */
				
				if (!cutoffDedcFeeWtTax.equals("0") // Default property value
						&& !cutoffDedcFeeWtTax.equals("") // Got data but no value.
						) {
					if (tbcol4.size()<10){
	            		for (int i=10-tbcol4.size();i>0;--i){
	            			tbcol4.add(" ");
						}
	            	}
					//UPDATE BY Vincent 20160322 4月份6區帳單扣減項目 START:002 ADD LINE:663~665、667
				
					//UPDATE BY Vincent 20160815 END:002
				}           
	            

	            
//	            if((18-tbcol4.size())>0){
//					for (int i=18-tbcol4.size();i>0;--i){
//						tbcol4.add(" ");
//					}
//				}
	            if((16-tbcol4.size())>0){
					for (int i=16-tbcol4.size();i>0;--i){
						tbcol4.add(" ");
					}
				}
	            // Middle for money.
	            
	            
	            // #region - Right for money.
	            
	            LOG.info("dirty=" + data.getDirty() + ", serv=" + data.getServ() + ", chag_chag_baoyu=" + data.getChagBaoyu()
	            		+ ", chag_chag_baoyu2=" + data.getChagBaoyu2() + ", pbserv=" + data.getPbserv() + ", levy=" + data.getLevy());
//	            tbcol4.add("");		//ADD BY Vincent 20160620
//	            tbcol4.add(thousandDecimalFormat.format((NumberUtils.toInt(data.getDirty().trim())
//	            		+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
//	            		+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
//	            		+ NumberUtils.toInt(data.getLevy().trim()))) + "元");
	            

	            
	            
	            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getServ().trim())) + "元", 
	            		data.getServ().trim()).equals("")){
	            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getServ().trim())) + "元");
	            }
	            // 水源保育費
	            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getChagBaoyu())) + "元",
	        		data.getChagBaoyu()).equals("")){
	            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getChagBaoyu())) + "元");
	            }
	            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getChagBaoyu2())) + "元", 
	            		data.getChagBaoyu2()).equals("")){
	            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getChagBaoyu2())) + "元");
	            }
	            //create_moneytable(func.trans_zero(df.format(NumberUtils.toInt(allowance.trim())) + "元", allowance.trim()), FontChinese_10, Writer, 435, 573 - func.move_textTable(2, tables, allowance));
	            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getPbserv().trim())) + "元", 
	            		data.getPbserv().trim()).equals("")){
	            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getPbserv().trim())) + "元");
	            }
	            
	            
	            if((25-tbcol4.size())>0){
					for (int i=25-tbcol4.size();i>0;--i){
						tbcol4.add(" ");
					}
				}
	            
	            mapdata.put("tbcol4", tbcol4);
	            // Right.
	            
	            
	            if(!pdfutil.trans_zero("◎本期預繳金額", data.getLastfee().trim()).equals("")){
	            	mapdata.put("stbcol1desc", "◎本期預繳金額");
	            	mapdata.put("stbcol1",thousandDecimalFormat.format(NumberUtils.toInt(data.getLastfee().trim()) - NumberUtils.toInt(data.getPrefee().trim())) + "元");
	            }else{
	            	mapdata.put("stbcol1","");
	            	mapdata.put("stbcol1desc","");
	            }
	            if (!pdfutil.trans_zero("上期預繳餘額", data.getLastfee().trim()).equals("")){
	            	mapdata.put("stbcol2desc", "上期預繳餘額");
	            	mapdata.put("stbcol2",thousandDecimalFormat.format(NumberUtils.toInt(data.getLastfee().trim())) + "元");
	            }else{
	            	mapdata.put("stbcol2","");
	            	mapdata.put("stbcol2desc","");
	            }
	            if (!pdfutil.trans_zero("本期扣繳後預繳餘額", data.getLastfee().trim()).equals("")){
	            	mapdata.put("stbcol3desc", "本期扣繳後預繳餘額");
	            	mapdata.put("stbcol3",thousandDecimalFormat.format(NumberUtils.toInt(data.getPrefee().trim())) + "元");
	            }else{
	            	mapdata.put("stbcol3","");
	            	mapdata.put("stbcol3desc","");
	            }
	            
	            
	            

	            mapdata.put("WATERNO", data.getWaterNO());
	            mapdata.put("WATERNO2", data.getWaterNoOfStation()+"-"+data.getWaterNoOfSerialNO()+"-"+data.getWaterNoOfCheck());
	            
	            mapdata.put("WNSTATION", data.getWaterNoOfStation());
	            mapdata.put("WNSERIALNO", data.getWaterNoOfSerialNO());
	            mapdata.put("WNCHECK", data.getWaterNoOfCheck());
	            
	            mapdata.put("CO2", (int) Math.round((Double.parseDouble(data.getToqty().trim()) + 
	            		Double.parseDouble(data.getSqty().trim())) * PropertiesTWCEBill.getCO2()));
	            
	            String htmlfile=ebillPath+ File.separator +"html"+ File.separator + imgfilename+ ".html";
	            File input = new File(htmlfile);
	            
	            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(input),"UTF-8"));
				
				template.process(mapdata, out);
				
				out.flush();
	            
				convertHtml2pdf(htmlfile,outputStream);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        
        
        
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		        

	}
	

	private void genBarcode_3(String title, String code, String filepath) throws Exception{
		String s = title;       
		net.sourceforge.barbecue.Barcode barcode = BarcodeFactory.createCode39(code, false);
		barcode.setBarWidth(2);
        barcode.setBarHeight(30);
		barcode.setDrawingText(false);
		
        File f = new File(filepath);
        if (f.exists()){
			//檔案刪除
			f.delete();            
		}

        BufferedImage image = new BufferedImage(barcode.getWidth(),barcode.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
        Graphics2D g = (Graphics2D) image.getGraphics();
       
        
        g.setBackground(Color.white);
        g.fillRect(0,0,barcode.getWidth(),barcode.getHeight());		
        g.setColor(Color.BLACK);


        barcode.draw(g, 0, 0);

        //用ImageIO來存檔
        ImageIO.write(image,"JPG",f);	
	}
	
	public void convertHtml2pdf(String src, OutputStream outputStream) throws IOException {

        PdfWriter pdfWriter = new PdfWriter(outputStream);
        
        PdfDocument pdf = new PdfDocument(pdfWriter);
        pdf.setDefaultPageSize(new PageSize(PageSize.A4));

        
	    ConverterProperties properties = new ConverterProperties();

	    FontProvider fontProvider = new DefaultFontProvider();

	    
	    properties.setFontProvider(fontProvider);
	   
	    HtmlConverter.convertToPdf(new FileInputStream(src), pdf, properties);

         System.out.println("down");	   

	}
	
	private void genBarcode(String title, String code, String filepath) throws Exception{
        String s = title;       
		net.sourceforge.barbecue.Barcode barcode = BarcodeFactory.createCode39(code, false);
		barcode.setBarWidth(0);
        barcode.setBarHeight(20);
		barcode.setDrawingText(false);
		
        File f = new File(filepath);
        if (f.exists()){
			//檔案刪除
				f.delete();            
		}

        BufferedImage image = new BufferedImage(barcode.getWidth(),barcode.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
        Graphics2D g = (Graphics2D) image.getGraphics();
       
        
        g.setBackground(Color.white);
        g.fillRect(0,0,barcode.getWidth(),barcode.getHeight());		
        g.setColor(Color.BLACK);

        barcode.draw(g, 0, 0);

        //用ImageIO來存檔
        ImageIO.write(image,"JPG",f);	
}
	private void genBarcodetype2(String title, String code, String filepath) throws Exception{
        //String s = title;   
		double scaleRate = 0.8;
		File reScaleJpg = getReScaleJpg(filepath,scaleRate,code);
		
		//net.sourceforge.barbecue.Barcode barcode = BarcodeFactory.createCode39(code, false);
        //barcode.setDrawingText(false);
        
        //File f = new File(filepath);

        //BufferedImage image = new BufferedImage(barcode.getWidth(),barcode.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        
        //Graphics2D g = (Graphics2D) image.getGraphics();        
        
        //g.setBackground(Color.white);
        //g.fillRect(0,0,barcode.getWidth(),barcode.getHeight());		
        //g.setColor(Color.BLACK);

        ////測量文字中心點
        ////FontRenderContext fc = g.getFontRenderContext();
        
        ////g.drawString(s,0,16);

        ////測試時發現createEAN13這種barcode的位置比較奇怪，所以用座標位移來畫
        //g.translate(0,20);
        //barcode.draw(g, 0, 0);

        ////用ImageIO來存檔
        //ImageIO.write(image,"JPG",f);	
}
	
	private void genQRcode(String filepath, String qrCodeText) throws WriterException, IOException {
		// Create the ByteMatrix for the QR-Code that encodes the given String
        File f = new File(filepath);
        if (f.exists()){
			//檔案刪除
				f.delete();            
		}
		Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
//		BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText,
//				BarcodeFormat.QR_CODE, 210, 210, hints);
		BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText,
				BarcodeFormat.QR_CODE, 220, 220, hints);
		// Make the BufferedImage that are to hold the QRCode
		int matrixWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
				BufferedImage.TYPE_INT_RGB);
		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, matrixWidth, matrixWidth);
		// Paint and save the image using the ByteMatrix
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < matrixWidth; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		ImageIO.write(image, "png", f);
	}
	
	
	private static File getReScaleJpg(String jpgFilePath,double scaleRate,String barcodeID) {
        File retFile = null;
        try {
            
            //Barcode barcode  = BarcodeFactory.createCode39("086240205523",true);
            Barcode barcode  = BarcodeFactory.createCode39(barcodeID,false);
            barcode.setDrawingText(false);
            barcode.setBarWidth(0);
            barcode.setBarHeight(30);
            BufferedImage image = new BufferedImage(orgBestWidth, orgBestHigth, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d1 = (Graphics2D) image.getGraphics();
            g2d1.setColor(Color.WHITE);
            g2d1.fillRect(0,0,orgBestWidth,orgBestHigth);
            //barcode .draw(g2d1, 10, 5);
            barcode .draw(g2d1, 0, 0);
            retFile =  new File(jpgFilePath);
            if (retFile.exists()){
    			//檔案刪除
            	retFile.delete();            
    		}
            ImageIO.write(image, "jpg", retFile);
            
            //// create reScaleJpg File by scaleRate
            //int width = image.getWidth();
            //int height = image.getHeight();
            //java.awt.Image scaledImage = image.getScaledInstance((int)(width*scaleRate), (int)(height*scaleRate), java.awt.Image.SCALE_DEFAULT);
            //BufferedImage bi2 = new BufferedImage((int)(width*scaleRate), (int)(height*scaleRate), BufferedImage.TYPE_BYTE_BINARY);
            //Graphics2D g2d2 = (Graphics2D) bi2.getGraphics();
            //g2d2.setColor(Color.WHITE);
            //g2d2.fillRect(0,0, (int)(width*scaleRate), (int)(height*scaleRate));
           // g2d2.drawImage(scaledImage,0,0,null);
           // retFile =  new File(jpgFilePath);
            //ImageIO.write(bi2, "jpg", retFile);
            
            return retFile;
        }
        catch(Exception e) {
            System.out.println("getReScaleJpg error!");
            e.printStackTrace();
            return retFile;
        }
        
    }
	
}
