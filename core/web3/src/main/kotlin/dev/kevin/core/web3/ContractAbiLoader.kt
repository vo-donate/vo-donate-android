package dev.kevin.core.web3

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

/**
 * Utility for loading contract ABI from JSON files using kotlinx.serialization
 */
object ContractAbiLoader {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Serializable
    data class AbiFunction(
        val name: String = "",
        val type: String,
        val inputs: List<AbiParameter> = emptyList(),
        val outputs: List<AbiParameter> = emptyList(),
        val stateMutability: String? = null,
        val anonymous: Boolean? = null
    )
    
    @Serializable
    data class AbiParameter(
        val name: String = "",
        val type: String,
        val internalType: String? = null,
        val indexed: Boolean? = null
    )
    
    @Serializable
    data class ContractAbi(
        val abi: List<AbiFunction>
    )
    
    /**
     * Load ABI from resource file
     */
    private fun loadFromResource(resourcePath: String): String {
        val classLoader = this::class.java.classLoader
            ?: throw IllegalArgumentException("ClassLoader not found")
        val inputStream: InputStream = classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
        
        return inputStream.bufferedReader().use { it.readText() }
    }
    
    /**
     * Load donation contract ABI
     */
    fun loadDonationAbi(): String {
        return loadFromResource("donation_abi.json")
    }
    
    /**
     * Parse ABI JSON to structured format using kotlinx.serialization
     */
    fun parseAbi(abiJson: String): ContractAbi {
        return json.decodeFromString<ContractAbi>(abiJson)
    }
    
    /**
     * Get specific function from ABI
     */
    fun getFunction(abiJson: String, functionName: String): AbiFunction? {
        val contractAbi = parseAbi(abiJson)
        return contractAbi.abi.find { it.name == functionName && it.type == "function" }
    }
    
    /**
     * Get all functions from ABI
     */
    fun getFunctions(abiJson: String): List<AbiFunction> {
        val contractAbi = parseAbi(abiJson)
        return contractAbi.abi.filter { it.type == "function" }
    }
    
    /**
     * Get all events from ABI
     */
    fun getEvents(abiJson: String): List<AbiFunction> {
        val contractAbi = parseAbi(abiJson)
        return contractAbi.abi.filter { it.type == "event" }
    }
    
    /**
     * Check if function exists in ABI
     */
    fun hasFunction(abiJson: String, functionName: String): Boolean {
        return getFunction(abiJson, functionName) != null
    }
    
    /**
     * Get function signature for Web3j
     */
    fun getFunctionSignature(function: AbiFunction): String {
        val paramTypes = function.inputs.joinToString(",") { it.type }
        return "${function.name}($paramTypes)"
    }
    
    /**
     * Get all function names from ABI
     */
    fun getFunctionNames(abiJson: String): List<String> {
        return getFunctions(abiJson).map { it.name }.filter { it.isNotEmpty() }
    }
    
    /**
     * Parse ABI and return detailed contract information
     */
    fun getContractInfo(abiJson: String): ContractInfo {
        val contractAbi = parseAbi(abiJson)
        val functions = contractAbi.abi.filter { it.type == "function" }
        val events = contractAbi.abi.filter { it.type == "event" }
        val constructors = contractAbi.abi.filter { it.type == "constructor" }
        
        return ContractInfo(
            totalFunctions = functions.size,
            totalEvents = events.size,
            totalConstructors = constructors.size,
            functionNames = functions.map { it.name }.filter { it.isNotEmpty() },
            eventNames = events.map { it.name }.filter { it.isNotEmpty() },
            readOnlyFunctions = functions.filter { it.stateMutability == "view" || it.stateMutability == "pure" }.map { it.name },
            payableFunctions = functions.filter { it.stateMutability == "payable" }.map { it.name }
        )
    }
    
    @Serializable
    data class ContractInfo(
        val totalFunctions: Int,
        val totalEvents: Int,
        val totalConstructors: Int,
        val functionNames: List<String>,
        val eventNames: List<String>,
        val readOnlyFunctions: List<String>,
        val payableFunctions: List<String>
    )
}