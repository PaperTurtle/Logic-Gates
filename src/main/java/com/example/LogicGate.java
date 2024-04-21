package com.example;

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

public abstract class LogicGate {
    // List to hold input gates
    protected List<LogicGate> inputs;
    // Path to the SVG file for this gate type
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

    public LogicGate(String svgFilePath, List<Point2D> inputPoints, Point2D outputPoint) {
        this.inputs = new ArrayList<>();
        this.svgFilePath = svgFilePath;
        this.inputPoints = (inputPoints != null) ? inputPoints : new ArrayList<>();
        this.outputPoint = outputPoint;

        for (int i = 0; i < this.inputPoints.size(); i++) {
            inputConnections.add(new ArrayList<>());
        }
    }

    /**
     * Abstract method to compute the output based on the inputs.
     */
    public abstract boolean evaluate();

    /**
     * Adds an input gate to this logic gate.
     * 
     * @param input the LogicGate to be added.
     */
    public void addInput(LogicGate input) {
        if (!inputs.contains(input)) {
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
            evaluate();
            propagateStateChange();
        }
    }

    public void addInputConnection(Line line, int inputIndex) {
        List<Line> connections = getInputConnections(inputIndex);
        if (connections != null) {
            connections.add(line);
        }
    }

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
            ((CircuitCanvas) canvas).setupOutputInteraction(outputMarker, this);
        }
    }

    public void propagateStateChange() {
        boolean newState = evaluate(); // Re-evaluate the state based on current inputs
        if (newState != currentState) {
            currentState = newState;
            updateOutputConnectionsColor(newState); // Update connection colors based on the new state

            for (LogicGate gate : outputGates) {
                gate.propagateStateChange(); // Propagate state change to connected gates
            }
            for (LogicGate gate : outputGates) {
                if (gate instanceof Lightbulb) {
                    ((Lightbulb) gate).toggleLight(currentState); // Specifically update lightbulb states
                }
            }
        }
    }

    public void updateOutputConnectionsColor(boolean state) {
        Color newColor = state ? Color.RED : Color.BLACK; // Use RED for true, BLACK for false
        for (Line line : outputConnections) {
            Platform.runLater(() -> line.setStroke(newColor)); // Ensure UI update happens on the JavaFX thread
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
        for (LogicGate gate : outputGates) {
            gate.removeInput(this);
            gate.removeInputConnection(line, gate.findInputConnectionIndex(line));
            gate.evaluate();
            gate.propagateStateChange();
        }
    }

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

    public List<LogicGate> getOutputGates() {
        return outputGates;
    }

    public void highlight() {
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
    }

    public void unhighlight() {
        imageView.setStyle("");
    }
}
