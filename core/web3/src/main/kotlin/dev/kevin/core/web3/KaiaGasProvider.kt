package dev.kevin.core.web3

import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

/**
 * Gas provider optimized for Kaia (formerly Klaytn) blockchain
 */
class KaiaGasProvider(
    private val _gasPrice: BigInteger,
    private val _gasLimit: BigInteger
) : ContractGasProvider {
    
    companion object {
        // Kaia-optimized gas prices (in wei)
        val SLOW_GAS_PRICE = BigInteger.valueOf(20_000_000_000L)    // 20 Gwei
        val STANDARD_GAS_PRICE = BigInteger.valueOf(25_000_000_000L) // 25 Gwei
        val FAST_GAS_PRICE = BigInteger.valueOf(30_000_000_000L)    // 30 Gwei
        
        // Default gas limit
        val DEFAULT_GAS_LIMIT = BigInteger.valueOf(3_000_000L)
        
        // Operation-specific gas limits
        val VOTE_GAS_LIMIT = BigInteger.valueOf(150_000L)
        val DONATE_GAS_LIMIT = BigInteger.valueOf(100_000L)
        val FINALIZE_GAS_LIMIT = BigInteger.valueOf(200_000L)
        val WITHDRAW_GAS_LIMIT = BigInteger.valueOf(100_000L)
        
        /**
         * Standard gas provider (recommended for most transactions)
         */
        fun standard(): KaiaGasProvider {
            return KaiaGasProvider(STANDARD_GAS_PRICE, DEFAULT_GAS_LIMIT)
        }
        
        /**
         * Fast gas provider (for urgent transactions)
         */
        fun fast(): KaiaGasProvider {
            return KaiaGasProvider(FAST_GAS_PRICE, DEFAULT_GAS_LIMIT)
        }
        
        /**
         * Slow gas provider (for non-urgent transactions)
         */
        fun slow(): KaiaGasProvider {
            return KaiaGasProvider(SLOW_GAS_PRICE, DEFAULT_GAS_LIMIT)
        }
    }
    
    fun getGasPrice(contractFunc: String?): BigInteger = _gasPrice
    
    override fun getGasPrice(): BigInteger = _gasPrice
    
    fun getGasLimit(contractFunc: String?): BigInteger = _gasLimit
    
    override fun getGasLimit(): BigInteger = _gasLimit

    override fun getGasLimit(transaction: Transaction?): BigInteger = _gasLimit
    
    /**
     * Get gas price in Gwei
     */
    fun getGasPriceInGwei(): BigInteger {
        return _gasPrice.divide(BigInteger.valueOf(1_000_000_000L))
    }
    
    /**
     * Get estimated cost for a specific operation
     */
    fun getEstimatedCost(operation: String): BigInteger {
        val gasLimit = when (operation.lowercase()) {
            "vote" -> VOTE_GAS_LIMIT
            "donate" -> DONATE_GAS_LIMIT
            "finalize" -> FINALIZE_GAS_LIMIT
            "withdraw" -> WITHDRAW_GAS_LIMIT
            else -> _gasLimit
        }
        return _gasPrice.multiply(gasLimit)
    }
    
    /**
     * Create a new gas provider with different gas price
     */
    fun withGasPrice(newGasPrice: BigInteger): KaiaGasProvider {
        return KaiaGasProvider(newGasPrice, _gasLimit)
    }
    
    /**
     * Create a new gas provider with different gas limit
     */
    fun withGasLimit(newGasLimit: BigInteger): KaiaGasProvider {
        return KaiaGasProvider(_gasPrice, newGasLimit)
    }
}