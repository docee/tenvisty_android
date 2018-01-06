/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2013. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.hichip;

import android.util.Log;

public class HiSmartWifiSet {
	
	public static final byte AuthModeOpen     = 0x00;
	public static final byte AuthModeWEPOPEN  = 0x01;
	public static final byte AuthModeWEPSHARE = 0x02;
	
	public static final byte AuthModeWPAEAP = 0x03;
	public static final byte AuthModeWPA2EAP = 0x04;
	public static final byte AuthModeWPA1EAPWPA2EAP = 0x05;
	
	public static final byte AuthModeWPAPSKAES = 0x06;
	public static final byte AuthModeWPAPSKTKIP = 0x07;
	public static final byte AuthModeWPAPSKTKIPAES = 0x08;

	public static final byte AuthModeWPA2PSKAES = 0x09;
	public static final byte AuthModeWPA2PSKTKIP = 10;
	public static final byte AuthModeWPA2PSKTKIPAES = 11;

	public static final byte AuthModeWPA1PSKWPA2PSKAES = 12;
	public static final byte AuthModeWPA1PSKWPA2PSKTKIP = 13;
	public static final byte AuthModeWPA1PSKWPA2PSKTKIPAES = 14;
	
 
    
    /**
     * Start SmartConnection with Home AP
     * 
     * @SSID : SSID of Home AP
	 * @Password : Password of Home AP
	 * @Auth : Auth of Home AP
     */
    public static native int HiStartSmartConnection(String SSID, String Password, byte Auth);
    
	/**
     * Stop SmartConnection by user
     * 
     */
	public static native int HiStopSmartConnection();
	 
	
	static {
		Log.d("tag", "install  libHiSmartWifiSet.so");
        System.loadLibrary("HiSmartWifiSet");
    }
	
}