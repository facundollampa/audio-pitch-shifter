package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.Oscilloscope.OscilloscopeEventHandler;
import logic.AudioPitchShifter;


public class MainFrame extends JFrame implements OscilloscopeEventHandler{

    private static final long serialVersionUID = 1L;

    private final JSlider pitchSlider;
    private final JSlider gainSlider;

    private final JLabel pitch_factorLabel;
    private final JLabel gain_factorLabel;

    private AudioPitchShifter audioPitchShifter = new AudioPitchShifter(this);
    private final GraphPanel panel;

    public static void main(String[] args) {
        JFrame frame = new MainFrame();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(600,300);
        frame.setVisible(true);
    }


    public MainFrame() {
        audioPitchShifter.initiate();
        panel = new GraphPanel();

        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Audio Pitch Shifter");

        pitchSlider = new JSlider(20, 250);
        pitchSlider.setValue(100);
        pitchSlider.addChangeListener(parameterSettingPitch);

        gainSlider = new JSlider(0,200);
        gainSlider.setValue(100);
        gainSlider.addChangeListener(parameterSettingGain);

        JPanel pitchPanel = new JPanel(new BorderLayout());
        pitchPanel.setBorder(new TitledBorder("1. Frecuency"));
        JPanel params_individual = new JPanel(new GridLayout(0,3));
        JLabel label_agudo = new JLabel("High Voice Pitch");
        JLabel label_normal = new JLabel("Normal Voice Pitch");
        JLabel label_grave = new JLabel("Low Voice Pitch");
        params_individual.add(label_agudo);
        params_individual.add(label_normal);
        params_individual.add(label_grave);
        pitch_factorLabel = new JLabel("Pitch Factor 100%");
        pitchPanel.add(pitch_factorLabel,BorderLayout.NORTH);
        pitchPanel.add(params_individual,BorderLayout.CENTER);
        pitchPanel.add(pitchSlider,BorderLayout.SOUTH);

        JPanel gainPanel = new JPanel(new BorderLayout());
        gain_factorLabel = new JLabel("Gain Factor 100%");
        gainPanel.add(gain_factorLabel,BorderLayout.NORTH);
        gainPanel.add(gainSlider,BorderLayout.CENTER);
        gainPanel.setBorder(new TitledBorder("2. Volume"));

        this.add(pitchPanel,BorderLayout.NORTH);
        this.add(gainPanel,BorderLayout.CENTER);
        this.add(panel,BorderLayout.SOUTH);
    }

    private ChangeListener parameterSettingGain = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (!audioPitchShifter.isDispatcherNull()) {
                audioPitchShifter.onChangeGainValue(gainSlider.getValue() / 100.0);
            }
            gain_factorLabel.setText("Gain Factor "+ Math.round(audioPitchShifter.getGain() * 100)+"%");
        }
    };

    private ChangeListener parameterSettingPitch = new ChangeListener(){
        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (arg0.getSource() instanceof JSlider) {
                audioPitchShifter.onChangePitchValue(pitchSlider.getValue() / 100.0);
            }
            pitch_factorLabel.setText("Pitch Factor " + Math.round(audioPitchShifter.getPitchValue()* 100) + "%");
        }};

    @Override
    public void handleEvent(float[] data, AudioEvent event) {
        panel.paint(data,event);
        panel.repaint();
    }
}
