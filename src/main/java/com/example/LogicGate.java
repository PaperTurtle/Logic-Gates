package com.example;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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

    public LogicGate(String svgFilePath, List<Point2D> inputPoints, Point2D outputPoint) {
        this.inputs = new ArrayList<>();
        this.svgFilePath = svgFilePath;
        this.inputPoints = inputPoints;
        this.outputPoint = outputPoint;
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
        }
    }

    /**
     * Removes an input gate from this logic gate.
     * 
     * @param input the LogicGate to be removed.
     */
    public void removeInput(LogicGate input) {
        inputs.remove(input);
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

    public String getSvgFilePath() {
        return svgFilePath;
    }

    public List<Point2D> getInputPoints() {
        return inputPoints;
    }

    public void setInputPoints(List<Point2D> inputPoints) {
        this.inputPoints = inputPoints;
    }

    public Point2D getOutputPoint() {
        return outputPoint;
    }

    public void setOutputPoint(Point2D outputPoint) {
        this.outputPoint = outputPoint;
    }

    public void setPosition(double x, double y) {
        if (imageView != null) {
            imageView.setX(x);
            imageView.setY(y);
            updateMarkers();
        }
    }

    private void updateMarkers() {
        if (outputMarker != null) {
            outputMarker.setCenterX(imageView.getX() + outputPoint.getX());
            outputMarker.setCenterY(imageView.getY() + outputPoint.getY());
        }
        for (int i = 0; i < inputPoints.size(); i++) {
            Circle marker = inputMarkers.get(i);
            Point2D point = inputPoints.get(i);
            marker.setCenterX(imageView.getX() + point.getX());
            marker.setCenterY(imageView.getY() + point.getY());
        }
    }

    public void createVisualRepresentation(Pane canvas) {
        Image image = SvgUtil.loadSvgImage(getSvgFilePath());
        imageView = new ImageView(image);
        canvas.getChildren().add(imageView);

        outputMarker = new Circle(outputPoint.getX(), outputPoint.getY(), 5, Color.RED);
        canvas.getChildren().add(outputMarker);

        for (Point2D point : inputPoints) {
            Circle inputMarker = new Circle(point.getX(), point.getY(), 5, Color.BLUE);
            inputMarkers.add(inputMarker);
            canvas.getChildren().add(inputMarker);
        }

        if (canvas instanceof CircuitCanvas) {
            ((CircuitCanvas) canvas).setupOutputInteraction(outputMarker, this);
        }
    }

    public void handleDrag(double newX, double newY) {
        setPosition(newX, newY);
    }

    /**
     * Optional method to update the output of this gate.
     * This could be used to notify or update connected output devices or gates.
     */
    public void updateOutput() {
        // TODO Implement functionality later
    }

}
