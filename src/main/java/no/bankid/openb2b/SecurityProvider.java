package no.bankid.openb2b;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityProvider {

    static final CertificateFactory CERTIFICATE_FACTORY;

    static {

        Security.addProvider(new BouncyCastleProvider());

        try {
            CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static List<X509CertificateHolder> toCertificateHolders(List<? extends Certificate> signerChain) {
        return signerChain.stream().map(SecurityProvider::toCertificateHolder).collect(Collectors.toList());
    }

    static X509CertificateHolder toCertificateHolder(Certificate c) {
        try {
            return new JcaX509CertificateHolder((X509Certificate) c);
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
