package twcebillsysbatch.pdf.report2;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;


import freemarker.template.Configuration;
import freemarker.template.Template;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;


public class PdfReportOnBankDebitsNoticeNew111 extends PdfReport {
	private static final Logger LOG = Logger.getLogger(PdfReportOnBankDebitsNotice.class);
	private static final int PDF_TOTAL_WIDTH = 520;
	
	/**
	 * 依據pdf data產出pdf檔案到目標OutputStream.
	 * method內容修改自PdfReport4.OperateFile2(...).
	 * @param data 在其他地方準備好的pdf內容檔
	 * @param outputStream 產生pdf檔案目標OutputStream
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
		
		//1.讀html template
		Configuration cfg = new Configuration();
		cfg.setDefaultEncoding("UTF-8");
		try {
			cfg.setDirectoryForTemplateLoading(new File(PropertiesTWCEBill.getPdfNoticeBank103Path()));
		
			// Load the template
			Template template;		
			//template = cfg.getTemplate(PropertiesTWCEBill.getPdfNoticeBank103Path());	
			template=cfg.getTemplate("BankDebitsNotice111.ftl");	
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
            BigDecimal Bopamt=null;
            BigDecimal Bopamt2=null;
            if( Integer.parseInt(yyyyYearstr) >= 201904){
             Bopamt = new BigDecimal(data.getOP_AMT().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
             Bopamt2 = new BigDecimal(data.getOP_AMT2().trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
			
			Map<String, Object> mapdata = new HashMap<String, Object>();
			mapdata.put("REMARK",data.getReGenFlag());
			mapdata.put("FILEPATH",pdfimgPath);
			mapdata.put("YYY", ""+data.getRocYear());//帳單年
			mapdata.put("MM", ""+NumberUtils.toInt(data.getMonthString()));//帳單月
			mapdata.put("MM2", ""+data.getMonthString());//帳單月
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
			
			mapdata.put("WATERDTS",DateTool.changeRocDateDisplay(PdfTool.genMeteredDate(data.getLastMeteredDate()), "/"));
			mapdata.put("WATERDTE",DateTool.changeRocDateDisplay(data.getMeteredDate(), "/"));
			//mapdata.put("WATERADDR", ""+data.getWaterAddress());
			
			mapdata.put("WATERADDR", ""+EbillService.AESDecrypt(data.getWaterAddress()));
			
			

			String bank = data.getAc1() + data.getAc2();

			
			if(bank.length() > 0){
				mapdata.put("BANKTITLE", "(金融機構代繳用戶)");
				mapdata.put("BANK", "代繳帳號 " + data.getAc1() + data.getAc2() + "********");
				mapdata.put("PYYYMM", DateTool.changeRocDateDisplay(data.getDeduct(), "/"));//繳費期限月
				mapdata.put("DUEDATETITLEC",  "扣繳日期");//代繳顯示扣繳日期
				mapdata.put("DUEDATETITLEE",  "(Payment Date)");//代繳顯示Payment Date
			}else{
				mapdata.put("BANKTITLE", "");
				mapdata.put("BANK", "");
				mapdata.put("PYYYMM", ""+data.getRocYear()+"/"+data.getMonthString()+"/21");//繳費期限月
				mapdata.put("DUEDATETITLEC",  "繳費期限");//代收顯示繳費期限
				mapdata.put("DUEDATETITLEE",  "(Due Date)");//代收顯示Due Date
			}
			mapdata.put("INVONO", StringUtils.isEmpty(data.getWrbaInvoNO())?"":"用戶營利事業統一編號 " +data.getWrbaInvoNO());
		
//			mapdata.put("LASTAVGQTY", data.getLastAvgQty() + " 度"); //去年同期日平均度數
//			mapdata.put("AVGQTY", data.getAvgQty() + " 度");//本期日平均度數
//			mapdata.put("TOTAL","$" + thousandDecimalFormat.format(NumberUtils.toInt(data.getTotal().trim())) + "元");//應繳總金額
			
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
			
			
			//20210312供水區先隱藏起來
//			if(!data.getChgSuppArea().equals("")){
//				mapdata.put("SUPPAREA","貴用戶為第"+data.getChgSuppArea()+"供水區。");
//			}
			
			
			//add by jenny 20150710 			
			//ADD BY JENNY 20151027 新增旗山所(7A)也要顯示宣導語
            String tmpwaterno=data.getWaterNO().toString();
 
            
            
            String receiptNumber = StringUtils.leftPad(data.getChagBillNO1(), 2, "0") + StringUtils.leftPad(data.getChagBillNO2(), 8, "0");
            mapdata.put("RECEIPTNUMBER",receiptNumber); //收據號碼

            EbillService ebillService = SpringTool.getBean(EbillService.class);
    		ebillService.init(DBDataTool.getInstance(), dbutil);
            //String lastYyyyMm = ebillService.getLastBillYyyyMm(data.getWaterNO(), yyyyYearstr);
    		Map<String, String> lastBillInfo = ebillService.getLastBillInfo(data.getWaterNO(),yyyyYearstr);
    		String lastWaterno = lastBillInfo.get("WATERNO");
    		String lastYyyyMm = lastBillInfo.get("YYMM");
            if(!lastYyyyMm.equals("")){
            	String lastRocYM = DateTool.adYMtoRocYM(lastYyyyMm);
            	//Map<String, Object> rtntyymms=DBDataTool.getInstance().getInvoiceNoByWaternoYM(lastRocYM,data.getWaterNO());
            	Map<String, Object> rtntyymms=DBDataTool.getInstance().getInvoiceNoByWaternoYM(lastRocYM,lastWaterno);
            	if(!StringUtils.trimToEmpty(ObjectUtils.toString(rtntyymms.get("INVOICENO"))).equals("")){
            		mapdata.put("INVOICENO",ObjectUtils.toString(rtntyymms.get("INVOICENO")));
            		mapdata.put("LASTYYMM",lastRocYM.substring(0,3) + "年" + lastRocYM.substring(lastRocYM.length() - 2) + "月");
            	}
            }
            //UPDATE BY Vincent 20160317 END:001
			
			List<String> discountlists = new ArrayList<String>();

			
			if (!StringUtils.defaultIfBlank(data.getTempNote(), "").equals("")) {
				discountlists.add(data.getTempNote());
            }
			//99年7月加入宣導訊息
            //if ("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta())) {
            //	String tmpamt="";
            //	if ("電子帳單回饋金".equals(data.getAllowanceSta())){tmpamt=data.getAllowance();}
            //	else{tmpamt=data.getAllow2();}
            //	discountlists.add("感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金"+tmpamt.trim().substring(1, 2)+"元，直接於本期水費折抵。");
            //}
			
			if (discountlists.size()<=0){
				discountlists.add("");
			}
			
			mapdata.put("discountlists", discountlists);
			
			List<String> memolists = new ArrayList<String>();
			
			//if (StringUtils.isNotEmpty(data.getWrbaInvoNO())) {
				//memolists.add("本通知內容如有爭議時，以本公司用水當地服務（營運）所所存為準。");

            //} else {
            	memolists.add("營業稅分別併入各項費用欄內，依法不另列示。");
            	memolists.add("本通知內容如有爭議時，以本公司用水當地服務（營運）所所存為準。");

                //99年7月加入宣導訊息
                if ("電子帳單回饋金".equals(data.getAllowanceSta()) || "電子帳單回饋金".equals(data.getAllow2Sta())) {
                    // 折扣金有兩類(扣繳戶扣5元，其餘扣3元
                	String showMsgtmp="";
                	int afterConvert2=0; 
                	int afterConvert=0;
                	
                	if ("電子帳單回饋金".equals(data.getAllowanceSta())){
//                		showMsgtmp=data.getAllowance().trim().substring(1, 2);
                		//20220907 電子帳單回饋金改成5元改成彈性
                		showMsgtmp=data.getAllowance();
                		afterConvert = Integer.parseInt(showMsgtmp);
                	}else{
                		//showMsgtmp=data.getAllow2().trim().substring(1, 2);
                    	//判斷回饋金 有可能有AllowanceSta=電子帳單回饋金 但getAllow2=0會產生錯誤
                    	if(Integer.parseInt(data.getAllow2().trim())==0){
                    		afterConvert2=1;
                    	}else{
//                    		showMsgtmp=data.getAllow2().trim().substring(1, 2);
                    		//20220907 電子帳單回饋金改成5元改成彈性
                    		showMsgtmp=data.getAllow2();
                    		afterConvert = Integer.parseInt(showMsgtmp);
                    	}
                		
                	}
                	
                    LOG.info("水號=" + data.getWaterNO() + ",回饋金: " + data.getAllowance().trim() + "->" 
                    		+  showMsgtmp);
                    if(afterConvert2==1){
                    	memolists.add("");
                    }else{

                    	memolists.add("感謝您使用本公司電子帳單，為響應節能減碳，本期電子帳單折扣金"+Math.abs(afterConvert)+"元，直接於本期水費折抵。");//20180608 
                    }

                }
            //}
			mapdata.put("memolists", memolists);
			
			
			List<String> tbcol1 = new ArrayList<String>();
			
			tbcol1.add("用水種別 ");

			tbcol1.add("工作區 ");
			tbcol1.add("水表口徑 ");
			//tbcol1.add("本公司營利事業統一編號");
			tbcol1.add("本期水費扣繳日 ");
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
			
			

			if(!pdfutil.trans_zero("公共用水分攤戶數 ", data.getChagBillScount()).equals("")){
				tbcol1.add("公共用水分攤度數/戶數 ");
			}
			
			
			tbcol1.add("本期實用度數");

			if(!pdfutil.trans_zero("本期總表指針數 ", "" + NumberUtils.toInt(data.getMasterReading())).equals("")){
				tbcol1.add("本期總表指針數 ");
			}
			if(!pdfutil.trans_zero("上期總表指針數 ", "" + NumberUtils.toInt(data.getMasterLastread())).equals("")){
				tbcol1.add("上期總表指針數 ");
			}
			
			

			

			if(!pdfutil.trans_zero("契約度數 ", data.getContract().trim()).equals("")){
				tbcol1.add("契約度數 ");
			}
			 //20190716 新增分表總實用度數  只有總表才有
	       	if( data.getBRANCH().trim().equals("1")){
	       		if(!pdfutil.trans_zero("分表總實用度數  ", "" + NumberUtils.toInt(data.getSTQTY())).equals("")){
		       		tbcol1.add("分表總實用度數 ");
		    	}
			}
	       		       	
			
			if((29-tbcol1.size())>0){
				for (int i=25-tbcol1.size();i>0;--i){
					tbcol1.add(" ");
				}
			}
			mapdata.put("tbcol1", tbcol1);

			
			List<String> tbcol2 = new ArrayList<String>();
			
			tbcol2.add(PropertiesTool.getWaterTypeDesc(data.getWaterType().toString().trim()));
			
			

			tbcol2.add("" + NumberUtils.toInt(data.getDiameter()));

			tbcol2.add(DateTool.changeRocDateDisplay(data.getDeduct(), "/"));
			
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

            //102年1月後加入新欄位 - 註記
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
//            	//tbcol2.add(data.getSqty().trim());		//TEST20161007
//            	tbcol2.add(data.getChagBillScount());
//            }
            if(!pdfutil.trans_zero(data.getChagBillScount(), data.getChagBillScount()).equals("")){
            	tbcol2.add(data.getM_SHSQTY()+"/"+data.getChagBillScount());
            }

            tbcol2.add("" + (NumberUtils.toInt(data.getToqty().trim()) + NumberUtils.toInt(data.getSqty().trim())));
            
            
//            tbcol2.add("");		//UPDATE BY Vincent 20160711
//            tbcol2.add("" + (NumberUtils.toInt(data.getLastChagRealScale().trim())));
            
            if (!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterReading()), "" + NumberUtils.toInt(data.getMasterReading())).equals("")){
            	tbcol2.add("" + NumberUtils.toInt(data.getMasterReading()));
            }
            if (!pdfutil.trans_zero("" + NumberUtils.toInt(data.getMasterLastread()), "" + 
            		NumberUtils.toInt(data.getMasterLastread())).equals("")){
            	tbcol2.add("" + NumberUtils.toInt(data.getMasterLastread()));
            }

            if (!pdfutil.trans_zero(data.getContract().trim(), data.getContract().trim()).equals("")){
            	tbcol2.add(data.getContract().trim());
            }

	       	if( data.getBRANCH().trim().equals("1")){
	       		if(!pdfutil.trans_zero(data.getSTQTY().trim(), data.getSTQTY().trim()).equals("")){
	       			tbcol2.add(data.getSTQTY().trim());
		    	}
			}
            
	       	
            if((29-tbcol2.size())>0){
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

            
//            if((18-tbcol3.size())>0){
//				for (int i=18-tbcol3.size();i>0;--i){
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
            

            

}

			
            
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
            
            
            
            

            // 固定補助費說明 desc from PdfDataOnOrgProof.java
            if (!pdfutil.trans_zero(
            		data.getAllowanceSta(), data.getLevy().trim()).equals("")){
            	tbcol3.add(data.getAllowanceSta());
            }
            
            if((29-tbcol3.size())>0){
				for (int i=25-tbcol3.size();i>0;--i){
					tbcol3.add(" ");
				}
			}
            
            mapdata.put("tbcol3", tbcol3);
            
            
            // Right.
            
          //水費項目小計用應繳總金額回推
            BigDecimal totamt = new BigDecimal(data.getTotal().trim());
            

            BigDecimal subamt2 ;

            if(!pdfutil.trans_zero("◎本期預繳金額", data.getLastfee().trim()).equals("")){
            	//本期預繳金額
            	BigDecimal prepayamt = new BigDecimal(NumberUtils.toInt(data.getLastfee().trim()) - NumberUtils.toInt(data.getPrefee().trim()));
            	totamt=totamt.add(prepayamt);
            }
            
            String subamt1=thousandDecimalFormat.format(Math.round(totamt.subtract(subamt2).doubleValue())) + "元";
            	            
            
            List<String> tbcol4 = new ArrayList<String>();
//            tbcol4.add("");		//ADD BY Vincent 20160620
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
            
            //108年4月後加入新欄位 應收操作維護費,加退操作維護費,工程改善費,加退工程改善費
            if( Integer.parseInt(yyyyYearstr) >= 201904){
				if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bopamt) + "元", data.getOP_AMT().trim()).equals("")){
					tbcol4.add(waterSubamtDetailFormat.format(Bopamt) + "元");
				}
				if(!pdfutil.trans_zero(waterSubamtDetailFormat.format(Bopamt2) + "元", data.getOP_AMT2().trim()).equals("")){
					tbcol4.add(waterSubamtDetailFormat.format(Bopamt2) + "元");
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

			
			
    		if(  !pdfutil.trans_zero(waterSubamtDetailFormat.format(NumberUtils.toInt(data.getPbfee().trim())) + "元", data.getPbfee().trim()).equals("")&& EbillService.checkPromoteWaterno(data.getWaterNO(),"202011")&&  Integer.parseInt(yyyyYearstr) >= 202008 && Integer.parseInt(yyyyYearstr) <= 202012 ){

				if (tbcol4.size()<10){
            		for (int i=10-tbcol4.size();i>0;--i){
            			tbcol4.add(" ");
					}
            	}
				if(( yyyyYearstr.equals("202009") || yyyyYearstr.equals("202010") || yyyyYearstr.equals("202011") || yyyyYearstr.equals("202012"))){
					tbcol4.add("八月電子帳單");//20200515 修改備註為註
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
				//UPDATE BY Vincent 20160322 4月份6區帳單扣減項目 START:002
				//UPDATE BY Vincent 20160815 10月份10區帳單扣減項目 START:002
				if((yyyyYearstr.equals("201604") || yyyyYearstr.equals("201605") || yyyyYearstr.equals("201606") || yyyyYearstr.equals("201607") || yyyyYearstr.equals("201608") || yyyyYearstr.equals("201609") || yyyyYearstr.equals("201610") || yyyyYearstr.equals("201611") || yyyyYearstr.equals("201612")) && data.getWaterNO().substring(0,1).equals("6")){
					tbcol4.add("0206震災停水水");
					tbcol4.add("元");
				}else if((yyyyYearstr.equals("201610") || yyyyYearstr.equals("201611") || yyyyYearstr.equals("201612") || yyyyYearstr.equals("201701")) && data.getWaterNO().substring(0,1).equals("A")){
					tbcol4.add("扣減"+cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("201612") || yyyyYearstr.equals("201701") || yyyyYearstr.equals("201702") || yyyyYearstr.equals("201703") || yyyyYearstr.equals("201704") || yyyyYearstr.equals("201705")|| yyyyYearstr.equals("201706") || yyyyYearstr.equals("201707")) && data.getWaterNO().substring(0,1).equals("7")){
					tbcol4.add("姬颱風扣減"+cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("201706") || yyyyYearstr.equals("201707") || yyyyYearstr.equals("201708") || yyyyYearstr.equals("201709") || yyyyYearstr.equals("201710") || yyyyYearstr.equals("201711") || yyyyYearstr.equals("201712") || yyyyYearstr.equals("201801") || yyyyYearstr.equals("201802") || yyyyYearstr.equals("201803")) && (data.getWaterNO().substring(0,1).equals("4")||data.getWaterNO().substring(0,1).equals("B"))){
					tbcol4.add("水道停水補助"+cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("201712") || yyyyYearstr.equals("201801") || yyyyYearstr.equals("201802") || yyyyYearstr.equals("201803") || yyyyYearstr.equals("201804")|| yyyyYearstr.equals("201806")) && (data.getWaterNO().substring(0,1).equals("1"))){
					tbcol4.add("費補助"+cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("201807") || yyyyYearstr.equals("201808")) && (data.getWaterNO().substring(0,1).equals("9"))){
					tbcol4.add("扣減"+cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("202011") || yyyyYearstr.equals("202012") || yyyyYearstr.equals("202101") || yyyyYearstr.equals("202102") || yyyyYearstr.equals("202103") || yyyyYearstr.equals("202104")|| yyyyYearstr.equals("202105") || yyyyYearstr.equals("202106")) && data.getWaterNO().substring(0,1).equals("7")){
					tbcol4.add("停水事件扣減"+cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("202105") || yyyyYearstr.equals("202106") || yyyyYearstr.equals("202107") || yyyyYearstr.equals("202108")|| yyyyYearstr.equals("202109")) && (data.getWaterNO().substring(0,1).equals("1")||data.getWaterNO().substring(0,1).equals("C"))){
					tbcol4.add("黃補償十日基");
					tbcol4.add(cutoffDedcFeeWtTax+"元");
				}else if((yyyyYearstr.equals("202210")) && (data.getWaterNO().substring(0,1).equals("2"))){
					tbcol4.add("");
				}else if((yyyyYearstr.equals("202210")|| yyyyYearstr.equals("202211")) && (data.getWaterNO().substring(0,1).equals("5"))){
					tbcol4.add("");
				}else if((yyyyYearstr.equals("202212") || yyyyYearstr.equals("202301") || yyyyYearstr.equals("202302") || yyyyYearstr.equals("202303")) && (data.getWaterNO().substring(0,1).equals("9")||data.getWaterNO().substring(0,1).equals("A"))){
					tbcol4.add("");
				}else if((yyyyYearstr.equals("202304")|| yyyyYearstr.equals("202305")) && (data.getWaterNO().substring(0,1).equals("2"))){
					tbcol4.add("");
				}else{
					tbcol4.add("扣除「停水扣減");
					tbcol4.add("元");
				}
				//tbcol4.add("元");
				//UPDATE BY Vincent 20160322 END:002
				//UPDATE BY Vincent 20160815 END:002
			}           
            

            // Middle for money.
//            if((18-tbcol4.size())>0){
//				for (int i=18-tbcol4.size();i>0;--i){
//					tbcol4.add(" ");
//				}
//			}
			
			if((16-tbcol4.size())>0){
				for (int i=16-tbcol4.size();i>0;--i){
					tbcol4.add(" ");
				}
			}
			
            
            // #region - Right for money.
            
            LOG.info("dirty=" + data.getDirty() + ", serv=" + data.getServ() + ", chag_chag_baoyu=" + data.getChagBaoyu()
            		+ ", chag_chag_baoyu2=" + data.getChagBaoyu2() + ", pbserv=" + data.getPbserv() + ", levy=" + data.getLevy());

            if( Integer.parseInt(yyyyYearstr) >= 201904){
            tbcol4.add(thousandDecimalFormat.format((NumberUtils.toInt(data.getDirty().trim())
					+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
					+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
					+ NumberUtils.toInt(data.getMB_AMT()) + NumberUtils.toInt(data.getMB_AMT2().trim())
					+ NumberUtils.toInt(data.getLevy().trim()))) + "元");
            }else{
            	tbcol4.add(thousandDecimalFormat.format((NumberUtils.toInt(data.getDirty().trim())
        		+ NumberUtils.toInt(data.getServ().trim()) + NumberUtils.toInt(data.getChagBaoyu())
        		+ NumberUtils.toInt(data.getChagBaoyu2()) + NumberUtils.toInt(data.getPbserv().trim())
        		+ NumberUtils.toInt(data.getLevy().trim()))) + "元");	
            }

			if( Integer.parseInt(yyyyYearstr) >= 201907){
	            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getDirty().trim())) + "元", 
	            		data.getDirty().trim()).equals("") || !pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getFOUL_AMT2().trim())) + "元", 
	    	            		data.getFOUL_AMT2().trim()).equals("")){
	            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getDirty().trim())-NumberUtils.toInt(data.getFOUL_AMT2().trim())) + "元");
			}
			}else{
	            if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getDirty().trim())) + "元", 
	            		data.getDirty().trim()).equals("")){
	            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getDirty().trim())) + "元");
	            }
				
			}
            

			if( Integer.parseInt(yyyyYearstr) >= 201907){
				if(!pdfutil.trans_zero(thousandDecimalFormat.format(NumberUtils.toInt(data.getFOUL_AMT2().trim())) + "元", 
						data.getFOUL_AMT2().trim()).equals("")){
					tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getFOUL_AMT2().trim())) + "元");
				}
			}
            
            
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
            
            if(!pdfutil.trans_zero(
            		thousandDecimalFormat.format(NumberUtils.toInt(data.getLevy().trim())) + "元", data.getLevy().trim()).equals("")){
            	tbcol4.add(thousandDecimalFormat.format(NumberUtils.toInt(data.getLevy().trim())) + "元");
            }
            
            if((29-tbcol4.size())>0){
				for (int i=26-tbcol4.size();i>0;--i){
					tbcol4.add(" ");
				}
			}
            
            mapdata.put("tbcol4", tbcol4);

            
            
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
            System.out.println("toqty:"+data.getToqty());
            System.out.println("sqty:"+data.getToqty());
            System.out.println("CO2toDisplay:"+PropertiesTWCEBill.getCO2());
            System.out.println("CO2:"+(int) Math.round((Double.parseDouble("5310") + 
            		Double.parseDouble("0")) * 0.167));
            
            String htmlfile=ebillPath+ File.separator +"html"+ File.separator + imgfilename+ ".html";
            File input = new File(htmlfile);
            
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(input),"UTF-8"));
			
			template.process(mapdata, out);
			
			out.flush();
            
		//3.將html檔轉成pdf
			convertHtml2pdf(htmlfile,outputStream);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(data.getWaterNO());
			e.printStackTrace();
		}
		

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

}