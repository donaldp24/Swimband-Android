package com.aquaticsafetyconceptsllc.iswimband;

public class Msg {
	public static final long kMagic      = 0x69446576L; // iDev
	public static final int kMsgVersion = 1;


	public enum msg_e{
	    READY,				    // 0
	    GET_BANDS,              // 1
	    PUSH_BANDS,             // 2
	    GET_BAND_STATUS,        // 3
		PUSH_BAND_STATUS,       // 4
	    MSG_ID_MAX             // 5
	};

	public enum band_state_e {
	    BAND_STATE_UNKNOWN, //      = 0,
	    BAND_STATE_NOT_PRESENT, //  = 1,
	    BAND_STATE_DISCOVERED, //   = 2,
	    BAND_STATE_CONNECTED, //    = 3,
	    BAND_STATE_DISCONNECTED// = 4,
	};

	public enum device_state_e {
	    DEVICE_STATE_UNKNOWN, //      = 0,
	    DEVICE_STATE_NOT_PRESENT, //  = 1,
	    DEVICE_STATE_DISCOVERED, //   = 2,
	    DEVICE_STATE_CONNECTED, //    = 3,
	    DEVICE_STATE_DISCONNECTED// = 4,
	};

	public class msg_header_t{
	    int		    magic;
		int         msgId;
	    int         msgLen;
	    int         msgVersion;
	};

	public class ready_msg_t {
	    msg_header_t hdr = new msg_header_t();
	    char[] deviceName = new char[64];
	    char[] appVersion = new char[64];
	};

	public class get_bands_msg_t{
	    msg_header_t hdr = new msg_header_t();
	};
	
	public class band_t {
	    char[] bandName = new char[64];
	    char[] masterName = new char[64];
	    band_state_e currentState;
	} ;

	public class push_bands_msg_t{
	    msg_header_t hdr = new msg_header_t();
	    int numBands;
	    band_t band = new band_t();
	} ;

	public class get_band_status_msg_t {
	    msg_header_t hdr = new msg_header_t();
	    char[] bandName = new char[64];
	} ;

	public class push_band_status_msg_t {
	    msg_header_t hdr = new msg_header_t();
	    band_t band = new band_t();
	} ;
}
