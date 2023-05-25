package com.aspiralimited.oddsmarket.client.v4.websocket.handlers.statekeeping.model;

import com.aspiralimited.oddsmarket.api.v4.websocket.dto.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class Player {

    private final int id;
    private final String name;

    public static Player of(PlayerDto dto) {
        return new Player(dto.id, dto.name);
    }
}
