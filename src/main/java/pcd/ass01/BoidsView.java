package pcd.ass01;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class BoidsView implements ChangeListener, ActionListener {

	private static final String START_LABEL = "START";
	private static final String STOP_LABEL = "STOP";
	private static final String PAUSE_LABEL = "PAUSE";
	private static final String RESUME_LABEL = "RESUME";

	private JFrame frame;
	private BoidsPanel boidsPanel;
	private JSlider cohesionSlider, separationSlider, alignmentSlider;
	private JButton startButton, stopButton, pauseButton, resumeButton;
	private JPanel buttonsPanel;
	private BoidsModel model;
	private ConcurrentBoidsSimulator simulator;
	private int width, height;

	public BoidsView(BoidsModel model, int width, int height) {
		this.model = model;
		this.width = width;
		this.height = height;

		// Ask for number of boids before showing the main GUI
		int numberOfBoids = promptForNumberOfBoids();
		if (numberOfBoids > 0) {
			model.createBoids(numberOfBoids);
			initializeGUI();
		} else {
			System.exit(0);
		}
	}

	public void setSimulator(ConcurrentBoidsSimulator simulator) {
		this.simulator = simulator;
		// Enable button from the beginning
		enableButtonsBasedOnState(false, true, true, false);
	}

	private int promptForNumberOfBoids() {
		while (true) {
			String input = JOptionPane.showInputDialog(null,
					"Enter the number of boids for the simulation:",
					"Boids Simulation Setup",
					JOptionPane.QUESTION_MESSAGE);

			if (input == null) return -1; // User pressed cancel

			try {
				int value = Integer.parseInt(input);
				if (value > 0) return value;

				JOptionPane.showMessageDialog(null,
						"Please enter a positive number of boids.",
						"Invalid Input",
						JOptionPane.ERROR_MESSAGE);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null,
						"Please enter a valid number.",
						"Invalid Input",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void initializeGUI() {
		frame = new JFrame("Boids Simulation");
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel controlPanel = createControlPanel();

		buttonsPanel = createButtonsPanel();

		boidsPanel = new BoidsPanel(this, model);

		// Add components to main panel
		mainPanel.add(boidsPanel, BorderLayout.CENTER);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		mainPanel.add(buttonsPanel, BorderLayout.NORTH);

		frame.setContentPane(mainPanel);
		frame.setVisible(true);
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBackground(new Color(230, 230, 235));

		startButton = new JButton(START_LABEL);
		stopButton = new JButton(STOP_LABEL);
		pauseButton = new JButton(PAUSE_LABEL);
		resumeButton = new JButton(RESUME_LABEL);

		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		pauseButton.addActionListener(this);
		resumeButton.addActionListener(this);

		panel.add(startButton);
		panel.add(pauseButton);
		panel.add(resumeButton);
		panel.add(stopButton);

		// Initial button states
		enableButtonsBasedOnState(true, false, true, false);

		return panel;
	}

	private void enableButtonsBasedOnState(boolean start, boolean stop, boolean pause, boolean resume) {
		startButton.setEnabled(start);
		stopButton.setEnabled(stop);
		pauseButton.setEnabled(pause);
		resumeButton.setEnabled(resume);
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBorder(BorderFactory.createTitledBorder("Behavior Controls"));
		controlPanel.setPreferredSize(new Dimension(width, 275));
		controlPanel.setBackground(new Color(230, 230, 235));

		JPanel avoidancePanel = createSliderWithLabel("Separation:", "Low", "High");
		separationSlider = createParameterSlider();
		avoidancePanel.add(separationSlider);

		JPanel directionPanel = createSliderWithLabel("Alignment:", "Low", "High");
		alignmentSlider = createParameterSlider();
		directionPanel.add(alignmentSlider);

		JPanel groupingPanel = createSliderWithLabel("Cohesion:", "Low", "High");
		cohesionSlider = createParameterSlider();
		groupingPanel.add(cohesionSlider);

		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(avoidancePanel);
		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(directionPanel);
		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(groupingPanel);
		controlPanel.add(Box.createVerticalGlue());

		return controlPanel;
	}

	private JPanel createSliderWithLabel(String title, String minLabel, String maxLabel) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setBackground(new Color(230, 230, 235));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(titleLabel);

		return panel;
	}

	private JSlider createParameterSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("0") );
		labelTable.put( 10, new JLabel("1") );
		labelTable.put( 20, new JLabel("2") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
		slider.addChangeListener(this);
		return slider;
	}

	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var val = separationSlider.getValue();
			model.setSeparationWeight(0.1*val);
		} else if (e.getSource() == cohesionSlider) {
			var val = cohesionSlider.getValue();
			model.setCohesionWeight(0.1*val);
		} else {
			var val = alignmentSlider.getValue();
			model.setAlignmentWeight(0.1*val);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton source = (JButton) e.getSource();

		if (source == startButton) {
			// Start the simulation in a new thread to avoid blocking the GUI
			new Thread(() -> {
				simulator.runSimulation();
			}).start();
			enableButtonsBasedOnState(false, true, true, false);
		} else if (source == stopButton) {
			simulator.stopSimulation();
			cohesionSlider.setValue(10);
			separationSlider.setValue(10);
			alignmentSlider.setValue(10);
			enableButtonsBasedOnState(true, false, false, false);
		} else if (source == pauseButton) {
			simulator.pauseSimulation();
			enableButtonsBasedOnState(false, true, false, true);
		} else if (source == resumeButton) {
			simulator.resumeSimulation();
			enableButtonsBasedOnState(false, true, true, false);
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}