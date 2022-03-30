package org.jumpmind.pos.util.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@Slf4j
public class JWTSecurityService {
    public String encrypt(String stringToEncrypt, String encryptionPublicKey) throws JOSEException {
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256CBC_HS512);
        Payload payloadToEncrypt = new Payload(stringToEncrypt);
        JWEObject encryptionObject = new JWEObject(header, payloadToEncrypt);

        RSAKey rsaPublicKey = (RSAKey) JWK.parseFromPEMEncodedObjects(encryptionPublicKey);
        encryptionObject.encrypt(new RSAEncrypter(rsaPublicKey));

        return encryptionObject.serialize();
    }

    public String decrypt(String sourceEncryptedStr, String jwePrivateKey) throws JOSEException, ParseException {
        RSAKey rsaPrivateKey = (RSAKey) JWK.parseFromPEMEncodedObjects(jwePrivateKey);

        RSADecrypter decrypter = new RSADecrypter(rsaPrivateKey);
        JWEObject object = JWEObject.parse(sourceEncryptedStr);
        object.decrypt(decrypter);

        return object.getPayload().toString();
    }
}