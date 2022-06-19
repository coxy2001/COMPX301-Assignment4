public class RetinalTester {
    static int MAX_PEOPLE = 20;
    static int MAX_IMAGE = 5;

    public static void main(String[] args) {
        // retest(args);
        testAll(args);
    }

    public static void testAll(String[] args) {
        int incorrect = 0;
        int tests = 0;
        for (int i1 = 1; i1 <= MAX_IMAGE; i1++) {
            for (int p1 = 1; p1 <= MAX_PEOPLE; p1++) {
                String image1 = "RIDB/IM00000" + i1 + "_" + p1 + ".jpg";
                System.out.println("Testing: " + i1 + "_" + p1);
                int p2 = p1 + 1;
                for (int i2 = i1; i2 <= MAX_IMAGE; i2++) {
                    for (; p2 <= MAX_PEOPLE; p2++) {
                        String image2 = "RIDB/IM00000" + i2 + "_" + p2 + ".jpg";

                        String[] arg = new String[] { image1, image2 };
                        int result = RetinalMatch.compareImages(arg);
                        int expected = 0;

                        if (p1 == p2)
                            expected = 1;

                        if (result != expected) {
                            System.out.print("Testing: " + i1 + "_" + p1 + " vs " + i2 + "_" + p2 + " - ");
                            System.out.println("Incorrect!! Expected: " + expected + " Got: " + result);
                            incorrect++;
                        }
                        tests++;
                    }
                    p2 = 1;
                }
            }
        }
        System.out.println(incorrect);
        System.out.println(tests);
        System.out.println((double) incorrect / (double) tests);
        System.out.println(100 - ((double) incorrect / (double) tests));
    }

    public static void retest(String[] args) {
        int[][] retest = new int[][] {
                { 1, 1 },
                { 1, 7 },
                { 1, 8 },
                { 2, 1 },
                { 2, 3 },
                { 2, 7 },
                { 2, 15 },
                { 3, 7 },
                { 3, 8 },
                { 4, 1 },
                { 4, 7 },
                { 4, 12 },
                { 4, 14 },
                { 5, 1 },
                { 5, 3 },
                { 5, 7 },
                { 5, 8 }
        };

        for (int[] image : retest) {
            int i1 = image[0];
            int p1 = image[1];
            String image1 = "RIDB/IM00000" + i1 + "_" + p1 + ".jpg";
            System.out.println("Testing: " + i1 + "_" + p1);
            for (int i2 = 1; i2 <= MAX_IMAGE; i2++) {
                for (int p2 = 1; p2 <= MAX_PEOPLE; p2++) {
                    String image2 = "RIDB/IM00000" + i2 + "_" + p2 + ".jpg";

                    String[] arg = new String[] { image1, image2 };
                    int result = RetinalMatch.compareImages(arg);
                    int expected = 0;

                    if (p1 == p2)
                        expected = 1;

                    if (result != expected) {
                        System.out.print("Testing: " + i1 + "_" + p1 + " vs " + i2 + "_" + p2 + " - ");
                        System.out.println("Incorrect!! Expected: " + expected + " Got: " + result);
                    }
                }
            }
        }
    }
}
