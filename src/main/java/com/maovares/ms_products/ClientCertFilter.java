package com.maovares.ms_products;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ClientCertFilter extends OncePerRequestFilter {

    @Value("${CLIENT_CERT_THUMBPRINT:}")
    private String expectedThumbprint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("X-ARR-ClientCert");
        if (header == null || header.isBlank()) {
            logger.warn("Rechazado: header X-ARR-ClientCert ausente");
            reject(response);
            return;
        }
        if (expectedThumbprint == null || expectedThumbprint.isBlank()) {
            logger.warn("Rechazado: CLIENT_CERT_THUMBPRINT no configurada");
            reject(response);
            return;
        }
        try {
            byte[] der = Base64.getDecoder().decode(header);
            X509Certificate cert = (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(der));
            String thumbprint = HexFormat.of().withUpperCase()
                    .formatHex(MessageDigest.getInstance("SHA-1").digest(cert.getEncoded()));
            String expected = expectedThumbprint.trim().toUpperCase();
            if (!MessageDigest.isEqual(thumbprint.getBytes(), expected.getBytes())) {
                logger.warn("Rechazado: thumbprint no coincide");
                reject(response);
                return;
            }
        } catch (Exception e) {
            logger.warn("Rechazado: certificado invalido (" + e.getClass().getSimpleName() + ")");
            reject(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/html");
        response.getWriter().write("<h1>Client Certificate Required</h1>");
    }
}
