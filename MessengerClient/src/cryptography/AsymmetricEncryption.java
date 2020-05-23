package cryptography;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Strings;
import org.bouncycastle.x509.X509V1CertificateGenerator;

import account_information.KeystorePair;

import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;


import static java.nio.charset.StandardCharsets.UTF_8;


public class AsymmetricEncryption {

    public final static String KEYSTORE_PATH = "key_store";
    public final static String LOCAL_PRIVATE_KEY_STRING = "local_private_key";
    public final static String LOCAL_CERTIFICATE_STRING = "local_certificate";
    private final static String ENCRYPTION_ALGORITHM = "RSA";
    private final static String PROVIDER = "BC";


    public static String encrypt(PublicKey publicKey, String plainText) throws Exception {
        Cipher encryptCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(PrivateKey privateKey, String cipherText) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        Cipher decriptCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    public static KeystorePair generateSelfSignedX509Certificate() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date validityEndDate = new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000);

        // Generate RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM, PROVIDER);
        keyPairGenerator.initialize(1024, new SecureRandom());

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Generate X.509 certificate
        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal("CN=Username");
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setSubjectDN(dnName);
        certGen.setIssuerDN(dnName); // use the same
        certGen.setNotBefore(validityBeginDate);
        certGen.setNotAfter(validityEndDate);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        X509Certificate certificate = certGen.generate(keyPair.getPrivate(), PROVIDER);

        return new KeystorePair(privateKey, certificate);
    }

    public static void writeToKeyStore(PrivateKey privateKey, Certificate certificate,
                                       String username, String keystorePassword) throws Exception {

        FileOutputStream fos = new FileOutputStream(username + "\\" + KEYSTORE_PATH);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        Certificate[] chain = {(Certificate) certificate};
        keyStore.setKeyEntry(LOCAL_PRIVATE_KEY_STRING, privateKey, keystorePassword.toCharArray(), chain);
        keyStore.setCertificateEntry(LOCAL_CERTIFICATE_STRING, certificate);
        keyStore.store(fos, keystorePassword.toCharArray());
        fos.close();
    }

    public static KeystorePair readFromKeyStore(String username, String keystorePassword) throws Exception {
        FileInputStream fis = new java.io.FileInputStream(username + "\\" + KEYSTORE_PATH);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(fis, keystorePassword.toCharArray());

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(LOCAL_PRIVATE_KEY_STRING,
                keystorePassword.toCharArray());
        Certificate certificate = (Certificate) keyStore.getCertificate(LOCAL_CERTIFICATE_STRING);

        return new KeystorePair(privateKey, certificate);
    }

    public static String keyToString(Key key) {
        byte[] keyBytes = key.getEncoded();
        String keyString = Base64.getEncoder().encodeToString(keyBytes);

        return keyString;
    }

    public static Key stringToKey(String keyString) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        KeyFactory factory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM, PROVIDER);
        Key key = factory.generatePublic(new X509EncodedKeySpec(keyBytes));

        return key;
    }

}
