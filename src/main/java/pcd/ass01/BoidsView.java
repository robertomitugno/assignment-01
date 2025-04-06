package pcd.ass01;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class BoidsView implements ChangeListener, ActionListener {

	private JFrame frame;
	private BoidsPanel boidsPanel;
	private JSlider cohesionSlider, separationSlider, alignmentSlider;
	private JButton startButton, stopButton, pauseButton, resumeButton;
	private BoidsModel model;
	private ConcurrentBoidsSimulator simulator;
	private int width, height;

	public BoidsView(BoidsModel model, int width, int height) {
		this.model = model;
		this.width = width;
		this.height = height;

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
		enableButtonsBasedOnState(SimulationState.STOPPED);
	}

	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	private int promptForNumberOfBoids() {
		while (true) {
			String input = JOptionPane.showInputDialog(frame,
					"Enter the number of boids for the simulation:",
					"Boids Simulation Setup",
					JOptionPane.QUESTION_MESSAGE);

			if (input == null) return -1;

			try {
				int value = Integer.parseInt(input);
				if (value > 0) return value;

				JOptionPane.showMessageDialog(frame,
						"Please enter a positive number of boids.",
						"Invalid Input",
						JOptionPane.ERROR_MESSAGE);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame,
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

		JPanel controlPanel = createBehaviorPanel();
		JPanel buttonsPanel = createButtonsPanel();
		boidsPanel = new BoidsPanel(this, model);

		mainPanel.add(boidsPanel, BorderLayout.CENTER);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		mainPanel.add(buttonsPanel, BorderLayout.NORTH);

		frame.setContentPane(mainPanel);
		frame.setVisible(true);
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBackground(new Color(230, 230, 235));

		startButton = new JButton("START");
		stopButton = new JButton("STOP");
		pauseButton = new JButton("PAUSE");
		resumeButton = new JButton("RESUME");

		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		pauseButton.addActionListener(this);
		resumeButton.addActionListener(this);

		panel.add(startButton);
		panel.add(pauseButton);
		panel.add(resumeButton);
		panel.add(stopButton);

		enableButtonsBasedOnState(SimulationState.STOPPED);

		return panel;
	}

	private enum SimulationState {
		STOPPED, RUNNING, PAUSED
	}

	private void enableButtonsBasedOnState(SimulationState state) {
		switch (state) {
			case STOPPED:
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				break;
			case RUNNING:
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				pauseButton.setEnabled(true);
				resumeButton.setEnabled(false);
				break;
			case PAUSED:
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				break;
		}
	}

	private JPanel createBehaviorPanel() {
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

		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("0"));
		labelTable.put(10, new JLabel("1"));
		labelTable.put(20, new JLabel("2"));
		slider.setLabelTable(labelTable);

		slider.setPaintLabels(true);
		slider.addChangeListener(this);
		return slider;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		double value = source.getValue() * 0.1;

		if (source == separationSlider) {
			model.setSeparationWeight(value);
		} else if (source == cohesionSlider) {
			model.setCohesionWeight(value);
		} else if (source == alignmentSlider) {
			model.setAlignmentWeight(value);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton source = (JButton) e.getSource();

		if (source == startButton) {
			startSimulation();
		} else if (source == stopButton) {
			stopSimulation();
		} else if (source == pauseButton) {
			pauseSimulation();
		} else if (source == resumeButton) {
			resumeSimulation();
		}
	}

	private void startSimulation() {
		simulator.startSimulation();
		enableButtonsBasedOnState(SimulationState.RUNNING);
	}

	private void stopSimulation() {
		simulator.stopSimulation();
		resetSimulation();
	}

	private void resetSimulation() {
		int numberOfBoids = promptForNumberOfBoids();

		if (numberOfBoids <= 0) {
			enableButtonsBasedOnState(SimulationState.STOPPED);
			return;
		}

		cohesionSlider.setValue(10);
		separationSlider.setValue(10);
		alignmentSlider.setValue(10);

		model.createBoids(numberOfBoids);
		boidsPanel.repaint();
		enableButtonsBasedOnState(SimulationState.STOPPED);
	}

	private void pauseSimulation() {
		simulator.pauseSimulation();
		enableButtonsBasedOnState(SimulationState.PAUSED);
	}

	private void resumeSimulation() {
		simulator.resumeSimulation();
		enableButtonsBasedOnState(SimulationState.RUNNING);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}