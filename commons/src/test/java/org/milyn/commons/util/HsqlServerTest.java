/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.commons.util;

import org.hsqldb.ServerConstants;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link HsqlServer}.
 *
 * @author <a href="mailto:danielbevenius@gmail.com">Daniel Bevenius</a>
 */
public class HsqlServerTest {
    @Test
    @Ignore("want to see if there might be a restriction on the bamboo machines that cause this test to sometimes fail.")
    public void startStop() throws Exception {
        for (int i = 0; i < 50; i++) {
            startStopCycle();
        }
    }

    private void startStopCycle() throws Exception {
        HsqlServer hsqlServer = new HsqlServer(1999);
        hsqlServer.stop();
        assertEquals(ServerConstants.SERVER_STATE_SHUTDOWN, hsqlServer.getState());
    }

}
