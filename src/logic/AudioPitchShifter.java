package logic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.resample.RateTransposer;

public class AudioPitchShifter {
	
	/**
	 * 
	 */
	private AudioDispatcher dispatcher;
	private WaveformSimilarityBasedOverlapAdd wsola;
	private GainProcessor gain;
	private AudioPlayer audioPlayer;
	private RateTransposer rateTransposer;
	private AudioFormat format;
	private static double currentFactor;// pitch shift factor
	private double gainValue;
	private double sampleRate;

	public AudioPitchShifter() {
		currentFactor = 1;
		gainValue = 1;
		
	}
	
	public void initiate() {
		
		if(dispatcher != null){
			dispatcher.stop();
		}
		
		try {
			
			setParameters();				
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			
			if(!AudioSystem.isLineSupported(info)) {
				System.out.println("line not supported");
			}
			
			final TargetDataLine targetline = (TargetDataLine) AudioSystem.getLine(info);
			targetline.open(format);
			targetline.start();
			final AudioInputStream stream = new AudioInputStream(targetline);//antes estaba line
			JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
			
			startDispatcher(audioStream);
			
			Thread audioProcessor = new Thread(dispatcher);
			audioProcessor.start();
		} 
		catch (LineUnavailableException e) {e.printStackTrace();}	
	}
	
	private void setParameters() {
		format = new AudioFormat(44100, 16, 1, true,true);
		rateTransposer = new RateTransposer(currentFactor);
		gain = new GainProcessor(1.0);
		try {
			audioPlayer = new AudioPlayer(format);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sampleRate = format.getSampleRate();
		wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(currentFactor, sampleRate));	
	}
	
	private void startDispatcher(JVMAudioInputStream audioStream) {
		// create a new dispatcher
		dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(),wsola.getOverlap()); 
		
		wsola.setDispatcher(dispatcher);
		dispatcher.addAudioProcessor(wsola);
		dispatcher.addAudioProcessor(rateTransposer);
		dispatcher.addAudioProcessor(gain);
		dispatcher.addAudioProcessor(audioPlayer);
		dispatcher.addAudioProcessor(new AudioProcessor() {
			
		@Override
		public void processingFinished() {
			if(true){//era loop antes
			dispatcher =null;
			initiate();
			}	
		}
		@Override
			public boolean process(AudioEvent audioEvent) {
				return true;
			}
		});
	}
	
	public void changeGain(double newGainValue) {
		gainValue = newGainValue;
		gain.setGain(gainValue);
	}
	
	public double getGain() {
		return gainValue;
	}
	
	public void changeCurrentFactor(double newCurrentFactor) {
		currentFactor = newCurrentFactor;
		wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(currentFactor, sampleRate));
		rateTransposer.setFactor(currentFactor);
	}
	public double getCurrentFactor() {
		return currentFactor;
	}
	
	public boolean isDispatcherNull() {
		return dispatcher == null;
	}
	

}
