import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class RetinalMatch {
    public static void release_main(String[] args) {
        if (args.length != 2) {
            System.err.println("Needs two arguments");
            return;
        }

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread(args[0]);
        Imgcodecs.imwrite("out.jpg", imagePipeline(image1));
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread("RIDB/IM000001_2.JPG");
        Imgcodecs.imwrite("out.jpg", imagePipeline(image1));

        System.out.println("1");
        System.out.println("0");
    }

    public static Mat imagePipeline(Mat image) {
        // Create kernel
        Mat kernel = Mat.ones(3, 3, CvType.CV_8UC1);
        kernel.put(-1, -1, 0);
        kernel.put(-1, +1, 0);
        kernel.put(+1, -1, 0);
        kernel.put(+1, +1, 0);

        // Use green colour channel
        ArrayList<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        image = channels.get(1);

        // Resize image
        int newSize = 2;
        Imgproc.resize(image, image, new Size(image.width() / newSize, image.height() / newSize));

        // Adaptive Threshold
        Imgproc.adaptiveThreshold(image, image, 255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 3);

        // Opening and Closing
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, kernel);

        return image;
    }
}
