package com.elias.test;

import com.elias.CertificateDownloader;
import org.junit.Test;
import picocli.CommandLine;

/**
 * @author: chen
 * @date: 2019/7/25
 **/

public class CertificateDownloaderTest {
    //用于证书解密的密钥
    private String apiV3key = "";
    // 商户号
    private static String mchId = "";
    // 商户证书序列号
    private static String mchSerialNo = "";
    // 商户私钥
    private static String mchPrivateKeyFilePath = "";
    // 微信支付平台证书
    private static String wechatpayCertificateFilePath = "";
    //下载成功后保存证书的路径
    private static String outputFilePath = "";

    @Test
    public void testCertDownload() {
        String[] args = {"-k", apiV3key, "-m", mchId, "-f", mchPrivateKeyFilePath,
                "-s", mchSerialNo, "-o", outputFilePath, "-c", wechatpayCertificateFilePath};
        CommandLine.run(new CertificateDownloader(), args);
    }

}
