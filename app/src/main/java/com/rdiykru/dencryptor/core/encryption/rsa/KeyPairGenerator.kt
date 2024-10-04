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

	fun encrypt(message: BigInteger, publicKey: PublicKey): BigInteger {
		if (message >= publicKey.n) {
			throw IllegalArgumentException("Message must be smaller than the modulus (n)")
		}
		return message.modPow(publicKey.e, publicKey.n)
	}

	fun decrypt(ciphertext: BigInteger, privateKey: PrivateKey): BigInteger {
		if (ciphertext >= privateKey.n) {
			throw IllegalArgumentException("Ciphertext must be smaller than the modulus (n)")
		}
		return ciphertext.modPow(privateKey.d, privateKey.n)
	}
}
