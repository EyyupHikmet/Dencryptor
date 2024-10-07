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

}