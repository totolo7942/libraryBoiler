package utils.big.reader.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CollectBO implements Cloneable{
    private long prdNo;
    private String partnerCd;
    private String modelNo;
    private String modelSubNo;
    private long minPrice;
    private long min2ndPrice;
    private String minPriceYn;
    private String modelNm;
    private Date createDt;
    private Date updateDt;
    private String addKey;

    private long mobileMinPrice;
    private long mobileMin2ndPrice;
    private String mobileMinPriceYn;
    private String modelMngTypCd;		// 모델관리유형코드(MT204)
    private long deliveryCost;
}
