package com.cluster.pos.providers.smartpeak.p1000.pboc.pinpad;




/**
 * 
 * 
 * @author hz
 * @date 20160504
 * @version 1.0.0
 * @function
 * @lastmodify
 */
public interface OnPinDialogListener {
	int OK = 0; /* 正确输入密码之后返回 */
	int CANCEL = 1; /* 取消输入密码返回 */
	void OnPinInput(int result);
	void OnCreateOver();
}
