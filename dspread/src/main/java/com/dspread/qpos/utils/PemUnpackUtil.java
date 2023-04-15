package com.dspread.qpos.utils;

import java.util.Map;

public class PemUnpackUtil {

    /** 用途文本. 如“BEGIN PUBLIC KEY”中的“PUBLIC KEY”. */
    public final static String PURPOSE_TEXT = "PURPOSE_TEXT";
    /** 用途代码. R私钥， U公钥. */
    public final static String PURPOSE_CODE = "PURPOSE_CODE";

    /** PEM解包.
     *
     * <p>从PEM密钥数据中解包得到纯密钥数据. 即去掉BEGIN/END行，并作BASE64解码. 若没有BEGIN/END, 则直接做BASE64解码.</p>
     *
     * @param data  源数据.
     * @param otherresult   其他返回值. 支持 PURPOSE_TEXT, PURPOSE_CODE。
     * @return  返回解包后的纯密钥数据.
     */
    public static String PemUnpack(String data, Map<String, String> otherresult) {
        String rt = null;
        final String SIGN_BEGIN = "-BEGIN";
        final String SIGN_END = "-END";
        int datelen = data.length();
        String purposetext = "";
        String purposecode = "";
        if (null!=otherresult) {
            purposetext = otherresult.get(PURPOSE_TEXT);
            purposecode = otherresult.get(PURPOSE_CODE);
            if (null==purposetext) purposetext= "";
            if (null==purposecode) purposecode= "";
        }
        // find begin.
        int bodyPos = 0;    // 主体内容开始的地方.
        int beginPos = data.indexOf(SIGN_BEGIN);
        if (beginPos>=0) {
            // 向后查找换行符后的首个字节.
            boolean isFound = false;
            boolean hadNewline = false; // 已遇到过换行符号.
            boolean hyphenHad = false;  // 已遇到过“-”符号.
            boolean hyphenDone = false; // 已成功获取了右侧“-”的范围.
            int p = beginPos + SIGN_BEGIN.length();
            int hyphenStart = p;    // 右侧“-”的开始位置.
            int hyphenEnd = hyphenStart;    // 右侧“-”的结束位置. 即最后一个“-”字符的位置+1.
            while(p<datelen) {
                char ch = data.charAt(p);
                // 查找右侧“-”的范围.
                if (!hyphenDone) {
                    if (ch=='-') {
                        if (!hyphenHad) {
                            hyphenHad = true;
                            hyphenStart = p;
                            hyphenEnd = hyphenStart;
                        }
                    } else {
                        if (hyphenHad) { // 无需“&& !hyphenDone”，因为外层判断了.
                            hyphenDone = true;
                            hyphenEnd = p;
                        }
                    }
                }
                // 向后查找换行符后的首个字节.
                if (ch=='\n' || ch=='\r') {
                    hadNewline = true;
                } else {
                    if (hadNewline) {
                        // 找到了.
                        bodyPos = p;
                        isFound = true;
                        break;
                    }
                }
                // next.
                ++p;
            }
            // purposetext
            if (hyphenDone && null!=otherresult) {
                purposetext = data.substring(beginPos + SIGN_BEGIN.length(), hyphenStart).trim();
                String purposetextUp = purposetext.toUpperCase();
                if (purposetextUp.indexOf("PRIVATE")>=0) {
                    purposecode = "R";
                } else if (purposetextUp.indexOf("PUBLIC")>=0) {
                    purposecode = "U";
                }
                otherresult.put(PURPOSE_TEXT, purposetext);
                otherresult.put(PURPOSE_CODE, purposecode);
            }
            // bodyPos.
            if (isFound) {
                //OK.
            } else if (hyphenDone) {
                // 以右侧右侧“-”的结束位置作为主体开始.
                bodyPos = hyphenEnd;
            } else {
                // 找不到结束位置，只能退出.
                return rt;
            }
        }
        // find end.
        int bodyEnd = datelen;  // 主体内容的结束位置. 即最后一个字符的位置+1.
        int endPos = data.indexOf(SIGN_END, bodyPos);
        if (endPos>=0) {
            // 向前查找换行符前的首个字节.
            boolean isFound = false;
            boolean hadNewline = false;
            int p = endPos-1;
            while(p >= bodyPos) {
                char ch = data.charAt(p);
                if (ch=='\n' || ch=='\r') {
                    hadNewline = true;
                } else {
                    if (hadNewline) {
                        // 找到了.
                        bodyEnd = p+1;
                        break;
                    }
                }
                // next.
                --p;
            }
            if (!isFound) {
                // 忽略.
            }
        }
        // get body.
        if (bodyPos>=bodyEnd) {
            return rt;
        }
        String body = data.substring(bodyPos, bodyEnd).trim();
        return body;
    }


}
