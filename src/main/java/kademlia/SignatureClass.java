package kademlia;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureClass {
    // Create signature for specific contect from RPC message
    public static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }


    // Verify signatures from received RPC messages
    public static boolean verify(byte[] data, byte[] signature, String publicKeyStr) throws Exception {
        // Convert public key from String to PublicKey
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }
}
