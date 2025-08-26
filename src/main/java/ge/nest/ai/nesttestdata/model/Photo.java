package ge.nest.ai.nesttestdata.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("photo")
public record Photo(String url) {
}
