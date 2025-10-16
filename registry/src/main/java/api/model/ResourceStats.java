package api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "resource_stats")
@AllArgsConstructor
@NoArgsConstructor
public class ResourceStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private Integer cpu;

  @Column
  private Integer ram;

  @Column
  private Integer traffic;

  @Column
  private Double cpuCost;

  @Column
  private Double gbCost;

  @Column
  private Double trafficCost;
}

