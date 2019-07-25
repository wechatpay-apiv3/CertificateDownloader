package com.elias.cert;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: chen
 * @date: 2019/7/24
 **/
@Data
public class CertificateList {
    @SerializedName("data")
    private List<CertificateItem> certs = new ArrayList<>();
}

