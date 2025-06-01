package dev.kevin.core.web3

import org.web3j.protocol.klaytn.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.crypto.KlayCredentials
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

/**
 * Web3 configuration for Kaia (formerly Klaytn) blockchain
 */
object Web3Config {
    
    // Kaia Testnet RPC endpoints
    private const val KAIA_TESTNET_RPC = "https://public-en.kairos.node.kaia.io"
    private const val KAIA_MAINNET_RPC = "https://public-en.node.kaia.io"
    
    // Chain IDs
    const val KAIA_TESTNET_CHAIN_ID = 1001L
    const val KAIA_MAINNET_CHAIN_ID = 8217L
    
    // Default gas settings for Kaia
    object GasSettings {
        val DEFAULT_GAS_PRICE = BigInteger.valueOf(25_000_000_000L) // 25 Gwei
        val DEFAULT_GAS_LIMIT = BigInteger.valueOf(3_000_000L)
        
        // Operation-specific gas limits
        val VOTE_GAS_LIMIT = BigInteger.valueOf(150_000L)
        val DONATE_GAS_LIMIT = BigInteger.valueOf(100_000L)
        val FINALIZE_GAS_LIMIT = BigInteger.valueOf(200_000L)
        val WITHDRAW_GAS_LIMIT = BigInteger.valueOf(100_000L)
    }
    
    /**
     * Create Web3j instance for Kaia testnet
     */
    fun createTestnetWeb3j(): Web3j {
        return Web3j.build(HttpService(KAIA_TESTNET_RPC))
    }
    
    /**
     * Create Web3j instance for Kaia mainnet
     */
    fun createMainnetWeb3j(): Web3j {
        return Web3j.build(HttpService(KAIA_MAINNET_RPC))
    }
    
    /**
     * Create Web3j instance with custom RPC endpoint
     */
    fun createCustomWeb3j(rpcEndpoint: String): Web3j {
        return Web3j.build(HttpService(rpcEndpoint))
    }
    
    /**
     * Create credentials from private key
     */
    fun createCredentials(privateKey: String): KlayCredentials {
        return KlayCredentials.create(privateKey)
    }
    
    /**
     * Create custom gas provider
     */
    fun createGasProvider(gasPrice: BigInteger, gasLimit: BigInteger): ContractGasProvider {
        return KaiaGasProvider(gasPrice, gasLimit)
    }
    
    /**
     * Get default gas provider for testnet
     */
    fun getDefaultTestnetGasProvider(): ContractGasProvider {
        return KaiaGasProvider.standard()
    }
}