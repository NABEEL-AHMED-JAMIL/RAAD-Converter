package com.raad.converter.api;


import com.raad.converter.comparison.ImageComparison;
import com.raad.converter.domain.*;
import com.raad.converter.util.ReadTextFromImageProcessor;
import com.raad.converter.util.ExceptionUtil;
import io.swagger.annotations.Api;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;


@RestController
@RequestMapping("/captcha")
@CrossOrigin(origins = "*")
@Api(tags = {"RAAD-Image-Processing"})
public class RaadImageProcessingApi {

    public Logger logger = LoggerFactory.getLogger(RaadImageProcessingApi.class);

    public String PNG = ".png";
    private ResponseDTO response;
    private Map<String, Object> extData;

    public String imagePath = String.format("C:\\Users\\Nabeel.Ahmed\\Downloads\\captcha\\%s.png", UUID.randomUUID());

    @Autowired
    private ReadTextFromImageProcessor imageProcessor;
    private ITesseract instance = new Tesseract();

    @RequestMapping(path = "/image-text-reader",  method = RequestMethod.POST)
    public ResponseEntity<?> imageTextReader(ImageProcessReqeust imageProcessReqeust) {
        try {
            ResponseDTO response = new ResponseDTO();
            response.setMessage("Success Process");
            BufferedImage bufferedImage = imageCleanProcess(imageProcessReqeust);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            baos.flush();
            response.setData(imageInByte);
            response.setText(readTextFromImage(bufferedImage));
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            logger.error("imageTextReader -- Error occurred " + ex);
            return ResponseEntity.ok().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

    @RequestMapping(path = "/image-compare-reader",  method = RequestMethod.POST)
    public ResponseEntity<?> imageCompareReader(ImageCompare imageCompare) {
        try {
            ResponseDTO response = new ResponseDTO();
            response.setMessage("Success Process");
            FileSocket fileSocket = imageCompare(imageCompare);
            response.setData(fileSocket);
            response.setText("Image Compare");
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            logger.error("imageTextReader -- Error occurred " + ex);
            return ResponseEntity.ok().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

    //http://mundrisoft.com/tech-bytes/compare-images-using-java/
    @RequestMapping(path = "/imageReaderV1",  method = RequestMethod.POST)
    public ResponseEntity<?> imageReaderV1(ImageCompare imageCompare) {
        try {
            response = new ResponseDTO();
            // base image
            BufferedImage bImage = ImageIO.read(imageCompare.getOldImage().getInputStream());
            // compare image
            BufferedImage cImage = ImageIO.read(imageCompare.getNewImage().getInputStream());
            // height & width
            int height = bImage.getHeight();
            int width = bImage.getWidth();
            // process image
            BufferedImage rImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    try {
                        int pixelC = cImage.getRGB(x, y);
                        int pixelB = bImage.getRGB(x, y);
                        if (pixelB == pixelC ) {
                            rImage.setRGB(x, y,  bImage.getRGB(x, y));
                        } else {
                            int a= 0xff |  bImage.getRGB(x, y)>>24 ,
                                    r= 0xff &  bImage.getRGB(x, y)>>16 ,
                                    g= 0x00 &  bImage.getRGB(x, y)>>8,
                                    b= 0x00 &  bImage.getRGB(x, y);

                            int modifiedRGB=a<<24|r<<16|g<<8|b;
                            rImage.setRGB(x,y,modifiedRGB);
                        }
                    } catch (Exception e) {
                        // handled hieght or width mismatch
                        rImage.setRGB(x, y, 0x80ff0000);
                    }
                }
            }
            // convert buffered image to image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String imgType = imageCompare.getOldImage().getOriginalFilename()
                    .substring(imageCompare.getOldImage().getOriginalFilename().indexOf("."));
            if (imgType.equalsIgnoreCase(PNG)) {
                ImageIO.write(rImage, "png", baos);
            } else {
                ImageIO.write(rImage, "jpg", baos);
            }
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            baos.flush();
            response.setData(imageInByte);
            response.setMessage("Image Process Result.");
            return ResponseEntity.ok().body(response);
        } catch (Exception ex) {
            response.setMessage("Process Fail.");
            response.setText("Some error occurs.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @RequestMapping(path = "/imageReaderV2",  method = RequestMethod.POST)
    public ResponseEntity<?> imageReaderV2(ImageCompare imageCompare) {
        try {
            response = new ResponseDTO();
            extData = new HashMap<>();
            // base image
            BufferedImage bImage = ImageIO.read(imageCompare.getOldImage().getInputStream());
            // compare image
            BufferedImage cImage = ImageIO.read(imageCompare.getNewImage().getInputStream());
            //Create ImageComparison object for it.
            ImageComparison imageComparison = new ImageComparison(bImage, cImage);
            // oky
            if(imageCompare.getThreshold() != null) {
                imageComparison.setThreshold(imageCompare.getThreshold());
            }
            // oky
            if(imageCompare.getRectangleLineWidth() != null) {
                imageComparison.setRectangleLineWidth(imageCompare.getRectangleLineWidth());
            }
            // oky
            if(imageCompare.getFillDifferenceRectangles() != null
                    && imageCompare.getPercentOpacityDifferenceRectangles() != null) {
                imageComparison.setDifferenceRectangleFilling(imageCompare.getFillDifferenceRectangles(),
                        imageCompare.getPercentOpacityDifferenceRectangles());
            }
            if(imageCompare.getFillExcludedRectangles() != null && imageCompare.getPercentOpacityExcludedRectangles() != null) {
                imageComparison.setExcludedRectangleFilling(imageCompare.getFillExcludedRectangles(),
                        imageCompare.getPercentOpacityExcludedRectangles());
            }
            if(imageCompare.getDrawExcludedRectangles() != null) {
                imageComparison.setDrawExcludedRectangles(imageCompare.getDrawExcludedRectangles());
            }
            if(imageCompare.getMaximalRectangleCount() != null) {
                imageComparison.setMaximalRectangleCount(imageCompare.getMaximalRectangleCount());
            }
            if(imageCompare.getMinimalRectangleSize() != null) {
                imageComparison.setMinimalRectangleSize(imageCompare.getMinimalRectangleSize());
            }
            if(imageCompare.getPixelToleranceLevel() != null) {
                imageComparison.setPixelToleranceLevel(imageCompare.getPixelToleranceLevel());
            }
            if(imageCompare.getAllowingPercentOfDifferentPixels() != null) {
                imageComparison.setAllowingPercentOfDifferentPixels(imageCompare.getAllowingPercentOfDifferentPixels());
            }
            //After configuring the ImageComparison object, can be executed compare() method:
            ImageComparisonResult imageComparisonResult = imageComparison.compareImages();
            // result
            extData.put("Status", imageComparisonResult.getImageComparisonState());
            if(imageComparisonResult.getRectangles() != null) {
                extData.put("total_rect", imageComparisonResult.getRectangles().size());
                extData.put("rect_size", imageComparisonResult.getRectangles()
                        .stream().map(rectangle -> {
                            return rectangle.size();
                        }).collect(Collectors.toList()));
            }
            System.out.println(extData);
            //And Result Image
            BufferedImage resultImage = imageComparisonResult.getResult();
            // convert buffered image to image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String imgType = imageCompare.getOldImage().getOriginalFilename()
                    .substring(imageCompare.getOldImage().getOriginalFilename().indexOf("."));
            if (imgType.equalsIgnoreCase(PNG)) {
                ImageIO.write(resultImage, "png", baos);
            } else {
                ImageIO.write(resultImage, "jpg", baos);
            }
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            baos.flush();
            response.setData(imageInByte);
            response.setMessage("Image Process Result.");
            response.setExt(extData);
            return ResponseEntity.ok().body(response);

        } catch (Exception ex) {
            response.setMessage("Process Fail.");
            response.setText("Some error occurs.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    public BufferedImage imageCleanProcess(ImageProcessReqeust imageProcessReqeust) throws Exception {
        logger.info("Process For Clean Noise");
        // load the open cv
        OpenCV.loadShared();
        Mat original_image = bufferedImageToMat(ImageIO.read(imageProcessReqeust.getFile().getInputStream()));
        // save the image with in local store
        Imgcodecs.imwrite(imagePath,original_image);
        // now read image with gray scale
        original_image = Imgcodecs.imread(imagePath, IMREAD_GRAYSCALE);
        Size kernelSize = new Size(original_image.width()*imageProcessReqeust.getSize(), original_image.height()*imageProcessReqeust.getSize());
        // resize the image and place into the same place
        Imgproc.resize(original_image,original_image,kernelSize);
        // thres-hold and image place into the same place
        if(imageProcessReqeust.getProcess().equals("LINE_AA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.LINE_AA);
        } else if(imageProcessReqeust.getProcess().equals("LINE_8")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.LINE_8);
        } else if(imageProcessReqeust.getProcess().equals("LINE_4")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.LINE_4);
        } else if(imageProcessReqeust.getProcess().equals("CV_BLUR_NO_SCALE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_BLUR_NO_SCALE);
        } else if(imageProcessReqeust.getProcess().equals("CV_BLUR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_BLUR);
        } else if(imageProcessReqeust.getProcess().equals("CV_GAUSSIAN")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_GAUSSIAN);
        } else if(imageProcessReqeust.getProcess().equals("CV_MEDIAN")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_MEDIAN);
        } else if(imageProcessReqeust.getProcess().equals("CV_BILATERAL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_BILATERAL);
        } else if(imageProcessReqeust.getProcess().equals("CV_GAUSSIAN_5x5")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_GAUSSIAN_5x5);
        } else if(imageProcessReqeust.getProcess().equals("CV_SCHARR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_SCHARR);
        } else if(imageProcessReqeust.getProcess().equals("CV_MAX_SOBEL_KSIZE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_MAX_SOBEL_KSIZE);
        } else if(imageProcessReqeust.getProcess().equals("CV_RGBA2mRGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_RGBA2mRGBA);
        } else if(imageProcessReqeust.getProcess().equals("CV_mRGBA2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_mRGBA2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("CV_WARP_FILL_OUTLIERS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_WARP_FILL_OUTLIERS);
        } else if(imageProcessReqeust.getProcess().equals("CV_WARP_INVERSE_MAP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_WARP_INVERSE_MAP);
        } else if(imageProcessReqeust.getProcess().equals("CV_SHAPE_RECT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_SHAPE_RECT);
        } else if(imageProcessReqeust.getProcess().equals("CV_SHAPE_CROSS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_SHAPE_CROSS);
        } else if(imageProcessReqeust.getProcess().equals("CV_SHAPE_ELLIPSE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_SHAPE_ELLIPSE);
        } else if(imageProcessReqeust.getProcess().equals("CV_SHAPE_CUSTOM")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_SHAPE_CUSTOM);
        } else if(imageProcessReqeust.getProcess().equals("CV_CHAIN_CODE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_CHAIN_CODE);
        } else if(imageProcessReqeust.getProcess().equals("CV_LINK_RUNS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_LINK_RUNS);
        } else if(imageProcessReqeust.getProcess().equals("CV_POLY_APPROX_DP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_POLY_APPROX_DP);
        } else if(imageProcessReqeust.getProcess().equals("CV_CONTOURS_MATCH_I1")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_CONTOURS_MATCH_I1);
        } else if(imageProcessReqeust.getProcess().equals("CV_CONTOURS_MATCH_I2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_CONTOURS_MATCH_I2);
        } else if(imageProcessReqeust.getProcess().equals("CV_CONTOURS_MATCH_I3")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_CONTOURS_MATCH_I3);
        } else if(imageProcessReqeust.getProcess().equals("CV_CLOCKWISE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_CLOCKWISE);
        } else if(imageProcessReqeust.getProcess().equals("CV_COUNTER_CLOCKWISE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COUNTER_CLOCKWISE);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_CORREL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_CORREL);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_CHISQR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_CHISQR);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_INTERSECT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_INTERSECT);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_BHATTACHARYYA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_BHATTACHARYYA);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_HELLINGER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_HELLINGER);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_CHISQR_ALT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_CHISQR_ALT);
        } else if(imageProcessReqeust.getProcess().equals("CV_COMP_KL_DIV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_COMP_KL_DIV);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_MASK_3")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_MASK_3);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_MASK_5")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_MASK_5);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_MASK_PRECISE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_MASK_PRECISE);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_LABEL_CCOMP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_LABEL_CCOMP);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_LABEL_PIXEL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_LABEL_PIXEL);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_USER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_USER);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_L1")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_L1);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_L2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_L2);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_C")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_C);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_L12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_L12);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_FAIR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_FAIR);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_WELSCH")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_WELSCH);
        } else if(imageProcessReqeust.getProcess().equals("CV_DIST_HUBER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_DIST_HUBER);
        } else if(imageProcessReqeust.getProcess().equals("CV_CANNY_L2_GRADIENT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_CANNY_L2_GRADIENT);
        } else if(imageProcessReqeust.getProcess().equals("CV_HOUGH_STANDARD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_HOUGH_STANDARD);
        } else if(imageProcessReqeust.getProcess().equals("CV_HOUGH_PROBABILISTIC")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_HOUGH_PROBABILISTIC);
        } else if(imageProcessReqeust.getProcess().equals("CV_HOUGH_MULTI_SCALE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_HOUGH_MULTI_SCALE);
        } else if(imageProcessReqeust.getProcess().equals("CV_HOUGH_GRADIENT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CV_HOUGH_GRADIENT);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_ERODE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_ERODE);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_DILATE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_DILATE);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_OPEN")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_OPEN);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_CLOSE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_CLOSE);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_GRADIENT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_GRADIENT);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_TOPHAT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_TOPHAT);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_BLACKHAT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_BLACKHAT);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_HITMISS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_HITMISS);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_RECT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_RECT);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_CROSS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_CROSS);
        } else if(imageProcessReqeust.getProcess().equals("MORPH_ELLIPSE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MORPH_ELLIPSE);
        } else if(imageProcessReqeust.getProcess().equals("INTER_NEAREST")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_NEAREST);
        } else if(imageProcessReqeust.getProcess().equals("INTER_LINEAR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_LINEAR);
        } else if(imageProcessReqeust.getProcess().equals("INTER_CUBIC")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_CUBIC);
        } else if(imageProcessReqeust.getProcess().equals("INTER_AREA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_AREA);
        } else if(imageProcessReqeust.getProcess().equals("INTER_LANCZOS4")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_LANCZOS4);
        } else if(imageProcessReqeust.getProcess().equals("INTER_LINEAR_EXACT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_LINEAR_EXACT);
        } else if(imageProcessReqeust.getProcess().equals("INTER_MAX")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_MAX);
        } else if(imageProcessReqeust.getProcess().equals("WARP_FILL_OUTLIERS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.WARP_FILL_OUTLIERS);
        } else if(imageProcessReqeust.getProcess().equals("WARP_INVERSE_MAP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.WARP_INVERSE_MAP);
        } else if(imageProcessReqeust.getProcess().equals("WARP_POLAR_LINEAR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.WARP_POLAR_LINEAR);
        } else if(imageProcessReqeust.getProcess().equals("WARP_POLAR_LOG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.WARP_POLAR_LOG);
        } else if(imageProcessReqeust.getProcess().equals("INTER_BITS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_BITS);
        } else if(imageProcessReqeust.getProcess().equals("INTER_BITS2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_BITS2);
        } else if(imageProcessReqeust.getProcess().equals("INTER_TAB_SIZE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_TAB_SIZE);
        } else if(imageProcessReqeust.getProcess().equals("INTER_TAB_SIZE2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTER_TAB_SIZE2);
        } else if(imageProcessReqeust.getProcess().equals("DIST_USER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_USER);
        } else if(imageProcessReqeust.getProcess().equals("DIST_L1")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_L1);
        } else if(imageProcessReqeust.getProcess().equals("DIST_L2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_L2);
        } else if(imageProcessReqeust.getProcess().equals("DIST_C")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_C);
        } else if(imageProcessReqeust.getProcess().equals("DIST_L12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_L12);
        } else if(imageProcessReqeust.getProcess().equals("DIST_FAIR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_FAIR);
        } else if(imageProcessReqeust.getProcess().equals("DIST_WELSCH")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_WELSCH);
        } else if(imageProcessReqeust.getProcess().equals("DIST_HUBER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_HUBER);
        } else if(imageProcessReqeust.getProcess().equals("DIST_MASK_3")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_MASK_3);
        } else if(imageProcessReqeust.getProcess().equals("DIST_MASK_5")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_MASK_5);
        } else if(imageProcessReqeust.getProcess().equals("DIST_MASK_PRECISE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_MASK_PRECISE);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_BINARY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_BINARY);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_BINARY_INV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_BINARY_INV);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_TRUNC")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_TRUNC);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_TOZERO")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_TOZERO);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_TOZERO_INV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_TOZERO_INV);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_MASK")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_MASK);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_OTSU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_OTSU);
        } else if(imageProcessReqeust.getProcess().equals("THRESH_TRIANGLE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.THRESH_TRIANGLE);
        } else if(imageProcessReqeust.getProcess().equals("ADAPTIVE_THRESH_MEAN_C")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.ADAPTIVE_THRESH_MEAN_C);
        } else if(imageProcessReqeust.getProcess().equals("ADAPTIVE_THRESH_GAUSSIAN_C")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        } else if(imageProcessReqeust.getProcess().equals("PROJ_SPHERICAL_ORTHO")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.PROJ_SPHERICAL_ORTHO);
        } else if(imageProcessReqeust.getProcess().equals("PROJ_SPHERICAL_EQRECT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.PROJ_SPHERICAL_EQRECT);
        } else if(imageProcessReqeust.getProcess().equals("GC_BGD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_BGD);
        } else if(imageProcessReqeust.getProcess().equals("GC_FGD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_FGD);
        } else if(imageProcessReqeust.getProcess().equals("GC_PR_BGD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_PR_BGD);
        } else if(imageProcessReqeust.getProcess().equals("GC_PR_FGD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_PR_FGD);
        } else if(imageProcessReqeust.getProcess().equals("GC_INIT_WITH_RECT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_INIT_WITH_RECT);
        } else if(imageProcessReqeust.getProcess().equals("GC_INIT_WITH_MASK")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_INIT_WITH_MASK);
        } else if(imageProcessReqeust.getProcess().equals("GC_EVAL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_EVAL);
        } else if(imageProcessReqeust.getProcess().equals("GC_EVAL_FREEZE_MODEL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.GC_EVAL_FREEZE_MODEL);
        } else if(imageProcessReqeust.getProcess().equals("DIST_LABEL_CCOMP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_LABEL_CCOMP);
        } else if(imageProcessReqeust.getProcess().equals("DIST_LABEL_PIXEL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.DIST_LABEL_PIXEL);
        } else if(imageProcessReqeust.getProcess().equals("FLOODFILL_FIXED_RANGE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.FLOODFILL_FIXED_RANGE);
        } else if(imageProcessReqeust.getProcess().equals("FLOODFILL_MASK_ONLY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.FLOODFILL_MASK_ONLY);
        } else if(imageProcessReqeust.getProcess().equals("CC_STAT_LEFT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CC_STAT_LEFT);
        } else if(imageProcessReqeust.getProcess().equals("CC_STAT_TOP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CC_STAT_TOP);
        } else if(imageProcessReqeust.getProcess().equals("CC_STAT_WIDTH")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CC_STAT_WIDTH);
        } else if(imageProcessReqeust.getProcess().equals("CC_STAT_HEIGHT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CC_STAT_HEIGHT);
        } else if(imageProcessReqeust.getProcess().equals("CC_STAT_AREA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CC_STAT_AREA);
        } else if(imageProcessReqeust.getProcess().equals("CC_STAT_MAX")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CC_STAT_MAX);
        } else if(imageProcessReqeust.getProcess().equals("CCL_WU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CCL_WU);
        } else if(imageProcessReqeust.getProcess().equals("CCL_DEFAULT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CCL_DEFAULT);
        } else if(imageProcessReqeust.getProcess().equals("CCL_GRANA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CCL_GRANA);
        } else if(imageProcessReqeust.getProcess().equals("RETR_EXTERNAL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.RETR_EXTERNAL);
        } else if(imageProcessReqeust.getProcess().equals("RETR_LIST")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.RETR_LIST);
        } else if(imageProcessReqeust.getProcess().equals("RETR_CCOMP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.RETR_CCOMP);
        } else if(imageProcessReqeust.getProcess().equals("RETR_TREE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.RETR_TREE);
        } else if(imageProcessReqeust.getProcess().equals("RETR_FLOODFILL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.RETR_FLOODFILL);
        } else if(imageProcessReqeust.getProcess().equals("CHAIN_APPROX_NONE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CHAIN_APPROX_NONE);
        } else if(imageProcessReqeust.getProcess().equals("CHAIN_APPROX_SIMPLE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CHAIN_APPROX_SIMPLE);
        } else if(imageProcessReqeust.getProcess().equals("CHAIN_APPROX_TC89_L1")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CHAIN_APPROX_TC89_L1);
        } else if(imageProcessReqeust.getProcess().equals("CHAIN_APPROX_TC89_KCOS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CHAIN_APPROX_TC89_KCOS);
        } else if(imageProcessReqeust.getProcess().equals("CONTOURS_MATCH_I1")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CONTOURS_MATCH_I1);
        } else if(imageProcessReqeust.getProcess().equals("CONTOURS_MATCH_I2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CONTOURS_MATCH_I2);
        } else if(imageProcessReqeust.getProcess().equals("CONTOURS_MATCH_I3")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.CONTOURS_MATCH_I3);
        } else if(imageProcessReqeust.getProcess().equals("HOUGH_STANDARD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HOUGH_STANDARD);
        } else if(imageProcessReqeust.getProcess().equals("HOUGH_PROBABILISTIC")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HOUGH_PROBABILISTIC);
        } else if(imageProcessReqeust.getProcess().equals("HOUGH_MULTI_SCALE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HOUGH_MULTI_SCALE);
        } else if(imageProcessReqeust.getProcess().equals("HOUGH_GRADIENT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HOUGH_GRADIENT);
        } else if(imageProcessReqeust.getProcess().equals("LSD_REFINE_NONE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.LSD_REFINE_NONE);
        } else if(imageProcessReqeust.getProcess().equals("LSD_REFINE_STD")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.LSD_REFINE_STD);
        } else if(imageProcessReqeust.getProcess().equals("LSD_REFINE_ADV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.LSD_REFINE_ADV);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_CORREL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_CORREL);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_CHISQR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_CHISQR);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_INTERSECT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_INTERSECT);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_BHATTACHARYYA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_BHATTACHARYYA);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_HELLINGER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_HELLINGER);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_CHISQR_ALT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_CHISQR_ALT);
        } else if(imageProcessReqeust.getProcess().equals("HISTCMP_KL_DIV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.HISTCMP_KL_DIV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_GRAY2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_GRAY2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_GRAY2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_GRAY2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_GRAY2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_GRAY2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_GRAY2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_GRAY2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2BGR565")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2BGR565);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2BGR565")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2BGR565);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5652BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5652BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5652RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5652RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2BGR565")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2BGR565);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2BGR565")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2BGR565);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5652BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5652BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5652RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5652RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_GRAY2BGR565")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_GRAY2BGR565);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5652GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5652GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2BGR555")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2BGR555);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2BGR555")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2BGR555);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5552BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5552BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5552RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5552RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2BGR555")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2BGR555);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2BGR555")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2BGR555);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5552BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5552BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5552RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5552RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_GRAY2BGR555")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_GRAY2BGR555);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR5552GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR5552GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2XYZ")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2XYZ);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2XYZ")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2XYZ);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_XYZ2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_XYZ2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_XYZ2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_XYZ2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2YCrCb")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2YCrCb);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2YCrCb")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2YCrCb);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YCrCb2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YCrCb2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YCrCb2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YCrCb2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2HSV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2HSV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2HSV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2HSV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2Lab")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2Lab);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2Lab")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2Lab);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2Luv")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2Luv);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2Luv")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2Luv);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2HLS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2HLS);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2HLS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2HLS);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HSV2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HSV2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HSV2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HSV2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Lab2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Lab2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Lab2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Lab2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Luv2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Luv2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Luv2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Luv2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HLS2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HLS2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HLS2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HLS2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2HSV_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2HSV_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2HSV_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2HSV_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2HLS_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2HLS_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2HLS_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2HLS_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HSV2BGR_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HSV2BGR_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HSV2RGB_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HSV2RGB_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HLS2BGR_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HLS2BGR_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_HLS2RGB_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_HLS2RGB_FULL);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_LBGR2Lab")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_LBGR2Lab);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_LRGB2Lab")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_LRGB2Lab);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_LBGR2Luv")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_LBGR2Luv);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_LRGB2Luv")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_LRGB2Luv);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Lab2LBGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Lab2LBGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Lab2LRGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Lab2LRGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Luv2LBGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Luv2LBGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_Luv2LRGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_Luv2LRGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2YUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2YUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2YUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2YUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_NV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_NV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_NV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_NV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_NV21")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_NV21);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_NV21")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_NV21);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420sp2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420sp2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420sp2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420sp2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_NV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_NV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_NV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_NV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_NV21")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_NV21);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_NV21")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_NV21);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420sp2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420sp2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420sp2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420sp2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420p2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420p2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420p2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420p2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420p2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420p2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420p2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420p2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_NV21")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_NV21);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_NV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_NV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420sp2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420sp2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV420p2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV420p2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_UYVY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_UYVY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_UYVY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_UYVY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_Y422")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_Y422);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_Y422")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_Y422);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_UYNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_UYNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_UYNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_UYNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_UYVY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_UYVY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_UYVY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_UYVY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_Y422")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_Y422);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_Y422")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_Y422);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_UYNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_UYNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_UYNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_UYNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_YUY2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_YUY2);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_YUY2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_YUY2);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_YVYU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_YVYU);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_YVYU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_YVYU);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_YUYV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_YUYV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_YUYV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_YUYV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGB_YUNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGB_YUNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGR_YUNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGR_YUNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_YUY2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_YUY2);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_YUY2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_YUY2);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_YVYU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_YVYU);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_YVYU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_YVYU);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_YUYV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_YUYV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_YUYV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_YUYV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2RGBA_YUNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2RGBA_YUNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2BGRA_YUNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2BGRA_YUNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_UYVY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_UYVY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_YUY2")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_YUY2);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_Y422")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_Y422);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_UYNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_UYNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_YVYU")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_YVYU);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_YUYV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_YUYV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_YUV2GRAY_YUNV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_YUV2GRAY_YUNV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2mRGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2mRGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_mRGBA2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_mRGBA2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2YUV_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2YUV_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2YUV_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2YUV_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2YUV_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2YUV_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2YUV_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2YUV_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2YUV_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2YUV_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2YUV_I420")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2YUV_I420);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2YUV_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2YUV_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2YUV_IYUV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2YUV_IYUV);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGB2YUV_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGB2YUV_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGR2YUV_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGR2YUV_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_RGBA2YUV_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_RGBA2YUV_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BGRA2YUV_YV12")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BGRA2YUV_YV12);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2BGR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2BGR);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2RGB")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2RGB);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2GRAY")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2GRAY);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2BGR_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2BGR_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2BGR_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2BGR_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2BGR_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2BGR_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2BGR_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2BGR_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2RGB_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2RGB_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2RGB_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2RGB_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2RGB_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2RGB_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2RGB_VNG")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2RGB_VNG);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2BGR_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2BGR_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2BGR_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2BGR_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2BGR_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2BGR_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2BGR_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2BGR_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2RGB_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2RGB_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2RGB_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2RGB_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2RGB_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2RGB_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2RGB_EA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2RGB_EA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2BGRA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2BGRA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerBG2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerBG2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGB2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGB2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerRG2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerRG2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_BayerGR2RGBA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_BayerGR2RGBA);
        } else if(imageProcessReqeust.getProcess().equals("COLOR_COLORCVT_MAX")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLOR_COLORCVT_MAX);
        } else if(imageProcessReqeust.getProcess().equals("INTERSECT_NONE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTERSECT_NONE);
        } else if(imageProcessReqeust.getProcess().equals("INTERSECT_PARTIAL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTERSECT_PARTIAL);
        } else if(imageProcessReqeust.getProcess().equals("INTERSECT_FULL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.INTERSECT_FULL);
        } else if(imageProcessReqeust.getProcess().equals("TM_SQDIFF")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.TM_SQDIFF);
        } else if(imageProcessReqeust.getProcess().equals("TM_SQDIFF_NORMED")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.TM_SQDIFF_NORMED);
        } else if(imageProcessReqeust.getProcess().equals("TM_CCORR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.TM_CCORR);
        } else if(imageProcessReqeust.getProcess().equals("TM_CCORR_NORMED")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.TM_CCORR_NORMED);
        } else if(imageProcessReqeust.getProcess().equals("TM_CCOEFF")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.TM_CCOEFF);
        } else if(imageProcessReqeust.getProcess().equals("TM_CCOEFF_NORMED")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.TM_CCOEFF_NORMED);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_AUTUMN")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_AUTUMN);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_BONE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_BONE);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_JET")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_JET);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_WINTER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_WINTER);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_RAINBOW")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_RAINBOW);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_OCEAN")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_OCEAN);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_SUMMER")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_SUMMER);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_SPRING")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_SPRING);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_COOL")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_COOL);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_HSV")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_HSV);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_PINK")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_PINK);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_HOT")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_HOT);
        } else if(imageProcessReqeust.getProcess().equals("COLORMAP_PARULA")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.COLORMAP_PARULA);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_CROSS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_CROSS);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_TILTED_CROSS")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_TILTED_CROSS);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_STAR")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_STAR);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_DIAMOND")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_DIAMOND);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_SQUARE")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_SQUARE);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_TRIANGLE_UP")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_TRIANGLE_UP);
        } else if(imageProcessReqeust.getProcess().equals("MARKER_TRIANGLE_DOWN")) {
            Imgproc.threshold(original_image, original_image, imageProcessReqeust.getThresh(), imageProcessReqeust.getBackground(), Imgproc.MARKER_TRIANGLE_DOWN);
        }
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(PNG, original_image, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    public FileSocket imageCompare(ImageCompare imageCompare) throws IOException {
        // first convert the stream to mat image
        Mat oldImageMat = bufferedImageToMatV1(ImageIO.read(imageCompare.getOldImage().getInputStream()));
        Mat newImageMat = bufferedImageToMatV1(ImageIO.read(imageCompare.getNewImage().getInputStream()));
        // convert both image into gray scale

        return null;
    }

    public String readTextFromImage(BufferedImage img) throws Exception {
        logger.info("Process For Read Text From Image");
        return this.instance.doOCR(img);
    }

    // change IMREAD_GRAYSCALE
    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public static Mat bufferedImageToMatV1(BufferedImage bi) {
        Mat mat = new Mat();
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
}
