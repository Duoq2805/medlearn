package com.duoq.medlearn.flashcard.mapper;

import com.duoq.medlearn.flashcard.dto.response.FlashcardDeckResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardResponse;
import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import com.duoq.medlearn.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface FlashcardMapper {

    @Mapping(target = "createdBy", source = "createdBy.id")
    FlashcardDeckResponse toDeckResponse(FlashcardDeck deck);

    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "deckId", source = "deck.id")
    @Mapping(target = "deckTitle", source = "deck.title")
    FlashcardResponse toResponse(Flashcard flashcard);

    default String deckTitle(com.duoq.medlearn.flashcard.entity.FlashcardDeck deck) {
        return deck != null ? deck.getTitle() : null;
    }
}
