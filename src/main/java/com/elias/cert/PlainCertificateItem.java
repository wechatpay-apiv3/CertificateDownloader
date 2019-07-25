package com.elias.cert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlainCertificateItem {

    private String serialNo;

    private OffsetDateTime effectiveTime;

    private OffsetDateTime expireTime;

    private String plainCertificate;
}
