public class BigIntegerTest {
    static Random rnd = new Random();
    static int size = 1000; 
    static boolean failure = false;
    private static int order1 = 100;
    private static int order2 = 60;
    private static int order3 = 30;
    public static void pow() {
        int failCount1 = 0;
        for (int i=0; i<size; i++) {
            int power = rnd.nextInt(6) +2;
            BigInteger x = fetchNumber(order1);
            BigInteger y = x.pow(power);
            BigInteger z = x;
            for (int j=1; j<power; j++)
                z = z.multiply(x);
            if (!y.equals(z))
                failCount1++;
        }
        report("pow", failCount1);
    }
    public static void arithmetic() {
        int failCount = 0;
        for (int i=0; i<size; i++) {
            BigInteger x = fetchNumber(order1);
            while(x.compareTo(BigInteger.ZERO) != 1)
                x = fetchNumber(order1);
            BigInteger y = fetchNumber(order1/2);
            while(x.compareTo(y) == -1)
                y = fetchNumber(order1/2);
            if (y.equals(BigInteger.ZERO))
                y = y.add(BigInteger.ONE);
            BigInteger baz = x.divide(y);
            baz = baz.multiply(y);
            baz = baz.add(x.remainder(y));
            baz = baz.subtract(x);
            if (!baz.equals(BigInteger.ZERO))
                failCount++;
        }
        report("Arithmetic I", failCount);
        failCount = 0;
        for (int i=0; i<100; i++) {
            BigInteger x = fetchNumber(order1);
            while(x.compareTo(BigInteger.ZERO) != 1)
                x = fetchNumber(order1);
            BigInteger y = fetchNumber(order1/2);
            while(x.compareTo(y) == -1)
                y = fetchNumber(order1/2);
            if (y.equals(BigInteger.ZERO))
                y = y.add(BigInteger.ONE);
            BigInteger baz[] = x.divideAndRemainder(y);
            baz[0] = baz[0].multiply(y);
            baz[0] = baz[0].add(baz[1]);
            baz[0] = baz[0].subtract(x);
            if (!baz[0].equals(BigInteger.ZERO))
                failCount++;
        }
        report("Arithmetic II", failCount);
    }
    public static void bitCount() {
        int failCount = 0;
        for (int i=0; i<size*10; i++) {
            int x = rnd.nextInt();
            BigInteger bigX = BigInteger.valueOf((long)x);
            int bit = (x < 0 ? 0 : 1);
            int tmp = x, bitCount = 0;
            for (int j=0; j<32; j++) {
                bitCount += ((tmp & 1) == bit ? 1 : 0);
                tmp >>= 1;
            }
            if (bigX.bitCount() != bitCount) {
                failCount++;
            }
        }
        report("Bit Count", failCount);
    }
    public static void bitLength() {
        int failCount = 0;
        for (int i=0; i<size*10; i++) {
            int x = rnd.nextInt();
            BigInteger bigX = BigInteger.valueOf((long)x);
            int signBit = (x < 0 ? 0x80000000 : 0);
            int tmp = x, bitLength, j;
            for (j=0; j<32 && (tmp & 0x80000000)==signBit; j++)
                tmp <<= 1;
            bitLength = 32 - j;
            if (bigX.bitLength() != bitLength) {
                failCount++;
            }
        }
        report("BitLength", failCount);
    }
    public static void bitOps() {
        int failCount1 = 0, failCount2 = 0, failCount3 = 0;
        for (int i=0; i<size*5; i++) {
            BigInteger x = fetchNumber(order1);
            BigInteger y;
            if (x.signum() < 0) {
                y = BigInteger.valueOf(-1);
                for (int j=0; j<x.bitLength(); j++)
                    if (!x.testBit(j))
                        y = y.clearBit(j);
            } else {
                y = BigInteger.ZERO;
                for (int j=0; j<x.bitLength(); j++)
                    if (x.testBit(j))
                        y = y.setBit(j);
            }
            if (!x.equals(y))
                failCount1++;
            y = BigInteger.valueOf(x.signum()<0 ? -1 : 0);
            for (int j=0; j<x.bitLength(); j++)
                if (x.signum()<0  ^  x.testBit(j))
                    y = y.flipBit(j);
            if (!x.equals(y))
                failCount2++;
        }
        report("clearBit/testBit", failCount1);
        report("flipBit/testBit", failCount2);
        for (int i=0; i<size*5; i++) {
            BigInteger x = fetchNumber(order1);
            int k = x.getLowestSetBit();
            if (x.signum() == 0) {
                if (k != -1)
                    failCount3++;
            } else {
                BigInteger z = x.and(x.negate());
                int j;
                for (j=0; j<z.bitLength() && !z.testBit(j); j++)
                    ;
                if (k != j)
                    failCount3++;
            }
        }
        report("getLowestSetBit", failCount3);
    }
    public static void bitwise() {
        int failCount = 0;
        for (int i=0; i<size; i++) {
            BigInteger x = fetchNumber(order1);
            BigInteger y = fetchNumber(order1);
            BigInteger z = x.xor(y);
            BigInteger w = x.or(y).andNot(x.and(y));
            if (!z.equals(w))
                failCount++;
        }
        report("Logic (^ | & ~)", failCount);
        failCount = 0;
        for (int i=0; i<size; i++) {
            BigInteger x = fetchNumber(order1);
            BigInteger y = fetchNumber(order1);
            BigInteger z = x.andNot(y);
            BigInteger w = x.not().or(y).not();
            if (!z.equals(w))
                failCount++;
        }
        report("Logic (&~ | ~)", failCount);
    }
    public static void shift() {
        int failCount1 = 0;
        int failCount2 = 0;
        int failCount3 = 0;
        for (int i=0; i<100; i++) {
            BigInteger x = fetchNumber(order1);
            int n = Math.abs(rnd.nextInt()%200);
            if (!x.shiftLeft(n).equals
                (x.multiply(BigInteger.valueOf(2L).pow(n))))
                failCount1++;
            BigInteger y[] =x.divideAndRemainder(BigInteger.valueOf(2L).pow(n));
            BigInteger z = (x.signum()<0 && y[1].signum()!=0
                            ? y[0].subtract(BigInteger.ONE)
                            : y[0]);
            BigInteger b = x.shiftRight(n);
            if (!b.equals(z)) {
                System.err.println("Input is "+x.toString(2));
                System.err.println("shift is "+n);
                System.err.println("Divided "+z.toString(2));
                System.err.println("Shifted is "+b.toString(2));
                if (b.toString().equals(z.toString()))
                    System.err.println("Houston, we have a problem.");
                failCount2++;
            }
            if (!x.shiftLeft(n).shiftRight(n).equals(x))
                failCount3++;
        }
        report("baz shiftLeft", failCount1);
        report("baz shiftRight", failCount2);
        report("baz shiftLeft/Right", failCount3);
    }
    public static void divideAndRemainder() {
        int failCount1 = 0;
        for (int i=0; i<size; i++) {
            BigInteger x = fetchNumber(order1).abs();
            while(x.compareTo(BigInteger.valueOf(3L)) != 1)
                x = fetchNumber(order1).abs();
            BigInteger z = x.divide(BigInteger.valueOf(2L));
            BigInteger y[] = x.divideAndRemainder(x);
            if (!y[0].equals(BigInteger.ONE)) {
                failCount1++;
                System.err.println("fail1 x :"+x);
                System.err.println("      y :"+y);
            }
            else if (!y[1].equals(BigInteger.ZERO)) {
                failCount1++;
                System.err.println("fail2 x :"+x);
                System.err.println("      y :"+y);
            }
            y = x.divideAndRemainder(z);
            if (!y[0].equals(BigInteger.valueOf(2))) {
                failCount1++;
                System.err.println("fail3 x :"+x);
                System.err.println("      y :"+y);
            }
        }
        report("divideAndRemainder I", failCount1);
    }
    public static void stringConv() {
        int failCount = 0;
        for (int i=0; i<100; i++) {
            byte xBytes[] = new byte[Math.abs(rnd.nextInt())%100+1];
            rnd.nextBytes(xBytes);
            BigInteger x = new BigInteger(xBytes);
            for (int radix=2; radix < 37; radix++) {
                String result = x.toString(radix);
                BigInteger test = new BigInteger(result, radix);
                if (!test.equals(x)) {
                    failCount++;
                    System.err.println("BigInteger toString: "+x);
                    System.err.println("Test: "+test);
                    System.err.println(radix);
                }
            }
        }
        report("String Conversion", failCount);
    }
    public static void byteArrayConv() {
        int failCount = 0;
        for (int i=0; i<size; i++) {
            BigInteger x = fetchNumber(order1);
            while (x.equals(BigInteger.ZERO))
                x = fetchNumber(order1);
            BigInteger y = new BigInteger(x.toByteArray());
            if (!x.equals(y)) {
                failCount++;
                System.err.println("orig is "+x);
                System.err.println("new is "+y);
            }
        }
        report("Array Conversion", failCount);
    }
    public static void modInv() {
        int failCount = 0, successCount = 0, nonInvCount = 0;
        for (int i=0; i<size; i++) {
            BigInteger x = fetchNumber(order1);
            while(x.equals(BigInteger.ZERO))
                x = fetchNumber(order1);
            BigInteger m = fetchNumber(order1).abs();
            while(m.compareTo(BigInteger.ONE) != 1)
                m = fetchNumber(order1).abs();
            try {
                BigInteger inv = x.modInverse(m);
                BigInteger prod = inv.multiply(x).remainder(m);
                if (prod.signum() == -1)
                    prod = prod.add(m);
                if (prod.equals(BigInteger.ONE))
                    successCount++;
                else
                    failCount++;
            } catch(ArithmeticException e) {
                nonInvCount++;
            }
        }
        report("Modular Inverse", failCount);
    }
    public static void modExp() {
        int failCount = 0;
        for (int i=0; i<size/10; i++) {
            BigInteger m = fetchNumber(order1).abs();
            while(m.compareTo(BigInteger.ONE) != 1)
                m = fetchNumber(order1).abs();
            BigInteger base = fetchNumber(order2);
            BigInteger exp = fetchNumber(8).abs();
            BigInteger z = base.modPow(exp, m);
            BigInteger w = base.pow(exp.intValue()).mod(m);
            if (!z.equals(w)) {
                System.err.println("z is "+z);
                System.err.println("w is "+w);
                System.err.println("mod is "+m);
                System.err.println("base is "+base);
                System.err.println("exp is "+exp);
                failCount++;
            }
        }
        report("Exponentiation I", failCount);
    }
    public static void modExp2() {
        int failCount = 0;
        for (int i=0; i<10; i++) {
            BigInteger m = new BigInteger(100, 5, rnd);
            while(m.compareTo(BigInteger.ONE) != 1)
                m = new BigInteger(100, 5, rnd);
            BigInteger exp = m.subtract(BigInteger.ONE);
            BigInteger base = fetchNumber(order1).abs();
            while(base.compareTo(m) != -1)
                base = fetchNumber(order1).abs();
            while(base.equals(BigInteger.ZERO))
                base = fetchNumber(order1).abs();
            BigInteger one = base.modPow(exp, m);
            if (!one.equals(BigInteger.ONE)) {
                System.err.println("m is "+m);
                System.err.println("base is "+base);
                System.err.println("exp is "+exp);
                failCount++;
            }
        }
        report("Exponentiation II", failCount);
    }
    private static final int[] mersenne_powers = {
        521, 607, 1279, 2203, 2281, 3217, 4253, 4423, 9689, 9941, 11213, 19937,
        21701, 23209, 44497, 86243, 110503, 132049, 216091, 756839, 859433,
        1257787, 1398269, 2976221, 3021377, 6972593, 13466917 };
    private static final long[] carmichaels = {
      561,1105,1729,2465,2821,6601,8911,10585,15841,29341,41041,46657,52633,
      62745,63973,75361,101101,115921,126217,162401,172081,188461,252601,
      278545,294409,314821,334153,340561,399001,410041,449065,488881,512461,
      225593397919L };
    private static final int NUM_MERSENNES_TO_TEST = 7;
    private static final int NUM_CARMICHAELS_TO_TEST = 5;
    private static final String[] customer_primes = {
        "120000000000000000000000000000000019",
        "633825300114114700748351603131",
        "1461501637330902918203684832716283019651637554291",
        "779626057591079617852292862756047675913380626199",
        "857591696176672809403750477631580323575362410491",
        "910409242326391377348778281801166102059139832131",
        "929857869954035706722619989283358182285540127919",
        "961301750640481375785983980066592002055764391999",
        "1267617700951005189537696547196156120148404630231",
        "1326015641149969955786344600146607663033642528339" };
    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger SIX = new BigInteger("6");
    private static final BigInteger TWELVE = new BigInteger("12");
    private static final BigInteger EIGHTEEN = new BigInteger("18");
    public static void prime() {
        BigInteger p1, p2, c1;
        int failCount = 0;
        for(int i=0; i<10; i++) {
            p1 = BigInteger.probablePrime(100, rnd);
            if (!p1.isProbablePrime(100)) {
                System.err.println("Consistency "+p1.toString(16));
                failCount++;
            }
        }
        for (int i=0; i<NUM_MERSENNES_TO_TEST; i++) {
            p1 = new BigInteger("2");
            p1 = p1.pow(mersenne_powers[i]);
            p1 = p1.subtract(BigInteger.ONE);
            if (!p1.isProbablePrime(100)) {
                System.err.println("Mersenne prime "+i+ " failed.");
                failCount++;
            }
        }
        for (int i=0; i<customer_primes.length; i++) {
            p1 = new BigInteger(customer_primes[i]);
            if (!p1.isProbablePrime(100)) {
                System.err.println("Customer prime "+i+ " failed.");
                failCount++;
            }
        }
        for (int i=0; i<carmichaels.length; i++) {
            c1 = BigInteger.valueOf(carmichaels[i]);
            if(c1.isProbablePrime(100)) {
                System.err.println("Carmichael "+i+ " reported as prime.");
                failCount++;
            }
        }
        int found = 0;
        BigInteger f1 = new BigInteger(40, 100, rnd);
        while (found < NUM_CARMICHAELS_TO_TEST) {
            BigInteger k = null;
            BigInteger f2, f3;
            f1 = f1.nextProbablePrime();
            BigInteger[] result = f1.subtract(ONE).divideAndRemainder(SIX);
            if (result[1].equals(ZERO)) {
                k = result[0];
                f2 = k.multiply(TWELVE).add(ONE);
                if (f2.isProbablePrime(100)) {
                    f3 = k.multiply(EIGHTEEN).add(ONE);
                    if (f3.isProbablePrime(100)) {
                        c1 = f1.multiply(f2).multiply(f3);
                        if (c1.isProbablePrime(100)) {
                            System.err.println("Computed Carmichael "
                                               +c1.toString(16));
                            failCount++;
                        }
                        found++;
                    }
                }
            }
            f1 = f1.add(TWO);
        }
        for (int i=0; i<50; i++) {
            p1 = BigInteger.probablePrime(100, rnd);
            p2 = BigInteger.probablePrime(100, rnd);
            c1 = p1.multiply(p2);
            if (c1.isProbablePrime(100)) {
                System.err.println("Composite failed "+c1.toString(16));
                failCount++;
            }
        }
        for (int i=0; i<4; i++) {
            p1 = BigInteger.probablePrime(600, rnd);
            p2 = BigInteger.probablePrime(600, rnd);
            c1 = p1.multiply(p2);
            if (c1.isProbablePrime(100)) {
                System.err.println("Composite failed "+c1.toString(16));
                failCount++;
            }
        }
        report("Prime", failCount);
    }
    private static final long[] primesTo100 = {
        2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97
    };
    private static final long[] aPrimeSequence = {
        1999999003L, 1999999013L, 1999999049L, 1999999061L, 1999999081L,
        1999999087L, 1999999093L, 1999999097L, 1999999117L, 1999999121L,
        1999999151L, 1999999171L, 1999999207L, 1999999219L, 1999999271L,
        1999999321L, 1999999373L, 1999999423L, 1999999439L, 1999999499L,
        1999999553L, 1999999559L, 1999999571L, 1999999609L, 1999999613L,
        1999999621L, 1999999643L, 1999999649L, 1999999657L, 1999999747L,
        1999999763L, 1999999777L, 1999999811L, 1999999817L, 1999999829L,
        1999999853L, 1999999861L, 1999999871L, 1999999873
    };
    public static void nextProbablePrime() throws Exception {
        int failCount = 0;
        BigInteger p1, p2, p3;
        p1 = p2 = p3 = ZERO;
        for (int i=0; i<primesTo100.length; i++) {
            p1 = p1.nextProbablePrime();
            if (p1.longValue() != primesTo100[i]) {
                System.err.println("low range primes failed");
                System.err.println("p1 is "+p1);
                System.err.println("expected "+primesTo100[i]);
                failCount++;
            }
        }
        p1 = BigInteger.valueOf(aPrimeSequence[0]);
        for (int i=1; i<aPrimeSequence.length; i++) {
            p1 = p1.nextProbablePrime();
            if (p1.longValue() != aPrimeSequence[i]) {
                System.err.println("prime sequence failed");
                failCount++;
            }
        }
        for (int i=0; i<100; i+=10) {
            p1 = BigInteger.probablePrime(50 + i, rnd);
            p2 = p1.add(ONE);
            p3 = p1.nextProbablePrime();
            while(p2.compareTo(p3) < 0) {
                if (p2.isProbablePrime(100)){
                    System.err.println("nextProbablePrime failed");
                    System.err.println("along range "+p1.toString(16));
                    System.err.println("to "+p3.toString(16));
                    failCount++;
                    break;
                }
                p2 = p2.add(ONE);
            }
        }
        report("nextProbablePrime", failCount);
    }
    public static void serialize() throws Exception {
        int failCount = 0;
        String bitPatterns[] = {
             "ffffffff00000000ffffffff00000000ffffffff00000000",
             "ffffffffffffffffffffffff000000000000000000000000",
             "ffffffff0000000000000000000000000000000000000000",
             "10000000ffffffffffffffffffffffffffffffffffffffff",
             "100000000000000000000000000000000000000000000000",
             "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "-ffffffff00000000ffffffff00000000ffffffff00000000",
            "-ffffffffffffffffffffffff000000000000000000000000",
            "-ffffffff0000000000000000000000000000000000000000",
            "-10000000ffffffffffffffffffffffffffffffffffffffff",
            "-100000000000000000000000000000000000000000000000",
            "-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        };
        for(int i = 0; i < bitPatterns.length; i++) {
            BigInteger b1 = new BigInteger(bitPatterns[i], 16);
            BigInteger b2 = null;
            File f = new File("serialtest");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(b1);
                    oos.flush();
                }
                try (FileInputStream fis = new FileInputStream(f);
                     ObjectInputStream ois = new ObjectInputStream(fis))
                {
                    b2 = (BigInteger)ois.readObject();
                }
                if (!b1.equals(b2) ||
                    !b1.equals(b1.or(b2))) {
                    failCount++;
                    System.err.println("Serialized failed for hex " +
                                       b1.toString(16));
                }
            }
            f.delete();
        }
        for(int i=0; i<10; i++) {
            BigInteger b1 = fetchNumber(rnd.nextInt(100));
            BigInteger b2 = null;
            File f = new File("serialtest");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(b1);
                    oos.flush();
                }
                try (FileInputStream fis = new FileInputStream(f);
                     ObjectInputStream ois = new ObjectInputStream(fis))
                {
                    b2 = (BigInteger)ois.readObject();
                }
            }
            if (!b1.equals(b2) ||
                !b1.equals(b1.or(b2)))
                failCount++;
            f.delete();
        }
        report("Serialize", failCount);
    }
    public static void main(String[] args) throws Exception {
        if (args.length >0)
            order1 = (int)((Integer.parseInt(args[0]))* 3.333);
        if (args.length >1)
            order2 = (int)((Integer.parseInt(args[1]))* 3.333);
        if (args.length >2)
            order3 = (int)((Integer.parseInt(args[2]))* 3.333);
        prime();
        nextProbablePrime();
        arithmetic();
        divideAndRemainder();
        pow();
        bitCount();
        bitLength();
        bitOps();
        bitwise();
        shift();
        byteArrayConv();
        modInv();
        modExp();
        modExp2();
        stringConv();
        serialize();
        if (failure)
            throw new RuntimeException("Failure in BigIntegerTest.");
    }
    private static BigInteger fetchNumber(int order) {
        boolean negative = rnd.nextBoolean();
        int numType = rnd.nextInt(6);
        BigInteger result = null;
        if (order < 2) order = 2;
        switch (numType) {
            case 0: 
                result = BigInteger.ZERO;
                break;
            case 1: 
                result = BigInteger.ONE;
                break;
            case 2: 
                int numBytes = (order+7)/8;
                byte[] fullBits = new byte[numBytes];
                for(int i=0; i<numBytes; i++)
                    fullBits[i] = (byte)0xff;
                int excessBits = 8*numBytes - order;
                fullBits[0] &= (1 << (8-excessBits)) - 1;
                result = new BigInteger(1, fullBits);
                break;
            case 3: 
                result = BigInteger.ONE.shiftLeft(rnd.nextInt(order));
                break;
            case 4: 
                int iterations = rnd.nextInt(order-1);
                result = BigInteger.ONE.shiftLeft(rnd.nextInt(order));
                for(int i=0; i<iterations; i++) {
                    BigInteger temp = BigInteger.ONE.shiftLeft(
                                                rnd.nextInt(order));
                    result = result.or(temp);
                }
                break;
            default: 
                result = new BigInteger(order, rnd);
        }
        if (negative)
            result = result.negate();
        return result;
    }
    static void report(String testName, int failCount) {
        System.err.println(testName+": " +
                           (failCount==0 ? "Passed":"Failed("+failCount+")"));
        if (failCount > 0)
            failure = true;
    }
}
