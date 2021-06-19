package com.example.demo.tools

import java.security.Key
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec


object ToolsSessionKey {

    fun readKey(byteArray: ByteArray): Key = SecretKeySpec(byteArray, "AES")

    fun decodeSessionKey(byteArrayPublicKey: ByteArray, privateKey: Key): Key {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val byteArray : ByteArray
        try {
            byteArray = cipher.doFinal(byteArrayPublicKey)
        }
        catch (e : Exception){
            val generator = KeyGenerator.getInstance("AES")
            generator.init(256, SecureRandom(Date().toString().toByteArray()))
            return generator.generateKey()
        }
        return SecretKeySpec(byteArray, "AES")
    }


    fun generateSessionKeyAndEncodeIt(byteArrayPublicKey: ByteArray): Pair<Key, ByteArray> {

        if (byteArrayPublicKey.isEmpty()) println("error")

        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(byteArrayPublicKey))

        // better do not change seed, default seed is more unpredictable
        val secureRandom = SecureRandom(Date().toString().toByteArray())

        val generator = KeyGenerator.getInstance("AES")
        generator.init(256, secureRandom)

        val sessionKey = generator.generateKey()

        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        // return sessionKey and encoded sessionKey
        return Pair(sessionKey, cipher.doFinal(sessionKey.encoded))

    }

    fun encodeSessionKey(byteArraySessionKey: ByteArray, privateKey: Key): Key {

        val cipher = Cipher.getInstance("RSA")

        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val byteArraySessionKey = cipher.doFinal(byteArraySessionKey)

        return SecretKeySpec(byteArraySessionKey, "AES")
    }
}