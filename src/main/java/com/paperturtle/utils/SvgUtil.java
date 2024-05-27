package com.paperturtle.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * The SvgUtil class provides utility methods for handling SVG files.
 * It includes a method to load an SVG file and convert it into a JavaFX Image.
 * 
 * @author Seweryn Czabanowski
 */
public class SvgUtil {
    /**
     * Default constructor for SvgUtil.
     */
    private SvgUtil() {
    }

    /**
     * Loads an SVG file from the specified path and converts it into a JavaFX
     * Image.
     * 
     * @param svgFilePath the path to the SVG file to load.
     * @return a JavaFX Image representing the SVG file, or null if an error
     *         occurred.
     */
    public static Image loadSvgImage(String svgFilePath) {
        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(SvgUtil.class.getResourceAsStream(svgFilePath));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
                return SwingFXUtils.toFXImage(javax.imageio.ImageIO.read(inputStream), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
