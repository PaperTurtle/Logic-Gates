package com.paperturtle;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Pair;

public abstract class LogicGate implements CircuitComponent {
    protected List<LogicGate> inputs;
    protected String svgFilePath;
    protected List<Point2D> inputPoints;
    protected Point2D outputPoint;
    protected ImageView imageView;
    protected List<Circle> inputMarkers = new ArrayList<>();
    protected Circle outputMarker;
    protected List<List<Line>> inputConnections = new ArrayList<>();
    protected List<Line> outputConnections = new ArrayList<>();
    protected List<LogicGate> outputGates = new ArrayList<>();
    protected boolean currentState = false;
    private static long idCounter = 0;
    protected String id;

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
        if (inputs.size() < inputPoints.size() && !inputs.contains(input)) {
            inputs.add(input);
            evaluate();
            propagateStateChange();
        }
    }

    /**
     * Removes an input gate from this logic gate.
     * 
     * @param input the LogicGate to be removed.
     */
    public void removeInput(LogicGate input) {
        if (inputs.contains(input)) {
            inputs.remove(input);
            int index = inputs.indexOf(input);
            if (index != -1) {
                List<Line> connectionsToRemove = new ArrayList<>(inputConnections.get(index));
                for (Line line : connectionsToRemove) {
                    this.removeInputConnection(line, index);
                }
            }
            evaluate();
            propagateStateChange();
        }
    }

    /**
     * Adds an input connection to this gate.
     * 
     * @param line       the Line to be added.
     * @param inputIndex the index of the input connection.
     */
    public void addInputConnection(Line line, int inputIndex) {
        List<Line> connections = getInputConnections(inputIndex);
        if (connections != null) {
            connections.add(line);
        }
    }

    /**
     * Removes an input connection from this gate.
     * 
     * @param line       the Line to be removed.
     * @param inputIndex the index of the input connection.
     */
    public void removeInputConnection(Line line, int inputIndex) {
        if (inputIndex >= 0 && inputIndex < inputConnections.size()) {
            inputConnections.get(inputIndex).remove(line);
        }
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
        if (outputMarker != null) {
            outputMarker.setCenterX(imageView.getX() + outputPoint.getX());
            outputMarker.setCenterY(imageView.getY() + outputPoint.getY());
        }
        for (int i = 0; i < inputPoints.size(); i++) {
            if (i < inputMarkers.size()) {
                Circle marker = inputMarkers.get(i);
                Point2D point = inputPoints.get(i);
                marker.setCenterX(imageView.getX() + point.getX());
                marker.setCenterY(imageView.getY() + point.getY());
            } else {
                System.err.println("No marker available for input point at index " + i);
            }
        }
    }

    /**
     * Updates the position of the output connections.
     */
    private void updateConnections() {
        if (outputMarker != null) {
            Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(), outputMarker.getCenterY());
            for (Line line : outputConnections) {
                line.setStartX(outputPos.getX());
                line.setStartY(outputPos.getY());
            }
        }

        for (int i = 0; i < inputMarkers.size(); i++) {
            Circle inputMarker = inputMarkers.get(i);
            Point2D inputPos = inputMarker.localToParent(inputMarker.getCenterX(), inputMarker.getCenterY());

            List<Line> connections = getInputConnections(i);
            if (connections != null) {
                for (Line inputLine : connections) {
                    inputLine.setEndX(inputPos.getX());
                    inputLine.setEndY(inputPos.getY());
                }
            }
        }
    }

    /**
     * Returns the input connections for the given index.
     * 
     * @param index the index of the input connection.
     * @return the list of input connections.
     */
    public List<Line> getInputConnections(int index) {
        if (index >= 0 && index < inputConnections.size()) {
            return inputConnections.get(index);
        }
        return null;
    }

    /**
     * Creates the visual representation of the gate on the canvas.
     * 
     * @param canvas the canvas to draw the gate on.
     */
    public void createVisualRepresentation(Pane canvas) {
        Image image = SvgUtil.loadSvgImage(getSvgFilePath());
        imageView = new ImageView(image);
        canvas.getChildren().add(imageView);

        if (outputPoint != null) {
            outputMarker = new Circle(outputPoint.getX(), outputPoint.getY(), 5, Color.RED);
            canvas.getChildren().add(outputMarker);
        }

        if (inputPoints != null) {
            inputConnections.clear();
            for (Point2D point : inputPoints) {
                Circle inputMarker = new Circle(point.getX(), point.getY(), 5, Color.BLUE);
                inputMarkers.add(inputMarker);
                canvas.getChildren().add(inputMarker);
                inputConnections.add(new ArrayList<>());
            }
        }

        if (canvas instanceof CircuitCanvas && outputMarker != null) {
            ((CircuitCanvas) canvas).getInteractionManager().setupOutputInteraction(outputMarker, this);
        }
    }

    /**
     * Propagates the state change to the output gates.
     */
    public void propagateStateChange() {
        boolean newState = evaluate();
        if (newState != currentState) {
            currentState = newState;
            updateOutputConnectionsColor(newState);

            for (LogicGate gate : outputGates) {
                gate.propagateStateChange();
            }
            for (LogicGate gate : outputGates) {
                if (gate instanceof Lightbulb) {
                    ((Lightbulb) gate).toggleLight(currentState);
                }
            }
        }
    }

    /**
     * Updates the color of the output connections based on the state.
     * 
     * @param state the state of the output.
     */
    public void updateOutputConnectionsColor(boolean state) {
        Color newColor = state ? Color.RED : Color.BLACK;
        for (Line line : outputConnections) {
            Platform.runLater(() -> line.setStroke(newColor));
        }
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
        outputConnections.add(line);
    }

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
        for (List<Line> connections : inputConnections) {
            if (connections.contains(line)) {
                return inputConnections.indexOf(connections);
            }
        }
        return -1;
    }

    /**
     * Removes an output connection from this gate.
     * 
     * @param line the Line to be removed.
     */
    public void removeOutputConnection(Line line) {
        outputConnections.remove(line);
        for (LogicGate gate : new ArrayList<>(outputGates)) {
            gate.removeInput(this);
            gate.removeInputConnection(line, gate.findInputConnectionIndex(line));
            gate.evaluate();
            gate.propagateStateChange();
        }
    }

    /**
     * Removes an input connection from this gate.
     * 
     * @param line the Line to be removed.
     */
    public void removeInputConnection(Line line) {
        inputConnections.stream()
                .filter(connections -> connections.contains(line))
                .forEach(connections -> {
                    connections.remove(line);
                    evaluate();
                    propagateStateChange();
                });
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
     * Returns the data for this gate.
     * 
     * @return the GateData object.
     */
    public GateData getGateData() {
        GateData data = new GateData();
        data.id = this.getId();
        data.type = this.getClass().getSimpleName();
        data.position = new Point2D(imageView.getX(), imageView.getY());
        data.state = currentState;

        for (LogicGate input : inputs) {
            data.inputs.add(
                    new GateData.ConnectionData(input.getId(), input.outputGates.indexOf(this)));
        }

        for (LogicGate output : outputGates) {
            data.outputs
                    .add(new GateData.ConnectionData(output.getId(), output.inputs.indexOf(this)));
        }

        return data;
    }

    /**
     * Returns the data for this gate to be copied to the clipboard.
     * 
     * @return the ClipboardData object.
     */
    public ClipboardData getGateClipboardData() {
        ClipboardData data = new ClipboardData();
        data.id = this.getId();
        data.type = this.getClass().getSimpleName();
        data.position = new Point2D(imageView.getX(), imageView.getY());
        data.state = currentState;

        for (LogicGate input : inputs) {
            data.inputs.add(
                    new ClipboardData.ConnectionData(input.getId(), input.outputGates.indexOf(this)));
        }

        for (LogicGate output : outputGates) {
            data.outputs
                    .add(new ClipboardData.ConnectionData(output.getId(), output.inputs.indexOf(this)));
        }

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

}
