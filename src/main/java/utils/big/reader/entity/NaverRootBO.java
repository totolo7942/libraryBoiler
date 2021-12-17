package utils.big.reader.entity;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@XmlRootElement(name="modelProductList")
@Getter
@Setter
public class NaverRootBO {
    private List<NaverModelBO> modelProduct;
}
