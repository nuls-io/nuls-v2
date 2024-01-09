/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.block.model.GenesisBlock;
import io.nuls.block.test.BlockGenerator;
import io.nuls.core.basic.VarInt;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BlockGeneratorTest {

    /**
     * 测试区块生成器生成区块的连续性
     * @throws Exception
     */
    @Test
    public void generate() throws Exception {
        int start = 1;
        int count = 10;
        List<Block> blocks = new ArrayList<>();

        GenesisBlock genesisBlock = GenesisBlock.getInstance(0, 0);
        blocks.add(genesisBlock);

        Block preBlock = genesisBlock;
        do{
            Block block = BlockGenerator.generate(preBlock);
            blocks.add(block);
            preBlock = block;
            start++;
        } while (start < count);

        for (int i = 0; i < blocks.size()-1; i++) {
            NulsHash prehash = blocks.get(i).getHeader().getHash();
            NulsHash hash = blocks.get(i+1).getHeader().getPreHash();
            Assert.assertEquals(prehash, hash);
        }
    }

    /**
     * 测试区块生成器生成区块的分叉
     * @throws Exception
     */
    @Test
    public void fork() throws Exception {
        Block root = BlockGenerator.generate(null);
        Block block1 = BlockGenerator.generate(root, 1, "1");
        Block block2 = BlockGenerator.generate(root, 2, "1");
        Assert.assertEquals(root.getHeader().getHash(), block1.getHeader().getPreHash());
        Assert.assertEquals(block1.getHeader().getPreHash(), block2.getHeader().getPreHash());
        Assert.assertNotEquals(block1.getHeader().getHash(), block2.getHeader().getHash());
    }

    @Test
    public void blockParse() throws Exception {
        String hex = "ffb548660ee00b358b25eeb8d0a235bd8c575b5c2ee3d52ffff8640efbfc33cf728ecae7af3aaa458f9aa56512214423039282fdc9b52a5f569bda941dc3ff0ddaf9fe63f32da600020000005c31d30100670072f8fe6324000f000f00500a0020c2756186e5c8b0bffd07a5f91fe48eddc85667abc8fe6d574f3c6e87d7d4e0ff659b3bfc8896e564d39612e6354d3c9a41205ab27ab978008f1ac342ef25fbf4cd2d8dfaa4ee6aad21036e538acc8982563a4f82420f8ef80b409f7ea2c3b8711727c25791a7c5e147f04730450221008e4266bb4fae6453288e1fd58ef6b9d4286758e0bca33eed72c7090f991d42b102201c488b7d171ca466a5a642583b1efc7cf384c85e13ef9588224a63834939c32e0100daf9fe6300008a000217010001ee198567860c534a164f869fb3cf7e8854a2527201000100e5bb2908000000000000000000000000000000000000000000000000000000000000000000000000170100035d7683b0134503db3385d7fc4721e7b94d6fc48101000100e74fde00000000000000000000000000000000000000000000000000000000000000000000000000000200d0f9fe6300008c0117010001b2a74c73b3d63ceae361f05e00355115d0255d3f01000100005d880600000000000000000000000000000000000000000000000000000000080000000000000000000117010001aca553215d606c663cac99e8503abe19f76289eb0100010060d68606000000000000000000000000000000000000000000000000000000000000000000000000692102b638c69aebc4d2b952aff586e658023a7ba5abfac065c7ff7175c6309b5a030946304402201ce0c4e459a55ffb2cf7b5cc2055d1510d1ec5f435c2b028b55d434391a8780802200a1707dce78a089c3fdd8d0287041e57cb8fbd3a9934ba77a0f38bfb5a674360";
        Block block100 = new Block();
        block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        List<Transaction> txs = block100.getTxs();
        for (Transaction tx : txs) {
            if (tx.getTransactionSignature() == null) continue;
            System.out.println(String.format("type: %s, tx whole: %s", tx.getType(), HexUtil.encode(tx.serialize())));
            System.out.println(String.format("tx: %s", HexUtil.encode(tx.serializeForHash())));
            System.out.println(String.format("sign: %s", HexUtil.encode(tx.getTransactionSignature())));
        }
    }

    @Test
    public void varint() {
        String hex = "0000000000000000000000000000000000000000";
        System.out.println(HexUtil.encode(new VarInt(HexUtil.decode(hex).length).encode()));
    }

    @Test
    public void txParse() throws Exception {
        String txHex = "02001b1e1c6409636f696e2074657374008c01170100011b7ddde08e2994b7f61adfcf3d2106f97bff409a01000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117010001ddad24283470fd27554f257fe8daa7099e05d7990100010040420f00000000000000000000000000000000000000000000000000000000000000000000000000692102ef5b152e3f15eb0c50c9916161c2309e54bd87b9adce722d69716bcdef85f54746304402204ad75b49a85540c522dae1ec89ad66ed451034b74d3ffc7ecd2837e2091942a402204f4a8f51dda0b537400e28ec82cb8915884c8acbe010f923dcc4eb8185a32b6d";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(txHex), 0);
        tx.getHash();
        System.out.println(String.format("tx: %s", HexUtil.encode(tx.serializeForHash())));
        System.out.println(String.format("sign: %s", HexUtil.encode(tx.getTransactionSignature())));
    }

    @Test
    public void txTest() throws Exception {
        String hex = "02008093006400008c0117010001b2a0187dfeb154a0852c822190317a96fdba4d7001000100e0ac003b00000000000000000000000000000000000000000000000000000000080000000000000000000117010001aca553215d606c663cac99e8503abe19f76289eb010001004026ff3a000000000000000000000000000000000000000000000000000000000000000000000000";
        Transaction tx = new Transaction();
        tx.parse(HexUtil.decode(hex), 0);
        tx.setRemark(("南航一教授在课堂上媚外洗脑，学生在黑板上表达不满，学校回应播报文章\n" +
                "\n" +
                "育学笔谈\n" +
                "\n" +
                "2023-03-08 16:42\n" +
                "安徽\n" +
                "星问答计划创作者,中小学教师,优质教育领域创作者,内容评审官,活力创作者\n" +
                "关注\n" +
                "文/育学笔谈（分享校园见闻、评述教育现象）\n" +
                "\n" +
                "在当今的网络上，人们对“公知”恨之入骨，道理很简单，公知利用自己的权威性和优势地位到处传播不当言论，向接触他的人传播错误的世界观、人生观和价值观，导致三观尽毁。\n" +
                "\n" +
                "他们自以为喝了一点“洋墨水”，到处宣扬西方的价值观，缺少事情本身的是非曲直判断，尤其是在大学课堂上，如果公知老师在课堂上天天都在发布歪理邪说，学生是何感想？\n" +
                "\n" +
                "\n" +
                "01\n" +
                "南航一教授在课堂上媚外洗脑\n" +
                "南京航空航天大学一位陈姓教授突然被学生举报，直指他在课堂上发布不当言论，给学生灌输错误的认知和价值观，从而在网络上引起轩然大波，人们不禁要问：\n" +
                "\n" +
                "有些教授怎么吃着锅里的饭，还非要砸锅？师德何存？职业道德何在？\n" +
                "\n" +
                "\n" +
                "有知名大V爆料，南航经管学院的陈教授在财税课堂上给学生洗脑，称中国全靠欧美国家赏饭吃，如果外国实施制裁和经济封锁，一大半的国人都没有饭吃。\n" +
                "\n" +
                "02\n" +
                "学生在黑板上表达不满\n" +
                "学生听到这些内容感到严重不适，有学生将陈教授上课视频录了下来，并在黑板上用文字表达了不满“张口好似乏走狗”，可见当时学生有多气愤，还有学生选择了举报。\n" +
                "\n" +
                "\n" +
                "另据文史学者刘继兴介绍，陈教授在课堂上还公开强调美国的优越性，理由让人觉得很荒诞，以至于学生纷纷吐槽：这是网络上被传播了无数次，无数次都被打脸的老谣言，但这位大学教授却言之凿凿，公开侮辱学生的智商。\n" +
                "\n" +
                "这些公知教授平时不学无术，凭借着“老本”在课堂上信口开河，始终不愿意用发展的眼光去放眼当今的世界，宁可冒着说错的风险也不愿意认真学习和思考，试图强行向学生灌输错误的言论和认知。\n" +
                "\n" +
                "\n" +
                "03\n" +
                "学校回应：调查，零容忍\n" +
                "此事在网络上引发舆论后，南航公开做出回应：\n" +
                "\n" +
                "确认网络上出现了关于一位老师在课堂上发表不当言论的帖子，学校非常重视，已经在第一时间启动调查程序，坚持对师德问题零容忍，调查期间暂停该老师的教学工作。\n" +
                "\n" +
                "\n" +
                "有很多南航的校友和网友表示，堂堂的南航，怎么会有如此不堪的公知教授？一定要调查到底，追究其责任。\n" +
                "\n" +
                "\n" +
                "客观地说，虽然南航的调查结果未出，但此类学生举报老师的事件一般都是板上钉钉，如果没有确凿证据，学生是不会在黑板上写下那句话，更不会录制视频和举报，该教授的不当言论大概率是存在的。\n" +
                "\n" +
                "唯一让人遗憾的是，南航一直以来的良好声誉和社会影响力居然被一个公知教授拖累了，对学生的影响也不容小觑。要知道公知的观念和言论不是一朝一夕的事情，而是一个持续输出和影响的过程。\n" +
                "\n" +
                "04\n" +
                "结语\n" +
                "虽然学生选择了公开表达不满和举报，但此前听过课的学生呢？这是一个值得深思的问题，学校为何一直没有发现此类问题？这些都需要调查。\n" +
                "\n" +
                "\n" +
                "值得庆幸的是，学生勇敢地站出来反对，这是我们教育的成功之处，学生没有被公知的言论所左右。\n" +
                "\n" +
                "就像此前合肥某高校的一位副教授在中学演讲一样，公然媚外，认为外国人的基因更加优良，导致一位中学生夺过话题高呼：我们努力学习是为了中华民族的伟大复兴，不是为了和外国人结合！\n" +
                "\n" +
                "普通的学生都有一身正气，教授为何做不到？").getBytes(StandardCharsets.UTF_8));
        byte[] bytes = tx.serializeForHash();
        System.out.println(bytes.length);
        System.out.println(HexUtil.encode(bytes));
    }
    // 转账交易签名
    //   CLA: e0
    //   INS: 06
    //    P1: 00
    //    P2: 00
    //    Lc: 95
    // CData: 02008093006400008c0117010001b2a0187dfeb154a0852c822190317a96fdba4d7001000100e0ac003b00000000000000000000000000000000000000000000000000000000080000000000000000000117010001aca553215d606c663cac99e8503abe19f76289eb010001004026ff3a000000000000000000000000000000000000000000000000000000000000000000000000

    // 完整报文: e00600009502008093006400008c0117010001b2a0187dfeb154a0852c822190317a96fdba4d7001000100e0ac003b00000000000000000000000000000000000000000000000000000000080000000000000000000117010001aca553215d606c663cac99e8503abe19f76289eb010001004026ff3a000000000000000000000000000000000000000000000000000000000000000000000000

}