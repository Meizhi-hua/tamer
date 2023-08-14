@TestTargetClass(targets.Charsets.IBM864.class)
@AndroidOnly("icu different from RI")
public class Charset_SingleByte_IBM864 extends Charset_SingleByteAbstractTest {
    protected void setUp() throws Exception {
        charsetName = "cp864"; 
        allChars = theseChars(new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 28, 27, 127, 29, 30, 31, 
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 
            64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 
            96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 
            112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 26, 
            176, 183, 8729, 8730, 9618, 9472, 9474, 9532, 9508, 9516, 9500, 9524, 9488, 9484, 9492, 9496, 
            946, 8734, 966, 177, 189, 188, 8776, 171, 187, 65271, 65272, 65533, 65533, 65275, 65276, 8203, 
            160, 173, 65154, 163, 164, 65156, 65533, 65533, 65166, 65167, 65173, 65177, 1548, 65181, 65185, 65189, 
            1632, 1633, 1634, 1635, 1636, 1637, 1638, 1639, 1640, 1641, 65233, 1563, 65201, 65205, 65209, 1567, 
            162, 65152, 65153, 65155, 65157, 65226, 65163, 65165, 65169, 65171, 65175, 65179, 65183, 65187, 65191, 65193, 
            65195, 65197, 65199, 65203, 65207, 65211, 65215, 65219, 65223, 65227, 65231, 166, 172, 247, 215, 65225, 
            1600, 65235, 65239, 65243, 65247, 65251, 65255, 65259, 65261, 65263, 65267, 65213, 65228, 65230, 65229, 65249, 
            65149, 65148, 65253, 65257, 65260, 65264, 65266, 65232, 65237, 65269, 65270, 65245, 65241, 65265, 9632, 65533});
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "functionalCoDec_REPR",
        args = {}
    )
    public static void test_Bytes_DifferentOnes_Android() throws CharacterCodingException {
        decodeReplace(
                theseBytes(new int[]{26, 28, 37, 127, 159, 215, 216, 241}),
                new char[] {28, 127, 37, 26, 8203, 65219, 65223, 65148} );
    }
}