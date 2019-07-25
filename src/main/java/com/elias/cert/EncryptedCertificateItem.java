package com.elias.cert;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author: chen
 * @date: 2019/7/24
 **/
@Data
public class EncryptedCertificateItem {
    @SerializedName("algorithm")
    private String algorithm;

    @SerializedName("nonce")
    private String nonce;

    @SerializedName("associated_data")
    private String associatedData;

    @SerializedName("ciphertext")
    private String ciphertext;
}

