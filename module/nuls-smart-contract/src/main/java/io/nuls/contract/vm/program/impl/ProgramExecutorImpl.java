/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.program.impl;

import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.dto.BlockHeaderDto;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.code.ClassCodes;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeUtils;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.util.Constants;
import io.nuls.core.crypto.HexUtil;
import org.apache.commons.lang3.StringUtils;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.StateSource;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.constant.ContractConstant.BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC;
import static io.nuls.contract.constant.ContractConstant.BALANCE_TRIGGER_METHOD_NAME;

public class ProgramExecutorImpl implements ProgramExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProgramExecutorImpl.class);

    private final ProgramExecutorImpl parent;

    private final VMContext vmContext;

    private final Source<byte[], byte[]> source;

    private final Repository repository;

    private final byte[] prevStateRoot;

    private final long beginTime;

    private final Map<ByteArrayWrapper, ProgramAccount> accounts;

    private long blockNumber;

    private long currentTime;

    private boolean revert;

    private Chain chain;

    private final Thread thread;

    public ProgramExecutorImpl(VMContext vmContext, Chain chain) {
        this(null, vmContext, stateSource(chain), null, null, null, null);
        this.chain = chain;
    }

    private ProgramExecutorImpl(ProgramExecutorImpl programExecutor, VMContext vmContext, Source<byte[], byte[]> source, Repository repository, byte[] prevStateRoot,
                                Map<ByteArrayWrapper, ProgramAccount> accounts, Thread thread) {
        this.parent = programExecutor;
        this.vmContext = vmContext;
        this.source = source;
        this.repository = repository;
        this.prevStateRoot = prevStateRoot;
        this.beginTime = this.currentTime = System.currentTimeMillis();
        this.accounts = accounts;
        this.thread = thread;
    }

    public ProgramExecutor callProgramExecutor() {
        ProgramExecutorImpl programExecutor = new ProgramExecutorImpl(this, vmContext, source, repository, prevStateRoot, accounts, thread);
        // add by pierre at 2019-12-03 用于当存在合约内部调用合约，共享同一个合约的内存数据
        programExecutor.contractObjects = this.contractObjects;
        programExecutor.contractChanges = this.contractChanges;
        programExecutor.contractArrays = this.contractArrays;
        programExecutor.contractObjectRefCount = this.contractObjectRefCount;
        // end code by pierre
        return programExecutor;
    }

    @Override
    public int getCurrentChainId() {
        Chain c = getCurrentChain();
        if (c != null) {
            return c.getChainId();
        }
        return 0;
    }

    private Chain getCurrentChain() {
        ProgramExecutorImpl programExecutor = this;
        while (programExecutor.chain == null) {
            programExecutor = programExecutor.parent;
            if (programExecutor == null) {
                break;
            }
        }
        if (programExecutor != null) {
            return programExecutor.chain;
        }
        return null;
    }

    @Override
    public ProgramExecutor begin(byte[] prevStateRoot) {
        if (log.isDebugEnabled()) {
            log.debug("begin vm root: {}", HexUtil.encode(prevStateRoot));
        }
        Repository repository = new RepositoryRoot(source, prevStateRoot);
        return new ProgramExecutorImpl(this, vmContext, source, repository, prevStateRoot, new HashMap<>(), Thread.currentThread());
    }

    @Override
    public ProgramExecutor startTracking() {
        checkThread();
        if (log.isDebugEnabled()) {
            log.debug("startTracking");
        }
        Repository track = repository.startTracking();
        return new ProgramExecutorImpl(this, vmContext, source, track, null, new HashMap<>(), thread);
    }

    @Override
    public void commit() {
        checkThread();
        if (!revert) {
            repository.commit();
            if (prevStateRoot == null) {
                if (parent.blockNumber == 0) {
                    parent.blockNumber = blockNumber;
                }
                if (parent.blockNumber != blockNumber) {
                    throw new RuntimeException(String.format("must use the same block number, parent blockNumber is [%s], this blockNumber is [%s]", parent.blockNumber, blockNumber));
                }
            } else {
                if (vmContext != null) {
                    BlockHeaderDto blockHeaderDto;
                    try {
                        blockHeaderDto = vmContext.getBlockHeader(getCurrentChainId(), blockNumber);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    byte[] parentHash = HexUtil.decode(blockHeaderDto.getPreHash());
                    byte[] hash = HexUtil.decode(blockHeaderDto.getHash());
                    Block block = new Block(parentHash, hash, blockNumber);
                    getCurrentChain().getDefaultConfig().blockStore().saveBlock(block, BigInteger.ONE, true);
                    getCurrentChain().getDefaultConfig().pruneManager().blockCommitted(block.getHeader());
                }
                getCurrentChain().getCommonConfig().dbFlushManager().flush();
            }
            logTime("commit");
        }
    }

    public String getCrossTokenSystemContract() {
        return vmContext.getCrossTokenSystemContract();
    }

    @Override
    public byte[] getRoot() {
        checkThread();
        byte[] root;
        if (!revert) {
            root = repository.getRoot();
        } else {
            root = this.prevStateRoot;
        }
        if (log.isDebugEnabled()) {
            log.debug("end vm root: {}, runtime: {}", HexUtil.encode(root), System.currentTimeMillis() - beginTime);
        }
        return root;
    }

    @Override
    public ProgramResult create(ProgramCreate programCreate) {
        checkThread();
        ProgramInvoke programInvoke = new ProgramInvoke();
        programInvoke.setContractAddress(programCreate.getContractAddress());
        programInvoke.setAddress(NativeAddress.toString(programInvoke.getContractAddress()));
        programInvoke.setSender(programCreate.getSender());
        programInvoke.setPrice(programCreate.getPrice());
        programInvoke.setGasLimit(programCreate.getGasLimit());
        programInvoke.setValue(programCreate.getValue() != null ? programCreate.getValue() : BigInteger.ZERO);
        programInvoke.setNumber(programCreate.getNumber());
        programInvoke.setData(programCreate.getContractCode());
        programInvoke.setMethodName("<init>");
        programInvoke.setArgs(programCreate.getArgs() != null ? programCreate.getArgs() : new String[0][0]);
        programInvoke.setEstimateGas(programCreate.isEstimateGas());
        programInvoke.setCreate(true);
        programInvoke.setInternalCall(false);
        programInvoke.setViewMethod(false);
        programInvoke.setSenderPublicKey(programCreate.getSenderPublicKey());
        return execute(programInvoke);
    }

    @Override
    public ProgramResult call(ProgramCall programCall) {
        checkThread();
        ProgramInvoke programInvoke = new ProgramInvoke();
        programInvoke.setContractAddress(programCall.getContractAddress());
        programInvoke.setAddress(NativeAddress.toString(programInvoke.getContractAddress()));
        programInvoke.setSender(programCall.getSender());
        programInvoke.setPrice(programCall.getPrice());
        programInvoke.setGasLimit(programCall.getGasLimit());
        programInvoke.setValue(programCall.getValue() != null ? programCall.getValue() : BigInteger.ZERO);
        programInvoke.setNumber(programCall.getNumber());
        programInvoke.setMethodName(programCall.getMethodName());
        programInvoke.setMethodDesc(programCall.getMethodDesc());
        programInvoke.setArgs(programCall.getArgs() != null ? programCall.getArgs() : new String[0][0]);
        programInvoke.setEstimateGas(programCall.isEstimateGas());
        programInvoke.setCreate(false);
        programInvoke.setInternalCall(programCall.isInternalCall());
        programInvoke.setViewMethod(programCall.isViewMethod());
        programInvoke.setSenderPublicKey(programCall.getSenderPublicKey());
        return execute(programInvoke);
    }

    private Map<String, Map<ObjectRef, Map<String, Object>>> contractObjects;
    private Map<String, Set<ObjectRef>> contractChanges;
    private Map<String, Map<String, Object>> contractArrays;
    private Map<String, BigIntegerWrapper> contractObjectRefCount;

    private ProgramResult execute(ProgramInvoke programInvoke) {
        if (programInvoke.getPrice() < 1) {
            return revert("gas price must be greater than zero");
        }
        if (programInvoke.getGasLimit() < 1) {
            return revert("gas must be greater than zero");
        }

        long maxGas;
        if (programInvoke.isViewMethod()) {
            maxGas = vmContext.getCustomMaxViewGasLimit(getCurrentChainId());
        } else {
            maxGas = VM.MAX_GAS;
        }
        if (programInvoke.getGasLimit() > maxGas) {
            return revert("gas must be less than " + maxGas);
        }
        if (programInvoke.getValue().compareTo(BigInteger.ZERO) < 0) {
            return revert("value can't be less than zero");
        }
        blockNumber = programInvoke.getNumber();

        logTime("start");

        VM vm = null;
        try {
            byte[] contractAddressBytes = programInvoke.getContractAddress();
            byte[] sender = programInvoke.getSender();
            String contractAddress = programInvoke.getAddress();
            String methodName = programInvoke.getMethodName();
            String methodDescBase = programInvoke.getMethodDesc();
            byte[] contractCodeData = programInvoke.getData();
            BigInteger transferValue = programInvoke.getValue();
            Map<String, ClassCode> classCodes;
            if (programInvoke.isCreate()) {
                if (contractCodeData == null) {
                    return revert("contract code can't be null");
                }
                classCodes = ClassCodeLoader.loadJarCache(contractCodeData);
                logTime("load new code");
                ProgramChecker.check(classCodes);
                logTime("check code");
                AccountState accountState = repository.getAccountState(contractAddressBytes);
                if (accountState != null) {
                    return revert(String.format("contract[%s] already exists", contractAddress));
                }
                accountState = repository.createAccount(contractAddressBytes, sender);
                logTime("new account state");
                repository.saveCode(contractAddressBytes, contractCodeData);
                logTime("save code");
            } else {
                if ("<init>".equals(methodName)) {
                    return revert("can't invoke <init> method");
                }
                AccountState accountState = repository.getAccountState(contractAddressBytes);
                if (accountState == null) {
                    return revert(String.format("contract[%s] does not exist", contractAddress));
                }
                logTime("load account state");
                if (accountState.getNonce().compareTo(BigInteger.ZERO) <= 0) {
                    return revert(String.format("contract[%s] has stopped", contractAddress));
                }
                byte[] codes = repository.getCode(contractAddressBytes);
                classCodes = ClassCodeLoader.loadJarCache(codes);
                logTime("load code");
            }


            vm = VMFactory.createVM();
            logTime("load vm");

            vm.setProgramExecutor(this);
            vm.heap.loadClassCodes(classCodes);
            // add by pierre at 2019-11-21 标记 当存在合约内部调用合约，共享同一个合约的内存数据 需要协议升级 done
            //Log.debug("++++++++++++++++++++");
            //Log.warn(programInvoke.toString());
            //Log.info("this.contractObjectRefCount: {}", this.contractObjectRefCount);
            //Log.info("vm.heap.objectRefCount: {}", vm.heap.objectRefCount);
            boolean isUpgradedV240 = ProtocolGroupManager.getCurrentVersion(getCurrentChainId()) >= ContractContext.UPDATE_VERSION_V240;
            if(isUpgradedV240) {
                if(contractObjects == null) {
                    contractObjects = new HashMap<>();
                    contractObjects.put(contractAddress, vm.heap.objects);
                } else {
                    Map<ObjectRef, Map<String, Object>> objectRefMapMap = contractObjects.get(contractAddress);
                    if(objectRefMapMap != null) {
                        if(programInvoke.isInternalCall()) {
                            //Log.info("共享heap.objects");
                            vm.heap.objects = objectRefMapMap;
                        }
                    } else {
                        contractObjects.put(contractAddress, vm.heap.objects);
                    }
                }
                if(contractArrays == null) {
                    contractArrays = new HashMap<>();
                    contractArrays.put(contractAddress, vm.heap.arrays);
                } else {
                    Map<String, Object> arraysMap = contractArrays.get(contractAddress);
                    if(arraysMap != null) {
                        if(programInvoke.isInternalCall()) {
                            //Log.info("共享heap.arrays");
                            vm.heap.arrays = arraysMap;
                        }
                    } else {
                        contractArrays.put(contractAddress, vm.heap.arrays);
                    }
                }
                if(contractChanges == null) {
                    //Log.info("新建map和heap.changes");
                    contractChanges = new HashMap<>();
                    contractChanges.put(contractAddress, vm.heap.changes);
                } else {
                    Set<ObjectRef> changesObjectRefs = contractChanges.get(contractAddress);
                    if(changesObjectRefs != null) {
                        if(programInvoke.isInternalCall()) {
                            //Log.info("共享heap.changes: {}", changesObjectRefs.hashCode());
                            vm.heap.changes = changesObjectRefs;
                        }
                    } else {
                        //Log.info("新增heap.changes");
                        contractChanges.put(contractAddress, vm.heap.changes);
                    }
                }
            }
            // end code by pierre
            vm.methodArea.loadClassCodes(classCodes);

            logTime("load classes");

            ClassCode contractClassCode = getContractClassCode(classCodes);
            String methodDesc = ProgramDescriptors.parseDesc(methodDescBase);
            MethodCode methodCode = vm.methodArea.loadMethod(contractClassCode.name, methodName, methodDesc);

            if (methodCode == null) {
                return revert(String.format("can't find method %s%s", methodName, methodDescBase == null ? "" : methodDescBase));
            }
            if (!methodCode.isPublic) {
                return revert("can only invoke public method");
            }
            if (!methodCode.hasPayableAnnotation() && transferValue.compareTo(BigInteger.ZERO) > 0) {
                return revert(String.format("contract[%s]'s method[%s] is not a payable method", contractAddress, methodCode.name));
            }
            // 不允许非系统调用此方法
            boolean isBalanceTriggerForConsensusContractMethod = BALANCE_TRIGGER_METHOD_NAME.equals(methodName) &&
                    BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC.equals(methodDescBase);
            if (isBalanceTriggerForConsensusContractMethod) {
                if (sender != null) {
                    return revert("can't invoke _payable(String[][] args) method");
                }
            }
            if (methodCode.argsVariableType.size() != programInvoke.getArgs().length) {
                do {
                    if (isBalanceTriggerForConsensusContractMethod && programInvoke.getArgs().length > 0) {
                        break;
                    }
                    return revert(String.format("require %s parameters in method [%s%s]",
                            methodCode.argsVariableType.size(), methodCode.name, methodCode.normalDesc));
                } while (false);
            }

            logTime("load method");

            ObjectRef objectRef;
            if (programInvoke.isCreate()) {
                objectRef = vm.heap.newContract(contractAddressBytes, contractClassCode, repository);
            } else {
                objectRef = vm.heap.loadContract(contractAddressBytes, contractClassCode, repository);
            }

            // add by pierre at 2019-11-21 标记 当存在合约内部调用合约，共享同一个合约的内存数据 需要协议升级 done
            if(isUpgradedV240) {
                if(contractObjectRefCount == null) {
                    //Log.info("新建map和heap.objectRefCount");
                    contractObjectRefCount = new HashMap<>();
                    contractObjectRefCount.put(contractAddress, vm.heap.objectRefCount);
                } else {
                    BigIntegerWrapper objectRefCount = contractObjectRefCount.get(contractAddress);
                    if(objectRefCount != null) {
                        if(programInvoke.isInternalCall()) {
                            //Log.info("共享heap.objectRefCount: {}", objectRefCount.hashCode());
                            vm.heap.objectRefCount = objectRefCount;
                        }
                    } else {
                        //Log.info("新增heap.objectRefCount");
                        contractObjectRefCount.put(contractAddress, vm.heap.objectRefCount);
                    }
                }
            }
            // end code by pierre

            logTime("load contract ref");

            if (transferValue.compareTo(BigInteger.ZERO) > 0) {
                getAccount(contractAddressBytes).addBalance(transferValue);
            }
            vm.setRepository(repository);
            vm.setGas(programInvoke.getGasLimit());
            vm.addGasUsed(contractCodeData == null ? 0 : contractCodeData.length);

            logTime("load end");

            vm.run(objectRef, methodCode, vmContext, programInvoke);

            logTime("run");

            ProgramResult programResult = new ProgramResult();
            programResult.setGasUsed(vm.getGasUsed());
            programResult.setDebugEvents(vm.getDebugEvents());

            Result vmResult = vm.getResult();
            Object resultValue = vmResult.getValue();
            if (vmResult.isError() || vmResult.isException()) {
                if (resultValue != null && resultValue instanceof ObjectRef) {
                    vm.setResult(new Result());
                    String error = vm.heap.runToString((ObjectRef) resultValue);
                    String stackTrace = vm.heap.stackTrace((ObjectRef) resultValue);
                    programResult.error(error);
                    programResult.setStackTrace(stackTrace);
                } else {
                    programResult.error(null);
                }

                logTime("contract exception");

                this.revert = true;

                programResult.setGasUsed(vm.getGasUsed());

                return programResult;
            }

            programResult.setTransfers(vm.getTransfers());
            programResult.setInternalCalls(vm.getInternalCalls());
            programResult.setEvents(vm.getEvents());
            programResult.setInvokeRegisterCmds(vm.getInvokeRegisterCmds());
            programResult.setOrderedInnerTxs(vm.getOrderedInnerTxs());

            if (resultValue != null) {
                if (resultValue instanceof ObjectRef) {
                    String result;
                    if(methodCode.hasJSONSerializableAnnotation()) {
                        result = NativeUtils.objectRef2Json((ObjectRef) resultValue, vm.heap, vm.methodArea);
                    } else {
                        result = vm.heap.runToString((ObjectRef) resultValue);
                    }
                    programResult.setResult(result);
                } else {
                    programResult.setResult(resultValue.toString());
                }
            }

            if (methodCode.isPublic && methodCode.hasViewAnnotation()) {
                this.revert = true;
                programResult.view();
                programResult.setGasUsed(vm.getGasUsed());
                return programResult;
            }

            logTime("contract return");

            Map<DataWord, DataWord> contractState = vm.heap.contractState();
            logTime("contract state");

            //Log.error(programInvoke.toString());
            for (Map.Entry<DataWord, DataWord> entry : contractState.entrySet()) {
                DataWord key = entry.getKey();
                DataWord value = entry.getValue();
                //Log.info("add storage row, key: {}, value: {}", key.asString(), value.asString());
                repository.addStorageRow(contractAddressBytes, key, value);
            }
            //Log.debug("---------------------\n");
            logTime("add contract state");

            if (programInvoke.isCreate()) {
                repository.setNonce(contractAddressBytes, BigInteger.ONE);
            }
            programResult.setGasUsed(vm.getGasUsed());
            // 当合约用到nonce时，维护了临时nonce
            programResult.setAccounts(accounts);

            return programResult;
        } catch (ErrorException e) {
            this.revert = true;
            ProgramResult programResult = new ProgramResult();
            programResult.setGasUsed(e.getGasUsed());
            logTime("error");
            if(vm != null) {
                programResult.setDebugEvents(vm.getDebugEvents());
            }
            return programResult.error(e.getMessage());
        } catch (Exception e) {
            Log.error(e);
            ProgramResult programResult = revert(e.getMessage());
            return programResult;
        }
    }

    private ProgramResult revert(String errorMessage) {
        return revert(errorMessage, null);
    }

    private ProgramResult revert(String errorMessage, String stackTrace) {
        this.revert = true;
        ProgramResult programResult = new ProgramResult();
        programResult.setStackTrace(stackTrace);
        logTime("revert");
        return programResult.revert(errorMessage);
    }

    @Override
    public ProgramResult stop(long blockNumber, byte[] address, byte[] sender) {
        checkThread();
        AccountState accountState = repository.getAccountState(address);
        if (accountState == null) {
            return revert("can't find contract");
        }
        if (!FastByteComparisons.equal(sender, accountState.getOwner())) {
            return revert("only the owner can stop the contract");
        }
        BigInteger balance = getTotalBalance(address, null);
        if (BigInteger.ZERO.compareTo(balance) != 0) {
            return revert("contract balance is not zero");
        }
        if (BigInteger.ZERO.compareTo(accountState.getNonce()) >= 0) {
            return revert("contract has stopped");
        }

        this.blockNumber = blockNumber;
        repository.setNonce(address, BigInteger.ZERO);

        ProgramResult programResult = new ProgramResult();

        return programResult;
    }

    @Override
    public ProgramStatus status(byte[] address) {
        checkThread();
        this.revert = true;
        AccountState accountState = repository.getAccountState(address);
        if (accountState == null) {
            return ProgramStatus.not_found;
        } else {
            BigInteger nonce = repository.getNonce(address);
            if (BigInteger.ZERO.compareTo(nonce) >= 0) {
                return ProgramStatus.stop;
            } else {
                return ProgramStatus.normal;
            }
        }
    }

    public ProgramAccount getAccount(byte[] address) {
        ByteArrayWrapper addressWrapper = new ByteArrayWrapper(address);
        ProgramAccount account = accounts.get(addressWrapper);
        if (account == null) {
            BigInteger balance;
            BigInteger freeze;
            String nonce = null;
            ContractBalance contractBalance = getBalance(address);
            if (contractBalance != null) {
                balance = contractBalance.getBalance();
                freeze = contractBalance.getFreeze();
                nonce = contractBalance.getNonce();
            } else {
                balance = BigInteger.ZERO;
                freeze = BigInteger.ZERO;
            }
            account = new ProgramAccount(address, balance, nonce);
            account.setFreeze(freeze);
            accounts.put(addressWrapper, account);
        }
        return account;
    }

    private ContractBalance getBalance(byte[] address) {
        ContractBalance contractBalance = null;
        if (vmContext != null) {
            contractBalance = vmContext.getBalance(getCurrentChainId(), address);
        }
        return contractBalance;
    }

    private BigInteger getTotalBalance(byte[] address, Long blockNumber) {
        BigInteger balance = BigInteger.ZERO;
        if (vmContext != null) {
            balance = vmContext.getTotalBalance(getCurrentChainId(), address);
        }
        return balance;
    }

    @Override
    public List<ProgramMethod> method(byte[] address) {
        checkThread();
        this.revert = true;
        byte[] codes = repository.getCode(address);
        return jarMethod(codes);
    }

    @Override
    public List<ProgramMethod> jarMethod(byte[] jarData) {
        this.revert = true;
        if (jarData == null || jarData.length < 1) {
            return new ArrayList<>();
        }
        Map<String, ClassCode> classCodes = ClassCodeLoader.loadJarCache(jarData);
        return getProgramMethods(classCodes);
    }

    private void checkThread() {
        //if (thread == null) {
        //    throw new RuntimeException("must use the begin method");
        //}
        //Thread currentThread = Thread.currentThread();
        //if (!currentThread.equals(thread)) {
        //    throw new RuntimeException(String.format("method must be executed in %s, current %s", thread, currentThread));
        //}
    }

    private static List<ProgramMethod> getProgramMethods(Map<String, ClassCode> classCodes) {
        List<ProgramMethod> programMethods = getProgramMethodCodes(classCodes).stream().map(methodCode -> {
            ProgramMethod method = new ProgramMethod();
            method.setName(methodCode.name);
            method.setDesc(methodCode.normalDesc);
            method.setArgs(methodCode.args);
            method.setReturnArg(methodCode.returnArg);
            method.setView(methodCode.hasViewAnnotation());
            method.setPayable(methodCode.hasPayableAnnotation());
            method.setJsonSerializable(methodCode.hasJSONSerializableAnnotation());
            method.setEvent(false);
            return method;
        }).collect(Collectors.toList());
        programMethods.addAll(getEventConstructor(classCodes));
        return programMethods;
    }

    public static List<MethodCode> getProgramMethodCodes(Map<String, ClassCode> classCodes) {
        Map<String, MethodCode> methodCodes = new LinkedHashMap<>();
        ClassCode contractClassCode = getContractClassCode(classCodes);
        if (contractClassCode != null) {
            contractMethods(methodCodes, classCodes, contractClassCode, false);
        }
        return methodCodes.values().stream().collect(Collectors.toList());
    }

    private static ClassCode getContractClassCode(Map<String, ClassCode> classCodes) {
        return classCodes.values().stream().filter(classCode -> classCode.interfaces.contains(ProgramConstants.CONTRACT_INTERFACE_NAME)).findFirst().orElse(null);
    }

    private static void contractMethods(Map<String, MethodCode> methodCodes, Map<String, ClassCode> classCodes, ClassCode classCode, boolean isSupperClass) {
        classCode.methods.stream().filter(methodCode -> {
            if (methodCode.isPublic && !methodCode.isAbstract) {
                return true;
            } else {
                return false;
            }
        }).forEach(methodCode -> {
            if (isSupperClass && Constants.CONSTRUCTOR_NAME.equals(methodCode.name)) {
            } else if (Constants.CLINIT_NAME.equals(methodCode.name)) {
            } else {
                String name = methodCode.name + "." + methodCode.desc;
                methodCodes.putIfAbsent(name, methodCode);
            }
        });
        String superName = classCode.superName;
        if (StringUtils.isNotEmpty(superName)) {
            classCodes.values().stream().filter(code -> superName.equals(code.name)).findFirst()
                    .ifPresent(code -> {
                        contractMethods(methodCodes, classCodes, code, true);
                    });
        }
    }

    private static Set<ProgramMethod> getEventConstructor(Map<String, ClassCode> classCodes) {
        Map<String, MethodCode> methodCodes = new LinkedHashMap<>();
        getEventClassCodes(classCodes).forEach(classCode -> {
            for (MethodCode methodCode : classCode.methods) {
                if (methodCode.isConstructor) {
                    methodCodes.put(methodCode.fullName, methodCode);
                }
            }
        });
        return methodCodes.values().stream()
                .filter(methodCode -> methodCode.isConstructor)
                .map(methodCode -> {
                    ProgramMethod method = new ProgramMethod();
                    method.setName(methodCode.classCode.simpleName);
                    method.setDesc(methodCode.normalDesc);
                    method.setArgs(methodCode.args);
                    method.setReturnArg(methodCode.returnArg);
                    method.setView(false);
                    method.setPayable(false);
                    method.setJsonSerializable(false);
                    method.setEvent(true);
                    return method;
                }).collect(Collectors.toSet());
    }

    private static List<ClassCode> getEventClassCodes(Map<String, ClassCode> classCodes) {
        ClassCodes allCodes = new ClassCodes(classCodes);
        return classCodes.values().stream().filter(classCode -> !classCode.isAbstract
                && allCodes.instanceOf(classCode, ProgramConstants.EVENT_INTERFACE_NAME))
                .collect(Collectors.toList());
    }

    private static Source<byte[], byte[]> stateSource(Chain chain) {
        SystemProperties config = SystemProperties.getDefault();
        CommonConfig commonConfig = CommonConfig.newInstance(chain.getChainId());
        chain.setCommonConfig(commonConfig);
        DefaultConfig defaultConfig = DefaultConfig.newInstance(commonConfig);
        chain.setDefaultConfig(defaultConfig);
        StateSource stateSource = commonConfig.stateSource();
        stateSource.setConfig(config);
        stateSource.setCommonConfig(commonConfig);
        return stateSource;
    }

    public void logTime(String message) {
        if (log.isDebugEnabled()) {
            long currentTime = System.currentTimeMillis();
            long step = currentTime - this.currentTime;
            long runtime = currentTime - this.beginTime;
            this.currentTime = currentTime;
            ProgramTime.cache.putIfAbsent(message, new ProgramTime());
            ProgramTime time = ProgramTime.cache.get(message);
            time.add(step);
            log.debug("[{}] runtime: {}ms, step: {}ms, {}", message, runtime, step, time);
        }
//        if (step > 100) {
//            List<String> list = new ArrayList<>();
//            list.add(String.format("%s, runtime: %sms, step: %sms", message, runtime, step));
//            try {
//                FileUtils.writeLines(new File("/tmp/long.log"), list, true);
//            } catch (IOException e) {
//                log.error("", e);
//            }
//        }
    }

}
