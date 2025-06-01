package dev.kevin.core.web3

import org.web3j.protocol.klaytn.Web3j
import java.math.BigInteger

/**
 * Main interface for Web3 interactions with donation smart contracts
 *
 * This class provides a unified API for interacting with:
 * - DonationProposal contracts
 * - DonationProposalFactory contract
 * - MemberRegistry contract
 *
 * Uses ABI-based RPC calls to deployed smart contracts
 */
class Web3Interface(
    private val isTestnet: Boolean = true,
    private val customRpcEndpoint: String? = null
) {

    private val web3j: Web3j by lazy {
        when {
            customRpcEndpoint != null -> Web3Config.createCustomWeb3j(customRpcEndpoint)
            isTestnet -> Web3Config.createTestnetWeb3j()
            else -> Web3Config.createMainnetWeb3j()
        }
    }

    private val gasProvider: KaiaGasProvider = KaiaGasProvider.standard()

    // Contract interactions
    val donation by lazy { DonationContractInteractor(web3j, gasProvider) }
    val factory by lazy { DonationFactoryInteractor(web3j, gasProvider) }
    val memberRegistry by lazy { MemberRegistryInteractor(web3j, gasProvider) }

    companion object {
        /**
         * Create interface for Kaia testnet
         */
        fun forTestnet(): Web3Interface {
            return Web3Interface(isTestnet = true)
        }

        /**
         * Create interface for Kaia mainnet
         */
        fun forMainnet(): Web3Interface {
            return Web3Interface(isTestnet = false)
        }

        /**
         * Create interface with custom RPC endpoint
         */
        fun withCustomRpc(rpcEndpoint: String): Web3Interface {
            return Web3Interface(customRpcEndpoint = rpcEndpoint)
        }
    }

    /**
     * Test connection to the blockchain network
     */
    private suspend fun testConnection() = donation.testConnection()

    /**
     * Get network information
     */
    suspend fun getNetworkInfo(): Result<DonationContractInteractor.NetworkInfo> {
        return this.testConnection()
    }

    /**
     * Utility functions for common operations
     */
    object Utils {
        /**
         * Convert KAIA to Wei
         */
        fun kaiaToWei(kaia: Double): BigInteger {
            val kaiaDecimal = java.math.BigDecimal.valueOf(kaia)
            val weiDecimal =
                kaiaDecimal.multiply(java.math.BigDecimal.valueOf(1_000_000_000_000_000_000L))
            return weiDecimal.toBigInteger()
        }

        /**
         * Convert Wei to KAIA
         */
        fun weiToKaia(wei: BigInteger): Double {
            val weiDecimal = java.math.BigDecimal(wei)
            val kaiaDecimal =
                weiDecimal.divide(java.math.BigDecimal.valueOf(1_000_000_000_000_000_000L))
            return kaiaDecimal.toDouble()
        }

        /**
         * Generate a new wallet for testing
         */
        fun generateTestWallet(): TestWallet {
            val credentials = org.web3j.crypto.Keys.createEcKeyPair()
            val privateKey = credentials.privateKey.toString(16)
            val address = org.web3j.crypto.Credentials.create(privateKey).address

            return TestWallet(
                privateKey = "0x$privateKey",
                address = address
            )
        }

        data class TestWallet(
            val privateKey: String,
            val address: String
        )
    }

    /**
     * Contract deployment configuration
     */
    data class DeployedContracts(
        val factoryAddress: String,
        val memberRegistryAddress: String,
        val chainId: Long
    ) {
        companion object {
            /**
             * Kaia testnet deployed contracts
             * Replace with actual deployed contract addresses
             */
            val KAIA_TESTNET = DeployedContracts(
                factoryAddress = "", // Replace with actual
                memberRegistryAddress = "", // Replace with actual
                chainId = Web3Config.KAIA_TESTNET_CHAIN_ID
            )

            /**
             * Kaia mainnet deployed contracts
             * Replace with actual deployed contract addresses
             */
            val KAIA_MAINNET = DeployedContracts(
                factoryAddress = "0x0000000000000000000000000000000000000000", // Replace with actual
                memberRegistryAddress = "0x0000000000000000000000000000000000000000", // Replace with actual
                chainId = Web3Config.KAIA_MAINNET_CHAIN_ID
            )
        }
    }

    /**
     * Get deployed contract addresses for current network
     */
    fun getDeployedContracts(): DeployedContracts {
        return if (isTestnet) {
            DeployedContracts.KAIA_TESTNET
        } else {
            DeployedContracts.KAIA_MAINNET
        }
    }

    /**
     * Complete workflow example functions
     */

    /**
     * Complete proposal creation workflow
     */
    suspend fun createProposalWorkflow(
        proposalText: String,
        durationMinutes: Int,
        creatorPrivateKey: String
    ): Result<String> {
        val contracts = getDeployedContracts()

        return factory.createProposal(
            factoryAddress = contracts.factoryAddress,
            proposalText = proposalText,
            donationDurationInMinutes = BigInteger.valueOf(durationMinutes.toLong()),
            creatorPrivateKey = creatorPrivateKey
        ).map { it.proposalAddress }
    }

    /**
     * Complete voting workflow
     */
    suspend fun voteWorkflow(
        proposalAddress: String,
        approve: Boolean,
        stakeKaia: Double,
        voterPrivateKey: String
    ): Result<DonationContractInteractor.VoteTransaction> {
        val stakeWei = Utils.kaiaToWei(stakeKaia)

        return donation.voteOnProposal(
            proposalAddress = proposalAddress,
            approve = approve,
            stakeAmountWei = stakeWei,
            voterPrivateKey = voterPrivateKey
        )
    }

    /**
     * Complete donation workflow
     */
    suspend fun donateWorkflow(
        proposalAddress: String,
        donationKaia: Double,
        donorPrivateKey: String
    ): Result<DonationContractInteractor.DonationTransaction> {
        val donationWei = Utils.kaiaToWei(donationKaia)

        return donation.donateToProposal(
            proposalAddress = proposalAddress,
            donationAmountWei = donationWei,
            donorPrivateKey = donorPrivateKey
        )
    }

    /**
     * Get comprehensive proposal information
     */
    suspend fun getProposalInfo(proposalAddress: String): Result<ProposalInfo> {
        return try {
            val summary = donation.getProposalSummary(proposalAddress).getOrThrow()
            val stakeAmount = donation.getStakeAmount(proposalAddress).getOrThrow()

            val proposalInfo = ProposalInfo(
                summary = summary,
                stakeAmountKaia = Utils.weiToKaia(stakeAmount),
                totalDonationsKaia = Utils.weiToKaia(summary.totalDonations)
            )

            Result.success(proposalInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class ProposalInfo(
        val summary: DonationContractInteractor.ProposalSummary,
        val stakeAmountKaia: Double,
        val totalDonationsKaia: Double
    )
}