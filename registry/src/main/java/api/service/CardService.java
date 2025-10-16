package api.service;

import api.dto.card.AddCardRequest;
import api.dto.card.CardResponse;
import api.model.Card;
import api.repository.CardRepository;
import api.util.StripeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CardService {
  private final CardRepository cardRepository;
  private final StripeProvider stripeProvider;

  public CardResponse addCard(AddCardRequest request) throws Exception {
    Card card = stripeProvider.attachCard(request.getPaymentMethodId());
    return mapToDto(card);
  }

  public List<CardResponse> getCards() {
    return cardRepository.findAll()
        .stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  private CardResponse mapToDto(Card card) {
    return new CardResponse(
        card.getId(),
        card.getBrand(),
        card.getLast4(),
        card.getExpMonth(),
        card.getExpYear());
  }
}
