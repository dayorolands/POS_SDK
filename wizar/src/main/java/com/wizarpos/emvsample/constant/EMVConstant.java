package com.wizarpos.emvsample.constant;

public interface EMVConstant
{
	boolean debug = true;
	boolean enableLED = true;
	boolean enableBeep = true;

	String APP_TAG = "emvsample";
	String APP_VERSION = "000001";
	
	int MAX_CAPK = 40;
	int MAX_AID = 20;
	
	byte ONLINE_FAIL    = -1;
	byte ONLINE_DENIAL  =  0;
	byte ONLINE_SUCCESS =  1;
	
	int  DEFAULT_IDLE_TIME_SECONDS = 60;
	byte[] DEFAULT_KEY = {(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11};
	byte MAX_AMOUNT_LENGTH = 13;
	int IDLE_TIME_SECONDS = 60;

	
	/*-----  TRANSACTION TYPES  ---------------------------------*/
	byte TRAN_GOODS                     =  0;
	byte TRAN_SETTLE                    =  1;
	// offline
	byte QUERY_CARD_RECORD              =  2;
	
	byte QUERY_SPECIFIC                 =  3;
	byte QUERY_TRANS_DETAIL             =  4;

	
	// EMV TRANS
	byte EMV_TRANS_GOODS_SERVICE = 0x00;
	byte EMV_TRANS_CASH          = 0x01;
	byte EMV_TRANS_PRE_AUTH      = 0x03;
	byte EMV_TRANS_INQUIRY       = 0x04;
	byte EMV_TRANS_TRANSFER      = 0x05;
	byte EMV_TRANS_PAYMENT       = 0x06;
	byte EMV_TRANS_ADMIN         = 0x07;
	byte EMV_TRANS_CASHBACK      = 0x09;
	byte EMV_TRANS_CARD_RECORD   = 0x0A;
	byte EMV_TRANS_EC_BALANCE    = 0x0B;
	byte EMV_TRANS_LOAD_RECORD   = 0x0C;
	
	byte T_NOCAPTURE      = 0x02;
	byte T_NORECEIPT      = 0x04;
	byte T_OFFLINE        = 0x08;
	
	/*-----  parameter set TYPES  ---------------------------------*/
	byte PARAM_TID                = 1;
	byte PARAM_MID                = 2;
	byte PARAM_BATCH              = 3; // 设置批次号
	byte PARAM_TRACE              = 4; // 设置流水号(凭证号)
    byte PARAM_COMM_PRIMARY_IP    = 5;
    byte PARAM_COMM_PRIMARY_PORT  = 6;
    byte PARAM_MERCHANT_NAME      = 7;
    byte PARAM_UPLOAD_TYPE        = 8;
    byte PARAM_FORCE_ONLINE       = 9;
    byte PARAM_RECEIPT            =10;
    // EMV
	byte PARAM_COUNTRYCODE                      = 11;
    byte PARAM_IFD                              = 12;
    byte PARAM_CURRENCY_CODE                    = 13;
    byte PARAM_CURRENCY_EXPONENT                = 14;
    byte PARAM_TERMINAL_TYPE                    = 15;
    byte PARAM_TERMINAL_CAPABILITIES            = 16;
    byte PARAM_ADDITIONAL_TERMINAL_CAPABILITIES = 17;
    // QPBOC
	byte PARAM_TTQ                              = 18;
    byte PARAM_STATUS_CHECK                     = 19;
    // limit
	byte PARAM_EC_TERM_TRANS_LIMIT              = 20;
    byte PARAM_CONTACTLESS_LIMIT                = 21;
    byte PARAM_CONTACTLESS_FLOOR_LIMIT          = 22;
    byte PARAM_CVM_LIMIT                        = 23;
    
	// ProcessState
	byte PROCESS_NORMAL         = 0;
	byte PROCESS_CAPTURE_ONLINE = 1;
	byte PROCESS_CONFIMATION    = 2;
	byte PROCESS_REVERSAL       = 3;
	byte PROCESS_ADVICE_ONLINE  = 4;
	byte PROCESS_BATCH          = 5;
	byte PROCESS_ADVICE_OFFLINE = 6;
	
	  
	//----------------------------------------------------------------------
	//  Transaction ENTRY mode
	//----------------------------------------------------------------------
	byte MANUAL_ENTRY      = 1; //手工输入卡号
    byte SWIPE_ENTRY       = 2; //读取磁条卡号
    byte SCAN_ENTRY        = 3; //读取条形码
    byte INSERT_ENTRY      = (byte)0x80; //读取IC卡
    byte CONTACTLESS_ENTRY = 6;
    // PIN Mode
	byte CAN_PIN      = 0;  //可输密码
    byte CANNOT_PIN   = 1;  //不可输密码
    
    byte LOGON_MODE  = 3;
    byte LOGOFF_MODE = 4;
    byte TEST_MODE   = 5;
    byte CLOSE_MODE  = 6;

    byte TRACK2_ERROR = 0x10;
    byte TRACK1_ERROR = (TRACK2_ERROR << 1);
    byte TRACK3_ERROR = (TRACK2_ERROR << 2);

    byte TRY_CNT = 3;
    
    // transaction state
	byte STATE_REQUEST_CARD            = 1;
    byte STATE_CONFIRM_CARD            = 2;
	byte STATE_INPUT_AMOUNT            = 3;
    byte STATE_INPUT_EXPIRE_DATE       = 4;
    byte STATE_INPUT_TRANS_DATE        = 5;
    byte STATE_INPUT_TICKET            = 6;
    byte STATE_INPUT_RRN               = 7;
    byte STATE_INPUT_AUTH_CODE         = 8;
    byte STATE_INPUT_TIP               = 9;
    byte STATE_INPUT_ONLINE_PIN        =10;
    byte STATE_PROCESS_ONLINE          =11;
    byte STATE_INPUT_ADMIN_PASS        =12;
    byte STATE_REQUEST_CARD_ERROR      =13;
    byte STATE_SHOW_TRANS_INFO         =14;
	byte STATE_INPUT_OFFLINE_PIN       =15;
    byte STATE_PROCESS_EMV_CARD        =16;
    byte STATE_SELECT_EMV_APP          =17;
    byte STATE_CONFIRM_ID              =18;
    byte STATE_CONFIRM_BYPASS_PIN      =19;
    byte STATE_SHOW_EMV_CARD_TRANS     =20;
    byte STATE_CONFIRM_REFERRAL        =21;

    byte STATE_REMOVE_CARD             =32;
    
    byte STATE_TRANS_END               =40;
    
    // Activity Notifier
	int MSR_READ_DATA_NOTIFIER                =  1;
	int MSR_READ_ERROR_NOTIFIER               =  2;
	int MSR_OPEN_ERROR_NOTIFIER               =  3;
	int COMM_CONNECTING_NOTIFIER              =  4;
	int COMM_WRITE_DATA_NOTIFIER              =  5;
	int COMM_READ_DATA_NOTIFIER               =  6;
	int COMM_CONNECTED_NOTIFIER               =  7;
	int COMM_CONNECT_ERROR_NOTIFIER           =  8;
	int PACK8583_ERROR_NOTIFIER               =  9;
	int PACK8583_SUCCESS_NOTIFIER             = 10;

	int PIN_SUCCESS_NOTIFIER                  = 12;
	int PIN_ERROR_NOTIFIER                    = 13;
	int PIN_CANCELLED_NOTIFIER                = 14;
	int PIN_TIMEOUT_NOTIFIER                  = 15;
	int EMV_PROCESS_NEXT_COMPLETED_NOTIFIER   = 16;
	int REMOVE_CARD_NOTIFIER                  = 17;
	int CARD_OPEN_ERROR_NOTIFIER              = 18;
	int AID_INFO_CHANGED_NOTIFIER             = 19;
	int CAPK_INFO_CHANGED_NOTIFIER            = 20;
	int EXCEPTION_FILE_INFO_CHANGED_NOTIFIER  = 21;
	int REVOKED_CAPK_INFO_CHANGED_NOTIFIER    = 22;
	int CARD_INSERT_NOTIFIER                  = 23;
	int CARD_TAPED_NOTIFIER                   = 24;
	int CARD_REMOVE_NOTIFIER                  = 25;
	int CARD_ERROR_NOTIFIER                   = 26;
	int CONTACTLESS_HAVE_MORE_CARD_NOTIFIER   = 27;
	int PRINT_PAUSE_TIMER_NOTIFIER            = 28;

	int PREPROCESS_ERROR_NOTIFIER				= 29;
	int CARD_CONTACTLESS_ANTISHAKE			= 30;
	int OFFLINE_PIN_NOTIFIER                  = 31;

	// 通讯设备状态
	byte COMM_DISCONNECTED  = 0x00;
	byte COMM_CONNECTING    = 0x01;
	byte COMM_CONNECTED     = 0x02;
	
	int SMART_CARD_EVENT_INSERT_CARD = 0;
	int SMART_CARD_EVENT_REMOVE_CARD = 1;
//	final int SMART_CARD_EVENT_POWER_ON	   = 2;
//	final int SMART_CARD_EVENT_POWER_OFF   = 3;
int SMART_CARD_EVENT_POWERON_ERROR = 9;
	int SMART_CARD_EVENT_CONTALESS_HAVE_MORE_CARD  = 10;
	int 	SMART_CARD_EVENT_CONTALESS_ANTI_SHAKE	 = 11;
	
	// Key Type
	int SINGLE_KEY = 0;
	int DOUBLE_KEY = 1;
	
	// Timer Mode
	byte TIMER_IDLE = 0;
	byte TIMER_FINISH = 1;

	// EMV Kernel Type
	byte CONTACT_EMV_KERNAL = 1;
	byte CONTACTLESS_EMV_KERNAL = 2;

	// POLL Card Status
	byte WAIT_INSERT_CARD = 1;
	byte WAIT_REMOVE_CARD = 2;
	
	// IC Card Type
	int CARD_CONTACT     = 1;
	int CARD_CONTACTLESS = 2;

	// Pinpad Type
	int PINPAD_CUSTOM_UI = 1;
	int PINPAD_SYSTEM_UI = 2;
	int PINPAD_NONE      = 3;
	
	// EMV STATUS
	byte STATUS_ERROR    		= 0; //执行报错
  	byte STATUS_CONTINUE    	= 1; //还未完成
  	byte STATUS_COMPLETION 	= 2; //完成

  	// EMV Return Code
	byte EMV_START                    = 0;  // EMV Transaction Started
	byte EMV_CANDIDATE_LIST           = 1;  //
	byte EMV_APP_SELECTED             = 2;  // Application Select Completed
	byte EMV_READ_APP_DATA            = 3;  // Read Application Data Completed
	byte EMV_DATA_AUTH                = 4;  // Data Authentication Completed
	byte EMV_OFFLINE_PIN              = 5;
	byte EMV_ONLINE_ENC_PIN           = 6;  // notify Application prompt Caldholder enter Online PIN

	byte EMV_PROCESS_ONLINE           = 8;  // notify Application to Process Online
  	
  	byte APPROVE_OFFLINE              = 1;	/** Transaction approved Offline */
byte APPROVE_ONLINE               = 2;	/** Transaction approved Online */
byte DECLINE_OFFLINE              = 3;  	/** Transaction declined Offline */
byte DECLINE_ONLINE               = 4;	/** Transaction declined Online */

  	// emv error code
byte ERROR_NO_APP              =  1; // Selected Application do not in the Candidate List when Application Select
  	byte ERROR_CARD_BLOCKED        =  2;
  	byte ERROR_APP_SELECT          =  3; // Parse Card Returned Data Error when Application Select
  	byte ERROR_INIT_APP            =  4; // card return 6A81 when Application Select
  	byte ERROR_EXPIRED_CARD        =  5; // Error when Application Select
  	byte ERROR_APP_DATA            =  6; // Application Interchange Profile(AIP) and Application File Locator(AFL) not exist when Initialize Application
  	byte ERROR_DATA_INVALID        =  7; // Error when Initialize Application Data
  	byte ERROR_DATA_AUTH           =  8;
  	byte ERROR_GEN_AC              =  9;
  	byte ERROR_PROCESS_CMD         = 10; // Error when Read Application Data
  	byte ERROR_SERVICE_NOT_ALLOWED = 11; /** Service not Allowed */
byte ERROR_PINENTERY_TIMEOUT	 = 12; /** PIN Entry timeout */
byte ERROR_OFFLINE_VERIFY	     = 13; /** Check Offline PIN Error when Cardholder Verify */
byte ERROR_NEED_ADVICE         = 14; /** Communication Error with Host, but the card need advice, halted the transaction */
byte ERROR_USER_CANCELLED      = 15; /** User cancelled transaction  */
byte ERROR_AMOUNT_OVER_LIMIT   = 16;
  	byte ERROR_AMOUNT_ZERO         = 17;
  	byte ERROR_OTHER_CARD          = 18;
  	byte ERROR_APP_BLOCKED         = 20;
	byte ERROR_POWER_ON_AGAIN	     = 21;
    byte ERROR_CONTACT_DURING_CONTACTLESS = 22;

	byte ERROR_MSD_NOT_SUPPORTED   = 30;
	byte ERROR_AMOUNT_NOT_PRESENT  = 31;
	byte ERROR_CCC                 = 32;
	byte ERROR_EXCHANGE_RR_DATA    = 33;
	byte ERROR_GET_PDOL_DATA       = 34;
	byte ERROR_RESTART       	     = 35;
	byte ERROR_SEE_PHONE           = 36;
	byte ERROR_NEXT_AID            = 37;  // Used for AMEX Express or Discover ZIP: if Select PPSE return 6A82, Select AID List
	byte ERROR_ANOTHER_INTERFACE   = 38;

}
