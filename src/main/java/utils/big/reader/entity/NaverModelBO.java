package utils.big.reader.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverModelBO {
    private String matchNvMid;					//원부코드
    private String isPopularModel;			//중분류별 인기 상우 100 여부
    private String productName;					//원부명
    private String cateCode;					//카테고리코드
    private String cateName;						//카테고리명
    private String fullCateCode;				//전체 카테고리코드
    private String fullCateName;					//전체 카테고리명
    private long lowestPrice;						//최저가
    private String lowestPriceDevice;			//최저가 가격의 디바이스구분
    private long productCount;				//상품수
    private String makerName;					//제조사명
    private String brandName;					//브랜드명
    private String useAttr;						//하위 원부 존재 여부
    private String modelType;		// 원부타입 (MANUAL : 수동원부, AUTO : 자동원부)

    private NaverProductListBO lowestProductList;
    private NaverProductListBO mallProductList;
    private NaverProductListBO lowestProductListByMall;
    private NaverAttrRootBO attrList;

}
