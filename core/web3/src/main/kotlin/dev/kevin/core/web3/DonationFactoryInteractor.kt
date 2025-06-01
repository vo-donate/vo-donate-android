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
 * Interactor for DonationProposalFactory contract
 */
class DonationFactoryInteractor(
    private val web3j: Web3j = Web3Config.createTestnetWeb3j(),
    private val gasProvider: ContractGasProvider = Web3Config.getDefaultTestnetGasProvider()
) {
    
    data class ProposalCreatedEvent(
        val proposalAddress: String,
        val proposer: String,
        val text: String,
        val donationDeadline: BigInteger,
        val transactionHash: String
    )
    
    /**
     * Create a new donation proposal
     */
    suspend fun createProposal(
        factoryAddress: String,
        proposalText: String,
        donationDurationInMinutes: BigInteger,
        creatorPrivateKey: String
    ): Result<ProposalCreatedEvent> = withContext(Dispatchers.IO) {
        try {
            val credentials = Web3Config.createCredentials(creatorPrivateKey)
            val transactionManager = KlayRawTransactionManager(web3j, credentials, Web3Config.KAIA_TESTNET_CHAIN_ID)
            
            // Create proposal function call
            val function = org.web3j.abi.datatypes.Function(
                "createProposal",
                listOf(
                    Utf8String(proposalText),
                    Uint256(donationDurationInMinutes)
                ),
                emptyList()
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            
            val gasPrice = gasProvider.gasPrice
            val gasLimit = BigInteger.valueOf(500_000L) // Higher gas limit for contract creation
            
            val transactionReceipt = transactionManager.sendTransaction(
                gasPrice,
                gasLimit,
                factoryAddress,
                encodedFunction,
                BigInteger.ZERO
            )
            
            val receipt = transactionReceipt
                ?: return@withContext Result.failure(Exception("Transaction failed"))
            
            // Parse logs to get the created proposal address
            val logs = receipt.rawResponse
            if (logs.isEmpty()) {
                return@withContext Result.failure(Exception("No events emitted"))
            }
            
            // For simplicity, we'll extract the proposal address from the first log
            // In a production app, you'd properly decode the ProposalCreated event
            val proposalCreatedEvent = ProposalCreatedEvent(
                proposalAddress = "0x" + logs.substring(26, 66), // Extract address from log data
                proposer = credentials.address,
                text = proposalText,
                donationDeadline = BigInteger.valueOf(System.currentTimeMillis() / 1000)
                    .add(donationDurationInMinutes.multiply(BigInteger.valueOf(60))),
                transactionHash = receipt.transactionHash
            )
            
            Result.success(proposalCreatedEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all deployed proposals from the factory
     */
    suspend fun getProposals(factoryAddress: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val function = org.web3j.abi.datatypes.Function(
                "getProposals",
                emptyList(),
                listOf(object : TypeReference<DynamicArray<Address>>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, factoryAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val addressArray = decodedResult[0].value as List<*>
            val proposals = addressArray.mapNotNull { it?.toString() }
            
            Result.success(proposals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific proposal address by index
     */
    suspend fun getProposalByIndex(factoryAddress: String, index: BigInteger): Result<String> = withContext(Dispatchers.IO) {
        try {
            val function = org.web3j.abi.datatypes.Function(
                "deployedProposals",
                listOf(Uint256(index)),
                listOf(object : TypeReference<Address>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, factoryAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val proposalAddress = decodedResult[0].value as String
            
            Result.success(proposalAddress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the member registry address
     */
    suspend fun getRegistryAddress(factoryAddress: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val function = org.web3j.abi.datatypes.Function(
                "registryAddress",
                emptyList(),
                listOf(object : TypeReference<Address>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, factoryAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            
            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }
            
            val decodedResult = FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val registryAddress = decodedResult[0].value as String
            
            Result.success(registryAddress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}