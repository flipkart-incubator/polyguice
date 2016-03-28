package com.flipkart.polyguice.trooper;

import com.flipkart.polyguice.core.support.Polyguice;
import com.flipkart.polyguice.trooper.exceptions.LifeCycleException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PolyContainerTest {

    @Mock
    Polyguice polyguiceMock1;

    @Mock
    Polyguice polyguiceMock2;

    @Mock
    LifeCycleListener lifeCycleListener1;

    @Mock
    LifeCycleListener lifeCycleListener2;

    @Test
    public void testStart_shouldPrepareRegisterdTroopers() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(polyguiceMock1);
        polyContainer.register(polyguiceMock2);
        polyContainer.start();
        verify(polyguiceMock1, times(1)).prepare();
        verify(polyguiceMock2, times(1)).prepare();
    }

    @Test
    public void testStart_shouldStartRegisteredTroopersOnlyOnce() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(polyguiceMock1);
        polyContainer.register(polyguiceMock2);
        polyContainer.start();
        verify(polyguiceMock1, times(1)).prepare();
        verify(polyguiceMock2, times(1)).prepare();
        polyContainer.start();
        verifyNoMoreInteractions(polyguiceMock1);
        verifyNoMoreInteractions(polyguiceMock2);
    }

    @Test
    public void testStop_shouldStopRegisterdTroopers() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(polyguiceMock1);
        polyContainer.register(polyguiceMock2);
        polyContainer.start();
        polyContainer.stop();
        verify(polyguiceMock1, times(1)).stop();
        verify(polyguiceMock2, times(1)).stop();
    }

    @Test
    public void testStop_shouldStopOnlyIfStartedInThePast() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(polyguiceMock1);
        polyContainer.register(polyguiceMock2);
        /* Won't stop if it was never started */
        polyContainer.stop();
        verifyZeroInteractions(polyguiceMock1);
        verifyZeroInteractions(polyguiceMock2);
    }

    @Test
    public void testStop_shouldStopTroopersEvenOnExceptions() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(polyguiceMock1);
        doThrow(new RuntimeException()).when(polyguiceMock1).stop();
        polyContainer.register(polyguiceMock2);
        polyContainer.start();
        try {
            polyContainer.stop();
        } catch (RuntimeException e) {
            // Do nothing.
        }
        verify(polyguiceMock1, times(1)).stop();
        verify(polyguiceMock2, times(1)).stop();
    }
    @Test(expected = LifeCycleException.class)
    public void testStop_shouldRaiseExceptionIfStopFails() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(polyguiceMock1);
        doThrow(new RuntimeException()).when(polyguiceMock1).stop();
        polyContainer.register(polyguiceMock2);
        polyContainer.start();
        polyContainer.stop();
    }

    @Test
    public void testLifecycleCallbacks_shouldBeCalledBeforePreparingTroopers() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(lifeCycleListener1);
        polyContainer.register(lifeCycleListener2);
        polyContainer.register(polyguiceMock1);
        when(polyguiceMock1.prepare()).thenThrow(new RuntimeException());
        try {
            polyContainer.start();
        } catch (RuntimeException e) {
            // Do nothing. Basically we want to assert that preStart is called.
        }
        verify(lifeCycleListener1,times(1)).preStart();
        verify(lifeCycleListener2,times(1)).preStart();
    }

    @Test
    public void testLifecycleCallbacks_shouldBeCalledBeforeStoppingTroopers() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(lifeCycleListener1);
        polyContainer.register(lifeCycleListener2);
        polyContainer.register(polyguiceMock1);
        doThrow(new RuntimeException()).when(polyguiceMock1).stop();
        polyContainer.start();
        try {
            polyContainer.stop();
        } catch (RuntimeException e) {
            // Do nothing. Basically we want to assert that preStop is called.
        }
        verify(lifeCycleListener1,times(1)).preStop();
        verify(lifeCycleListener2,times(1)).preStop();
    }

    @Test
    public void testLifecycleCallbacks_shouldBeCalledDespiteExceptions() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(lifeCycleListener1);
        polyContainer.register(lifeCycleListener2);
        polyContainer.register(polyguiceMock1);
        doThrow(new RuntimeException()).when(lifeCycleListener1).preStop();
        polyContainer.start();
        try {
            polyContainer.stop();
        } catch (RuntimeException e) {
            // Do nothing. Basically we want to assert that preStart is called.
        }
        verify(lifeCycleListener1,times(1)).preStop();
        verify(lifeCycleListener2,times(1)).preStop();
        verify(polyguiceMock1,times(1)).stop();
    }
    @Test(expected = LifeCycleException.class)
    public void testLifecycleCallbacks_shouldRaiseExceptionIfPreStopFails() throws Exception {
        final PolyContainer polyContainer = new PolyContainer();
        polyContainer.register(lifeCycleListener1);
        polyContainer.register(lifeCycleListener2);
        polyContainer.register(polyguiceMock1);
        doThrow(new RuntimeException()).when(lifeCycleListener1).preStop();
        polyContainer.start();
        polyContainer.stop();
    }
}