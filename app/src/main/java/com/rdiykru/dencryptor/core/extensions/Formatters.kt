package com.rdiykru.dencryptor.core.extensions

import java.math.BigInteger

object Formatters {
	fun String.toBigInteger(): BigInteger {
		return BigInteger(this.toByteArray())
	}

	fun BigInteger.toStringValue(): String {
		return String(this.toByteArray())
	}

}