package com.rdiykru.dencryptor.core.encryption.rsa

import java.math.BigInteger
import java.security.SecureRandom

object RSA {

	private const val E = "65537"
	private val random = SecureRandom()

	data class KeyPair(val publicKey: PublicKey, val privateKey: PrivateKey)

	data class PublicKey(val e: BigInteger, val n: BigInteger)

	data class PrivateKey(val d: BigInteger, val n: BigInteger)

	fun generateKeyPair(bitLength: Int): KeyPair {
		val p = generateStrongPrime(bitLength / 2)
		var q: BigInteger

		do {
			q = generateStrongPrime(bitLength / 2)
		} while (p == q || p.subtract(q).abs().bitLength() < bitLength / 4)

		val n = p.multiply(q)
		val totient = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))

		var e = BigInteger(E)
		while (totient.gcd(e) != BigInteger.ONE) {
			e = BigInteger.probablePrime(16, random)
		}

		val d = e.modInverse(totient)

		val publicKey = PublicKey(e, n)
		val privateKey = PrivateKey(d, n)

		return KeyPair(publicKey, privateKey)
	}

	private fun generateStrongPrime(bitLength: Int): BigInteger {
		var prime: BigInteger
		do {
			prime = BigInteger.probablePrime(bitLength, random)
		} while (!isStrongPrime(prime))
		return prime
	}

	private fun isStrongPrime(prime: BigInteger): Boolean {
		return prime.isProbablePrime(100) // 100 rounds of certainty
	}

	fun encrypt(message: String, publicKey: PublicKey): String {
		val messageBytes = message.toByteArray(Charsets.UTF_8)
		val messageBigInt = BigInteger(1, messageBytes) // Convert to BigInteger (positive)

		if (messageBigInt >= publicKey.n) {
			throw IllegalArgumentException("Message must be smaller than the modulus (n)")
		}

		val ciphertextBigInt = messageBigInt.modPow(publicKey.e, publicKey.n)
		return ciphertextBigInt.toString(16) // Convert BigInteger to hex string for transmission
	}

	fun decrypt(ciphertextHex: String, privateKey: PrivateKey): String {
		val ciphertextBigInt = BigInteger(ciphertextHex, 16) // Convert hex string to BigInteger

		if (ciphertextBigInt >= privateKey.n) {
			throw IllegalArgumentException("Ciphertext must be smaller than the modulus (n)")
		}

		val decryptedBigInt = ciphertextBigInt.modPow(privateKey.d, privateKey.n)
		val decryptedBytes = decryptedBigInt.toByteArray()
		return String(decryptedBytes, Charsets.UTF_8) // Convert bytes back to String
	}
}
