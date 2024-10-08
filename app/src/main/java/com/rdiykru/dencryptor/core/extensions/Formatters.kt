package com.rdiykru.dencryptor.core.extensions

import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import java.math.BigInteger

object Formatters {
	fun String.toBigInteger(): BigInteger {
		return BigInteger(this.toByteArray())
	}

	fun BigInteger.toStringValue(): String {
		return String(this.toByteArray())
	}
	fun RSA.PublicKey.size(): Int {
		return (n.bitLength() / 8) // Assuming n is the modulus in the RSA key
	}

	fun RSA.PrivateKey.size(): Int {
		return (n.bitLength() / 8) // Assuming n is the modulus in the RSA key
	}
	// Extension function to convert a String to PublicKey
	fun String.toPublicKey(): RSA.PublicKey? {
		return this.split(":").takeIf { it.size == 2 }?.let {
			try {
				val e = BigInteger(it[0])
				val n = BigInteger(it[1])
				RSA.PublicKey(e, n)
			} catch (e: NumberFormatException) {
				null // Handle the error appropriately
			}
		}
	}

	// Extension function to convert a String to PrivateKey
	fun String.toPrivateKey(): RSA.PrivateKey? {
		return this.split(":").takeIf { it.size == 2 }?.let {
			try {
				val d = BigInteger(it[0])
				val n = BigInteger(it[1])
				RSA.PrivateKey(d, n)
			} catch (e: NumberFormatException) {
				null // Handle the error appropriately
			}
		}
	}
	// Extension function to convert PublicKey to String
	fun RSA.PublicKey.toStringFormat(): String {
		return "${e.toString()}:${n.toString()}"
	}

	// Extension function to convert PrivateKey to String
	fun RSA.PrivateKey.toStringFormat(): String {
		return "$d:$n"
	}
}