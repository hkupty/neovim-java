/*
 * MIT License
 *
 * Copyright (c) 2018 Ensar Sarajčić
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ensarsarajcic.neovim.java.testing;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ManualQueueExecutorServiceTest {

    @Mock
    ExecutorService executorService;

    @InjectMocks
    ManualQueueExecutorService manualQueueExecutorService;

    @Test
    public void delegatesNotSubmittingMethodsToExecutorService() throws InterruptedException {
        manualQueueExecutorService.shutdown();

        given(executorService.shutdownNow()).willReturn(Collections.emptyList());
        manualQueueExecutorService.shutdownNow();

        given(executorService.isShutdown()).willReturn(true);
        assertTrue(manualQueueExecutorService.isShutdown());

        given(executorService.isTerminated()).willReturn(true);
        assertTrue(manualQueueExecutorService.isTerminated());

        given(executorService.awaitTermination(1, TimeUnit.MILLISECONDS)).willReturn(true);
        assertTrue(manualQueueExecutorService.awaitTermination(1, TimeUnit.MILLISECONDS));

        verify(executorService).shutdown();
        verify(executorService).shutdownNow();
        verify(executorService).isShutdown();
        verify(executorService).isTerminated();
        verify(executorService).awaitTermination(1, TimeUnit.MILLISECONDS);
    }

    @Test
    public void doesNotImmediatelySubmitToExecutorService() throws InterruptedException, TimeoutException, ExecutionException {
        manualQueueExecutorService.submit(() -> {});
        verify(executorService, never()).submit(any(Runnable.class));

        manualQueueExecutorService.submit(() -> null);
        verify(executorService, never()).submit(any(Callable.class));

        manualQueueExecutorService.submit(() -> {}, null);
        verify(executorService, never()).submit(any(Runnable.class), any());

        manualQueueExecutorService.invokeAll(Collections.singletonList(() -> null));
        verify(executorService, never()).invokeAll(any());

        manualQueueExecutorService.invokeAll(Collections.singletonList(() -> null), 1, TimeUnit.MILLISECONDS);
        verify(executorService, never()).invokeAll(Collections.singletonList(() -> null), 1, TimeUnit.MILLISECONDS);

        new Thread(() -> {
            try {
                manualQueueExecutorService.invokeAny(Collections.singletonList(() -> null));
                // Since this is blocking, getting to this line is failure
                fail("Not blocking!");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

        new Thread(() -> {
            try {
                manualQueueExecutorService.invokeAny(Collections.singletonList(() -> null), 1, TimeUnit.MILLISECONDS);
                verify(executorService, never()).invokeAny(Collections.singletonList(() -> null), 1, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void submitsWhenManuallyRun() throws InterruptedException, ExecutionException {
        Future future = Mockito.mock(Future.class);
        given(executorService.submit(any(Callable.class))).willReturn(future);
        Runnable runnable = () -> {};
        Future first = manualQueueExecutorService.submit(runnable);
        verify(executorService, never()).submit(runnable);

        manualQueueExecutorService.runOne();
        first.get();
        verify(executorService).submit(any(Callable.class));

        manualQueueExecutorService.submit(() -> null);
        Future second = manualQueueExecutorService.submit(() -> null);
        manualQueueExecutorService.submit(() -> null);
        Future third = manualQueueExecutorService.submit(() -> null);
        verify(executorService, times(1)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        manualQueueExecutorService.runOne();

        second.get();

        verify(executorService, times(3)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        manualQueueExecutorService.runOne();

        third.get();

        verify(executorService, times(5)).submit(any(Callable.class));

        Future fourth = manualQueueExecutorService.submit(() -> {}, null);
        verify(executorService, times(5)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        fourth.get();
        verify(executorService, times(6)).submit(any(Callable.class));

        List<Future<Object>> fifth = manualQueueExecutorService.invokeAll(Lists.newArrayList(() -> null, Object::new));
        verify(executorService, times(6)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        fifth.get(0).get();
        verify(executorService, times(7)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        fifth.get(1).get();
        verify(executorService, times(8)).submit(any(Callable.class));

        List<Future<Object>> sixth = manualQueueExecutorService.invokeAll(Lists.newArrayList(() -> null, Object::new), 100, TimeUnit.MILLISECONDS);
        verify(executorService, times(8)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        sixth.get(0).get();
        verify(executorService, times(9)).submit(any(Callable.class));

        manualQueueExecutorService.runOne();
        sixth.get(1).get();
        verify(executorService, times(10)).submit(any(Callable.class));
    }
}