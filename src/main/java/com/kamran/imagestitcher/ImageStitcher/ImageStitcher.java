package com.kamran.imagestitcher.ImageStitcher;

import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_stitching;

import java.io.File;
import java.util.Date;

/**
 * This class will stitch two images using some functions from JavaCv library
 *
 * It is compulsory that both images and output folder exists and their file path is given to the
 * object using {@link #setOutPutFolder(String)} {@list #setImageOne} and {@link #setImageOne(String)}
 * functions before calling {@link #execute()}
 */

public class ImageStitcher {
    private final String TAG = "ImageStitcher";
    private String imageOnePath, imageTwoPath, outPutPath;
    private ImageStitcherInterface isInterface;

    /**
     * Constructor,
     * @param imageStitcherInterface is instance of {@see ImageStitcherInterface} for callback functions
     */
    public ImageStitcher(ImageStitcherInterface imageStitcherInterface) {
        isInterface = imageStitcherInterface;
    }

    /**
     * set filepath of first image
     * @param path
     * @return for cascading
     */
    public ImageStitcher setImageOne(String path) {
        imageOnePath = path;
        return this;
    }

    /**
     * set filepath of second image
     * @param path
     * @return
     */
    public ImageStitcher setImageTwo(String path) {
        imageTwoPath = path;
        return this;
    }

    /**
     * set file path of out put folder,
     * this is the folder in which stitched image of imageOne and imageTwo will be saved
     * @param path
     * @return
     */
    public ImageStitcher setOutPutFolder(String path) {
        outPutPath = path;
        return this;
    }

    /**
     * Check if images and output folder are already created
     * call {@link #stitchImages()} if both images and output folder is available
     * call {@see ImageStitcherInterface#onError()} other wise with error message in {@see Exception}
     * object
     */
    public void execute() {
        try {
            if (!new File(imageOnePath).exists()) {
                isInterface.onError(new Exception("Image One file not found"));
            } else if (!new File(imageTwoPath).exists()) {
                isInterface.onError(new Exception("Image Two file not found"));
            } else if (!new File(outPutPath).exists() || !new File(outPutPath).isDirectory()) {
                isInterface.onError(new Exception("Output folder not found"));
            } else {
                stitchImages();
            }
        } catch (Exception e) {
            isInterface.onError(e);
        }

    }

    /**
     * This function will stitch both images and save the result in folder with path given to
     * {@link #setOutPutFolder(String)}
     */
    private void stitchImages() {
        opencv_core.MatVector imgs = new opencv_core.MatVector();

        opencv_core.Mat mat1 = opencv_imgcodecs.imread(imageOnePath);
        opencv_core.Mat mat2 = opencv_imgcodecs.imread(imageTwoPath);

        opencv_core.Mat pano = new opencv_core.Mat();
        opencv_stitching.Stitcher stitcher = opencv_stitching.Stitcher.createDefault(false);

        imgs.put(mat1, mat2);
        int status = stitcher.stitch(imgs, pano);

        if (status != opencv_stitching.Stitcher.OK) {
            Log.e(TAG, "Can't stitch images, error code = " + status);
            if (status == 1) {
                Exception exception = new Exception("Common area in image in not enough");
                isInterface.onError(exception);
            } else {
                Exception exception = new Exception("Unknown error status " + status);
                isInterface.onError(exception);
            }
        } else {
            opencv_imgcodecs.imwrite(outPutPath + "/stitched" + new Date().getTime() + ".jpg", pano);
            isInterface.onComplete();
        }
    }
}
