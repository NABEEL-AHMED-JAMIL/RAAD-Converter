package com.raad.converter.decoder;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;

public class CaptchaDecoder {

    public static void main(String args[]) throws Exception {
        Tesseract instance = new Tesseract();
        BufferedImage img = ImageIO.read(new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\captchasecurityimages.png"));
        String imgText = instance.doOCR(img);
        Thread.sleep(1000);
       System.out.println("Text " + imgText);
    }
}
