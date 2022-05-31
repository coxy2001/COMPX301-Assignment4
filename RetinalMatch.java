import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class RetinalMatch {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Needs two arguments");
            return;
        }

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread(args[0]);
        Mat dst1 = new Mat();
        Imgproc.cvtColor(image1, dst1, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(dst1, dst1);
        // Imgproc.threshold(dst1, dst1, 80, 255 ,0);
        Imgcodecs.imwrite("out.jpg", dst1);

        System.out.println("1");
        System.out.println("0");
    }
}