package dev.kevin.core.web3

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for ContractAbiLoader using kotlinx.serialization
 */
class ContractAbiLoaderTest {
    
    @Test
    fun `test load donation ABI from resources`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        
        assertNotNull(abiJson)
        assertTrue(abiJson.isNotEmpty())
        assertTrue(abiJson.contains("getSummary"))
        assertTrue(abiJson.contains("vote"))
        assertTrue(abiJson.contains("donate"))
    }
    
    @Test
    fun `test parse ABI JSON with kotlinx serialization`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        val contractAbi = ContractAbiLoader.parseAbi(abiJson)
        
        assertNotNull(contractAbi)
        assertTrue(contractAbi.abi.isNotEmpty())
        
        // Should contain multiple types of ABI entries
        val types = contractAbi.abi.map { it.type }.distinct()
        assertTrue(types.contains("function"))
        assertTrue(types.contains("constructor"))
        assertTrue(types.contains("event"))
    }
    
    @Test
    fun `test get specific function from ABI`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        
        // Test getting getSummary function
        val getSummaryFunction = ContractAbiLoader.getFunction(abiJson, "getSummary")
        assertNotNull(getSummaryFunction)
        assertEquals("getSummary", getSummaryFunction.name)
        assertEquals("function", getSummaryFunction.type)
        assertTrue(getSummaryFunction.outputs.isNotEmpty())
        
        // Test getting vote function
        val voteFunction = ContractAbiLoader.getFunction(abiJson, "vote")
        assertNotNull(voteFunction)
        assertEquals("vote", voteFunction.name)
        assertEquals("function", voteFunction.type)
        assertEquals("payable", voteFunction.stateMutability)
        
        // Test getting donate function
        val donateFunction = ContractAbiLoader.getFunction(abiJson, "donate")
        assertNotNull(donateFunction)
        assertEquals("donate", donateFunction.name)
        assertEquals("function", donateFunction.type)
        assertEquals("payable", donateFunction.stateMutability)
    }
    
    @Test
    fun `test get all functions from ABI`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        val functions = ContractAbiLoader.getFunctions(abiJson)
        
        assertTrue(functions.isNotEmpty())
        
        // Check that all returned items are functions
        assertTrue(functions.all { it.type == "function" })
        
        // Check for specific expected functions
        val functionNames = functions.map { it.name }
        assertTrue(functionNames.contains("getSummary"))
        assertTrue(functionNames.contains("vote"))
        assertTrue(functionNames.contains("donate"))
        assertTrue(functionNames.contains("STAKE_AMOUNT"))
        assertTrue(functionNames.contains("hasVoted"))
    }
    
    @Test
    fun `test get events from ABI`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        val events = ContractAbiLoader.getEvents(abiJson)
        
        assertTrue(events.isNotEmpty())
        
        // Check that all returned items are events
        assertTrue(events.all { it.type == "event" })
        
        // Check for ProposalCreated event
        val eventNames = events.map { it.name }
        assertTrue(eventNames.contains("ProposalCreated"))
    }
    
    @Test
    fun `test hasFunction utility`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        
        assertTrue(ContractAbiLoader.hasFunction(abiJson, "getSummary"))
        assertTrue(ContractAbiLoader.hasFunction(abiJson, "vote"))
        assertTrue(ContractAbiLoader.hasFunction(abiJson, "donate"))
        assertFalse(ContractAbiLoader.hasFunction(abiJson, "nonExistentFunction"))
    }
    
    @Test
    fun `test function signature generation`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        
        val voteFunction = ContractAbiLoader.getFunction(abiJson, "vote")
        assertNotNull(voteFunction)
        
        val signature = ContractAbiLoader.getFunctionSignature(voteFunction)
        assertEquals("vote(bool)", signature)
        
        val getSummaryFunction = ContractAbiLoader.getFunction(abiJson, "getSummary")
        assertNotNull(getSummaryFunction)
        
        val getSummarySignature = ContractAbiLoader.getFunctionSignature(getSummaryFunction)
        assertEquals("getSummary()", getSummarySignature)
    }
    
    @Test
    fun `test get function names`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        val functionNames = ContractAbiLoader.getFunctionNames(abiJson)
        
        assertTrue(functionNames.isNotEmpty())
        assertTrue(functionNames.contains("getSummary"))
        assertTrue(functionNames.contains("vote"))
        assertTrue(functionNames.contains("donate"))
        
        // Should not contain empty names
        assertFalse(functionNames.contains(""))
    }
    
    @Test
    fun `test get contract info`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        val contractInfo = ContractAbiLoader.getContractInfo(abiJson)
        
        assertTrue(contractInfo.totalFunctions > 0)
        assertTrue(contractInfo.totalEvents > 0)
        assertTrue(contractInfo.totalConstructors > 0)
        
        // Check function categorization
        assertTrue(contractInfo.readOnlyFunctions.isNotEmpty())
        assertTrue(contractInfo.payableFunctions.isNotEmpty())
        
        // Specific checks for donation contract
        assertTrue(contractInfo.functionNames.contains("getSummary"))
        assertTrue(contractInfo.payableFunctions.contains("vote"))
        assertTrue(contractInfo.payableFunctions.contains("donate"))
        assertTrue(contractInfo.readOnlyFunctions.contains("STAKE_AMOUNT"))
        
        assertTrue(contractInfo.eventNames.contains("ProposalCreated"))
    }
    
    @Test
    fun `test ABI parameter parsing`() {
        val abiJson = ContractAbiLoader.loadDonationAbi()
        
        val voteFunction = ContractAbiLoader.getFunction(abiJson, "vote")
        assertNotNull(voteFunction)
        
        // Vote function should have one input parameter (approve: bool)
        assertEquals(1, voteFunction.inputs.size)
        val approveParam = voteFunction.inputs.first()
        assertEquals("approve", approveParam.name)
        assertEquals("bool", approveParam.type)
        assertEquals("bool", approveParam.internalType)
        
        val getSummaryFunction = ContractAbiLoader.getFunction(abiJson, "getSummary")
        assertNotNull(getSummaryFunction)
        
        // getSummary should have multiple outputs
        assertTrue(getSummaryFunction.outputs.size > 5)
        
        // Check some specific outputs
        val outputs = getSummaryFunction.outputs
        assertTrue(outputs.any { it.type == "address" })
        assertTrue(outputs.any { it.type == "string" })
        assertTrue(outputs.any { it.type == "uint256" })
        assertTrue(outputs.any { it.type == "bool" })
    }
}