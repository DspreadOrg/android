package com.dspread.demoui.utils;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.util.ASN1Dump;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.dspread.demoui.utils.QPOSUtil.HexStringToByteArray;
import static com.dspread.demoui.utils.QPOSUtil.byteArray2Hex;

public class ParseASN1Util {
    private static String encryptData;
    private static String encryptDataWith3Des;
    private static String signData;
    private static String nonce;
    private static String header;
    private static String digest;
    private static String IVStr;
    private static String tr31Data;
    private static String publicKey;

    public static String getEncryptData() {
        return encryptData;
    }

    public static String getEncryptDataWith3Des() {
        return encryptDataWith3Des;
    }

    public static String getSignData() {
        return signData;
    }

    public static String getNonce() {
        return nonce;
    }

    public static String getHeader() {
        return header;
    }

    public static String getDigest() {
        return digest;
    }

    public static String getIVStr() {
        return IVStr;
    }

    public static String getTr31Data() {
        return tr31Data;
    }

    public static String getPublicKey() {
        return publicKey;
    }

    public static void parseASN1(String s){
        byte[] data = HexStringToByteArray(s);
        ASN1InputStream ais = new ASN1InputStream(data);
        ASN1Primitive primitive = null;
        try {
            while((primitive=ais.readObject())!=null){
                System.out.println("sequence->"+primitive);
                if(primitive instanceof ASN1Sequence){
                    ASN1Sequence sequence = (ASN1Sequence)primitive;
                    ASN1SequenceParser parser = sequence.parser();
                    ASN1Encodable encodable = null;
                    while((encodable=parser.readObject())!=null){
                        primitive = encodable.toASN1Primitive();
                        String re2 = ASN1Dump.dumpAsString(primitive,true);

//                        System.out.println("prop->"+primitive);
                        System.out.println("prop33->"+re2);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                ais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parseASN1new(String s){
        byte[] data = HexStringToByteArray(s);
        ASN1InputStream ais = new ASN1InputStream(data);
        ASN1Primitive primitive = null;
        try {
            while((primitive=ais.readObject())!=null){
                if(primitive instanceof ASN1Sequence){
                    ASN1Sequence sequence = (ASN1Sequence)primitive;
                    ASN1SequenceParser parser = sequence.parser();
                    ASN1Encodable encodable = null;
                    while((encodable=parser.readObject())!=null){
                        primitive = encodable.toASN1Primitive();
                        if(primitive instanceof ASN1TaggedObject){
                            ASN1TaggedObject asn1TaggedObject = (ASN1TaggedObject) primitive;
                            primitive = asn1TaggedObject.getObject();
                            if(primitive instanceof DERSequence){
                                DERSequence derSequence = (DERSequence) primitive;
                                int size = derSequence.size();
                                for(int i = 0 ; i < size; i++){
                                    ASN1Encodable encodable1 = derSequence.getObjectAt(i);
                                    primitive = encodable1.toASN1Primitive();
                                    if(primitive instanceof ASN1TaggedObject){
                                        ASN1TaggedObject asn1TaggedObject2 = (ASN1TaggedObject) primitive;
                                        primitive = asn1TaggedObject2.getObject();
                                    }else if(primitive instanceof ASN1Sequence){
                                        ASN1Sequence sequence2 = (ASN1Sequence)primitive;
                                        ASN1Encodable asn1Encodable = sequence2.getObjectAt(1);
                                        primitive = asn1Encodable.toASN1Primitive();
                                        if(primitive instanceof ASN1TaggedObject){
                                            ASN1TaggedObject object = (ASN1TaggedObject) primitive;
                                            if(object.getObject().toString().startsWith("#")){
                                                encryptData = object.getObject().toString().substring(1);
                                            }
                                            System.out.println("666->"+((ASN1TaggedObject)primitive).getObject());
                                        }
                                    }else if(primitive instanceof DERSet){
                                        DERSet derSet = (DERSet) primitive;
                                        int setLen = derSet.size();
                                        System.out.println("setLen->"+setLen);
                                        for(int j = 0 ; j < setLen; j ++){
                                            encodable = derSet.getObjectAt(j);
                                            primitive = encodable.toASN1Primitive();
                                            if(primitive instanceof ASN1Sequence){
                                                sequence = (ASN1Sequence) primitive;
                                                for(int m = 0 ;m < sequence.size();m++){
                                                    encodable = sequence.getObjectAt(m);
                                                    primitive = encodable.toASN1Primitive();
                                                    if(primitive instanceof ASN1Sequence){
                                                        ASN1Sequence sequence2 = (ASN1Sequence) primitive;
                                                        size = sequence2.size();
                                                        for(int n = 0 ; n < size; n ++){
                                                            encodable = sequence2.getObjectAt(n);
                                                            if(encodable.toString().contains("1.2.840.113549.1.9.25.3")){
                                                                nonce = encodable.toString();
                                                            }else if(encodable.toString().contains("1.2.840.113549.1.9.4")){
                                                                digest = encodable.toString();
                                                            }else if(encodable.toString().contains("1.2.840.113549.1.7.1")){
                                                                header = encodable.toString();
                                                            }
                                                            System.out.println("000->"+encodable.toString());
                                                        }
                                                    }else {
                                                        if(encodable.toString().startsWith("#")){
                                                            signData = encodable.toString().substring(1);
                                                        }
                                                    }
                                                }
                                            }
                                            System.out.println("888->"+encodable.toString());
                                        }
                                    }
                                }
                            }else {
                                encryptDataWith3Des = primitive.toString().substring(1);
                                System.out.println("asn tag:"+primitive.toString());
                            }
                        }else if (primitive instanceof DERTaggedObject) {
                            DERTaggedObject derTaggedObject = (DERTaggedObject) primitive;
                            System.out.println("DERTaggedObject:"+derTaggedObject.toString());
                        }else if (primitive instanceof DERSet) {
                            DERSet derSet = (DERSet) primitive;
                            System.out.println("DERSet:"+derSet.toString());
                        }else if (primitive instanceof ASN1ObjectIdentifier){
                            ASN1ObjectIdentifier objectIdentifier = (ASN1ObjectIdentifier) primitive;
                            System.out.println("ASN1ObjectIdentifier:"+objectIdentifier.toString());
                        } else if (primitive instanceof DLSet) {
                            DLSet dlSet = (DLSet) primitive;
                            System.out.println("out DLSet:"+dlSet.toString());
                            int size = dlSet.size();
                            for(int i = 0 ; i < size;i++){
                                ASN1Encodable asn1Encodable = dlSet.getObjectAt(i);
                                System.out.println("asn1Encodable:"+asn1Encodable.toString());
                                String[] array = asn1Encodable.toString().split(",");
                                tr31Data = array[0].substring(1);
                            }
                        } else if (primitive instanceof ASN1String) {
                            ASN1String string = (ASN1String) primitive;
                            System.out.println("PrintableString:"+string.getString());
                        }else if (primitive instanceof ASN1UTCTime) {
                            ASN1UTCTime asn1utcTime = (ASN1UTCTime) primitive;
                            System.out.println("UTCTime:"+asn1utcTime.getTime());
                        }else if (primitive instanceof DERSequence) {
                            DERSequence derSequence = (DERSequence) primitive;
                            System.out.println("DERSequence:"+derSequence.toString());
                        }else if(primitive instanceof DLSequence) {
                            DLSequence dlSequence = (DLSequence) primitive;
                            System.out.println("dlSequence:"+dlSequence.toString());
                            if(dlSequence.toString().contains("#")){
                                IVStr = dlSequence.toString();
                                IVStr = IVStr.substring(IVStr.indexOf("#")+1,IVStr.indexOf("]"));
                            }
                        }
//                        String re2 = ASN1Dump.dumpAsString(primitive,true);
//                        System.out.println("prop33->"+re2);

                    }
                }else if(primitive instanceof DLSet){//得出公钥加密的数据
                    DLSet dlSet = (DLSet) primitive;
                    System.out.println("DLSet:"+dlSet.toString());
                    int size = dlSet.size();
                    for(int i = 0 ; i < size; i ++){
                        ASN1Encodable encodable = dlSet.getObjectAt(i);
                        encodable.toString();
                        primitive = encodable.toASN1Primitive();
                        if(primitive instanceof ASN1Sequence){
                            ASN1Sequence sequence2 = (ASN1Sequence) primitive;
                            sequence2.getObjectAt(1);
                            encryptData = sequence2.getObjectAt(3).toString().substring(1);
                            System.out.println("ttt:"+sequence2.getObjectAt(3).toString());
                        }else{
                            System.out.println("ss:"+encodable.toString());
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                ais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void getServerPubkey(String s){
        byte[] data = HexStringToByteArray(s);
        ASN1InputStream ais = new ASN1InputStream(data);
        ASN1Primitive primitive = null;
        try {
            while((primitive=ais.readObject())!=null){
                System.out.println("sequence->"+primitive);
                if(primitive instanceof ASN1Sequence){
                    ASN1Sequence sequence = (ASN1Sequence)primitive;
                    ASN1SequenceParser parser = sequence.parser();
                    ASN1Encodable encodable = null;
                    while((encodable=parser.readObject())!=null){
                        primitive = encodable.toASN1Primitive();
                        String re2 = ASN1Dump.dumpAsString(primitive,true);
                        if(primitive instanceof ASN1Sequence){
                            sequence = (ASN1Sequence)primitive;
                            int size = sequence.size();
                            for(int i = 0 ; i < size; i ++){
                                encodable = sequence.getObjectAt(i);
                                System.out.println("encodable = "+encodable.toString());
                                primitive = encodable.toASN1Primitive();
                                if(primitive instanceof ASN1Sequence){
                                    ASN1Sequence sequence1 = (ASN1Sequence)primitive;
                                    for(int j = 0 ; j < sequence1.size();j ++){
                                        ASN1Encodable encodable2 = sequence1.getObjectAt(j);
                                        if(encodable2.toString().contains("#")){
                                            publicKey = encodable2.toString();
                                        }
                                        System.out.println("encodable2 = "+encodable2.toString());
                                    }
                                }
                            }
                        }
//                        if(re2.contains("DER Bit String")){
//                            publickModule = re2.substring(re2.indexOf("DER Bit String"));
//                            System.out.println("mudole = "+publickModule);
//                        }
//                        System.out.println("prop->"+primitive);
//                        System.out.println("prop33->"+re2);
                    }
                }else if (primitive instanceof DERSequence) {
                    DERSequence derSequence = (DERSequence) primitive;
                    System.out.println("DERSequence:"+derSequence.toString());
                }else if(primitive instanceof DLSequence) {
                    DLSequence dlSequence = (DLSequence) primitive;
                    System.out.println("dlSequence:"+dlSequence.toString());
                    if(dlSequence.toString().contains("#")){
                        IVStr = dlSequence.toString();
                        IVStr = IVStr.substring(IVStr.indexOf("#")+1,IVStr.indexOf("]"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                ais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSHA256Value(String str){
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byteArray2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    public static void main(String[] args) {
//        parseASN1("30819D0201013181973081941370413031313242315458303045303130304B5331384646464630303030303030303031453030303030414332313038323435443442443733373445443932464142363838373438314544363034344137453635433239463132393739383931384441394434353631443235324143414641020102040AFFFF0000000001E00000020100130A4473707265616442444B04021D58");
//        String ap = "308205A906092A864886F70D010702A082059A30820596020149310D300B06096086480165030402013082032506092A864886F70D010703A082031604820312020100318201FB308201F70201003081A830819C310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311B3019060355040C0C12447370726561642044657669636573204341311B301906035504410C12447370726561642044657669636573204341311B301906035504030C1244737072656164204465766963657320434102074EB0D600009880304306092A864886F70D0101073036300B0609608648016503040201301806092A864886F70D010108300B0609608648016503040201300D06092A864886F70D01010904000482010092583A07F6280625EE4CA043E3245F2CD6CCA8BAE6E198F4046A5DDE055723D2591A84DDCA4D7F7BB1B179881FD9EC4E33ED22333A9008DAEB3C3B1D7143D1953F2363BEA4C0D2592667C3468F228F856A95A6DCA1FA9CA0AB05D25DC612E7E2BF2AE3012D22C78BB7224C8C8E02146929937C3DF9FA3589B2A486C132477ACFA50BE09528FCBFDA43079AF54C050843BE4BDE701D246D8D8A4C947F12AFD97A66010459BBAE4ED627F687CC3E6DC30B5B35FE3564D9FB07F501B57A73A70AB9C3398E14391B16A5FE45C374984219F0B3A3265A82D3F5A48CEEF3998DCEA59F1CC5821B51605C66C8FD2687778C84B51CCE51C1FBFA876F978E0A9546C425FF3082010C06092A864886F70D010701301406082A864886F70D03070408C8FA8F2094E103118081E85816DF38AEC7C0E569C011DB7212278A767C8934770C7E994E9508E256B693973FBB4B47A78A9F6B1AB2D326CC2A76A53E3731B8A8128B1DE4BEDCCA51E0E740C1A474C21C8CF4A4726F4FBE0DC5CE41C4DB7A2CDBB2EF7B2C0F61B50E34A1A327A5069EB23524DB0D8119C4C407B90277B806288ECAC2826AF8AF6D092B29E90C03554986F38345B6BB247BC1498C2185661BDE318ADECAF199E798D70A058305F686ECC3A267D28EED6052483401EB5B5B84F897CAEA7968B8EEAB23F465CE3F1E7F7F7E402D1AA681D76D34CF9EC0B6BBBE9A513B8C42E5EA5319E218AC996F87767966DBD8F8318202573082025302014930819C308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B444820434102074EB0D60000987E300B0609608648016503040201A0818E301806092A864886F70D010903310B06092A864886F70D0107033020060A2A864886F70D01091903311204104CDCEDD916AAACEEAE548A1C5B0A0EAA301F06092A864886F70D0107013112041041303031364B30544E30304530303030302F06092A864886F70D01090431220420A0E06A133DA8D4A5EC5A2E51E468B470B19E13834019A0C2563BA39308660A1F300D06092A864886F70D0101010500048201003BA0F51DC5B3400E5CD29429663008713C3B61DE0C053590296421635218AEB228A1802C971B18CCF0A137D66FE07B08A0B2A592F11557CC401C353C859E1B82C4BAE146F8AC2955BD1326A3482B173E5589B321FBA0517DCA071F120D0940DC7B8CD33C861E1403CCBD7C3203F1609D261D38B415A0BF234CC9370D18B1004D89BE4C7C4631C7A5D3A1010F0371E25F70B8000D5B94C946571D0F6A730DEF57950AED18839B38B0FF6497D03E960194CF3F113C57575F62E8299FCDE855A1BD36ECE5CAF3DC9F942387A76A329715EC09FDBED3C4FACA06160D538EC00D0166D46152D61F6C665F749E91A0E70E532CE726525B946ACD81510FF47146F00994";
//        ap = ap.replace("A081","3081");
//        parseASN1new(ap);
//        System.out.println("en = "+encryptData);
//        System.out.println("sign = "+signData);
//        nonce = nonce.substring(nonce.indexOf("#")+1,nonce.indexOf("]"));
//        header = header.substring(header.indexOf("#")+1,header.indexOf("]"));
//        digest = digest.substring(digest.indexOf("#")+1,digest.indexOf("]"));
//        System.out.println("nonce = "+nonce);
//        System.out.println("header = "+header);
//        System.out.println("digest = "+digest);
//
//        parseASN1new(encryptData.substring(6));
//        System.out.println("IVStr = "+IVStr);
//        System.out.println("encryptDataWith3Des = "+encryptDataWith3Des);
//        System.out.println("final en = "+encryptData);
//        String s = "30819D0201013181973081941370413031313242315458303045303130304B5331384646464630303030303030303031453030303030414332313038323435443442443733373445443932464142363838373438314544363034344137453635433239463132393739383931384441394434353631443235324143414641020102040AFFFF0000000001E00000020100130A4473707265616442444B04021D58";
//        parseASN1new(s);
//        System.out.println("tr31 = "+tr31Data.substring(1));
        String a = "308203B33082029BA00302010202074EB0D60000987E300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330363030303030305A170D3330303330373030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D56311D301B06035504410C14447370726561645F417573524B4D533130312D56311D301B06035504030C14447370726561645F417573524B4D533130312D5630820122300D06092A864886F70D01010105000382010F003082010A0282010100D7FD40DD513EE82491FABA3EB734C3FE69C79973797007A2183EC9C468F73D8E1CB669DDA6DC32CA125F9FAEAC0C0556893C9196FB123B06BC9B880EEF367CD17000C7E0ECF7313DD2D396F29C8D977A65946258BE5A4133462F0675161407EED3D263BC20E9271B9070DCC1A6376F89E7E9E2B304BC756E3E3B61B869A2E39F11067D00B5BA3817673A730F42DC4C037FC214207C70A1E3E43F7D7494E71EBDD5BB0E9AFAE32E422DB90B85E230DF406FB12470AD0360FD7BDFDD1A29BCE91655A835129858A0E9EB04845A80F1E9F8EAA20C67C6B8A61113D6FFDD7DF5719778A03A30F69B0DD9033D5E975F723CC18792CC6988250A7DBD20901450651A810203010001300D06092A864886F70D01010B050003820101008F002AE3AFB49C2E7D99CC7B933617D180CB4E8EA13CBCBE7469FC4E5124033F06E4C3B0DAB3C6CA4625E3CD53E7B86C247CDF100E266059366F8FEEC746507E1B0D0924029805AAB89FCE1482946B8B0C1F546DD56B399AB48891B731148C878EF4D02AE641717A3D381C7B62011B76A6FFBF20846217EB68149C96B4B134F980060A542DBE2F32BF7AD308F26A279B41C65E32D4E260AE68B3010685CE36869EFF09D211CE64401F417A72F29F49A2EE713ACC37C29AECBFEBE571EF11D883815F54FA3E52A917CC3D6B008A3E3C52164FF5591D869026D248873F15DE531104F329C279FC5B6BC28ABC833F8C31BEF47783A5D5B9C534A57530D9AE463DC3";
        getServerPubkey(a);
    }

    public static String parseToken(String rkmsResponse,String tag){
        String response = rkmsResponse.substring(1,rkmsResponse.length()-1);
        System.out.println("spilt:"+response);
        String[] split = response.split(";");
        System.out.println("spilt:"+ Arrays.toString(split));
        if (split.length != 0) {
            for (int i = 0; i < split.length; i++) {
                System.out.println("data:"+split[i]);
                if(split[i].substring(0,2).equals(tag)){
                    return split[i].substring(2);
                }
            }
        }
        return null;
    }

    public static String addTagToCommand(String command,String tag,String value){
        command = command.substring(0,command.length()-2)+";"+tag+value+";]";
        return command;
    }

    public static String generateNonce() {
        try {
            SecureRandom localSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes_key = new byte[16];
            localSecureRandom.nextBytes(bytes_key);
            String nonce = QPOSUtil.byteArray2Hex(bytes_key);
            return nonce;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
