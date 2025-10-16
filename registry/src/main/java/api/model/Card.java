package api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "cards")
@Entity
public class Card {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false) // ID карты
  private String paymentMethodId;

  @Column(name = "stripe_customer_id")
  private String stripeCustomerId;

  @Column
  private String brand; // Visa, MasterCard

  @Column
  private String last4; // последние 4 цифры карты

  @Column
  private Long expMonth;

  @Column
  private Long expYear;
}
