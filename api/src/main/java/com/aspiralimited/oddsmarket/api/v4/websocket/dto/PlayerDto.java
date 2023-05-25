package com.aspiralimited.oddsmarket.api.v4.websocket.dto;

import lombok.AllArgsConstructor;

import java.util.List;

import static com.aspiralimited.oddsmarket.api.v4.websocket.dto.ValueReader.asInt;

@AllArgsConstructor
public class PlayerDto {

    public int id;
    public String name;

    public PlayerDto(List<?> values, List<String> fields) {
        for (int i = 0; i < values.size(); i++) {
            String field = fields.get(i);
            Object value = values.get(i);

            if (value == null) continue;

            switch (field) {
                case "id":
                    this.id = asInt(value);
                    break;

                case "name":
                    this.name = value.toString();
                    break;

                default:
                    System.out.println("unknown field: " + field + "[" + value + "]");
            }
        }
    }

    @Override
    public String toString() {
        return "PlayerDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
