import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.features2d.KAZE;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class RetinalMatch {
    public static void main(String[] args) {
        System.out.println(compareImagesTesting(args));
    }

    public static int compareImages(String[] args) {
        if (args.length != 2) {
            System.err.println("Needs two arguments");
            return -1;
        }

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread(args[0]);
        Mat image2 = Imgcodecs.imread(args[1]);

        double similarity = similarityVeins(image1, image2);
        double brightDiff = differenceBrightSpot(image1, image2);
        // double darkDiff = differenceDarkSpot(image1, image2);

        return isMatch(similarity, brightDiff, 0);
    }

    public static int compareImagesTesting(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image1 = Imgcodecs.imread("RIDB/IM000001_1.JPG");
        Mat image2 = Imgcodecs.imread("RIDB/IM000002_1.JPG");

        double similarity = similarityVeins(image1, image2);
        double brightDiff = differenceBrightSpot(image1, image2);
        // double darkDiff = differenceDarkSpot(image1, image2);

        System.out.println(similarity);
        System.out.println(brightDiff);
        // System.out.println(darkDiff);

        return isMatch(similarity, brightDiff, 0);
    }

    // Return binary image showing the retina veins 
    private static Mat maskVeins(Mat image) {
        // Use green colour channel
        ArrayList<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        image = channels.get(1);

        // Resize image
        int newSize = 2;
        Imgproc.resize(image, image, new Size(image.width() / newSize, image.height() / newSize));

        // Blur
        Imgproc.GaussianBlur(image, image, new Size(7, 7), 7);

        // Adaptive Threshold
        Imgproc.adaptiveThreshold(image, image, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 33, 2);

        // Remove border
        Imgproc.floodFill(image, new Mat(), new Point(240, 25), new Scalar(0));

        // Create kernel
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));

        // Opening and Closing
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel);

        return image;
    }

    // Return similarity of images
    public static double similarityVeins(Mat image1, Mat image2) {
        // Get keypoints from both images using SIFT
        // SIFT sift = SIFT.create();
        var sift = KAZE.create();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        MatOfKeyPoint keyPoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        sift.detectAndCompute(maskVeins(image1), new Mat(), keyPoints1, descriptors1, false);
        sift.detectAndCompute(maskVeins(image2), new Mat(), keyPoints2, descriptors2, false);

        // Set query to smallest set of descriptors and train to the largest set
        Mat query = descriptors1;
        Mat train = descriptors2;
        if (descriptors1.size().height > descriptors2.size().height) {
            query = descriptors2;
            train = descriptors1;
        }

        // Find keypoint matches
        ArrayList<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        BFMatcher.create().knnMatch(query, train, matches, 2);

        // Find good keypoint matches
        ArrayList<DMatch> goodMatches = new ArrayList<>();
        var matchesIter = matches.iterator();
        while (matchesIter.hasNext()) {
            List<DMatch> match = matchesIter.next().toList();
            DMatch m = match.get(0);
            DMatch n = match.get(1);
            if (m.distance < 0.75 * n.distance)
                goodMatches.add(m);
        }

        // Return similarity. Good matches divided total matches
        return (double) goodMatches.size() / (double) matches.size();
    }

    // Return location of bright spot
    private static Point locationBrightSpot(Mat image) {
        // Change image to grayscale and threshold out the bright spot
        Mat temp = new Mat();
        Imgproc.cvtColor(image, temp, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(temp, temp);
        Imgproc.threshold(temp, temp, 250, 255, 0);

        // Reduce the noise of small bright spots via erosion
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(40, 40));
        Imgproc.erode(temp, temp, kernel);

        // Locate the white pixels of the bright spot
        Mat wLocMat = Mat.zeros(temp.size(), temp.channels());
        Core.findNonZero(temp, wLocMat);

        // Return point in centre if the bright spot can't be found
        if (wLocMat.rows() == 0)
            return new Point(temp.width() / 2, temp.height() / 2);

        // Calculate the 'centre' of the bright spot
        int xSum = 0;
        int ySum = 0;
        for (int i = 0; i < wLocMat.rows(); i++) {
            for (int j = 0; j < 2; j++) {
                if (j == 0) {
                    xSum += (int) wLocMat.get(i, 0)[j];
                } else {
                    ySum += (int) wLocMat.get(i, 0)[j];
                }
            }
        }
        int xAvg = xSum / wLocMat.rows();
        int yAvg = ySum / wLocMat.rows();

        // Return centre of bright spot
        return new Point(xAvg, yAvg);
    }

    // Return location of dark spot
    private static Point locationDarkSpot(Mat image) {
        // Change the image to grayscale, then threshold out the dark spot, 
        // invert the colours to set dark spot to white
        Mat temp = new Mat();
        Imgproc.cvtColor(image, temp, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(temp, temp);
        Imgproc.blur(temp, temp, new Size(15, 15));
        Imgproc.threshold(temp, temp, 60, 255, 0);
        Imgproc.threshold(temp, temp, 30, 255, Imgproc.THRESH_BINARY_INV);

        Mat wLocMat = Mat.zeros(temp.size(), temp.channels());
        Core.findNonZero(temp, wLocMat);

        int xSum = 0;
        int xCount = 0;
        int ySum = 0;
        int yCount = 0;

        // Calculate the 'centre' of the dark spot using data from centre section of image
        for (int i = 0; i < wLocMat.rows(); i++) {
            if (wLocMat.get(i, 0)[0] > 600
                    && (int) wLocMat.get(i, 0)[0] < 800
                    && (int) wLocMat.get(i, 0)[1] > 350
                    && (int) wLocMat.get(i, 0)[1] < 650) {
                xSum += (int) wLocMat.get(i, 0)[0];
                xCount += 1;
                ySum += (int) wLocMat.get(i, 0)[1];
                yCount += 1;
            }
        }

        // If a dark spot is found, return the centre of the spot,
        // else return centre of image if dark spot not found 
        if (xCount > 0 && yCount > 0) {
            int xAvg = xSum / xCount;
            int yAvg = ySum / yCount;
            return new Point(xAvg, yAvg);
        } else {
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
        if (similarity > 0.115)
            return 1;
        if (similarity > 0.11 && brightDiff < 199)
            return 1;
        if (similarity > 0.105 && brightDiff < 99)
            return 1;
        return 0;
    }
}
