package com.example.tappos.GlobalFunctions;

import android.util.Log;

import com.example.tappos.ISOMessages.CustomISOPackager;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.util.ProtectedLogListener;
import org.jpos.util.Logger;

import java.io.IOException;
import java.util.Random;

public class Channel {
    private static final String TAG = "Custom-Channel";

    public static ISOMsg sendOnline(ISOMsg isoMsg, CustomISOPackager packager)
            throws ISOException, IOException {

        ISOMsg response = new ISOMsg();

        Logger logger = new Logger();
        ProtectedLogListener protectedLog = new ProtectedLogListener();
        Configuration conf = new SimpleConfiguration();
        conf.put("protect", "2 35");
        conf.put("wipe", "52");
        protectedLog.setConfiguration(conf);
        logger.addListener(protectedLog);

        ASCIIChannel channel = new ASCIIChannel("167.99.254.180", 32020, packager);

        channel.setLogger(logger, "ProtectedLogListener");
        ISOMsg duplicate = isoMsg;

        isoMsg.dump(System.out, "AWASH/request");
        logISOMsg(isoMsg);

        channel.setTimeout(45 * 1000);

        try {
            // Use simulateTrx here
            response = simulateTrx(duplicate);
        } catch (Exception e) {
            Log.i(TAG, "connection failed");
            return null;
        }

        if (channel.isConnected()) {
            channel.send(duplicate);
            Log.i(TAG, "send(): waiting for response");
            try {
                response = channel.receive();
            } catch (Exception ex) {
                // No response from Host, perform reversal transaction
                Log.i(TAG, "No response from Host");
                Log.i(TAG, "Doing Reversal .....");
                ISOMsg isoMSG = new ISOMsg();
                channel.connect();
                channel.send(isoMSG);

                try {
                    response = channel.receive();
                } catch (Exception e) {
                    Log.i(TAG, "connection failed");
                    Log.i(TAG, "-----main error-------" + e);
                    return null;
                }

                response.dump(System.out, "AWASH/response");
                logISOMsg(response);
                channel.disconnect();
                return response;
            }

            Log.i(TAG, "send(): Received response");

            channel.disconnect();
        }

        return response;
    }

    public static ISOMsg simulateTrx(ISOMsg buildData) {
        ISOMsg isoMSG = new ISOMsg();
        isoMSG.set(2, buildData.getString(2));
        isoMSG.set(4, buildData.getString(4));
        isoMSG.set(11, buildData.getString(11));
        isoMSG.set(12, buildData.getString(12));
        isoMSG.set(13, buildData.getString(13));
        isoMSG.set(24, "003");

        Random random = new Random();
        long randomNumber = (long) (random.nextDouble() * 1e12);
        String RRN = String.format("%012d", randomNumber);
        isoMSG.set(37, RRN);

        int randomAuth = 100000 + random.nextInt(900000);

        isoMSG.set(38, String.valueOf(randomAuth));
        isoMSG.set(39, "00");
        isoMSG.set(41, buildData.getString(41));

        return isoMSG;
    }

    public static void logISOMsg(ISOMsg msg) {
        System.out.println("----ISO MESSAGE-----");
        StringBuilder builder = new StringBuilder();

        try {
            builder.append("  <MTI> : " + msg.getMTI());
            builder.append("\n");
            for (int i = 1; i < msg.getMaxField(); i++) {
                if (msg.hasField(i)) {
                    builder.append("    <Field id =" + i + " >" + " value=" + msg.getString(i) + "/>");
                    builder.append("\n");
                }
            }
        } catch (ISOException e) {
            e.printStackTrace();
        } finally {
            Log.i(TAG, builder.toString());
        }
    }
}
