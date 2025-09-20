import java.math.BigInteger;
import java.util.Arrays;

public class SAI288 {

    public static BigInteger factorial(int n){
        BigInteger result = BigInteger.ONE;
        for(int i=2;i<=n;i++) result = result.multiply(BigInteger.valueOf(i));
        return result;
    }

    public static double exponomialConstant(int i, double gamma, double R, double tau, double[] S_list, double phi){
        double exp_factor = Math.pow(gamma, i/R);
        double ssum = 0;
        for(int j=0;j<=Math.min(i,S_list.length-1);j++)
            ssum += S_list[j]*Math.pow(phi,j);
        return exp_factor*tau*ssum;
    }

    public static double proofOfExponomial(int n,int r,int delta_n,int delta_r){
        try{
            double term1 = factorial(n).doubleValue()/(factorial(r).doubleValue()*factorial(n-r).doubleValue());
            double term2 = factorial(delta_n).doubleValue()/(factorial(delta_r).doubleValue()*factorial(delta_n-delta_r).doubleValue());
            return Math.abs(term1-term2);
        }catch(Exception e){
            return 0.0;
        }
    }

    public static byte[] sai288Hash(byte[] data){
        BigInteger h = BigInteger.ZERO;
        for(int i=0;i<data.length;i++)
            h = h.add(BigInteger.valueOf(data[i] & 0xFF).shiftLeft(8*(i%36)));
        h = h.mod(BigInteger.ONE.shiftLeft(288));
        byte[] out = new byte[36];
        byte[] bytes = h.toByteArray();
        int start = Math.max(0, bytes.length-36);
        System.arraycopy(bytes, start, out, 36-Math.min(36,bytes.length-start), Math.min(36,bytes.length-start));
        return out;
    }
} 
