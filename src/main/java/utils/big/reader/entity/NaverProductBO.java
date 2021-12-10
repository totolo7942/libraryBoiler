package utils.big.reader.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverProductBO {
    private String mallPid;				//상품번호
    private long ranking;				//순위
    private long price;					//상품가격
    private String nvMid;				//네이버 상품번호
    private String mallId;				//몰명
    private long deliveryCost;			//배송비
}
