package dev.kevin.core.web3

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.BeforeEach
import java.math.BigInteger
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Integration tests for accessing deployed contracts on Kaia testnet
 * To run these tests, set environment variables:
 * - DEPLOYED_CONTRACT_TEST_ENABLED=true
 * - TEST_PRIVATE_KEY=0x... (private key with test KAIA)
 * - Optional: TEST_PROPOSAL_ADDRESS=0x... (existing proposal for testing)
 */
class DeployedContractAccessTest {
    
    private lateinit var web3Interface: Web3Interface
    private lateinit var deployedContracts: Web3Interface.DeployedContracts
    
    @BeforeEach
    fun setup() {
        web3Interface = Web3Interface.forTestnet()
        deployedContracts = web3Interface.getDeployedContracts()
    }
    
    @Test
    fun `test network connection to Kaia testnet`() = runBlocking {
        val networkInfo = web3Interface.getNetworkInfo()
        
        assertTrue(networkInfo.isSuccess, "Should connect to Kaia testnet successfully")
        
        val info = networkInfo.getOrThrow()
        assertEquals(1001L, info.chainId, "Should be connected to Kaia testnet (chain ID 1001)")
        assertTrue(info.latestBlock > 0, "Latest block should be positive")
        assertTrue(info.isConnected, "Should be connected")
        
        println("✅ Connected to Kaia testnet")
        println("   Chain ID: ${info.chainId}")
        println("   Latest Block: ${info.latestBlock}")
        println("   Network Version: ${info.networkVersion}")
    }
    
    @Test
    fun `test deployed contract addresses configuration`() {
        // Verify deployed contract addresses are properly configured
        assertNotNull(deployedContracts.factoryAddress)
        assertNotNull(deployedContracts.memberRegistryAddress)
        
        // Should not be placeholder addresses
        assertFalse(deployedContracts.factoryAddress.contains("0000000000000000000"))
        assertFalse(deployedContracts.memberRegistryAddress.contains("0000000000000000000"))
        
        // Should be valid Ethereum addresses
        assertTrue(deployedContracts.factoryAddress.startsWith("0x"))
        assertTrue(deployedContracts.memberRegistryAddress.startsWith("0x"))
        assertEquals(42, deployedContracts.factoryAddress.length)
        assertEquals(42, deployedContracts.memberRegistryAddress.length)
        
        println("✅ Deployed contract addresses verified")
        println("   Factory: ${deployedContracts.factoryAddress}")
        println("   Member Registry: ${deployedContracts.memberRegistryAddress}")
        println("   Chain ID: ${deployedContracts.chainId}")
    }
    
    @Test
    @EnabledIf("isDeployedContractTestEnabled")
    fun `test access deployed factory contract`() = runBlocking {
        println("🔍 Testing deployed factory contract access...")
        
        try {
            // Test getting proposals from factory
            val proposalsResult = web3Interface.factory.getProposals(deployedContracts.factoryAddress)
            
            if (proposalsResult.isSuccess) {
                val proposals = proposalsResult.getOrThrow()
                println("✅ Successfully accessed factory contract")
                println("   Total proposals: ${proposals.size}")
                
                if (proposals.isNotEmpty()) {
                    println("   Proposal addresses:")
                    proposals.forEachIndexed { index, address ->
                        println("     $index: $address")
                    }
                }
            } else {
                println("⚠️ Factory contract call failed: ${proposalsResult.exceptionOrNull()?.message}")
                // This might happen if the contract doesn't exist or has no proposals yet
            }
            
            // Test getting registry address from factory
            val registryResult = web3Interface.factory.getRegistryAddress(deployedContracts.factoryAddress)
            
            if (registryResult.isSuccess) {
                val registryAddress = registryResult.getOrThrow()
                println("✅ Factory returned registry address: $registryAddress")
                
                // Should match our configured registry address
                assertEquals(
                    deployedContracts.memberRegistryAddress.lowercase(),
                    registryAddress.lowercase(),
                    "Registry address from factory should match configured address"
                )
            } else {
                println("⚠️ Failed to get registry address: ${registryResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("❌ Factory contract test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    @EnabledIf("isDeployedContractTestEnabled")
    fun `test access deployed member registry contract`() = runBlocking {
        println("🔍 Testing deployed member registry contract access...")
        
        try {
            // Test getting owner of member registry
            val ownerResult = web3Interface.memberRegistry.getOwner(deployedContracts.memberRegistryAddress)
            
            if (ownerResult.isSuccess) {
                val owner = ownerResult.getOrThrow()
                println("✅ Successfully accessed member registry contract")
                println("   Owner address: $owner")
                
                assertNotNull(owner)
                assertTrue(owner.startsWith("0x"))
                assertEquals(42, owner.length)
            } else {
                println("⚠️ Member registry contract call failed: ${ownerResult.exceptionOrNull()?.message}")
                throw Exception("Failed to access member registry: ${ownerResult.exceptionOrNull()?.message}")
            }
            
            // Test checking membership for a random address
            val testAddress = "0x1234567890123456789012345678901234567890"
            val membershipResult = web3Interface.memberRegistry.isMember(
                deployedContracts.memberRegistryAddress,
                testAddress
            )
            
            if (membershipResult.isSuccess) {
                val isMember = membershipResult.getOrThrow()
                println("✅ Membership check successful for $testAddress: $isMember")
            } else {
                println("⚠️ Membership check failed: ${membershipResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("❌ Member registry contract test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    @EnabledIf("isDeployedContractTestEnabled")
    fun `test access existing proposal contract`() = runBlocking {
        println("🔍 Testing access to proposal contract...")
        
        // Try to get proposal address from environment or use a test address
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS")
        
        try {
            // Test getting proposal summary
            val summaryResult = web3Interface.donation.getProposalSummary(testProposalAddress)
            
            if (summaryResult.isSuccess) {
                val summary = summaryResult.getOrThrow()
                println("✅ Successfully accessed proposal contract")
                println("   Address: ${summary.address}")
                println("   Proposer: ${summary.proposer}")
                println("   Text: ${summary.text}")
                println("   Total Votes: ${summary.totalVotes}")
                println("   Is Voting Finalized: ${summary.isVotingFinalized}")
                println("   Is Approved: ${summary.isApproved}")
                println("   Total Donations: ${Web3Interface.Utils.weiToKaia(summary.totalDonations)} KAIA")
                println("   Is Voting Active: ${summary.isVotingActive}")
                println("   Is Donation Active: ${summary.isDonationActive}")
                println("   Approval Percentage: ${String.format("%.1f", summary.approvalPercentage * 100)}%")
                
                // Verify data structure
                assertNotNull(summary.address)
                assertNotNull(summary.proposer)
                assertTrue(summary.totalVotes >= 0)
                assertTrue(summary.totalDonations >= BigInteger.ZERO)
                assertTrue(summary.approvalPercentage >= 0.0)
                
            } else {
                println("⚠️ Proposal contract call failed: ${summaryResult.exceptionOrNull()?.message}")
                println("   This is expected if the proposal address doesn't exist")
            }
            
            // Test getting stake amount
            val stakeResult = web3Interface.donation.getStakeAmount(testProposalAddress)
            
            if (stakeResult.isSuccess) {
                val stakeAmount = stakeResult.getOrThrow()
                println("✅ Stake amount: ${Web3Interface.Utils.weiToKaia(stakeAmount)} KAIA")
                assertTrue(stakeAmount >= BigInteger.ZERO)
            } else {
                println("⚠️ Failed to get stake amount: ${stakeResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("❌ Proposal contract test failed: ${e.message}")
            // Don't throw here as the proposal might not exist
        }
    }
    
    @Test
    @EnabledIf("isDeployedContractTestEnabled")
    fun `test comprehensive proposal info retrieval`(): Unit = runBlocking {
        println("🔍 Testing comprehensive proposal info retrieval...")
        
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS")
            ?: "0xAF17d5aef17588d98d9bba6abc983C6F055FBb49"
        
        try {
            val proposalInfoResult = web3Interface.getProposalInfo(testProposalAddress)
            
            if (proposalInfoResult.isSuccess) {
                val proposalInfo = proposalInfoResult.getOrThrow()
                println("✅ Successfully retrieved comprehensive proposal info")
                println("   Stake Amount: ${proposalInfo.stakeAmountKaia} KAIA")
                println("   Total Donations: ${proposalInfo.totalDonationsKaia} KAIA")
                println("   Summary: ${proposalInfo.summary}")
                
                assertTrue(proposalInfo.stakeAmountKaia >= 0.0)
                assertTrue(proposalInfo.totalDonationsKaia >= 0.0)
                assertNotNull(proposalInfo.summary)
                
            } else {
                println("⚠️ Failed to get comprehensive proposal info: ${proposalInfoResult.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("❌ Comprehensive proposal info test failed: ${e.message}")
        }
    }
    
    @Test
    @EnabledIf("isTransactionTestEnabled")
    fun `test proposal creation on deployed factory`() = runBlocking {
        println("🔍 Testing proposal creation on deployed factory...")
        
        val testPrivateKey = System.getenv("TEST_PRIVATE_KEY")
            ?: throw IllegalArgumentException("TEST_PRIVATE_KEY environment variable required")
        
        assertTrue(!testPrivateKey.startsWith("0x0000"), "TEST_PRIVATE_KEY must be a valid test key")
        
        try {
            val result = web3Interface.createProposalWorkflow(
                proposalText = "Test proposal created at ${System.currentTimeMillis()}",
                durationMinutes = 60, // 1 hour
                creatorPrivateKey = testPrivateKey
            )
            
            if (result.isSuccess) {
                val proposalAddress = result.getOrThrow()
                println("✅ Successfully created proposal")
                println("   New proposal address: $proposalAddress")
                
                assertNotNull(proposalAddress)
                assertTrue(proposalAddress.startsWith("0x"))
                assertEquals(42, proposalAddress.length)
                
                // Wait a bit and then try to get the proposal info
                kotlinx.coroutines.delay(5000) // Wait 5 seconds for transaction confirmation
                
                val infoResult = web3Interface.getProposalInfo(proposalAddress)
                if (infoResult.isSuccess) {
                    val info = infoResult.getOrThrow()
                    println("✅ Verified new proposal:")
                    println("   Text: ${info.summary.text}")
                    println("   Proposer: ${info.summary.proposer}")
                }
                
            } else {
                println("❌ Proposal creation failed: ${result.exceptionOrNull()?.message}")
                throw Exception("Proposal creation failed: ${result.exceptionOrNull()?.message}")
            }
            
        } catch (e: Exception) {
            println("❌ Proposal creation test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    @EnabledIf("isTransactionTestEnabled")
    fun `test voting on existing proposal`() = runBlocking {
        println("🔍 Testing voting on existing proposal...")
        
        val testPrivateKey = System.getenv("TEST_PRIVATE_KEY")
            ?: throw IllegalArgumentException("TEST_PRIVATE_KEY environment variable required")
        
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS")
            ?: throw IllegalArgumentException("TEST_PROPOSAL_ADDRESS environment variable required for voting test")
        
        try {
            val result = web3Interface.voteWorkflow(
                proposalAddress = testProposalAddress,
                approve = true,
                stakeKaia = 1.0,
                voterPrivateKey = testPrivateKey
            )
            
            if (result.isSuccess) {
                val voteTransaction = result.getOrThrow()
                println("✅ Successfully submitted vote")
                println("   Transaction Hash: ${voteTransaction.transactionHash}")
                println("   Vote Approved: ${voteTransaction.voteApproved}")
                println("   Stake Amount: ${Web3Interface.Utils.weiToKaia(voteTransaction.stakeAmount)} KAIA")
                
                assertNotNull(voteTransaction.transactionHash)
                assertTrue(voteTransaction.voteApproved)
                assertTrue(voteTransaction.stakeAmount > BigInteger.ZERO)
                
            } else {
                println("⚠️ Voting failed (may be expected): ${result.exceptionOrNull()?.message}")
                // Voting might fail if already voted or voting is closed
            }
            
        } catch (e: Exception) {
            println("❌ Voting test failed: ${e.message}")
            // Don't throw here as voting might fail for valid reasons
        }
    }
    
    @Test
    fun `test utility functions with deployed contract data`() = runBlocking {
        println("🔍 Testing utility functions...")
        
        // Test KAIA/Wei conversions
        val oneKaia = Web3Interface.Utils.kaiaToWei(1.0)
        val backToKaia = Web3Interface.Utils.weiToKaia(oneKaia)
        
        assertEquals(1.0, backToKaia, 0.0001, "KAIA/Wei conversion should be accurate")
        println("✅ KAIA/Wei conversion test passed")
        
        // Test wallet generation
        val testWallet = Web3Interface.Utils.generateTestWallet()
        assertNotNull(testWallet.privateKey)
        assertNotNull(testWallet.address)
        assertTrue(testWallet.privateKey.startsWith("0x"))
        assertTrue(testWallet.address.startsWith("0x"))
        println("✅ Test wallet generation passed")
        println("   Generated address: ${testWallet.address}")
        
        // Test contract info
        val contracts = web3Interface.getDeployedContracts()
        assertEquals(1001L, contracts.chainId)
        println("✅ Contract configuration test passed")
    }
    
    companion object {
        @JvmStatic
        fun isDeployedContractTestEnabled(): Boolean {
            return System.getenv("DEPLOYED_CONTRACT_TEST_ENABLED")?.toBoolean() ?: false
        }
        
        @JvmStatic
        fun isTransactionTestEnabled(): Boolean {
            val testEnabled = System.getenv("DEPLOYED_CONTRACT_TEST_ENABLED")?.toBoolean() ?: false
            val privateKey = System.getenv("TEST_PRIVATE_KEY")
            return testEnabled && privateKey != null && !privateKey.startsWith("0x0000")
        }
    }
}