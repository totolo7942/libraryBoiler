package utils.big.reader.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverAttrModelBO {
    private String attrName;
    private String attrId;
    private long attrLowestPrice;
    private long attrProductCount;
    private NaverProductListBO attrProductList;
    private NaverProductListBO mallProductList;
}
