package com.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class SvgUtil {

    public static Image loadSvgImage(String svgFilePath) {
        try {
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(SvgUtil.class.getResourceAsStream(svgFilePath));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return SwingFXUtils.toFXImage(javax.imageio.ImageIO.read(inputStream), null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
