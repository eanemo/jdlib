package com.emaraic.jdlib;

import com.emaraic.utils.FaceDescriptor;
import com.emaraic.utils.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

//
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
/**
 *
 * @author Taha Emara 
 * Website: http://www.emaraic.com 
 * Email : taha@emaraic.com
 * Created on: Nov 21, 2020
 */
public class Jdlib {

    private String facialLandmarksModelPath;
    private String faceEmbeddingModelPath;

    public Jdlib(String facialLandmarksModelPath, String faceEmbeddingModelPath) {
        // This is done due to the Windows platform charater path separator
        this.facialLandmarksModelPath = facialLandmarksModelPath.replace('\\', '/');
        this.faceEmbeddingModelPath = faceEmbeddingModelPath.replace('\\', '/');
        loadLib();
    }

    public Jdlib(String facialLandmarksModelPath) {
        // This is done due to the Windows platform charater path separator
        this.facialLandmarksModelPath = facialLandmarksModelPath.replace('\\', '/');
        this.faceEmbeddingModelPath = null;
        loadLib();
    }

    private native long getFaceDectorHandler();

    private native long getShapePredictorHandler(String modelPath);

    private native long getFaceEmbeddingHandler(String modelPath);

    private native ArrayList<Rectangle> faceDetect(long faceDetectorHandler, byte[] pixels, int h, int w);

    private native ArrayList<FaceDescriptor> getFacialLandmarks(long shapePredictorHandler, long faceDetectorHandler, byte[] pixels, int h, int w);

    private native ArrayList<FaceDescriptor> getFacialLandmarksWholeImage(long shapePredictorHandler, long faceDetectorHandler, byte[] pixels, int h, int w);

    private native ArrayList<FaceDescriptor> getFaceEmbeddings(long FaceEmbeddingHandler, long shapePredictorHandler, long faceDetectorHandler, byte[] pixels, int h, int w);

    private void loadLib() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        String name = System.mapLibraryName("Jdlib");

        String libpath = "";
        if (os.contains("linux")) {
            libpath = "/native" + File.separator + "linux" + File.separator + name;
        } else if (os.contains("mac")) {
            libpath = "/native" + File.separator + "macosx" + File.separator + name;
        } else if (os.contains("windows")) {
            libpath = "/native" + "/windows" + "/" + name;
            System.out.println("Loading " + libpath + " ... ");
        } else {
            throw new java.lang.UnsupportedOperationException(os + " is not supported. Try to recompile Jdlib on your machine and then use it.");
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            //System.out.println("Loading library " + libpath);
            inputStream = Jdlib.class.getResourceAsStream(libpath);
            
            File fileOut = File.createTempFile("Jdlib", ".dll");
            
            // Copy file to tmp shared lib
            Files.copy(inputStream, fileOut.toPath(), StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();
            System.load(fileOut.toString());
            //System.out.println("Library loaded.");
        } catch (Exception e) {
            System.err.println("Error During Loading Lib: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("Error During Closing Input Stream!!");
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println("Error During Closing Output Stream!!");
                }
            }
        }
    }

    public ArrayList<Rectangle> detectFace(BufferedImage img) {
        Image image = new Image(img);
        ArrayList<Rectangle> data = faceDetect(getFaceDectorHandler(), image.pixels, image.height, image.width);
        if (data == null) {
            System.err.println("Jdlib | detectFace | Null data!!");
            data = new ArrayList<>(Collections.EMPTY_LIST);
        }
        return data;
    }

    public ArrayList<FaceDescriptor> getFaceLandmarks(BufferedImage img) {
        Image image = new Image(img);
        ArrayList<FaceDescriptor> data = getFacialLandmarks(getShapePredictorHandler(facialLandmarksModelPath), getFaceDectorHandler(), image.pixels, image.height, image.width);
        if (data == null) {
            System.err.println("Jdlib | getFaceLandmarks | Null data!!");
            data = new ArrayList<>(Collections.EMPTY_LIST);
        }
        return data;
    }

    public ArrayList<FaceDescriptor> getFaceLandmarksWholeImage(BufferedImage img) {
        Image image = new Image(img);
        ArrayList<FaceDescriptor> data = getFacialLandmarksWholeImage(getShapePredictorHandler(facialLandmarksModelPath), getFaceDectorHandler(), image.pixels, image.height, image.width);
        if (data == null) {
            System.err.println("Jdlib | getFaceLandmarks | Null data!!");
            data = new ArrayList<>(Collections.EMPTY_LIST);
        }
        return data;
    }

    public ArrayList<FaceDescriptor> getFaceEmbeddings(BufferedImage img) {
        if (facialLandmarksModelPath == null) {
            throw new IllegalArgumentException("Path to face embedding model isn't provided!");
        }
        
        Image image = new Image(img);
        ArrayList<FaceDescriptor> data = getFaceEmbeddings(getFaceEmbeddingHandler(faceEmbeddingModelPath), getShapePredictorHandler(facialLandmarksModelPath), getFaceDectorHandler(), image.pixels, image.height, image.width);
        if (data == null) {
            System.err.println("Jdlib | getFaceEmbeddings | Null data!!");
            data = new ArrayList<>(Collections.EMPTY_LIST);
        }
        return data;
    }
}
