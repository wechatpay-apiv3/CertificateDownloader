package com.elias.cert;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author: chen
 * @date: 2019/7/24
 **/
@Data
public class CertificateItem {
    @SerializedName("serial_no")
    private String serialNo;

    @SerializedName("effective_time")
    private OffsetDateTime effectiveTime;

    @SerializedName("expire_time")
    private OffsetDateTime expireTime;

    @SerializedName("encrypt_certificate")
    private EncryptedCertificateItem encryptCertificate;
}

