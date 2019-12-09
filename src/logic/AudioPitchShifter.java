package logic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.Oscilloscope;
import be.tarsos.dsp.Oscilloscope.OscilloscopeEventHandler;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.resample.RateTransposer;

public class AudioPitchShifter {

    private AudioFormat format;
    private GainProcessor gain;
    private AudioPlayer audioPlayer;
    private AudioDispatcher dispatcher;
    private RateTransposer rateTransposer;
    private WaveformSimilarityBasedOverlapAdd wsola;

    private double gainValue;
    private double pitchValue;
    private double sampleRate;
    private OscilloscopeEventHandler oscilloscopeEventHandler;

    public AudioPitchShifter(OscilloscopeEventHandler handler) {
        gainValue = 1;
        pitchValue = 1;
        oscilloscopeEventHandler = handler;
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

            audioPlayer = new AudioPlayer(format);
            final TargetDataLine targetline = (TargetDataLine) AudioSystem.getLine(info);
            targetline.open(format,4096);
            targetline.start();

            final AudioInputStream stream = new AudioInputStream(targetline);
            JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);

            startDispatcher(audioStream);

            Thread audioProcessor = new Thread(dispatcher);
            audioProcessor.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();}
        }

    private void setParameters() {
        gain = new GainProcessor(1.0);
        format = new AudioFormat(44100, 16, 1, true,true);
        sampleRate = format.getSampleRate();
        rateTransposer = new RateTransposer(pitchValue);
        wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(pitchValue, sampleRate));
    }

    private void startDispatcher(JVMAudioInputStream audioStream) {
        dispatcher = new AudioDispatcher(audioStream, wsola.getInputBufferSize(),wsola.getOverlap());
        wsola.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(wsola);
        dispatcher.addAudioProcessor(rateTransposer);
        dispatcher.addAudioProcessor(gain);
        dispatcher.addAudioProcessor(new Oscilloscope(oscilloscopeEventHandler));
        dispatcher.addAudioProcessor(audioPlayer);
    }

    public void onChangeGainValue(double newGainValue) {
        gainValue = newGainValue;
        gain.setGain(gainValue);
    }

    public double getGain() {
        return gainValue;
    }

    public void onChangePitchValue(double newPitchValue) {
        pitchValue = newPitchValue;
        wsola.setParameters(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(pitchValue, sampleRate));
        rateTransposer.setFactor(pitchValue);
    }

    public double getPitchValue() {
        return pitchValue;
    }

    public boolean isDispatcherNull() {
        return dispatcher == null;
    }
}
