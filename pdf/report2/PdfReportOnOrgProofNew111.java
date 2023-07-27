package twcebillsysbatch.pdf.report2;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.imageio.*;

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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import jodd.util.StringUtil;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import freemarker.template.Configuration;
import freemarker.template.Template;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;

public class PdfReportOnOrgProofNew111 extends PdfReport {
	private static final Logger LOG = LoggerFactory.getLogger(PdfReportOnOrgProof.class);
	protected BmbaService bmbaService;
	
	private static final int PDF_TOTAL_WIDTH = 550;
	
	private static final int orgBestWidth = 360;//410
    private static final int orgBestHigth = 40;
	
	@Override
	public void output(PdfData data, OutputStream outputStream) {
		Integer yyyyYear = Integer.parseInt(data.getRocYear())+1911;
		String yyyyYearstr=yyyyYear.toString()+data.getMonthString();
		String ebillPath=PropertiesTWCEBill.getProofDirPath()+ File.separator + yyyyYearstr ;
		if (StringUtils.isEmpty(data.getWrbaInvoNO())) {
			ebillPath=PropertiesTWCEBill.getReceiptDirPath()+ File.separator + yyyyYearstr ;
		}
		String pdfimgPath=PropertiesTWCEBill.getPdfNoticeBank103Path();
		String rocDateTensUnits = "";
        if (String.valueOf(data.getRocYear()).length() >= 3) {//100年後
            rocDateTensUnits = String.valueOf(data.getRocYear()).substring(1, 3);
        } else {//99年以前
            rocDateTensUnits = String.valueOf(data.getRocYear());
        }
        String imgfilename=rocDateTensUnits + data.getMonthString() +data.getWaterNO();
        //載具編號
        String tmpcarrierid="";

        boolean  W860paid = getWhetherW860Success(data.getWaterNO(),yyyyYearstr);

        if(!StringUtils.isEmpty(data.getCarrierID())){
        	if (!data.getAc1().equals("")){
        		if(data.getIsW890Paid().equals("Y")){
        			tmpcarrierid=data.getCarrierID().toString();
        		}else if(W860paid){ //20180314 FOR重複繳費，判斷是是否扣帳成功
        			tmpcarrierid=data.getCarrierOrigID().toString();
        		}else if(!data.getCarrierID().equals(data.getCarrierOrigID())){ //20180314 判斷載具不相等時，使用新載具，FOR補扣帳與扣帳不成功
    				tmpcarrierid=data.getCarrierID().toString();
    			}else if(Integer.parseInt(yyyyYearstr)>=201705){
    				tmpcarrierid=data.getCarrierOrigID().toString();
    			}else{
    				tmpcarrierid=data.getCarrierID().toString();
    			};

			}else{
				tmpcarrierid=data.getCarrierID().toString();
			}
        }		

        if (!tmpcarrierid.equals("")){        	
            
//        	}
        	if (tmpcarrierid.length()>=23){
        		String tmpstr=tmpcarrierid.substring(0,5)+" "+tmpcarrierid.substring(5,15)+" "+tmpcarrierid.substring(15);
        		tmpcarrierid=tmpstr;
        	}else if (tmpcarrierid.length()>=21){//201811載具縮短為21碼
        		String tmpstr=tmpcarrierid.substring(0,5)+" "+tmpcarrierid.substring(5,15)+" "+tmpcarrierid.substring(15);
        		tmpcarrierid=tmpstr;
        	}
            
        	String filepath =ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator + imgfilename+ "_CARRIERID.jpg";
			try {
				if (!data.getAc1().equals("")){
					if(W860paid){ //20180314 FOR重複繳費，判斷是是否扣帳成功
						genBarcodetype2("年期別-載具流水號-檢核碼",data.getCarrierOrigID().toString(),filepath);
	        		}else if(!data.getCarrierID().equals(data.getCarrierOrigID())){ //20180314 判斷載具不相等時，使用新載具，FOR補扣帳與扣帳不成功
						genBarcodetype2("年期別-載具流水號-檢核碼",data.getCarrierID().toString(),filepath);
	    			}else if(Integer.parseInt(yyyyYearstr)>=201705){ //20170427 代繳使用原始載具
	    				genBarcodetype2("年期別-載具流水號-檢核碼",data.getCarrierOrigID().toString(),filepath);
	    			}else{
	    				genBarcodetype2("年期別-載具流水號-檢核碼",data.getCarrierID().toString(),filepath);
	    			};
				}else{
					genBarcodetype2("年期別-載具流水號-檢核碼",data.getCarrierID().toString(),filepath);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		//1.讀html template
		Configuration cfg = new Configuration();
		cfg.setDefaultEncoding("UTF-8");
		try {
			cfg.setDirectoryForTemplateLoading(new File(PropertiesTWCEBill.getPdfNoticeBank103Path()));
		
			// Load the template
			Template template;		
			//template = cfg.getTemplate(PropertiesTWCEBill.getPdfNoticeBank103Path());	
			template=cfg.getTemplate("ReportOnOrgProof111.ftl");//
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

            BigDecimal Bopamt = null;
            BigDecimal Bopamt2= null;

            

            BigDecimal Brealdelay= null;

            BigDecimal totamt = new BigDecimal(data.getTotal().trim());
            

            

            

            BigDecimal subamt2;

            String subamt1=thousandDecimalFormat.format(Math.round(totamt.subtract(subamt2).doubleValue())) + "元";
           
            
            //減去稅就是應稅銷售
            String subamtnotax =thousandDecimalFormat.format(Math.round(totamt.subtract(subamt2).subtract(Btax).doubleValue())) + "元";
 	       

            

	        if(!data.getRealfee().trim().equals("0")&& !data.getRealfee().equals(data.getTotal().trim())){
        		if (StringUtils.isEmpty(data.getWrbaInvoNO())) {
        			Brealdelay= new BigDecimal((NumberUtils.toInt(data.getRealfee().trim())-NumberUtils.toInt(data.getTotal().trim()))).setScale(2, BigDecimal.ROUND_HALF_UP);
        		}else{
        			Brealdelay= new BigDecimal((NumberUtils.toInt(data.getRealfee().trim())-NumberUtils.toInt(data.getTotal().trim()))).setScale(2, BigDecimal.ROUND_HALF_UP);
        		}
	        }else if(data.getIsW880Paid().equals("Y")){
	        	
		        if(!data.getW880Total().trim().equals("0")&& !data.getW880Total().equals(data.getTotal().trim())){
	        		if (StringUtils.isEmpty(data.getWrbaInvoNO())) {
	        			Brealdelay= new BigDecimal((NumberUtils.toInt(data.getW880Total().trim())-NumberUtils.toInt(data.getTotal().trim()))).setScale(2, BigDecimal.ROUND_HALF_UP);
	        		}else{
	        			Brealdelay= new BigDecimal((NumberUtils.toInt(data.getW880Total().trim())-NumberUtils.toInt(data.getTotal().trim()))).setScale(2, BigDecimal.ROUND_HALF_UP);
	        		}
		        }
	        	
	        }
            
            
			Map<String, Object> mapdata = new HashMap<String, Object>();
			mapdata.put("REMARK",data.getReGenFlag());
			if (!tmpcarrierid.equals("")&&("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta()))) {
				mapdata.put("SHOWCARRIERID",tmpcarrierid);
				

        		if(!data.getNPOBAN().equals("")){
        			mapdata.put("SHOWCARRIERID",null);
        		}

        		if(tmpcarrierid.substring(0,1).equals("/")){
        			mapdata.put("SHOWCARRIERID",null);
        			mapdata.put("SHOWCARRIERID2",tmpcarrierid);
        		}
			}
			mapdata.put("FILEPATH",pdfimgPath);
			mapdata.put("imgname",ebillPath+ File.separator +"html"+ File.separator + "barcode" + File.separator+imgfilename);
			mapdata.put("YYY", ""+data.getRocYear());//帳單年
			mapdata.put("MM", ""+NumberUtils.toInt(data.getMonthString()));//帳單月
			mapdata.put("MM2", ""+data.getMonthString());//帳單月

			mapdata.put("PYYYMM", ""+data.getRocYear()+"/"+data.getMonthString()+"/21");//繳費期限月
			
			mapdata.put("RPTDATE", ""+data.getPrintDate()); //印製日期
			mapdata.put("ZIPCODE", ""+data.getWrbaPostCode2());//ZIPCODE
			mapdata.put("ADDR", StringUtils.isEmpty(data.getWrbaComuAddr())?
					data.getWrbaEmpeAddr2():
						data.getWrbaComuAddr());
						

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

			mapdata.put("WATERADDR", ""+EbillService.AESDecrypt(data.getWaterAddress()));
			
			
			

			mapdata.put("BANK", data.getAc1() + data.getAc2() + "********");

			mapdata.put("INVONO", StringUtils.isEmpty(data.getWrbaInvoNO())?"":"用戶營利事業統一編號 " +data.getWrbaInvoNO());

			
			mapdata.put("LASTAVGQTY", data.getLastAvgQty()); //去年同期日平均度數
			mapdata.put("AVGQTY", data.getAvgQty());//本期日平均度數
			mapdata.put("THISQTY", (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())) ); //本期用水度數
			mapdata.put("LASTQTY", (NumberUtils.toInt(data.getLAST_REAL_QTY().trim())));//上期實用度數
			mapdata.put("LASTDAVGQ", data.getLAST_DAVGQ()); //上期日平均用水度數
			mapdata.put("LYEARREALQTY", (NumberUtils.toInt(data.getLYEAR_REAL_QTY().trim())));//去年同期實用度數
			
            String acType = data.getAc1().toString().trim();
            String acTypeDesc = PropertiesTool.getACDesc(acType);
            if (acTypeDesc==null){acTypeDesc="";}

			
			
            
            if(!data.getRealfee().equals("0") && !data.getRealfee().equals(data.getTotal())){
				mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getRealfee().trim())) + "元");//應繳總金額
				mapdata.put("TOTAL2",thousandDecimalFormat.format(NumberUtils.toInt(data.getRealfee().trim())) + "元");//應繳總金額
			}else if(data.getIsW880Paid().equals("Y")){
		        if(!data.getW880Total().trim().equals("0")&& !data.getW880Total().equals(data.getTotal().trim())){
		        	mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getW880Total().trim())) + "元");//應繳總金額
					mapdata.put("TOTAL2",thousandDecimalFormat.format(NumberUtils.toInt(data.getW880Total().trim())) + "元");//應繳總金額
		        }else {
		        	mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
					mapdata.put("TOTAL2",thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
		        }	
	        }else{
				mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
				mapdata.put("TOTAL2",thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
			}
			
			
			
			String registration_number = PdfTool.genRegistrationNumber(data.getWaterNO());
			mapdata.put("WATERINVONO", registration_number);


			
			//add by jenny 20150710 
            String tmpwaterno=data.getWaterNO().toString();

            
            //上期發票號碼及上期帳單年月
            //UPDATE BY Vincent 取得上期發票號碼及上期帳單年月 20160317 START:001
            /*String tmpinvoiceno=data.getInvoiceNo().toString();
            if (!tmpinvoiceno.equals("")){
            	mapdata.put("INVOICENO",tmpinvoiceno);
            	mapdata.put("LASTYYMM",data.getLastYYYMM().toString());
            }*/
            EbillService ebillService = SpringTool.getBean(EbillService.class);
    		ebillService.init(DBDataTool.getInstance(), dbutil);

    		Map<String, String> lastBillInfo = ebillService.getLastBillInfo(data.getWaterNO(),yyyyYearstr);
    		String lastWaterno = lastBillInfo.get("WATERNO");
    		String lastYyyyMm = lastBillInfo.get("YYMM");
    		if(lastYyyyMm.equals("")){
    		mapdata.put("LASTYYMM","");//新水號無前期
        	mapdata.put("INVOICENO","");//新水號無前期
    		}else if(!lastYyyyMm.equals("")){
            	System.out.println("LYM checked");
            	String lastRocYM = DateTool.adYMtoRocYM(lastYyyyMm);
            	//Map<String, Object> rtntyymms=DBDataTool.getInstance().getInvoiceNoByWaternoYM(lastRocYM,data.getWaterNO());
            	Map<String, Object> rtntyymms=DBDataTool.getInstance().getInvoiceNoByWaternoYM(lastRocYM,lastWaterno);
            	String invoiceM = "";

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
            System.out.println("LYM over");

            if (!data.getInvoiceNo2().equals("")){
	            String billRocYyymm = data.getRocYear() + data.getMonthString();
	            if(!billRocYyymm.equals("")){
	            	Map<String, Object> rtntyymms2=DBDataTool.getInstance().getInvoiceNoByWaternoYM(billRocYyymm,data.getWaterNO());
	            	String invoiceM = "";
	            	mapdata.put("THISYYMM","");
	            	mapdata.put("INVOICENO2","");
	            	if(!StringUtils.trimToEmpty(ObjectUtils.toString(rtntyymms2.get("INVOICENO"))).equals("")){
	            			            		
	            		mapdata.put("INVOICENO2",ObjectUtils.toString(rtntyymms2.get("INVOICENO")));

	            		switch(billRocYyymm.substring(billRocYyymm.length() - 2)){
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
//	            		mapdata.put("THISYYMM",billRocYyymm.substring(0,3) + "年" + invoiceM + "月");
	            		mapdata.put("THISYYMM",billRocYyymm.substring(0,3) + "<span>年</span>" + invoiceM + "<span>月</span>");
	            	}
	            }
			}

            
            
			
			String receiptNumber = StringUtils.leftPad(data.getChagBillNO1(), 2, "0") + StringUtils.leftPad(data.getChagBillNO2(), 8, "0");
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

            	mapdata.put("AMT2DESC","營業稅"); 
            	mapdata.put("AMT2",thousandDecimalFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元"); 
            	mapdata.put("AMT3DESC","代徵費用"); 

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
			//add by jenny 20150710 
			//ADD BY JENNY 20151027 新增旗山所(7A)也要顯示宣導語
			if (StringUtils.isNotEmpty(data.getWrbaInvoNO())) {
				memolists.add("備註：本通知內容如有爭議時，以本公司用水當地服務（營運）所所存為準。");
				
            } else {
            	memolists.add("備註：1.營業稅分別併入各項費用欄內，依法不另列示。");
            	memolists.add("2.本通知內容如有爭議時，以本公司用水當地服務（營運）所所存為準。");

                //99年7月加入宣導訊息
                if ("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta())) {

                	String showMsgtmp="";
                	int afterConvert2=0; 
                	if ("電子帳單回饋金".equals(data.getAllowanceSta())){
                		showMsgtmp=data.getAllowance().trim().substring(1, 2);
                	}else{

                    	if(Integer.parseInt(data.getAllow2().trim())==0){
                    		afterConvert2=1;
                    	}else{
                    		showMsgtmp=data.getAllow2().trim().substring(1, 2);
                    	}
                	}
                	
                    LOG.info("水號=" + data.getWaterNO() + ",回饋金: " + data.getAllowance().trim() + "->" 
                    		+  showMsgtmp);
                    /*memolists.add("3.感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金" + showMsgtmp + 
                    		"元，直接於本期水費折抵。");*/
                    if(afterConvert2==1){
                    	memolists.add("");
                    }else{
                    	/*memolists.add("3.感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金" + showMsgtmp + 
                        		"元，直接於本期水費折抵。");*/
                    	memolists.add("3.感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金3元，直接於本期水費折抵。");
                    }
                }                
            }
			mapdata.put("memolists", memolists);
			
			
			List<String> tbcol1 = new ArrayList<String>();
			
//			tbcol1.add("收據號碼 ");
//			if (!data.getAc1().equals("")){
////				tbcol1.add("代繳金融行庫 ");
////				tbcol1.add("代繳帳號 ");
//				
//				//判斷本月帳單是否為代收,代收則不顯示
//				//Map<String, Object> isAgenInOk = isAgenInOk(yyyyYearstr, data.getWaterNO());
//				Map<String, Object> w860 = getW860AccountValue(yyyyYearstr, data.getWaterNO());
//				if(!w860.isEmpty()){
//					tbcol1.add("代繳金融行庫 ");
//					tbcol1.add("代繳帳號 ");
//				}
//			}
			tbcol1.add("用水種別 ");

			tbcol1.add("工作區 ");
			tbcol1.add("水表口徑 ");
			//bcol1.add("本公司營利事業統一編號 ");
			tbcol1.add("本期水費扣繳日  ");
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
			
//			if(!pdfutil.trans_zero("公共用水分攤戶數 ", data.getChagBillScount()).equals("")){
//				tbcol1.add("公共用水分攤戶數 ");
//			}
			
			if(!pdfutil.trans_zero("公共用水分攤戶數 ", data.getChagBillScount()).equals("")){
				tbcol1.add("公共用水分攤度數/戶數 ");
			}

			
			tbcol1.add("本期實用度數");
//			tbcol1.add("Total quantity of water used");		//UPDATE BY Vincent 新增英文欄位 20160711
//			tbcol1.add("上期實用度數");
			if(!pdfutil.trans_zero("本期總表指針數 ", "" + NumberUtils.toInt(data.getMasterReading())).equals("")){
				tbcol1.add("本期總表指針數 ");
			}
			if(!pdfutil.trans_zero("上期總表指針數 ", "" + NumberUtils.toInt(data.getMasterLastread())).equals("")){
				tbcol1.add("上期總表指針數 ");
			}
			
			
//			if(!pdfutil.trans_zero("分攤總度數 ", "" + NumberUtils.toInt(data.getMasterSqty())).equals("")){
//				tbcol1.add("分攤總度數 ");
//			}	            
            
			if(!pdfutil.trans_zero("契約度數 ", data.getContract().trim()).equals("")){
				tbcol1.add("契約度數 ");
			}
			

	       	if( data.getBRANCH().trim().equals("1")){
	       		if(!pdfutil.trans_zero("分表總實用度數  ", "" + NumberUtils.toInt(data.getSTQTY())).equals("")){
		       		tbcol1.add("分表總實用度數 ");
		    	}
			}
			

	       	
	       	
			if((27-tbcol1.size())>0){
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
            tbcol2.add(DateTool.changeRocDateDisplay(data.getDeduct(), "/"));
            
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
            
            
//            if(!pdfutil.trans_zero(data.getChagBillScount(), data.getChagBillScount()).equals("")){
//            	tbcol2.add(data.getChagBillScount().trim());
//            }
//            
            if(!pdfutil.trans_zero(data.getChagBillScount(), data.getChagBillScount()).equals("")){
            	tbcol2.add(data.getM_SHSQTY()+"/"+data.getChagBillScount());
            }

            
//            tbcol2.add("" + (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())));
            

            
            tbcol2.add("" + (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())));
            
//            tbcol2.add("");		//UPDATE BY Vincent 20160711
//            tbcol2.add("" + (NumberUtils.toInt(data.getLastChagRealScale().trim())));
            
            if(!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterReading()), "" + 
            		NumberUtils.toInt(data.getMasterReading())).equals("")){
            	tbcol2.add("" + NumberUtils.toInt(data.getMasterReading()));
            }
            
            if(!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterLastread()), "" + 
            		NumberUtils.toInt(data.getMasterLastread())).equals("")){
            	tbcol2.add("" + NumberUtils.toInt(data.getMasterLastread()));
            }
            
                  
            
//            if(!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterSqty()), "" + 
//            		NumberUtils.toInt(data.getMasterSqty())).equals("")){
//            	tbcol2.add(pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterSqty()), "" + 
//	            		NumberUtils.toInt(data.getMasterSqty())));
//            }
            if (!pdfutil.trans_zero(data.getContract().trim(), data.getContract().trim()).equals("")){
            	tbcol2.add(data.getContract().trim());
            }
			

	       	if( data.getBRANCH().trim().equals("1")){
	       		if(!pdfutil.trans_zero(data.getSTQTY().trim(), data.getSTQTY().trim()).equals("")){
	       			tbcol2.add(data.getSTQTY().trim());
		    	}
			}
            
    	
            if((27-tbcol2.size())>0){
				for (int i=25-tbcol2.size();i>0;--i){
					tbcol2.add(" ");
				}
			}
            mapdata.put("tbcol2", tbcol2);
            
            List<String> tbcol3 = new ArrayList<String>();
         // Middle for 說明字樣.
//            tbcol3.add(" Subtotal Water Fee");	//ADD BY Vincent 新增英文欄位 20160620
//            tbcol3.add("◎水費項目小計金額");
            tbcol3.add("水費項目小計");
            if (!pdfutil.trans_zero("基本費", data.getBasicfee().trim()).equals("")){
            	tbcol3.add("基本費");
            }
            if (!pdfutil.trans_zero("用水費", data.getFee().trim()).equals("")){
            	tbcol3.add("用水費");
            }
//            if (!pdfutil.trans_zero("退還/追繳水費", data.getPbfee().trim()).equals("")){
//            	tbcol3.add("退還/追繳水費");
//            }
            

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

            
	        if(!data.getRealfee().trim().equals("0")&& !data.getRealfee().equals(data.getTotal().trim())){
	            if (!pdfutil.trans_zero("遲延費", data.getRealfee().trim()).equals("")){
	            	tbcol3.add("遲延費");
	            }
	        }else if(data.getIsW880Paid().equals("Y")){
	        	
		        if(!data.getW880Total().trim().equals("0")&& !data.getW880Total().equals(data.getTotal().trim())){
		            if (!pdfutil.trans_zero("遲延費", data.getW880Total().trim()).equals("")){
		            	tbcol3.add("遲延費");
		            }
		        }
	        	
	        }
            
            if (!pdfutil.trans_zero(data.getAllowanceSta(), data.getAllowance().trim()).equals("")){
            	tbcol3.add(data.getAllowanceSta().trim());
            }
//            if (!pdfutil.trans_zero(data.getAllow2Sta(), data.getAllow2().trim()).equals("")){
//            	tbcol3.add(data.getAllow2Sta().trim());
//            }
            if (!pdfutil.trans_zero(data.getAllow2Sta(), data.getAllow2().trim()).equals("")){
	            	//tbcol3.add(data.getAllow2Sta().trim());
            	if(Math.abs(NumberUtils.toInt(data.getAllow2().trim()))<=3){
            		tbcol3.add(data.getAllow2Sta().trim());
            	}else if(Math.abs(NumberUtils.toInt(data.getAllow2().trim()))>3){
//            		tbcol3.add("電子帳單/行動支付回饋金");
            		//20220913 112電子帳單回饋金變5元
            		tbcol3.add(data.getAllow2Sta().trim());
            	}
	        }else if(!pdfutil.trans_zero("行動支付回饋金", data.getAllow2().trim()).equals("")){
	        	tbcol3.add("行動支付回饋金");
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
					//UPDATE BY Vincent 20160322 4月份6區帳單扣減項目 START:001
					//UPDATE BY Vincent 20160815 END:001
				}
            
//            if((19-tbcol3.size())>0){
//				for (int i=19-tbcol3.size();i>0;--i){
//					tbcol3.add(" ");
//				}
//			}
	            if((16-tbcol3.size())>0){
				for (int i=16-tbcol3.size();i>0;--i){
					tbcol3.add(" ");
				}
			}
				
				
            

            // Right for 說明字樣.
//            tbcol3.add(" Subtotal Levy");	//ADD BY Vincent 新增英文欄位 20160620
//            tbcol3.add("◎代徵費用小計金額"); // 第一欄固定會顯示
            tbcol3.add("代徵費用小計"); // 第一欄固定會顯示
            // by Ca 20150312 : 污水處理費改為污水下水道使用費
//            if (!pdfutil.trans_zero("污水下水道使用費", data.getDirty().trim()).equals("")){
//            	tbcol3.add("污水下水道使用費");
//            }
                      
            
			
            if (!pdfutil.trans_zero("清除處理費", data.getServ().trim()).equals("")){
            	tbcol3.add("清除處理費");
            }
//            if (!pdfutil.trans_zero("水源保育費", data.getChagBaoyu()).equals("")){
//            	tbcol3.add("水源保育費");
//            }
	        if (!pdfutil.trans_zero("水源保育與回饋費", data.getChagBaoyu()).equals("")){
	        	tbcol3.add("水源保育與回饋費");
	        }
//            if (!pdfutil.trans_zero("加退水源保育費", data.getChagBaoyu2()).equals("")){
//            	tbcol3.add("加退水源保育費");
//            }
//            if (!pdfutil.trans_zero("加退水源保育與回饋費", data.getChagBaoyu2()).equals("")){
//            	tbcol3.add("加退水源保育與回饋費");
//            }
	        

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
            

            // 固定補助費說明 desc from PdfDataOnOrgProof.java
            if (!pdfutil.trans_zero(
            		data.getAllowanceSta(), data.getLevy().trim()).equals("")){
            	tbcol3.add(data.getAllowanceSta());
            }            
            

            
            if((27-tbcol3.size())>0){
				for (int i=25-tbcol3.size();i>0;--i){
					tbcol3.add(" ");
				}
			}
            
            mapdata.put("tbcol3", tbcol3);
            
            
            
            // Right.
            
          
            
            
            if(!pdfutil.trans_zero("◎本期預繳金額", data.getLastfee().trim()).equals("")){
            	//本期預繳金額
            	BigDecimal prepayamt = new BigDecimal(NumberUtils.toInt(data.getLastfee().trim()) - NumberUtils.toInt(data.getPrefee().trim()));
            	totamt=totamt.add(prepayamt);
            }
            
            
            	        
            
            List<String> tbcol4 = new ArrayList<String>();

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
            
            if((!data.getRealfee().trim().equals("0")&& !data.getRealfee().equals(data.getTotal().trim()))||data.getIsW880Paid().equals("Y")){
                
                if (StringUtils.isEmpty(data.getWrbaInvoNO())) {
                    if (!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元", data.getTax().trim()).equals("")){
                    	tbcol4.add(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元");
                    }  
        		}else{

        			
                    if (!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元", data.getTax().trim()).equals("")){
                       	tbcol4.add(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元");
                    }
        		}  
           }else{
               if (!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元", data.getTax().trim()).equals("")){
               	tbcol4.add(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getTax().trim())) + "元");
               }  
           }
            
            
            
            

            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bdelayfee) + "元", data.getDelayfee().trim()).equals("")){
            	tbcol4.add(waterSubamtDetailFormat.format(Bdelayfee) + "元");
            }

            
	        if(!data.getRealfee().trim().equals("0")&& !data.getRealfee().equals(data.getTotal().trim())){
           	 if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Brealdelay) + "元", data.getRealfee().trim()).equals("")){
	            	tbcol4.add(waterSubamtDetailFormat.format(Brealdelay) + "元");
	            }
	        }else if(data.getIsW880Paid().equals("Y")){
	        	
		        if(!data.getW880Total().trim().equals("0")&& !data.getW880Total().equals(data.getTotal().trim())){
		           	 if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Brealdelay) + "元", data.getW880Total().trim()).equals("")){
			            	tbcol4.add(waterSubamtDetailFormat.format(Brealdelay) + "元");
			            }
		        }
	        	
	        }
            
            
            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getAllowance().trim())) + "元", data.getAllowance().trim()).equals("")){
            	tbcol4.add(waterSubamtDetailFormat.format(Ballowance) + "元");
            }
            if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getAllow2().trim())) + "元", data.getAllow2().trim()).equals("")){
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

			
			/* <START> 停水扣減. 根據pdfData去決定是否要印停水扣減，故不限區. */
			
			if (!cutoffDedcFeeWtTax.equals("0") // Default property value
					&& !cutoffDedcFeeWtTax.equals("") // Got data but no value.
					) {
				if (tbcol4.size()<10){
            		for (int i=10-tbcol4.size();i>0;--i){
            			tbcol4.add(" ");
					}
            	}
				//UPDATE BY Vincent 20160322 4月份6區帳單扣減項目 START:002 ADD LINE:647~649、651

				//UPDATE BY Vincent 20160815 END:002
			}           
            
//            if((19-tbcol4.size())>0){
//				for (int i=19-tbcol4.size();i>0;--i){
//					tbcol4.add(" ");
//				}
//			}
			
			if((16-tbcol4.size())>0){
				for (int i=16-tbcol4.size();i>0;--i){
					tbcol4.add(" ");
				}
			}
			
            // Middle for money.
            
            
            // #region - Right for money.
            
            
            
            LOG.info("dirty=" + data.getDirty() + ", serv=" + data.getServ() + ", chag_chag_baoyu=" + data.getChagBaoyu()
            		+ ", chag_chag_baoyu2=" + data.getChagBaoyu2() + ", pbserv=" + data.getPbserv() + ", levy=" + data.getLevy());

            

            
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

            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getPbserv().trim())) + "元", 
            		data.getPbserv().trim()).equals("")){
            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getPbserv().trim())) + "元");
            }
            

            if(!pdfutil.trans_zero(
            		thousandDecimalFormat.format(NumberUtils.toInt(data.getLevy().trim())) + "元", data.getLevy().trim()).equals("")){
            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getLevy().trim())) + "元");
            }
            
            if (!pdfutil.trans_zero("水費折扣", data.getDiscountamt().trim()).equals("")){
            	if((27-tbcol4.size())>0){
					for (int i=25-tbcol4.size();i>0;--i){
						tbcol4.add(" ");
					}
				}

	            
            }
            else{
	            if((27-tbcol4.size())>0){
					for (int i=25-tbcol4.size();i>0;--i){
						tbcol4.add(" ");
					}
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
            
            
            
            // #endregion - Right for money.
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
			LOG.info("帳單資料轉html(" + htmlfile + ")");
		//3.將html檔轉成pdf
			convertHtml2pdf(htmlfile,outputStream);
			
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
		
	
	public void convertHtml2pdf(String src, OutputStream outputStream) throws IOException {

        PdfWriter pdfWriter = new PdfWriter(outputStream);
        
        PdfDocument pdf = new PdfDocument(pdfWriter);
        pdf.setDefaultPageSize(new PageSize(PageSize.A4));

        
	    ConverterProperties properties = new ConverterProperties();

	    FontProvider fontProvider = new DefaultFontProvider();

	    for (String font : FONTS) {
	        FontProgram fontProgram = FontProgramFactory.createFont(font);
	        fontProvider.addFont(fontProgram);
	    }
	    
	    properties.setFontProvider(fontProvider);
	   
	    HtmlConverter.convertToPdf(new FileInputStream(src), pdf, properties);

         System.out.println("down");	   

	}

	private static File getReScaleJpg(String jpgFilePath,double scaleRate,String barcodeID) {
        File retFile = null;
        try {
            
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
            
            return retFile;
        }
        catch(Exception e) {
            System.out.println("getReScaleJpg error!");
            e.printStackTrace();
            return retFile;
        }
        
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

    
}