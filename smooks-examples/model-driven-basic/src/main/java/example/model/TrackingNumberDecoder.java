/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package example.model;

import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DataDecoder;

import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Decoder for the Tracking numbers.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TrackingNumberDecoder implements DataDecoder {

    private static Pattern lineSplitter = Pattern.compile("$", Pattern.MULTILINE);

    public Object decode(String historyText) throws DataDecodeException {
        // break the history up line by line - 1 tracking-number per line
        String[] unparsedTrackingNumber = lineSplitter.split(historyText);
        List<TrackingNumber> tnList = new Vector<TrackingNumber>(unparsedTrackingNumber.length);
        TrackingNumber[] trackingNumbers;

        // iterate over and parse the tracking-number lines
        for (int i = 0; i < unparsedTrackingNumber.length; i++) {
            String[] tokens = unparsedTrackingNumber[i].trim().split(":");

            if (tokens.length == 2) {
                TrackingNumber trackingNumber = new TrackingNumber();

                trackingNumber.setShipperID(tokens[0]);
                trackingNumber.setShipmentNumber(tokens[1]);
                tnList.add(trackingNumber);
            }
        }

        trackingNumbers = new TrackingNumber[tnList.size()];
        tnList.toArray(trackingNumbers);

        return trackingNumbers;
    }
}
