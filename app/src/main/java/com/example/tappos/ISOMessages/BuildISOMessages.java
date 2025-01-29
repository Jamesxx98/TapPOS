package com.example.tappos.ISOMessages;

import static com.example.tappos.GlobalFunctions.Custom.getDateTime;

import com.example.tappos.GlobalFunctions.Custom;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

public class BuildISOMessages {

    public static void buildCustomMessage(ISOMsg isoMsg, String cardNumber, String expDate, String amount) throws ISOException {
        isoMsg.setMTI("0100");
        isoMsg.set(2, cardNumber);
        isoMsg.set(3, "300000");
        isoMsg.set(4, Custom.formatAmount(amount));
        isoMsg.set(11, ISOUtil.zeropad("000001", 6));
        isoMsg.set(12, getDateTime()[1]);
        isoMsg.set(13, getDateTime()[0]);
        isoMsg.set(14, expDate);
        isoMsg.set(22, "070");
        isoMsg.set(24, "001");
        isoMsg.set(25, "00");
        isoMsg.set(41, "12345678");
        isoMsg.set(42, "11234567");

        isoMsg.set(52, "1234567891234567");

        isoMsg.set(62, ISOUtil.zeropad("000001", 6));
    }
}
