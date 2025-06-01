package dev.kevin.core.web3

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.klaytn.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.tx.KlayRawTransactionManager
import org.web3j.tx.gas.ContractGasProvider

/**
 * Enhanced contract interactor for donation smart contracts using ABI-based RPC calls
 */
class DonationContractInteractor(
    private val web3j: Web3j = Web3Config.createTestnetWeb3j(),
    private val gasProvider: ContractGasProvider = Web3Config.getDefaultTestnetGasProvider()
) {
    
    companion object {
        // Contract ABI is loaded from donation_abi.json
        private val abiJson by lazy { ContractAbiLoader.loadDonationAbi() }
    }
    
    data class NetworkInfo(
        val chainId: Long,
        val latestBlock: Long,
        val networkVersion: String,
        val isConnected: Boolean
    )
    
    data class ProposalSummary(
        val address: String,
        val proposer: String,
        val text: String,
        val totalVotes: Int,
        val isVotingFinalized: Boolean,
        val isApproved: Boolean,
        val totalDonations: BigInteger,
        val donationEndTime: BigInteger,
        val isVotingActive: Boolean,
        val isDonationActive: Boolean,
        val approvalPercentage: Double
    )
    
    data class VoteTransaction(
        val transactionHash: String,
        val voteApproved: Boolean,
        val stakeAmount: BigInteger
    )
    
    data class DonationTransaction(
        val transactionHash: String,
        val donationAmount: BigInteger
    )
    
    /**
     * Test connection to the blockchain network
     */
    suspend fun testConnection(): Result<NetworkInfo> = withContext(Dispatchers.IO) {
        try {
            val latestBlock = web3j.ethBlockNumber().send().blockNumber
            val chainId = web3j.ethChainId().send().chainId
            val networkVersion = web3j.netVersion().send().netVersion
            
            val networkInfo = NetworkInfo(
                chainId = chainId.toLong(),
                latestBlock = latestBlock.toLong(),
                networkVersion = networkVersion,
                isConnected = true
            )
            Result.success(networkInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get proposal summary using getSummary() function
     */
    suspend fun getProposalSummary(contractAddress: String): Result<ProposalSummary> = withContext(Dispatchers.IO) {
        try {
            // Create getSummary function call
            val function = Function(
                "getSummary",
                emptyList(),
                listOf(
                    object : TypeReference<Address>() {},  // proposer
                    object : TypeReference<Utf8String>() {}, // text
                    object : TypeReference<Uint256>() {}, // voteCount
                    object : TypeReference<Bool>() {},    // finalized
                    object : TypeReference<Uint256>() {}, // totalDonation
                    object : TypeReference<Uint256>() {}, // donationEndTime
                    object : TypeReference<Uint256>() {}, // voterCount
                    object : TypeReference<Bool>() {},    // votePassed
                    object : TypeReference<Uint256>() {}  // STAKE_AMOUNT
                )
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            
            if (decodedResult.size < 9) {
                return@withContext Result.failure(Exception("Invalid response from contract"))
            }
            
            val proposer = decodedResult[0].value as String
            val text = decodedResult[1].value as String
            val voteCount = decodedResult[2].value as BigInteger
            val finalized = decodedResult[3].value as Boolean
            val totalDonation = decodedResult[4].value as BigInteger
            val donationEndTime = decodedResult[5].value as BigInteger
            val voterCount = decodedResult[6].value as BigInteger
            val votePassed = decodedResult[7].value as Boolean
            
            val currentTime = System.currentTimeMillis() / 1000
            val isVotingActive = !finalized && currentTime < donationEndTime.toLong()
            val isDonationActive = finalized && votePassed && currentTime < donationEndTime.toLong()
            
            val approvalPercentage = if (voterCount > BigInteger.ZERO) {
                voteCount.toDouble() / voterCount.toDouble()
            } else {
                0.0
            }
            
            val summary = ProposalSummary(
                address = contractAddress,
                proposer = proposer,
                text = text,
                totalVotes = voteCount.toInt(),
                isVotingFinalized = finalized,
                isApproved = votePassed,
                totalDonations = totalDonation,
                donationEndTime = donationEndTime,
                isVotingActive = isVotingActive,
                isDonationActive = isDonationActive,
                approvalPercentage = approvalPercentage
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get stake amount required for voting
     */
    suspend fun getStakeAmount(contractAddress: String): Result<BigInteger> = withContext(Dispatchers.IO) {
        try {
            val function = Function(
                "STAKE_AMOUNT",
                emptyList(),
                listOf(object : TypeReference<Uint256>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val stakeAmount = decodedResult[0].value as BigInteger
            
            Result.success(stakeAmount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submit a vote on a proposal
     */
    suspend fun voteOnProposal(
        proposalAddress: String,
        approve: Boolean,
        stakeAmountWei: BigInteger,
        voterPrivateKey: String
    ): Result<VoteTransaction> = withContext(Dispatchers.IO) {
        try {
            val credentials = Web3Config.createCredentials(voterPrivateKey)
            val transactionManager = KlayRawTransactionManager(web3j, credentials, Web3Config.KAIA_TESTNET_CHAIN_ID)
            
            // Create vote function call
            val function = Function(
                "vote",
                listOf(Bool(approve)),
                emptyList()
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            
            // Send transaction with stake amount as value
            val gasPrice = gasProvider.gasPrice
            val gasLimit = gasProvider.gasLimit
            
            val transactionReceipt = transactionManager.sendTransaction(
                gasPrice,
                gasLimit,
                proposalAddress,
                encodedFunction,
                stakeAmountWei
            )
            
            val receipt = transactionReceipt
                ?: return@withContext Result.failure(Exception("Transaction failed"))
            
            val voteTransaction = VoteTransaction(
                transactionHash = receipt.transactionHash,
                voteApproved = approve,
                stakeAmount = stakeAmountWei
            )
            
            Result.success(voteTransaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Donate to an approved proposal
     */
    suspend fun donateToProposal(
        proposalAddress: String,
        donationAmountWei: BigInteger,
        donorPrivateKey: String
    ): Result<DonationTransaction> = withContext(Dispatchers.IO) {
        try {
            val credentials = Web3Config.createCredentials(donorPrivateKey)
            val transactionManager = KlayRawTransactionManager(web3j, credentials, Web3Config.KAIA_TESTNET_CHAIN_ID)
            
            // Create donate function call
            val function = Function(
                "donate",
                emptyList(),
                emptyList()
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            
            // Send transaction with donation amount as value
            val gasPrice = gasProvider.gasPrice
            val gasLimit = gasProvider.gasLimit
            
            val transactionReceipt = transactionManager.sendTransaction(
                gasPrice,
                gasLimit,
                proposalAddress,
                encodedFunction,
                donationAmountWei
            )
            
            val receipt = transactionReceipt
                ?: return@withContext Result.failure(Exception("Transaction failed"))
            
            val donationTransaction = DonationTransaction(
                transactionHash = receipt.transactionHash,
                donationAmount = donationAmountWei
            )
            
            Result.success(donationTransaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if an address has already voted
     */
    suspend fun hasVoted(contractAddress: String, voterAddress: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val function = Function(
                "hasVoted",
                listOf(Address(voterAddress)),
                listOf(object : TypeReference<Bool>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val hasVoted = decodedResult[0].value as Boolean
            
            Result.success(hasVoted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get donation amount from a specific donor
     */
    suspend fun getDonationAmount(contractAddress: String, donorAddress: String): Result<BigInteger> = withContext(Dispatchers.IO) {
        try {
            val function = Function(
                "donations",
                listOf(Address(donorAddress)),
                listOf(object : TypeReference<Uint256>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val donationAmount = decodedResult[0].value as BigInteger
            
            Result.success(donationAmount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}