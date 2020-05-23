import java.security.PrivateKey;
import java.security.cert.Certificate;

public class KeystorePair {
	
	private PrivateKey privateKey;
	private Certificate certificate;
	
	public KeystorePair(PrivateKey privateKey, Certificate certificate) {
		this.privateKey = privateKey;
		this.certificate = certificate;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public Certificate getCertificate() {
		return certificate;
	}

	public String toString() {
		return "KeystorePair: " + privateKey.getAlgorithm() + "\n" + certificate.toString();
	}
}
