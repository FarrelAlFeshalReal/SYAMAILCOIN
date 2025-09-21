import java.math.BigInteger;
import java.security.SecureRandom;

public class MLDSA_Syamailcoin {
    public static final BigInteger Q = new BigInteger("251");
    public static final BigInteger P = new BigInteger("1009");
    public static final BigInteger G = new BigInteger("2");
    public static final int K = 1;
    private static final SecureRandom random = new SecureRandom();

    public static class Signature {
        public BigInteger r;
        public BigInteger s;
        public Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }
        public String toString() {
            return r.toString() + ":" + s.toString();
        }
    }

    public static Signature sign(BigInteger privateKey, BigInteger message) {
        byte[] messageHash = SAI288.sai288Hash(message.toByteArray());
        BigInteger hashedMessage = new BigInteger(1, messageHash).mod(Q);
        BigInteger k = new BigInteger(Q.bitLength(), random).mod(Q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
        BigInteger r = G.modPow(k, P).mod(Q);
        BigInteger s = k.modInverse(Q).multiply(hashedMessage.add(privateKey.multiply(r))).mod(Q);
        System.out.println("ML-DSA: Signed message with r=" + r + ", s=" + s);
        return new Signature(r, s);
    }

    public static boolean verify(BigInteger publicKey, BigInteger message, Signature sig) {
        byte[] messageHash = SAI288.sai288Hash(message.toByteArray());
        BigInteger hashedMessage = new BigInteger(1, messageHash).mod(Q);
        BigInteger w = sig.s.modInverse(Q);
        BigInteger u1 = hashedMessage.multiply(w).mod(Q);
        BigInteger u2 = sig.r.multiply(w).mod(Q);
        BigInteger v = G.modPow(u1, P).multiply(publicKey.modPow(u2, P)).mod(P).mod(Q);
        boolean valid = v.equals(sig.r);
        System.out.println("ML-DSA: Verification " + (valid ? "succeeded" : "failed") + " for r=" + sig.r);
        return valid;
    }
}
