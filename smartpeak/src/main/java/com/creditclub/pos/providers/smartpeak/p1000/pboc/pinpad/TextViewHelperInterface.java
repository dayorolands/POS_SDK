package com.creditclub.pos.providers.smartpeak.p1000.pboc.pinpad;




/**
 * 
 * 
 * @author hz
 * @date 20160504
 * @version 1.0.0
 * @function
 * @lastmodify
 */
public interface TextViewHelperInterface {
	void add(String tx);
	void addPins(int len, int key);
	void back();
	boolean isFinished();
	void clean();
	boolean isPwdCorrect(String correct);
}
