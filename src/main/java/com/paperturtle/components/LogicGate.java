package com.paperturtle.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.paperturtle.data.ClipboardData;
import com.paperturtle.data.GateData;
import com.paperturtle.gui.CircuitCanvas;
import com.paperturtle.utils.CircuitComponent;
import com.paperturtle.utils.SvgUtil;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Pair;

/**
 * Abstract class representing a logic gate in a digital circuit.
 * 
 * @author Seweryn Czabanowski
 */
public abstract class LogicGate implements CircuitComponent {
    /**
     * The list of logic gates that serve as inputs to this gate.
     */
    protected List<LogicGate> inputs;

    /**
     * The file path to the SVG file that represents this gate.
     */
    protected String svgFilePath;

    /**
     * The list of points where input connections can be made to this gate.
     */
    protected List<Point2D> inputPoints;

    /**
     * The point where output connections can be made from this gate.
     */
    protected Point2D outputPoint;

    /**
     * The ImageView that displays this gate.
     */
    protected ImageView imageView;

    /**
     * The list of markers that indicate where input connections can be made to this
     * gate.
     */
    protected List<Circle> inputMarkers = new ArrayList<>();

    /**
     * The marker that indicates where output connections can be made from this
     * gate.
     */
    protected Circle outputMarker;

    /**
     * The list of connections made to each input of this gate.
     */
    protected List<List<Line>> inputConnections = new ArrayList<>();

    /**
     * The list of connections made from the output of this gate.
     */
    protected List<Line> outputConnections = new ArrayList<>();

    /**
     * The list of gates that are connected to the output of this gate.
     */
    protected List<LogicGate> outputGates = new ArrayList<>();

    /**
     * The current state of this gate (true for on, false for off).
     */
    protected boolean currentState = false;

    /**
     * A counter used to generate unique IDs for each instance of LogicGate.
     */
    private static long idCounter = 0;

    /**
     * The unique ID of this gate.
     */
    protected String id;

    private int maxOutputConnections = 1;

    /**
     * Constructs a LogicGate object with the specified SVG file path, input points,
     * and output point.
     * 
     * @param svgFilePath the path to the SVG file representing the gate.
     * @param inputPoints the list of input points.
     * @param outputPoint the output point.
     */
    public LogicGate(String svgFilePath, List<Point2D> inputPoints, Point2D outputPoint) {
        this.inputs = new ArrayList<>();
        this.svgFilePath = svgFilePath;
        this.inputPoints = (inputPoints != null) ? inputPoints : new ArrayList<>();
        this.outputPoint = outputPoint;
        this.id = "Gate" + idCounter++;

        for (int i = 0; i < this.inputPoints.size(); i++) {
            inputConnections.add(new ArrayList<>());
        }
    }

    /**
     * Abstract method to compute the output based on the inputs.
     * 
     * @return the output of the gate.
     */
    public abstract boolean evaluate();

    /**
     * Abstract method to get the truth table data for this gate.
     * 
     * @return the truth table data.
     */
    public abstract List<Pair<Boolean[], Boolean>> getTruthTableData();

    /**
     * Adds an input gate to this logic gate.
     * 
     * @param input the LogicGate to be added.
     */
    public void addInput(LogicGate input) {
        if (inputs.size() < inputPoints.size()) {
            inputs.add(input);
            evaluateAndPropagate();
        }
    }

    /**
     * Removes an input gate from this logic gate.
     * 
     * @param input the LogicGate to be removed.
     */
    public void removeInput(LogicGate input) {
        int index = inputs.indexOf(input);
        if (index != -1) {
            List<Line> connections = new ArrayList<>(inputConnections.get(index));
            connections.forEach(line -> removeInputConnection(line, index));
            inputs.remove(index);
            evaluateAndPropagate();
        }
    }

    /**
     * Adds an input connection to this gate.
     * 
     * @param line       the Line to be added.
     * @param inputIndex the index of the input connection.
     */
    public void addInputConnection(Line line, int inputIndex) {
        Optional.ofNullable(getInputConnections(inputIndex)).ifPresent(connections -> connections.add(line));
    }

    /**
     * Removes an input connection from this gate.
     * 
     * @param line       the Line to be removed.
     * @param inputIndex the index of the input connection.
     */
    public void removeInputConnection(Line line, int inputIndex) {
        if (inputIndex >= 0 && inputIndex < inputConnections.size()) {
            List<Line> connections = inputConnections.get(inputIndex);
            if (connections != null && connections.remove(line)) {
                evaluateAndPropagate();
            }
        }
    }

    /**
     * Sets the position of the gate on the canvas.
     * 
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     */
    public void setPosition(double x, double y) {
        if (imageView != null) {
            imageView.setX(x);
            imageView.setY(y);
            updateMarkers();
            updateConnections();
        }
    }

    /**
     * Updates the position of the markers and connections.
     */
    private void updateMarkers() {
        Optional.ofNullable(outputMarker).ifPresent(marker -> setMarkerPosition(marker, outputPoint));
        for (int i = 0; i < inputPoints.size(); i++) {
            if (i < inputMarkers.size()) {
                setMarkerPosition(inputMarkers.get(i), inputPoints.get(i));
            } else {
                System.err.println("No marker available for input point at index " + i);
            }
        }
    }

    /**
     * Sets the position of the marker on the canvas.
     * 
     * @param marker the marker to set the position for.
     * @param point  the point to set the position to.
     */
    private void setMarkerPosition(Circle marker, Point2D point) {
        marker.setCenterX(imageView.getX() + point.getX());
        marker.setCenterY(imageView.getY() + point.getY());
        marker.toFront();
    }

    /**
     * Updates the position of the output connections.
     */
    private void updateConnections() {
        Optional.ofNullable(outputMarker).ifPresent(marker -> {
            Point2D outputPos = marker.localToParent(marker.getCenterX(), marker.getCenterY());
            outputConnections.forEach(line -> setLineStart(line, outputPos));
        });

        for (int i = 0; i < inputMarkers.size(); i++) {
            Circle inputMarker = inputMarkers.get(i);
            Point2D inputPos = inputMarker.localToParent(inputMarker.getCenterX(), inputMarker.getCenterY());
            Optional.ofNullable(getInputConnections(i))
                    .ifPresent(connections -> connections.forEach(line -> setLineEnd(line, inputPos)));
        }
    }

    /**
     * Sets the start position of the line.
     * 
     * @param line the Line to be added.
     * @param pos  the position of the marker.
     */
    private void setLineStart(Line line, Point2D pos) {
        line.setStartX(pos.getX());
        line.setStartY(pos.getY());
    }

    /**
     * Sets the end position of the line.
     * 
     * @param line the Line to be added.
     * @param pos  the position of the marker.
     */
    private void setLineEnd(Line line, Point2D pos) {
        line.setEndX(pos.getX());
        line.setEndY(pos.getY());
    }

    /**
     * Creates the visual representation of the gate on the canvas.
     * 
     * @param canvas the canvas to draw the gate on.
     */
    public void createVisualRepresentation(Pane canvas) {
        imageView = new ImageView(SvgUtil.loadSvgImage(svgFilePath));
        canvas.getChildren().add(imageView);

        if (outputPoint != null) {
            outputMarker = createMarker(outputPoint, Color.RED, canvas);
        }

        inputPoints.forEach(point -> {
            Circle inputMarker = createMarker(point, Color.BLUE, canvas);
            inputMarkers.add(inputMarker);
            inputConnections.add(new ArrayList<>());
        });

        if (canvas instanceof CircuitCanvas && outputMarker != null) {
            ((CircuitCanvas) canvas).getInteractionManager().setupOutputInteraction(outputMarker, this);
        }
    }

    /**
     * Creates a marker on the canvas.
     * 
     * @param point  the point to create the marker at.
     * @param color  the color of the marker.
     * @param canvas the canvas to draw the marker on.
     * @return the Circle object representing the marker.
     */
    private Circle createMarker(Point2D point, Color color, Pane canvas) {
        Circle marker = new Circle(point.getX(), point.getY(), 5, color);
        canvas.getChildren().add(marker);
        marker.toFront();
        return marker;
    }

    /**
     * Propagates the state change to the output gates.
     */
    public void propagateStateChange() {
        boolean newState = evaluate();
        if (newState != currentState) {
            currentState = newState;
            updateOutputConnectionsColor(newState);
            outputGates.forEach(LogicGate::propagateStateChange);
        }
    }

    /**
     * Updates the color of the output connections based on the state.
     * 
     * @param state the state of the output.
     */
    public void updateOutputConnectionsColor(boolean state) {
        Color newColor = state ? Color.RED : Color.BLACK;
        outputConnections.forEach(line -> Platform.runLater(() -> line.setStroke(newColor)));
    }

    /**
     * Handles the drag event for the gate.
     * 
     * @param newX the new x-coordinate.
     * @param newY the new y-coordinate.
     */
    public void handleDrag(double newX, double newY) {
        setPosition(newX, newY);
    }

    /**
     * Adds an output connection to this gate.
     * 
     * @param line the Line to be added.
     */
    public void addOutputConnection(Line line) {
        if (outputConnections.size() < maxOutputConnections) {
            outputConnections.add(line);
        }
    }

    /**
     * Adds an output gate to this gate.
     * 
     * @param gate the LogicGate to be added.
     */
    public void addOutputGate(LogicGate gate) {
        if (gate != null && !outputGates.contains(gate)) {
            outputGates.add(gate);
        }
    }

    /**
     * Finds the index of the input connection for the given line.
     * 
     * @param line the Line to find the index for.
     * @return the index of the input connection.
     */
    public int findInputConnectionIndex(Line line) {
        return inputConnections.stream()
                .filter(connections -> connections.contains(line))
                .findFirst()
                .map(inputConnections::indexOf)
                .orElse(-1);
    }

    /**
     * Removes an output connection from this gate.
     * 
     * @param line the Line to be removed.
     */
    public void removeOutputConnection(Line line) {
        if (outputConnections.remove(line)) {
            List<LogicGate> outputGatesCopy = new ArrayList<>(outputGates);
            for (LogicGate gate : outputGatesCopy) {
                int index = gate.findInputConnectionIndex(line);
                if (index != -1) {
                    gate.removeInputConnection(line, index);
                    gate.removeInput(this);
                    gate.evaluateAndPropagate();
                }
            }
        }
    }

    /**
     * Removes an input connection from this gate.
     * 
     * @param line the Line to be removed.
     */
    public void removeInputConnection(Line line) {
        inputConnections.forEach(connections -> {
            if (connections.remove(line)) {
                evaluateAndPropagate();
            }
        });
    }

    /**
     * Returns the list of input gates for this gate.
     * 
     * @return the list of input gates.
     */
    public List<LogicGate> getInputs() {
        return inputs;
    }

    /**
     * Returns the list of input markers for this gate.
     * 
     * @return the list of input markers.
     */
    public List<Circle> getInputMarkers() {
        return inputMarkers;
    }

    /**
     * Returns the list of input connections for this gate.
     * 
     * @return the list of input connections.
     */
    public List<List<Line>> getInputConnections() {
        return inputConnections;
    }

    /**
     * Returns the input connections for the given index.
     * 
     * @param index the index of the input connection.
     * @return the list of input connections.
     */
    public List<Line> getInputConnections(int index) {
        return index >= 0 && index < inputConnections.size() ? inputConnections.get(index) : null;
    }

    /**
     * Returns the output marker for this gate.
     * 
     * @return the output marker.
     */
    public Circle getOutputMarker() {
        return outputMarker;
    }

    /**
     * Returns the list of output connections for this gate.
     * 
     * @return the list of output connections.
     */
    public List<Line> getOutputConnections() {
        return outputConnections;
    }

    /**
     * Returns the ImageView for this gate.
     * 
     * @return the ImageView.
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Returns the list of output gates for this gate.
     * 
     * @return the list of output gates.
     */
    public List<LogicGate> getOutputGates() {
        return outputGates;
    }

    /**
     * Returns the current output value of the gate.
     * This method will invoke evaluate() to ensure the latest input is returned.
     * 
     * @return the current output of the logic gate.
     */
    public boolean getOutput() {
        return evaluate();
    }

    /**
     * Returns the path to the SVG file for this gate.
     * 
     * @return the path to the SVG file.
     */
    public String getSvgFilePath() {
        return svgFilePath;
    }

    /**
     * Returns the list of input points for this gate.
     * 
     * @return the list of input points.
     */
    public List<Point2D> getInputPoints() {
        return inputPoints;
    }

    /**
     * Sets the list of input points for this gate.
     * 
     * @param inputPoints the list of input points.
     */
    public void setInputPoints(List<Point2D> inputPoints) {
        this.inputPoints = inputPoints;
    }

    /**
     * Returns the output point for this gate.
     * 
     * @return the output point.
     */
    public Point2D getOutputPoint() {
        return outputPoint;
    }

    /**
     * Sets the output point for this gate.
     * 
     * @param outputPoint the output point.
     */
    public void setOutputPoint(Point2D outputPoint) {
        this.outputPoint = outputPoint;
    }

    /**
     * Returns the data for this gate.
     * 
     * @return the GateData object.
     */
    public GateData getGateData() {
        GateData data = new GateData();
        data.id = getId();
        data.type = getClass().getSimpleName();
        data.position = getPosition();
        data.state = currentState;
        data.maxOutputConnections = getMaxOutputConnections();

        inputs.forEach(input -> data.inputs.add(new GateData.ConnectionData(input.getId(), inputs.indexOf(input))));
        outputGates.forEach(output -> {
            output.getInputConnections().forEach(connections -> connections.stream()
                    .filter(this::isConnected)
                    .forEach(line -> data.outputs.add(new GateData.ConnectionData(output.getId(),
                            output.getInputConnections().indexOf(connections)))));
        });

        return data;
    }

    /**
     * Returns the data for this gate to be copied to the clipboard.
     * 
     * @return the ClipboardData object.
     */
    public ClipboardData getGateClipboardData() {
        ClipboardData data = new ClipboardData();
        data.id = getId();
        data.type = getClass().getSimpleName();
        data.position = getPosition();
        data.state = currentState;
        data.maxOutputConnections = getMaxOutputConnections();

        inputs.forEach(input -> data.inputs
                .add(new ClipboardData.ConnectionData(input.getId(), input.outputGates.indexOf(this))));
        outputGates.forEach(output -> data.outputs
                .add(new ClipboardData.ConnectionData(output.getId(), output.inputs.indexOf(this))));

        return data;
    }

    /**
     * Returns the id of this gate.
     * 
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of this gate.
     * 
     * @param id the id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the position of the gate.
     * 
     * @return the position of the gate.
     */
    public Point2D getPosition() {
        return new Point2D(imageView.getX(), imageView.getY());
    }

    /**
     * Returns the type of the component.
     * 
     * @return the type of the component.
     */
    public String getComponentType() {
        return "gate";
    }

    /**
     * Checks if the line is connected to the output marker.
     * 
     * @param line the line to be added.
     * @return true if an input connection can be added, false otherwise.
     */
    private boolean isConnected(Line line) {
        return line.getStartX() == getOutputMarker().getCenterX() && line.getStartY() == getOutputMarker().getCenterY();
    }

    /**
     * Returns the maximum number of output connections allowed for this gate.
     * 
     * @return the maximum number of output connections.
     */
    public int getMaxOutputConnections() {
        return maxOutputConnections;
    }

    /**
     * Sets the maximum number of output connections allowed for this gate.
     * 
     * @param maxOutputConnections the maximum number of output connections.
     */
    public void setMaxOutputConnections(int maxOutputConnections) {
        this.maxOutputConnections = maxOutputConnections;
    }

    /**
     * Evaluates the gate and propagates the state change.
     */
    private void evaluateAndPropagate() {
        evaluate();
        propagateStateChange();
    }

}
