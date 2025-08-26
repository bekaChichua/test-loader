package ge.nest.ai.nesttestdata.model;

import ge.nest.commons.valueobjects.spaces.SpaceType;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Table("space")
@Data
public class Space {
    private final SpaceType type;
    private final double area;

    public Space(SpaceType type, double area) {
        this.type = type;
        this.area = area < 0 ? 5 : area;
    }
}
