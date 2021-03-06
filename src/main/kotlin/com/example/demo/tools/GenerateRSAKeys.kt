package com.example.demo.tools

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class GenerateRSAKeys(private val hashPassword: ByteArray) {
    private val algorithm = "RSA"
    private val privateKeyPathFile = "privateKey/private.key"
    private val publicKeyPathFile = "publicKey/public.key"

    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    private val initVector = "encryptionIntVec"
    private val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

    init {
        if (!areKeysPresent())
            generateKeys()
        else
            readKeys()

//        encrypt("cze", publicKey)
//        print(decrypt( encrypt("cze", publicKey),privateKey))
    }

    private fun areKeysPresent(): Boolean = File(privateKeyPathFile).exists() && File(publicKeyPathFile).exists()

    fun getKeys(): Pair<PrivateKey, PublicKey> = Pair(privateKey, publicKey)

    private fun readKeys() {
        val privateKeyFile = File(privateKeyPathFile)
        val publicKeyFile = File(publicKeyPathFile)

        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey: SecretKey = SecretKeySpec(hashPassword, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

        try {

            val publicKeyByteArray = Files.readAllBytes(publicKeyFile.toPath())
            val publicKeyBytesDecrypted = cipher.doFinal(publicKeyByteArray)
            publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyBytesDecrypted))

            val privateKeyByteArray = Files.readAllBytes(privateKeyFile.toPath())
            val privateKeyBytesDecrypted = cipher.doFinal(privateKeyByteArray)
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytesDecrypted))
        }
        catch (e : Exception){
            val keyMockGen = KeyPairGenerator.getInstance(algorithm)
            keyMockGen.initialize(1024)
            val keyMock = keyMockGen.generateKeyPair()
            publicKey = keyMock.public
            privateKey = keyMock.private
        }
    }

    private fun generateKeys() {
        try {
            val keyGen = KeyPairGenerator.getInstance(algorithm)
            keyGen.initialize(1024)
            val key = keyGen.generateKeyPair()

            val privateKeyFile: File = File(privateKeyPathFile)
            val publicKeyFile: File = File(publicKeyPathFile)

            if (privateKeyFile.parentFile != null) {
                privateKeyFile.parentFile.mkdirs()
            }
            privateKeyFile.createNewFile()
            if (publicKeyFile.parentFile != null) {
                publicKeyFile.parentFile.mkdirs()
            }
            publicKeyFile.createNewFile()

            publicKey = key.public
            privateKey = key.private

            val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey: SecretKey = SecretKeySpec(hashPassword, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)

            val byteArrayPublic = cipher.doFinal(publicKey.encoded)
            val fos = FileOutputStream(publicKeyFile)
            fos.write(byteArrayPublic)

            val byteArrayPrivate = cipher.doFinal(privateKey.encoded)
            val fos2 = FileOutputStream(privateKeyFile)
            fos2.write(byteArrayPrivate)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//
//    private fun encrypt(text: String, key: PublicKey?): ByteArray? {
//        var cipherText: ByteArray? = null
//        try {
//            // get an RSA cipher object and print the provider
//            val cipher = Cipher.getInstance(algorytm)
//            // encrypt the plain text using the public key
//            cipher.init(Cipher.ENCRYPT_MODE, key)
//            cipherText = cipher.doFinal(text.toByteArray())
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return cipherText
//    }
//
//    /**
//     * Decrypt text using private key.
//     *
//     * @param text
//     * :encrypted text
//     * @param key
//     * :The private key
//     * @return plain text
//     * @throws java.lang.Exception
//     */
//    fun decrypt(text: ByteArray?, key: PrivateKey?): String {
//        var dectyptedText: ByteArray? = null
//        try {
//            // get an RSA cipher object and print the provider
//            val cipher = Cipher.getInstance(algorytm)
//
//            // decrypt the text using the private key
//            cipher.init(Cipher.DECRYPT_MODE, key)
//            dectyptedText = cipher.doFinal(text)
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
//        return String(dectyptedText!!)
//    }
//

}