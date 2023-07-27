package twcebillsysbatch.domain;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;

import twcebillsysbatch.tool.DateTool;

/**
 * 產生pdf用之資料domain object.
 * 提供給PdfReport的output API產生pdf使用
 * 
 * <p>注意!!! do NOT change the member variables!</p>
 * 
 * @author monsta
 *
 */
public class PdfData {

	//印製日期
	private String printDate;
	//民國年
	private String rocYear;
	//月份參數
	private String monthString;
	//pdf所屬記帳年月
	private DateTime dateTimeArgument;
	private String waterNO;
	//用水地址
	private String waterAddress;
	//通訊地址
	private String mailAddress;
	//收據號碼-檔號
	private String batchNO;
	//用水種別
	private String waterType;
	//口徑
	private String diameter;
	//實用度數
	private String toqty;
	//分攤|附表度數
	private String sqty;
	//契約度數
	private String contract;
	//優待後水費(未減回饋費)
	private String fee;
	//加退水費
	private String pbfee;
	//基本費
	private String basicfee;
	//固定補助費小計
	private String allowance;
	//臨時補助費小計
	private String allow2;
	//應收營業稅
	private String tax;
	//清除處理費
	private String serv;
	//加退清除處理費
	private String pbserv;
	//應收污水處理費(含加退)；20150312 by Ca 污水下水道使用費；
	private String dirty;
	//保護區優惠水費--回饋費(含加退)
	private String feedback;
	//應收水源保育費(含加退)
	private String preserve;
	//應繳總金額
	private String total;
	//本期預繳金額
	private String prefee;
	//上期預繳餘額
	private String lastfee;
	private String delayfee;
	private String billDate;
	private String duedate;
	private String deduct;
	private String lastMeteredDate;
	private String meteredDate;
	private String userName;
	private String ac2;
	private String reading;
	private String lastread;
	private String nextMeteredDate;
	private String nextBillDate;
	private String ac1;
	private String realfee;
	private String masterNO;
	//設定總表本期指針
	private String masterReading;
	//總表上期指針
	private String masterLastread;
	//總表度數
	private String masterSqty;
	private String rule33;
	private String flood50;
	private String allow33;
	private String allow50;
	private String saveRatio;
	private String saveAllow;
	private String lastAvgQty;
	private String avgQty;
	private String allowanceSta;
	//臨時補助費說明
	private String allow2Sta;
	private String levy;
	private String mno;//水表表號
	private String chagWorkArea1;
	private String chagWorkArea2;
	private String chagHandScnum;
	private String chagHandSpnum;
	private String chagDsby;
	private String chagBillScount;
	private String chagDsby2;
	private String chagBillNO1;
	private String chagBillNO2;
	private String chagBaoyu;
	private String chagBaoyu2;
	private String chagWatrPeriod;
	private String chagMetrState;
	private String waterStationName;
	private String waterStationTel;
	private String waterStationAddr;
	private String waterStationTel2;
	private String wrbaInvoNO;
	private String wrbaOthrMark1;
	private String wrbaPostCode2;
	private String wrbaEmpeAddr2;
	private String wrbaComuAddr;
	//繳費期限之月份字串,ex:09
	private String paymentDeadlineMonth;
	//代收期限
	private Date receivingDuedate;
	//代收之銷帳編號
	private String writeOffNumber;
	//代收查核碼
	private String checkingCode;
	//代收期限之民國格式日期字串
	private String receivingDuedateRocString;
	//收款張日期
	private String stampDate;
	//通知註記
	private String mstus;
	//本期繳費起始日期
	private String chagCurnDate;
	/** 停水扣減費用 default="0" */
	private String waterCutoffDedcFeeWtTax = "0"; // 201308加入欄位，有停水扣減才要印。
	
	//add by jenny 102年3月後加入新欄位
	private String last_chag_real_scale;  //上期實用度數
	
	private String chg_supp_area;
	
	private String paiddt; //waterfee的銷帳日期
	
	private String discountamt; //折扣金額 add by 20150528
	
	private String carrierid; //載具編號 //105年1月新增
	private String invoiceno; //上期發票號碼//105年1月新增
	private String invoiceno2; //上期發票號碼//105年1月新增
	private String lastyyymm; //上期帳單年月//105年1月新增
	private String carrieridorig; //原始載具編號//106年5月新增
	private String OP_AMT;//應收操作維護費 //108年3月新增
	private String OP_AMT2;//加退操作維護費//108年3月新增
	private String MB_AMT;//應收工程改善費//108年3月新增
	private String MB_AMT2;//加退工程改善費//108年3月新增
	
	private String FOUL_AMT2;//加退汙水下水道使用費//108年6月新增
	
	
	private String WAEE_SUBSIDY;//國防部補助金額//108年8月新增
	
	private String SREAD1;//副表本月指針//110年5月新增
	
	private String SREAD2;//副表上月指針//110年5月新增
	
	private String stqty;//分表總實用度數

	private String branch;	//總表分表判斷
	
	private String chagCityCode;//縣市代碼
	
	
	private String regenflag; //補產註記
	
	private String isW890Paid; //代收註記
	
	private String isW880Paid; //補扣帳註記
	private String W880Total;//補扣帳總金額
	
	
	private String LAST_DAVGQ;//上期日平均用水度數
	private String LAST_REAL_QTY;	//上期實用度數
	private String LYEAR_REAL_QTY;	//去年同期實用度數

	
	private String M_SHSQTY;	//分攤總度數
	
	private String NPOBAN;	//愛心碼
	
		
	/** 有時候需要加上的通知字樣 */
	private String tempNote;
	
	public String getRealfee() {
		if(StringUtils.isEmpty(this.realfee)) {
			return "0";
		}
		return this.realfee;
	}

	/**
	 * 取得站所編號兩碼，從完整水號得來
	 * @return
	 */
	public String getStationNO() {
		if(StringUtils.isNotEmpty(this.waterNO) && this.waterNO.length() > 2) {
			return this.waterNO.substring(0, 2);
		}
		return null;
	}

	/**
	 * 取得水號中的站所編號
	 * @return
	 */
	public String getWaterNoOfStation() {
		return this.waterNO.substring(0, 2);
	}

	/**
	 * 取得水號中去掉前面站所編號與後面檢號的中間序號部份
	 * @return
	 */
	public String getWaterNoOfSerialNO() {
		return this.waterNO.substring(2, 10);
	}

	/**
	 * 取得水號最後一碼檢字號部分
	 * @return
	 */
	public String getWaterNoOfCheck() {
		return this.waterNO.substring(10, 11);
	}

	/**
	 * @return 印製日期 (eg. 103.01.24/15:58)
	 */
	public String getPrintDate() {
		return printDate;
	}

	/**
	 * @param printDate 印製日期 (eg. 103.01.24/15:58)
	 */
	public void setPrintDate(String printDate) {
		this.printDate = printDate;
	}

	public String getRocYear() {
		return rocYear;
	}

	public void setRocYear(String rocYear) {
		this.rocYear = rocYear;
	}

	/**
	 * 取得兩位數的帳單月份,ex:08 or 09 or 10
	 * @return
	 */
	public String getMonthString() {
		return monthString;
	}

	public void setMonthString(String monthString) {
		this.monthString = monthString;
	}

	public DateTime getDateTimeArgument() {
		return dateTimeArgument;
	}

	public void setDateTimeArgument(DateTime dateTimeArgument) {
		this.dateTimeArgument = dateTimeArgument;
	}

	/** Column: waterno. 水號 */
	public String getWaterNO() {
		return waterNO;
	}

	/** Column: waterno. 水號 */
	public void setWaterNO(String waterNO) {
		this.waterNO = waterNO;
	}

	public String getWaterAddress() {
		return waterAddress;
	}

	public void setWaterAddress(String waterAddress) {
		this.waterAddress = waterAddress;
	}

	public String getMailAddress() {
		return mailAddress;
	}

	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}

	/** Column: batchNO. 收據號碼-檔號 */
	public String getBatchNO() {
		return batchNO;
	}

	/** Column: batchNO. 收據號碼-檔號 */
	public void setBatchNO(String batchNO) {
		this.batchNO = batchNO;
	}

	public String getWaterType() {
		return waterType;
	}

	public void setWaterType(String waterType) {
		this.waterType = waterType;
	}

	public String getDiameter() {
		return diameter;
	}

	public void setDiameter(String diameter) {
		this.diameter = diameter;
	}

	public String getToqty() {
		return toqty;
	}

	public void setToqty(String toqty) {
		this.toqty = toqty;
	}

	public String getSqty() {
		return sqty;
	}

	public void setSqty(String sqty) {
		this.sqty = sqty;
	}

	public String getContract() {
		return contract;
	}

	public void setContract(String contract) {
		this.contract = contract;
	}

	/** Column: fee. 優待後水費(未減回饋費) */
	public String getFee() {
		return fee;
	}

	/** Column: fee. 優待後水費(未減回饋費) */
	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getPbfee() {
		return pbfee;
	}

	public void setPbfee(String pbfee) {
		this.pbfee = pbfee;
	}
	
	/** Column: basicfee. 基本費 */
	public String getBasicfee() {
		return basicfee;
	}

	/** Column: basicfee. 基本費 */
	public void setBasicfee(String basicfee) {
		this.basicfee = basicfee;
	}

	/**
	 * Property of allowance (the value is from WaterFee DB basically)
	 * @return 電子帳單折扣金/電子帳單回饋金
	 */
	public String getAllowance() {
		return allowance;
	}

	/**
	 * Property of allowance (the value is from WaterFee DB basically)
	 * @param allowance 電子帳單折扣金/電子帳單回饋金
	 */
	public void setAllowance(String allowance) {
		this.allowance = allowance;
	}

	public String getAllow2() {
		return allow2;
	}

	public void setAllow2(String allow2) {
		this.allow2 = allow2;
	}

	public String getTax() {
		return tax;
	}

	public void setTax(String tax) {
		this.tax = tax;
	}

	/**
	 * 清除處理費
	 * @return
	 */
	public String getServ() {
		return serv;
	}

	/**
	 * 清除處理費
	 * @param serv
	 */
	public void setServ(String serv) {
		this.serv = serv;
	}

	public String getPbserv() {
		return pbserv;
	}

	public void setPbserv(String pbserv) {
		this.pbserv = pbserv;
	}

	public String getDirty() {
		return dirty;
	}

	public void setDirty(String dirty) {
		this.dirty = dirty;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	/**
	 * @return 應收水源保育費(含加退)
	 */
	public String getPreserve() {
		return preserve;
	}

	/**
	 * @param preserve 應收水源保育費(含加退)
	 */
	public void setPreserve(String preserve) {
		this.preserve = preserve;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getPrefee() {
		return prefee;
	}

	public void setPrefee(String prefee) {
		this.prefee = prefee;
	}

	/** 上期預繳餘額 */
	public String getLastfee() {
		return lastfee;
	}

	/** 上期預繳餘額 */
	public void setLastfee(String lastfee) {
		this.lastfee = lastfee;
	}

	/** 遲延繳付費 */
	public String getDelayfee() {
		return delayfee;
	}

	/** 遲延繳付費 */
	public void setDelayfee(String delayfee) {
		this.delayfee = delayfee;
	}

	/** 本期收費日 */
	public String getBillDate() {
		return billDate;
	}

	/** 本期收費日 */
	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}

	/** 限繳日期 */
	public String getDuedate() {
		return duedate;
	}

	/** 限繳日期 */
	public void setDuedate(String duedate) {
		this.duedate = duedate;
	}

	/**	送行庫扣帳日期 */
	public String getDeduct() {
		return deduct;
	}

	/**	送行庫扣帳日期 */
	public void setDeduct(String deduct) {
		this.deduct = deduct;
	}

	/**	Column: lastmetered. 上期抄表日 */
	public String getLastMeteredDate() {
		return lastMeteredDate;
	}

	/**	Column: lastmetered. 上期抄表日 */
	public void setLastMeteredDate(String lastMeteredDate) {
		this.lastMeteredDate = lastMeteredDate;
	}

	/**	Column: metered. 本期抄表日 */
	public String getMeteredDate() {
		return meteredDate;
	}

	/**	Column: metered. 本期抄表日 */
	public void setMeteredDate(String meteredDate) {
		this.meteredDate = meteredDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAc2() {
		return ac2;
	}

	public void setAc2(String ac2) {
		this.ac2 = ac2;
	}

	/**
	 * 本期指針數
	 * @return
	 */
	public String getReading() {
		return reading;
	}

	/**
	 * 本期指針數
	 * @param reading
	 */
	public void setReading(String reading) {
		this.reading = reading;
	}

	/** Column: lastread. 主表上月指針 / 上期指針數 */
	public String getLastread() {
		return lastread;
	}

	/** Column: lastread. 主表上月指針 */
	public void setLastread(String lastread) {
		this.lastread = lastread;
	}

	/**	Column: nextmetered. 下期抄表日 */
	public String getNextMeteredDate() {
		return nextMeteredDate;
	}

	/**	Column: nextmetered. 下期抄表日 */
	public void setNextMeteredDate(String nextMeteredDate) {
		this.nextMeteredDate = nextMeteredDate;
	}

	public String getNextBillDate() {
		return nextBillDate;
	}

	public void setNextBillDate(String nextBillDate) {
		this.nextBillDate = nextBillDate;
	}

	public String getAc1() {
		return ac1;
	}

	public void setAc1(String ac1) {
		this.ac1 = ac1;
	}

	public String getMasterNO() {
		return masterNO;
	}

	public void setMasterNO(String masterNO) {
		this.masterNO = masterNO;
	}

	public String getMasterReading() {
		return masterReading;
	}

	public void setMasterReading(String masterReading) {
		this.masterReading = masterReading;
	}

	public String getMasterLastread() {
		return masterLastread;
	}

	public void setMasterLastread(String masterLastread) {
		this.masterLastread = masterLastread;
	}

	public String getMasterSqty() {
		return masterSqty;
	}

	public void setMasterSqty(String masterSqty) {
		this.masterSqty = masterSqty;
	}

	public String getRule33() {
		return rule33;
	}

	public void setRule33(String rule33) {
		this.rule33 = rule33;
	}

	public String getFlood50() {
		return flood50;
	}

	public void setFlood50(String flood50) {
		this.flood50 = flood50;
	}

	public String getAllow33() {
		return allow33;
	}

	public void setAllow33(String allow33) {
		this.allow33 = allow33;
	}

	public String getAllow50() {
		return allow50;
	}

	public void setAllow50(String allow50) {
		this.allow50 = allow50;
	}

	public String getSaveRatio() {
		return saveRatio;
	}

	public void setSaveRatio(String saveRatio) {
		this.saveRatio = saveRatio;
	}

	public String getSaveAllow() {
		return saveAllow;
	}

	public void setSaveAllow(String saveAllow) {
		this.saveAllow = saveAllow;
	}

	public String getLastAvgQty() {
		return lastAvgQty;
	}

	public void setLastAvgQty(String lastAvgQty) {
		this.lastAvgQty = lastAvgQty;
	}

	public String getAvgQty() {
		return avgQty;
	}

	public void setAvgQty(String avgQty) {
		this.avgQty = avgQty;
	}

	/**
	 * 固定補助費說明 from PdfDataOnOrgProof.java. 就是一個說明from db，不一定固定。
	 * eg. 回饋設置垃圾場, 楠梓大社地區中油, 電子帳單回饋金, 嘉義鹿草地區補助, 蘭嶼鄉補助款, NULL
	 * @return
	 */
	public String getAllowanceSta() {
		return allowanceSta;
	}
	
	/**
	 * 固定補助費說明 from PdfDataOnOrgProof.java. 就是一個說明from db，不一定固定。
	 * eg. 回饋設置垃圾場, 楠梓大社地區中油, 電子帳單回饋金, 嘉義鹿草地區補助, 蘭嶼鄉補助款, (NULL)
	 * @param allowanceSta
	 */
	public void setAllowanceSta(String allowanceSta) {
		this.allowanceSta = allowanceSta;
	}

	/**
	 * 臨時補助費說明 from PdfDataOnOrgProof.java. 就是一個說明，不一定臨時。
	 * eg. 電子帳單回饋金, (NULL)
	 * @return
	 */
	public String getAllow2Sta() {
		return allow2Sta;
	}

	/**
	 * 臨時補助費說明 from PdfDataOnOrgProof.java. 就是一個說明，不一定臨時。
	 * eg. 電子帳單回饋金, (NULL)
	 * @param allow2Sta
	 */
	public void setAllow2Sta(String allow2Sta) {
		this.allow2Sta = allow2Sta;
	}

	public String getLevy() {
		return levy;
	}

	public void setLevy(String levy) {
		this.levy = levy;
	}

	
	public String getMno() {
		return mno;
	}

	/**
	 * 水表表號。
	 * @param mno
	 */
	
	public void setMno(String mno) {
		this.mno = mno;
	}
	
	public String getChagWorkArea1() {
		return chagWorkArea1;
	}

	public void setChagWorkArea1(String chagWorkArea1) {
		this.chagWorkArea1 = chagWorkArea1;
	}

	public String getChagWorkArea2() {
		return chagWorkArea2;
	}

	public void setChagWorkArea2(String chagWorkArea2) {
		this.chagWorkArea2 = chagWorkArea2;
	}

	public String getChagHandScnum() {
		return chagHandScnum;
	}

	public void setChagHandScnum(String chagHandScnum) {
		this.chagHandScnum = chagHandScnum;
	}

	public String getChagHandSpnum() {
		return chagHandSpnum;
	}

	public void setChagHandSpnum(String chagHandSpnum) {
		this.chagHandSpnum = chagHandSpnum;
	}

	public String getChagDsby() {
		return chagDsby;
	}

	public void setChagDsby(String dsby) {
		this.chagDsby = dsby;
	}

	public String getChagBillScount() {
		return chagBillScount;
	}

	public void setChagBillScount(String chagBillScount) {
		this.chagBillScount = chagBillScount;
	}

	public String getChagDsby2() {
		return chagDsby2;
	}

	public void setChagDsby2(String dsby2) {
		this.chagDsby2 = dsby2;
	}

	public String getChagBillNO1() {
		return chagBillNO1;
	}

	public void setChagBillNO1(String chagBillNO1) {
		this.chagBillNO1 = chagBillNO1;
	}

	public String getChagBillNO2() {
		return chagBillNO2;
	}

	public void setChagBillNO2(String chagBillNO2) {
		this.chagBillNO2 = chagBillNO2;
	}

	/**
	 * 水源保育費
	 * @return
	 */
	public String getChagBaoyu() {
		return chagBaoyu;
	}

	/**
	 * 水源保育費
	 * @param chagBaoyu
	 */
	public void setChagBaoyu(String chagBaoyu) {
		this.chagBaoyu = chagBaoyu;
	}

	public String getChagBaoyu2() {
		return chagBaoyu2;
	}

	public void setChagBaoyu2(String chagBaoyu2) {
		this.chagBaoyu2 = chagBaoyu2;
	}

	public String getChagWatrPeriod() {
		return chagWatrPeriod;
	}

	public void setChagWatrPeriod(String chagWatrPeriod) {
		this.chagWatrPeriod = chagWatrPeriod;
	}

	public String getChagMetrState() {
		return chagMetrState;
	}

	public void setChagMetrState(String chagMetrState) {
		this.chagMetrState = chagMetrState;
	}

	/**
	 * @return 服務站所名稱
	 */
	public String getWaterStationName() {
		return waterStationName;
	}

	/**
	 * @param waterStationName 服務站所名稱
	 */
	public void setWaterStationName(String waterStationName) {
		this.waterStationName = waterStationName;
	}

	public String getWaterStationTel() {
		return waterStationTel;
	}

	public void setWaterStationTel(String waterStationTel) {
		this.waterStationTel = waterStationTel;
	}

	/**
	 * @return 服務站所地址
	 */
	public String getWaterStationAddr() {
		return waterStationAddr;
	}

	/**
	 * @param waterStationAddr 服務站所地址
	 */
	public void setWaterStationAddr(String waterStationAddr) {
		this.waterStationAddr = waterStationAddr;
	}

	public String getWaterStationTel2() {
		return waterStationTel2;
	}

	public void setWaterStationTel2(String waterStationTel2) {
		this.waterStationTel2 = waterStationTel2;
	}

	/**
	 * @return 統一編號/統編
	 */
	public String getWrbaInvoNO() {
		return wrbaInvoNO;
	}

	/**
	 * @param wrbaInvoNO 統一編號/統編
	 */
	public void setWrbaInvoNO(String wrbaInvoNO) {
		this.wrbaInvoNO = wrbaInvoNO;
	}

	public String getWrbaOthrMark1() {
		return wrbaOthrMark1;
	}

	public void setWrbaOthrMark1(String wrbaOthrMark1) {
		this.wrbaOthrMark1 = wrbaOthrMark1;
	}

	public String getWrbaPostCode2() {
		return wrbaPostCode2;
	}

	public void setWrbaPostCode2(String wrbaPostCode2) {
		this.wrbaPostCode2 = wrbaPostCode2;
	}

	public String getWrbaEmpeAddr2() {
		return wrbaEmpeAddr2;
	}

	public void setWrbaEmpeAddr2(String wrbaEmpeAddr2) {
		this.wrbaEmpeAddr2 = wrbaEmpeAddr2;
	}

	public String getWrbaComuAddr() {
		return wrbaComuAddr;
	}

	
	public String getchagCityCode() {
		return chagCityCode;
	}

	public void setchagCityCode(String chagCityCode) {
		this.chagCityCode = chagCityCode;
	}
	
	
	
	public void setWrbaComuAddr(String wrbaComuAddr) {
		this.wrbaComuAddr = wrbaComuAddr;
	}

	public void setRealfee(String realfee) {
		this.realfee = realfee;
	}

	public String getPaymentDeadlineMonth() {
		return paymentDeadlineMonth;
	}

	public void setPaymentDeadlineMonth(String paymentDeadlineMonth) {
		this.paymentDeadlineMonth = paymentDeadlineMonth;
	}

	/**
	 * 取得代收繳費期限的Date物件
	 * @return
	 */
	public Date getReceivingDuedate() {
		return receivingDuedate;
	}

	/**
	 * 設定代收繳費期限日期Date物件,同時會設定代收期限日期民國日期字串，ex:0990321 or 1011021
	 * @param receivingDuedate
	 */
	public void setReceivingDuedate(Date receivingDuedate) {
		this.receivingDuedate = receivingDuedate;
		this.receivingDuedateRocString = DateTool.dateToRocString(receivingDuedate);
	}

	/**
	 * 取得代收銷帳編號
	 * @return
	 */
	public String getWriteOffNumber() {
		return writeOffNumber;
	}

	/**
	 * 設定銷帳編號
	 * @param writeOffNumber
	 */
	public void setWriteOffNumber(String writeOffNumber) {
		this.writeOffNumber = writeOffNumber;
	}

	public String getCheckingCode() {
		return checkingCode;
	}

	public void setCheckingCode(String checkingCode) {
		this.checkingCode = checkingCode;
	}

	/**
	 * 取得代收繳費期限的民國年
	 * @return
	 */
	public String getReceivingDuedateRocYear() {
		Validate.notEmpty(getReceivingDuedateRocString(), "代收繳費期限roc string為empty, 無法取得民國年!");
		return getReceivingDuedateRocString().substring(0, 3);
	}

	/**
	 * 取得代收繳費期限的兩位數月份,ex:09 or 10 
	 * @return
	 */
	public String getReceivingDuedateRocMonth() {
		Validate.notNull(getReceivingDuedateRocString(), "代收繳費期限roc string為empty, 無法取得民國月!");
		return getReceivingDuedateRocString().substring(3, 5);
	}

	public String getReceivingDuedateRocString() {
		return receivingDuedateRocString;
	}

	public void setReceivingDuedateRocString(String receivingDuedateRocString) {
		this.receivingDuedateRocString = receivingDuedateRocString;
	}

	/**
	 * @return the stampDate
	 */
	public String getStampDate() {
		return stampDate;
	}

	/**
	 * @param stampDate the stampDate to set
	 */
	public void setStampDate(String stampDate) {
		this.stampDate = stampDate;
	}

	/** Column: mstus. 水表情況代碼 / 註記代碼 */
	public String getMstus() {
		return mstus;
	}

	/** Column: mstus. 水表情況代碼 / 註記代碼 */
	public void setMstus(String mstus) {
		this.mstus = mstus;
	}

	/**
	 * @return the chagCurnDate
	 */
	public String getChagCurnDate() {
		return chagCurnDate;
	}

	/**
	 * @param chagCurnDate the chagCurnDate to set
	 */
	public void setChagCurnDate(String chagCurnDate) {
		this.chagCurnDate = chagCurnDate;
	}
		
	public String getLastChagRealScale(){
		return last_chag_real_scale;	
	}
	
	public void setLastChagRealScale(String last_chag_real_scale) {
		this.last_chag_real_scale = last_chag_real_scale;
	}
	
	public String getChgSuppArea(){		
		return chg_supp_area;	
	}
	
	public void setChgSuppArea(String chg_supp_area){
		this.chg_supp_area = chg_supp_area;
	}

	public String getWaterCutoffDedcFeeWtTax() {
		return this.waterCutoffDedcFeeWtTax;
	}

	public void setWaterCutoffDedcFee(String waterCutoffDedcFee) {
		this.waterCutoffDedcFeeWtTax = waterCutoffDedcFee;
	}
	
	public String getPaiDdt() {
		return paiddt;
	}

	public void setPaiDdt(String paiDdt) {
		
		if (paiDdt.length()==7)
		{   
			//103 126
			String tmp=paiDdt;
			tmp=paiDdt.substring(0,3)+"/"+paiDdt.substring(3,5)+"/"+paiDdt.substring(paiDdt.length()-2);
			this.paiddt = tmp;
		}else{
			this.paiddt = paiDdt;
		}
	}

	/**
	 * @return the tempNoce - 有時候需要加上的通知字樣 
	 */
	public String getTempNote() {
		return tempNote;
	}

	/**
	 * @param tempNote the tempNote to set - 有時候需要加上的通知字樣 
	 */
	public void setTempNote(String tempNote) {
		this.tempNote = tempNote;
	}
	
	/**
	 * @param tempNote the tempNote to set - 有時候需要加上的通知字樣 
	 */
	public String getDiscountamt() {
		return discountamt;
	}
	
	public void setDiscountamt(String discountAmt){
		this.discountamt = discountAmt;
	}
	
	
	/** Column: CARRIERID. 載具編號 */
	public String getCarrierID() {
		return carrierid;
	}

	/** Column: CARRIERID. 載具編號 */
	public void setCarrierID(String carrierid) {
		this.carrierid = carrierid;
	}
	
	/** Column: INVOICENO. 發票號碼 */
	public String getInvoiceNo() {
		return invoiceno;
	}

	/** Column: INVOICENO. 發票號碼 */
	public void setInvoiceNo(String invoiceno) {
		this.invoiceno = invoiceno;
	}
	
	
	/** Column: INVOICENO. 發票號碼 */
	public String getInvoiceNo2() {
		return invoiceno2;
	}

	/** Column: INVOICENO. 發票號碼 */
	public void setInvoiceNo2(String invoiceno2) {
		this.invoiceno2 = invoiceno2;
	}
	
	public String getLastYYYMM(){
		return lastyyymm;
	}
	
	public void setLastYYYMM(String lastyyymm) {
		this.lastyyymm = lastyyymm;
	}
	
	/** Column: CARRIERID_ORIG. 原始載具編號 20170428 by Karen*/
	public String getCarrierOrigID() {
		return carrieridorig;
	}

	/** Column: CARRIERID_ORIG. 原始載具編號 20170428  by Karen*/
	public void setCarrierOrigID(String carrieridorig) {
		this.carrieridorig = carrieridorig;
	}
	
	/** Column: OP_AMT. 操作維護費 20190305 by Karen*/
	public String getOP_AMT() {
		return OP_AMT;
	}

	/** Column: OP_AMT. 操作維護費 20190305  by Karen*/
	public void setOP_AMT(String OP_AMT) {
		this.OP_AMT = OP_AMT;
	}
	
	/** Column: OP_AMT2. 加退操作維護費 20190305 by Karen*/
	public String getOP_AMT2() {
		return OP_AMT2;
	}

	/** Column: OP_AMT2. 加退操作維護費 20190305  by Karen*/
	public void setOP_AMT2(String OP_AMT2) {
		this.OP_AMT2 = OP_AMT2;
	}
	
	/** Column: MB_AMT. 工程改善費 20190305 by Karen*/
	public String getMB_AMT() {
		return MB_AMT;
	}

	/** Column: MB_AMT. 工程改善費 20190305  by Karen*/
	public void setMB_AMT(String MB_AMT) {
		this.MB_AMT = MB_AMT;
	}
	
	
	/** Column: MB_AMT. 加退工程改善費 20190305 by Karen*/
	public String getMB_AMT2() {
		return MB_AMT2;
	}

	/** Column: MB_AMT. 加退工程改善費 20190305  by Karen*/
	public void setMB_AMT2(String MB_AMT2) {
		this.MB_AMT2 = MB_AMT2;
	}
	
	/** Column: FOUL_AMT2. 加退汙水下水道使用費 20190621 by Karen*/
	public String getFOUL_AMT2() {
		return FOUL_AMT2;
	}

	/** Column: FOUL_AMT2. 加退汙水下水道使用費 20190621  by Karen*/
	public void setFOUL_AMT2(String FOUL_AMT2) {
		this.FOUL_AMT2 = FOUL_AMT2;
	}
	
	
	/** Column: WAEE_SUBSIDY. 國防部補助金額 20190806 by Karen*/
	public String getWAEE_SUBSIDY() {
		return WAEE_SUBSIDY;
	}

	/** Column: WAEE_SUBSIDY.國防部補助金額 20190806  by Karen*/
	public void setWAEE_SUBSIDY(String WAEE_SUBSIDY) {
		this.WAEE_SUBSIDY = WAEE_SUBSIDY;
	}
	
	
	
	/** Column: SREAD1. 副表本月指針20210407 by Karen*/
	public String getSREAD1() {
		return SREAD1;
	}

	/** Column: SREAD1.副表本月指針 20210407  by Karen*/
	public void setSREAD1(String SREAD1) {
		this.SREAD1 = SREAD1;
	}
	
	
	/** Column: WAEE_SUBSIDY. 副表上月指針 20210407 by Karen*/
	public String getSREAD2() {
		return SREAD2;
	}

	/** Column: SREAD2.副表上月指針 20210407  by Karen*/
	public void setSREAD2(String SREAD2) {
		this.SREAD2 = SREAD2;
	}
	
	
	
	
	
	public String getSTQTY() {//分表總實用度數
		return stqty;
	}

	public void setSTQTY(String stqty) {//分表總實用度數
		this.stqty = stqty;
	}
	
	
	public String getBRANCH() {//總表分表判斷
		return branch;
	}

	public void setBRANCH(String branch) {//總表分表判斷
		this.branch = branch;
	}
	
	
	
	public String getReGenFlag(){
		return regenflag;
	}
	public void setReGenFlag(String regenflag) {
		this.regenflag = regenflag;
	}
	
	public String getIsW890Paid(){
		return isW890Paid;
	}
	public void setIsW890Paid(String isW890Paid) {
		this.isW890Paid = isW890Paid;
	}
	
	public String getIsW880Paid(){
		return isW880Paid;
	}
	public void setIsW880Paid(String isW880Paid) {
		this.isW880Paid = isW880Paid;
	}
	
	public String getW880Total(){
		return W880Total;
	}
	public void setW880Total(String W880Total) {
		this.W880Total = W880Total;
	}
	
	
	public String getLAST_DAVGQ(){//上期日平均用水度數
		return LAST_DAVGQ;
	}
	public void setLAST_DAVGQ(String LAST_DAVGQ) {
		this.LAST_DAVGQ = LAST_DAVGQ;
	}
	public String getLAST_REAL_QTY(){//上期實用度數
		return LAST_REAL_QTY;
	}
	public void setLAST_REAL_QTY(String LAST_REAL_QTY) {
		this.LAST_REAL_QTY = LAST_REAL_QTY;
	}
	
	public String getLYEAR_REAL_QTY(){//去年同期實用度數
		return LYEAR_REAL_QTY;
	}
	public void setLYEAR_REAL_QTY(String LYEAR_REAL_QTY) {
		this.LYEAR_REAL_QTY = LYEAR_REAL_QTY;
	}
	
	public String getNPOBAN() {//愛心碼
		return NPOBAN;
	}

	public void setNPOBAN(String NPOBAN) {//愛心碼
		this.NPOBAN = NPOBAN;
	}
	
	
	public String getM_SHSQTY(){//分攤總度數
		return M_SHSQTY;
	}
	public void setM_SHSQTY(String M_SHSQTY) {
		this.M_SHSQTY = M_SHSQTY;
	}
	
	
}
