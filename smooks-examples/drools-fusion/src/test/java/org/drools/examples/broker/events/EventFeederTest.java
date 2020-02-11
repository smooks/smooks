/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.broker.events;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class EventFeederTest {

	@Test
    public void testFeedPseudoClock() {
        // create the events for the test
        final Event< ? >[] events = new Event[]{new EventImpl<>(1000,
                "one"), new EventImpl<>(1100,
                "two"), new EventImpl<>(1120,
                "three"), new EventImpl<>(1300,
                "four"), new EventImpl<>(1550,
                "five"), new EventImpl<>(1700,
                "six"),};

        final Iterator<Event< ? >> it = Arrays.asList( events ).iterator();

        // create mock objects for the source and the receiver
        final EventSource source = mock( EventSource.class );
        final EventReceiver receiver = mock( EventReceiver.class );

        when( source.hasNext() ).thenAnswer((Answer<Boolean>) invocation -> it.hasNext());
        when( source.getNext() ).thenAnswer((Answer<Event<?>>) invocation -> it.next());
        
        // create the scheduler used by drools and the feeder to be tested
        PseudoClockScheduler clock = new PseudoClockScheduler();
        EventFeeder feeder = new EventFeeder( clock,
                                              source,
                                              receiver );

        feeder.feed();
        // advancing time step by step, so that we can test state between advances
        for( Event<?> event : events ) {
            clock.advanceTime( event.getTimestamp() - clock.getCurrentTime(),
                               TimeUnit.MILLISECONDS );
        }

        verify( source, times(7) ).hasNext();
        verify( source, times(6) ).getNext();
    }

}
