package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.Oscilloscope.OscilloscopeEventHandler;
import logic.AudioPitchShifter;


public class MainFrame extends JFrame implements OscilloscopeEventHandler{

    private static final long serialVersionUID = 1L;
    private final JSlider factorSlider;
    private final JLabel pitch_factorLabel;
    private final JLabel gain_factorLabel;
    private final JSlider gainSlider;
    private AudioPitchShifter audioPitchShifter = new AudioPitchShifter(this);
    private final GraphPanel panel;

    public static void main(String[] args) {
        JFrame frame = new MainFrame();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(600,300);//tamaño de la ventana completa
        frame.setVisible(true);
    }


    public MainFrame() {
    	audioPitchShifter.initiate();
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Audio Pitch Shifter");

        factorSlider = new JSlider(20, 250);
        factorSlider.setValue(100);
        factorSlider.setPaintLabels(true);
        factorSlider.addChangeListener(parameterSettingChangedListener);

        panel = new GraphPanel();
        gainSlider = new JSlider(0,200);
        gainSlider.setValue(100);
        gainSlider.setPaintLabels(true);
        gainSlider.addChangeListener(parameterSettingGain);

        JPanel params = new JPanel(new BorderLayout());
        params.setBorder(new TitledBorder("1. Change frecuency value"));
        JPanel params_individual = new JPanel(new GridLayout(0,3));
        JLabel label_agudo = new JLabel("High Voice Pitch");
        JLabel label_normal = new JLabel("Normal Voice Pitch");
        JLabel label_grave = new JLabel("Low Voice Pitch");
        params_individual.add(label_agudo);
        params_individual.add(label_normal);
        params_individual.add(label_grave);
        pitch_factorLabel = new JLabel("Factor 100%");
        pitch_factorLabel.setToolTipText("Factor de frecuncia en % (100 es normal, 50 es el doble de frecuencia y 200 es la mitad)");
        params.add(pitch_factorLabel,BorderLayout.NORTH);
        params.add(params_individual,BorderLayout.CENTER);
        params.add(factorSlider,BorderLayout.SOUTH);

        JPanel gainPanel = new JPanel(new BorderLayout());
        gain_factorLabel = new JLabel("Gain - Factor 100%");
        gain_factorLabel.setToolTipText("Volume in % (normal value is 100)");
        gainPanel.add(gain_factorLabel,BorderLayout.NORTH);
        gainPanel.add(gainSlider,BorderLayout.CENTER);
        gainPanel.setBorder(new TitledBorder("2. Volume"));

        //aqui esta ej JFrame de la ventana completa
        this.add(panel,BorderLayout.SOUTH);
//        this.add(inputSubPanel,BorderLayout.NORTH);
        this.add(params,BorderLayout.NORTH);
        this.add(gainPanel,BorderLayout.CENTER);
    }

    private ChangeListener parameterSettingGain = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (!audioPitchShifter.isDispatcherNull()) {
                audioPitchShifter.changeGain(gainSlider.getValue() / 100.0);
            }
            gain_factorLabel.setText("Ganancia - Factor "+ Math.round(audioPitchShifter.getGain() * 100)+"%");
        }
    };

    private ChangeListener parameterSettingChangedListener = new ChangeListener(){
        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (arg0.getSource() instanceof JSlider) {
                audioPitchShifter.changeCurrentFactor(factorSlider.getValue() / 100.0);
            }
            pitch_factorLabel.setText("Factor " + Math.round(audioPitchShifter.getCurrentFactor()* 100) + "%");
        }};
    	
    @Override
    public void handleEvent(float[] data, AudioEvent event) {
    	panel.paint(data,event);
    	panel.repaint();
    }
}
