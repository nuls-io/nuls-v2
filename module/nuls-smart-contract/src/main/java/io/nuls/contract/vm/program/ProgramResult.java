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
package io.nuls.contract.vm.program;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ethereum.db.ByteArrayWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgramResult {

    private long gasUsed;

    private String result;

    private boolean revert;

    private boolean error;

    private String errorMessage;

    private String stackTrace;

    private String nonce;
    private Map<ByteArrayWrapper, ProgramAccount> accounts;

    private List<ProgramTransfer> transfers = new ArrayList<>();

    private List<ProgramInternalCall> internalCalls = new ArrayList<>();

    private List<String> events = new ArrayList<>();

    private List<ProgramInvokeRegisterCmd> invokeRegisterCmds = new ArrayList<>();

    public ProgramResult revert(String errorMessage) {
        this.revert = true;
        this.errorMessage = errorMessage;
        return this;
    }

    public ProgramResult error(String errorMessage) {
        this.error = true;
        this.errorMessage = errorMessage;
        return this;
    }

    public static ProgramResult getFailed(String errorMessage) {
        ProgramResult result = new ProgramResult();
        return result.error(errorMessage);
    }

    public static ProgramResult getFailed() {
        ProgramResult result = new ProgramResult();
        return result.error(null);
    }

    public void view() {
        this.transfers = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public ProgramResult() {
    }

    public boolean isSuccess() {
        return !error && !revert;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isRevert() {
        return revert;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Map<ByteArrayWrapper, ProgramAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<ByteArrayWrapper, ProgramAccount> accounts) {
        this.accounts = accounts;
    }

    public List<ProgramTransfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<ProgramTransfer> transfers) {
        this.transfers = transfers;
    }

    public List<ProgramInternalCall> getInternalCalls() {
        return internalCalls;
    }

    public void setInternalCalls(List<ProgramInternalCall> internalCalls) {
        this.internalCalls = internalCalls;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public List<ProgramInvokeRegisterCmd> getInvokeRegisterCmds() {
        return invokeRegisterCmds;
    }

    public void setInvokeRegisterCmds(List<ProgramInvokeRegisterCmd> invokeRegisterCmds) {
        this.invokeRegisterCmds = invokeRegisterCmds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ProgramResult)) {
            return false;
        }

        ProgramResult that = (ProgramResult) o;

        return new EqualsBuilder()
                .append(getGasUsed(), that.getGasUsed())
                .append(isRevert(), that.isRevert())
                .append(isError(), that.isError())
                .append(getResult(), that.getResult())
                .append(getErrorMessage(), that.getErrorMessage())
                .append(getStackTrace(), that.getStackTrace())
                .append(getNonce(), that.getNonce())
                .append(getTransfers(), that.getTransfers())
                .append(getInternalCalls(), that.getInternalCalls())
                .append(getEvents(), that.getEvents())
                .append(getInvokeRegisterCmds(), that.getInvokeRegisterCmds())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getGasUsed())
                .append(getResult())
                .append(isRevert())
                .append(isError())
                .append(getErrorMessage())
                .append(getStackTrace())
                .append(getNonce())
                .append(getTransfers())
                .append(getInternalCalls())
                .append(getEvents())
                .append(getInvokeRegisterCmds())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ProgramResult{" +
                "gasUsed=" + gasUsed +
                ", result='" + result + '\'' +
                ", revert=" + revert +
                ", error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", nonce=" + nonce +
                ", transfers=" + transfers +
                ", internalCalls=" + internalCalls +
                ", events=" + events +
                ", invokeRegisterCmds=" + invokeRegisterCmds +
                '}';
    }
}
