package com.telpo.tps550.api.printer;

import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public class PT723F08401PrinterCore {
    public static int BitmapWidth = 0;
    private static int LBlank = 0;
    public static int PrintDataHeight = 0;
    private static int RBlank = 0;

    public static byte[] PrintDataFormat(Bitmap bmp) {
        try {
            return CompressPrintData(CreatePrintBitmpaData(bmp));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] CompressPrintData(byte[] pData) {
        int i;
        try {
            byte[] bArr = new byte[BitmapWidth];
            List<byte[]> AllDatas = new ArrayList<>();
            List<byte[]> LineDatas = new ArrayList<>();
            List<byte[]> BlankDatas = new ArrayList<>();
            for (int i2 = 0; i2 < PrintDataHeight; i2++) {
                boolean bBlankLine = true;
                int rowL = 0;
                int rowR = 0;
                int iRowColIdx = i2 * BitmapWidth;
                byte[] bOneLine = new byte[BitmapWidth];
                for (int j = 0; j < BitmapWidth; j++) {
                    byte OnePix = pData[iRowColIdx + j];
                    if (OnePix != 0) {
                        if (j == 0) {
                            rowL = 0;
                        } else if (rowL > rowR) {
                            rowL = rowR;
                        }
                        rowR = j;
                        bBlankLine = false;
                    }
                    bOneLine[j] = OnePix;
                }
                if (!bBlankLine) {
                    if (LBlank == 0) {
                        LBlank = rowL;
                    } else {
                        if (LBlank < rowL) {
                            i = LBlank;
                        } else {
                            i = rowL;
                        }
                        LBlank = i;
                    }
                    RBlank = RBlank < rowR ? rowR : RBlank;
                    int BlankDatasSize = BlankDatas.size();
                    if (BlankDatasSize > 0) {
                        if (BlankDatasSize > 24) {
                            if (LineDatas.size() > 0) {
                                AllDatas.add(TrimBitmapBlank(LineDatas));
                            }
                            AllDatas.add(CreateFeedLineCMD(BlankDatas));
                            LineDatas = new ArrayList<>();
                        } else {
                            LineDatas.addAll(BlankDatas);
                        }
                        BlankDatas = new ArrayList<>();
                    }
                    LineDatas.add(bOneLine);
                    if (LineDatas.size() == 100) {
                        AllDatas.add(TrimBitmapBlank(LineDatas));
                        LineDatas = new ArrayList<>();
                    }
                } else {
                    BlankDatas.add(bOneLine);
                }
            }
            int BlankDatasSize2 = BlankDatas.size();
            if (BlankDatasSize2 <= 0) {
                AllDatas.add(TrimBitmapBlank(LineDatas));
            } else if (BlankDatasSize2 > 24) {
                if (LineDatas.size() > 0) {
                    AllDatas.add(TrimBitmapBlank(LineDatas));
                }
                AllDatas.add(CreateFeedLineCMD(BlankDatas));
            } else {
                LineDatas.addAll(BlankDatas);
                AllDatas.add(TrimBitmapBlank(LineDatas));
            }
            return sysCopy(AllDatas);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] CreateFeedLineCMD(List<byte[]> BlankDatas) {
        try {
            List<byte[]> BL = new ArrayList<>();
            int iLineCnt = BlankDatas.size();
            for (int iLine = 0; iLine < iLineCnt; iLine += 240) {
                byte[] OneFeedCMD = new byte[3];
                OneFeedCMD[0] = 27;
                OneFeedCMD[1] = 74;
                if (iLineCnt - iLine > 240) {
                    OneFeedCMD[2] = -16;
                } else {
                    OneFeedCMD[2] = (byte) (((iLineCnt - iLine) * 8) / 8);
                }
                BL.add(OneFeedCMD);
            }
            return sysCopy(BL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] TrimBitmapBlank(List<byte[]> LineDatas) {
        try {
            int iDataWid = (RBlank - LBlank) + 1;
            int iLine = 0;
            int iLineCnt = LineDatas.size();
            int iPKIndex = 0;
            byte[] RtnData = new byte[((iDataWid * iLineCnt) + ((iLineCnt % 2300 > 0 ? (iLineCnt / 2300) + 1 : iLineCnt / 2300) * 8))];
            while (iLine < iLineCnt) {
                int iCurPackageLine = iLine + 2300 < iLineCnt ? 2300 : iLineCnt - iLine;
                int iCurPackageBegin = iPKIndex * 2308;
                RtnData[iCurPackageBegin] = 29;
                RtnData[iCurPackageBegin + 1] = 118;
                RtnData[iCurPackageBegin + 2] = 48;
                RtnData[iCurPackageBegin + 3] = 0;
                RtnData[iCurPackageBegin + 4] = (byte) (iDataWid % 256);
                RtnData[iCurPackageBegin + 5] = (byte) (iDataWid / 256);
                RtnData[iCurPackageBegin + 6] = (byte) (iCurPackageLine % 256);
                RtnData[iCurPackageBegin + 7] = (byte) (iCurPackageLine / 256);
                for (int l = iLine; l < iLineCnt; l++) {
                    System.arraycopy(LineDatas.get(l), LBlank, RtnData, (iPKIndex * 2308) + (l * iDataWid) + 8, iDataWid);
                }
                iLine += 2300;
                iPKIndex++;
            }
            LBlank = 0;
            RBlank = 0;
            return RtnData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] CreatePrintBitmpaData(Bitmap bmp) {
        int nDataIndex = 0;
        try {
            int nWidth = bmp.getWidth();
            int nHeight = bmp.getHeight();
            PrintDataHeight = nHeight;
            BitmapWidth = (nWidth % 8 == 0 ? nWidth : ((nWidth / 8) + 1) * 8) / 8;
            int nSize = nHeight * BitmapWidth;
            byte[] bPrintData = new byte[nSize];
            for (int i = 0; i < nSize; i++) {
                bPrintData[i] = 0;
            }
            for (int startLine = 0; startLine < nHeight; startLine++) {
                int[] arrPixColor = new int[nWidth];
                bmp.getPixels(arrPixColor, 0, nWidth, 0, startLine, nWidth, 1);
                int nBitIndex = 0;
                for (int w = 0; w < nWidth; w++) {
                    nBitIndex++;
                    int nPixColor = arrPixColor[w];
                    if (nBitIndex > 8) {
                        nBitIndex = 1;
                        nDataIndex++;
                    }
                    if (nPixColor != -1) {
                        int nTempValue = 1 << (8 - nBitIndex);
                        int r = Color.red(nPixColor);
                        int g = Color.green(nPixColor);
                        int b = Color.blue(nPixColor);
                        if (r < 128 || g < 128 || b < 128) {
                            bPrintData[nDataIndex] = (byte) (bPrintData[nDataIndex] | nTempValue);
                        }
                    }
                }
                nDataIndex = BitmapWidth * (startLine + 1);
            }
            return bPrintData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray2 : srcArrays) {
            System.arraycopy(srcArray2, 0, destArray, destLen, srcArray2.length);
            destLen += srcArray2.length;
        }
        return destArray;
    }
}
