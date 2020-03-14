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
package example;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DbETLTest {

	@Test
    public void test() throws Exception {

        Main main = new Main();
        main.startDatabase();

        try {
            main.runSmooksTransform();
            List<Map<String, Object>> orders = main.getOrders();
            List<Map<String, Object>> orderItems = main.getOrderItems();

            assertEquals(2, orders.size());
            assertEquals("{ORDERNUMBER=1, USERNAME=user1, STATUS=0, NET=59.97, TOTAL=64.92, ORDDATE=2006-11-15}", orders.get(0).toString());
            assertEquals("{ORDERNUMBER=2, USERNAME=user2, STATUS=0, NET=81.3, TOTAL=91.06, ORDDATE=2006-11-15}", orders.get(1).toString());
            assertEquals(4, orderItems.size());
            assertEquals("{ORDERNUMBER=1, QUANTITY=1, PRODUCT=364, TITLE=The 40-Year-Old Virgin, PRICE=28.98}", orderItems.get(0).toString());
            assertEquals("{ORDERNUMBER=1, QUANTITY=1, PRODUCT=299, TITLE=Pulp Fiction, PRICE=30.99}", orderItems.get(1).toString());
            assertEquals("{ORDERNUMBER=2, QUANTITY=2, PRODUCT=983, TITLE=Gone with The Wind, PRICE=25.8}", orderItems.get(2).toString());
            assertEquals("{ORDERNUMBER=2, QUANTITY=3, PRODUCT=299, TITLE=Lethal Weapon 2, PRICE=55.5}", orderItems.get(3).toString());
        } finally {
            main.stopDatabase();
        }
    }



    public static void main(String[] args) throws IOException, SAXException {
        //printReport("edi-orders-parser.xml");
        //writeBigFile();
        //eatBigFile();
    }

    private static void eatBigFile() throws IOException, SAXException {
        Smooks smooks = new Smooks("./smooks-configs/smooks-config.xml");

        try {
            FileReader reader = new FileReader("/zap/big-edi.edi");

            try {
                long start = System.currentTimeMillis();
                smooks.filterSource(smooks.createExecutionContext(), new StreamSource(reader), null);
                System.out.println("Took: " + (System.currentTimeMillis() - start));
            } finally {
                reader.close();
            }
        } finally {
            smooks.close();
        }
    }

    private static void writeBigFile() throws IOException {
        FileWriter writer = new FileWriter("/zap/big-edi.edi");

        try {
            writer.write("MLS*Wed Nov 15 13:45:28 EST 2006\n");

            String toadd = "HDR*1*0*59.97*64.92*4.95\n" +
                    "CUS*user1*Harry^Fletcher*SD\n" +
                    "ORD*1*1*364*The 40-Year-Old Virgin*28.98\n" +
                    "ORD*2*1*299*Pulp Fiction*30.99\n" +
                    "HDR*2*0*81.30*91.06*9.76\n" +
                    "CUS*user2*George^Hook*SD\n" +
                    "ORD*3*2*983*Gone with The Wind*25.80\n" +
                    "ORD*4*3*299*Lethal Weapon 2*55.50\n";

            for(int i = 0; i < 2015748; i++) {
                writer.write(toadd);
                writer.flush();
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }
}
