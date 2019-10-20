/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.deserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.*;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.model.ApiModelProperty;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-07-15
 */
public class TxDeserialization {

    @Test
    public void test() throws NulsException, JsonProcessingException {
        //String txStr1 = "1000ca58555d007c020001f7ec6473df12e751d64cf20a8baa7edd50810f81020002e76b5df12f6f8a00ba9910dc750d248aa0d86a370000000000000000000000000000000000000000000000000000000000000000010000000000000019000000000000000d6465706f736974466f724f776e0e28292072657475726e20766f6964008c0117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100ba67f7050000000000000000000000000000000000000000000000000000000008309a49ca52c05d42000117020002e76b5df12f6f8a00ba9910dc750d248aa0d86a370200010000e1f5050000000000000000000000000000000000000000000000000000000000000000000000006a2103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e3473045022100851d60c37738ffbc4abee2072ccad3e1722ddff7eca2ff9c5653fba21a81c9a1022078b9693cd6325abe8009d5e30e0b7b29cf61df671425c1fa5d2ae6f01f7fef59";
        String txStr1 = "10001535a85d006a0200013452fcc77369361d225f61ea34b9930f98d2b536020002a6d26cb6b330c7a0aaf47848e5c27e3abe1a980400000000000000000000000000000000000000000000000000000000000000007a500000000000001900000000000000087365744167656e740001004801170200013452fcc77369361d225f61ea34b9930f98d2b536020001008a62090000000000000000000000000000000000000000000000000000000000087e8ac0a9028cd89b00006a2103e56a675cd355d11bc7667d53adfc7c70d50abb4df7cf438df55253db5e10e22d473045022100b293a59aa4db375e6ccee44033bb115f535ac1f098e992ace4e65cb87581dd0402203ac0896e5e0fba202279d4aacca2bd99af51789884b6d15e9195a3aa0625f317";

        Transaction tx1 = new Transaction();
        tx1.parse(new NulsByteBuffer(HexUtil.decode(txStr1)));

        CallContractData data = new CallContractData();
        data.parse(new NulsByteBuffer(tx1.getTxData()));
        System.out.println(JSONUtils.obj2PrettyJson(data));
        System.out.println(JSONUtils.obj2PrettyJson(tx1));

        //Transaction tx2 = new Transaction();
        //tx2.parse(new NulsByteBuffer(HexUtil.decode(txStr2)));
        //CoinData coinData2 = tx2.getCoinDataInstance();
        //
        //System.out.println(JSONUtils.obj2PrettyJson(tx2));
    }

    @Test
    public void contractReturnTxTest() throws NulsException, JsonProcessingException {
        String receiveTxStr = "13005229a95d000046000117010001a8e78f6b9e8eec915a9775cb999f3df21644671a0100010096f10e0000000000000000000000000000000000000000000000000000000000000000000000000000";
        String makeTxStr = "13005229a95d000046000117010001a8e78f6b9e8eec915a9775cb999f3df21644671a010001004b990c0000000000000000000000000000000000000000000000000000000000000000000000000000";

        Transaction tx1 = new Transaction();
        tx1.parse(new NulsByteBuffer(HexUtil.decode(receiveTxStr)));

        System.out.println(JSONUtils.obj2PrettyJson(tx1));

        Transaction tx2 = new Transaction();
        tx2.parse(new NulsByteBuffer(HexUtil.decode(makeTxStr)));

        System.out.println(JSONUtils.obj2PrettyJson(tx2));
    }

    @Test
    public void test1() throws Exception {
        String txStr = "150037fa2b5d005700409452a3030000000000000000000000000000000000000000000000000000020002aefee5362dad1a404814709bfe1a9d91e988d6ef5b281974bdc4ac6b0590ea93079e4ef52ceecdb7db37737b32eb5e8b9e60e6618c0117020002aefee5362dad1a404814709bfe1a9d91e988d6ef0200010000409452a30300000000000000000000000000000000000000000000000000000890f46669c10fb6cd000117020002aefee5362dad1a404814709bfe1a9d91e988d6ef0200010000409452a3030000000000000000000000000000000000000000000000000000ffffffffffffffff00";
        Transaction tx = new Transaction();
        tx.parse(new NulsByteBuffer(HexUtil.decode(txStr)));
        CoinData coinData1 = tx.getCoinDataInstance();
        Deposit deposit = new Deposit();
        deposit.parse(new NulsByteBuffer(tx.getTxData()));
        System.out.println(JSONUtils.obj2PrettyJson(deposit));
        System.out.println(JSONUtils.obj2PrettyJson(tx));
    }

    @Test
    public void testCreateContractData() throws NulsException, IOException {
        String dataHex = "0200010321e497457c2d91b2e564c4aef7a65ff02c310d020002ea7847b9f362576a44aa49aaa2cfd6c6ef499b39fdc917504b03040a00000000008ca94a4f000000000000000000000000090000004d4554412d494e462f504b03040a00000008008ba94a4f8a80aa706d00000083000000140000004d4554412d494e462f4d414e49464553542e4d46f34dcccb4c4b2d2ed10d4b2d2acecccfb35230d433e0e5722c4acec82c4b2d420807e4a45694162bc02478b99c4a33734a749d2aad140aab2a0a9378b99c8b52134b5253c0428e0589c919a90abe8965a9790ac67a667a46100d29ba5e29d9203b2cf40ce28d8c0c79b978b900504b03040a00000000008ba94a4f00000000000000000000000003000000696f2f504b03040a00000000008ba94a4f00000000000000000000000008000000696f2f6e756c732f504b03040a00000000008ba94a4f0000000000000000000000000b000000696f2f6e756c732f76322f504b03040a00000000008ba94a4f00000000000000000000000014000000696f2f6e756c732f76322f636f6e74726163742f504b03040a00000008008ba94a4fd2c303d03f0e0000ac24000021000000696f2f6e756c732f76322f636f6e74726163742f4465784e554c532e636c617373dd5879601c5779ff8d34da59adc6922d4bb656969db57349ab959538b21564c7b5e52338b1adc492639c00f1485a491b49bbca6ae4c469a117bd886809774203010a8e098703b66c9326a489438002a10985022da1b4859450e841a10187f6f7bd793b7b78568e13feaa8f37df7bef9bf7bee3f71db35ffcd5671e01d08533116cc4872cfc450415f85035aec28765f8880c8765b83f8223f8a8500fc8f031193e1ec127f0c9308e46f0203e252b9f0ee358185b225829ebdbaab114c76bb00e73329cb07052984ec9519f8970efa130fe32c2958765f248189f0de3510b7f65e13139f971e13b2d5b8f87f144189f93c52785fff3327c218c2f86f1d7427e298c2f0bdf57e4f8a7e4aeaf0aefdf84f1b46c3f23c3d764f85b0b5f17c66f5441fda97c4e667f27c33765ff5b16be6de1ef2dfc8381054399b49b7586dcbedbd2c9ac81153b5399cef4ccc474676ea3737a78bc73f3f07036393dbdde8035e9a49d51610d4f3ae3c9ecf664d2c0929db738079dce49c71debec4d8dee48bb49b2903becfa3c8b6e4bb963c359e736cefa4676efddd96f209a4a0f652639dd77f65e73e0de40663c993650e75d38e3a6263a773953bca9ba3f359a76dc992cafda56bcbb617e9d8265dfc833170c3a134e7ac817a9a6400503b5feae16ca7d19d7be421945cad086543ae56e34d0d91acc16bcda768301734b663829d64ca593bb67260793d9016770822bf53b3343cec40d4e362573bd68ba63a96903cb7d510faec94bbb3579bb1889f24472b8d8d6c5895b30094f2773385bd93abfc2225d55c6e35db56726eda6269337a4a65314e43a27eb4c26dd6476733a9d711d37954953aa78f0798ecfd3b92779eb4c2a9b1c16588efa82c45adbce05f908c5de95437d64b46012e28ec24268541371ed032263b4b3dfcda6d2a3ebdbca4547e578f290d8faac3708bd7ed7191a2730b4ed1b79535020749fc38ee57ddfe0ca095bf44b9a9f461fa1afaee485a3c117ae3b97e3ca29db50aa814e016540ab442c95c17ba5a9b5ec2535c3c9a9cc74caf518abddcce6a1a10cd1c3bca5a395e999971623aa084865e05000a4eb9c43e2145eb7485fd7e78e31743c03ed3d97475ea6bf42cea4a789e964472967c34d37050187d1e70e8df56587059f4f05a0f11549f76bd7ec7c3356456a98185519864fd77bda93caf6fd87260733139cba45536b2a9b1a5298b626376b1b5a6e8eb27339dd83cc8ef3b7587959dd0c113992cd4cf67ad863ace76e2b44cc1b7edd5e3a4f9b568ea814e6d55b022b27e30e5d80753d2e5d2e54612123b5b7b8585a4e2ef416156eea17065e5194948dff25c575ebd09457bbd81031e9276f1f1aa389a9e22fff5fc5c5f9fabbd10b97b3527fa31bbc6e4e2a8098ae7a449ce161c5374074d7b19ece0c25d57c3b814ebc4ffbce966a72d099989197fa3333d9a1e4f694d4325bf70aab45421bd7e13bc448be1b7ab5333dc6c267e3d5d86163a70cbb64d88d1d169eb5f15ddc6c631336dbd882ad36b661ab857fb4f13dfc13abf9aee9d1d87432cdec174b4dc798b26339a3c7543761e19f6dfc0bbe6fa3179b5908cee6d7fdad8d1fc8e9cfe15f6dfc10cf332c82ec6963bb70dd875b6dfc08ff66e09281b1646c92872ac56343aa6cc40693b1099a32e6127fb13b92d98c8dbb3069e3c7f889810be5152fb997e53770a9700595ec82770832e6bbc581756161e99a8d7fc77fd8f84fdc2c1933eba4a747d8a8291fc2c67fe1a736eec5211befc67b080db93d357cd65dcb3da9624ea02c36a670abb4976c65629991982ec136fe1b3fb171b558ee6778dec6cf4105db7c05f32e2b77ecdbf10e1b1362c1f7811dd482a256dac6ffc8f12fe017367e29d4fb8567d93c4d6b9175fa066f490e316b2d0f8cda9cf199355b25969a02b9882ad51ca655156e992ffec9c7bed49990f82bcc4a9e18ebdb6e942fa1a0f7f7525dbe6465bdb696b9aff5c680de53bafc1bb7ede9a3c0533314fbca804b02ae3d7b8907308f13e1415206b157b34c4c39d9a4e489b25dde0e1e4b2f1b585d8ea3fca765c6d391a1d9da160479cbf133188b02d17a7d50da3f6b25a8af2a9f4cc3d333831a11a16c7232733029a2e970928fe4990937353541045a2a25f48d8835ae29ab5668387530359cc44a7ea86f8481df80fc58c174c70ff90a495aeac9cca79edbf473bb7e5ead9f4c9d7cf71a455fcbff3b4be6bb4ae6bb0be67d68227d1daee7b8872bf7a1927f81def80918f18750b1ff042a8fc3cc935579329427adf849848fa33ade7e1c9178e2386a1e54e7f773bc04d51c57c1c472d461059a71012e460c97e14274e3226ce06c33b906c815f36ec75edca07ecee8c53e6a6928ea35a42a48efc78d7c8ab4d7f359c167232f8f1f43f51cecfa0527512b5254e705a85587b6f1f2381ad0ae2e5ae2bde85fd4889b288481d792b60c12af2361e0f5b8595ba6138692a34a2e3aea9f1d528babd599b6c7a0cf3470e025095a572ae81a0a7a0505ed2a23686f90a04ea0a075a58276070a3aa804949787f914199a3d41eb44d0855ad052bfd6d3a3c07a4abb81d25e8528319cf761b32f71b3efc366ed43917d41852fbb370ea9cb4588b4b656acbd7ed11ceaef56a2448eb6d72ff66735473f850651ae5289b204f2f3d76602ad9746dc82a5bca68581923760cc172786244678df28691315b50b4a4d39869416645047434ba9358e21d49e3885c64aec2bf5de35b4c7b558c628ccdba2c5bfbc05b728ef09355ede162c775a845e6d0befce5358528147b1b4d4ad7d059ad6fa97d5eacb4af44b23130ccbba425836952ad64fc506e8e8bd656039ae9c5c72177b8200583695caff9a405866193f95eae57be82991a1fb24a274fc1c9adf07cb3c0cb3b27e9992770e2dbb124fc2e2e62e5e50d59e20e71c96e77de401a5991782b22d61b8b43040630c9c8b08bc750445de5fddbe5add9856266ca1475de52f814d0d2a176e32cea0dec20c0e6a2d6ee3b344fbdb71485bfa59580aa2e31d25e2afa0f889cfe5661770167a18b1fd95e6c358b9bf927e99c3aafefefd666e7ea137aff2e61d73b8a8bfbfc74cd45f5c7f49d454675f1a6f4f749c406ba9d263f45d8a69789caa4d30dad244474629dde589e62b3dae95164a1054a1a83ba87e25391af19bf82dfa430c118659bbc9b037797e5f5c5902636f7c03dea8adf0002d27b5a0ed2cbc253aa266b42a1a8a5a5115ec12e227d0568ac13f2106ef442b6695dcaff50ef3e56ec36fabc816ea77b4dc6df85d525efeff3d52a6a27e9f5495a2de44ad428afa035296a2fe9054586974793850a3f9c73fc21f7bfa1afdbc440e3fd2ee7b385ee4ef76ce3422ccfb0a2111579e6fe931e37348f454454d7abaf9bd398e0e7270295a45847bafb60ae8f94e7e75b5077dae77aa957d87b142339dc265157a47ce5d2dbb89a83987cbf3985987051cdf4abbdf45f0bf0d97e31de8c13bd99cbc8bfbef616abc9b88b887cafe3977eec507d93adc8f0f28bf6cf494f6fd72c4f7cb117ad0f3cb118da71e62f0cda44c9e9c62abb29526136435211256c85abcc9781131061aff6d32ae6c9e0f697712189ee55fa02b237c2e9ed7f2fe6c8d32683947d04657f484084eb3d409716e75f55834a5c9d0cc65e79eb0f0462ddf3b6d9a211a568bda3372b2a09e2b27b0f6702197ba6a759ef5182cdf6fc5f9e1c2a2fc9028ca0e7288a487ea0e951eaabdf490f7712f1672fc307dfc11faf830bba3fbe9858fb2843dc0c6e7e34c899f6076fb24e3e1283dfd203e864fe3091cc3d3386154e1a4518f53cadf239ea973fe26a5fd4d4afb9bd42d5e1c92ba43f95ba8b7287f5f832f280c84d48dbb541c1e24a6fe54e29037d5aa8c53ad701183ede182ff1a2407b7285cbc883d1e442e56f8b8a0ea9c51fa67b928c5b7f894b470a0242b9d7fd032a2244e2bee530775eed60babf7253a8a82cbeb881f66327b843df167d9923e4ac33fc69ee171d6e42758f69e647dfabc326edc93cd0fa6037e301df083e9800ea6515544ada5b9c0699c2f58deea07cb1b69ef30d74e9f8f015e4aec10cf1d7e50e40288d6b1a2a11e5383bc27a4d8a2210feae78f6ecb43b7558a6e2f837d8946fe328dfc151af929bc0a5f65aff434d1fd0cb3cdd7d8657d9d48fb06ebc1373147203c8e6f2ba3eff1ece11bfdb46ff4d3bed14fe7104d4a239a9487e8d793db55889ed3e8b594731a50bdb408bdaba483788988bdcb6f0aa77497dea45a9d9c85730dd0d144be31f67af4ef508867497f977dcff70afa9c265fc126a67829f9b50ce57c9fc3ea77064b72221634366f67cef744798c4fa9a46b2447e5445927c8b7b4341d091f0266d47c12612e0a25429a4ac815aaee7e9f97ff80ddc573fc90f8213f099f27f47fc4b6f1c7053dca1a5fe0355a60a1bc1ea5968d9ccb4fdb4abebb407be49df0ba4a6fef5a1d22b5a83883a885779d41634eb99216bc427e0ad3e1f13a5e2d509a6573129a43779495b9f9ee1cd0255098b1f949d7dd802b4fa2670eeb7b6ac81a295ab1e3092909d11a2fdbc759a6f996ad678982596bbca380b3352e15c18cd6e4c8aaa85dd4dcc525f9e2a734dacf08f19ff33bec052afa0b7e259fa1b22f52bd5fb119fa5fdc495b8921dfe4e9e21b72d687f6acdf34cdfa4dd32ccbbcd734cdb204784dd3acdf34cdfa4dd3ac6e9a8492afbd6a45c9d75e845239782fa91acad6c776612b5d226ea842c562dd395e1579197d56a1b7eef53bed0778ae48d9159ca2db1352cc0b80c815d505d1d61b4a5a66a3128d8689e52c41ab8c102e35aa718511298063976fc52e1f8e5d7ecbdca5b3f2727e8eba0a8efadba16e53615805a6e70af9b1536bf48cd668605e8d3a8abb13d9f296eec1221d9bd23d1c465d4ee3d5451aab20346aa9f1426abc0897b13cbfca68c0d5c612f41b4d055a0ff85a0ff85a0ff85a0ff85a7717685d8790d23aaaaaf5b29af9347fbfaff9433c476e5c5b945c3cef45f56f4ff4a06c464daf7e44cd4447fea3cf2c70e632d4192d68309653bd15881b3174192b0bd45aebabb5d6576badafd65af6b7a2561d8bb764997c6ef156aef5bf88a8659cb965bed2fb01a5e707ff0f504b03040a00000000008ca94a4f0000000000000000000000000f0000004d4554412d494e462f6d6176656e2f504b03040a00000000008ca94a4f0000000000000000000000001a0000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f504b03040a00000000008ca94a4f000000000000000000000000370000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f6e756c732d736d617274636f6e74726163742d6172636865747970652f504b03040a00000008002c893d4fbbdfeee06f020000dc0600003e0000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f6e756c732d736d617274636f6e74726163742d6172636865747970652f706f6d2e786d6ca554db8adb30107d5e7f851bfa6ac9de141a82d6cbb2b450c8d2c2a6a5afaaadd8cada9291e45c28fdf78e255f73691e6abf68cecc199d198d441e0f65e1ef98d25c8a875984c2d963ec914ac92d4b8c0f4ea11f66b931d512e392ee9840b4a249ce905419fef6f5057f4021903cbffb2c6579d0bca7edf77bb49f5bc27d1846f8e7cbea15329434e0421b2a1236a66bbed4d6bb92093556d5cded073a7cd7a20f3a7560603908ec596c99a494292b7eb826c4d64bf004f3bc3b922959575fd2984b24ea42a3dd3dc11de6d25065f8862606802622d02520891446011850054accb162048f021db33d803844d19ce0ce723e414b76239f0d6934a6ac62226522e14cc7dedd083836e679113a7d1b55d1448cb48133e8b7db9553dd4d6c2774f5b4fef4ba1e09071f9e6e7d53c9a49d2742e466735a7cc5ff47cec8b49d02e457cd0b9b8754459dc164ba9cceb0eb41338c5337596ecc5acab48249096ef2125956bc602a7084b312861ae6e8633385a312c009e56f78562b7b2f1c7647b4ac55c2e2082d086ed7adc7509531e33cedbaf540dd32e5228bbfaf3f07e0ed6db70f3edf88e05127ae7705d2b09cd61a95722bffd10e766049e07a72b3151182ffb4154d82ba51a7fb9a3aa40540654e358be19cde6806b7c4999d3393b4e8b8ad196fe98e82e866d985e149dcc51300b8a45c3c1754eb7e9cbb4945cfede2a9aa0aee5e34785cfaf83e059c4f5d32610668c0e2f7bfdb1719d931452957604875fcd3f4ad0d1a78ef82c0d74c41c7fc4a2ae307c1a5a4d1225c4457f97b785c724860a45fd05a24b90f6697f57242a36a7621df000d7dbc30610d7a728623409f4d61b7d6f63eb7d7d703d4752af6fe02504b03040a00000008008ba94a4fb83b38b9720000007a000000450000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f6e756c732d736d617274636f6e74726163742d6172636865747970652f706f6d2e70726f7065727469657315c8b10ac3201000d0ddaf38c81cd1cbd440a60ea543e8d0fc8035d74668359ca790bf8f7de3eb6e14899dd00aaf03665729aa6ed90a3cbc80358076b43822c2f5b9001a7b519538871427a3eda03e9cca7e5fa790742cdfac2b2ac712dece4bdb7ff5f9d7c6a728dcb277ec379263277502504b010214030a00000000008ca94a4f000000000000000000000000090000000000000000001000ed41000000004d4554412d494e462f504b010214030a00000008008ba94a4f8a80aa706d00000083000000140000000000000000000000a481270000004d4554412d494e462f4d414e49464553542e4d46504b010214030a00000000008ba94a4f000000000000000000000000030000000000000000001000ed41c6000000696f2f504b010214030a00000000008ba94a4f000000000000000000000000080000000000000000001000ed41e7000000696f2f6e756c732f504b010214030a00000000008ba94a4f0000000000000000000000000b0000000000000000001000ed410d010000696f2f6e756c732f76322f504b010214030a00000000008ba94a4f000000000000000000000000140000000000000000001000ed4136010000696f2f6e756c732f76322f636f6e74726163742f504b010214030a00000008008ba94a4fd2c303d03f0e0000ac240000210000000000000000000000a48168010000696f2f6e756c732f76322f636f6e74726163742f4465784e554c532e636c617373504b010214030a00000000008ca94a4f0000000000000000000000000f0000000000000000001000ffffe60f00004d4554412d494e462f6d6176656e2f504b010214030a00000000008ca94a4f0000000000000000000000001a0000000000000000001000ffff131000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f504b010214030a00000000008ca94a4f000000000000000000000000370000000000000000001000ffff4b1000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f6e756c732d736d617274636f6e74726163742d6172636865747970652f504b010214030a00000008002c893d4fbbdfeee06f020000dc0600003e0000000000000000000000a481a01000004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f6e756c732d736d617274636f6e74726163742d6172636865747970652f706f6d2e786d6c504b010214030a00000008008ba94a4fb83b38b9720000007a000000450000000000000000000000a4816b1300004d4554412d494e462f6d6176656e2f696f2e6e756c732e76322f6e756c732d736d617274636f6e74726163742d6172636865747970652f706f6d2e70726f70657274696573504b0506000000000c000c00730300004014000000000c6465785f6e756c735f303031963a000000000000190000000000000002010431303030010431303030";
        String dataHex1 = "";
        CreateContractData data = new CreateContractData();
        data.parse(new NulsByteBuffer(Hex.decode(dataHex + dataHex1)));
        byte[] code = data.getCode();
        FileUtils.writeByteArrayToFile(new File("/Users/pierreluo/IdeaProjects/nuls_newer_2.0/module/nuls-smart-contract/src/test/java/io/nuls/contract/deserialization/create.jar"), code, false);
        System.out.println(JSONUtils.obj2PrettyJson(data));
    }

    class Deposit extends BaseNulsData {
        @ApiModelProperty(description = "委托金额")
        private BigInteger deposit;
        @ApiModelProperty(description = "委托的节点HASH")
        private NulsHash agentHash;
        @ApiModelProperty(description = "委托账户")
        private byte[] address;
        @ApiModelProperty(description = "委托时间")
        private transient long time;
        @ApiModelProperty(description = "状态")
        private transient int status;
        @ApiModelProperty(description = "委托交易HASH")
        private transient NulsHash txHash;
        @ApiModelProperty(description = "委托交易被打包的高度")
        private transient long blockHeight = -1L;
        @ApiModelProperty(description = "退出委托高度")
        private transient long delHeight = -1L;

        /**
         * serialize important field
         */
        @Override
        protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
            stream.writeBigInteger(deposit);
            stream.write(address);
            stream.write(agentHash.getBytes());

        }

        @Override
        public void parse(NulsByteBuffer byteBuffer) throws NulsException {
            this.deposit = byteBuffer.readBigInteger();
            this.address = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
            this.agentHash = byteBuffer.readHash();
        }

        @Override
        public int size() {
            int size = 0;
            size += SerializeUtils.sizeOfBigInteger();
            size += Address.ADDRESS_LENGTH;
            size += NulsHash.HASH_LENGTH;
            return size;
        }

        public BigInteger getDeposit() {
            return deposit;
        }

        public void setDeposit(BigInteger deposit) {
            this.deposit = deposit;
        }

        public NulsHash getAgentHash() {
            return agentHash;
        }

        public void setAgentHash(NulsHash agentHash) {
            this.agentHash = agentHash;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public NulsHash getTxHash() {
            return txHash;
        }

        public void setTxHash(NulsHash txHash) {
            this.txHash = txHash;
        }

        public long getBlockHeight() {
            return blockHeight;
        }

        public void setBlockHeight(long blockHeight) {
            this.blockHeight = blockHeight;
        }

        public long getDelHeight() {
            return delHeight;
        }

        public void setDelHeight(long delHeight) {
            this.delHeight = delHeight;
        }

        public byte[] getAddress() {
            return address;
        }

        public void setAddress(byte[] address) {
            this.address = address;
        }

        public Set<byte[]> getAddresses() {
            Set<byte[]> addressSet = new HashSet<>();
            addressSet.add(this.address);
            return addressSet;
        }

        @Override
        public Deposit clone() throws CloneNotSupportedException {
            return (Deposit) super.clone();
        }
    }

    class Agent extends BaseNulsData {

        /**
         * 节点地址
         * agent address
         **/
        @ApiModelProperty(description = "节点地址")
        private byte[] agentAddress;

        /**
         * 打包地址
         * packing address
         **/
        @ApiModelProperty(description = "出块地址")
        private byte[] packingAddress;

        /**
         * 奖励地址
         * reward address
         * */
        @ApiModelProperty(description = "奖励地址")
        private byte[] rewardAddress;

        /**
         * 保证金
         * deposit
         * */
        @ApiModelProperty(description = "保证金")
        private BigInteger deposit;

        /**
         * 佣金比例
         * commission rate
         * */
        @ApiModelProperty(description = "佣金比例")
        private byte commissionRate;

        /**
         * 创建时间
         * create time
         **/
        @ApiModelProperty(description = "创建时间")
        private transient long time;

        /**
         * 所在区块高度
         * block height
         * */
        @ApiModelProperty(description = "所在区块高度")
        private transient long blockHeight = -1L;

        /**
         * 该节点注销所在区块高度
         * Block height where the node logs out
         * */
        @ApiModelProperty(description = "节点注销高度")
        private transient long delHeight = -1L;

        /**
         *0:待共识 unConsensus, 1:共识中 consensus
         * */
        @ApiModelProperty(description = "状态，0:待共识 unConsensus, 1:共识中 consensus")
        private transient int status;

        /**
         * 信誉值
         * credit value
         * */
        @ApiModelProperty(description = "信誉值")
        private transient double creditVal;

        /**
         *  总委托金额
         *Total amount entrusted
         * */
        @ApiModelProperty(description = "节点总委托金额")
        private transient BigInteger totalDeposit = BigInteger.ZERO;

        /**
         * 交易HASH
         * transaction hash
         * */
        @ApiModelProperty(description = "创建该节点的交易HASH")
        private transient NulsHash txHash;

        /**
         * 参与共识人数
         * Participation in consensus
         * */
        @ApiModelProperty(description = "参与共识人数")
        private transient int memberCount;

        /**
         *别名不序列化
         * Aliases not serialized
         * */
        @ApiModelProperty(description = "节点别名")
        private transient String alais;
        @Override
        public int size() {
            int size = 0;
            size += SerializeUtils.sizeOfBigInteger();
            size += this.agentAddress.length;
            size += this.rewardAddress.length;
            size += this.packingAddress.length;
            size += 1;
            return size;
        }

        @Override
        protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
            stream.writeBigInteger(deposit);
            stream.write(agentAddress);
            stream.write(packingAddress);
            stream.write(rewardAddress);
            stream.write(this.commissionRate);
        }

        @Override
        public void parse(NulsByteBuffer byteBuffer) throws NulsException {
            this.deposit = byteBuffer.readBigInteger();
            this.agentAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
            this.packingAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
            this.rewardAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
            this.commissionRate = byteBuffer.readByte();
        }


        public byte[] getPackingAddress() {
            return packingAddress;
        }

        public void setPackingAddress(byte[] packingAddress) {
            this.packingAddress = packingAddress;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public byte getCommissionRate() {
            return commissionRate;
        }

        public void setCommissionRate(byte commissionRate) {
            this.commissionRate = commissionRate;
        }

        public long getBlockHeight() {
            return blockHeight;
        }

        public void setBlockHeight(long blockHeight) {
            this.blockHeight = blockHeight;
        }

        public void setCreditVal(double creditVal) {
            this.creditVal = creditVal;
        }

        public double getCreditVal() {
            return creditVal < 0d ? 0D : this.creditVal;
        }

        public double getRealCreditVal(){
            return this.creditVal;
        }

        public void setTxHash(NulsHash txHash) {
            this.txHash = txHash;
        }

        public NulsHash getTxHash() {
            return txHash;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getDelHeight() {
            return delHeight;
        }

        public void setDelHeight(long delHeight) {
            this.delHeight = delHeight;
        }

        public byte[] getAgentAddress() {
            return agentAddress;
        }

        public void setAgentAddress(byte[] agentAddress) {
            this.agentAddress = agentAddress;
        }

        public byte[] getRewardAddress() {
            return rewardAddress;
        }

        public void setRewardAddress(byte[] rewardAddress) {
            this.rewardAddress = rewardAddress;
        }

        public int getMemberCount() {
            return memberCount;
        }

        public void setMemberCount(int memberCount) {
            this.memberCount = memberCount;
        }


        public BigInteger getDeposit() {
            return deposit;
        }

        public void setDeposit(BigInteger deposit) {
            this.deposit = deposit;
        }

        public BigInteger getTotalDeposit() {
            return totalDeposit;
        }

        public void setTotalDeposit(BigInteger totalDeposit) {
            this.totalDeposit = totalDeposit;
        }

        @Override
        public Agent clone() throws CloneNotSupportedException {
            return (Agent) super.clone();
        }

        public Set<byte[]> getAddresses() {
            Set<byte[]> addressSet = new HashSet<>();
            addressSet.add(this.agentAddress);
            return addressSet;
        }

        public String getAlais() {
            return alais;
        }

        public void setAlais(String alais) {
            this.alais = alais;
        }
    }
}
