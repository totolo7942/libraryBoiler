package utils.big.reader.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="modelProductList")
@Getter
@Setter
public class NaverRootBO {
    private List<NaverModelBO> modelProduct;
}
