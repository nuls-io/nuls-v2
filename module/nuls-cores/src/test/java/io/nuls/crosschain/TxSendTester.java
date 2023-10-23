package io.nuls.crosschain;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.base.model.ResetChainInfoTransaction;
import io.nuls.crosschain.base.model.bo.txdata.ResetChainInfoData;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxSendTester {

    @Test
    public void test() throws Exception {
//        NoUse.mockModule();
        String prikey = "";
        ECKey ecKey = ECKey.fromPrivate(HexUtil.decode(prikey));
        byte[] address = AddressTool.getAddress(ecKey.getPubKey(), 1);


        ResetChainInfoTransaction tx = new ResetChainInfoTransaction();
        tx.setTime(System.currentTimeMillis() / 1000);
        ResetChainInfoData txData = new ResetChainInfoData();
        txData.setJson("{\n" +
                "            \"chainId\":1,\n" +
                "            \"chainName\":\"nuls\",\n" +
                "            \"minAvailableNodeNum\":0,\n" +
                "            \"maxSignatureCount\":100,\n" +
                "            \"signatureByzantineRatio\":66,\n" +
                "            \"addressPrefix\":\"NULS\",\n" +
                "            \"assetInfoList\":[\n" +
                "                {\n" +
                "                    \"assetId\":1,\n" +
                "                    \"symbol\":\"NULS\",\n" +
                "                    \"assetName\":\"\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":18,\n" +
                "                    \"symbol\":\"LCC\",\n" +
                "                    \"assetName\":\"LCC\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":6\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":4,\n" +
                "                    \"symbol\":\"OBEE\",\n" +
                "                    \"assetName\":\"ObeeNetwork\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":5,\n" +
                "                    \"symbol\":\"Galan\",\n" +
                "                    \"assetName\":\"GAN\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":25,\n" +
                "                    \"symbol\":\"TPU\",\n" +
                "                    \"assetName\":\"TPUSaas\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":33,\n" +
                "                    \"symbol\":\"Goblin\",\n" +
                "                    \"assetName\":\"Goblin\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":45,\n" +
                "                    \"symbol\":\"DATT\",\n" +
                "                    \"assetName\":\"DATT\",\n" +
                "                    \"usable\":false,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":13,\n" +
                "                    \"symbol\":\"DATT\",\n" +
                "                    \"assetName\":\"DATT\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":55,\n" +
                "                    \"symbol\":\"BCNT\",\n" +
                "                    \"assetName\":\"BCNToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":19,\n" +
                "                    \"symbol\":\"PETC\",\n" +
                "                    \"assetName\":\"PetCoin\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":17,\n" +
                "                    \"symbol\":\"LCC\",\n" +
                "                    \"assetName\":\"LCC\",\n" +
                "                    \"usable\":false,\n" +
                "                    \"decimalPlaces\":6\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":77,\n" +
                "                    \"symbol\":\"CROS\",\n" +
                "                    \"assetName\":\"cros_test\",\n" +
                "                    \"usable\":false,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":57,\n" +
                "                    \"symbol\":\"Goblin\",\n" +
                "                    \"assetName\":\"Goblin\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":21,\n" +
                "                    \"symbol\":\"EHT\",\n" +
                "                    \"assetName\":\"Earhart\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":87,\n" +
                "                    \"symbol\":\"TRG\",\n" +
                "                    \"assetName\":\"TokenRepublic\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":81,\n" +
                "                    \"symbol\":\"VIBK\",\n" +
                "                    \"assetName\":\"Vibook\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":98,\n" +
                "                    \"symbol\":\"BNF\",\n" +
                "                    \"assetName\":\"BonFi\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":114,\n" +
                "                    \"symbol\":\"KTLYO\",\n" +
                "                    \"assetName\":\"KatalyoToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":124,\n" +
                "                    \"symbol\":\"SNEGY\",\n" +
                "                    \"assetName\":\"Sonergy\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":133,\n" +
                "                    \"symbol\":\"FMP\",\n" +
                "                    \"assetName\":\"BitsFlea_Point\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":4\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":128,\n" +
                "                    \"symbol\":\"CBT\",\n" +
                "                    \"assetName\":\"CBToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":90,\n" +
                "                    \"symbol\":\"ARCH\",\n" +
                "                    \"assetName\":\"ARCHCOIN\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":10\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":103,\n" +
                "                    \"symbol\":\"MCP\",\n" +
                "                    \"assetName\":\"MyCryptoPlay\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":96,\n" +
                "                    \"symbol\":\"CBLT\",\n" +
                "                    \"assetName\":\"Cobalt\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":91,\n" +
                "                    \"symbol\":\"JTT\",\n" +
                "                    \"assetName\":\"JustTest\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":112,\n" +
                "                    \"symbol\":\"RYIP\",\n" +
                "                    \"assetName\":\"RYIPLATINUM\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":117,\n" +
                "                    \"symbol\":\"MES\",\n" +
                "                    \"assetName\":\"MesChain\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":113,\n" +
                "                    \"symbol\":\"NIA\",\n" +
                "                    \"assetName\":\"Nydronia\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":107,\n" +
                "                    \"symbol\":\"EGR\",\n" +
                "                    \"assetName\":\"EgorasToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":116,\n" +
                "                    \"symbol\":\"GHD\",\n" +
                "                    \"assetName\":\"Giftedhands\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":131,\n" +
                "                    \"symbol\":\"ZERO\",\n" +
                "                    \"assetName\":\"ZERO_Token\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":130,\n" +
                "                    \"symbol\":\"FAR\",\n" +
                "                    \"assetName\":\"FarSwap\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":99,\n" +
                "                    \"symbol\":\"RVX\",\n" +
                "                    \"assetName\":\"RiveX\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":136,\n" +
                "                    \"symbol\":\"GGTK\",\n" +
                "                    \"assetName\":\"GGToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":146,\n" +
                "                    \"symbol\":\"NABOX\",\n" +
                "                    \"assetName\":\"Nabox_Token\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":132,\n" +
                "                    \"symbol\":\"SHD\",\n" +
                "                    \"assetName\":\"SHIELD\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":143,\n" +
                "                    \"symbol\":\"XNINJA\",\n" +
                "                    \"assetName\":\"XNinjaSwap\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":119,\n" +
                "                    \"symbol\":\"PEET\",\n" +
                "                    \"assetName\":\"Peet\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":149,\n" +
                "                    \"symbol\":\"TICO\",\n" +
                "                    \"assetName\":\"TICOEXToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":148,\n" +
                "                    \"symbol\":\"JDI\",\n" +
                "                    \"assetName\":\"JDIToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":147,\n" +
                "                    \"symbol\":\"KFI\",\n" +
                "                    \"assetName\":\"KeFiToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":152,\n" +
                "                    \"symbol\":\"ARTDECO\",\n" +
                "                    \"assetName\":\"ARTDECO\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":150,\n" +
                "                    \"symbol\":\"TROP\",\n" +
                "                    \"assetName\":\"Interop\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":151,\n" +
                "                    \"symbol\":\"NFD\",\n" +
                "                    \"assetName\":\"NonFungibleDefi\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":157,\n" +
                "                    \"symbol\":\"SMG\",\n" +
                "                    \"assetName\":\"SmaugsNFT\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":158,\n" +
                "                    \"symbol\":\"ICH\",\n" +
                "                    \"assetName\":\"Ideachaincoin\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":159,\n" +
                "                    \"symbol\":\"KSF\",\n" +
                "                    \"assetName\":\"KesefFinance\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":139,\n" +
                "                    \"symbol\":\"Vox\",\n" +
                "                    \"assetName\":\"Vox\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":162,\n" +
                "                    \"symbol\":\"TOM\",\n" +
                "                    \"assetName\":\"TOM\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":156,\n" +
                "                    \"symbol\":\"PTE\",\n" +
                "                    \"assetName\":\"Peet\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":160,\n" +
                "                    \"symbol\":\"APPN\",\n" +
                "                    \"assetName\":\"APPN\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":163,\n" +
                "                    \"symbol\":\"DEFIY\",\n" +
                "                    \"assetName\":\"DeFiFarms\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":164,\n" +
                "                    \"symbol\":\"LIVENFT\",\n" +
                "                    \"assetName\":\"LiveNFTToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":170,\n" +
                "                    \"symbol\":\"TXO\",\n" +
                "                    \"assetName\":\"Texo\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":169,\n" +
                "                    \"symbol\":\"8BIT\",\n" +
                "                    \"assetName\":\"8bit\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":168,\n" +
                "                    \"symbol\":\"FIRE\",\n" +
                "                    \"assetName\":\"GreenFire\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":167,\n" +
                "                    \"symbol\":\"VNT\",\n" +
                "                    \"assetName\":\"VENTION\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":171,\n" +
                "                    \"symbol\":\"GMX\",\n" +
                "                    \"assetName\":\"GameX\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":172,\n" +
                "                    \"symbol\":\"CCFI\",\n" +
                "                    \"assetName\":\"CloudCoinFinance\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":176,\n" +
                "                    \"symbol\":\"PRB\",\n" +
                "                    \"assetName\":\"PremiumBlock\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":179,\n" +
                "                    \"symbol\":\"NFTC\",\n" +
                "                    \"assetName\":\"NFTCircle\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":2\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":174,\n" +
                "                    \"symbol\":\"XYL\",\n" +
                "                    \"assetName\":\"xyl\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":175,\n" +
                "                    \"symbol\":\"BDAM\",\n" +
                "                    \"assetName\":\"BDAMCoin\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":177,\n" +
                "                    \"symbol\":\"DNF\",\n" +
                "                    \"assetName\":\"DNFT\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":178,\n" +
                "                    \"symbol\":\"WHX\",\n" +
                "                    \"assetName\":\"WHITEX\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":183,\n" +
                "                    \"symbol\":\"ZOZO\",\n" +
                "                    \"assetName\":\"ZoZotoken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":181,\n" +
                "                    \"symbol\":\"EMC2\",\n" +
                "                    \"assetName\":\"EINSTEINTOKEN\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":9\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":180,\n" +
                "                    \"symbol\":\"MVH\",\n" +
                "                    \"assetName\":\"MovieCash\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":191,\n" +
                "                    \"symbol\":\"GCC\",\n" +
                "                    \"assetName\":\"GLOBALCOMMCOIN\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":194,\n" +
                "                    \"symbol\":\"OLD\",\n" +
                "                    \"assetName\":\"Oldtimer\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":198,\n" +
                "                    \"symbol\":\"DGP\",\n" +
                "                    \"assetName\":\"DGPAYMENT\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":225,\n" +
                "                    \"symbol\":\"ZINA\",\n" +
                "                    \"assetName\":\"Zinari\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":226,\n" +
                "                    \"symbol\":\"FUSE\",\n" +
                "                    \"assetName\":\"Niftyfuse\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":196,\n" +
                "                    \"symbol\":\"AVN\",\n" +
                "                    \"assetName\":\"AVNRichToken\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":234,\n" +
                "                    \"symbol\":\"LAND\",\n" +
                "                    \"assetName\":\"Landshare\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":237,\n" +
                "                    \"symbol\":\"Alkom\",\n" +
                "                    \"assetName\":\"Alkom\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":235,\n" +
                "                    \"symbol\":\"TIC\",\n" +
                "                    \"assetName\":\"TreasureIsland\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":187,\n" +
                "                    \"symbol\":\"XTMC\",\n" +
                "                    \"assetName\":\"XTMCSWAPAPP\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":18\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":242,\n" +
                "                    \"symbol\":\"VOLT\",\n" +
                "                    \"assetName\":\"Volterra\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":240,\n" +
                "                    \"symbol\":\"UV\",\n" +
                "                    \"assetName\":\"Unityventures\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":8\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":0,\n" +
                "                    \"symbol\":\"OxSGD\",\n" +
                "                    \"assetName\":\"OxSGD\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":4\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":276,\n" +
                "                    \"symbol\":\"OxSGD\",\n" +
                "                    \"assetName\":\"OxSGD\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":4\n" +
                "                },\n" +
                "                {\n" +
                "                    \"assetId\":281,\n" +
                "                    \"symbol\":\"OxUSD\",\n" +
                "                    \"assetName\":\"OxUSD\",\n" +
                "                    \"usable\":true,\n" +
                "                    \"decimalPlaces\":4\n" +
                "                }\n" +
                "            ],\n" +
                "            \"verifierList\":[ \n" +
                "                \"NULSd6Hga4mQkHAnQdhUiMmv1V3kQ4a84JaSb\",\n" +
                "\"NULSd6HgU65xXgWR2jrXEKwXivQj6WCBYVhoA\",\n" +
                "\"NULSd6HgX1bNap7DxUftecymeB7VjdHz4HvKU\",\n" +
                "\"NULSd6HgetcSk3cZz5ps92mQUzaptUxv6WRcT\",\n" +
                "\"NULSd6HgY2LiyEDvMjsHqZpyajxSXEgk1mcxs\",\n" +
                "\"NULSd6HgapEiSk1PMYBZtbWBRMcCyjm7Vgjri\",\n" +
                "\"NULSd6HgirJR5i3SkwADF96QHP6uhLvBHE2yQ\",\n" +
                "\"NULSd6Hghpud5oDJ6FPXNwLiiU8xryqE1sYmv\",\n" +
                "\"NULSd6HgaSJ21zt1Qppb4rbuec2AkuaQLgnk8\",\n" +
                "\"NULSd6Hgb7SV891gcu1FB2V6pRDvSUcfd75o6\",\n" +
                "\"NULSd6HgbJmUep4wyreghHDk28AusSCrSuDMF\",\n" +
                "\"NULSd6HgcR5ouh1aDU3oktEoJGQConj4sr1KR\",\n" +
                "\"NULSd6HghM5wyVjegBaDsCkW9dvBnkuucejxw\",\n" +
                "\"NULSd6HgapV9vQS5NKCaJSMWa2uHgWxX13nGJ\",\n" +
                "\"NULSd6HgjKGXg6dk1K7vagf4kLDktf7DwsoXg\",\n" +
                "\"NULSd6HgZcP5H8Fg4y5Wf8AwiZ8nVhVZ4jBcS\",\n" +
                "\"NULSd6HgbhkGyxzvvosnRYWgnLAusCuWrtLNT\",\n" +
                "\"NULSd6HgU7dsGGbvWZBFcnLjGiJHz7ZA8x6to\",\n" +
                "\"NULSd6HghrhYaMdRcskPkM8vB65XX9v88EJEv\",\n" +
                "\"NULSd6HgXaZUYDy8DCopGMwEPnervgKbtqFDS\",\n" +
                "\"NULSd6HgcC78MaUw7tDo88rmg61tHCvZoEaXP\",\n" +
                "\"NULSd6Hga7pDvwJeQ3ufen3rubekkDFtWNZDg\",\n" +
                "\"NULSd6HgYLo5hPNcgqKtNtpA8dhC3Y37v5Ra9\",\n" +
                "\"NULSd6Hghv5CQdQwfjATtw76MuWTnP7DT4xER\",\n" +
                "\"NULSd6HgZeQRuhxXZbCAELYHNVEm8bvSULR27\",\n" +
                "\"NULSd6HgiCCVyZvs162WNS6Gzvf22aFN8Y47P\",\n" +
                "\"NULSd6HgdcJnk1R18vS73DGj38EtWMMdipogY\",\n" +
                "\"NULSd6HgWTKbD7YBAAvP7NjKik4nRWuSw76Ny\",\n" +
                "\"NULSd6HgYTwiDWmisFDqgDzcK8Sf197G3L28Z\",\n" +
                "\"NULSd6Hgj56Sotnk28qnRWoeQ2Ew3KuPAXnpg\",\n" +
                "\"NULSd6HgjHLnaPdyPYADSZy9UqewvJFEkAUP1\",\n" +
                "\"NULSd6HgbcWZAxSGf38JxX9opKwgVuC6B8Hin\",\n" +
                "\"NULSd6HgjF8iCrjpymFy6LVRmEjQG8AFayFDi\",\n" +
                "\"NULSd6HgcUQ4paNjBiRSXevwpYGhVVnvwdks8\",\n" +
                "\"NULSd6HgfVbGkmqCD1ycCPj2ZMJ7hYLrBu6eM\",\n" +
                "\"NULSd6Hghpg9Nk7BNrsAccV49FAVyvMqCVWnv\",\n" +
                "\"NULSd6HgX4mBo5v9nwESRwFn9Ns9LUqYAE91L\",\n" +
                "\"NULSd6Hgh1HF2wmtRRTZTwYiVUGVriJnm8Ucs\",\n" +
                "\"NULSd6HgZDQifPEXXTmUa2RzLLCrS4kzauf5C\",\n" +
                "\"NULSd6HgVsgcK6Mt4cPyyPBZ9umZnacb7bAr7\",\n" +
                "\"NULSd6HgdMZ6fzUp4my8VkascvWx2LcKJNFMp\",\n" +
                "\"NULSd6HgVtmHWQsKidqHuTB1cV9kGPVciXNXk\",\n" +
                "\"NULSd6HgVrfwxnmh7ycBddC8NDQ5LqDz6vK69\",\n" +
                "\"NULSd6Hge5eGAaryhAQtZjHxSNw1qwDtHARHJ\",\n" +
                "\"NULSd6HgcXGFs46UbyNDUaLuE4AviF5GCTbyh\",\n" +
                "\"NULSd6HgeLusFRaAJNkR9ofm5Rx32F1whpKe4\",\n" +
                "\"NULSd6Hgd81Ce2HqUmgFxwLvwPNXknGbaaNMu\",\n" +
                "\"NULSd6HggQ1xMawWTMWyPtKWciMfR4CTavLrp\",\n" +
                "\"NULSd6HgWvZUZTRzTURM8hRmWBvpzVmHTB1gn\",\n" +
                "\"NULSd6HghnX3XnuFFTLfzx6TEzMsX6RKeZWsu\",\n" +
                "\"NULSd6HgW5vV415MNoY2JTCaxa7NhcaHSo8Fg\",\n" +
                "\"NULSd6Hgezi6y7LCRW4iLrXELyvH36fN12wHK\",\n" +
                "\"NULSd6HgctDviYK6yDBKJd6WGhRRkByLhUrva\",\n" +
                "\"NULSd6HgZbtnPKbxWAhg994HNQG76wSs4RAPq\",\n" +
                "\"NULSd6HgZBcw88QkNen1wdM7EVaJ595kkHddN\",\n" +
                "\"NULSd6HgiQ5CTko7Zx59vcBeHyxPAJQ7P8T48\",\n" +
                "\"NULSd6HgXfnDCizGjLfBxrkV59WN4uTGzHMdi\",\n" +
                "\"NULSd6HggpjM8RB5WbjzLc7bLvQDjHuRcongN\",\n" +
                "\"NULSd6HgZZiqYSDiyz3xGN4tzNhgrho9EN59H\",\n" +
                "\"NULSd6HgdNumANdW3LxB7NEZd4oa7otR4LkPN\",\n" +
                "\"NULSd6HgcpKcxYFkeRDg8MJrSEF1A1SkH2KRM\",\n" +
                "\"NULSd6HgXhUTCKGEwHiGmnPmWFAvXyANTdjrn\",\n" +
                "\"NULSd6Hgh5VupJJ7Evabv9QQ2Pst8mjPS4dXT\",\n" +
                "\"NULSd6HgWyGYLML5CadRiND5RJghYc5vbiycJ\",\n" +
                "\"NULSd6HgfLT3nmbxtHZm8CsiTwjs1HxXJtmDT\",\n" +
                "\"NULSd6HgggfeRCjx5Ka4LVRug4i6q5ymfaag8\",\n" +
                "\"NULSd6HgYE7AKJmkAoPtjXxSHPkqxzmyPebkT\",\n" +
                "\"NULSd6HghcXawGBzwaufEoHchhWJrsJwSsitY\",\n" +
                "\"NULSd6HgfwTZKqKVnV3awiDKnEbF7RjZufWCN\",\n" +
                "\"NULSd6Hgfd4H28qGCrzGdhgX2G5PKj6rejsUG\",\n" +
                "\"NULSd6HgeTroMh3M59hbMWqeYqbgVXioa8Jmz\",\n" +
                "\"NULSd6HgefSM6RkURTdWu1i9mB96ufGfxqg71\",\n" +
                "\"NULSd6HghMrN2DTVmcBgiomKhvUETkg6c6N8E\",\n" +
                "\"NULSd6HgZivXvXs4su6cuyFzgtBTkk9fTqxyy\",\n" +
                "\"NULSd6HgWWEmZSxffcNVHc1kjbebkejWjVtpV\",\n" +
                "\"NULSd6HgbBmFpomGuvmSa76dirtzAt2rWKn4w\",\n" +
                "\"NULSd6HgXNrFd9NJMF5ExuSnGteKgHSKsfmXX\",\n" +
                "\"NULSd6HgiYHK1WWeNWk1Dx1wUb1usQXQXic11\",\n" +
                "\"NULSd6Hge7xHDnvsSpnzbR2gWHd31zJ1How11\",\n" +
                "\"NULSd6Hgc5VNP4rF4wxdiXEQKpBKUE4w5RS22\",\n" +
                "\"NULSd6HgeQwXLdre69ArkqVZNDqMLU4CaAz33\",\n" +
                "\"NULSd6HgcjAKAgq8jjXgBCcNLEJUvJEYcoj44\",\n" +
                "\"NULSd6HgiDdTjcuvhqzm3bomyBFZmosV3ei55\"\n" +
                "            ],\n" +
                "            \"registerTime\":0\n" +
                "        }");
        tx.setTxData(txData.serialize());

        CoinData coinData = new CoinData();
        CoinFrom from = new CoinFrom();
        from.setAddress(address);
        from.setAmount(BigInteger.valueOf(30000000));
        from.setAssetsChainId(1);
        from.setAssetsId(1);
        from.setLocked((byte) 0);
        from.setNonce(HexUtil.decode("7bf509a99dbeceb5"));
        coinData.getFrom().add(from);
        CoinTo to = new CoinTo();
        to.setAddress(address);
        to.setAmount(BigInteger.ZERO);
        to.setAssetsId(1);
        to.setAssetsChainId(1);
        to.setLockTime(0);
        coinData.getTo().add(to);

        tx.setCoinData(coinData.serialize());

        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> list = new ArrayList<>();
        P2PHKSignature sig = new P2PHKSignature();
        sig.setPublicKey(ecKey.getPubKey());
        NulsSignData data = new NulsSignData();
        data.setSignBytes(ecKey.sign(tx.getHash().getBytes()));
        sig.setSignData(data);
        list.add(sig);
        transactionSignature.setP2PHKSignatures(list);
        tx.setTransactionSignature(transactionSignature.serialize());
        Log.info(tx.getHash().toHex());
        Log.info(HexUtil.encode(tx.serialize()));
        Log.info("" + tx.size());
//        sendTx(2, HexUtil.encode(tx.serialize()));
    }

    @SuppressWarnings("unchecked")
    public static void sendTx(int chainId, String tx) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("tx", tx);
        try {
            /*boolean ledgerValidResult = commitUnconfirmedTx(chain,tx);
            if(!ledgerValidResult){
                throw new NulsException(ConsensusErrorCode.FAILED);
            }*/
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                //rollBackUnconfirmTx(chain,tx);
                throw new RuntimeException();
            }
        } catch (NulsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
