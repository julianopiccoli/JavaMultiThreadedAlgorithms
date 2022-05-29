package jpiccoli.mt.crosscorrelation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Command-line utility used to test the functionality provided by MTSignalScan.
 * It generates a rectangular signal with a positive interval followed by a negative one.
 * The signal is then attenuated and added to a random-noise-filled waveform.
 * 
 * @author Piccoli
 *
 */
public class ScanSignalTest {
	
	private static final int DEFAULT_POSITIVE_LENGTH = 300;
	private static final float DEFAULT_POSITIVE_VALUE = 20.0f;
	private static final int DEFAULT_NEGATIVE_LENGTH = 200;
	private static final float DEFAULT_NEGATIVE_VALUE = 10.0f;
	private static final int DEFAULT_WAVEFORM_LENGTH = 100000000;
	private static final float DEFAULT_NOISE_AMPLITUDE = 30.0f;
	private static final float DEFAULT_SIGNAL_GAIN = 0.7f;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		
		List<String> argumentsList = new ArrayList<>(Arrays.asList(args));
		
		if (argumentsList.contains("--help")) {
			printHelp();
			return;
		}
		
		int positiveSignalLength = findIntegerParameter(argumentsList, "positive-signal-length").orElse(DEFAULT_POSITIVE_LENGTH);
		float positiveSignalValue = findFloatParameter(argumentsList, "positive-signal-value").orElse(DEFAULT_POSITIVE_VALUE);
		int negativeSignalLength = findIntegerParameter(argumentsList, "negative-signal-length").orElse(DEFAULT_NEGATIVE_LENGTH);
		float negativeSignalValue = findFloatParameter(argumentsList, "negative-signal-value").orElse(DEFAULT_NEGATIVE_VALUE);
		
		float[] signal = createSignal(positiveSignalLength, positiveSignalValue, negativeSignalLength, negativeSignalValue);
		
		int waveformLength = findIntegerParameter(argumentsList, "waveform-length").orElse(DEFAULT_WAVEFORM_LENGTH);
		float noiseAmplitude = findFloatParameter(argumentsList, "noise-amplitude").orElse(DEFAULT_NOISE_AMPLITUDE);
		waveformLength = Math.max(signal.length, waveformLength);
		
		float[] waveform = createWaveform(waveformLength, noiseAmplitude);
		
		int offset = findIntegerParameter(argumentsList, "signal-offset").orElseGet(() -> {
			Random random = new Random();
			return random.nextInt(waveform.length - signal.length);			
		});
		float signalGain = findFloatParameter(argumentsList, "signal-gain").orElse(DEFAULT_SIGNAL_GAIN);
		addSignalToWaveform(signal, waveform, offset, signalGain);
		
		int threads = findIntegerParameter(argumentsList, "threads").orElseGet(() -> Runtime.getRuntime().availableProcessors());
		
		String imageFilePath = findParameter(argumentsList, "output-image");
		if (imageFilePath != null) {
			WaveformPrinter.printWaveform(imageFilePath, waveform, offset, signal.length);
		}
		
		System.out.printf("Signal positive interval samples count: %d\n", positiveSignalLength);
		System.out.printf("Signal positive interval value : %f\n", positiveSignalValue);
		System.out.printf("Signal negative interval samples count: %d\n", negativeSignalLength);
		System.out.printf("Signal negative interval value : %f\n", negativeSignalValue);
		System.out.printf("Captured waveform length: %d\n", waveformLength);
		System.out.printf("Added noise amplitude: %f\n", noiseAmplitude);
		System.out.printf("Position of the signal in the waveform: %d\n", offset);
		System.out.printf("Signal gain: %f\n", signalGain);
		System.out.printf("Number of threads: %d\n", threads);
		System.out.println();
		System.out.println("Scanning...");
		
		MTSignalScan mtFindSignal = new MTSignalScan(signal, waveform);
		
		long beforeStartTimestamp = System.currentTimeMillis();
		
		Future<Integer> calculatedPositionFuture = mtFindSignal.start(threads);
		int calculatedPosition = calculatedPositionFuture.get();
		
		long afterFinishTimestamp = System.currentTimeMillis();
		
		System.out.printf("Scan result: %d\n", calculatedPosition);
		System.out.printf("Distance from actual position: %d\n", Math.abs(offset - calculatedPosition));
		System.out.printf("Ellapsed time: %d ms", afterFinishTimestamp - beforeStartTimestamp);
		
	}
	
	private static void printHelp() {
		System.out.println("Usage: java jpiccoli.mt.crosscorrelation.ScanSignalTest [option-1] [value-1] [option-2] [value-2] ...");
		System.out.println("Options:");
		System.out.println("--positive-signal-length [integer-value]: number of samples of the signal's positive interval");
		System.out.println("--positive-signal-value [float-value]: value of the signal during the positive interval");		
		System.out.println("--negative-signal-length [integer-value]: number of samples of the signal's negative interval");
		System.out.println("--negative-signal-value [float-value]: value of the signal during the negative interval");
		System.out.println("--waveform-length [integer-value]: number of samples of the captured waveform");
		System.out.println("--noise-amplitude [float-value]: amplitude of the noise that will be added to the captured waveform");
		System.out.println("--signal-offset [integer-value]: position at the waveform were the signal will be inserted");
		System.out.println("--signal-gain [float-value]: gain to be applied to the signal prior to addition to the waveform");
		System.out.println("--output-image [string-value]: file path were an imagem with the generated waveform will be stored");
		System.out.println("--threads [integer-value]: number of threads to be used in the scan process");
	}
	
	private static float[] createSignal(int positiveLength, float positiveValue, int negativeLength, float negativeValue) {
		
		positiveLength = Math.max(positiveLength, 0);
		negativeLength = Math.max(negativeLength, 0);
		positiveValue = Math.abs(positiveValue);
		negativeValue = -Math.abs(negativeValue);
		
		float[] signal = new float[positiveLength + negativeLength];				
		for (int index = 0; index < positiveLength; index++) {
			signal[index] = positiveValue;
		}		
		for (int index = positiveLength; index < signal.length; index++) {
			signal[index] = negativeValue;
		}
		
		return signal;
		
	}
	
	private static float[] createWaveform(int length, float noiseAmplitude) {
		
		length = Math.max(length, 0);
		noiseAmplitude = Math.abs(noiseAmplitude);
		
		Random random = new Random();
		float[] waveform = new float[length];
		for (int index = 0; index < waveform.length; index++) {
			waveform[index] = random.nextFloat() * noiseAmplitude * (random.nextBoolean() ? 1 : -1);
		}
		
		return waveform;
		
	}
	
	private static void addSignalToWaveform(float[] signal, float[] waveform, int offset, float signalGain) {
		
		offset = Math.min(Math.max(offset, 0), waveform.length - signal.length);
		
		for (int index = 0; index < signal.length; index++) {
			waveform[index + offset] += signal[index] * signalGain;
		}
		
	}
	
	private static Optional<Float> findFloatParameter(List<String> args, String parameterName) {
		return Optional.ofNullable(findParameter(args, parameterName)).map(Float::parseFloat);
	}
	
	private static Optional<Integer> findIntegerParameter(List<String> args, String parameterName) {
		return Optional.ofNullable(findParameter(args, parameterName)).map(Integer::parseInt);
	}
	
	private static String findParameter(List<String> args, String parameterName) {
		parameterName = "--" + parameterName;
		int index = args.indexOf(parameterName);
		if (index >= 0) {
			int valueIndex = index + 1;
			if (valueIndex < args.size()) {
				return args.get(valueIndex);
			}
		}
		return null;
	}

}
