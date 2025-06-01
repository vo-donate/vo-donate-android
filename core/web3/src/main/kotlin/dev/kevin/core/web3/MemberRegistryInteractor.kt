package dev.kevin.core.web3

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.protocol.klaytn.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.tx.KlayRawTransactionManager
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.StaticGasProvider

/**
 * Interactor for MemberRegistry contract
 */
class MemberRegistryInteractor(
    private val web3j: Web3j = Web3Config.createTestnetWeb3j(),
    private val gasProvider: ContractGasProvider = Web3Config.getDefaultTestnetGasProvider()
) {

    data class MemberRegistrationEvent(
        val userAddress: String,
        val isRegistered: Boolean,
        val transactionHash: String
    )

    /**
     * Check if an address is a registered member
     */
    suspend fun isMember(registryAddress: String, userAddress: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val function = org.web3j.abi.datatypes.Function(
                    "isMember",
                    listOf(Address(userAddress)),
                    listOf(object : TypeReference<Bool>() {})
                )

                val encodedFunction = FunctionEncoder.encode(function)
                val response = web3j.ethCall(
                    Transaction.createEthCallTransaction(null, registryAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST
                ).send()

                if (response.hasError()) {
                    return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
                }

                val decodedResult =
                    FunctionReturnDecoder.decode(response.value, function.outputParameters)
                val isMember = decodedResult[0].value as Boolean

                Result.success(isMember)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Register a new member (only owner can call this)
     */
    suspend fun registerMember(
        registryAddress: String,
        userAddress: String,
        ownerPrivateKey: String
    ): Result<MemberRegistrationEvent> = withContext(Dispatchers.IO) {
        try {
            val credentials = Web3Config.createCredentials(ownerPrivateKey)
            val from = credentials.address
            val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST)
                .send().transactionCount
            val gasProvider = StaticGasProvider(gasProvider.gasPrice, gasProvider.gasLimit)
            val transactionManager = KlayRawTransactionManager(web3j, credentials, Web3Config.KAIA_TESTNET_CHAIN_ID)

            /**
             * val transactionManager = RawTransactionManager(web3j, credentials, Web3Config.KAIA_TESTNET_CHAIN_ID)
             * val transactionReceipt = transactionManager.sendTransaction(
            gasPrice,
            gasLimit,
            registryAddress,
            encodedFunction,
            BigInteger.ZERO
            )
             */

            val function = Function(
                "register",
                listOf(Address(userAddress)),
                emptyList()
            )

            val encodedFunction = FunctionEncoder.encode(function)

            val transactionReceipt = transactionManager.sendTransaction(
                gasProvider.gasPrice,
                gasProvider.gasLimit,
                registryAddress,
                encodedFunction,
                BigInteger.ZERO
            )

            val receipt = transactionReceipt
                ?: return@withContext Result.failure(Exception("Transaction failed"))

            val registrationEvent = MemberRegistrationEvent(
                userAddress = userAddress,
                isRegistered = true,
                transactionHash = receipt.transactionHash
            )

            Result.success(registrationEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unregister a member (only owner can call this)
     */
    suspend fun unregisterMember(
        registryAddress: String,
        userAddress: String,
        ownerPrivateKey: String
    ): Result<MemberRegistrationEvent> = withContext(Dispatchers.IO) {
        try {
            val credentials = Web3Config.createCredentials(ownerPrivateKey)
            val transactionManager =
                KlayRawTransactionManager(web3j, credentials, Web3Config.KAIA_TESTNET_CHAIN_ID)

            val function = Function(
                "unregister",
                listOf(Address(userAddress)),
                emptyList()
            )

            val encodedFunction = FunctionEncoder.encode(function)

            val gasPrice = gasProvider.gasPrice
            val gasLimit = BigInteger.valueOf(100_000L)

            val transactionReceipt = transactionManager.sendTransaction(
                gasPrice,
                gasLimit,
                registryAddress,
                encodedFunction,
                BigInteger.ZERO
            )

            val receipt = transactionReceipt
                ?: return@withContext Result.failure(Exception("Transaction failed"))

            val registrationEvent = MemberRegistrationEvent(
                userAddress = userAddress,
                isRegistered = false,
                transactionHash = receipt.transactionHash
            )

            Result.success(registrationEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the owner of the registry contract
     */
    suspend fun getOwner(registryAddress: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val function = Function(
                "owner",
                emptyList(),
                listOf(object : TypeReference<Address>() {})
            )

            val encodedFunction = FunctionEncoder.encode(function)
            val response = web3j.ethCall(
                Transaction.createEthCallTransaction(null, registryAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            if (response.hasError()) {
                return@withContext Result.failure(Exception("Contract call failed: ${response.error.message}"))
            }

            val decodedResult =
                FunctionReturnDecoder.decode(response.value, function.outputParameters)
            val owner = decodedResult[0].value as String

            Result.success(owner)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}