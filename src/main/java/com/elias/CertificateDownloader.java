package com.elias;


import com.elias.cert.CertificateItem;
import com.elias.cert.CertificateList;
import com.elias.cert.PlainCertificateItem;
import com.elias.util.JsonUtils;
import com.wechat.pay.contrib.apache.httpclient.Validator;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.CertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sun.security.x509.X509CertImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: chen
 * @date: 2019/7/24
 **/

@Command(name = "微信支付平台证书下载工具", mixinStandardHelpOptions = true, version = "1.0.0")
public class CertificateDownloader implements Runnable {
    @Option(names = {"-k", "--key"}, description = "证书解密的密钥", required = true)
    private String apiV3key;

    @Option(names = {"-m", "--mchid"}, description = "商户号", required = true)
    private String merchantId;

    @Option(names = {"-f", "--privatekey"}, description = "商户私钥文件", required = true)
    private String privateKeyFilePath;

    @Option(names = {"-s", "--serialno"}, description = "商户证书序列号", required = true)
    private String serialNo;

    @Option(names = {"-o", "--output"}, description = "证书保存路径", required = true)
    private String outputFilePath;

    @Option(names = {"-c", "--wechatpay-cert"}, description = "微信支付平台证书，用于验签")
    private String wechatpayCertificatePath;

    private static final String CertDownloadPath = "https://api.mch.weixin.qq.com/v3/certificates";

    @Override
    public void run() {
        System.out.printf("apiV3key=[%s]%n", apiV3key);
        System.out.printf("privateKey file path=[%s]%n", privateKeyFilePath);
        System.out.printf("merchant's certificate serial number=[%s]%n", serialNo);

        try {
            System.out.println("=== download begin ===");
            List<PlainCertificateItem> plainCerts = downloadCertificate();
            System.out.println("=== download done ===");

            if (plainCerts != null) {
                System.out.println("=== save begin ===");
                saveCertificate(plainCerts);
                System.out.println("=== save done ===");
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private List<PlainCertificateItem> downloadCertificate() throws IOException, GeneralSecurityException {
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(merchantId, serialNo, PemUtil.loadPrivateKey(new FileInputStream(privateKeyFilePath)));

        if (wechatpayCertificatePath == null) {
            //不做验签
            builder.withValidator(response -> true);
        } else {
            List<X509Certificate> certs = new ArrayList<>();
            certs.add(PemUtil.loadCertificate(new FileInputStream(wechatpayCertificatePath)));
            builder.withWechatpay(certs);
        }

        HttpGet httpGet = new HttpGet(CertDownloadPath);
        httpGet.addHeader("Accept", "application/json");

        try (CloseableHttpClient client = builder.build()) {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());
            if (statusCode == 200) {
                System.out.println("body:" + body);
                CertificateList certList = JsonUtils.convertJsonToCertList(body);
                return decryptAndValidate(certList, response);
            } else {
                System.out.println("download failed,resp code=" + statusCode + ",body=" + body);
                throw new IOException("request failed");
            }
        }
    }

    private List<PlainCertificateItem> decryptAndValidate(CertificateList certList, CloseableHttpResponse response) throws GeneralSecurityException, IOException {
        List<PlainCertificateItem> plainCerts = new ArrayList<>();
        List<X509Certificate> x509Certs = new ArrayList<>();
        AesUtil decryptor = new AesUtil(apiV3key.getBytes(StandardCharsets.UTF_8));
        for (CertificateItem item : certList.getCerts()) {
            PlainCertificateItem plainCert = new PlainCertificateItem(
                    item.getSerialNo(), item.getEffectiveTime(), item.getExpireTime(),
                    decryptor.decryptToString(
                            item.getEncryptCertificate().getAssociatedData().getBytes(StandardCharsets.UTF_8),
                            item.getEncryptCertificate().getNonce().getBytes(StandardCharsets.UTF_8),
                            item.getEncryptCertificate().getCiphertext()));
            System.out.println(plainCert.getPlainCertificate());

            ByteArrayInputStream inputStream = new ByteArrayInputStream(plainCert.getPlainCertificate().getBytes(StandardCharsets.UTF_8));
            X509Certificate x509Cert = PemUtil.loadCertificate(inputStream);
            plainCerts.add(plainCert);
            x509Certs.add(x509Cert);
        }

        //从下载的证书中，获取对报文签名的私钥所对应的证书，并进行验签来验证证书准确
        Verifier verifier = new CertificatesVerifier(x509Certs);
        Validator validator = new WechatPay2Validator(verifier);
        boolean isCorrectCert = validator.validate(response);
        System.out.println(isCorrectCert ? "=== validate success ===" : "=== validate failed ===");
        return isCorrectCert ? plainCerts : null;
    }

    private void saveCertificate(List<PlainCertificateItem> cert) throws IOException {
        File file = new File(outputFilePath);
        file.mkdirs();

        for (PlainCertificateItem item : cert) {
            String outputAbsoluteFilename = file.getAbsolutePath() + File.separator + "wechatpay_" + item.getSerialNo() + ".pem";
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outputAbsoluteFilename), StandardCharsets.UTF_8))) {
                writer.write(item.getPlainCertificate());
            }

            System.out.println("save cert file absolute path = " + outputAbsoluteFilename);
        }
    }


    public static void main(String[] args) {
        CommandLine.run(new CertificateDownloader(), args);
    }
}