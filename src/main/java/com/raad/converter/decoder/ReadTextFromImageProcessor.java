package com.raad.converter.decoder;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;


@Component
@Scope("prototype")
public class ReadTextFromImageProcessor {

    public Logger logger = LoggerFactory.getLogger(ReadTextFromImageProcessor.class);

    private ITesseract instance = new Tesseract();

    public static void imageProcessV1(String filename) throws IOException, TesseractException {
        String imagePath = String.format("C:\\Users\\Nabeel.Ahmed\\Downloads\\captcha\\%s.png", UUID.randomUUID());
        // load the open cv
        OpenCV.loadShared();
        // load the original image
        Mat original_image = Imgcodecs.imread("C:\\Users\\Nabeel.Ahmed\\Downloads\\captcha\\"+filename, IMREAD_GRAYSCALE);
        // large image for remvoe noise
        Mat large_image = new Mat();
        Size kernelSize = new Size(original_image.width()*2, original_image.height()*2);
        Imgproc.resize(original_image,large_image,kernelSize);
        Mat final_image = new Mat(large_image.rows(), large_image.cols(), CvType.CV_8UC4);
        Imgproc.threshold(large_image, final_image, 100, 150, Imgproc.CCL_GRANA);
        // small again after noise remove
        Mat small_image = new Mat();
        kernelSize = new Size(final_image.width(), final_image.height());
        Imgproc.resize(final_image, small_image, kernelSize);
        Imgcodecs.imwrite(imagePath,small_image);
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", small_image, mob);
        byte ba[] = mob.toArray();
        BufferedImage ipimage = ImageIO.read(new ByteArrayInputStream(ba));

//        double d = ipimage.getRGB(ipimage.getTileWidth() / 2,ipimage.getTileHeight() / 2);
//        if (d >= -1.4211511E7 && d < -7254228) {
//            processImg(ipimage, 3f, -10f);
//        }
//        else if (d >= -7254228 && d < -2171170) {
//            processImg(ipimage, 1.455f, -47f);
//        }
//        else if (d >= -2171170 && d < -1907998) {
//            processImg(ipimage, 1.35f, -10f);
//        }
//        else if (d >= -1907998 && d < -257) {
//            processImg(ipimage, 1.19f, 0.5f);
//        }
//        else if (d >= -257 && d < -1) {
//            processImg(ipimage, 1f, 0.5f);
//        }
//        else if (d >= -1 && d < 2) {
//            processImg(ipimage, 1f, 0.35f);
//        }

////        ipimage = processImg(ipimage);
//        ImageIO.write(ipimage, "png", new File(imagePath));
        ITesseract instance = new Tesseract();
        System.out.println(filename + " " + instance.doOCR(ipimage));
    }


    public static void processImg(BufferedImage ipimage, float scaleFactor, float offset) throws IOException, TesseractException {
        BufferedImage opimage = new BufferedImage(ipimage.getHeight(), ipimage.getHeight(), ipimage.getType());
        Graphics2D graphic = opimage.createGraphics();
        graphic.drawImage(ipimage, 0, 0, ipimage.getHeight(), ipimage.getHeight(), null);
        graphic.dispose();
        RescaleOp rescale = new RescaleOp(3f, -10f, null);
        BufferedImage fopimage = rescale.filter(opimage, null);
        ImageIO.write(fopimage, "jpg",new File(String.format("C:\\Users\\Nabeel.Ahmed\\Downloads\\captcha\\%s.png", UUID.randomUUID())));
    }


    public static void listFilesForFolder(final File folder) throws IOException, TesseractException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                //imageProcessV1(fileEntry.getName());
                System.out.println(String.format("<p>%s <a href=\"%s\"> %s</a></p>", fileEntry.getName(), fileEntry.getName(),fileEntry.getName()));
            }
        }
    }

}
