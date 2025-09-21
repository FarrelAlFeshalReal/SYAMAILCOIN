import java.math.BigInteger;
import java.util.Arrays;

public class SAI288 {
    private static final int[] SAI288_IV = {
        0x243F6A88, 0x85A308D3, 0x13198A2E, 0x03707344,
        0xA4093822, 0x299F31D0, 0x082EFA98, 0xEC4E6C89, 0x452821E6
    };

    public static BigInteger factorial(int n) {
        if (n < 0) return BigInteger.ZERO;
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) result = result.multiply(BigInteger.valueOf(i));
        return result;
    }

    public static double exponomialConstant(int i, double gamma, double R, double tau, double[] S_list, double phi) {
        double exp_factor = Math.pow(gamma, i / R);
        double ssum = 0;
        for (int j = 0; j <= Math.min(i, S_list.length - 1); j++)
            ssum += S_list[j] * Math.pow(phi, j);
        return exp_factor * tau * ssum;
    }

    public static double proofOfExponomial(int n, int r, int delta_n, int delta_r) {
        System.out.println("Calculating Delta Maths for Proof of Exponomial...");
        System.out.println("Parameters: n=" + n + ", r=" + r + ", delta_n=" + delta_n + ", delta_r=" + delta_r);
        try {
            if (n > 20 || delta_n > 20) {
                double term1 = n * Math.log(n) - n + 0.5 * Math.log(2 * Math.PI * n) -
                               (r * Math.log(r) - r + 0.5 * Math.log(2 * Math.PI * r)) -
                               ((n - r) * Math.log(n - r) - (n - r) + 0.5 * Math.log(2 * Math.PI * (n - r)));
                double term2 = delta_n * Math.log(delta_n) - delta_n + 0.5 * Math.log(2 * Math.PI * delta_n) -
                               (delta_r * Math.log(delta_r) - delta_r + 0.5 * Math.log(2 * Math.PI * delta_r)) -
                               ((delta_n - delta_r) * Math.log(delta_n - delta_r) - (delta_n - delta_r) + 0.5 * Math.log(2 * Math.PI * (delta_n - delta_r)));
                return Math.abs(Math.exp(term1) - Math.exp(term2));
            } else {
                double term1 = factorial(n).doubleValue() / (factorial(r).doubleValue() * factorial(n - r).doubleValue());
                double term2 = factorial(delta_n).doubleValue() / (factorial(delta_r).doubleValue() * factorial(delta_n - delta_r).doubleValue());
                return Math.abs(term1 - term2);
            }
        } catch (Exception e) {
            System.out.println("Delta Maths error: " + e.getMessage());
            return 0.0;
        }
    }

    public static byte[] sai288Hash(byte[] data) {
        byte[] paddedData = padTo576Bits(data);
        int[] S = Arrays.copyOf(SAI288_IV, SAI288_IV.length);
        double gamma = 1.05, R = 10.0, tau = 0.5, phi = 0.9;

        for (int blockStart = 0; blockStart < paddedData.length; blockStart += 72) {
            int[] M = new int[18];
            for (int i = 0; i < 18; i++) {
                int offset = blockStart + i * 4;
                M[i] = ((paddedData[offset] & 0xFF) << 24) | ((paddedData[offset + 1] & 0xFF) << 16) |
                       ((paddedData[offset + 2] & 0xFF) << 8) | (paddedData[offset + 3] & 0xFF);
            }

            for (int t = 0; t < 64; t++) {
                int f1 = (S[(t + 1) % 9] ^ M[t % 18]) + (int)(Math.pow(gamma, t / R) * tau) ^
                         Integer.rotateLeft(S[(t + 4) % 9], (int)(phi * t) % 32);
                int f2 = (S[(t + 5) % 9] + M[(int)(t * phi) % 18]) ^
                         Integer.rotateRight(S[(t + 7) % 9], t % 29);
                int T = S[t % 9];
                S[t % 9] = (f1 + f2 + T) & 0xFFFFFFFF;
            }

            for (int i = 0; i < 9; i++) S[i] = S[i] ^ M[i % 18];
        }

        byte[] hash = new byte[36];
        for (int i = 0; i < 9; i++) {
            hash[i * 4] = (byte)(S[i] >>> 24);
            hash[i * 4 + 1] = (byte)(S[i] >>> 16);
            hash[i * 4 + 2] = (byte)(S[i] >>> 8);
            hash[i * 4 + 3] = (byte)(S[i]);
        }
        return hash;
    }

    private static byte[] padTo576Bits(byte[] data) {
        int paddingLength = 72 - (data.length % 72);
        if (paddingLength == 72) paddingLength = 0;
        byte[] padded = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, padded, 0, data.length);
        if (paddingLength > 0) padded[data.length] = (byte) 0x80;
        return padded;
    }
}
