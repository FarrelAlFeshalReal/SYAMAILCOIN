import java.math.BigInteger;
import java.util.Random;
import java.io.*;
import java.util.*;

public class MLDSA_Syamailcoin {

    private static final BigInteger Q = new BigInteger("251"); // testing small
    private static final BigInteger P = new BigInteger("1009");
    private static final BigInteger G = new BigInteger("2");
    private static final Random random = new Random();

    public static class Signature {
        public BigInteger r;
        public BigInteger s;
        public Signature(BigInteger r, BigInteger s){ this.r=r; this.s=s; }
        public String toString(){ return r.toString()+":"+s.toString(); }
    }

    public static Signature sign(BigInteger privateKey, BigInteger message){
        BigInteger k = new BigInteger(Q.bitLength(), random).mod(Q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
        BigInteger r = G.modPow(k, P).mod(Q);
        BigInteger s = k.modInverse(Q).multiply(message.add(privateKey.multiply(r))).mod(Q);
        return new Signature(r,s);
    }

    public static boolean verify(BigInteger publicKey, BigInteger message, Signature sig){
        BigInteger w = sig.s.modInverse(Q);
        BigInteger u1 = message.multiply(w).mod(Q);
        BigInteger u2 = sig.r.multiply(w).mod(Q);
        BigInteger v = G.modPow(u1,P).multiply(publicKey.modPow(u2,P)).mod(P).mod(Q);
        return v.equals(sig.r);
    }

    public static BigInteger hashBlockToInteger(byte[] hash){
        return new BigInteger(1, hash);
    }

    public static void main(String[] args){
        try{
            List<String> blocks = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new FileReader("blockrecursive.jsonl"))){
                String line;
                while((line=br.readLine())!=null) blocks.add(line);
            }

            BigInteger privateKey = new BigInteger(Q.bitLength(), random).mod(Q);
            BigInteger publicKey = G.modPow(privateKey,P);

            List<String> signedBlocks = new ArrayList<>();
            for(String block : blocks){
                byte[] blockHash = SAI288.sai288Hash(block.getBytes());
                BigInteger message = hashBlockToInteger(blockHash);
                Signature sig = sign(privateKey,message);
                String signedBlock = block.substring(0, block.length()-1)+",\"MLDSA\":\""+sig.toString()+"\"}";
                signedBlocks.add(signedBlock);
            }

            try(PrintWriter pw = new PrintWriter(new FileWriter("blockrecursive_signed.jsonl"))){
                for(String sb: signedBlocks) pw.println(sb);
            }

            System.out.println("ML-DSA selesai, semua blok Syamailcoin sudah ditandatangani.");

        }catch(Exception e){ e.printStackTrace(); }
    }
}
