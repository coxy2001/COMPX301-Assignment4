import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class RetinalMatch {
    public static void main(String[] args) {
        test_main(args);
    }

    public static void release_main(String[] args) {
        if (args.length != 2) {
            System.err.println("Needs two arguments");
            return;
        }

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread(args[0]);
        Mat image2 = Imgcodecs.imread(args[1]);

        image1 = imagePipeline(image1);
        image2 = imagePipeline(image2);

        System.out.println(imageCompare(image1, image2));
    }

    public static void test_main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread("RIDB/IM000001_2.JPG");
        Imgcodecs.imwrite("out.jpg", imagePipeline(image1));

        System.out.println(0);
    }

    // Return binary image showing the retina veins 
    public static Mat imagePipeline(Mat image) {
        // Use green colour channel
        ArrayList<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        image = channels.get(1);

        // Resize image
        int newSize = 2;
        Imgproc.resize(image, image, new Size(image.width() / newSize, image.height() / newSize));

        // Blur
        Imgproc.GaussianBlur(image, image, new Size(7, 7), 5);

        // Adaptive Threshold
        Imgproc.adaptiveThreshold(image, image, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 33, 2);

        // Remove border
        Imgproc.floodFill(image, new Mat(), new Point(240, 25), new Scalar(255));

        // Create kernel
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));

        // Opening and Closing
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, kernel);

        return image;
    }

    // Return location of bright spot
    public static Point locationBrightSpot(Mat image) {
        // TODO: Convert to black and white, threshold brightspot, erode image, get centre of spot
        return new Point(0, 0);
    }

    // Return location of dark spot
    public static Point locationDarkSpot(Mat image) {
        // TODO: Convert to black and white, threshold darkspot, erode image, get centre of spot
        return new Point(0, 0);
    }

    // Return similarity of images
    public static double imageCompare(Mat image1, Mat image2) {
        // TODO: Find technique to compare two images
        return 0;
    }
}
