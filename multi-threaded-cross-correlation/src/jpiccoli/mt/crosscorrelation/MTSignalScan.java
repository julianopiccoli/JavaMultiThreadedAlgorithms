package jpiccoli.mt.crosscorrelation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides functionality for searching for a specific signal in a collection of samples.
 * The waveform may contain noise and the signal may be attenuated.
 * The algorithm uses Cross Correlation (https://en.wikipedia.org/wiki/Cross-correlation) to define the most likely position of the signal.
 * It can use multiple threads to deliver results faster.
 * @author Piccoli
 *
 */
public class MTSignalScan {
	
	private final float[] signal;
	private final float[] capturedWaveform;
	private final int limit;
	
	private AtomicInteger nextOffset;
	private float maximumCorrelation;
	private FindSignalFuture future;
	
	/**
	 * Creates an instance of the scanner for searching for the specified
	 * signal in the provided waveform.
	 * @param signal Array containing the signal for which to search.
	 * @param capturedWaveform The waveform containing the signal to be searched.
	 */
	public MTSignalScan(final float[] signal, final float[] capturedWaveform) {
		this.signal = signal;
		this.capturedWaveform = capturedWaveform;
		limit = capturedWaveform.length - signal.length;
	}
	
	/**
	 * Starts the search. The processing will be executed asynchronously and the
	 * result will be delivered in the returned Future.
	 * Important: this method is not thread-safe. Once a scan process was started,
	 * it can only be restarted after the resulting Future is terminated.
	 * @param threadsCount The number of threads that will be used for computing the result.
	 * @return A Future which resolves with the position of the signal in the waveform.
	 */
	public Future<Integer> start(int threadsCount) {
		ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
		nextOffset = new AtomicInteger(0);
		maximumCorrelation = Float.MIN_VALUE;
		future = new FindSignalFuture(executor);
		for (int index = 0; index < threadsCount; index++) {
			executor.submit(this::mainLoop);
		}
		executor.shutdown();
		return future;
	}
	
	private void mainLoop() {
		float localMaxCorrelation = Float.MIN_VALUE;
		int localPosition = -1;
		int localOffset = nextOffset.getAndIncrement();
		while (localOffset < limit && !Thread.interrupted()) {
			float correlation = calcCorrelation(signal, capturedWaveform, localOffset);
			if (correlation > localMaxCorrelation) {
				localMaxCorrelation = correlation;
				localPosition = localOffset;
			}
			localOffset = nextOffset.getAndIncrement();
		}
		checkAndSwap(localPosition, localOffset);
	}
	
	private synchronized void checkAndSwap(int offset, float correlation) {
		if (correlation > maximumCorrelation) {
			maximumCorrelation = correlation;
			future.setPosition(offset);
		}
	}
	
	private static float calcCorrelation(final float[] signal, final float[] capturedWaveform, final int offset) {
		float sum = 0;
		for (int index = 0; index < signal.length; index++) {
			sum += signal[index] * capturedWaveform[index + offset];
		}
		return sum;
	}
	
	private static class FindSignalFuture implements Future<Integer> {
		
		private ExecutorService executorService;
		private boolean cancelled;
		private int position;
		
		private FindSignalFuture(ExecutorService executorService) {
			this.executorService = executorService;
			cancelled = false;
			position = -1;
		}
		
		private void setPosition(int position) {
			this.position = position;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (!cancelled) {
				if (mayInterruptIfRunning) {
					executorService.shutdownNow();
				}
				cancelled = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public boolean isDone() {
			return cancelled || executorService.isTerminated();
		}

		@Override
		public Integer get() throws InterruptedException, ExecutionException {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			return position;
		}

		@Override
		public Integer get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			boolean terminated = executorService.awaitTermination(timeout, unit);
			if (!terminated) {
				throw new TimeoutException();
			}
			return position;
		}
		
	}
	
}
