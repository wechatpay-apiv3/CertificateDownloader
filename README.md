# Certificate Downloader

Certificate Downloader 是 Java 微信支付 APIv3 平台证书的命令行下载工具。该工具可从 `https://api.mch.weixin.qq.com/v3/certificates` 接口获取商户可用证书，并使用 [APIv3 密钥](https://wechatpay-api.gitbook.io/wechatpay-api-v3/ren-zheng/api-v3-mi-yao) 和 AES_256_GCM 算法进行解密，并把解密后证书下载到指定位置。

该工具使用了 [wechatpay-apache-httpclient]()、[Maven](https://github.com/apache/maven)、[picocli](https://github.com/remkop/picocli)、[gson]()、[lombok](https://github.com/rzwitserloot/lombok) 等库。

## 使用

该工具已经通过 Maven 打包成 CertificateDownloader.jar，可在 [release ](https://github.com/EliasZzz/CertificateDownloader/releases) 中下载。

执行  `java -jar CertificateDownloader.jar -h `，查看帮助：

![1564047129669](images/help.png)

这里，必需参数有：

- 商户的私钥文件，即 `-f`
- 证书解密的密钥，即 `-k`
- 商户号，即 `-m`
- 保存证书的路径，即 `-o`
- 商户证书的序列号，即 `-s`

非必需参数有：

- 微信支付证书，用于验签，即 `-c` 

完整命令如：

```
java -jar CertificateDownloader.jar -k ${apiV3key} -m ${mchId} -f ${mchPrivateKeyFilePath} -s ${mchSerialNo} -o ${outputFilePath} -c ${wechatpayCertificateFilePath}
```

## 常见问题

### 第一次下载证书

对于微信支付平台的应答，需要使用平台证书来进行验签；但平台证书只能通过 [获取平台证书接口](https://wechatpay-api.gitbook.io/wechatpay-api-v3/jie-kou-wen-dang/ping-tai-zheng-shu#huo-qu-ping-tai-zheng-shu-lie-biao) 下载，所以当第一次去获取证书时，会出现个“死循环”。

为解决这个“死循环”，可以临时跳过验签，来获得证书。也就是说可以不提供微信支付证书参数（-c 参数）来下载，在下载得到证书后，**工具会使用证书对报文的签名进行验证**，**如果通过则说明证书正确**。

## 参考

- [微信支付 wechatpay-apache-httpclient 文档](https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient/blob/master/README.md)
- [微信支付 APIv3 文档](https://wechatpay-api.gitbook.io/wechatpay-api-v3/)
- [Picocli 文档](https://github.com/remkop/picocli/blob/master/README.md)
- [Gson 文档](https://github.com/google/gson/blob/master/README.md)
