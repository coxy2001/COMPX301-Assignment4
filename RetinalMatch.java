import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class RetinalMatch {
    public static void main(String[] args) {
        System.out.println(test_main(args));
    }

    public static int release_main(String[] args) {
        if (args.length != 2) {
            System.err.println("Needs two arguments");
            return -1;
        }

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread(args[0]);
        Mat image2 = Imgcodecs.imread(args[1]);

        double similarity = imageCompare(image1, image2);
        double brightDiff = differenceBrightSpot(image1, image2);
        double darkDiff = differenceDarkSpot(image1, image2);

        return isMatch(similarity, brightDiff, darkDiff);
    }

    public static int test_main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread("RIDB/IM000001_1.JPG");
        Mat image2 = Imgcodecs.imread("RIDB/IM000001_5.JPG");

        double similarity = imageCompare(image1, image2);
        double brightDiff = differenceBrightSpot(image1, image2);
        double darkDiff = differenceDarkSpot(image1, image2);

        System.out.println(similarity);
        System.out.println(brightDiff);
        System.out.println(darkDiff);

        return isMatch(similarity, brightDiff, darkDiff);
    }

    // Return binary image showing the retina veins 
    private static Mat imagePipeline(Mat image) {
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

    // Return similarity of images
    public static double imageCompare(Mat image1, Mat image2) {
        SIFT sift = SIFT.create();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        sift.detectAndCompute(imagePipeline(image1), new Mat(), keyPoints1, descriptors1, false);
        sift.detectAndCompute(imagePipeline(image2), new Mat(), keyPoints2, descriptors2, false);

        ArrayList<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        BFMatcher.create().knnMatch(descriptors1, descriptors2, matches, 2);

        ArrayList<DMatch> goodMatches = new ArrayList<>();
        var matchesIter = matches.iterator();
        while (matchesIter.hasNext()) {
            List<DMatch> match = matchesIter.next().toList();
            DMatch m = match.get(0);
            DMatch n = match.get(1);
            if (m.distance < 0.75 * n.distance)
                goodMatches.add(m);

        }

        return (double) goodMatches.size() / (double) matches.size();
    }

    // Return location of bright spot
    public static Point locationBrightSpot(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(image, image);
        Imgproc.threshold(image, image, 250, 255 ,0);
        Imgproc.erode(image, image, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(40, 40)));
        Mat wLocMat = Mat.zeros(image.size(), image.channels()); 
        Core.findNonZero(image, wLocMat);
        int xSum = 0;
        int ySum = 0;

        for (int i=0; i<wLocMat.rows(); i++){
            for (int j=0; j<2; j++){
                if (j == 0){
                    xSum += (int)wLocMat.get(i, 0)[j];
                }
                else{
                    ySum += (int)wLocMat.get(i, 0)[j];
                }
            }
        }
        int xAvg = xSum / wLocMat.rows();
        int yAvg = ySum / wLocMat.rows();        
        return new Point(xAvg, yAvg);
    }

    // Return location of dark spot
    public static Point locationDarkSpot(Mat image) {
        // TODO: Convert to black and white, threshold darkspot, erode image, get centre of spot
        Mat temp = new Mat();
        Imgproc.cvtColor(image, temp, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(temp, temp);
        Imgproc.blur(temp, temp, new Size(15, 15));
        Imgproc.threshold(temp, temp, 60, 255 ,0);
        Imgproc.threshold(temp, temp, 30, 255, Imgproc.THRESH_BINARY_INV);
        Mat wLocMat = Mat.zeros(temp.size(), temp.channels()); 
        Core.findNonZero(temp, wLocMat);
        int xSum = 0;
        int xCount = 0;
        int ySum = 0;
        int yCount = 0;

        for (int i=0; i<wLocMat.rows(); i++){
            if ((int)wLocMat.get(i, 0)[0] > 600 
                    && (int)wLocMat.get(i, 0)[0] < 800
                    && (int)wLocMat.get(i, 0)[1] > 350 
                    && (int)wLocMat.get(i, 0)[1] < 650){
                        xSum += (int)wLocMat.get(i, 0)[0];
                        xCount += 1;
                        ySum += (int)wLocMat.get(i, 0)[1];
                        yCount += 1;
                    }
        }
        Imgcodecs.imwrite("out.jpg", temp);
        if (xCount > 0 && yCount > 0){
            int xAvg = xSum / xCount;
            int yAvg = ySum / yCount; 
            System.out.println(xAvg + " " + yAvg);
            return new Point(xAvg, yAvg);
        }
        else {
            System.out.println("No dark spot found");
            return new Point(image.width() / 2, image.height() / 2);
        }      
    }

    // Calculate distance between two points
    private static double calcDistance(Point p1, Point p2) {
        double dx = Math.pow(p1.x - p2.x, 2);
        double dy = Math.pow(p1.y - p2.y, 2);
        return Math.sqrt(dx + dy);
    }

    // Return distance between two bright spots 
    public static double differenceBrightSpot(Mat image1, Mat image2) {
        return calcDistance(locationBrightSpot(image1), locationBrightSpot(image2));
    }

    // Return distance between two dark spots
    public static double differenceDarkSpot(Mat image1, Mat image2) {
        return calcDistance(locationDarkSpot(image1), locationDarkSpot(image2));
    }

    // Determine match with image similarity, bright spot distance, and dark spot distance
    public static int isMatch(double similarity, double brightDiff, double darkDiff) {
        if (similarity > 0.10 && brightDiff < 99 && darkDiff < 99)
            return 1;
        return 0;
    }
}
