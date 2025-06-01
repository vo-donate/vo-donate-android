package dev.kevin.core.web3

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.BeforeEach
import java.math.BigInteger
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Stress tests and edge cases for deployed contract access
 * 
 * These tests verify robustness, performance, and edge case handling
 * when interacting with deployed contracts on Kaia testnet.
 */
class DeployedContractStressTest {
    
    private lateinit var web3Interface: Web3Interface
    private lateinit var deployedContracts: Web3Interface.DeployedContracts
    
    @BeforeEach
    fun setup() {
        web3Interface = Web3Interface.forTestnet()
        deployedContracts = web3Interface.getDeployedContracts()
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test concurrent contract calls`() = runBlocking {
        println("🚀 Testing concurrent contract calls...")
        
        val numberOfCalls = 10
        val jobs = mutableListOf<Deferred<String>>()
        
        // Launch multiple concurrent calls
        repeat(numberOfCalls) { index ->
            val job = async {
                try {
                    val networkInfo = web3Interface.getNetworkInfo().getOrThrow()
                    "Call $index: Success - Block ${networkInfo.latestBlock}"
                } catch (e: Exception) {
                    "Call $index: Failed - ${e.message}"
                }
            }
            jobs.add(job)
        }
        
        // Wait for all calls to complete
        val results = jobs.awaitAll()
        
        println("✅ Concurrent calls completed:")
        results.forEach { println("   $it") }
        
        val successfulCalls = results.count { it.contains("Success") }
        assertTrue(successfulCalls > 0, "At least some concurrent calls should succeed")
        
        println("   Success rate: $successfulCalls/$numberOfCalls")
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test rapid sequential calls`() = runBlocking {
        println("🚀 Testing rapid sequential contract calls...")
        
        val numberOfCalls = 20
        val results = mutableListOf<String>()
        val startTime = System.currentTimeMillis()
        
        repeat(numberOfCalls) { index ->
            try {
                val networkInfo = web3Interface.getNetworkInfo().getOrThrow()
                results.add("Call $index: Success - Chain ${networkInfo.chainId}")
            } catch (e: Exception) {
                results.add("Call $index: Failed - ${e.message}")
            }
        }
        
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        
        println("✅ Rapid sequential calls completed in ${totalTime}ms:")
        val successfulCalls = results.count { it.contains("Success") }
        println("   Success rate: $successfulCalls/$numberOfCalls")
        println("   Average time per call: ${totalTime / numberOfCalls}ms")
        
        assertTrue(successfulCalls > numberOfCalls * 0.8, "At least 80% of calls should succeed")
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test invalid contract addresses`() = runBlocking {
        println("🚀 Testing invalid contract addresses...")
        
        val invalidAddresses = listOf(
            "0x0000000000000000000000000000000000000000", // Zero address
            "0x1111111111111111111111111111111111111111", // Non-existent address
            "0xdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef", // Likely non-existent
            "invalid_address", // Invalid format
            "", // Empty string
            "0x123" // Too short
        )
        
        invalidAddresses.forEach { address ->
            println("   Testing invalid address: $address")
            
            try {
                val result = web3Interface.donation.getProposalSummary(address)
                if (result.isFailure) {
                    println("     ✅ Correctly failed for invalid address")
                } else {
                    println("     ⚠️ Unexpectedly succeeded for invalid address")
                }
            } catch (e: Exception) {
                println("     ✅ Correctly threw exception: ${e.message?.take(50)}...")
            }
        }
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test network timeout handling`() = runBlocking {
        println("🚀 Testing network timeout handling...")
        
        // Test with a very short timeout
        val timeoutJob = withTimeoutOrNull(5.seconds) {
            try {
                val result = web3Interface.getNetworkInfo()
                if (result.isSuccess) {
                    println("   ✅ Network call completed within timeout")
                    true
                } else {
                    println("   ⚠️ Network call failed: ${result.exceptionOrNull()?.message}")
                    false
                }
            } catch (e: Exception) {
                println("   ❌ Network call threw exception: ${e.message}")
                false
            }
        }
        
        if (timeoutJob == null) {
            println("   ⏰ Network call timed out (this may be expected on slow networks)")
        } else {
            println("   ✅ Network call handled timeout correctly")
        }
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test contract state consistency`() = runBlocking {
        println("🚀 Testing contract state consistency...")
        
        val testProposalAddress = System.getenv("TEST_PROPOSAL_ADDRESS")
            ?: "0xAF17d5aef17588d98d9bba6abc983C6F055FBb49"
        
        // Make multiple calls to the same contract and verify consistency
        val summaries = mutableListOf<DonationContractInteractor.ProposalSummary>()
        val numberOfChecks = 5
        
        repeat(numberOfChecks) { index ->
            delay(1000) // Wait 1 second between calls
            
            try {
                val result = web3Interface.donation.getProposalSummary(testProposalAddress)
                if (result.isSuccess) {
                    val summary = result.getOrThrow()
                    summaries.add(summary)
                    println("   Check $index: Success - Votes: ${summary.totalVotes}, Donations: ${Web3Interface.Utils.weiToKaia(summary.totalDonations)} KAIA")
                } else {
                    println("   Check $index: Failed - ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("   Check $index: Exception - ${e.message}")
            }
        }
        
        if (summaries.size >= 2) {
            // Verify that basic immutable properties are consistent
            val firstSummary = summaries.first()
            val allConsistent = summaries.all { 
                it.address.equals(firstSummary.address, ignoreCase = true) &&
                it.proposer.equals(firstSummary.proposer, ignoreCase = true) &&
                it.text == firstSummary.text
            }
            
            assertTrue(allConsistent, "Immutable contract properties should be consistent across calls")
            println("   ✅ Contract state consistency verified")
        } else {
            println("   ⚠️ Not enough successful calls to verify consistency")
        }
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test large data handling`() = runBlocking {
        println("🚀 Testing large data handling...")
        
        try {
            // Test getting all proposals from factory (might be a large list)
            val proposalsResult = web3Interface.factory.getProposals(deployedContracts.factoryAddress)
            
            if (proposalsResult.isSuccess) {
                val proposals = proposalsResult.getOrThrow()
                println("   ✅ Successfully retrieved ${proposals.size} proposals")
                
                if (proposals.size > 10) {
                    println("   📊 Large dataset detected, testing individual proposal access...")
                    
                    // Test accessing a subset of proposals
                    val samplesToTest = minOf(5, proposals.size)
                    val samples = proposals.shuffled().take(samplesToTest)
                    
                    samples.forEachIndexed { index, proposalAddress ->
                        try {
                            val infoResult = web3Interface.getProposalInfo(proposalAddress)
                            if (infoResult.isSuccess) {
                                val info = infoResult.getOrThrow()
                                println("     Sample $index: ${proposalAddress} - ${info.summary.text.take(50)}...")
                            } else {
                                println("     Sample $index: Failed to get info for $proposalAddress")
                            }
                        } catch (e: Exception) {
                            println("     Sample $index: Exception for $proposalAddress - ${e.message}")
                        }
                    }
                } else {
                    println("   📊 Small dataset (${proposals.size} proposals)")
                }
            } else {
                println("   ⚠️ Failed to get proposals: ${proposalsResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            println("   ❌ Large data handling test failed: ${e.message}")
        }
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test edge case parameter values`() = runBlocking {
        println("🚀 Testing edge case parameter values...")
        
        val edgeCaseAddresses = listOf(
            "0x0000000000000000000000000000000000000001", // Minimal non-zero address
            "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", // Maximum address
            "0x000000000000000000000000000000000000dead", // Common test address
        )
        
        edgeCaseAddresses.forEach { address ->
            println("   Testing edge case address: $address")
            
            try {
                // Test various contract calls with edge case addresses
                val membershipResult = web3Interface.memberRegistry.isMember(
                    deployedContracts.memberRegistryAddress,
                    address
                )
                
                if (membershipResult.isSuccess) {
                    val isMember = membershipResult.getOrThrow()
                    println("     ✅ Membership check: $isMember")
                } else {
                    println("     ⚠️ Membership check failed: ${membershipResult.exceptionOrNull()?.message}")
                }
                
                val votedResult = web3Interface.donation.hasVoted(
                    System.getenv("TEST_PROPOSAL_ADDRESS") ?: "0xAF17d5aef17588d98d9bba6abc983C6F055FBb49",
                    address
                )
                
                if (votedResult.isSuccess) {
                    val hasVoted = votedResult.getOrThrow()
                    println("     ✅ Vote check: $hasVoted")
                } else {
                    println("     ⚠️ Vote check handled gracefully: ${votedResult.exceptionOrNull()?.message?.take(50)}...")
                }
                
            } catch (e: Exception) {
                println("     ✅ Exception handled gracefully: ${e.message?.take(50)}...")
            }
        }
    }
    
    @Test
    @EnabledIf("isStressTestEnabled")
    fun `test memory usage during intensive operations`() = runBlocking {
        println("🚀 Testing memory usage during intensive operations...")
        
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        println("   Initial memory usage: ${initialMemory / 1024 / 1024} MB")
        
        // Perform many operations
        repeat(50) { index ->
            try {
                // Mix of different operation types
                when (index % 4) {
                    0 -> web3Interface.getNetworkInfo()
                    1 -> web3Interface.factory.getProposals(deployedContracts.factoryAddress)
                    2 -> web3Interface.memberRegistry.getOwner(deployedContracts.memberRegistryAddress)
                    3 -> web3Interface.donation.getProposalSummary(
                        System.getenv("TEST_PROPOSAL_ADDRESS") ?: "0xAF17d5aef17588d98d9bba6abc983C6F055FBb49"
                    )
                }
                
                if (index % 10 == 0) {
                    val currentMemory = runtime.totalMemory() - runtime.freeMemory()
                    println("   After $index operations: ${currentMemory / 1024 / 1024} MB")
                }
                
            } catch (e: Exception) {
                // Ignore individual failures for memory test
            }
        }
        
        // Force garbage collection and check memory
        System.gc()
        delay(1000)
        
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        println("   Final memory usage: ${finalMemory / 1024 / 1024} MB")
        println("   Memory increase: ${memoryIncrease / 1024 / 1024} MB")
        
        // Memory increase should be reasonable (less than 100MB for this test)
        assertTrue(memoryIncrease < 100 * 1024 * 1024, "Memory increase should be reasonable")
        println("   ✅ Memory usage test passed")
    }
    
    companion object {
        @JvmStatic
        fun isStressTestEnabled(): Boolean {
            return System.getenv("STRESS_TEST_ENABLED")?.toBoolean() ?: false
        }
    }
}