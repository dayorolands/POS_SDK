package com.telpo.tps550.api;

public class ErrorCode {
    private static final int BASE_PRN_ERR = 61696;
    private static final int BASE_SYS_ERR = 61440;
    public static final int ERR_INVALID_PARAM = -2;
    public static final int ERR_LOW_POWER = -3;
    public static final int ERR_PRN_FONT = 61699;
    public static final int ERR_PRN_GATE_OPEN = 61701;
    public static final int ERR_PRN_NOT_CUT = 61702;
    public static final int ERR_PRN_NO_BALCK_BLOCK = 61700;
    public static final int ERR_PRN_NO_PAPER = 61697;
    public static final int ERR_PRN_OVER_TEMP = 61698;
    public static final int ERR_SYS_ALREADY_INIT = 61444;
    public static final int ERR_SYS_INVALID = 61441;
    public static final int ERR_SYS_NOT_SUPPORT = 61447;
    public static final int ERR_SYS_NO_DEV = 61442;
    public static final int ERR_SYS_NO_INIT = 61443;
    public static final int ERR_SYS_NO_PERMISSION = 61449;
    public static final int ERR_SYS_OVER_FLOW = 61445;
    public static final int ERR_SYS_TIMEOUT = 61446;
    public static final int ERR_SYS_UNEXPECT = 61448;
    public static final int ERR_UNEXPECT = -1;
    public static final int OK = 0;
}
