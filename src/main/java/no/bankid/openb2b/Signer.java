package no.bankid.openb2b;

import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.util.Base64;
import java.util.Optional;

import static no.bankid.openb2b.SecurityProvider.SHA_512_WITH_RSA_SIGNER_BUILDER;
import static no.bankid.openb2b.SecurityProvider.toCertificateHolder;
import static org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers.id_pkix_ocsp_response;

/**
 * See rfc5652 (https://tools.ietf.org/html/rfc5652) for details about cms content.
 */
class Signer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Signer.class);


    static byte[] sign(byte[] dataToBeSigned,
                       CertPath signerCertPath,
                       PrivateKey signerKey,
                       @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<byte[]> ocspResponse) {
        try {
            LOGGER.info("Signs a message");

            ContentSigner sha512Signer = SHA_512_WITH_RSA_SIGNER_BUILDER.build(signerKey);
            X509CertificateHolder signerCert = toCertificateHolder(signerCertPath.getCertificates().get(0));
            DigestCalculatorProvider digestProvider = new JcaDigestCalculatorProviderBuilder().build();
            JcaSignerInfoGeneratorBuilder infoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder(digestProvider);

            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSignerInfoGenerator(infoGeneratorBuilder.build(sha512Signer, signerCert));
            generator.addCertificates(new JcaCertStore(signerCertPath.getCertificates()));

            ocspResponse.ifPresent(response ->
                    generator.addOtherRevocationInfo(id_pkix_ocsp_response, OCSPResponse.getInstance(response)));

            LOGGER.info(ocspResponse
                    .map(response -> "EMBEDS an OCSP Response in the result")
                    .orElse("NO OCSP Response in the result"));

            CMSSignedData cmsSignedData = generator.generate(new CMSProcessableByteArray(dataToBeSigned), false);

            return Base64.getEncoder().encode(cmsSignedData.getEncoded());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
