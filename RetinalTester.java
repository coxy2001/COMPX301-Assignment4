public class RetinalTester {
    // static int MAX_PEOPLE = 20;
    static int MAX_PEOPLE = 5;
    static int MAX_IMAGE = 5;

    public static void main(String[] args) {
        for (int i1 = 1; i1 <= MAX_IMAGE; i1++) {
            for (int p1 = 1; p1 <= MAX_PEOPLE; p1++) {
                String image1 = "RIDB/IM00000" + i1 + "_" + p1 + ".jpg";
                for (int i2 = i1; i2 <= MAX_IMAGE; i2++) {
                    for (int p2 = p1; p2 <= MAX_PEOPLE; p2++) {
                        String image2 = "RIDB/IM00000" + i2 + "_" + p2 + ".jpg";

                        String[] arg = new String[] { image1, image2 };
                        int result = RetinalMatch.release_main(arg);
                        int expected = 0;

                        if (p1 == p2)
                            expected = 1;

                        System.out.println("Testing: " + i1 + "_" + p1 + " vs " + i2 + "_" + p2);
                        if (result == expected)
                            System.out.println("Correct");
                        else
                            System.out.println("Incorrect!! Expected: " + expected + " Got: " + result);
                    }
                }
            }
        }
    }
}
