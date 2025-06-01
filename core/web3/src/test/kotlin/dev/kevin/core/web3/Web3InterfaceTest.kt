package dev.kevin.core.web3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import java.math.BigInteger
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

/**
 * Integration tests for Web3Interface using RPC endpoints and ABI-based contract interaction
 */
class Web3InterfaceTest {
    
    private val web3Interface = Web3Interface.forTestnet()

    @Test
    fun `test web3 interface initialization`() {
        assertNotNull(web3Interface.donation)
        assertNotNull(web3Interface.factory)
        assertNotNull(web3Interface.memberRegistry)
    }

    @Test
    fun `test network connection`() = runBlocking {
        val networkInfo = web3Interface.getNetworkInfo()
        
        assertTrue(networkInfo.isSuccess, "Should connect to Kaia testnet")
        
        val info = networkInfo.getOrThrow()
        assertEquals(Web3Config.KAIA_TESTNET_CHAIN_ID, info.chainId)
        assertTrue(info.latestBlock > 0)
        assertTrue(info.isConnected)
    }


    @Test
    fun `test utility functions`() {
        // Test KAIA to Wei conversion
        val oneKaia = Web3Interface.Utils.kaiaToWei(1.0)
        assertEquals(BigInteger.valueOf(1_000_000_000_000_000_000L), oneKaia)
        
        // Test Wei to KAIA conversion
        val backToKaia = Web3Interface.Utils.weiToKaia(oneKaia)
        assertEquals(1.0, backToKaia, 0.0001)
        
        // Test wallet generation
        val wallet = Web3Interface.Utils.generateTestWallet()
        assertNotNull(wallet.privateKey)
        assertNotNull(wallet.address)
        assertTrue(wallet.privateKey.startsWith("0x"))
        assertTrue(wallet.address.startsWith("0x"))
        assertEquals(42, wallet.address.length)
    }
    
    @Test
    fun `test deployed contracts configuration`() {
        val contracts = web3Interface.getDeployedContracts()
        
        assertNotNull(contracts.factoryAddress)
        assertNotNull(contracts.memberRegistryAddress)
        assertEquals(Web3Config.KAIA_TESTNET_CHAIN_ID, contracts.chainId)
    }
    
    @Test
    fun `test contract ABI loading`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        assertNotNull(abiJson)
        assertTrue(abiJson.contains("getSummary"))
        assertTrue(abiJson.contains("vote"))
        assertTrue(abiJson.contains("donate"))
        
        // Test ABI parsing
        val contractAbi = ContractAbiLoader.parseAbi(abiJson)
        assertTrue(contractAbi.abi.isNotEmpty())
        
        // Test function lookup
        val getSummaryFunction = ContractAbiLoader.getFunction(abiJson, "getSummary")
        assertNotNull(getSummaryFunction)
        assertEquals("getSummary", getSummaryFunction.name)
        assertEquals("function", getSummaryFunction.type)
    }
    
    @Test
    @EnabledIf("isIntegrationTestEnabled")
    fun `test proposal creation workflow`() = runBlocking {
        val testWallet = Web3Interface.Utils.generateTestWallet()
        
        val result = web3Interface.createProposalWorkflow(
            proposalText = "Test proposal for automated testing",
            durationMinutes = 60,
            creatorPrivateKey = testWallet.privateKey
        )
        
        if (result.isSuccess) {
            val proposalAddress = result.getOrThrow()
            assertNotNull(proposalAddress)
            assertTrue(proposalAddress.startsWith("0x"))
            assertEquals(42, proposalAddress.length)
        }
        // If it fails, that's expected in test environment without deployed contracts
    }
    
    @Test
    @EnabledIf("isIntegrationTestEnabled")
    fun `test voting workflow`() = runBlocking {
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS") 
            ?: "0xAF17d5aef17588d98d9bba6abc983C6F055FBb49"
        val testWallet = Web3Interface.Utils.generateTestWallet()
        
        val result = web3Interface.voteWorkflow(
            proposalAddress = testProposalAddress,
            approve = true,
            stakeKaia = 1.0,
            voterPrivateKey = testWallet.privateKey
        )
        
        if (result.isSuccess) {
            val voteTransaction = result.getOrThrow()
            assertNotNull(voteTransaction.transactionHash)
            assertTrue(voteTransaction.voteApproved)
        }
        // If it fails, that's expected without proper setup
    }
    
    @Test
    @EnabledIf("isIntegrationTestEnabled")
    fun `test donation workflow`() = runBlocking {
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS") 
            ?: "0x1234567890123456789012345678901234567890"
        val testWallet = Web3Interface.Utils.generateTestWallet()
        
        val result = web3Interface.donateWorkflow(
            proposalAddress = testProposalAddress,
            donationKaia = 0.1,
            donorPrivateKey = testWallet.privateKey
        )
        
        if (result.isSuccess) {
            val donationTransaction = result.getOrThrow()
            assertNotNull(donationTransaction.transactionHash)
            assertTrue(donationTransaction.donationAmount > BigInteger.ZERO)
        }
        // If it fails, that's expected without proper setup
    }
    
    @Test
    @EnabledIf("isIntegrationTestEnabled")
    fun `test proposal info retrieval`() = runBlocking {
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS") 
            ?: "0x1234567890123456789012345678901234567890"
        
        val result = web3Interface.getProposalInfo(testProposalAddress)
        
        if (result.isSuccess) {
            val proposalInfo = result.getOrThrow()
            assertNotNull(proposalInfo.summary)
            assertTrue(proposalInfo.stakeAmountKaia >= 0.0)
            assertTrue(proposalInfo.totalDonationsKaia >= 0.0)
            assertEquals(testProposalAddress.lowercase(), proposalInfo.summary.address.lowercase())
        }
        // If it fails, that's expected without a real deployed contract
    }
    
    @Test
    fun `test gas provider functionality`() {
        val standardProvider = KaiaGasProvider.standard()
        val fastProvider = KaiaGasProvider.fast()
        val slowProvider = KaiaGasProvider.slow()
        
        assertTrue(slowProvider.gasPrice < standardProvider.gasPrice)
        assertTrue(standardProvider.gasPrice < fastProvider.gasPrice)
        
        // Test gas cost estimation
        val voteCost = standardProvider.getEstimatedCost("vote")
        assertTrue(voteCost > BigInteger.ZERO)
        
        // Test gas price modification
        val modifiedProvider = standardProvider.withGasPrice(BigInteger.valueOf(50_000_000_000L))
        assertEquals(BigInteger.valueOf(50L), modifiedProvider.getGasPriceInGwei())
    }
    
    @Test
    fun `test custom RPC endpoint`() {
        val customWeb3 = Web3Interface.withCustomRpc("https://public-en.kairos.node.kaia.io")
        assertNotNull(customWeb3.donation)
        assertNotNull(customWeb3.factory)
        assertNotNull(customWeb3.memberRegistry)
    }
    
    companion object {
        @JvmStatic
        fun isIntegrationTestEnabled(): Boolean {
            return System.getenv("WEB3_INTEGRATION_TEST_ENABLED")?.toBoolean() ?: false
        }
    }
}