/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.provider.model.dto.block;

import io.nuls.base.data.Block;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.provider.model.dto.TransactionDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-06-29
 */
@Data
@NoArgsConstructor
@ApiModel(description = "blockJSON 区块信息(包含区块头信息, 交易信息), 只返回对应的部分数据")
public class BlockDto {

    @ApiModelProperty(description = "区块头信息, 只返回对应的部分数据")
    private BlockHeaderDto header;
    @ApiModelProperty(description = "交易列表", type = @TypeDescriptor(value = List.class, collectionElement = TransactionDto.class))
    private List<TransactionDto> txs;

    public BlockDto(Block block) throws NulsException {
        this.header = new BlockHeaderDto(block.getHeader());
        this.txs = new LinkedList<>();
        List<Transaction> txList = block.getTxs();
        if(txList == null || txList.isEmpty()) {
            return;
        }
        for(Transaction tx : txList) {
            this.txs.add(new TransactionDto(tx));
        }
    }
}
