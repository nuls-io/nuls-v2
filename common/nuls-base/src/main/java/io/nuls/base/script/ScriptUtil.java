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
package io.nuls.base.script;


import java.util.List;
public class ScriptUtil {

    /**
     * Generate unlocking scripts based on transaction signatures and public keys （P2PSH）
     *
     * @param sigByte    Transaction signature
     * @param pubkeyByte Public key
     * @return Script      Generate unlock script
     */
    public static Script createP2PKHInputScript(byte[] sigByte, byte[] pubkeyByte) {
        return ScriptBuilder.createNulsInputScript(sigByte, pubkeyByte);
    }

    /**
     * Generate locking script based on output address
     *
     * @param address Output address
     * @return Script  The generated locking script
     */
    public static Script createP2PKHOutputScript(byte[] address) {
        return ScriptBuilder.createOutputScript(address, 1);
    }


    /**
     * M-NIn multi signature mode, based on multiple public keys andM-NGenerate redemption script
     *
     * @param pub_keys Public Key List
     * @param m        Indicates at least how many signatures are required for verification to pass
     * @return Script  The generated locking script
     */
    public static Script creatRredeemScript(List<String> pub_keys, int m) {
        return ScriptBuilder.createNulsRedeemScript(m, pub_keys);
    }

    /**
     * M-NIn multi signature mode, based on multiple public keys andM-NGenerate unlock script（NIt's the length of the public key list）
     *
     * @param signatures      Signature List
     * @param multisigProgram When the transaction isP2SHWhen, it represents the redemption script
     * @return Script     Generate unlock script
     */
    public static Script createP2SHInputScript(List<byte[]> signatures, Script multisigProgram) {
        return ScriptBuilder.createNulsP2SHMultiSigInputScript(signatures, multisigProgram);
    }

    /**
     * M-NIn multi signature mode, based on multiple public keys andM-NGenerate lock script（NIt's the length of the public key list）
     *
     * @param redeemScript Redemption script
     * @return Script  The generated locking script
     */
/*    public static Script createP2SHOutputScript(Script redeemScript) {
        return ScriptBuilder.createP2SHOutputScript(redeemScript);
    }*/

    /**
     * M-NIn multi signature mode, generate locking scripts based on output addresses
     *
     * @param address Output address
     * @return Script  The generated locking script
     */
    public static Script createP2SHOutputScript(byte[] address) {
        return ScriptBuilder.createOutputScript(address, 0);
    }

    public static void main(String[] args) {
        /**
         * Script serialization test code
         * */
        try {
            /*TransferTransaction tx = new TransferTransaction();
            tx.setTime(TimeService.currentTimeMillis());

            CoinData coinData = new CoinData();
            List<Coin> from = new ArrayList<Coin>();
            for(int i=0;i<3;i++){
                String addr = "tx_hash+index"+1;
                Coin from_coin = new Coin(addr.getBytes(),Na.valueOf(100));
                from.add(from_coin);
            }
            List<Coin> to = new ArrayList<Coin>();
            for(int i=0;i<3;i++){
                String addr = "Nsdybg1xmP7z4PTUKKN26stocrJ1qrU"+1;
                Coin to_coin = new Coin(AddressTool.getAddress(addr),Na.valueOf(100));
                to_coin.setScript(ScriptBuilder.createOutputScript(AddressTool.getAddress(addr),0));
                to.add(to_coin);
            }
            coinData.setFrom(from);
            coinData.setTo(to);
            tx.setCoinData(coinData);
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            List<Script> scripts = new ArrayList<Script>();
            for(int i=0;i<3;i++){
                String addr = "Nse84JjkxBoR9zXLGftu8X8xjsfnwxW"+1;
                scripts.add(ScriptBuilder.createOutputScript(AddressTool.getAddress(addr),1));
            }
            sig.setScripts(scripts);
            sig.setPublicKey("publickey".getBytes());

            sig.setSignData(new NulsSignData());
            tx.setBlockSignature(sig.serialize());

            byte[] bytes = tx.serialize();


            TransferTransaction tx2 = new TransferTransaction();
            tx2.parse(new NulsByteBuffer(bytes));
            System.out.println(bytes.length);
            for (Coin coin : tx2.getCoinData().getTo()) {
                System.out.println(coin.getNa());
                System.out.println(coin.getScript().getChunks());
            }
            P2PKHScriptSig scriptSig = new P2PKHScriptSig();
            scriptSig.parse(new NulsByteBuffer(tx2.getBlockSignature()));
            for (Script script:scriptSig.getScripts()) {
                System.out.println(script.getChunks());
            }*/

            /**
             * Script creation test code
             * */
         /*   //P2PKHInput
            byte[] signbyte = "cVLwRLTvz3BxDAWkvS3yzT9pUcTCup7kQnfT2smRjvmmm1wAP6QT".getBytes();
            byte[] pubkeyByte = "public_key".getBytes();
            Script inputScript = createP2PKHInputScript(signbyte, pubkeyByte);
            System.out.println("P2PKH_INPUT:" + inputScript.getChunks());
            //System.out.println(new String(inputScript.getChunks().get(0).entity));
            //P2PKHOutput
            byte[] addrByte = "Nsdybg1xmP7z4PTUKKN26stocrJ1qrUJ".getBytes();
            Script outputScript = createP2PKHOutputScript(addrByte);
            System.out.println("P2PKH_OUTPUT:" + outputScript.getChunks());
            //redeemScript
            List<String> pub_keys = new ArrayList<String>();
            for (int i = 0; i < 3; i++) {
                pub_keys.add("Nsdybg1xmP7z4PTUKKN26stocrJ1qrU" + i);
            }
            Script redeemScript = creatRredeemScript(pub_keys, 2);
            System.out.println("REDEEM:" + redeemScript.getChunks());
            //P2SHInput
            List<byte[]> signBytes = new ArrayList<byte[]>();
            for (int i = 0; i < 3; i++) {
                signBytes.add("cVLwRLTvz3BxDAWkvS3yzT9pUcTCup7kQnfT2smRjvmmm1wAP6Q".getBytes());
            }
            System.out.println(redeemScript.getProgram().length);
            Script p2shInput = createP2SHInputScript(signBytes, redeemScript);
            System.out.println("P2SH_INPUT:" + p2shInput.getChunks());
            ScriptChunk scriptChunk = p2shInput.getChunks().get(p2shInput.getChunks().size() - 1); //scriptChunk.dataWhat is stored is the serialization information of the redemption script
            Script redeemScriptParse = new Script(scriptChunk.entity);
            System.out.println(redeemScriptParse.getChunks());
            //P2SHOutput
            Script p2shOutput = createP2SHOutputScript(redeemScript);
            System.out.println("P2SH_OUTPUT:" + p2shOutput.getChunks());

            System.out.println(Arrays.toString(SerializeUtils.sha256hash160("03a690c7f3b07e320566162b0ff7d79c8c9f453c0a4a13305fcd90f4e4f4cf215c".getBytes())));
*/
            /**
             * P2PKHScript validation test code
             * */
           /* Na values = Na.valueOf(10);
            byte[] from = "".getBytes();                               //Enter address
            byte[] to   = "".getBytes();                               //Output address
            String pub_key = "";                                       //Enter account public key
            String password ="";
            String remark ="";
            Na price = Na.valueOf(5);
            TransferTransaction tx = new TransferTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            CoinData coinData = new CoinData();
            Coin toCoin = new Coin(to, values);
            coinData.getTo().add(toCoin);
            if (price == null) {
                price = TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES;
            }
            CoinDataResult coinDataResult = accountLedgerService.getCoinData(from, values, tx.size() +  + coinData.size(), price);
            if (!coinDataResult.isEnough()) {
                //return Result.getFailed(AccountLedgerErrorCode.INSUFFICIENT_BALANCE);
                System.out.println("Insufficient balance！");
                return;
            }
            coinData.setFrom(coinDataResult.getCoinList());
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
            tx.setCoinData(coinData);
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            P2PKHScriptSig sig = new P2PKHScriptSig();
            //sig.setPublicKey(account.getPubKey());*/
            //Using the current transaction'shashAnd the private key account of the account
            //sig.setSignData(accountService.signDigest(tx.getHash().getBytes(), account, password));
            //tx.setBlockSignature(sig.serialize());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
