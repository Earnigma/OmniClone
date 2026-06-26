package com.omniclone.engine

import com.android.apksig.ApkSigner as AndroidApkSigner
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Date
import java.util.UUID

/**
 * Generates a unique RSA-2048 keystore per clone and signs an APK with
 * JAR (v1), APK Signature Scheme v2, and APK Signature Scheme v3 signatures.
 *
 * The keystore is persisted alongside the clone output so the same certificate can be used to
 * update the clone later. A v3 signature provides key rotation support and signing-lineage is
 * omitted because each clone has a one-off key.
 */
class ApkSigner(private val keystoreDir: File) {

    companion object {
        private const val KEY_ALIAS = "omniclone"
        private const val KEY_PASSWORD = "omniclone"
        private const val CERT_VALIDITY_YEARS = 30L
        private const val RSA_KEY_SIZE = 2048

        init {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }

    /**
     * Sign the provided [unsignedApk].
     *
     * @param cloneId Unique identifier for the clone; used to derive the keystore file name.
     * @param unsignedApk Path to the APK that needs to be signed.
     * @param outputFile Destination signed APK file.
     * @return The signed APK file path.
     * @throws IllegalStateException if key generation or signing fails.
     */
    fun sign(cloneId: String, unsignedApk: File, outputFile: File): String {
        require(unsignedApk.exists()) { "Unsigned APK does not exist: ${unsignedApk.absolutePath}" }
        outputFile.parentFile?.mkdirs()

        val keystoreFile = getKeystoreFile(cloneId)
        val (privateKey, certificate) = loadOrCreateKeyPair(keystoreFile, cloneId)
        val certificates = listOf(certificate)

        val signerConfig = AndroidApkSigner.SignerConfig.Builder(
            "OmniClone-$cloneId",
            privateKey,
            certificates
        ).build()

        val signer = AndroidApkSigner.Builder(listOf(signerConfig))
            .setInputApk(unsignedApk)
            .setOutputApk(outputFile)
            .setMinSdkVersion(24)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .setV4SigningEnabled(false)
            .build()

        signer.sign()

        if (!outputFile.exists() || outputFile.length() == 0L) {
            throw IllegalStateException("Signed APK was not created")
        }

        return outputFile.absolutePath
    }

    /**
     * Generate or load a unique RSA-2048 keystore for a clone.
     */
    private fun loadOrCreateKeyPair(keystoreFile: File, cloneId: String): Pair<java.security.PrivateKey, X509Certificate> {
        if (keystoreFile.exists()) {
            return loadKeyPair(keystoreFile)
        }

        keystoreDir.mkdirs()
        val keyPair = generateRsaKeyPair()
        val certificate = generateSelfSignedCertificate(
            subject = X500Name("CN=OmniClone-$cloneId, O=OmniClone, C=US"),
            keyPair = keyPair,
            notBefore = Date(),
            notAfter = Date(System.currentTimeMillis() + CERT_VALIDITY_YEARS * 365L * 24L * 60L * 60L * 1000L)
        )

        val keyStore = KeyStore.getInstance("BKS", BouncyCastleProvider.PROVIDER_NAME)
        keyStore.load(null, KEY_PASSWORD.toCharArray())
        keyStore.setKeyEntry(
            KEY_ALIAS,
            keyPair.private,
            KEY_PASSWORD.toCharArray(),
            arrayOf(certificate)
        )
        keystoreFile.outputStream().use { out ->
            keyStore.store(out, KEY_PASSWORD.toCharArray())
        }

        return keyPair.private to certificate
    }

    private fun loadKeyPair(keystoreFile: File): Pair<java.security.PrivateKey, X509Certificate> {
        val keyStore = KeyStore.getInstance("BKS", BouncyCastleProvider.PROVIDER_NAME)
        keystoreFile.inputStream().use { input ->
            keyStore.load(input, KEY_PASSWORD.toCharArray())
        }
        val entry = keyStore.getEntry(KEY_ALIAS, KeyStore.PasswordProtection(KEY_PASSWORD.toCharArray()))
            as? PrivateKeyEntry
            ?: throw IllegalStateException("Keystore entry is not a private key entry")
        val certificate = entry.certificate as X509Certificate
        return entry.privateKey to certificate
    }

    private fun generateRsaKeyPair(): java.security.KeyPair {
        val keyGen = java.security.KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME)
        keyGen.initialize(RSA_KEY_SIZE)
        return keyGen.generateKeyPair()
    }

    private fun generateSelfSignedCertificate(
        subject: X500Name,
        keyPair: java.security.KeyPair,
        notBefore: Date,
        notAfter: Date
    ): X509Certificate {
        val serialNumber = BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)
        val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)

        val certBuilder = X509v3CertificateBuilder(
            subject,
            serialNumber,
            notBefore,
            notAfter,
            subject,
            subjectPublicKeyInfo
        )

        val signer = JcaContentSignerBuilder("SHA256withRSA")
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .build(keyPair.private)

        return JcaX509CertificateConverter()
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .getCertificate(certBuilder.build(signer))
    }

    private fun getKeystoreFile(cloneId: String): File {
        val safeId = cloneId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(keystoreDir, "$safeId.bks")
    }
}
